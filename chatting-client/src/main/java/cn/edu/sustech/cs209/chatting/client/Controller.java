package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.GroupMessage;
import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.Request;
import cn.edu.sustech.cs209.chatting.common.Response;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * javafx 控制类.
 */
public class Controller implements Initializable {

  @FXML
  Label fileName;
  @FXML
  Button emojis;
  @FXML
  Button display;
  @FXML
  Label username;
  @FXML
  TextArea inputArea;
  @FXML
  Label currentUsername;
  @FXML
  ListView<String> chatList;
  @FXML
  ListView<Message> chatContentList;

  Socket socket;

  ObjectOutputStream objectOutputStream;

  ObjectInputStream objectInputStream;

  Lock lock;
  Condition receiveCondition;
  boolean flag;

  Response<?> response;

  RecordService recordService;

  File selectedFile;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    try {
      socket = new Socket("localhost", 12345);
      objectOutputStream = new ObjectOutputStream(
          new BufferedOutputStream(socket.getOutputStream()));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    lock = new ReentrantLock();
    receiveCondition = lock.newCondition();
    receiveThread(); // 接收线程

    Dialog<String> dialog = new TextInputDialog();
    dialog.setTitle("Login");
    dialog.setHeaderText(null);
    dialog.setContentText("Username:");

    Optional<String> input = dialog.showAndWait();

    while (true) {
      if (input.isPresent() && !input.get().isEmpty()) {
        Request<String> loginRequest = new Request<>("LOGIN", input.get());
        try {
          objectOutputStream.writeObject(loginRequest);
          objectOutputStream.flush();
        } catch (IOException e) {
          throw new RuntimeException(e);
        }

        // 线程同步
        lock.lock();
        try {
          while (!flag) {
            receiveCondition.await();
          }
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        } finally {
          lock.unlock();
        }
        flag = false;
        if (response == null) {
          throw new RuntimeException("没有收到Login响应");
        }
        if ((Boolean) response.getBody()) {
          username.setText(input.get());
          break;
        } else {
          dialog.setHeaderText("用户名已经存在，请重新输入！");
          input = dialog.showAndWait();
        }
      } else {
        System.out.println("Invalid username " + input + ", exiting");
        Platform.exit();
        System.exit(0);
      }
    }
    display.setVisible(false);
    recordService = new RecordServiceImpl(); // 记录消息
    chatContentList.setCellFactory(new MessageCellFactory());
    chatList.setCellFactory(new ChatListCellFactory());
  }

  /**
   * 接收消息的线程.
   */
  public void receiveThread() {
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          objectInputStream = new ObjectInputStream(
              new BufferedInputStream(socket.getInputStream()));
          while (true) {
            response = (Response<?>) objectInputStream.readObject();
            if (response.getType().equals("LOGIN RETURN")) { // 需要同步
              lock.lock();
              flag = true;
              receiveCondition.signal();
              lock.unlock();
            } else if (response.getType().equals("LIST RETURN")) { // 需要同步
              lock.lock();
              flag = true;
              receiveCondition.signal();
              lock.unlock();
            } else if (response.getType().equals("SINGLE RETURN")) { // 不需要同步
              Message message = (Message) response.getBody();
              System.out.println(message.getFileName());
              if (message.getFileName() != null) { // 如果收到了文件就保存文件
                saveFile(message.getFileName(), message.getFileBytes());
              }
              String from = message.getSentBy();
              if (!recordService.isSingleChat(from)) { // 第一次接收对方的消息
                recordService.addSingleChat(from);
              }
              recordService.addMessage(from, message); // 添加记录
              // 更新聊天面板
              Platform.runLater(new Runnable() {
                @Override
                public void run() {
                  if (!chatList.getItems().contains(from)) { // 左侧面板没有用户
                    chatList.getItems().add(0, from);
                  } else {
                    if (currentUsername.getText().equals(from)) { // 正在聊天
                      chatContentList.getItems().add(message);
                    } else { // 不在聊天
                      chatList.getItems().remove(from);
                      chatList.getItems().add(0, from);
                      recordService.changeToUnRead(from);
                    }
                  }
                }
              });
            } else if (response.getType().equals("GROUP RETURN")) { // 群聊消息
              GroupMessage message = (GroupMessage) response.getBody();
              if (message.getFileName() != null) { // 如果收到了文件就保存文件
                saveFile(message.getFileName(), message.getFileBytes());
              }
              String index = message.getGroup();
              if (!recordService.isGroupChat((index))) { // 第一次接收群聊信息
                recordService.addGroupChat(index, message.getMembers());
              }
              Message atomMessage = new Message(message.getTimestamp(),
                  message.getSentBy(), null, message.getData(),
                  message.getFileName(), message.getFileBytes());
              recordService.addMessage(index, atomMessage);
              // 更新聊天面板
              Platform.runLater(new Runnable() {
                @Override
                public void run() {
                  if (!chatList.getItems().contains(index)) { // 左侧面板没有用户
                    chatList.getItems().add(0, index);
                  } else {
                    if (currentUsername.getText().equals(index)) { // 正在聊天
                      chatContentList.getItems().add(atomMessage);
                    } else { // 不在聊天
                      chatList.getItems().remove(index);
                      chatList.getItems().add(0, index);
                      recordService.changeToUnRead(index);
                    }
                  }
                }
              });
            }
          }
        } catch (IOException | ClassNotFoundException e) {  // 服务器挂了
          Platform.runLater(new Runnable() {
            @Override
            public void run() {
              Alert alert = new Alert(AlertType.ERROR);
              alert.setTitle("Server close");
              alert.setHeaderText(null);
              alert.setContentText("Sorry, the server closed. Your chat will end!");
              alert.setOnCloseRequest(dialogEvent -> Platform.exit());
              alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                  Platform.exit();
                }
              });
            }
          });
        }

      }
    }).start();
  }

  /**
   * 创建单人聊天室.
   *
   * @throws IOException IO异常
   */
  @FXML
  public void createPrivateChat() throws IOException {
    AtomicReference<String> user = new AtomicReference<>();

    Stage stage = new Stage();
    ComboBox<String> userSel = new ComboBox<>();

    // FIXME: get the user list from server, the current user's name should be filtered out
    Request<String> objectRequest = new Request<>("LIST");
    objectOutputStream.writeObject(objectRequest);
    objectOutputStream.flush();

    // 线程同步
    lock.lock();
    try {
      while (!flag) {
        receiveCondition.await();
      }
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    } finally {
      lock.unlock();
    }
    flag = false;

    List<String> onlineList = (ArrayList<String>) response.getBody();
    for (String item : onlineList) {
      if (!item.equals(username.getText())) {
        userSel.getItems().add(item);
      }
    }

    Button okBtn = new Button("OK");
    okBtn.setOnAction(e -> {
      user.set(userSel.getSelectionModel().getSelectedItem());
      stage.close();
    });

    HBox box = new HBox(10);

    box.setAlignment(Pos.CENTER);
    box.setPadding(new Insets(20, 20, 20, 20));
    box.getChildren().addAll(userSel, okBtn);
    stage.setScene(new Scene(box));
    stage.showAndWait();

    // TODO: if the current user already chatted with the selected user, just open the chat with that user
    // TODO: otherwise, create a new chat item in the left panel, the title should be the selected user's name
    if (user.get() != null && !user.get().isEmpty()) {
      if (chatList.getItems().contains(user.get())) { // 左侧已经有列表了
        List<Message> messages = recordService.getMessages(user.get());
        chatContentList.getItems().clear();
        chatContentList.getItems().setAll(messages); // 恢复成原来的聊天窗口
      } else {
        recordService.addSingleChat(user.get()); // 加到记录里面
        chatList.getItems().add(0, user.get());
        chatContentList.getItems().clear(); // 新来的就清空就行了
      }
      chatList.getSelectionModel().select(user.get());
      currentUsername.setText(user.get());
    }
  }

  /**
   * A new dialog should contain a multi-select list, showing all user's name. You can select
   * several users that will be joined in the group chat, including yourself.
   *
   * <p>
   * The naming rule for group chats is similar to WeChat: If there are > 3 users: display the first
   * three usernames, sorted in lexicographic order, then use ellipsis with the number of users, for
   * example: UserA, UserB, UserC... (10) If there are <= 3 users: do not display the ellipsis, for
   * example: UserA, UserB (2)
   */
  @FXML
  public void createGroupChat() throws IOException {
    List<String> selected = new ArrayList<>();

    Stage dialog = new Stage();
    dialog.setTitle("选择群聊用户");
    dialog.setWidth(300);
    dialog.setHeight(400);

    // 创建一个多选框列表
    VBox checkBoxList = new VBox();
    checkBoxList.setSpacing(10);
    checkBoxList.setPadding(new Insets(10));

    // FIXME: get the user list from server, the current user's name should be filtered out
    Request<String> objectRequest = new Request<>("LIST");
    objectOutputStream.writeObject(objectRequest);
    objectOutputStream.flush();

    // 线程同步
    lock.lock();
    try {
      while (!flag) {
        receiveCondition.await();
      }
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    } finally {
      lock.unlock();
    }
    flag = false;

    List<String> onlineList = (ArrayList<String>) response.getBody();
    for (String item : onlineList) {
      if (!item.equals(username.getText())) {
        checkBoxList.getChildren().add(new CheckBox(item));
      }
    }

    Button okBtn = new Button("OK");
    okBtn.setOnAction(e -> {
      for (Node node :
          checkBoxList.getChildren()) {
        CheckBox cb = (CheckBox) node;
        if (cb.isSelected()) {
          selected.add(cb.getText());
        }
      }
      dialog.close();
    });
    // 创建一个布局，并在其中添加多选框列表和确认按钮
    VBox layout = new VBox();
    layout.setAlignment(Pos.CENTER);
    layout.getChildren().addAll(checkBoxList, okBtn);

    // 创建一个滚动窗口，以便在多选框列表过长时可以滚动查看
    ScrollPane scrollPane = new ScrollPane(layout);

    // 创建一个Scene，并将滚动窗口添加到Scene中
    Scene scene = new Scene(scrollPane);

    // 将Scene设置到Stage中，并显示Stage
    dialog.setScene(scene);
    dialog.showAndWait();

    if (selected.size() > 0) {
      selected.add(username.getText()); // 添加自己的name
      String title = Utils.getGroupName(selected);
      // TODO: if the current user already chatted with the selected user, just open the chat with that user
      // TODO: otherwise, create a new chat item in the left panel, the title should be the selected user's name
      if (chatList.getItems().contains(title)) { // 左侧已经有列表了
        List<Message> messages = recordService.getMessages(title);
        chatContentList.getItems().clear();
        chatContentList.getItems().setAll(messages); // 恢复成原来的聊天窗口
      } else {
        recordService.addGroupChat(title, selected); // 加到记录里面
        chatList.getItems().add(0, title);
        chatContentList.getItems().clear(); // 新来的就清空就行了
      }
      chatList.getSelectionModel().select(title);
      currentUsername.setText(title);
    }
  }

  /**
   * Sends the message to the <b>currently selected</b> chat.
   * <p>
   * Blank messages are not allowed. After sending the message, you should clear the text input
   * field.
   */
  @FXML
  public void doSendMessage() throws IOException {
    // TODO
    if (inputArea.getText().isEmpty()) {
      return;
    }
    String index = currentUsername.getText(); // 得到当前聊天的index
    String fileName_temp = null;
    byte[] fileBytes_temp = null;
    if (!fileName.getText().isEmpty()) { // 判断有没有上传文件
      fileName_temp = fileName.getText();
      fileBytes_temp = Files.readAllBytes(selectedFile.toPath());
    }
    if (recordService.isSingleChat(index)) {
      Message mes = new Message(System.currentTimeMillis(),
          username.getText(), index, inputArea.getText(), fileName_temp, fileBytes_temp);
      recordService.addMessage(index, mes); // 添加记录
      chatContentList.getItems().add(mes);
      // 向server发送信息
      Request<Message> sendRequest = new Request<>("SINGLE SEND");
      sendRequest.setBody(mes);
      objectOutputStream.writeObject(sendRequest);
      objectOutputStream.flush();
    } else {
      Message groupMessage = new GroupMessage(System.currentTimeMillis(), username.getText(),
          null, inputArea.getText(), index, recordService.getMembers(index), fileName_temp,
          fileBytes_temp);
      recordService.addMessage(index, groupMessage); // 添加记录
      chatContentList.getItems().add(groupMessage);
      // 向server发送信息
      Request<Message> sendRequest = new Request<>("GROUP SEND");
      sendRequest.setBody(groupMessage);
      objectOutputStream.writeObject(sendRequest);
      objectOutputStream.flush();
    }

    // 发送文件复原
    fileName.setText("");
    // 清空输入框
    inputArea.clear();
  }

  /**
   * 聊天列表修改后的回调函数.
   */
  public class ChatListCellFactory implements Callback<ListView<String>, ListCell<String>> {

    @Override
    public ListCell<String> call(ListView<String> stringListView) {
      return new ListCell<String>() {
        @Override
        public void updateItem(String item, boolean empty) {
          super.updateItem(item, empty);
          if (empty || Objects.isNull(item)) {
            setText(null);
            setGraphic(null);
          } else {
            StackPane stackPane = new StackPane();
            stackPane.setPrefHeight(30);

            Label label = new Label(item);
            stackPane.getChildren().add(label);
            Circle redDot = new Circle(5, javafx.scene.paint.Color.RED);
            stackPane.getChildren().add(redDot);
            StackPane.setAlignment(redDot, javafx.geometry.Pos.TOP_RIGHT);

            setGraphic(stackPane);

            redDot.setVisible(true);
            if (isSelected()) {
              recordService.changeToRead(item);
              currentUsername.setText(item);
              chatContentList.getItems().clear();
              chatContentList.getItems().addAll(recordService.getMessages(item));
              redDot.setVisible(false);
              if (recordService.isSingleChat(item)) {
                display.setVisible(false);
              } else if (recordService.isGroupChat(item)) {
                display.setVisible(true);
              }
            } else if (recordService.isRead(item)) {
              redDot.setVisible(false);
            }
          }
        }
      };
    }
  }

  /**
   * You may change the cell factory if you changed the design of {@code Message} model. Hint: you
   * may also define a cell factory for the chats displayed in the left panel, or simply override
   * the toString method.
   */
  private class MessageCellFactory implements Callback<ListView<Message>, ListCell<Message>> {

    @Override
    public ListCell<Message> call(ListView<Message> param) {
      return new ListCell<Message>() {

        @Override
        public void updateItem(Message msg, boolean empty) {
          super.updateItem(msg, empty);
          if (empty || Objects.isNull(msg)) {
            setText(null);
            setGraphic(null);
            return;
          }
          String toDisplay = null;
          if (msg.getFileName() != null) {
            if (msg.getSentBy().equals(username.getText())) {
              toDisplay = String.format("\"---成功发送文件: %s---\"\n%s", msg.getFileName(),
                  msg.getData());
            } else {
              toDisplay = String.format("\"---成功接收文件: %s---\"\n%s", msg.getFileName(),
                  msg.getData());
            }
          } else {
            toDisplay = msg.getData();
          }

          HBox wrapper = new HBox();
          Label nameLabel = new Label(msg.getSentBy());
          Label msgLabel = new Label(toDisplay);

          nameLabel.setPrefSize(50, 20);
          nameLabel.setWrapText(true);
          nameLabel.setStyle("-fx-border-color: black; -fx-border-width: 1px;");

          if (username.getText().equals(msg.getSentBy())) {
            wrapper.setAlignment(Pos.TOP_RIGHT);
            wrapper.getChildren().addAll(msgLabel, nameLabel);
            msgLabel.setPadding(new Insets(0, 20, 0, 0));
          } else {
            wrapper.setAlignment(Pos.TOP_LEFT);
            wrapper.getChildren().addAll(nameLabel, msgLabel);
            msgLabel.setPadding(new Insets(0, 0, 0, 20));
          }

          setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
          setGraphic(wrapper);
        }
      };
    }
  }

  @FXML
  private void showList(ActionEvent event) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle("当前该群聊的用户列表");
    alert.setHeaderText(null);
    alert.setContentText(String.join("\n", recordService.getMembers(currentUsername.getText())));
    alert.showAndWait();
  }

  @FXML
  private void showEmojiPanel() {
    // 创建一个新的窗口
    Stage emojiStage = new Stage();

    // 创建一个网格布局，用于显示 emoji 表情
    GridPane emojiGrid = new GridPane();
    emojiGrid.setHgap(10);
    emojiGrid.setVgap(10);
    emojiGrid.setAlignment(Pos.CENTER);

    // 添加 emoji 按钮
    Button smileyFace = new Button("\uD83D\uDE0A");
    smileyFace.setOnAction(actionEvent -> inputArea.appendText(smileyFace.getText()));
    Button grinningFace = new Button("\uD83D\uDE00");
    grinningFace.setOnAction(actionEvent -> inputArea.appendText(grinningFace.getText()));
    Button winkingFace = new Button("\uD83D\uDE09");
    winkingFace.setOnAction(actionEvent -> inputArea.appendText(winkingFace.getText()));
    Button heartEyes = new Button("\uD83D\uDE0D");
    heartEyes.setOnAction(actionEvent -> inputArea.appendText(heartEyes.getText()));
    Button thumbsUp = new Button("\uD83D\uDC4D");
    thumbsUp.setOnAction(actionEvent -> inputArea.appendText(thumbsUp.getText()));
    Button thumbsDown = new Button("\uD83D\uDC4E");
    thumbsDown.setOnAction(actionEvent -> inputArea.appendText(thumbsDown.getText()));
    Button clappingHands = new Button("\uD83D\uDC4F");
    clappingHands.setOnAction(actionEvent -> inputArea.appendText(clappingHands.getText()));
    Button partyPopper = new Button("\uD83C\uDF89");
    partyPopper.setOnAction(actionEvent -> inputArea.appendText(partyPopper.getText()));
    Button balloon = new Button("\uD83C\uDF88");
    balloon.setOnAction(actionEvent -> inputArea.appendText(balloon.getText()));
    Button birthdayCake = new Button("\uD83C\uDF82");
    birthdayCake.setOnAction(actionEvent -> inputArea.appendText(birthdayCake.getText()));

    // 将 emoji 按钮添加到网格布局
    emojiGrid.addRow(0, smileyFace, grinningFace, winkingFace, heartEyes, thumbsUp);
    emojiGrid.addRow(1, thumbsDown, clappingHands, partyPopper, balloon, birthdayCake);

    // 将网格布局添加到场景中
    Scene emojiScene = new Scene(emojiGrid, 300, 200);
    emojiStage.setScene(emojiScene);
    emojiStage.setTitle("Emojis");
    emojiStage.show();
  }

  @FXML
  private void chooseFile() {
    // 创建一个文件选择器
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("选择要上传的文件");

    // 显示文件选择器并等待用户选择文件
    selectedFile = fileChooser.showOpenDialog(new Stage());

    if (selectedFile != null) {
      fileName.setText(selectedFile.getName());
    }
  }

  // 保存文件
  private void saveFile(String fileName, byte[] content) throws IOException {
    File file = new File("D:\\test\\" + username.getText() + "\\" + fileName);
    File dir = file.getParentFile();
    if (!dir.exists()) {
      dir.mkdirs();
    }
    FileOutputStream outputStream = new FileOutputStream(file);
    outputStream.write(content);
    outputStream.close();
  }
}
