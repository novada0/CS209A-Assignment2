package cn.edu.sustech.cs209.chatting.common;

import java.util.List;

/**
 * 群聊消息类.
 */
public class GroupMessage extends Message {

  private String group;
  private List<String> members;

  public GroupMessage(Long timestamp, String sentBy, String sendTo, String data, String group,
      List<String> members,
      String fileName, byte[] fileBytes) {
    super(timestamp, sentBy, sendTo, data, fileName, fileBytes);
    this.group = group;
    this.members = members;
  }

  public String getGroup() {
    return group;
  }

  public void setGroup(String group) {
    this.group = group;
  }

  public List<String> getMembers() {
    return members;
  }

  public void setMembers(List<String> members) {
    this.members = members;
  }
}
