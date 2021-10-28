import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {

    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;

    public Client(Socket socket, String username) {
        try {
            this.socket = socket;
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.username = username;

        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public void sendMessage() {
        try {
            bufferedWriter.write(username);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            Scanner scanner = new Scanner(System.in); // Getting input from client

            while (socket.isConnected()) {
                String messageToSend = scanner.nextLine();
                bufferedWriter.write(Server.getTime() + " [" + username + "]: " + messageToSend);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }

        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public void listenToMessages() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String msgFromGroupChat;

                while (socket.isConnected()) {
                    try {
                        msgFromGroupChat = bufferedReader.readLine();
                        System.out.println(msgFromGroupChat);

                    } catch (IOException e) {
                        closeEverything(socket, bufferedReader, bufferedWriter);
                    }
                }
            }
        }).start(); // Starting our thread
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        try {
            if (socket != null) {
                socket.close();
            }
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        try {
            Socket socket = new Socket("localhost", 1122);

            Scanner scanner = new Scanner(System.in);
            System.out.println(ConsoleColors.CYAN + "Please provide a username for the chat: " + ConsoleColors.RESET);
            String username = scanner.nextLine();

            while (username == null || username.trim().isEmpty()) {
                System.out.println(ConsoleColors.YELLOW + "Username cannot be empty! Please try again: " + ConsoleColors.RESET);
                username = scanner.nextLine();
            }

            if (!username.isEmpty()) {
                Client client = new Client(socket, username);
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
                new ProcessBuilder("cmd", "/c", "type logo.txt").inheritIO().start().waitFor();
                System.out.println(ConsoleColors.PURPLE + "You are connected to 127.0.0.1:1122, to quit press ctrl + C or close the console." + ConsoleColors.RESET);
                System.out.println(ConsoleColors.CYAN + "Welcome to the chatroom " + username + ", say hi!" + ConsoleColors.RESET);
                client.listenToMessages();
                client.sendMessage();
            }

        } catch (ConnectException e) {
            System.out.println(ConsoleColors.RED + "Error: Server is offline! Please make sure to start the server first!" + ConsoleColors.RESET);
        }
    }
}