package it.tris.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerMain {
    public static void main(String[] args) {
        int port = 12345;
        LobbyManager lobbyManager = new LobbyManager();

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server avviato sulla porta " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nuovo client connesso: " + clientSocket.getInetAddress());
                ClientHandler clientHandler = new ClientHandler(clientSocket, lobbyManager);
                clientHandler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
