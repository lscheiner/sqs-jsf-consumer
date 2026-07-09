#!/bin/bash
set -e

echo ""
echo "===================================================="
echo "[LocalStack] Criando topicos SNS..."
echo "===================================================="

SNS_DIR="/opt/custom/sns"

if [ ! -d "$SNS_DIR" ]; then
  echo "[LocalStack] Pasta $SNS_DIR nao encontrada."
  exit 0
fi

CREATED_TOPICS=()

for file in "$SNS_DIR"/*.json; do
  [ -e "$file" ] || continue

  echo ""
  echo "[LocalStack] Processando arquivo: $file"

  TOPIC_NAME=$(python3 -c "
import json

with open('$file') as f:
    data = json.load(f)

print(data['TopicName'])
")

  echo "[LocalStack] Topic: $TOPIC_NAME"

  TOPIC_ARN=$(awslocal sns create-topic \
      --name "$TOPIC_NAME" \
      --query TopicArn \
      --output text)

  echo "[LocalStack] Topico criado: $TOPIC_ARN"

  SUBSCRIPTIONS=$(python3 -c "
import json

with open('$file') as f:
    data = json.load(f)

for subscription in data.get('Subscriptions', []):
    print(subscription['QueueName'])
")

  for QUEUE_NAME in $SUBSCRIPTIONS; do
    echo "[LocalStack] Associando fila SQS ao topico SNS: $QUEUE_NAME"

    QUEUE_URL=$(awslocal sqs get-queue-url \
        --queue-name "$QUEUE_NAME" \
        --query QueueUrl \
        --output text)

    QUEUE_ARN=$(awslocal sqs get-queue-attributes \
        --queue-url "$QUEUE_URL" \
        --attribute-names QueueArn \
        --query 'Attributes.QueueArn' \
        --output text)

    TMP_POLICY_FILE=$(mktemp)

    python3 -c "
import json

print(json.dumps({
    'QueueUrl': '$QUEUE_URL',
    'Attributes': {
        'Policy': json.dumps({
            'Version': '2012-10-17',
            'Statement': [
                {
                    'Sid': 'AllowSnsPublish',
                    'Effect': 'Allow',
                    'Principal': {'Service': 'sns.amazonaws.com'},
                    'Action': 'sqs:SendMessage',
                    'Resource': '$QUEUE_ARN',
                    'Condition': {
                        'ArnEquals': {
                            'aws:SourceArn': '$TOPIC_ARN'
                        }
                    }
                }
            ]
        })
    }
}))
" > "$TMP_POLICY_FILE"

    awslocal sqs set-queue-attributes \
        --cli-input-json file://"$TMP_POLICY_FILE"

    rm -f "$TMP_POLICY_FILE"

    awslocal sns subscribe \
        --topic-arn "$TOPIC_ARN" \
        --protocol sqs \
        --notification-endpoint "$QUEUE_ARN"

    echo "[LocalStack] Assinatura criada: $TOPIC_NAME -> $QUEUE_NAME"
  done

  CREATED_TOPICS+=("$TOPIC_NAME")
done

echo ""
echo "===================================================="
echo "[LocalStack] TOPICOS SNS CRIADOS:"
echo "===================================================="

for topic in "${CREATED_TOPICS[@]}"; do
  echo " - $topic"
done

echo ""
echo "===================================================="
echo "[LocalStack] Listagem de topicos via API:"
echo "===================================================="

awslocal sns list-topics

echo ""
echo "===================================================="
echo "[LocalStack] Fim SNS!"
echo "===================================================="
