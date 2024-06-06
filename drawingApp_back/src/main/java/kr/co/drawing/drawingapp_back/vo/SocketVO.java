package kr.co.drawing.drawingapp_back.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SocketVO {
    private String userName;
    private String content;
    private Object obj;
}
