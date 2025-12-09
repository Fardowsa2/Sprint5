package jonin;

/**
 * Simple SOS game: ends immediately when anyone forms an SOS.
 */
public class SimpleSOSGame extends AbstractSOSGame {
    private Player winner;

    public SimpleSOSGame(int boardSize) {
        super(boardSize);
        winner = null;
    }

    @Override
    protected void resetScores() {
        // no score tracking in simple mode
    }

    @Override
    protected void processMove(int row, int col) {
        int sos = checkForSOS(row, col);
        if (sos > 0) {
            gameOver = true;
            winner = currentPlayer;
        } else if (isBoardFull()) {
            gameOver = true;
            winner = null;  // draw
        } else {
            currentPlayer = (currentPlayer == Player.BLUE) ? Player.RED : Player.BLUE;
        }
    }

    @Override
    public String getGameStatus() {
        if (!gameOver) return "Ongoing: " + currentPlayer + " to move.";
        return (winner != null)
            ? "Game over: " + winner + " wins!"
            : "Game over: Draw.";
    }

    public Player getWinner() { return winner; }
}