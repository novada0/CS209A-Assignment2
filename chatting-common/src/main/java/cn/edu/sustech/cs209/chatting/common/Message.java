package cn.edu.sustech.cs209.chatting.common;

import java.io.Serializable;

/**
 * 个人消息类.
 */
public class Message implements Serializable {

  private Long timestamp;

  private String sentBy;

  private String sendTo;

  private String data;

  private byte[] fileBytes;

  private String fileName;


  public Message(Long timestamp, String sentBy, String sendTo, String data, String fileName,
      byte[] fileBytes) {
    this.timestamp = timestamp;
    this.sentBy = sentBy;
    this.sendTo = sendTo;
    this.data = data;
    this.fileName = fileName;
    this.fileBytes = fileBytes;
  }

  public Long getTimestamp() {
    return timestamp;
  }

  public String getSentBy() {
    return sentBy;
  }

  public String getSendTo() {
    return sendTo;
  }

  public String getData() {
    return data;
  }

  public byte[] getFileBytes() {
    return fileBytes;
  }

  public String getFileName() {
    return fileName;
  }
}
