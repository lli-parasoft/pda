package com.parasoft.demoapp.config.rabbitmq;

import com.parasoft.demoapp.dto.InventoryOperationRequestMessageDTO;
import com.parasoft.demoapp.dto.InventoryOperationResultMessageDTO;
import com.parasoft.demoapp.service.OrderMQService;
import com.parasoft.demoapp.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.stereotype.Component;

import static org.springframework.amqp.core.Address.AMQ_RABBITMQ_REPLY_TO;
import static org.springframework.kafka.support.mapping.AbstractJavaTypeMapper.DEFAULT_CLASSID_FIELD_NAME;

@Slf4j
@Component
public class RabbitMQInventoryResponseQueueListener extends RabbitMQRefreshableMessageListener {

    private final MessageConverter rabbitMqMessageConverter;
    private final OrderService orderService;
    private final OrderMQService orderMQService;

    public RabbitMQInventoryResponseQueueListener(MessageConverter rabbitMqMessageConverter,
                                                  OrderService orderService,
                                                  OrderMQService orderMQService,
                                                  SimpleRabbitListenerContainerFactory simpleRabbitListenerContainerFactory) {
        super(simpleRabbitListenerContainerFactory,
                RabbitMQConfig.getOrderServiceListenToQueue());

        this.rabbitMqMessageConverter = rabbitMqMessageConverter;
        this.orderService = orderService;
        this.orderMQService = orderMQService;
    }

    @Override
    public void onMessage(Message message) {
        message.getMessageProperties().setHeader(DEFAULT_CLASSID_FIELD_NAME, InventoryOperationResultMessageDTO.class.getName());
        Object object = rabbitMqMessageConverter.fromMessage(message);

        log.info("Order service receives a message from RabbitMQ queue: {} \n Message content: {}", message.getMessageProperties().getConsumerQueue(), object);

        InventoryOperationRequestMessageDTO messageToReply = orderService.handleMessageFromResponse((InventoryOperationResultMessageDTO)object);

        if(messageToReply == null) {
            log.info("Order service has no response message to reply.");
            return;
        }

        String replyTo = message.getMessageProperties().getReplyTo();
        if (replyTo != null) {
            orderMQService.sendToAmqRabbitMqReplyToQueue(messageToReply, replyTo);
        } else {
            orderMQService.sendToInventoryRequestDestination(messageToReply);
        }
    }
}
