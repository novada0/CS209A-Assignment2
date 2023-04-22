package cn.edu.sustech.cs209.chatting.server;

import cn.edu.sustech.cs209.chatting.common.Message;
import java.io.IOException;

/**
 * 定义了Server端的聊天服务.
 */
public interface ChaService {

  /**
   * 登录检查服务. 检查是否该用户名已存在
   *
   * @param username 用户名
   * @throws IOException IO异常
   */
  void loginCheck(String username) throws IOException;

  /**
   * 得到在线列表.
   *
   * @throws IOException IO异常
   */
  void getOnlineList() throws IOException;

  /**
   * 消息的单播发送.
   *
   * @param message 个人消息
   * @throws IOException IO异常
   */
  void unicastMessage(Message message) throws IOException;

  /**
   * 消息的多播发送.
   *
   * @param message 群聊消息
   * @throws IOException IO异常
   */
  void multicast(Message message) throws IOException;
}
