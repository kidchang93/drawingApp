<!--
<template>
<div id="room">
  유저이름 :
  <input type="text" v-model="userName">
  내용 :
  <input type="text" v-model="message" @keyup="sendMessage">
  <div v-for="(item, idx) in recvList" :key="idx">
    <h3>유저이름 : {{ item.userName }}</h3>
    <h3>내용 : {{ item.content }}</h3>
  </div>

</div>
</template>
<script>
import Stomp from 'webstomp-client'
import SockJS from 'sockjs-client'

export default {
  name: "Socket",
  data(){
    return{
      userName:"",
      message:"",
      recvList:[],

    }
  },
  created(){
    // App.vue가 생성되면 소켓 연결을 시도.
    this.connect()
  },
  methods:{

    sendMessage (e) {
      if (e.keyCode === 13 && this.userName !== '' && this.message !== ''){
        this.send()
        this.message = '';
      }
    },

    send(){
    console.log("Send Message : " + this.message);
    if (this.stompClient && this.stompClient.connected){
      const msg = {
        userName: this.userName,
        content: this.content,
      };
      this.stompClient.send("/receive", JSON.stringify(msg), {});
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
  }
}
</script>



<style scoped>

</style>-->
