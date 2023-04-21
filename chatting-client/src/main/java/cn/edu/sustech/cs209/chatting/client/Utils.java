package cn.edu.sustech.cs209.chatting.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Utils {


  public static String getGroupName(List<String> members) { // 需要包括自己
    List<String> sortedUsers = members.stream().sorted().collect(Collectors.toList());
    String title = null;
    if(sortedUsers.size() <= 3){
      title = String.format("%s (%d)", String.join(", ", sortedUsers), sortedUsers.size());
    }else
      title = String.format("%s... (%d)", String.join(", ", sortedUsers.subList(0, 3)), sortedUsers.size());
    return title;
  }

}
