package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Message;
import java.util.List;

public interface RecordService {
  void addSingleChat(String name);

  void addGroupChat(String groupName, List<String> members);

  void addMessage(String index, Message message);

  List<Message> getMessages(String index);

  boolean isRead(String index);

  void changeToRead(String index);

  void changeToUnRead(String index);

  boolean isSingleChat(String index);
  boolean isGroupChat(String index);

  List<String> getMembers(String groupName);

}
