#!/bin/bash
set -e

echo ""
echo "===================================================="
echo "[LocalStack] Criando filas SQS..."
echo "===================================================="

SQS_DIR="/opt/custom/sqs"

if [ ! -d "$SQS_DIR" ]; then
  echo "[LocalStack] Pasta $SQS_DIR não encontrada."
  exit 0
fi

CREATED_QUEUES=()

for file in "$SQS_DIR"/*.json; do
  [ -e "$file" ] || continue

  echo ""
  echo "[LocalStack] Processando arquivo: $file"

  QUEUE_NAME=$(python3 -c "
import json

with open('$file') as f:
    data = json.load(f)

print(data['QueueName'])
")

  CREATE_DLQ=$(python3 -c "
import json

with open('$file') as f:
    data = json.load(f)

print(str(data.get('CreateDlq', True)).lower())
")

  echo "[LocalStack] Queue: $QUEUE_NAME"
  echo "[LocalStack] CreateDlq: $CREATE_DLQ"

  #
  # Sem DLQ
  #
  if [ "$CREATE_DLQ" != "true" ]; then

      TMP_FILE=$(mktemp)

      python3 << EOF > "$TMP_FILE"
import json

with open("$file") as f:
    data = json.load(f)

data.pop("CreateDlq", None)

print(json.dumps(data))
EOF

      QUEUE_URL=$(awslocal sqs create-queue \
          --cli-input-json file://"$TMP_FILE" \
          --query QueueUrl \
          --output text)

      rm -f "$TMP_FILE"

      echo "[LocalStack] Fila criada sem DLQ: $QUEUE_NAME"

      CREATED_QUEUES+=("$QUEUE_NAME")

      continue
  fi

  #
  # Cria DLQ
  #
  if [[ "$QUEUE_NAME" == *.fifo ]]; then

      DLQ_NAME="${QUEUE_NAME%.fifo}-dlq.fifo"

      DLQ_URL=$(awslocal sqs create-queue \
          --queue-name "$DLQ_NAME" \
          --attributes FifoQueue=true \
          --query QueueUrl \
          --output text)

  else

      DLQ_NAME="${QUEUE_NAME}-dlq"

      DLQ_URL=$(awslocal sqs create-queue \
          --queue-name "$DLQ_NAME" \
          --query QueueUrl \
          --output text)

  fi

  echo "[LocalStack] DLQ criada: $DLQ_NAME"

  DLQ_ARN=$(awslocal sqs get-queue-attributes \
      --queue-url "$DLQ_URL" \
      --attribute-names QueueArn \
      --query 'Attributes.QueueArn' \
      --output text)

  echo "[LocalStack] DLQ ARN: $DLQ_ARN"

  TMP_FILE=$(mktemp)

  python3 << EOF > "$TMP_FILE"
import json

with open("$file") as f:
    data = json.load(f)

data.pop("CreateDlq", None)

attrs = data.setdefault("Attributes", {})

attrs["RedrivePolicy"] = json.dumps({
    "deadLetterTargetArn": "$DLQ_ARN",
    "maxReceiveCount": "5"
})

print(json.dumps(data))
EOF

  QUEUE_URL=$(awslocal sqs create-queue \
      --cli-input-json file://"$TMP_FILE" \
      --query QueueUrl \
      --output text)

  rm -f "$TMP_FILE"

  echo "[LocalStack] Fila criada com DLQ: $QUEUE_NAME"

  CREATED_QUEUES+=("$QUEUE_NAME")

done

echo ""
echo "===================================================="
echo "[LocalStack] FILAS SQS CRIADAS:"
echo "===================================================="

for q in "${CREATED_QUEUES[@]}"; do
  echo " - $q"
done

echo ""
echo "===================================================="
echo "[LocalStack] Listagem de filas via API:"
echo "===================================================="

awslocal sqs list-queues

echo ""
echo "===================================================="
echo "[LocalStack] Fim!"
echo "===================================================="