package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Message;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RecordServiceImpl implements RecordService {
  private Map<String, SingleChat> singleChatRecords;
  private Map<String, GroupChat> groupChatRecords;

  public RecordServiceImpl(){
    singleChatRecords = new HashMap<>();
    groupChatRecords = new HashMap<>();
  }

  @Override
  public void addSingleChat(String name) {
    singleChatRecords.put(name, new SingleChat(name, new ArrayList<>(), false));
  }

  @Override
  public void addGroupChat(String groupName, List<String> members) { // 需要包括自己
    groupChatRecords.put(groupName, new GroupChat(groupName, new ArrayList<>(), false, members));
  }

  @Override
  public void addMessage(String index, Message message) {
    if(isSingleChat(index))
      singleChatRecords.get(index).getMessages().add(message);
    else
      groupChatRecords.get(index).getMessages().add(message);
  }

  @Override
  public List<Message> getMessages(String index) {
    if(isSingleChat(index))
      return singleChatRecords.get(index).getMessages();
    return groupChatRecords.get(index).getMessages();
  }

  @Override
  public boolean isRead(String index) {
    if(isSingleChat(index))
      return singleChatRecords.get(index).isRead();
    return groupChatRecords.get(index).isRead();
  }

  @Override
  public void changeToRead(String index) {
    if(isSingleChat(index))
      singleChatRecords.get(index).setRead(true);
    else
      groupChatRecords.get(index).setRead(true);
  }

  @Override
  public void changeToUnRead(String index) {
    if(isSingleChat(index))
      singleChatRecords.get(index).setRead(false);
    else
      groupChatRecords.get(index).setRead(false);
  }

  @Override
  public boolean isSingleChat(String index) {
    return singleChatRecords.containsKey(index);
  }

  @Override
  public boolean isGroupChat(String index) {
    return groupChatRecords.containsKey(index);
  }

  @Override
  public List<String> getMembers(String groupName) {
    return groupChatRecords.get(groupName).getMember();
  }


}
