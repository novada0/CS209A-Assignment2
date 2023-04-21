package cn.edu.sustech.cs209.chatting.server;

import cn.edu.sustech.cs209.chatting.common.GroupMessage;
import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.Request;
import cn.edu.sustech.cs209.chatting.common.Response;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatServiceImpl implements ChaService{
  private boolean isClientOn;
  private String user;

  private Socket socket;
  private ObjectInputStream in;

  private ObjectOutputStream out;

  public ChatServiceImpl(Socket socket) throws IOException {
    this.socket = socket;
    isClientOn = true;
  }

  public void loginCheck(String username) throws IOException {
    Response<Boolean> response = new Response<>("LOGIN RETURN");
    if (Main.sessions.containsKey(username)) {
      response.setBody(false);
      out.writeObject(response);
      System.out.println(username+"已注册");
    } else {
      response.setBody(true);
      out.writeObject(response);
      this.user = username;
      Main.sessions.put(username, this);
      System.out.println(username+"成功登录");
    }
    out.flush();
  }

  public void getOnlineList() throws IOException {
    ArrayList<String> list = new ArrayList<>(Main.sessions.keySet());
    Response<ArrayList<String>> response = new Response<>("LIST RETURN");
    response.setBody(list);
    out.writeObject(response);
    out.flush();
  }

  // 单播发送
  public void unicastMessage(Message message) throws IOException {
    Response<Message> response = new Response<>("SINGLE RETURN");
    response.setBody(message);
    String sendTo = message.getSendTo();
    if(Main.sessions.containsKey(sendTo)) { // 对方还在线
      ObjectOutputStream toStream = Main.sessions.get(sendTo).out;
      toStream.writeObject(response);
      toStream.flush();
    }
  }

  // 多播发送
  public void multicast(Message message) throws IOException {
    GroupMessage groupMessage = (GroupMessage) message;
    List<String> members = groupMessage.getMembers();
    for(String member : members){ // 对所有接收方发送
      if(!member.equals(message.getSentBy()) && Main.sessions.containsKey(member)) { // 对方不是自己而且还在线
        Response<GroupMessage> response = new Response<>("GROUP RETURN");
        response.setBody(groupMessage);
        ObjectOutputStream toStream = Main.sessions.get(member).out;
        toStream.writeObject(response);
        toStream.flush();
      }
    }
  }

  public void serveDispatch() throws IOException, ClassNotFoundException{
    out = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
    in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
    while(true){
      Request<?> request = (Request<?>) in.readObject();
      System.out.println("COMMEND: " + request.getCommand());
      if(request.getCommand().equals("LOGIN")){
        loginCheck((String) request.getBody());
      } else if(request.getCommand().equals("LIST")){
        getOnlineList();
      } else if(request.getCommand().equals("SINGLE SEND")){
        unicastMessage((Message) request.getBody());
      } else if (request.getCommand().equals("GROUP SEND")) {
        multicast((Message) request.getBody());
      }
    }
  }

  public String getUser() {
    return user;
  }
}
