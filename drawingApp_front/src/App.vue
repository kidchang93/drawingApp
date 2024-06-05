<template>
  <div class="fabric">
    <button>도형</button>
    <canvas ref="canvasRef" width="500vw" height="500vh">
    </canvas>
  </div>
</template>

<script>
import { fabric } from 'fabric';
var canvas = null;
export default {
  name: "App",

  data() {
    return {}
  },
  mounted() {
    canvas= new fabric.Canvas(this.$refs.canvasRef,{
      isDrawingMode: false
    })
    const rect = new fabric.Rect({
      top: 100,
      left: 100,
      width:50,
      height:50,
      fill:"#f3f345",
      corner: 100,
      angle:360,  // 기울기 고정점
      borderColor:"#ff4445",   // 선택시 테두리 색깔 표시
      cornerColor:"#000000",    // 코너 색깔 조절 툴
      cornerSize:8,             // 코너 크기 조절 툴
    })
    canvas.add(rect);
  },


  methods: {
  }
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
</style>