<template>
  <div class="fabric">
    <div class="control-panel">
      <div class="color-panel">
        <button @click="setColor('black')" class="color-button" style="background-color: black;"></button>
        <button @click="setColor('blue')" class="color-button" style="background-color: blue;"></button>
        <button @click="setColor('red')" class="color-button" style="background-color: red;"></button>
      </div>
      <div class="color-panel">
        <button @click="toggleDrawing" class="control-button">
          {{ drawingIcon }}
        </button>
        <button @click="clearCanvasAndNotify" class="control-button">
          Eraser
        </button>
      </div>
    </div>
    <div class="content-wrapper">
      <canvas ref="canvasRef" :style="{ zIndex: isDrawingEnabled ? 1 : 0 }" width="800" height="800">

      </canvas>

    </div>
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted, computed } from 'vue';
import SockJS from 'sockjs-client';
import Stomp from 'webstomp-client';

export default {
  name: 'DrawingComponent',
  props: {
    roomId: {
      type: String,
      required: true
    },
    webSocketServer: {
      type: String,
      required: true
    }
  },
  setup(props) {
    const canvasRef = ref(null);
    const stompClient = ref(null);
    const isDrawingEnabled = ref(true);
    const color = ref('red');
    const isDrawing = ref(false);
    const previousPoint = ref(null);

    const getCanvasDataURL = () => canvasRef.value.toDataURL('image/png');

    const drawImageFromURL = (url) => {
      const context = canvasRef.value.getContext('2d');
      const image = new Image();
      image.src = url;
      image.onload = () => {
        context.drawImage(image, 0, 0);
      };
    };

    const drawLine = ({ ctx, prevPoint, currentPoint, color }) => {
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
    };

    const clearCanvas = () => {
      const context = canvasRef.value.getContext('2d');
      context.clearRect(0, 0, canvasRef.value.width, canvasRef.value.height);
    };

    const startDrawing = () => {
      isDrawing.value = true;
    };

    const stopDrawing = () => {
      isDrawing.value = false;
      previousPoint.value = null;
      const dataUrl = getCanvasDataURL();
      sendMessage({
        type: 'DRAW_END',
        data: dataUrl
      });
    };

    const handleMouseMove = (event) => {
      if (!isDrawing.value) return;

      const canvas = event.target;
      const currentPoint = {
        x: event.offsetX,
        y: event.offsetY
      };

      const drawData = {
        ctx: canvas.getContext('2d'),
        prevPoint: previousPoint.value,
        currentPoint: currentPoint,
        color: color.value
      };

      sendMessage({
        type: 'DRAW',
        data: JSON.stringify(drawData)
      });
      drawLine(drawData);
      console.log("drawData : ", drawData)
      previousPoint.value = currentPoint;
    };

    const sendMessage = (message) => {
      if (stompClient.value && stompClient.value.connected) {
        stompClient.value.send(`/receive`, JSON.stringify(message), {});
        console.log("send Receive : ",JSON.stringify(message));
      }
    };

    const handleDrawEvent = (drawData) => {
      const parsedData = JSON.parse(drawData);
      parsedData.ctx = canvasRef.value.getContext('2d');
      drawLine(parsedData);
      console.log("parsedData : ",parsedData)
    };

    const handleMessage = (message) => {
      const { data, type } = JSON.parse(message.body);
      if (type === 'DRAW') {
        handleDrawEvent(data);
      } else if (type === 'CLEAR') {
        clearCanvas();
      }
    };

    const initializeWebSocket = () => {
      const socket = new SockJS("http://localhost:8080");
      stompClient.value = Stomp.over(socket);

      stompClient.value.connect({}, () => {
        stompClient.value.subscribe("/send", handleMessage);
        sendMessage({
          type: 'JOIN',
          data: props.roomId
        });
      });
    };

    const clearCanvasAndNotify = () => {
      clearCanvas();
      sendMessage({
        type: 'CLEAR',
        data: ''
      });
    };

    const setColor = (newColor) => {
      color.value = newColor;
    };

    const toggleDrawing = () => {
      isDrawingEnabled.value = !isDrawingEnabled.value;
    };

    const drawingIcon = computed(() => (isDrawingEnabled.value ? 'Stop Drawing' : 'Start Drawing'));

    onMounted(() => {
      canvasRef.value.width = canvasRef.value.offsetWidth;
      canvasRef.value.height = canvasRef.value.offsetHeight;
      canvasRef.value.addEventListener('mousedown', startDrawing);
      canvasRef.value.addEventListener('mouseup', stopDrawing);
      canvasRef.value.addEventListener('mousemove', handleMouseMove);

      initializeWebSocket();
    });

    onUnmounted(() => {
      if (stompClient.value) {
        stompClient.value.disconnect();
      }
      canvasRef.value.removeEventListener('mousedown', startDrawing);
      canvasRef.value.removeEventListener('mouseup', stopDrawing);
      canvasRef.value.removeEventListener('mousemove', handleMouseMove);
    });

    return {
      canvasRef,
      isDrawingEnabled,
      color,
      setColor,
      clearCanvasAndNotify,
      toggleDrawing,
      drawingIcon
    };
  }
};
</script>

<style scoped>
.control-panel {
  display: flex;
  justify-content: space-between;

}
.color-panel {
  display: flex;
  gap: 10px;

}
.color-button {
  width: 40px;
  height: 40px;
  border: none;
  border-radius: 50%;
  cursor: pointer;
}
.control-button {
  padding: 10px;
  border: none;
  cursor: pointer;
}
.content-wrapper {
  position: relative;
  border: 1px black solid;

}
.card {
  position: absolute;
  top: 0;
  left: 0;
  border: 1px black solid;
}
</style>