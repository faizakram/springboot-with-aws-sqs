package spring.boot.aws;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class AwsConfiguration {

	@Value("${cloud.aws.credentials.access-key}")
	private String awsAccessKey;

	@Value("${cloud.aws.credentials.secret-key}")
	private String awsSecretKey;

	@Bean
	public ObjectMapper getObjectMapper() {
		return new ObjectMapper();
	}

	@Bean
	@Primary
	public AmazonSQSAsync amazonSQSAsync() {
		return AmazonSQSAsyncClientBuilder.standard().withRegion(Regions.AP_SOUTH_1)
				.withCredentials(new AWSStaticCredentialsProvider(
						new BasicAWSCredentials(awsAccessKey, awsSecretKey)))
				.build();
	}
}
