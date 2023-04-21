package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Message;
import java.util.List;

public class GroupChat extends Chat{
  private List<String> member;

  public GroupChat(String index, List<Message> messages,
      boolean isRead, List<String> member) {
    super(index, messages, isRead);
    this.member = member;
  }

  public List<String> getMember() {
    return member;
  }

  public void setMember(List<String> member) {
    this.member = member;
  }
}
