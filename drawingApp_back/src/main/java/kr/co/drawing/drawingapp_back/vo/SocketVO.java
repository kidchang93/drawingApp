package kr.co.drawing.drawingapp_back.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SocketVO {
    private String userName;
    private String content;
    private Object obj;
}
