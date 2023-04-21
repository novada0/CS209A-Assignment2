package cn.edu.sustech.cs209.chatting.server;

import cn.edu.sustech.cs209.chatting.common.Message;
import java.io.IOException;

public interface ChaService {
  void loginCheck(String username) throws IOException;

  void getOnlineList() throws IOException;

  void unicastMessage(Message message) throws IOException;

  void multicast(Message message) throws IOException;
}
