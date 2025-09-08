package ru.job4j.kraftconsumer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import ru.job4j.core.events.ProductCreatedEvent;
import ru.job4j.core.exceptions.NonRetryableException;
import ru.job4j.core.exceptions.RetryableException;
import ru.job4j.kraftconsumer.persistence.entity.ProcessedEventEntity;
import ru.job4j.kraftconsumer.persistence.repository.ProcessEventRepository;

@Service
@KafkaListener(topics = "product-created-events-topic")
public class ProductCreatedEventService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final RestTemplate restTemplate;
    private final ProcessEventRepository processEventRepository;

    public ProductCreatedEventService(
            RestTemplate restTemplate,
            ProcessEventRepository processEventRepository) {
        this.restTemplate = restTemplate;
        this.processEventRepository = processEventRepository;
    }


    @Transactional
    @KafkaHandler
    public void handle(
                        @Payload ProductCreatedEvent productCreatedEvent,
                        @Header("messageId") String messageId,
//                        для использования ручного подтверждения
//                        Acknowledgment acknowledgment,
                        @Header(KafkaHeaders.RECEIVED_KEY) String messageKey) {
        try {
            logger.info("Received event: {}, productId: {}", productCreatedEvent.getTitle(), productCreatedEvent.getProductId());
            if (processEventRepository.findByMessId(messageId)) {
                logger.info("Duplicate message id: {}", messageId);
                return;
            }
            String url = "http://localhost:8090/response/200";
            try {
                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
                if (response.getStatusCode().value() == HttpStatus.OK.value()) {
                    logger.info("Received event: {}", response.getBody());
                }
            } catch (ResourceAccessException e) {
                logger.error(e.getMessage());
                throw new RetryableException(e);
            } catch (HttpServerErrorException e) {
                logger.error(e.getMessage());
                throw new NonRetryableException(e);
            } catch (Exception e) {
                logger.error(e.getMessage());
                throw new NonRetryableException(e);
            }
            try {
                processEventRepository.save(new ProcessedEventEntity(messageId, productCreatedEvent.getProductId()));
            } catch (DataIntegrityViolationException e) {
                logger.error(e.getMessage());
                throw new NonRetryableException(e);
            }
            // Для ручного режима подтверждаем успешную обработку сообщения
//            acknowledgment.acknowledge();
        } catch (Exception e) {
                // В случае ошибки, не подтверждаем сообщение
                // Оно будет повторно доставлено позже
                logger.error("Failed to process message: {}", messageId, e);
                throw e;
        }
    }
}
