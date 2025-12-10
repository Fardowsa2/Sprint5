# SOS Game Project

## Overview
A fully-featured SOS game implementation in Java with a graphical user interface. SOS is a paper-and-pencil game where players take turns placing "S" or "O" letters on a grid, trying to form "SOS" sequences.

## Features

### Game Modes
- **Simple Game**: Game ends immediately when any player forms an SOS
- **General Game**: Players continue playing until board is full; highest SOS count wins

### Player Types
- **Human**: Manual play with mouse clicks
- **Computer**: AI opponent with random move selection

### Core Features
- Adjustable board size (3x3 to 8x8)
- Visual SOS line drawing (blue for Blue player, red for Red player)
- Turn-based gameplay with status display
- Score tracking for General mode

### Recording & Replay System
- **Record Games**: Save complete game sessions to file
- **Replay Games**: Load and replay saved games
- **Replay Controls**: Pause/resume and adjustable speed
- **Progress Tracking**: Shows move count during replay

## How to Run

### Prerequisites
- Java Development Kit (JDK) 8 or higher
- Basic Java runtime environment

### Compilation and Execution
1. Navigate to the project directory
2. Compile all Java files:
   ```
   javac jonin/*.java
   ```
3. Run the main application:
   ```
   java jonin.SOSGui
   ```

## File Structure

### Core Game Classes
- `AbstractSOSGame.java` - Abstract base class for game logic
- `SimpleSOSGame.java` - Simple mode implementation
- `GeneralSOSGame.java` - General mode implementation
- `Move.java` - Data class representing a game move

### Player Strategies
- `PlayerStrategy.java` - Interface for AI strategies
- `ComputerStrategy.java` - Random move AI implementation

### GUI Components
- `SOSGui.java` - Main graphical user interface
- All Swing components for game display and controls

### Test Classes
- `SimpleSOSGameTest.java` - Unit tests for Simple mode
- `GeneralSOSGameTest.java` - Unit tests for General mode
- `ComputerOpponentTest.java` - AI strategy tests

## How to Play

### Starting a New Game
1. Select game mode: Simple or General
2. Choose board size (3-8)
3. Set player types (Human/Computer for both Blue and Red players)
4. Click "New Game" to begin

### Game Rules
1. Players alternate turns placing either 'S' or 'O' on empty cells
2. Blue player always goes first
3. In Simple mode: Game ends when any SOS is formed
4. In General mode: Players continue until board is full
5. SOS can be formed horizontally, vertically, or diagonally

### Recording Games
1. Click the "Record" toggle button to start recording
2. Play the game as normal
3. Click "Record" again to stop and save the recording
4. Choose filename and location for the .sos file

### Replaying Games
1. Click the "Replay" button
2. Select a saved .sos file
3. Use controls to pause/resume and adjust replay speed
4. Watch the game replay with visual SOS lines

## Recording File Format

Recordings are saved as .sos files with the following format:
```
#SOS_RECORDING_V2.0
#Mode:[SIMPLE/GENERAL]
#BoardSize:[size]
#BluePlayer:[Human/Computer]
#RedPlayer:[Human/Computer]
#Timestamp:[timestamp]
#Moves:
row,col,letter,player
row,col,letter,player
...
```

## AI Strategy

The current AI (`ComputerStrategy`) uses a simple random move selection:
- Chooses a random empty cell
- Randomly selects 'S' or 'O'
- No advanced strategy or SOS detection

## Testing

The project includes JUnit tests for core game logic:
- Run tests using JUnit 5
- Test coverage includes:
  - Simple game win/draw conditions
  - General game scoring and extra turns
  - AI opponent functionality

## Controls Summary

### Main Controls
- **New Game**: Start a fresh game with current settings
- **Record**: Toggle game recording
- **Replay**: Load and replay a saved game
- **Pause/Resume**: Control replay playback
- **Speed Slider**: Adjust replay speed (100-2000ms per move)

### Player Settings
- **Game Mode**: Simple or General
- **Board Size**: 3-8
- **Player Type**: Human or Computer for each player
- **Letter Choice**: 'S' or 'O' for human players

## Known Issues

1. AI is very basic (random moves only)
2. No undo/redo functionality
3. Limited to 8x8 board maximum
4. Replay files must be in exact V2.0 format

## Future Enhancements

Potential improvements could include:
- Advanced AI with SOS pattern recognition
- Network multiplayer support
- Game statistics and history
- Custom board sizes beyond 8x8
- Move undo/redo functionality
- Tournament mode with multiple rounds

## License

This project is for educational purposes. Feel free to modify and extend the code.
