# Quick Start: AWS SQS + Spring Boot Processing FIFO Queues

![picture alt](https://faizakram.com/git-hub/aws-sqs/AWS-SQS.png "Aws Sqs")

AWS SQS (Simple Queue Service) can provide developers with flexibility and scalability when building microservice application(s). In this quick start tutorial, we will demonstrate how to configure a FIFO queue for a fictional online marketplace.

## What Is A FIFO Queue? ##
A FIFO (first in, first out) queue is used when the order of items or events is critical, or where duplicates items in the queue are not permitted.

## Configuring AWS ##

This tutorial assumes you have an AWS account already created. Begin by creating an SQS queue call **PendingOrders.fifo** in a region of your choice with **content-based deduplication** enabled. Each queue has its own unique URL.

The format of this URL is https://sqs.[region].amazonaws.com/[account_id]/[queue_name]. Take note of this URL, as it will need it to run the application (Set this in a **SIXTHPOINT_SQSURL** environment variable). You can see the URL for your SQS in the details screen of SQS in AWS console or by using the AWS CLI command **aws sqs list-queues**.

![picture alt](https://faizakram.com/git-hub/aws-sqs/sqs.png "Aws sqs")

## Content-Based Deduplication: ##
FIFO queues do not allow for duplicate messages. If you retry to send an identical message within the 5-minute deduplication interval, the message will be tossed out. This assures that your messages will be processed exactly once. So by enabling this feature, AWS SQS uses an SHA-256 hash to generate the deduplication ID using the body of the message; not the attributes of the message.

This is useful for our simple example because our message payload will contain only a **itemCount** which is the total number of items they are buying, and a **requestTime** which is the epoch time the order was received.

## The Application ##
The sample application is composed of a single **OrderProcessingService** which is stateful. This service handles submitting the order to the queue, as well as listening for orders via the **@SqsListener** annotation.

## Creating An Order ##
To create an order a **PendingOrder** class is written to a single JSON string. The default client for **AmazonSQSClientBuilder** is then used to send a message to the queue.

```
@Override
public void createOrder(int itemCount) {
	try {
	PendingOrder pendingOrder = new PendingOrder();
	pendingOrder.setItemCount(itemCount);
	pendingOrder.setRequestTime(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
	String pendingOrderJson = objectMapper.writeValueAsString(pendingOrder);
	amazonSQSAsync.sendMessage(new SendMessageRequest(endpoint, pendingOrderJson)
					.withMessageGroupId("groupdId"));
	} catch (final AmazonClientException | JsonProcessingException ase) {
		log.error("Error Message: " + ase.getMessage());
	}
}
```
The **PendingOrder** contains an **itemCount** which is the total number of items they are trying to purchase, and a **requestTime**. Since we are using content-based deduplication, this means that no more than one request can be done per second with the same **itemCount**.

## Processing The Queue ## 
Using the **@SqsListener** annotation, the application checks the **PendingOrders.fifo** periodically to process pending items. An item is read in and mapped using the object mapper. The **availableItems** is either decremented by the supplied **pendingOrder** count or an error logged for no more items remaining.

```
private int availableItems = 5;
@SqsListener("pendingorders.fifo")
	public void process(String json) throws IOException {
		PendingOrder pendingOrder = objectMapper.readValue(json, PendingOrder.class);
		log.info("Items purchased, now have {} items remaining", availableItems);
		if (availableItems > 0 && availableItems >= pendingOrder.getItemCount()) {
			availableItems = availableItems - pendingOrder.getItemCount();
			log.info("Items purchased, now have {} items remaining", availableItems);
		} else {
			log.error("No more items are available");
		}
	}
```

