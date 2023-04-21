package cn.edu.sustech.cs209.chatting.common;

import java.io.Serializable;

public class Request<T> implements Serializable {
  private String command;
  private T body;

  public Request(String command){
    this.command = command;
    body = null;
  }

  public Request(String command, T body){
    this.command = command;
    this.body = body;
  }

  public Request(){

  }

  public String getCommand() {
    return command;
  }

  public void setCommand(String command) {
    this.command = command;
  }

  public T getBody() {
    return body;
  }

  public void setBody(T body) {
    this.body = body;
  }
}
