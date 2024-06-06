<template>
  <div class="fabric">
    <button @click="addNewBox">사각형</button>
    <button @click="addCircle">원</button>
    <button @click="toJSON">JSON</button>
    <button @click="toImage">이미지</button>
    <canvas ref="canvasRef" width="500" height="500">
    </canvas>
  </div>
</template>

<script>
import { fabric } from 'fabric';
import work from './work.json';

var canvas = null;

export default {
  name: "App",

  data() {
    return {
      count:250,
      myImage:"https://mml.pstatic.net/www/mobile/edit/20240604_1095/upload_1717474370275V7DHT.png"
    }
  },
  methods: {
    // 사각형 추가
    addNewBox(){
      this.count = this.count+10;
      // 새로운 도형의 정보
      const rect = new fabric.Rect({
        top: 100,
        left: this.count,
        width:50,
        height:50,
        fill:"#f3f345",
        corner: 100,
        angle:360,  // 기울기 고정점
        borderColor:"#3845ff",   // 선택시 테두리 색깔 표시
        cornerColor:"#000000",    // 코너 색깔 조절 툴
        cornerSize:8,             // 코너 크기 조절 툴
      })
      canvas.add(rect);
      // setActiveObject 지금 추가한 객체를 선택한 상태로 만든다.
      canvas.setActiveObject(rect);
    },
    // 원 도형 추가 메서드
    addCircle(){
      const circle = new fabric.Circle({
        radius:40,
        fill:"green",
        top:300,
        left:300
      })
      canvas.add(circle);
      canvas.setActiveObject(circle);
    },
    // 변경중(활동중)인 객체의 정보 및 상태값
    onObjectScaled(){
      const obj = canvas.getActiveObject()

      const width = obj.width
      const height = obj.height
      const scaleX = obj.scaleX
      const scaleY = obj.scaleY

      // 원하는 형태로 obj 의 상태값을 담을 수 있다.
      obj.set({
        width: width*scaleX,
        height: height*scaleY,
        scaleX:1,
        scaleY:1,
      })
      console.log("w",width,"h",height);


      // console.log("obj",obj);

    },
    async toJSON(){

      const json = canvas.toDatalessJSON(["clipPath"]);
      const out =JSON.stringify(json, null, "\t");
      const blob = new Blob([out], {type: "text/plain"});
      const clipboardItemData = {[blob.type]: blob};

      const blobURL = URL.createObjectURL(blob);
      const a = document.createElement("a")
      a.href = blobURL;
      a.download = "work.json";
      a.click();
      URL.revokeObjectURL(blobURL);
    }
  },

  // Mounted

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
      borderColor:"#3845ff",   // 선택시 테두리 색깔 표시
      cornerColor:"#000000",    // 코너 색깔 조절 툴
      cornerSize:8,             // 코너 크기 조절 툴
    })


    // 글씨 입력 박스 생성
    const textBox = new fabric.Textbox("FabricJS",{
      left: 20,
      top: 20,
      fill: "black",
      stroke: "black",
      strokeWidth: 2,
      fontSize:16
    })

    // 써진 글씨 출력박스 생성, 입력 불가
    const text = new fabric.Text("CKHUB",{
      left: 100,
      top: 20,
      fill: "black",
      stroke: "black",
      strokeWidth: 2,
      fontSize:16
    })
    // JSON 파일을 기반으로 페이지에 렌더하는 함수
    canvas.loadFromJSON(work,(e)=>{
      canvas.renderAll();
    })
    // canvas.add(rect);
    // canvas.add(text);
    // canvas.add(textBox);
    canvas.on('object:scaling',this.onObjectScaled);
  },

  // 이미지 첨부
  /*new fabric.Image.fromURL(this.myImage,(_img) => {
    const _me = _img.set({left:0, top:0, width:_img.width, height:_img.height})
    canvas.add(_me)
  })*/

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