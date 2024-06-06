package kr.co.drawing.drawingapp_back.rest;

import kr.co.drawing.drawingapp_back.vo.SocketVO;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class SocketController {


    @MessageMapping("/receive")     // receive로 메세지를 받을 EndPoint로 설정.
    @SendTo("/send")             // /send로 메세지를 반환.
    public SocketVO socketHandler(SocketVO socketVO){
        // socketHandler는 /receive에서 메세지를 받고 /send로 메세지를 보냄.
        // 정의한 SocketVO를 인자값, 반환값으로 사용.
        String userName = socketVO.getUserName();
        String content = socketVO.getContent();
        Object obj = socketVO.getObj();

        // 생성자로 반환값을 생성.
        SocketVO result = new SocketVO(userName, content, obj);
        return result;
    }
}
