package org.example.cloud.config;

import io.awspring.cloud.sqs.config.SqsMessageListenerContainerFactory;
import io.awspring.cloud.sqs.support.converter.SqsHeaderMapper;
import io.awspring.cloud.sqs.support.converter.SqsMessagingMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

//
//import io.awspring.cloud.sqs.listener.SqsMessageListenerContainer;
//import io.awspring.cloud.sqs.support.converter.MessagingMessageConverter;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.messaging.Message;
//import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
//import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
//import software.amazon.awssdk.regions.Region;
//import software.amazon.awssdk.services.sqs.SqsAsyncClient;
//import io.awspring.cloud.sqs.operations.SqsTemplate;
//
//import java.net.URI;
//import java.util.UUID;
//
@Configuration
public class AwsSQSConfig {

    @Value("${spring.cloud.aws.credentials.access-key}")
    private String accessKey;

    @Value("${spring.cloud.aws.credentials.secret-key}")
    private String secretKey;

    @Value("${spring.cloud.aws.region.static}")
    private String region;

    @Bean
    public SqsHeaderMapper headerMapper() {
        return new CustomHeaderMapper();
    }

    @Bean
    SqsMessageListenerContainerFactory<Object> defaultSqsListenerContainerFactory(SqsAsyncClient sqsAsyncClient) {
        SqsMessageListenerContainerFactory<Object> factory = new SqsMessageListenerContainerFactory<>();
        factory.setSqsAsyncClient(sqsAsyncClient);

        SqsMessagingMessageConverter messageConverter = new SqsMessagingMessageConverter();

        messageConverter.setHeaderMapper(new CustomHeaderMapper());



        factory.configure(options -> options.messageConverter(messageConverter));
        return factory;
    }

}