package jonin;

/**
 * Strategy pattern for computing AI moves.
 */
public interface PlayerStrategy {
    Move chooseMove(AbstractSOSGame game, AbstractSOSGame.Player player);
}