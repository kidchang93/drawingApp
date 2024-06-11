<template>
  <div>

    <div class="fabric">
      <div style="display: inline-block; margin-left: 10px">


      <button id="drawing-mode" ref="drawingModeEl" class="btn btn-info" @click="drawingMode">Cancel drawing mode</button><br>
      <button id="clear-canvas" class="btn btn-info" @click="clearCanvas">Clear</button><br>

      <div id="drawing-mode-options" ref="drawingOptionsEl">
        <label for="drawing-mode-selector">Mode:</label>
        <select v-model="selectDrawingMode" id="drawing-mode-selector" @change="drawingModeSelector">
          <option v-for="item in DrawingModeList" :key="item.name">
            {{ item.name }}
          </option>
        </select>
        <br>

        <label for="drawing-line-width">Line width:</label>
        <span class="info">30</span>
        <input type="range" value="30" min="0" max="150" id="drawing-line-width" ref="drawingLineWidthEl" @change="drawingLineWidthChange"><br>

        <label for="drawing-color">Line color:</label>
        <input type="color" value="#005E7A" id="drawing-color" ref="drawingColorEl" @change="drawingColorChange"><br>
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

      <canvas ref="canvasRef" width="500" height="500" >
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
import {onMounted} from "vue";


var canvas = null;

// Element 의 ID 값을 불러오는 변수명 설정.
export default {
  name: "app",

  data() {
    return {
      selectDrawingMode:'',
      DrawingModeList:[
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

      recvList:[],
      canvasState:{},

    }

  },

  setup(){

  },

  created(){
    // App.vue가 생성되면 소켓 연결을 시도.
    this.connect();
  },
  computed:{

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
      canvas.isDrawingMode = false;
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
      canvas.isDrawingMode = false;
    },
    // 그리기
    toDraw(){
      canvas.isDrawingMode = true;

    },

    clearCanvas(){
      canvas.clear()

    },
    // Drawing 툴 꺼낼건지 선택
    drawingMode(){

      // 각 Element 에 접근하는 선택자 refs 로 설정.
      const drawingModeEl = this.$refs.drawingModeEl;
      const drawingOptionsEl = this.$refs.drawingOptionsEl;


      canvas.isDrawingMode = !canvas.isDrawingMode;
      if (canvas.isDrawingMode){
        drawingModeEl.innerHTML = 'Cancel drawing Mode';
        drawingOptionsEl.style.display = '';
      } else {
        drawingModeEl.innerHTML = 'Enter drawing Mode';
        drawingOptionsEl.style.display = 'none';
      }

    },
    // Pattern Brush 설정
    drawingModeSelector(){
      const selectedMode = this.selectDrawingMode;
      const selectedBrush = this.DrawingModeList.find(item => item.value === selectedMode);

      if (selectedBrush.value === 'pencil') {
        // hLine에 대한 메서드
        console.log("pencil 선택",selectedBrush.value)
        this.drawingColorChange();
        this.drawingLineWidthChange();
        console.log("brush 속성 : ", canvas.freeDrawingBrush)
      }

    },

    drawingColorChange(){
      const brush = canvas.freeDrawingBrush;
      brush.color = this.$refs.drawingColorEl.value;
      if (brush.getPatternSrc) {
        brush.source = brush.getPatternSrc.call(brush);
      }
    },
    drawingLineWidthChange(){
      const brush = canvas.freeDrawingBrush;
      brush.lineWidth = this.$refs.drawingLineWidthEl.value;
      canvas.freeDrawingBrush.width = parseInt(this.$refs.drawingLineWidthEl.value, 10) || 1;
    },

    // 웹소켓 연결
    connect() {
      const serverURL = "http://localhost:8080"
      let socket = new SockJS(serverURL);
      this.stompClient = Stomp.over(socket);

      console.log("소켓 연결을 시도합니다. 서버 주소 : " + JSON.stringify(socket.url))

      this.stompClient.connect(
          {},
          frame => {
            // 소켓 연결 성공
            this.connected = true;
            console.log('소켓 연결 성공', frame);

            // 서버의 메세지 전송 EndPoint를 구독합니다.
            // 이런 형태를 pub sub 구조라고 합니다.
            this.stompClient.subscribe("/send", res => {
              console.log('구독으로 받은 메세지 입니다.', res.body);

              // 받은 데이터를 json으로 파싱하고 리스트에 넣어줍니다.
              const receivedData = JSON.parse(res.body);
              this.recvList.push(receivedData);

              // updateCanvas를 이용해서 렌더링 한다.
              this.updateCanvas(receivedData.canvasState);
              // console.log("받았니? : " , this.recvList)

            })
          },
          error => {
            // 소켓 연결 실패
            console.log("소켓 연결 실패" + error);
            this.connected = false;
          }
      );
    },

    updateCanvas(canvasState) {
      canvas.loadFromJSON(canvasState, () => {
        canvas.renderAll();
      });
    },

  },

  // watch
  watch:{
    canvasState(newValue, oldValue){
      if (this.stompClient && this.stompClient.connected){

        const toDatalessJSON = canvas.toDatalessJSON().objects[0];
        const canvasObject = canvas.toObject();
        console.log("toDatalessJSON : ",toDatalessJSON);
        console.log("canvasObject : ",canvasObject);

        const msg = {
          userName: this.userName,
          content: this.message,
          canvasState: newValue,
        };

        this.stompClient.send("/receive", JSON.stringify(msg), {});
      }

      console.log("newValue : " + newValue);
      console.log("oldValue : " + oldValue);

    }
  },
  // Mounted
  mounted() {

    canvas = new fabric.Canvas(this.$refs.canvasRef, {
      isDrawingMode: true,
    })


    const toWatchWhenCanvasChanged = () => {
      this.canvasState = JSON.stringify(canvas.toDatalessJSON().objects[0]);

    }

    // 객체 추가시 상태값 변경해야 watch에서 감시하고 로직 수행
    canvas.on('object:added', () => {
      toWatchWhenCanvasChanged();
    });

    canvas.on('object:removed', () => {
      toWatchWhenCanvasChanged();
    });

    canvas.on('object:modified', () => {
      toWatchWhenCanvasChanged();
    });




    // 마우스 이벤트 발생 시
    // canvas.on('mouse:down', () => {
    //   toWatchWhenCanvasChanged();
    // })
    // canvas.on('mouse:move', () => {
    //   toWatchWhenCanvasChanged();
    // })
    //
    // canvas.off('mouse:up', () => {
    //   toWatchWhenCanvasChanged();
    // })

    // 그리기 이벤트 정보
    // const logCanvasState = () => {
    //   const canvasState = JSON.stringify(canvas.toObject());
    //
    //   const msg = {
    //     userName: this.userName,
    //     content: this.message,
    //     canvasState: canvasState,
    //   };
    //   if (this.stompClient && this.stompClient.connected){
    //     this.stompClient.send("/receive", JSON.stringify(msg), {});
    //   }
    //
    //   console.log("canvasState : " + canvasState.objects);
    // }
    // canvas.on('mouse:down', (e) => {
    //   console.log('MouseDown : ', e.pointer);
    //   this.watch.canvasState();
    // })
    // canvas.on('mouse:up', (e) => {
    //   console.log('MouseUp : ', e.pointer);
    //   canvas.off('mouse:move', logCanvasState(e.pointer));
    // })




    //
    // canvas.on('object:scaling',this.onObjectScaled);


    /* -- 예제를 위한 도구들 마운트될 때 표시

    // 이미지 첨부할 때는 CORS 정책에 위반되므로 다운로드가 안된다.
    // 고로 이미지 첨부시엔 CORS 정책을 포함하여 설정해준다.
    new fabric.Image.fromURL(this.myImage,(_img) => {
      const _me = _img.set({left:0, top:0, width:_img.width, height:_img.height})
      canvas.add(_me)
    },{ crossOrigin: 'anonymous' })

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
    // canvas.loadFromJSON(work,(e)=>{
    //   canvas.renderAll();
    // })

    // 캔버스에 추가하는 도구들
    canvas.add(rect);
    canvas.add(text);
    canvas.add(textBox);
    canvas.on('object:scaling',this.onObjectScaled);
  },

  */

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
pre{
  width: 100px;
  height: 100px;
  text-align: center;
}
</style>