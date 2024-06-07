package kr.co.drawing.drawingapp_back.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SocketVO {
    private String userName;
    private String content;
    private String canvasState;
}
