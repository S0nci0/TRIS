package it.tris.client;

import java.util.Random;

public class BotAI {
    private final Random random = new Random();
    private final Difficulty difficulty;

    public enum Difficulty {
        EASY,
        MEDIUM,
        IMPOSSIBLE
    }

    public BotAI(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public int[] getMove(String[][] board, String botSymbol, String playerSymbol) {
        switch (difficulty) {
            case EASY:
                return randomMove(board);
            case MEDIUM:
                int[] move = winningMove(board, botSymbol);
                if (move != null) return move;
                move = blockingMove(board, playerSymbol);
                if (move != null) return move;
                return randomMove(board);
            case IMPOSSIBLE:
                return bestMove(board, botSymbol, playerSymbol);
            default:
                return randomMove(board);
        }
    }

    private int[] randomMove(String[][] board) {
        int row, col;
        do {
            row = random.nextInt(3);
            col = random.nextInt(3);
        } while (!board[row][col].isEmpty());
        return new int[]{row, col};
    }

    private int[] winningMove(String[][] board, String symbol) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j].isEmpty()) {
                    board[i][j] = symbol;
                    if (checkWin(board, symbol)) {
                        board[i][j] = "";
                        return new int[]{i, j};
                    }
                    board[i][j] = "";
                }
            }
        }
        return null;
    }

    private int[] blockingMove(String[][] board, String opponentSymbol) {
        return winningMove(board, opponentSymbol);
    }

    private boolean checkWin(String[][] board, String symbol) {
        for (int i = 0; i < 3; i++) {
            if (board[i][0].equals(symbol) && board[i][1].equals(symbol) && board[i][2].equals(symbol)) return true;
            if (board[0][i].equals(symbol) && board[1][i].equals(symbol) && board[2][i].equals(symbol)) return true;
        }
        if (board[0][0].equals(symbol) && board[1][1].equals(symbol) && board[2][2].equals(symbol)) return true;
        if (board[0][2].equals(symbol) && board[1][1].equals(symbol) && board[2][0].equals(symbol)) return true;
        return false;
    }

    private int[] bestMove(String[][] board, String botSymbol, String playerSymbol) {
        int bestScore = Integer.MIN_VALUE;
        int[] move = new int[]{-1, -1};

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j].isEmpty()) {
                    board[i][j] = botSymbol;
                    int score = minimax(board, 0, false, botSymbol, playerSymbol);
                    board[i][j] = "";
                    if (score > bestScore) {
                        bestScore = score;
                        move = new int[]{i, j};
                    }
                }
            }
        }
        return move;
    }

    private int minimax(String[][] board, int depth, boolean isMaximizing, String botSymbol, String playerSymbol) {
        if (checkWin(board, botSymbol)) return 10 - depth;
        if (checkWin(board, playerSymbol)) return depth - 10;
        if (isFull(board)) return 0;

        if (isMaximizing) {
            int bestScore = Integer.MIN_VALUE;
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (board[i][j].isEmpty()) {
                        board[i][j] = botSymbol;
                        int score = minimax(board, depth + 1, false, botSymbol, playerSymbol);
                        board[i][j] = "";
                        bestScore = Math.max(score, bestScore);
                    }
                }
            }
            return bestScore;
        } else {
            int bestScore = Integer.MAX_VALUE;
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (board[i][j].isEmpty()) {
                        board[i][j] = playerSymbol;
                        int score = minimax(board, depth + 1, true, botSymbol, playerSymbol);
                        board[i][j] = "";
                        bestScore = Math.min(score, bestScore);
                    }
                }
            }
            return bestScore;
        }
    }

    private boolean isFull(String[][] board) {
        for (String[] row : board) {
            for (String cell : row) {
                if (cell.isEmpty()) return false;
            }
        }
        return true;
    }
}
