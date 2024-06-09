package kr.co.drawing.drawingapp_back.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic drawTopic(){
        return TopicBuilder
                .name("drawApp")
                .build();
    }

}
