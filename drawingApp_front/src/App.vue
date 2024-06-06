<template>
  <div class="fabric">
    <button @click="addNewBox">사각형</button>
    <button @click="addCircle">원</button>
    <button @click="toJSON">JSON</button>
    <button @click="toImage">이미지</button>
    <canvas ref="canvasRef" width="500" height="500" @mousedown="sendObj">
      <div v-for="(item, idx) in canvasObj" :key="idx">
        {{ item.canvasObj }}
      </div>
    </canvas>
    <hr>
    <h3>웹소켓</h3>
    <div id="app">
      유저이름 :
      <input type="text" v-model="userName">
      내용 :
      <input type="text" v-model="message" @keyup="sendMessage">
      <div v-for="(item, idx) in recvList" :key="idx">
        <h3>유저이름 : {{ item.userName }}</h3>
        <h3>내용 : {{ item.content }}</h3>
      </div>

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
      count:250,
      myImage:"https://mml.pstatic.net/www/mobile/edit/20240604_1095/upload_1717474370275V7DHT.png",
      userName:"",
      message:"",
      recvList:[],
      canvasObj:{},
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

    // JSON 파일로 현재 캔버스를 저장하는 함수
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
    },

    // 이미지 파일 저장
    // 이미지 첨부된 상태에서는 CORS 정책 위반때문에 다운로드가 안됨.
    // 이미지 첨부시 CORS 정책 명시 필수.
    toImage(){
      const ext = "png";  // 파일 형식
      const base64 = canvas.toDataURL({
        format: ext,
        enableRetinaScaling: true
      })
      const link = document.createElement("a");
      link.href = base64;
      link.download = `work.${ext}`;
      link.click();

    },
    // 웹소켓
    sendMessage(e) {
      if (e.keyCode === 13 && this.userName !== '' && this.message !== ''){
        this.send()
        this.message = '';
      }
    },
    // canvas 내용 보내기
    sendObj() {
      const canvasObj = this.canvasObj;

      this.stompClient.send("/receive",canvasObj,{});
      canvas.on('object:scaling',this.onObjectScaled);
      console.log("canvasObj : "+ canvasObj)
    },

    // 메세지 보내기
    send(){
      console.log("Send Obj : " + this.canvasObj);

      if (this.stompClient && this.stompClient.connected){
        const msg = {
          userName: this.userName,
          content: this.message,
          canvasObj: this.canvasObj,
        };
        this.stompClient.send("/receive", JSON.stringify(msg), {});
        console.log("JSON.stringify(msg) : " +JSON.stringify(msg))
      }
    },

    connect() {
      const serverURL = "http://localhost:8080"
      let socket = new SockJS(serverURL);
      this.stompClient = Stomp.over(socket);
      console.log("소켓 연결을 시도합니다. 서버 주소 : ${serverURL}")
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
              this.recvList.push(JSON.parse(res.body))

            })
          },
          error => {
            // 소켓 연결 실패
            console.log("소켓 연결 실패" + error);
            this.connected = false;
          }
      );
    }
  },

  // Mounted

  mounted() {


    canvas = new fabric.Canvas(this.$refs.canvasRef, {
      isDrawingMode: true
    })


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
</style>