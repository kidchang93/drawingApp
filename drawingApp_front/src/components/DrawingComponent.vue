<template>
  <div class="fabric">
    <div class="control-panel">
      <div class="color-panel">
        <button @click="setColor('black')" style="background-color: black; color: white;">까망</button>
        <button @click="setColor('blue')" style="background-color: blue; color: white;" >파랑</button>
        <button @click="setColor('red')" style="background-color: red; color: white;" >빨강</button>
      </div>
      <div class="color-panel">
        <button @click="toggleDrawing" :icon="drawingIcon" severity="secondary" size="large" text="" raised="" rounded="" aria-label="Toggle Drawing" />
        <button @click="clearCanvasAndNotify" icon="pi pi-eraser" severity="secondary" size="large" text="" raised="" rounded="" aria-label="Clear Canvas" />
      </div>
    </div>
    <div class="content-wrapper">
      <canvas ref="canvasRef" :style="{ zIndex: isDrawingEnabled ? 1 : 0 }" width="800" height="800"></canvas>
      <div class="card">
        <slot name="background"></slot>
      </div>
    </div>
  </div>
</template>

<script>
import {ref} from "vue";

export default {
  name: "DrawingComponent",

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
    const socketRef = ref(null);
    const isDrawingEnabled = ref(true);
    const color = ref('red');
    const isDrawing = ref(false);
    const previousPoint = ref(null);

    const getCanvasDataURL = () => {
      canvasRef.value.toDataURL("image/png");
    }

    const drawImageFromURL = (url) =>{
      const context = canvasRef.value.getContext("2d");
      const image = new Image();
      image.src = url;
      image.onload = () => {
        context.drawImage(image, 0, 0);
      }
    }

    const drawLine = ({ ctx, prevPoint, currentPoint, color}) => {

      const {x, y} = currentPoint;
      const startPoint = prevPoint ?? currentPoint;  // ??

      ctx.beginPath();
      ctx.lineWidth = 5;
      ctx.strokeStyle = color;
      ctx.moveTo(startPoint.x , startPoint.y);
      ctx.lineTo(x, y);
      ctx.stroke();

      ctx.fillStyle = color;
      ctx.beginPath();
      ctx.arc(startPoint.x, startPoint.y, 2, 0, 2 * Math.PI);
      ctx.fill();

    };

    const clearCanvas = () => {
      const context = canvasRef.value.getContext("2d");
      context.clearRect(0, 0, canvasRef.value.width, canvasRef.value.height);
    }

    const startDrawing = () => {
      isDrawing.value = true;
    }

    const stopDrawing = () => {
      isDrawing.value = false;
      previousPoint.value = null;
      const dataUrl = getCanvasDataURL();
      sendMessage({
        type: 'DRAW_END',
        data: dataUrl
      })
    }

    const handleMouseMove = (event) => {
      if (!isDrawing.value) return;

      const canvas = event.target;
      const currentPoint = {
        x: event.offsetX,
        y: event.offsetY
      }

      const drawData = {
        ctx: canvas.getContext("2d"),
        prevPoint: previousPoint.value,
        currentPoint: currentPoint,
        color: color.value,
      }

    }

    return {}
  }
}
</script>

<style>
.fabric {
  width: 800px;
  height: 800px;
  display: flex;
  align-items: center;
  justify-content: center;
}
canvas {
  border: 1px solid #42b983;
  background-color: green;
  border-radius: 8px ;
}
</style>