package it.tris.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PublicKey;

import javax.crypto.SecretKey;

import it.tris.common.AESUtils;
import it.tris.common.RSAUtils;

public class ClientHandler extends Thread {
    private final Socket socket;
    private final LobbyManager lobbyManager;
    private ClientHandler opponent;
    private boolean myTurn;
    private DataInputStream input;
    private DataOutputStream output;

    private SecretKey aesKey;
    private String privatePassword; 

    private static final KeyPair serverKeyPair = RSAUtils.generateRSAKeyPair();

    public ClientHandler(Socket socket, LobbyManager lobbyManager) {
        this.socket = socket;
        this.lobbyManager = lobbyManager;
    }

    public void setOpponent(ClientHandler opponent) {
        this.opponent = opponent;
    }

    public void setMyTurn(boolean myTurn) {
        this.myTurn = myTurn;
    }

    public String getPrivatePassword() {
        return privatePassword;
    }

    @Override
    public void run() {
        try {
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());

            exchangeKeys();

            String encryptedPassword = input.readUTF();
            String password = AESUtils.decrypt(encryptedPassword, aesKey);
            if (password != null && !password.isEmpty()) {
                this.privatePassword = password;
            } else {
                this.privatePassword = null;
            }

            lobbyManager.addClient(this);

            while (true) {
                String encryptedMessage = input.readUTF();
                String message = AESUtils.decrypt(encryptedMessage, aesKey);

                if (message.startsWith("MOVE|")) {
                    if (myTurn && opponent != null) {
                        opponent.sendEncryptedMessage(message);
                        myTurn = false;
                        opponent.setMyTurn(true);
                    }
                } else if (message.startsWith("WIN|") || message.equals("DRAW")) {
                    if (opponent != null) {
                        opponent.sendEncryptedMessage(message);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Client disconnesso.");
            try {
                if (opponent != null) {
                    opponent.sendEncryptedMessage("WIN|DISCONNECT");
                }
                socket.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void exchangeKeys() throws Exception {
        int keyLength = input.readInt();
        byte[] clientPublicKeyBytes = new byte[keyLength];
        input.readFully(clientPublicKeyBytes);
        PublicKey clientPublicKey = RSAUtils.getPublicKeyFromBytes(clientPublicKeyBytes);

        aesKey = AESUtils.generateAESKey();

        byte[] encryptedAESKey = RSAUtils.encrypt(aesKey.getEncoded(), clientPublicKey);

        output.writeInt(encryptedAESKey.length);
        output.write(encryptedAESKey);
        output.flush();
    }

    public void sendEncryptedMessage(String message) {
        try {
            String encrypted = AESUtils.encrypt(message, aesKey);
            output.writeUTF(encrypted);
            output.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendStartMessage(String symbol) {
        sendEncryptedMessage("START|" + symbol);
    }
}
