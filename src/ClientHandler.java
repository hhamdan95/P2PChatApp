import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable { // Implements Runnable so that the instances will be executed by a separate thread
    public static ArrayList < ClientHandler > clientHandlers = new ArrayList < > (); // Keep track of all our clients by broadcasting messages
    private Socket socket;
    private BufferedReader bufferedReader; // Read messages
    private BufferedWriter bufferedWriter; // Send messages
    private String clientUsername;

    public String getClientUsername() {
        return clientUsername;
    }

    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.clientUsername = bufferedReader.readLine(); // Wait for client username to be sent over from Client.java class
            clientHandlers.add(this); // Add this clientHandler to our clientHandlers list
            broadcastMessage(ConsoleColors.YELLOW + Server.getTime() + " SERVER: [" + clientUsername + "] has joined the chat!" + ConsoleColors.RESET); // Broadcast a server message when a new client joins

        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    // This method acts as separate thread to listen to messages so our program won't be stuck waiting for new messages
    @Override
    public void run() {
        String messageFromClient;

        while (socket.isConnected()) {
            try {
                messageFromClient = bufferedReader.readLine();
                broadcastMessage(messageFromClient);

            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }

    public void broadcastMessage(String messageToBroadcast) {
        for (ClientHandler clientHandler: clientHandlers) {
            try {
                if (!clientHandler.clientUsername.equals(clientUsername)) { // Make sure the client does not see his own broadcasted message
                    clientHandler.bufferedWriter.write(messageToBroadcast);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }

    public void removeClientHandler() { // Remove client from socket if he disconnects
        if (clientUsername != null) {
            clientHandlers.remove(this);
            broadcastMessage(ConsoleColors.YELLOW + Server.getTime() + " SERVER: " + clientUsername + " has left the chat!" + ConsoleColors.RESET);
            System.out.println(ConsoleColors.YELLOW + Server.getTime() + " A client has left the server! -> [Username: " + clientUsername + "]" + ConsoleColors.RESET);
        }
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        removeClientHandler();

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
}