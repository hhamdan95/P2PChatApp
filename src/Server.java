import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Server {

    private final ServerSocket serverSocket;

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void startServer() {
        System.out.println(ConsoleColors.CYAN + getTime() + " Server has started! Waiting for clients..." + ConsoleColors.RESET);

        try {
            while (!serverSocket.isClosed()) { // Ensure the server is constantly running
                Socket socket = serverSocket.accept(); // Wait for a client to connect
                ClientHandler clientHandler = new ClientHandler(socket); // Communicate with a client
                String username = clientHandler.getClientUsername();

                if (username != null) {
                    System.out.println(ConsoleColors.GREEN + getTime() + " New client has connected to the server! -> [Username: " + username + "]" + ConsoleColors.RESET);
                    Thread thread = new Thread(clientHandler); // Spawn a new thread to handle the connection of each client
                    thread.start();
                }
            }

        } catch (IOException e) {
            closeServer();
        }
    }

    public void closeServer() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getTime() {
        DateFormat dateFormat = new SimpleDateFormat("hh:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

    public static void main(String[] args) throws IOException {
        try {
            ServerSocket serverSocket = new ServerSocket(1122);
            Server server = new Server(serverSocket);
            server.startServer();

        } catch (BindException e) {
            System.out.println(ConsoleColors.YELLOW + "The server is already running on another instance!" + ConsoleColors.RESET);
        }

    }
}