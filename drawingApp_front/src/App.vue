<template>
  <div>

    <div class="fabric">
      <div style="display: inline-block; margin-left: 10px">

      <button @click="drawDataHandler">drawDataHandler</button>
      <button id="drawing-mode" ref="drawingModeEl" class="btn btn-info" @click="drawingMode">Cancel drawing mode</button><br>
      <button id="clear-canvas" class="btn btn-info" @click="clearCanvas">Clear</button><br>
<!--        <span class="info" v-for="item in mouseEvent" :key="item">{{ item }}</span>-->
      <div id="drawing-mode-options" ref="drawingOptionsEl">
        <label for="drawing-mode-selector">Mode : </label>
        <select v-model="selectDrawingMode" id="drawing-mode-selector" @change="drawingModeSelector">
          <option v-for="item in DrawingModeList" :key="item.name">
            {{ item.name }}
          </option>
        </select>
        <br>

        <label for="drawing-line-width">Line width:</label>
        <span class="info">{{ this.lineWidth }}</span>
        <input type="range" value="50" min="0" max="200" id="drawing-line-width" ref="drawingLineWidthEl" @change="drawingLineWidthChange"><br>

        <label for="drawing-color">Line color:</label>
        <input type="color" value="" id="drawing-color" ref="drawingColorEl" @change="drawingColorChange"><br>
<!--Shadow 쪽 버튼-->
<!--        <label for="drawing-shadow-color">Shadow color:</label>
        <input type="color" value="#005E7A" id="drawing-shadow-color" ref="drawingShadowColorEl" @change="drawingShadowColorChange"><br>

        <label for="drawing-shadow-width">Shadow width:</label>
        <span class="info">0</span>
        <input type="range" value="0" min="0" max="50" id="drawing-shadow-width" ref="drawingShadowWidth" @change="drawingShadowWidthChange"><br>

        <label for="drawing-shadow-offset">Shadow offset:</label>
        <span class="info">0</span>
        <input type="range" value="0" min="0" max="50" id="drawing-shadow-offset" ref="drawingShadowOffset" @change="drawingShadowOffsetChange"><br>-->
      </div>
    </div>
      <pre style="white-space: pre">
        <button @click="addNewBox">사각형</button>
        <button @click="addCircle">원</button>
        <button @click="toDraw">그리기</button>
      </pre>

      <canvas ref="canvasRef" width="500" height="500">
<!--        <div v-for="item in recvList" :key="item.canvasState">-->
<!--          {{ canvasState }}-->
<!--        </div>-->
      </canvas>
    </div>
  </div>
</template>

<script>
import { fabric } from 'fabric';
import work from './work.json';

import Stomp from 'webstomp-client'
import SockJS from 'sockjs-client'
import {onMounted, ref} from "vue";


var canvas = null;

// Element 의 ID 값을 불러오는 변수명 설정.
export default {
  name: "app",

  data() {
    return {
      // canvas:null,
      roomId: {
        type: String,
        required: true
      },
      webSocketServer: {
        type: String,
        required: true
      },

      selectDrawingMode: '',
      DrawingModeList: [
        {name: "선택해주세요", value: ""},
        {name: "pencil", value: "pencil"},
        {name: "hline", value: "hline"},
        // {name: "vline", value: "vline"},
        // {name: "square", value: "square"},
        // {name: "diamond", value: "diamond"},
        // {name: "texture", value: "texture"},
        // {name: "circle", value: "circle"},
        // {name: "spray", value: "spray"},
        // {name: "pattern", value: "pattern"},
      ],
      // 마우스 이벤트 변수
      mouseEvent: ['mouseOver', 'mouseDown', 'mouseMove', 'mouseUp'],

      // Draw 변수
      drawData: [],
      lineWidth: '',
      recvList: [],
      canvasState: {},
      message:[],
    }

  },

  setup() {

  },

  created() {

  },
  computed: {},
  methods: {
    // WebSocket 세팅
    initializeWebSocket() {
      const serverURL = "http://localhost:8080"
      let socket = new SockJS(serverURL);
      this.stompClient = Stomp.over(socket);

      let data = this.drawData;
      console.log("data : ", data)
      this.stompClient.connect({}, frame => {
            // 소켓 연결 성공
            this.connected = true;
            console.log('소켓 연결 성공', frame);

            // 서버의 메세지 전송 EndPoint를 구독합니다.
            // 이런 형태를 pub sub 구조라고 합니다.
            this.stompClient.subscribe("/send", JSON.parse(data))
        console.log("구독으로부터 받은 메세지 : ", data)
      });
    },

    // sendMessage 변수들이 message에 매핑되고 담아서 보내는 메서드
    sendMessage(message) {

      this.drawData = message;
      console.log("message : ",JSON.stringify(this.drawData));

      if (this.stompClient && this.stompClient.connected) {
        this.stompClient.send(`/receive`, JSON.stringify(message), {});
        console.log("send Receive : ",JSON.stringify(message));
      }
      return message;
    },

    canvasData(){

    },
    drawLine({ ctx, currentPoint, color }){
      const { x, y } = currentPoint;
      const startPoint = prevPoint ?? currentPoint;

      ctx.beginPath();
      ctx.lineWidth = 5;
      ctx.strokeStyle = color;
      ctx.moveTo(startPoint.x, startPoint.y);
      ctx.lineTo(x, y);
      ctx.stroke();

      ctx.fillStyle = color;
      ctx.beginPath();
      ctx.arc(startPoint.x, startPoint.y, 2, 0, 2 * Math.PI);
      ctx.fill();


      console.log("DrawLine : ",this.drawLine)
      return { ctx, currentPoint, color };
    },

    drawDataHandler(event){


      const ctx = this.$refs.canvasRef.getContext('2d');

      console.log("event : ",event)

      // const previousPoint = event.absolutePointer;
      //
      // console.log("previousPoint : ",previousPoint);

      const currentPoint = {
        x: event.e.offsetX,
        y: event.e.offsetY
      };

      const drawData = {
        ctx: ctx,
        // prevPoint: previousPoint,
        currentPoint: currentPoint,
        brushLineWidth: this.drawingLineWidthChange(),
        color: this.drawingColorChange(),
      };
      console.log("currentPoint",currentPoint)
      console.log("drawData : ",drawData)

      this.sendMessage(drawData)

    },

    mouseEventData(){

    },

    // 사각형 추가
    addNewBox() {
      this.count = this.count + 10;
      // 새로운 도형의 정보
      const rect = new fabric.Rect({
        top: 100,
        left: this.count,
        width: 50,
        height: 50,
        fill: "#f3f345",
        corner: 100,
        angle: 360,  // 기울기 고정점
        borderColor: "#3845ff",   // 선택시 테두리 색깔 표시
        cornerColor: "#000000",    // 코너 색깔 조절 툴
        cornerSize: 8,             // 코너 크기 조절 툴
      })
      canvas.add(rect);
      // setActiveObject 지금 추가한 객체를 선택한 상태로 만든다.
      canvas.setActiveObject(rect);
      canvas.isDrawingMode = false;
    },
    // 원 도형 추가 메서드
    addCircle() {
      const circle = new fabric.Circle({
        radius: 40,
        fill: "green",
        top: 300,
        left: 300
      })
      canvas.add(circle);
      canvas.setActiveObject(circle);
      canvas.isDrawingMode = false;
    },
    // 그리기
    toDraw() {
      canvas.isDrawingMode = true;
      // 현재 brush 상태를 가져옴
      this.drawingModeSelector();
    },

    clearCanvas() {
      canvas.clear()

    },
    // Drawing 툴 꺼낼건지 선택
    drawingMode() {

      // 각 Element 에 접근하는 선택자 refs 로 설정.
      const drawingModeEl = this.$refs.drawingModeEl;
      const drawingOptionsEl = this.$refs.drawingOptionsEl;


      canvas.isDrawingMode = !canvas.isDrawingMode;
      if (canvas.isDrawingMode) {
        drawingModeEl.innerHTML = 'Cancel drawing Mode';
        drawingOptionsEl.style.display = '';
      } else {
        drawingModeEl.innerHTML = 'Enter drawing Mode';
        drawingOptionsEl.style.display = 'none';
      }

    },
    // Pattern Brush 설정
    drawingModeSelector() {
      const selectedMode = this.selectDrawingMode;
      //
      const selectedBrush = this.DrawingModeList.find(item => item.value === selectedMode);

      const brush = canvas.freeDrawingBrush;
      const brushSource = brush.source;

      if (selectedBrush.value === '') {
        this.drawingColorChange();
        this.drawingLineWidthChange();
      } else if (selectedBrush.value === 'pencil') {
        // hLine에 대한 메서드
        console.log("pencil 선택", selectedBrush.value)
        this.drawingColorChange();
        this.drawingLineWidthChange();
        console.log("brush 속성 : ", canvas.freeDrawingBrush)
      } else if (selectedBrush.value === 'hline') {
        this.drawingColorChange();
        this.drawingLineWidthChange();
      }

    },

    drawingColorChange(brushColor) {
      brushColor = canvas.freeDrawingBrush;
      brushColor.color = this.$refs.drawingColorEl.value;
      if (brushColor.getPatternSrc) {
        brushColor.source = brushColor.getPatternSrc.call(brushColor);
      }
      console.log("brushColor",brushColor)
      return brushColor.color;
    },
    drawingLineWidthChange(brushLineWidth) {
      brushLineWidth = canvas.freeDrawingBrush;
      brushLineWidth.lineWidth = this.$refs.drawingLineWidthEl.value;
      canvas.freeDrawingBrush.width = parseInt(this.$refs.drawingLineWidthEl.value, 10) || 1;

      return brushLineWidth.lineWidth;
    },

  },

  // watch
  watch: {},
  // Mounted
  mounted() {
    canvas = new fabric.Canvas(this.$refs.canvasRef, {
      isDrawingMode: true,
    })

    this.initializeWebSocket();

    canvas.on('mouse:over',this.drawDataHandler)
    canvas.on('mouse:down', this.drawDataHandler)
    canvas.on('mouse:move', this.drawDataHandler)
    canvas.on('mouse:up', this.drawDataHandler)



    // canvas.on('mouse:over', this.drawDataHandler) ? this.mouseEvent = 'mouseOver' : null;
    // canvas.on('mouse:down', this.drawDataHandler) ? this.mouseEvent = 'mouseDown' : '';
    // canvas.on('mouse:move', this.drawDataHandler) ? this.mouseEvent = 'mouseMove' : '';
    // canvas.on('mouse:up', this.drawDataHandler) ? this.mouseEvent = 'mouseUp' : '';

    // 글씨 입력 박스 생성
    // const textBox = new fabric.Textbox("FabricJS",{
    //   left: 20,
    //   top: 20,
    //   fill: "black",
    //   stroke: "black",
    //   strokeWidth: 2,
    //   fontSize:16
    // })
    //
    // // 써진 글씨 출력박스 생성, 입력 불가
    // const text = new fabric.Text("CKHUB",{
    //   left: 100,
    //   top: 20,
    //   fill: "black",
    //   stroke: "black",
    //   strokeWidth: 2,
    //   fontSize:16
    // })

  },
}
</script>



<style scoped>
.fabric {

  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}
canvas {
  border: 1px solid #42b983;
  border-radius: 8px ;
}
pre{
  width: 100px;
  height: 100px;
  text-align: center;
}
</style>