package spring.boot.aws.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.stereotype.Service;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import spring.boot.aws.payload.PendingOrder;

@Service
public class OrderProcessingServiceImpl implements OrderProcessingService {

	private static final Logger log = LoggerFactory.getLogger(OrderProcessingServiceImpl.class);

	
	@Autowired
	private ObjectMapper objectMapper;
	
	private int availableItems = 5;

	@Autowired
	private AmazonSQSAsync amazonSQSAsync;
	
	@Value("${cloud.aws.fifo.que.end.point}")
	private String endpoint;

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
}
