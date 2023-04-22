package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Message;
import java.util.List;

/**
 * 定义了Client端的消息记录服务.
 */
public interface RecordService {

  /**
   * 添加个人聊天的服务.
   *
   * @param name 对方的用户名
   */
  void addSingleChat(String name);

  /**
   * 添加群聊的服务.
   *
   * @param groupName 群聊名
   * @param members   群成员
   */
  void addGroupChat(String groupName, List<String> members);

  /**
   * 添加消息记录.
   *
   * @param index   对方的名字或者群聊名
   * @param message 个人消息或群聊消息
   */
  void addMessage(String index, Message message);

  /**
   * 得到所有的消息记录.
   *
   * @param index 对方的名字或者群聊名
   * @return 对应的所有消息记录 List
   */
  List<Message> getMessages(String index);

  /**
   * 返回该消息是否已读.
   *
   * @param index 对方的名字或者群聊名
   * @return 是否已读
   */
  boolean isRead(String index);

  /**
   * 修改聊天为已读.
   *
   * @param index 对方的名字或者群聊名
   */
  void changeToRead(String index);

  /**
   * 修改聊天为未读.
   *
   * @param index 对方的名字或者群聊名
   */
  void changeToUnRead(String index);

  /**
   * 返回是否是单人聊天.
   *
   * @param index 对方的名字或者群聊名
   */
  boolean isSingleChat(String index);

  /**
   * 返回是否是群聊.
   *
   * @param index 对方的名字或者群聊名
   */
  boolean isGroupChat(String index);

  /**
   * 点击show users按钮时需要得到的该群聊的所有成员.
   *
   * @param groupName 群聊名
   * @return 该群聊的所有成员 List
   */
  List<String> getMembers(String groupName);

}
