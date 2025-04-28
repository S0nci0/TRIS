package it.tris.server;

import java.util.LinkedList;
import java.util.Queue;

public class LobbyManager {
    private final Queue<ClientHandler> waitingClients = new LinkedList<>();

    public synchronized void addClient(ClientHandler client) {
        waitingClients.add(client);
        tryPairing();
    }

    private void tryPairing() {
        if (waitingClients.size() < 2) {
            return;
        }

        ClientHandler[] array = waitingClients.toArray(new ClientHandler[0]);
        for (int i = 0; i < array.length; i++) {
            for (int j = i + 1; j < array.length; j++) {
                ClientHandler c1 = array[i];
                ClientHandler c2 = array[j];

                if (canMatch(c1, c2)) {
                    waitingClients.remove(c1);
                    waitingClients.remove(c2);

                    startGame(c1, c2);
                    return;
                }
            }
        }
    }

    private boolean canMatch(ClientHandler c1, ClientHandler c2) {
        if (c1.getPrivatePassword() == null && c2.getPrivatePassword() == null) {
            return true; 
        }
        if (c1.getPrivatePassword() != null && c1.getPrivatePassword().equals(c2.getPrivatePassword())) {
            return true; 
        }
        return false; 
    }

    private void startGame(ClientHandler player1, ClientHandler player2) {
        player1.setOpponent(player2);
        player2.setOpponent(player1);

        player1.sendStartMessage("X");
        player2.sendStartMessage("O");

        player1.setMyTurn(true); 
        player2.setMyTurn(false);
    }
}
