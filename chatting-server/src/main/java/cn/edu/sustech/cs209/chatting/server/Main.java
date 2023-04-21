package cn.edu.sustech.cs209.chatting.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Main {

    static Map<String, ChatServiceImpl> sessions = new HashMap<>();

    public static void main(String[] args) throws IOException {
        System.out.println("Starting server");
        ServerSocket serverSocket = new ServerSocket(12345);
        while (true){
            Socket socket = serverSocket.accept();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    ChatServiceImpl chatService = null;
                    try {
                        chatService = new ChatServiceImpl(socket);
                        chatService.serveDispatch();
                    } catch (IOException | ClassNotFoundException e) { // 对方离线了
                        assert chatService != null;
                        sessions.remove(chatService.getUser());
//                        throw new RuntimeException(e);
                    }
                }
            }).start();
        }
    }
}
