import SockJS from 'sockjs-client';
import Stomp from 'stompjs';

let stompClient = null;

export function initializeWebSocket(app) {
    if (!stompClient) {
        const socket = new SockJS('http://localhost:8080/');
        stompClient = Stomp.over(socket);

        stompClient.connect({}, (frame) => {
            console.log('Connected: ' + frame);
            app.config.globalProperties.$socket = stompClient;

            stompClient.subscribe('/send', (message) => {
                console.log('Received whiteboard message: ', message.body);
                app.config.globalProperties.$bus.emit('whiteboard-event', message);
            });
            stompClient.subscribe('/topic/join', (message) => {
                console.log('Received join message: ', message.body);
                app.config.globalProperties.$bus.emit('join', message);
            });
            stompClient.subscribe('/topic/leave', (message) => {
                console.log('Received leave message: ', message.body);
                app.config.globalProperties.$bus.emit('leave', message);
            });
        });
    }
}