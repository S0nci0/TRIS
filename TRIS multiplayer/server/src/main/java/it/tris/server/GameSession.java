package it.tris.server;

public class GameSession {
    private String[][] board = new String[3][3];
    private String currentPlayer;

    public GameSession() {
        
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = "";
            }
        }
        currentPlayer = "X";
    }

    public boolean makeMove(int row, int col, String playerSymbol) {
        if (board[row][col].isEmpty()) {
            board[row][col] = playerSymbol;
            currentPlayer = playerSymbol.equals("X") ? "O" : "X";
            return true;
        }
        return false;
    }

    public boolean checkWin(String playerSymbol) {
        
        for (int i = 0; i < 3; i++) {
            if (board[i][0].equals(playerSymbol) && board[i][1].equals(playerSymbol) && board[i][2].equals(playerSymbol)) return true;
            if (board[0][i].equals(playerSymbol) && board[1][i].equals(playerSymbol) && board[2][i].equals(playerSymbol)) return true;
        }
        if (board[0][0].equals(playerSymbol) && board[1][1].equals(playerSymbol) && board[2][2].equals(playerSymbol)) return true;
        if (board[0][2].equals(playerSymbol) && board[1][1].equals(playerSymbol) && board[2][0].equals(playerSymbol)) return true;
        return false;
    }

    public boolean isBoardFull() {
        for (String[] row : board) {
            for (String cell : row) {
                if (cell.isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    public String getCurrentPlayer() {
        return currentPlayer;
    }
}
