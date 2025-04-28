package it.tris.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.security.KeyPair;

import javax.crypto.SecretKey;

import it.tris.common.AESUtils;
import it.tris.common.RSAUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class GameController {

    @FXML private Button btn00;
    @FXML private Button btn01;
    @FXML private Button btn02;
    @FXML private Button btn10;
    @FXML private Button btn11;
    @FXML private Button btn12;
    @FXML private Button btn20;
    @FXML private Button btn21;
    @FXML private Button btn22;
    @FXML private Button returnToLobbyButton; // 🔥 Nuovo bottone

    private String mySymbol;
    private String opponentSymbol;
    private boolean myTurn = false;
    private boolean vsBot = false;
    private BotAI bot;

    private Socket socket;
    private DataInputStream input;
    private DataOutputStream output;
    private SecretKey aesKey;

    private final Button[][] buttons = new Button[3][3];

    private String playerNickname;
    private String privatePassword;

    @FXML
    public void initialize() {
        buttons[0][0] = btn00;
        buttons[0][1] = btn01;
        buttons[0][2] = btn02;
        buttons[1][0] = btn10;
        buttons[1][1] = btn11;
        buttons[1][2] = btn12;
        buttons[2][0] = btn20;
        buttons[2][1] = btn21;
        buttons[2][2] = btn22;
    }

    public void setPlayerNickname(String nickname) {
        this.playerNickname = nickname;
    }

    public void startBotGame(BotAI.Difficulty difficulty) {
        vsBot = true;
        bot = new BotAI(difficulty);
        mySymbol = "X";
        opponentSymbol = "O";
        myTurn = true;
    }

    public void startPublicMultiplayerGame() {
        this.privatePassword = null;
        vsBot = false;
        connectToServer();
    }

    public void startPrivateMultiplayerGame(String password) {
        this.privatePassword = password;
        vsBot = false;
        connectToServer();
    }

    private void connectToServer() {
        try {
            socket = new Socket("localhost", 12345);
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());

            exchangeKeys();

            if (privatePassword == null) {
                sendMessageWithoutHandlingError("");
            } else {
                sendMessageWithoutHandlingError(privatePassword);
            }

            new Thread(this::listenForMessages).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void exchangeKeys() throws Exception {
        KeyPair keyPair = RSAUtils.generateRSAKeyPair();

        byte[] publicKeyBytes = keyPair.getPublic().getEncoded();
        output.writeInt(publicKeyBytes.length);
        output.write(publicKeyBytes);
        output.flush();

        int aesKeyLength = input.readInt();
        byte[] encryptedAESKey = new byte[aesKeyLength];
        input.readFully(encryptedAESKey);

        byte[] aesKeyBytes = RSAUtils.decrypt(encryptedAESKey, keyPair.getPrivate());
        aesKey = AESUtils.getAESKeyFromBytes(aesKeyBytes);
    }

    private void listenForMessages() {
        try {
            while (true) {
                String encryptedMessage = input.readUTF();
                String message = AESUtils.decrypt(encryptedMessage, aesKey);

                if (message.startsWith("START|")) {
                    String symbol = message.split("\\|")[1];
                    mySymbol = symbol;
                    opponentSymbol = mySymbol.equals("X") ? "O" : "X";
                    myTurn = mySymbol.equals("X");
                } else if (message.startsWith("MOVE|")) {
                    String[] parts = message.split("\\|")[1].split(",");
                    int row = Integer.parseInt(parts[0]);
                    int col = Integer.parseInt(parts[1]);

                    Platform.runLater(() -> buttons[row][col].setText(opponentSymbol));
                    myTurn = true;
                } else if (message.startsWith("WIN|")) {
                    String winner = message.split("\\|")[1];
                    if (winner.equals(mySymbol)) {
                        showWinMessage("Ha vinto " + playerNickname + "!");
                    } else if (winner.equals("DISCONNECT")) {
                        showWinMessage("Il tuo avversario si è disconnesso!");
                    } else {
                        showWinMessage("Ha vinto il tuo avversario!");
                    }
                    socket.close();
                } else if (message.equals("DRAW")) {
                    showWinMessage("Pareggio!");
                    socket.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleMove(javafx.event.ActionEvent event) {
        if (!myTurn) return;

        Button clickedButton = (Button) event.getSource();
        if (!clickedButton.getText().isEmpty()) return;

        int row = -1, col = -1;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (buttons[i][j] == clickedButton) {
                    row = i;
                    col = j;
                }
            }
        }

        if (row != -1 && col != -1) {
            clickedButton.setText(mySymbol);
            if (vsBot) {
                if (checkWin(mySymbol)) {
                    showWinMessage("Ha vinto " + playerNickname + "!");
                } else if (isBoardFull()) {
                    showWinMessage("Pareggio!");
                } else {
                    botMove();
                }
            } else {
                sendMessage("MOVE|" + row + "," + col);
                myTurn = false;

                if (checkWin(mySymbol)) {
                    sendMessage("WIN|" + mySymbol);
                    showWinMessage("Ha vinto " + playerNickname + "!");
                } else if (isBoardFull()) {
                    sendMessage("DRAW");
                    showWinMessage("Pareggio!");
                }
            }
        }
    }

    private void botMove() {
        String[][] board = new String[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = buttons[i][j].getText();
            }
        }
        int[] move = bot.getMove(board, opponentSymbol, mySymbol);
        if (move != null) {
            buttons[move[0]][move[1]].setText(opponentSymbol);
            if (checkWin(opponentSymbol)) {
                showWinMessage("Ha vinto il bot!");
            } else if (isBoardFull()) {
                showWinMessage("Pareggio!");
            } else {
                myTurn = true;
            }
        }
    }

    private void sendMessage(String message) {
        try {
            String encrypted = AESUtils.encrypt(message, aesKey);
            output.writeUTF(encrypted);
            output.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendMessageWithoutHandlingError(String message) {
        try {
            String encrypted = AESUtils.encrypt(message, aesKey);
            output.writeUTF(encrypted);
            output.flush();
        } catch (Exception ignored) {
        }
    }

    private boolean checkWin(String symbol) {
        for (int i = 0; i < 3; i++) {
            if (buttons[i][0].getText().equals(symbol) && buttons[i][1].getText().equals(symbol) && buttons[i][2].getText().equals(symbol)) return true;
            if (buttons[0][i].getText().equals(symbol) && buttons[1][i].getText().equals(symbol) && buttons[2][i].getText().equals(symbol)) return true;
        }
        if (buttons[0][0].getText().equals(symbol) && buttons[1][1].getText().equals(symbol) && buttons[2][2].getText().equals(symbol)) return true;
        if (buttons[0][2].getText().equals(symbol) && buttons[1][1].getText().equals(symbol) && buttons[2][0].getText().equals(symbol)) return true;
        return false;
    }

    private boolean isBoardFull() {
        for (Button[] row : buttons) {
            for (Button btn : row) {
                if (btn.getText().isEmpty()) return false;
            }
        }
        return true;
    }

    private void showWinMessage(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Partita Finita");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();

            returnToLobbyButton.setVisible(true); // 🔥 Mostra bottone torna alla lobby
        });
    }

    @FXML
    private void returnToLobby() {
        try {
            Stage stage = (Stage) returnToLobbyButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LobbyView.fxml"));
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
            stage.setTitle("Tris Online - Lobby");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
