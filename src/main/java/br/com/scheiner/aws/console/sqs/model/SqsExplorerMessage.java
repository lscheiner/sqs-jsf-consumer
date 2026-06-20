package br.com.scheiner.aws.console.sqs.model;

import java.util.Map;

import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;

public class SqsExplorerMessage {

	private String messageId;

	private String receiptHandle;

	private String body;

	private String bodyFormatado;

	private String sentTimestamp;

	private String receiveCount;

	private Map<String, String> attributes;

	private Map<String, MessageAttributeValue> messageAttributes;

	public String getMessageId() {
		return this.messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public String getReceiptHandle() {
		return this.receiptHandle;
	}

	public void setReceiptHandle(String receiptHandle) {
		this.receiptHandle = receiptHandle;
	}

	public String getBody() {
		return this.body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getBodyFormatado() {
		return this.bodyFormatado;
	}

	public void setBodyFormatado(String bodyFormatado) {
		this.bodyFormatado = bodyFormatado;
	}

	public String getSentTimestamp() {
		return this.sentTimestamp;
	}

	public void setSentTimestamp(String sentTimestamp) {
		this.sentTimestamp = sentTimestamp;
	}

	public String getReceiveCount() {
		return this.receiveCount;
	}

	public void setReceiveCount(String receiveCount) {
		this.receiveCount = receiveCount;
	}

	public Map<String, String> getAttributes() {
		return this.attributes;
	}

	public void setAttributes(Map<String, String> attributes) {
		this.attributes = attributes;
	}

	public Map<String, MessageAttributeValue> getMessageAttributes() {
		return this.messageAttributes;
	}

	public void setMessageAttributes(Map<String, MessageAttributeValue> messageAttributes) {
		this.messageAttributes = messageAttributes;
	}
}
