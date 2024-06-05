<!--
<template>
<div id="app">
  <canvas ref="canvas"
          width="800"
          height="800"
          @mousedown="startDrawing"
          @mousemove="draw"
          @mouseup="stopDrawing"
          @mouseout="stopDrawing">
  </canvas>
  <div id="designTool">
    <button @click="buttonErase">모두 지우기</button>
    <button @click="increaseThickness">두껍게</button>
    <button @click="decreaseThickness">얇게</button>
  </div>
</div>
</template>

<script>
export default {
  name: "App",
  data(){
    return {
      drawing: false,
      context: null,
      lastX: 0,
      lastY: 0,
    }
  },
  mounted() {
    this.initCanvas();
  },
  methods: {
    // 캔버스 세팅
    initCanvas(){
      const canvas = this.$refs.canvas;
      this.context = canvas.getContext("2d");
      this.context.strokeStyle = "#000000";
      this.context.lineWidth = 10;
      this.context.lineCap = "round";
      console.log("initCanvas")
    },
    // 그리기 시작 함수
    startDrawing(e){
      this.drawing = true;
      this.lastX = e.offsetX;
      this.lastY = e.offsetY;
      console.log("startDrawing")
    },
    // 그리기 함수
    draw(e){
      if (!this.drawing) return;
      this.context.beginPath();
      this.context.moveTo(this.lastX, this.lastY);
      this.context.lineTo(e.offsetX, e.offsetY);
      this.context.stroke();
      this.lastX = e.offsetX;
      this.lastY = e.offsetY;
      console.log("draw", this.lastX, this.lastY)
    },
    // 그리기 스탑
    stopDrawing(){
      this.drawing = false;
    },
    // 지우개 버튼
    buttonErase(){
      this.context.clearRect(0,0, this.$refs.canvas.width, this.$refs.canvas.height);
    },
    // 두껍게 버튼
    increaseThickness(){
      const canvas = this.$refs.canvas;
      this.context = canvas.getContext("2d");
      this.context.lineWidth += 1;
      console.log("지금 두께 : ",this.context.lineWidth);
    },
    // 얇게 버튼
    decreaseThickness(){
      const canvas = this.$refs.canvas;
      this.context = canvas.getContext("2d");
      if (this.context.lineWidth > 1){
        this.context.lineWidth -= 1;
        console.log("지금 두께 : ",this.context.lineWidth);
      }
    }

  }


}
</script>

<style scoped>

</style>
-->
