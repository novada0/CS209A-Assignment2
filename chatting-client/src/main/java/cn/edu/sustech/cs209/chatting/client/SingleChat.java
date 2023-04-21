package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Message;
import java.util.List;

public class SingleChat extends Chat{
  private String name;

  public SingleChat(String name,
      List<Message> messages, boolean isRead) {
    super(name, messages, isRead);
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
