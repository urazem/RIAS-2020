package com.urazem.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.apache.log4j.Logger;


public class Server {

  private int port;
  private List<User> clients;
  private ServerSocket server;

  static Logger logger = Logger.getLogger(Server.class);
  public static void main(String[] args) throws IOException {
    new Server(12345).run();//запуск сервера поток
  }

  public Server(int port) {
    this.port = port;
    this.clients = new ArrayList<User>();
  }
//метод run запускает поток
  public void run() throws IOException {
    server = new ServerSocket(port) {
      protected void finalize() throws IOException {
        this.close();
      }
    };
    logger.info("Port 12345 is now open. Server is running");
    while (true) {
      // принимает нового клиента
      Socket client = server.accept();

      logger.info("Client is connected");
      // создание нового пользователя
      User newUser = new User(client, "user");

      // добавить сообщение newUser в список
      this.clients.add(newUser);

      // создать новый поток для обработки входящих сообщений нового пользователя
      new Thread(new UserHandler(this, newUser)).start();
      logger.info("Created a new thread to process incoming messages of a new client");
    }
  }

  // удалить пользователя из списка
  public void removeUser(User user){
    this.clients.remove(user);
  }

  // отправить входящие сообщения всем пользователям
  public void broadcastMessages(String msg) {
    for (User client : this.clients) {
      client.getOutStream().println(
        msg);
    }
  }


}
/*
* Обработчик(Handler) - это в основном очередь сообщений.
* Вы отправляете ему сообщение, и оно в конечном итоге обрабатывает его,
* вызывая метод run и передавая ему сообщение.
* Поскольку эти вызовы запуска всегда будут происходить в порядке сообщений,
* полученных в одном потоке, это позволяет вам сериализовать события.
* */

class UserHandler implements Runnable {

  private Server server;
  private User user;

  public UserHandler(Server server, User user) {
    this.server = server;
    this.user = user;

  }
  static Logger logger = Logger.getLogger(Server.class);
  public void run() {
    String message;
    // когда появляется новое сообщение, транслируется всем
    Scanner sc = new Scanner(this.user.getInputStream());

    while (sc.hasNextLine()) {
      message = sc.nextLine();
        server.broadcastMessages(message);
        logger.info("Get message to client: "+ message);
    }
    //завершение потока
    server.removeUser(user);
    sc.close();
    logger.info("Scanner closed and remove client");
  }
}

class User {
  private static int nbUser = 0;
  private int userId;
  private PrintStream streamOut;
  private InputStream streamIn;
  private String nickname;
  private Socket client;


  public User(Socket client, String name) throws IOException {
    this.streamOut = new PrintStream(client.getOutputStream());
    this.streamIn = client.getInputStream();
    this.client = client;
    this.nickname = name;
    this.userId = nbUser;
     nbUser += 1;
  }

  public PrintStream getOutStream(){
    return this.streamOut;
  }

  public InputStream getInputStream(){
    return this.streamIn;
  }

  public String getNickname(){
    return this.nickname;
  }

  public String toString(){
    return  this.getNickname();
  }
}


