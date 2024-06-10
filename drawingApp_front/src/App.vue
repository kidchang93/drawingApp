<template>
  <div>

    <div class="fabric">
      <pre style="white-space: pre">
        <button @click="addNewBox">사각형</button>
        <button @click="addCircle">원</button>
        <button @click="toDraw">그리기</button>
      </pre>
      <canvas ref="canvasRef" width="500" height="500" >
        <div v-for="item in recvList" :key="item.canvasState">
          {{ canvasState }}
        </div>
      </canvas>
    </div>
  </div>
</template>

<script>
import { fabric } from 'fabric';
import work from './work.json';

import Stomp from 'webstomp-client'
import SockJS from 'sockjs-client'


var canvas = null;

export default {
  name: "app",
  data() {
    return {
      recvList:[],
      canvasState:{}
    }
  },
  created(){
    // App.vue가 생성되면 소켓 연결을 시도.
    this.connect();
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

        const msg = {
          userName: this.userName,
          content: this.message,
          canvasState: newValue,
        };

        this.stompClient.send("/receive", JSON.stringify(msg), {});
        // console.log("버퍼사이즈 : ", this.stompClient.on.BUFFER_SIZE)
      }

      console.log("newValue : " + newValue);
      console.log("oldValue : " + oldValue);

    }
  },
  // Mounted
  mounted() {

    canvas = new fabric.Canvas(this.$refs.canvasRef, {
      isDrawingMode: false

    })

    const toWatchWhenCanvasChanged = () => {
      this.canvasState = JSON.stringify(canvas.toObject());
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