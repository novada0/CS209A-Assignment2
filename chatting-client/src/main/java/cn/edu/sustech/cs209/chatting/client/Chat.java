package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Message;
import java.util.List;


public class Chat {
  private String index;
  private List<Message> messages;
  private boolean isRead;

  public Chat(String index, List<Message> messages, boolean isRead) {
    this.index = index;
    this.messages = messages;
    this.isRead = isRead;
  }

  public Chat(){}

  public String getIndex() {
    return index;
  }

  public void setIndex(String index) {
    this.index = index;
  }

  public List<Message> getMessages() {
    return messages;
  }

  public void setMessages(List<Message> messages) {
    this.messages = messages;
  }

  public boolean isRead() {
    return isRead;
  }

  public void setRead(boolean read) {
    isRead = read;
  }
}
