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
import javax.swing.SwingConstants;
import javax.swing.JSlider;

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
import java.text.SimpleDateFormat;
import java.util.Date;
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
    private JLabel replayStatusLabel;

    private JToggleButton recordToggle;
    private JButton replayButton, pauseButton;
    private JSlider replaySpeedSlider;
    private List<String> moveHistory = new ArrayList<>();
    private boolean recording = false;
    private boolean isReplaying = false;
    private boolean replayPaused = false;
    private List<String> replayMoves;
    private int replayIndex;
    private Timer replayTimer;

    // lists to store SOS lines and their colors for drawing
    private List<Line2D> sosLines = new ArrayList<>();
    private List<Color> sosColors = new ArrayList<>();

    public SOSGui() {
        super("SOS Game - Sprint 5");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(950, 700);
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

        pauseButton = new JButton("Pause");
        pauseButton.setEnabled(false);
        pauseButton.addActionListener(e -> toggleReplayPause());
        top.add(pauseButton);

        top.add(new JLabel("Speed:"));
        replaySpeedSlider = new JSlider(SwingConstants.HORIZONTAL, 100, 2000, 600);
        replaySpeedSlider.setMajorTickSpacing(500);
        replaySpeedSlider.setPaintTicks(true);
        replaySpeedSlider.setPaintLabels(true);
        replaySpeedSlider.setEnabled(false);
        replaySpeedSlider.addChangeListener(e -> {
            if (replayTimer != null && !replayPaused) {
                replayTimer.setDelay(replaySpeedSlider.getValue());
            }
        });
        top.add(replaySpeedSlider);

        replayStatusLabel = new JLabel("");
        replayStatusLabel.setForeground(Color.BLUE);
        top.add(replayStatusLabel);

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
        replayStatusLabel.setText("");

        rebuildBoard(size);
        statusLabel.setText("Game started. Current turn: " + game.getCurrentPlayer());

        // Initialize recording with better header
        if (recording && !isReplaying) {
            moveHistory.clear();
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            String mode = simpleModeRadio.isSelected() ? "SIMPLE" : "GENERAL";
            String blueType = blueComputer.isSelected() ? "Computer" : "Human";
            String redType = redComputer.isSelected() ? "Computer" : "Human";
            
            // Enhanced header with mode, size, player types, and timestamp
            moveHistory.add("#SOS_RECORDING_V2.0");
            moveHistory.add("#Mode:" + mode);
            moveHistory.add("#BoardSize:" + size);
            moveHistory.add("#BluePlayer:" + blueType);
            moveHistory.add("#RedPlayer:" + redType);
            moveHistory.add("#Timestamp:" + timestamp);
            moveHistory.add("#Moves:");
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
            // Store move with player information
            moveHistory.add(row + "," + col + "," + letter + "," + player);
        }

        CellButton btn = cells[row][col];
        btn.setText(String.valueOf(letter));
        
        // Update the radio buttons to show what was played
        if (player == AbstractSOSGame.Player.BLUE) {
            blueS.setSelected(letter == 'S');
            blueO.setSelected(letter == 'O');
        } else {
            redS.setSelected(letter == 'S');
            redO.setSelected(letter == 'O');
        }
        
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
            statusLabel.setText("Recording enabled. Game will be saved when recording stops.");
            
            // Initialize with header
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            String mode = simpleModeRadio.isSelected() ? "SIMPLE" : "GENERAL";
            String blueType = blueComputer.isSelected() ? "Computer" : "Human";
            String redType = redComputer.isSelected() ? "Computer" : "Human";
            
            moveHistory.add("#SOS_RECORDING_V2.0");
            moveHistory.add("#Mode:" + mode);
            moveHistory.add("#BoardSize:" + game.getBoardSize());
            moveHistory.add("#BluePlayer:" + blueType);
            moveHistory.add("#RedPlayer:" + redType);
            moveHistory.add("#Timestamp:" + timestamp);
            moveHistory.add("#Moves:");
        } else {
            if (moveHistory.size() <= 7) { // Only header lines
                statusLabel.setText("No moves recorded.");
                return;
            }
            
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Save Game Recording");
            chooser.setSelectedFile(new File("sos_game_" + 
                new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".sos"));
            
            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File f = chooser.getSelectedFile();
                // Ensure .sos extension
                if (!f.getName().toLowerCase().endsWith(".sos")) {
                    f = new File(f.getAbsolutePath() + ".sos");
                }
                
                try (PrintWriter out = new PrintWriter(f)) {
                    for (String line : moveHistory) {
                        out.println(line);
                    }
                    int moveCount = moveHistory.size() - 7; // Subtract header lines
                    statusLabel.setText("Saved " + moveCount + " moves to " + f.getName());
                    JOptionPane.showMessageDialog(this, 
                        "Game recording saved successfully!\n" +
                        "File: " + f.getName() + "\n" +
                        "Total moves: " + moveCount,
                        "Recording Saved",
                        JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, 
                        "Error saving recording: " + ex.getMessage(),
                        "Save Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void startReplay() {
        if (isReplaying) return;
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Load Game Recording");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "SOS Game Files (*.sos)", "sos"));
        
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        
        File f = chooser.getSelectedFile();
        try (BufferedReader in = new BufferedReader(new FileReader(f))) {
            replayMoves = new ArrayList<>();
            String line;
            while ((line = in.readLine()) != null) replayMoves.add(line);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, 
                "Error reading file: " + ex.getMessage(),
                "Read Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (replayMoves.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "File is empty!",
                "Format Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Check for valid recording format
        if (!replayMoves.get(0).startsWith("#SOS_RECORDING_V2.0")) {
            JOptionPane.showMessageDialog(this, 
                "Invalid recording file format!",
                "Format Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Parse header information
        String mode = "SIMPLE";
        int size = 3;
        String blueType = "Human";
        String redType = "Human";
        
        for (String line : replayMoves) {
            if (line.startsWith("#Mode:")) {
                mode = line.substring(6).trim();
            } else if (line.startsWith("#BoardSize:")) {
                size = Integer.parseInt(line.substring(11).trim());
            } else if (line.startsWith("#BluePlayer:")) {
                blueType = line.substring(12).trim();
            } else if (line.startsWith("#RedPlayer:")) {
                redType = line.substring(11).trim();
            } else if (line.equals("#Moves:")) {
                break; // Stop parsing after moves header
            }
        }

        // Display replay mode
        String modeText = mode.equals("SIMPLE") ? "Simple Mode" : "General Mode";
        replayStatusLabel.setText("Replaying: " + modeText);
        replayStatusLabel.setForeground(Color.BLUE);

        // Set game parameters from recording
        simpleModeRadio.setSelected(mode.equals("SIMPLE"));
        generalModeRadio.setSelected(mode.equals("GENERAL"));
        boardSizeCombo.setSelectedItem(size);

        blueHuman.setSelected(blueType.equals("Human"));
        blueComputer.setSelected(blueType.equals("Computer"));
        redHuman.setSelected(redType.equals("Human"));
        redComputer.setSelected(redType.equals("Computer"));

        // Disable controls during replay
        isReplaying = true;
        replayPaused = false;
        recordToggle.setEnabled(false);
        replayButton.setEnabled(false);
        pauseButton.setEnabled(true);
        pauseButton.setText("Pause");
        replaySpeedSlider.setEnabled(true);
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

        // Clear the board first
        sosLines.clear();
        sosColors.clear();
        
        // Start new game with recorded settings
        game = mode.equals("SIMPLE") 
             ? new SimpleSOSGame(size) 
             : new GeneralSOSGame(size);
        
        rebuildBoard(size);
        
        // Find first move (skip header lines)
        replayIndex = 0;
        while (replayIndex < replayMoves.size()) {
            if (replayMoves.get(replayIndex).equals("#Moves:")) {
                replayIndex++; // Skip the #Moves: line
                break;
            }
            replayIndex++;
        }

        replayTimer = new Timer(replaySpeedSlider.getValue(), evt -> {
            if (replayPaused) return;
            
            if (replayIndex >= replayMoves.size()) {
                finishReplay();
                return;
            }
            
            String move = replayMoves.get(replayIndex++);
            if (move.startsWith("#") || move.trim().isEmpty()) {
                // Skip any comment or empty lines
                return;
            }
            
            try {
                String[] parts = move.split(",");
                if (parts.length >= 4) { // row,col,letter,player
                    int r = Integer.parseInt(parts[0].trim());
                    int c = Integer.parseInt(parts[1].trim());
                    char L = parts[2].trim().charAt(0);
                    
                    // Get player from the move
                    AbstractSOSGame.Player movePlayer = null;
                    String playerStr = parts[3].trim();
                    if (playerStr.equals("BLUE")) {
                        movePlayer = AbstractSOSGame.Player.BLUE;
                    } else if (playerStr.equals("RED")) {
                        movePlayer = AbstractSOSGame.Player.RED;
                    }
                    
                    if (movePlayer != null) {
                        // Apply the move
                        if (game.placeLetter(r, c, L)) {
                            // Update UI
                            CellButton btn = cells[r][c];
                            btn.setText(String.valueOf(L));
                            
                            // Check for SOS and draw lines
                            if (movePlayer == AbstractSOSGame.Player.BLUE) {
                                blueS.setSelected(L == 'S');
                                blueO.setSelected(L == 'O');
                            } else {
                                redS.setSelected(L == 'S');
                                redO.setSelected(L == 'O');
                            }
                            
                            // Detect and draw SOS lines
                            detectSOSLines(r, c, movePlayer);
                            statusLabel.setText(game.getGameStatus());
                            
                            // Update replay status
                            int totalMoves = countTotalMoves();
                            int currentMove = replayIndex - findMovesStartIndex();
                            if (currentMove < 0) currentMove = 0;
                            if (currentMove > totalMoves) currentMove = totalMoves;
                            replayStatusLabel.setText("Replaying " + modeText + " - Move " + 
                                currentMove + "/" + totalMoves);
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Error parsing move: " + move + " - " + e.getMessage());
                JOptionPane.showMessageDialog(this, 
                    "Error parsing move at line " + replayIndex + ": " + e.getMessage(),
                    "Parse Error",
                    JOptionPane.ERROR_MESSAGE);
                finishReplay();
            }
        });
        
        statusLabel.setText("Replay started - " + modeText);
        replayTimer.start();
    }

    // Helper method to count total moves in the replay file
    private int countTotalMoves() {
        if (replayMoves == null) return 0;
        int count = 0;
        boolean inMovesSection = false;
        for (String line : replayMoves) {
            if (line.equals("#Moves:")) {
                inMovesSection = true;
                continue;
            }
            if (inMovesSection && !line.startsWith("#") && !line.trim().isEmpty()) {
                count++;
            }
        }
        return count;
    }

    // Helper method to find where moves start
    private int findMovesStartIndex() {
        if (replayMoves == null) return 0;
        for (int i = 0; i < replayMoves.size(); i++) {
            if (replayMoves.get(i).equals("#Moves:")) {
                return i + 1; // Return index after #Moves: line
            }
        }
        return 0;
    }

    private void toggleReplayPause() {
        if (!isReplaying) return;
        
        replayPaused = !replayPaused;
        if (replayPaused) {
            pauseButton.setText("Resume");
            replayTimer.stop();
            statusLabel.setText("Replay Paused");
        } else {
            pauseButton.setText("Pause");
            replayTimer.setDelay(replaySpeedSlider.getValue());
            replayTimer.start();
            statusLabel.setText("Replay Resumed");
        }
    }

    private void finishReplay() {
        if (replayTimer != null) {
            replayTimer.stop();
        }
        isReplaying = false;
        replayPaused = false;

        // Show final result
        String result = game.getGameStatus();
        statusLabel.setText("Replay finished: " + result);
        replayStatusLabel.setText("");
        
        JOptionPane.showMessageDialog(this, 
            "Replay Complete!\n" + result,
            "Replay Finished",
            JOptionPane.INFORMATION_MESSAGE);

        // Re-enable controls
        recordToggle.setEnabled(true);
        replayButton.setEnabled(true);
        pauseButton.setEnabled(false);
        pauseButton.setText("Pause");
        replaySpeedSlider.setEnabled(false);
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
        
        // Clear replay data
        replayMoves = null;
        replayIndex = 0;
    }

    private static class CellButton extends JButton {
        final int row, col;
        CellButton(int r, int c) { super(""); row = r; col = c; }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SOSGui::new);
    }
}