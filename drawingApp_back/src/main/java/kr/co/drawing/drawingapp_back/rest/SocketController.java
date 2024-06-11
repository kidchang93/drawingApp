package kr.co.drawing.drawingapp_back.rest;

import com.google.gson.Gson;
import kr.co.drawing.drawingapp_back.vo.SocketVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;


import java.io.IOException;


@Controller
@RequiredArgsConstructor
@Slf4j
public class SocketController {

    private static Gson gson = new Gson();

    private final KafkaJsonProducer kafkaJsonProducer;

    @MessageMapping("/receive")     // receive로 메세지를 받을 EndPoint로 설정.
    @SendTo("/send")             // /send로 메세지를 반환.
    public SocketVO socketHandler(SocketVO socketVO) {
        // socketHandler는 /receive에서 메세지를 받고 /send로 메세지를 보냄.
        // 정의한 SocketVO를 인자값, 반환값으로 사용.
        String userName = socketVO.getUserName();
        String content = socketVO.getContent();
        String canvasState = socketVO.getCanvasState();
        String type = socketVO.getType();
        log.info("userName: {} , content: {} , canvas: {} : ", userName,content,canvasState);

        // 생성자로 반환값을 생성.
        SocketVO result = new SocketVO(type, canvasState);
        kafkaJsonProducer.sendMessage(result);
        return result;
    }

}
