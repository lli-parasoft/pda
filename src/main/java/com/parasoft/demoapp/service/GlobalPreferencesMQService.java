package com.parasoft.demoapp.service;

import com.parasoft.demoapp.config.activemq.ActiveMQConfig;
import com.parasoft.demoapp.dto.IndustryChangeMQMessageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class GlobalPreferencesMQService {

    @Autowired
    private JmsMessagingTemplate jmsMessagingTemplate;

    public void sendToIndustryChangeTopic(IndustryChangeMQMessageDTO messageDto) {
        jmsMessagingTemplate.convertAndSend(ActiveMQConfig.TOPIC_INDUSTRY_CHANGE, messageDto);
    }
}
