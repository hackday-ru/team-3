package net.b2ok;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;


public class WebSocketController extends TextWebSocketHandler {

    private class User{
        public final WebSocketSession session;
        public volatile WebSocketSession connectToSession;
        public final boolean type;

        public User(WebSocketSession session, boolean type) {
            this.session = session;
            this.type = type;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            User user = (User) o;

            return session != null ? session.equals(user.session) : user.session == null;

        }

        @Override
        public int hashCode() {
            return session != null ? session.hashCode() : 0;
        }

        @Override
        public String toString() {
            return "User{" +
                    "session=" + session +
                    ", connectToSession=" + connectToSession +
                    ", type=" + type +
                    '}';
        }
    }




    public static volatile Map<WebSocketSession, User> usersSession = new ConcurrentHashMap<>();
    public static volatile Queue<User> freeDoctorSession = new ConcurrentLinkedQueue<>();
    public static volatile Queue<User> freeUsersSession = new ConcurrentLinkedQueue<>();



    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {}

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        User user = usersSession.get(usersSession.remove(session).connectToSession);

        if (user == null){
            System.out.println("user == null");
            return;
        }

        user.connectToSession = null;

        if (user.type){
            freeDoctorSession.add(user);
        } else {
        //    freeUsersSession.add(user);
            WebSocketMessage webSocketMessage = new TextMessage("{\"cmd\": \"close\"}");
            try {
                System.out.println(user.toString());
                user.session.sendMessage(webSocketMessage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }




    }



    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        handleTransportError(session, new Exception("close"));

    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        //System.out.println(message.getPayload());
        OneMessage msg = null;
        try {
            msg = ChatieApplication.gson.fromJson(message.getPayload(), OneMessage.class);
            System.out.println(msg.toJson());
            switch (msg.getCmd()) {
                case "connect":
                    msg = connect(session, msg);
                    break;
                case "send":
                    msg = send(session, msg);
                    break;
            }

        } catch (Exception e){}

        if (msg != null) {
            WebSocketMessage webSocketMessage = new TextMessage(msg.toJson());
            try {
                session.sendMessage(webSocketMessage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private OneMessage connect(WebSocketSession session, OneMessage msg) {
        String param = msg.getParam();

        msg = null;
        if ((param != null) && (!param.equals(""))){
            User user = new User(session, true);
            usersSession.put(session, user);

            User getUser = freeUsersSession.poll();

            if (getUser == null) { //Если нету суецидников, то добавляем свободного врача
                freeDoctorSession.add(usersSession.get(session));
            } else {

                getUser.connectToSession = session;
                usersSession.get(session).connectToSession = getUser.session;


                msg = new OneMessage("connect", "", "1");

                WebSocketMessage webSocketMessage = new TextMessage(msg.toJson());
                try {
                    getUser.session.sendMessage(webSocketMessage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } else {
            User user = new User(session, false);
            usersSession.put(session, user);

            User getUser = freeDoctorSession.poll();

            if (getUser == null) { //Если нету врачей, то добавляем суецидников в очередь
                freeUsersSession.add(usersSession.get(session));
            } else {

                getUser.connectToSession = session;
                usersSession.get(session).connectToSession = getUser.session;


                msg = new OneMessage("connect", "", "1");

                WebSocketMessage webSocketMessage = new TextMessage(msg.toJson());
                try {
                    getUser.session.sendMessage(webSocketMessage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }




        return msg;
    }

    private OneMessage send(WebSocketSession session, OneMessage msg) throws IOException {

        usersSession.get(session).connectToSession.sendMessage(new TextMessage(msg.toJson()));

        return null;
    }

    private class Msg{
        public Long id;
        public Long user1;
        public Long user2;
        public String text;
        public Long time;
    }

}
