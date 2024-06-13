<template>
  <div>
    <div class="fabric" ref="fabric">
      <div style="display: inline-block; margin-left: 10px">
        <button @click="drawLine">선 그리기</button>
        <button id="drawing-mode" ref="drawingModeEl" class="btn btn-info" @click="drawingMode">그리기 모드 취소</button>
        <br>
        <button id="clear-canvas" class="btn btn-info" @click="clearCanvas">지우기</button>
        <br>
        <div id="drawing-mode-options" ref="drawingOptionsEl">
          <label for="drawing-mode-selector">모드:</label>
          <select v-model="selectDrawingMode" id="drawing-mode-selector" @change="drawingModeSelector">
            <option v-for="item in DrawingModeList" :key="item.name">
              {{ item.name }}
            </option>
          </select>
          <br>
          <label for="drawing-line-width">선 굵기:</label>
          <span class="info">{{ lineWidth }}</span>
          <input type="range" value="50" min="0" max="200" id="drawing-line-width" ref="drawingLineWidthEl" @change="drawingLineWidthChange">
          <br>
          <label for="drawing-color">선 색상:</label>
          <input type="color" id="drawing-color" ref="drawingColorEl" @change="drawingColorChange">
          <br>
        </div>
      </div>
      <pre style="white-space: pre">
        <button @click="addNewBox">사각형</button>
        <button @click="addCircle">원</button>
        <button @click="toDraw">그리기</button>
      </pre>
      <canvas ref="canvasRef" width="500" height="500"></canvas>
    </div>
  </div>
</template>

<script>
import { fabric } from 'fabric';
import Stomp from 'webstomp-client';
import SockJS from 'sockjs-client';
import { onMounted, ref } from "vue";

var canvas = null;

export default {
  name: "app",
  data() {
    return {
      selectDrawingMode: '',
      DrawingModeList: [
        { name: "모드 선택", value: "" },
        { name: "연필", value: "pencil" },
        { name: "수평선", value: "hline" },
      ],
      lineWidth: 1,
      recvList: [],
      stompClient: null,
      connected: false,
    }
  },
  methods: {
    initializeWebSocket() {
      const serverURL = "http://localhost:8080";
      let socket = new SockJS(serverURL);
      this.stompClient = Stomp.over(socket);
      this.stompClient.connect({}, frame => {
        this.connected = true;
        console.log('웹소켓 연결 성공', frame);

        this.stompClient.subscribe("/send", (data) => {
          const receivedMessage = JSON.parse(data.body);
          this.handleIncomingDrawingData(receivedMessage);
        });
      });
    },
    handleIncomingDrawingData(data) {
      if (data && data.path && data.options) {
        const path = new fabric.Path(data.path, data.options);
        canvas.add(path);
      }
    },
    sendDrawingData(path, options) {
      if (this.stompClient && this.stompClient.connected) {
        this.stompClient.send(`/receive`, JSON.stringify({ path, options }), {});
      }
    },
    drawLine() {
      // 선 그리기 로직 구현
    },
    addNewBox() {
      const rect = new fabric.Rect({
        top: 100,
        left: 100,
        width: 50,
        height: 50,
        fill: "#f3f345",
        corner: 100,
        angle: 360,
        borderColor: "#3845ff",
        cornerColor: "#000000",
        cornerSize: 8,
      });
      canvas.add(rect);
      canvas.setActiveObject(rect);
      canvas.isDrawingMode = false;
    },
    addCircle() {
      const circle = new fabric.Circle({
        radius: 40,
        fill: "green",
        top: 300,
        left: 300
      });
      canvas.add(circle);
      canvas.setActiveObject(circle);
      canvas.isDrawingMode = false;
    },
    toDraw() {
      canvas.isDrawingMode = true;
      this.drawingModeSelector();
    },
    clearCanvas() {
      canvas.clear();
    },
    drawingMode() {
      const drawingModeEl = this.$refs.drawingModeEl;
      const drawingOptionsEl = this.$refs.drawingOptionsEl;
      canvas.isDrawingMode = !canvas.isDrawingMode;
      drawingModeEl.innerHTML = canvas.isDrawingMode ? '그리기 모드 취소' : '그리기 모드';
      drawingOptionsEl.style.display = canvas.isDrawingMode ? '' : 'none';
    },
    drawingModeSelector() {
      const selectedMode = this.selectDrawingMode;
      const brush = canvas.freeDrawingBrush;

      if (selectedMode === 'pencil') {
        brush.color = this.$refs.drawingColorEl.value;
        brush.width = parseInt(this.$refs.drawingLineWidthEl.value, 10) || 1;
      } else if (selectedMode === 'hline') {
        // 수평선 그리기 모드 구현
      }
    },
    drawingColorChange() {
      const brush = canvas.freeDrawingBrush;
      brush.color = this.$refs.drawingColorEl.value;
    },
    drawingLineWidthChange() {
      const brush = canvas.freeDrawingBrush;
      brush.width = parseInt(this.$refs.drawingLineWidthEl.value, 10) || 1;
    },
  },
  mounted() {
    canvas = new fabric.Canvas(this.$refs.canvasRef, { isDrawingMode: true });
    this.initializeWebSocket();

    canvas.on('path:created', (event) => {
      const path = event.path.path;
      const options = {
        left: event.path.left,
        top: event.path.top,
        fill: event.path.fill,
        stroke: event.path.stroke,
        strokeWidth: event.path.strokeWidth,
        strokeLineCap: event.path.strokeLineCap,
        strokeLineJoin: event.path.strokeLineJoin,
      };
      this.sendDrawingData(path, options);
      console.log("path : ",path , "options : ",options)
    });
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
  border-radius: 8px;
}
pre {
  width: 100px;
  height: 100px;
  text-align: center;
}
</style>
