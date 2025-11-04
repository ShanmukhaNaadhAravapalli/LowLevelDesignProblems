package TictacToe;

import java.util.Scanner;

// Main class to run the game
class TicTacToeGame {
    public static void main(String[] args) {
        Game game = new Game();
        game.start();
    }
}

// Game class to manage the overall game flow
class Game {
    private Board board;
    private Player[] players;
    private int currentPlayerIndex;
    private GameStatus status;

    public Game() {
        initializeGame();
    }

    private void initializeGame() {
        this.board = new Board(3);
        this.players = new Player[2];
        this.players[0] = new Player("Player 1", PieceType.X);
        this.players[1] = new Player("Player 2", PieceType.O);
        this.currentPlayerIndex = 0;
        this.status = GameStatus.IN_PROGRESS;
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);

        while (status == GameStatus.IN_PROGRESS) {
            board.display();
            Player currentPlayer = players[currentPlayerIndex];
            System.out.println(currentPlayer.getName() + "'s turn (" + currentPlayer.getPieceType() + ")");

            // Get valid move
            Move move = null;
            while (move == null) {
                System.out.print("Enter row and column (0-2): ");
                try {
                    int row = scanner.nextInt();
                    int col = scanner.nextInt();
                    move = new Move(row, col, currentPlayer);

                    if (!board.makeMove(move)) {
                        System.out.println("Invalid move! Try again.");
                        move = null;
                    }
                } catch (Exception e) {
                    System.out.println("Invalid input! Please enter numbers between 0-2.");
                    scanner.nextLine(); // clear buffer
                }
            }

            // Check game status
            status = board.checkGameStatus();

            if (status == GameStatus.IN_PROGRESS) {
                currentPlayerIndex = (currentPlayerIndex + 1) % 2;
            }
        }

        // Game over
        board.display();
        displayResult();
        scanner.close();
    }

    private void displayResult() {
        switch (status) {
            case WINNER_X:
                System.out.println("Player 1 (X) wins!");
                break;
            case WINNER_O:
                System.out.println("Player 2 (O) wins!");
                break;
            case DRAW:
                System.out.println("Game ended in a draw!");
                break;
            default:
                break;
        }
    }
}

// Board class to manage the game board
class Board {
    private int size;
    private PieceType[][] grid;
    private int movesCount;

    public Board(int size) {
        this.size = size;
        this.grid = new PieceType[size][size];
        this.movesCount = 0;
        initializeBoard();
    }

    private void initializeBoard() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                grid[i][j] = PieceType.EMPTY;
            }
        }
    }

    public boolean makeMove(Move move) {
        int row = move.getRow();
        int col = move.getCol();

        // Validate move
        if (row < 0 || row >= size || col < 0 || col >= size ||
                grid[row][col] != PieceType.EMPTY) {
            return false;
        }

        // Make the move
        grid[row][col] = move.getPlayer().getPieceType();
        movesCount++;
        return true;
    }

    public GameStatus checkGameStatus() {
        // Check rows
        for (int i = 0; i < size; i++) {
            if (grid[i][0] != PieceType.EMPTY &&
                    grid[i][0] == grid[i][1] && grid[i][1] == grid[i][2]) {
                return grid[i][0] == PieceType.X ? GameStatus.WINNER_X : GameStatus.WINNER_O;
            }
        }

        // Check columns
        for (int j = 0; j < size; j++) {
            if (grid[0][j] != PieceType.EMPTY &&
                    grid[0][j] == grid[1][j] && grid[1][j] == grid[2][j]) {
                return grid[0][j] == PieceType.X ? GameStatus.WINNER_X : GameStatus.WINNER_O;
            }
        }

        // Check diagonals
        if (grid[0][0] != PieceType.EMPTY &&
                grid[0][0] == grid[1][1] && grid[1][1] == grid[2][2]) {
            return grid[0][0] == PieceType.X ? GameStatus.WINNER_X : GameStatus.WINNER_O;
        }

        if (grid[0][2] != PieceType.EMPTY &&
                grid[0][2] == grid[1][1] && grid[1][1] == grid[2][0]) {
            return grid[0][2] == PieceType.X ? GameStatus.WINNER_X : GameStatus.WINNER_O;
        }

        // Check for draw
        if (movesCount == size * size) {
            return GameStatus.DRAW;
        }

        return GameStatus.IN_PROGRESS;
    }

    public void display() {
        System.out.println("\nCurrent Board:");
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                System.out.print(grid[i][j].getSymbol() + " ");
                if (j < size - 1) System.out.print("| ");
            }
            System.out.println();
            if (i < size - 1) {
                System.out.println("---------");
            }
        }
        System.out.println();
    }

    public int getSize() {
        return size;
    }

    public PieceType[][] getGrid() {
        return grid;
    }
}

// Player class
class Player {
    private String name;
    private PieceType pieceType;

    public Player(String name, PieceType pieceType) {
        this.name = name;
        this.pieceType = pieceType;
    }

    public String getName() {
        return name;
    }

    public PieceType getPieceType() {
        return pieceType;
    }
}

// Move class to represent a player's move
class Move {
    private int row;
    private int col;
    private Player player;

    public Move(int row, int col, Player player) {
        this.row = row;
        this.col = col;
        this.player = player;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public Player getPlayer() {
        return player;
    }
}

// Enums for game state and piece types
enum PieceType {
    X("X"), O("O"), EMPTY(" ");

    private String symbol;

    PieceType(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }
}

enum GameStatus {
    IN_PROGRESS,
    WINNER_X,
    WINNER_O,
    DRAW
}
public class TicTacToe {
}

// Interface for different player types
interface PlayerStrategy {
    Move makeMove(Board board, PieceType pieceType);
}

// Human Player Strategy
class HumanPlayerStrategy implements PlayerStrategy {
    private Scanner scanner;

    public HumanPlayerStrategy(Scanner scanner) {
        this.scanner = scanner;
    }

    @Override
    public Move makeMove(Board board, PieceType pieceType) {
        System.out.print("Enter row and column (0-2): ");
        int row = scanner.nextInt();
        int col = scanner.nextInt();
        return new Move(row, col, null); // Player will be set later
    }
}

// Computer Player Strategy (Simple AI)
class ComputerPlayerStrategy implements PlayerStrategy {
    @Override
    public Move makeMove(Board board, PieceType pieceType) {
        // Simple AI - find first available spot
        PieceType[][] grid = board.getGrid();
        int size = board.getSize();

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (grid[i][j] == PieceType.EMPTY) {
                    System.out.println("Computer chooses: " + i + ", " + j);
                    return new Move(i, j, null);
                }
            }
        }
        return null;
    }
}

// Enhanced Player class with strategy
class EnhancedPlayer {
    private String name;
    private PieceType pieceType;
    private PlayerStrategy strategy;

    public EnhancedPlayer(String name, PieceType pieceType, PlayerStrategy strategy) {
        this.name = name;
        this.pieceType = pieceType;
        this.strategy = strategy;
    }

    public Move makeMove(Board board) {
        return strategy.makeMove(board, pieceType);
    }

    public String getName() {
        return name;
    }

    public PieceType getPieceType() {
        return pieceType;
    }
}

// Game Factory for different game modes
//class GameFactory {
//    public static Game createHumanVsHumanGame() {
//        Scanner scanner = new Scanner(System.in);
//        EnhancedPlayer[] players = new EnhancedPlayer[2];
//        players[0] = new EnhancedPlayer("Player 1", PieceType.X, new HumanPlayerStrategy(scanner));
//        players[1] = new EnhancedPlayer("Player 2", PieceType.O, new HumanPlayerStrategy(scanner));
//        return new Game(new Board(3), players, scanner);
//    }
//
//    public static Game createHumanVsComputerGame() {
//        Scanner scanner = new Scanner(System.in);
//        EnhancedPlayer[] players = new EnhancedPlayer[2];
//        players[0] = new EnhancedPlayer("Player 1", PieceType.X, new HumanPlayerStrategy(scanner));
//        players[1] = new EnhancedPlayer("Computer", PieceType.O, new ComputerPlayerStrategy());
//        return new Game(new Board(3), players, scanner);
//    }
//}

