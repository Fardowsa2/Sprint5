package jonin;

import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JToggleButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JLayeredPane;
import javax.swing.Timer;
import javax.swing.BoxLayout;
import javax.swing.Box;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Container;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.Color;

import java.awt.geom.Line2D;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.List;
import java.util.ArrayList;

public class SOSGui extends JFrame {
    private AbstractSOSGame game;
    private CellButton[][] cells;
    private PlayerStrategy blueStrategy, redStrategy;
    private JRadioButton simpleModeRadio, generalModeRadio;
    private JComboBox<Integer> boardSizeCombo;
    private JRadioButton blueHuman, blueComputer, blueS, blueO;
    private JRadioButton redHuman, redComputer, redS, redO;
    private JLabel statusLabel;

    private JToggleButton recordToggle;
    private JButton replayButton;
    private List<String> moveHistory = new ArrayList<>();
    private boolean recording = false;
    private boolean isReplaying = false;
    private List<String> replayMoves;
    private int replayIndex;
    private Timer replayTimer;

    // lists to store SOS lines and their colors for drawing
    private List<Line2D> sosLines = new ArrayList<>();
    private List<Color> sosColors = new ArrayList<>();

    public SOSGui() {
        super("SOS Game - Sprint 5");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(850, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Top Panel: Game mode, board size, New Game, Record, Replay
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        simpleModeRadio = new JRadioButton("Simple game", true);
        generalModeRadio = new JRadioButton("General game");
        ButtonGroup modeGroup = new ButtonGroup();
        modeGroup.add(simpleModeRadio);
        modeGroup.add(generalModeRadio);
        top.add(simpleModeRadio);
        top.add(generalModeRadio);

        top.add(new JLabel("Board size"));
        boardSizeCombo = new JComboBox<>(new Integer[]{3,4,5,6,7,8});
        boardSizeCombo.setSelectedIndex(5);
        top.add(boardSizeCombo);

        JButton newGame = new JButton("New Game");
        newGame.addActionListener(e -> startNewGame());
        top.add(newGame);

        recordToggle = new JToggleButton("Record");
        recordToggle.addActionListener(e -> toggleRecording(recordToggle.isSelected()));
        top.add(recordToggle);

        replayButton = new JButton("Replay");
        replayButton.addActionListener(e -> startReplay());
        top.add(replayButton);

        add(top, BorderLayout.NORTH);

        // Left Panel: Blue controls
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(Box.createVerticalStrut(20));
        left.add(new JLabel("Blue player"));
        blueHuman   = new JRadioButton("Human", true);
        blueComputer= new JRadioButton("Computer");
        ButtonGroup blueType = new ButtonGroup();
        blueType.add(blueHuman);
        blueType.add(blueComputer);
        left.add(blueHuman);
        left.add(blueComputer);
        blueS = new JRadioButton("S", true);
        blueO = new JRadioButton("O");
        ButtonGroup blueLetter = new ButtonGroup();
        blueLetter.add(blueS);
        blueLetter.add(blueO);
        left.add(blueS);
        left.add(blueO);
        add(left, BorderLayout.WEST);

        // Right Panel: Red controls
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.add(Box.createVerticalStrut(20));
        right.add(new JLabel("Red player"));
        redHuman    = new JRadioButton("Human", true);
        redComputer = new JRadioButton("Computer");
        ButtonGroup redType = new ButtonGroup();
        redType.add(redHuman);
        redType.add(redComputer);
        right.add(redHuman);
        right.add(redComputer);
        redS = new JRadioButton("S", true);
        redO = new JRadioButton("O");
        ButtonGroup redLetter = new ButtonGroup();
        redLetter.add(redS);
        redLetter.add(redO);
        right.add(redS);
        right.add(redO);
        add(right, BorderLayout.EAST);

        // Bottom Panel: Status
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        statusLabel = new JLabel("Game not started.");
        bottom.add(statusLabel);
        add(bottom, BorderLayout.SOUTH);

        startNewGame();
        setVisible(true);
    }

    private void startNewGame() {
        int size = (Integer) boardSizeCombo.getSelectedItem();
        game = simpleModeRadio.isSelected()
             ? new SimpleSOSGame(size)
             : new GeneralSOSGame(size);

        blueStrategy = blueComputer.isSelected() ? new ComputerStrategy() : null;
        redStrategy  = redComputer .isSelected() ? new ComputerStrategy() : null;

        blueS.setEnabled(blueHuman.isSelected());
        blueO.setEnabled(blueHuman.isSelected());
        redS .setEnabled(redHuman.isSelected());
        redO .setEnabled(redHuman.isSelected());

        sosLines.clear();
        sosColors.clear();

        rebuildBoard(size);
        statusLabel.setText("Game started. Current turn: " + game.getCurrentPlayer());

        // Initialize recording (include player types)
        if (recording) {
            moveHistory.clear();
            String mode   = simpleModeRadio.isSelected()  ? "SIMPLE"  : "GENERAL";
            String bluePT = blueComputer.isSelected()     ? "Computer": "Human";
            String redPT  = redComputer.isSelected()      ? "Computer": "Human";
            moveHistory.add(mode + "," + size + "," + bluePT + "," + redPT);
        }

        if (!isReplaying) {
            maybePerformAIMove();
        }
    }

    private void rebuildBoard(int size) {
        Container c = getContentPane();
        Component old = ((BorderLayout)c.getLayout()).getLayoutComponent(BorderLayout.CENTER);
        if (old != null) c.remove(old);

        JLayeredPane layer = new JLayeredPane();
        int cellSize = 60;
        layer.setPreferredSize(new Dimension(size * cellSize, size * cellSize));
        cells = new CellButton[size][size];

        for (int r = 0; r < size; r++) {
            for (int col = 0; col < size; col++) {
                CellButton btn = new CellButton(r, col);
                btn.setBounds(col * cellSize, r * cellSize, cellSize, cellSize);
                btn.setFont(btn.getFont().deriveFont(20f));
                btn.addActionListener(e -> performHumanMove(btn.row, btn.col));
                layer.add(btn, JLayeredPane.DEFAULT_LAYER);
                cells[r][col] = btn;
            }
        }

        JPanel draw = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                for (int i = 0; i < sosLines.size(); i++) {
                    g2.setStroke(new BasicStroke(4));
                    g2.setColor(sosColors.get(i));
                    g2.draw(sosLines.get(i));
                }
            }
        };
        draw.setOpaque(false);
        draw.setBounds(0, 0, size * cellSize, size * cellSize);
        layer.add(draw, JLayeredPane.PALETTE_LAYER);

        c.add(layer, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private void performHumanMove(int row, int col) {
        if (game.isGameOver() || isReplaying) return;
        boolean blueAI = blueComputer.isSelected()
                       && game.getCurrentPlayer() == AbstractSOSGame.Player.BLUE;
        boolean redAI  = redComputer.isSelected()
                       && game.getCurrentPlayer() == AbstractSOSGame.Player.RED;
        if (blueAI || redAI) return;

        AbstractSOSGame.Player player = game.getCurrentPlayer();
        char letter = (player == AbstractSOSGame.Player.BLUE)
                    ? (blueS.isSelected() ? 'S' : 'O')
                    : (redS .isSelected() ? 'S' : 'O');
        applyMove(row, col, letter, player);
    }

    private void maybePerformAIMove() {
        if (game.isGameOver() || isReplaying) return;
        AbstractSOSGame.Player cur = game.getCurrentPlayer();
        PlayerStrategy strat = (cur == AbstractSOSGame.Player.BLUE)
                             ? blueStrategy
                             : redStrategy;
        if (strat != null) {
            Move m = strat.chooseMove(game, cur);
            applyMove(m.getRow(), m.getCol(), m.getLetter(), cur);
        }
    }

    private void applyMove(int row, int col, char letter, AbstractSOSGame.Player player) {
        if (!game.placeLetter(row, col, letter)) return;

        if (recording && !isReplaying) {
            moveHistory.add(row + "," + col + "," + letter);
        }

        CellButton btn = cells[row][col];
        btn.setText(String.valueOf(letter));
        detectSOSLines(row, col, player);
        statusLabel.setText(game.getGameStatus());

        if (!game.isGameOver() && !isReplaying) {
            maybePerformAIMove();
        } else if (game.isGameOver() && !isReplaying) {
            JOptionPane.showMessageDialog(this, game.getGameStatus());
        }
    }

    private void detectSOSLines(int row, int col, AbstractSOSGame.Player player) {
        int size = game.getBoardSize();
        int[] dr = {-1,-1,-1, 0,0, 1,1,1};
        int[] dc = {-1, 0, 1,-1,1,-1,0,1};

        for (int i = 0; i < 8; i++) {
            String now = cells[row][col].getText();
            if ("S".equals(now)) {
                int r1 = row + dr[i], c1 = col + dc[i];
                int r2 = row + 2*dr[i], c2 = col + 2*dc[i];
                if (inBounds(r1,c1,size) && inBounds(r2,c2,size)
                 && "O".equals(cells[r1][c1].getText())
                 && "S".equals(cells[r2][c2].getText())) {
                    addLine(row, col, r2, c2, player);
                }
            }
            if ("O".equals(now)) {
                int r0 = row - dr[i], c0 = col - dc[i];
                int r1 = row + dr[i], c1 = col + dc[i];
                if (inBounds(r0,c0,size) && inBounds(r1,c1,size)
                 && "S".equals(cells[r0][c0].getText())
                 && "S".equals(cells[r1][c1].getText())) {
                    addLine(r0, c0, r1, c1, player);
                }
            }
        }
        repaint();
    }

    private boolean inBounds(int r, int c, int size) {
        return r >= 0 && r < size && c >= 0 && c < size;
    }

    private void addLine(int r1, int c1, int r2, int c2, AbstractSOSGame.Player player) {
        int cellSize = 60;
        double x1 = c1*cellSize + cellSize/2.0, y1 = r1*cellSize + cellSize/2.0;
        double x2 = c2*cellSize + cellSize/2.0, y2 = r2*cellSize + cellSize/2.0;
        sosLines.add(new Line2D.Double(x1,y1,x2,y2));
        sosColors.add(
            player == AbstractSOSGame.Player.BLUE ? Color.BLUE : Color.RED
        );
    }

    private void toggleRecording(boolean on) {
        recording = on;
        if (on) {
            moveHistory.clear();
            statusLabel.setText("Recording enabled.");
        } else {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File f = chooser.getSelectedFile();
                try (PrintWriter out = new PrintWriter(f)) {
                    for (String line : moveHistory) out.println(line);
                    statusLabel.setText("Saved " + moveHistory.size()
                                      + " moves to " + f.getName());
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Error saving: " + ex);
                }
            }
        }
    }

    private void startReplay() {
        if (isReplaying) return;
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File f = chooser.getSelectedFile();
        try (BufferedReader in = new BufferedReader(new FileReader(f))) {
            replayMoves = new ArrayList<>();
            String line;
            while ((line = in.readLine()) != null) replayMoves.add(line);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error reading: " + ex);
            return;
        }
        if (replayMoves.isEmpty()) return;

        // parse header: mode, size, blue type, red type
        String[] hdr = replayMoves.get(0).split(",");
        boolean simple = "SIMPLE".equals(hdr[0]);
        int size       = Integer.parseInt(hdr[1]);
        String bluePT  = hdr[2], redPT = hdr[3];

        simpleModeRadio.setSelected(simple);
        generalModeRadio.setSelected(!simple);
        boardSizeCombo.setSelectedItem(size);

        blueHuman.setSelected(bluePT.equals("Human"));
        blueComputer.setSelected(bluePT.equals("Computer"));
        redHuman.setSelected(redPT.equals("Human"));
        redComputer.setSelected(redPT.equals("Computer"));

        // disable controls during replay
        isReplaying = true;
        recordToggle.setEnabled(false);
        replayButton.setEnabled(false);
        simpleModeRadio.setEnabled(false);
        generalModeRadio.setEnabled(false);
        boardSizeCombo.setEnabled(false);
        blueHuman.setEnabled(false);
        blueComputer.setEnabled(false);
        blueS.setEnabled(false);
        blueO.setEnabled(false);
        redHuman.setEnabled(false);
        redComputer.setEnabled(false);
        redS.setEnabled(false);
        redO.setEnabled(false);

        startNewGame();  // resets board & strategies

        replayIndex = 1; // skip header
        replayTimer = new Timer(600, evt -> {
            if (replayIndex >= replayMoves.size()) {
                replayTimer.stop();
                isReplaying = false;

                // show final result
                String result = game.getGameStatus();
                statusLabel.setText("Replay finished: " + result);
                JOptionPane.showMessageDialog(this, result);

                // re-enable controls
                recordToggle.setEnabled(true);
                replayButton.setEnabled(true);
                simpleModeRadio.setEnabled(true);
                generalModeRadio.setEnabled(true);
                boardSizeCombo.setEnabled(true);
                blueHuman.setEnabled(true);
                blueComputer.setEnabled(true);
                blueS.setEnabled(blueHuman.isSelected());
                blueO.setEnabled(blueHuman.isSelected());
                redHuman.setEnabled(true);
                redComputer.setEnabled(true);
                redS.setEnabled(redHuman.isSelected());
                redO.setEnabled(redHuman.isSelected());
                return;
            }
            String[] parts = replayMoves.get(replayIndex++).split(",");
            int r = Integer.parseInt(parts[0]), c = Integer.parseInt(parts[1]);
            char L = parts[2].charAt(0);
            applyMove(r, c, L, game.getCurrentPlayer());
        });
        statusLabel.setText("Replaying...");
        replayTimer.start();
    }

    private static class CellButton extends JButton {
        final int row, col;
        CellButton(int r, int c) { super(""); row = r; col = c; }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SOSGui::new);
    }
}