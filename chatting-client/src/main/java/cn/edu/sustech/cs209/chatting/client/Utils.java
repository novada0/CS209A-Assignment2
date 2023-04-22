package cn.edu.sustech.cs209.chatting.client;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 工具类.
 */
public class Utils {

  /**
   * 根据小组成员得到聊天群名.
   *
   * @param members 聊天群的成员
   * @return 群名
   */
  public static String getGroupName(List<String> members) { // 需要包括自己
    List<String> sortedUsers = members.stream().sorted().collect(Collectors.toList());
    String title = null;
    if (sortedUsers.size() <= 3) {
      title = String.format("%s (%d)", String.join(", ", sortedUsers), sortedUsers.size());
    } else {
      title = String.format("%s... (%d)", String.join(", ", sortedUsers.subList(0, 3)),
          sortedUsers.size());
    }
    return title;
  }

}
