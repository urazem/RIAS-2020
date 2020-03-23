package com.urazem.client;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.IOException;

import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {

  private String host;
  private int port;
  static Logger logger = Logger.getLogger(Client.class);

  public static void main(String[] args) throws UnknownHostException, IOException {
    String command;

    while (true) {
      System.out.print("EchoClient> ");
      Scanner sc = new Scanner(System.in);

      while (sc.hasNextLine()) {
        command = sc.nextLine(); //считал команду
        if (command.contains("connect")||command.contains("c/")){
          connect(command);
        }else if (command.contains("quit")||command.contains("q/")) {
          quit(command);
        }
        else
          help();
      }
    }
  }
  public Client(String host, int port) {
    this.host = host;
    this.port = port;
  }

  public void run() throws UnknownHostException, IOException {


    Socket client = new Socket(host, port);  // подключить клиента к серверу
    logger.info("Socket is created");
    System.out.println( ConsoleColors.YELLOW_BOLD_BRIGHT +"Connection to MSRG Echo server established: /" + host + " /"+port+ConsoleColors.RESET);
    logger.info("Connection to MSRG Echo server established");

    PrintStream output = new PrintStream(client.getOutputStream()); // Получить выходной поток Socket (куда клиент отправляет свое сообщение)


    new Thread(new ReceivedMessagesHandler(client.getInputStream())).start(); // создать новый поток для обработки сообщений сервера

    // читать сообщения с клавиатуры и отправлять на сервер
    String command;

    // while new messages
    Scanner sc = new Scanner(System.in);
    System.out.print("EchoClient> ");
    while (sc.hasNextLine()) {
      System.out.print("EchoClient> ");
      command = sc.nextLine();
      if (command.contains("send") || command.contains("s/")) {
        if (command.contains(" ")) {
          if (!client.isClosed()) {
            int firstSpace = command.indexOf(" ");
            String message = command.substring(firstSpace + 1, command.length());

            output.println(message); //отправка сообщения серверу

            logger.info("Get message to server");
          } else
            System.out.println(ConsoleColors.RED + "С ума сошел что ли, ты же не подключен к серверу)" +
                    ConsoleColors.RESET);
          logger.warn("Don't connected to server");
        } else
          wrongСommand();
      }else if (command.contains("disconnect")||command.contains("d/")) {
          System.out.println(ConsoleColors.RED + "Connection terminated: 127.0.0.1 / 5555"+ ConsoleColors.RESET);
          logger.info("Client "+ host +" /"+port + " is disconnected");
          client.close();
          output.close();
      }else if (command.contains("quit")||command.contains("q/")) {
        quit(command, output, client);

      }else if (command.contains("connect")||command.contains("c/")) {
        connect(command);
      }
      else if (command.contains("help")||command.contains("h/")) {
          help();
      }else if (command.contains("logLevel")||command.contains("l/")) {
          logLevel(command);
      }
      else
        wrongСommand();
    }
    output.close();
    sc.close();
    client.close();

  }


  public static void connect(String command) throws IOException {
    try{
        if (command.contains(" ")) {
          int firstSpace = command.indexOf(" ");
          int secondSpace = command.indexOf(" ", firstSpace + 1);
          if(secondSpace>-1){
              String host = command.substring(firstSpace + 1, secondSpace);
              String _port = command.substring(secondSpace + 1, command.length());

              int port = Integer.parseInt(_port);

              new Client(host, port).run(); //запуск потока

          }else{
            System.out.println(ConsoleColors.RED + "Error! Not connected!"+
                    ConsoleColors.RESET);
            logger.error("Error! Not connected");
          }
        }
       else {
        wrongСommand();
      }
    }catch(IOException exception){
      System.out.println(ConsoleColors.RED + "Error! Not connected!"+
              ConsoleColors.RESET);
      logger.error("Error! Not connected: " + exception);
    }

  }

  public static void help(){
    System.out.println("\n" +
     ConsoleColors.YELLOW_BOLD_BRIGHT +  "c/" + ConsoleColors.RESET +" or "+ ConsoleColors.YELLOW_BOLD_BRIGHT+"connect <address> <port>       "+ ConsoleColors.RESET + "Пытается установить TCP соединение с эхо-сервером на основе заданного адреса сервера и номера порта эхо-службы. \n"+
     ConsoleColors.YELLOW_BOLD_BRIGHT +  "d/" + ConsoleColors.RESET +" or "+ ConsoleColors.YELLOW_BOLD_BRIGHT+"disconnect             "+ ConsoleColors.RESET + "Пытается отключиться от подключенного сервера. \n"+
     ConsoleColors.YELLOW_BOLD_BRIGHT +  "s/" + ConsoleColors.RESET +" or "+ ConsoleColors.YELLOW_BOLD_BRIGHT+"send <message>         "+ ConsoleColors.RESET + "Отправляет текстовое сообщение на эхо-сервер в соответствии с протоколом связи. \n"+
     ConsoleColors.YELLOW_BOLD_BRIGHT +  "l/" + ConsoleColors.RESET +" or "+ ConsoleColors.YELLOW_BOLD_BRIGHT+"logLevel <level>       "+ ConsoleColors.RESET + "Устанавливает логгер на указанный уровень логирования (all | debug | info | warn | error | fatal | off) \n"+
     ConsoleColors.YELLOW_BOLD_BRIGHT +  "q/" + ConsoleColors.RESET +" or "+ ConsoleColors.YELLOW_BOLD_BRIGHT+"quit      "+ ConsoleColors.RESET + "Разрывает активное соединение с сервером и завершает выполнение программы \n"+
     ConsoleColors.YELLOW_BOLD_BRIGHT +  "h/" + ConsoleColors.RESET +" or "+ ConsoleColors.YELLOW_BOLD_BRIGHT+"help      "+ ConsoleColors.RESET + "Справка \n");
  }

  public static void wrongСommand() {
    System.out.println(ConsoleColors.RED +"I don't know this command. Enter \"help\" for reference"+ConsoleColors.RESET);
    logger.error("Don't know this command");
  }

  public static void logLevel(String command) {
    if (command.contains(" ")) {
      int firstSpace = command.indexOf(" ");
      String level = command.substring(firstSpace + 1, command.length());
      //(ALL | DEBUG | INFO | WARN | ERROR | FATAL | OFF)
        if ("all".equals(level)) {
          logger.setLevel(Level.ALL);

          currentLoggerLevel(logger.getLevel());
        }else if("info".equals(level)){
          logger.setLevel(Level.INFO);

          currentLoggerLevel(logger.getLevel());
        }else if ("debug".equals(level)) {
          logger.setLevel(Level.DEBUG);

          currentLoggerLevel(logger.getLevel());
        }else if("warn".equals(level)){
          logger.setLevel(Level.WARN);

          currentLoggerLevel(logger.getLevel());
        }else if("error".equals(level)){
          logger.setLevel(Level.ERROR);

          currentLoggerLevel(logger.getLevel());
        }else if ("fatal".equals(level)) {
          logger.setLevel(Level.FATAL);

          currentLoggerLevel(logger.getLevel());
        }else if("OFF".equals(level)){
          logger.setLevel(Level.OFF);

          currentLoggerLevel(logger.getLevel());
        }
    }else{
      System.out.println(ConsoleColors.RED + "Error, write level!"+
              ConsoleColors.RESET);
      logger.error("Wrong enter command logLevel");
    }
  }
  public static void quit(String command,  PrintStream output,  Socket client) throws IOException {

      System.out.println(ConsoleColors.YELLOW_BOLD_BRIGHT + "Application exit! Bye :D"+
              ConsoleColors.RESET);
      logger.info("Client is exited");
      output.close();
      client.close();
      System.exit(0);

  }
  public static void quit(String command) throws IOException {

      System.out.println(ConsoleColors.YELLOW_BOLD_BRIGHT + "Application exit! Bye :D"+
              ConsoleColors.RESET);
      logger.info("Client is exited");
      System.exit(0);
  }

  public static void currentLoggerLevel(Level level) {
    System.out.println("Current logging level: " + level);
  }
}

class ReceivedMessagesHandler implements Runnable {

  private InputStream server;

  public ReceivedMessagesHandler(InputStream server) {
    this.server = server;
  }

  public void run() {
    // получать сообщения от сервера и распечатывать на экране
    Scanner s = new Scanner(server);
    String tmp = "";
    while (s.hasNextLine()) {
      tmp = s.nextLine();
      System.out.println(tmp);
      if (tmp.charAt(0) == '[') {
        tmp = tmp.substring(1, tmp.length()-1);
            }else{
        try {
          System.out.println(getTagValue(tmp));
        } catch(Exception ignore){}
      }
    }
    s.close();
  }

  public static String getTagValue(String xml){
    return  xml.split("<span>")[1].split("</span>")[0];
  }
}

class ConsoleColors {
  // Reset
  public static final String RESET = "\033[0m";  // Text Reset

  // Regular Colors
  public static final String RED = "\033[0;31m";     // RED
  public static final String YELLOW_BOLD_BRIGHT = "\033[1;93m";// YELLOW

}
