package kr.co.drawing.drawingapp_back.rest;

import kr.co.drawing.drawingapp_back.vo.SocketVO;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaJsonProducer {

    private final KafkaTemplate<String, SocketVO> kafkaTemplate;

    public void sendMessage(SocketVO socketVO){
        log.info("KafkaProducer 진입");
        Message<SocketVO> message = MessageBuilder
                .withPayload(socketVO)
                .setHeader(KafkaHeaders.TOPIC, "drawApp")
                .build();
        log.info("KafkaProducer : " + message);
        kafkaTemplate.send(message);
    }


}
