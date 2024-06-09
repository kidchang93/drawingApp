package kr.co.drawing.drawingapp_back.rest;

import kr.co.drawing.drawingapp_back.vo.SocketVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumer {



    @KafkaListener(topics = "drawApp", groupId = "myGroup")
    public void consumeJsonMsg(SocketVO socketVO) {

        log.info("Consuming the message from ckhub Topic :: " + socketVO.toString());
    }

}
