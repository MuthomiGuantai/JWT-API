package com.springjwt.Messages;

import com.springjwt.messaging.ProductMessageConsumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductMessageConsumerTest {

    @Mock
    private Logger logger;

    private ProductMessageConsumer productMessageConsumer;
    private MockedStatic<LoggerFactory> loggerFactoryMock;

    @BeforeEach
    void setUp() {
        loggerFactoryMock = mockStatic(LoggerFactory.class);
        loggerFactoryMock.when(() -> LoggerFactory.getLogger(ProductMessageConsumer.class))
                .thenReturn(logger);
        productMessageConsumer = new ProductMessageConsumer();
    }

    @AfterEach
    void tearDown() {
        loggerFactoryMock.close();
    }

    @Test
    void testReceiveMessage_LogsMessageSuccessfully() {
        String message = "Created product: Laptop by consumer@example.com";
        productMessageConsumer.receiveMessage(message);
        verify(logger, times(1)).info("Received message from product-queue: {}", message);
    }

    @Test
    void testReceiveMessage_EmptyMessage() {
        String message = "";
        productMessageConsumer.receiveMessage(message);
        verify(logger, times(1)).info("Received message from product-queue: {}", message);
    }

    @Test
    void testReceiveMessage_NullMessage() {
        String message = null;
        productMessageConsumer.receiveMessage(message);
        verify(logger, times(1)).info("Received message from product-queue: {}", (Object) null);
    }

    @Test
    void testReceiveMessage_CustomQueueName() {
        System.setProperty("consumer.queue.name", "custom-product-queue");
        String message = "Created product: Smartphone by user@example.com";
        productMessageConsumer.receiveMessage(message);
        verify(logger, times(1)).info("Received message from custom-product-queue: {}", message);
        System.clearProperty("consumer.queue.name");
    }
}
