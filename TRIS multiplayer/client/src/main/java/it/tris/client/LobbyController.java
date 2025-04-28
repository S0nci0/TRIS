package it.tris.client;

import java.util.Optional;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

public class LobbyController {

    @FXML
    private TextField nicknameField;

    @FXML
    private ChoiceBox<String> difficultyChoiceBox;

    @FXML
    public void initialize() {
        difficultyChoiceBox.getItems().addAll("Facile", "Medio", "Impossibile");
        difficultyChoiceBox.setValue("Facile");
    }

    @FXML
    private void startPublicMatchmaking() {
        openGameScene(false, null, null); // multiplayer pubblico
    }

    @FXML
    private void startPrivateMatchmaking() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Partita Privata");
        dialog.setHeaderText("Inserisci la password della partita privata:");
        dialog.setContentText("Password:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(password -> {
            openGameScene(false, password, null); // multiplayer privato con password
        });
    }

    @FXML
    private void playAgainstBot() {
        BotAI.Difficulty selectedDifficulty;
        switch (difficultyChoiceBox.getValue()) {
            case "Medio":
                selectedDifficulty = BotAI.Difficulty.MEDIUM;
                break;
            case "Impossibile":
                selectedDifficulty = BotAI.Difficulty.IMPOSSIBLE;
                break;
            default:
                selectedDifficulty = BotAI.Difficulty.EASY;
                break;
        }
        openGameScene(true, null, selectedDifficulty);
    }

    private void openGameScene(boolean vsBot, String password, BotAI.Difficulty botDifficulty) {
        try {
            Stage stage = (Stage) nicknameField.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/GameView.fxml"));
            Scene scene = new Scene(loader.load());
            GameController controller = loader.getController();

            controller.setPlayerNickname(nicknameField.getText());
            if (vsBot) {
                controller.startBotGame(botDifficulty);
            } else {
                if (password != null && !password.isEmpty()) {
                    controller.startPrivateMultiplayerGame(password);
                } else {
                    controller.startPublicMultiplayerGame();
                }
            }

            stage.setScene(scene);
            stage.setTitle("Tris Online - Partita");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

