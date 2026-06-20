package br.com.scheiner.aws.console.dashboard.model;

import br.com.scheiner.aws.console.resource.model.ServiceStatus;

public class DashboardSummary {

	private int sqsQueueCount;
	private int dynamoTableCount;
	private int snsTopicCount;
	private long redisKeyCount;
	private ServiceStatus localstackStatus;
	private ServiceStatus redisStatus;
	private String localstackEndpoint;
	private String redisHost;

	public int getSqsQueueCount() {
		return this.sqsQueueCount;
	}

	public void setSqsQueueCount(int sqsQueueCount) {
		this.sqsQueueCount = sqsQueueCount;
	}

	public int getDynamoTableCount() {
		return this.dynamoTableCount;
	}

	public void setDynamoTableCount(int dynamoTableCount) {
		this.dynamoTableCount = dynamoTableCount;
	}

	public int getSnsTopicCount() {
		return this.snsTopicCount;
	}

	public void setSnsTopicCount(int snsTopicCount) {
		this.snsTopicCount = snsTopicCount;
	}

	public long getRedisKeyCount() {
		return this.redisKeyCount;
	}

	public void setRedisKeyCount(long redisKeyCount) {
		this.redisKeyCount = redisKeyCount;
	}

	public ServiceStatus getLocalstackStatus() {
		return this.localstackStatus;
	}

	public void setLocalstackStatus(ServiceStatus localstackStatus) {
		this.localstackStatus = localstackStatus;
	}

	public ServiceStatus getRedisStatus() {
		return this.redisStatus;
	}

	public void setRedisStatus(ServiceStatus redisStatus) {
		this.redisStatus = redisStatus;
	}

	public String getLocalstackEndpoint() {
		return this.localstackEndpoint;
	}

	public void setLocalstackEndpoint(String localstackEndpoint) {
		this.localstackEndpoint = localstackEndpoint;
	}

	public String getRedisHost() {
		return this.redisHost;
	}

	public void setRedisHost(String redisHost) {
		this.redisHost = redisHost;
	}

	public boolean isLocalstackConnected() {
		return this.localstackStatus != null && this.localstackStatus.isConnected();
	}

	public boolean isRedisConnected() {
		return this.redisStatus != null && this.redisStatus.isConnected();
	}
}
