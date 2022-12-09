package com.parasoft.demoapp.config.kafka;

import com.parasoft.demoapp.dto.InventoryOperationRequestMessageDTO;
import com.parasoft.demoapp.dto.InventoryOperationResultMessageDTO;
import lombok.Getter;
import lombok.Setter;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Getter
public class KafkaConfig {
    public static final int ADMIN_CLIENT_TIMEOUT_MS = 5000;

    public static final String DEFAULT_ORDER_SERVICE_REQUEST_TOPIC = "inventory.request";
    public static final String DEFAULT_ORDER_SERVICE_RESPONSE_TOPIC = "inventory.response";

    @Getter private static final String inventoryServiceListenToTopic = DEFAULT_ORDER_SERVICE_REQUEST_TOPIC;
    @Getter private static final String inventoryServiceSendToTopic = DEFAULT_ORDER_SERVICE_RESPONSE_TOPIC;
    @Getter @Setter private static String orderServiceSendToTopic = DEFAULT_ORDER_SERVICE_REQUEST_TOPIC;
    @Getter @Setter private static String orderServiceListenToTopic = DEFAULT_ORDER_SERVICE_RESPONSE_TOPIC;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    public Map<String, Object> adminClientConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers);
        return props;
    }

    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                JsonSerializer.class);
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 2000);
        return props;
    }

    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.GROUP_ID_CONFIG,
                groupId);
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                JsonDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return props;
    }

    @Bean
    public ProducerFactory<String, InventoryOperationRequestMessageDTO> operationRequestProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public ConsumerFactory<String, InventoryOperationRequestMessageDTO> operationRequestConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs(),
                new StringDeserializer(),
                new JsonDeserializer<>(InventoryOperationRequestMessageDTO.class));
    }

    @Bean
    public ProducerFactory<String, InventoryOperationResultMessageDTO> operationResultProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public ConsumerFactory<String, InventoryOperationResultMessageDTO> operationResultConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs(),
                new StringDeserializer(),
                new JsonDeserializer<>(InventoryOperationResultMessageDTO.class));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, InventoryOperationRequestMessageDTO> operationRequestContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, InventoryOperationRequestMessageDTO> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(operationRequestConsumerFactory());
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, InventoryOperationResultMessageDTO> operationResultContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, InventoryOperationResultMessageDTO> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(operationResultConsumerFactory());
        return factory;
    }

    @Bean
    public KafkaTemplate<String, InventoryOperationRequestMessageDTO> operationRequestKafkaTemplate() {
        return new KafkaTemplate<>(operationRequestProducerFactory());
    }

    @Bean
    public KafkaTemplate<String, InventoryOperationResultMessageDTO> operationResultKafkaTemplate() {
        return new KafkaTemplate<>(operationResultProducerFactory());
    }
}