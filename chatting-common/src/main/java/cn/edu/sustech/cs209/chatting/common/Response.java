package cn.edu.sustech.cs209.chatting.common;

import java.io.Serializable;

public class Response<T> implements Serializable {
  private String type;
  private T body;

  public Response(String type, T body) {
    this.type = type;
    this.body = body;
  }

  public Response(String type) {
    this.type = type;
  }

  public Response(){

  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public T getBody() {
    return body;
  }

  public void setBody(T body) {
    this.body = body;
  }
}
