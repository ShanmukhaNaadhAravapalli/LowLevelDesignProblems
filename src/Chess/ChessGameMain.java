package Chess;

// Chess Game Low Level Design with Concurrency in Java

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

// Enums for game constants
enum Color {
    WHITE, BLACK
}

enum PieceType {
    KING, QUEEN, ROOK, BISHOP, KNIGHT, PAWN
}

enum GameStatus {
    ACTIVE, WHITE_WIN, BLACK_WIN, DRAW, RESIGNED, STALEMATE
}

// Position class to represent board coordinates
class Position {

    private final int row;
    private final int col;

    public Position(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public boolean isValid() {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }

}

// Move class to represent a chess move
class Move {

    private final Position from;
    private final Position to;
    private final Piece capturedPiece;
    private final boolean isSpecialMove; // castling, en passant, etc.

    public Move(Position from, Position to, Piece capturedPiece, boolean isSpecialMove) {
        this.from = from;
        this.to = to;
        this.capturedPiece = capturedPiece;
        this.isSpecialMove = isSpecialMove;
    }

    public Position getFrom() {
        return from;
    }

    public Position getTo() {
        return to;
    }

    public Piece getCapturedPiece() {
        return capturedPiece;
    }

    public boolean isSpecialMove() {
        return isSpecialMove;
    }

    @Override
    public String toString() {
        return from + " -> " + to;
    }
}

// Abstract Piece class
abstract class Piece {

    protected final Color color;
    protected final PieceType type;
    protected boolean hasMoved;

    public Piece(Color color, PieceType type) {
        this.color = color;
        this.type = type;
        this.hasMoved = false;
    }

    public Color getColor() {
        return color;
    }

    public PieceType getType() {
        return type;
    }

    public boolean hasMoved() {
        return hasMoved;
    }

    public void setMoved() {
        this.hasMoved = true;
    }

    public abstract List<Position> getPossibleMoves(Position current, Board board);

    public abstract boolean canMove(Position from, Position to, Board board);

    @Override
    public String toString() {
        return color + "_" + type;
    }
}

// Concrete piece implementations
class King extends Piece {

    public King(Color color) {
        super(color, PieceType.KING);
    }

    @Override
    public List<Position> getPossibleMoves(Position current, Board board) {
        List<Position> moves = new ArrayList<>();
        int[] dx = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] dy = {-1, 0, 1, -1, 1, -1, 0, 1};

        for (int i = 0; i < 8; i++) {
            Position newPos = new Position(current.getRow() + dx[i], current.getCol() + dy[i]);
            if (newPos.isValid() && canMove(current, newPos, board)) {
                moves.add(newPos);
            }
        }
        return moves;
    }

    @Override
    public boolean canMove(Position from, Position to, Board board) {
        int rowDiff = Math.abs(to.getRow() - from.getRow());
        int colDiff = Math.abs(to.getCol() - from.getCol());

        if (rowDiff <= 1 && colDiff <= 1) {
            Piece targetPiece = board.getPiece(to);
            return targetPiece == null || targetPiece.getColor() != this.color;
        }
        return false;
    }
}

class Queen extends Piece {

    public Queen(Color color) {
        super(color, PieceType.QUEEN);
    }

    @Override
    public List<Position> getPossibleMoves(Position current, Board board) {
        List<Position> moves = new ArrayList<>();

        // Combine rook and bishop moves
        int[][] directions = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};

        for (int[] dir : directions) {
            for (int i = 1; i < 8; i++) {
                Position newPos = new Position(current.getRow() + i * dir[0], current.getCol() + i * dir[1]);
                if (!newPos.isValid()) {
                    break;
                }

                Piece piece = board.getPiece(newPos);
                if (piece == null) {
                    moves.add(newPos);
                } else {
                    if (piece.getColor() != this.color) {
                        moves.add(newPos);
                    }
                    break;
                }
            }
        }
        return moves;
    }

    @Override
    public boolean canMove(Position from, Position to, Board board) {
        int rowDiff = to.getRow() - from.getRow();
        int colDiff = to.getCol() - from.getCol();

        // Check if move is along rank, file, or diagonal
        if (rowDiff != 0 && colDiff != 0 && Math.abs(rowDiff) != Math.abs(colDiff)) {
            return false;
        }

        return isPathClear(from, to, board);
    }

    private boolean isPathClear(Position from, Position to, Board board) {
        int rowDir = Integer.compare(to.getRow() - from.getRow(), 0);
        int colDir = Integer.compare(to.getCol() - from.getCol(), 0);

        int currentRow = from.getRow() + rowDir;
        int currentCol = from.getCol() + colDir;

        while (currentRow != to.getRow() || currentCol != to.getCol()) {
            if (board.getPiece(new Position(currentRow, currentCol)) != null) {
                return false;
            }
            currentRow += rowDir;
            currentCol += colDir;
        }

        Piece targetPiece = board.getPiece(to);
        return targetPiece == null || targetPiece.getColor() != this.color;
    }
}

class Rook extends Piece {

    public Rook(Color color) {
        super(color, PieceType.ROOK);
    }

    @Override
    public List<Position> getPossibleMoves(Position current, Board board) {
        List<Position> moves = new ArrayList<>();
        int[][] directions = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};

        for (int[] dir : directions) {
            for (int i = 1; i < 8; i++) {
                Position newPos = new Position(current.getRow() + i * dir[0], current.getCol() + i * dir[1]);
                if (!newPos.isValid()) {
                    break;
                }

                Piece piece = board.getPiece(newPos);
                if (piece == null) {
                    moves.add(newPos);
                } else {
                    if (piece.getColor() != this.color) {
                        moves.add(newPos);
                    }
                    break;
                }
            }
        }
        return moves;
    }

    @Override
    public boolean canMove(Position from, Position to, Board board) {
        if (from.getRow() != to.getRow() && from.getCol() != to.getCol()) {
            return false;
        }

        return isPathClear(from, to, board);
    }

    private boolean isPathClear(Position from, Position to, Board board) {
        int rowDir = Integer.compare(to.getRow() - from.getRow(), 0);
        int colDir = Integer.compare(to.getCol() - from.getCol(), 0);

        int currentRow = from.getRow() + rowDir;
        int currentCol = from.getCol() + colDir;

        while (currentRow != to.getRow() || currentCol != to.getCol()) {
            if (board.getPiece(new Position(currentRow, currentCol)) != null) {
                return false;
            }
            currentRow += rowDir;
            currentCol += colDir;
        }

        Piece targetPiece = board.getPiece(to);
        return targetPiece == null || targetPiece.getColor() != this.color;
    }
}

class Bishop extends Piece {

    public Bishop(Color color) {
        super(color, PieceType.BISHOP);
    }

    @Override
    public List<Position> getPossibleMoves(Position current, Board board) {
        List<Position> moves = new ArrayList<>();
        int[][] directions = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};

        for (int[] dir : directions) {
            for (int i = 1; i < 8; i++) {
                Position newPos = new Position(current.getRow() + i * dir[0], current.getCol() + i * dir[1]);
                if (!newPos.isValid()) {
                    break;
                }

                Piece piece = board.getPiece(newPos);
                if (piece == null) {
                    moves.add(newPos);
                } else {
                    if (piece.getColor() != this.color) {
                        moves.add(newPos);
                    }
                    break;
                }
            }
        }
        return moves;
    }

    @Override
    public boolean canMove(Position from, Position to, Board board) {
        int rowDiff = Math.abs(to.getRow() - from.getRow());
        int colDiff = Math.abs(to.getCol() - from.getCol());

        if (rowDiff != colDiff) {
            return false;
        }

        return isPathClear(from, to, board);
    }

    private boolean isPathClear(Position from, Position to, Board board) {
        int rowDir = Integer.compare(to.getRow() - from.getRow(), 0);
        int colDir = Integer.compare(to.getCol() - from.getCol(), 0);

        int currentRow = from.getRow() + rowDir;
        int currentCol = from.getCol() + colDir;

        while (currentRow != to.getRow() || currentCol != to.getCol()) {
            if (board.getPiece(new Position(currentRow, currentCol)) != null) {
                return false;
            }
            currentRow += rowDir;
            currentCol += colDir;
        }

        Piece targetPiece = board.getPiece(to);
        return targetPiece == null || targetPiece.getColor() != this.color;
    }
}

class Knight extends Piece {

    public Knight(Color color) {
        super(color, PieceType.KNIGHT);
    }

    @Override
    public List<Position> getPossibleMoves(Position current, Board board) {
        List<Position> moves = new ArrayList<>();
        int[][] knightMoves = {{2, 1}, {2, -1}, {-2, 1}, {-2, -1}, {1, 2}, {1, -2}, {-1, 2}, {-1, -2}};

        for (int[] move : knightMoves) {
            Position newPos = new Position(current.getRow() + move[0], current.getCol() + move[1]);
            if (newPos.isValid() && canMove(current, newPos, board)) {
                moves.add(newPos);
            }
        }
        return moves;
    }

    @Override
    public boolean canMove(Position from, Position to, Board board) {
        int rowDiff = Math.abs(to.getRow() - from.getRow());
        int colDiff = Math.abs(to.getCol() - from.getCol());

        if ((rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2)) {
            Piece targetPiece = board.getPiece(to);
            return targetPiece == null || targetPiece.getColor() != this.color;
        }
        return false;
    }
}

class Pawn extends Piece {

    public Pawn(Color color) {
        super(color, PieceType.PAWN);
    }

    @Override
    public List<Position> getPossibleMoves(Position current, Board board) {
        List<Position> moves = new ArrayList<>();
        int direction = (color == Color.WHITE) ? -1 : 1;

        // Move forward one square
        Position oneForward = new Position(current.getRow() + direction, current.getCol());
        if (oneForward.isValid() && board.getPiece(oneForward) == null) {
            moves.add(oneForward);

            // Move forward two squares from starting position
            if (!hasMoved) {
                Position twoForward = new Position(current.getRow() + 2 * direction, current.getCol());
                if (twoForward.isValid() && board.getPiece(twoForward) == null) {
                    moves.add(twoForward);
                }
            }
        }

        // Capture diagonally
        Position[] captures = {
                new Position(current.getRow() + direction, current.getCol() - 1),
                new Position(current.getRow() + direction, current.getCol() + 1)
        };

        for (Position capture : captures) {
            if (capture.isValid()) {
                Piece piece = board.getPiece(capture);
                if (piece != null && piece.getColor() != this.color) {
                    moves.add(capture);
                }
            }
        }

        return moves;
    }

    @Override
    public boolean canMove(Position from, Position to, Board board) {
        int direction = (color == Color.WHITE) ? -1 : 1;
        int rowDiff = to.getRow() - from.getRow();
        int colDiff = Math.abs(to.getCol() - from.getCol());

        // Forward move
        if (colDiff == 0) {
            if (rowDiff == direction && board.getPiece(to) == null) {
                return true;
            }
            if (!hasMoved && rowDiff == 2 * direction && board.getPiece(to) == null) {
                return true;
            }
        }

        // Diagonal capture
        if (colDiff == 1 && rowDiff == direction) {
            Piece targetPiece = board.getPiece(to);
            return targetPiece != null && targetPiece.getColor() != this.color;
        }

        return false;
    }
}

// Board class with thread safety
class Board {

    private final Piece[][] board;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();

    public Board() {
        board = new Piece[8][8];
        initializeBoard();
    }

    private void initializeBoard() {
        // Initialize pawns
        for (int col = 0; col < 8; col++) {
            board[1][col] = new Pawn(Color.BLACK);
            board[6][col] = new Pawn(Color.WHITE);
        }

        // Initialize other pieces
        Color[] colors = {Color.BLACK, Color.WHITE};
        int[] rows = {0, 7};

        for (int i = 0; i < 2; i++) {
            Color color = colors[i];
            int row = rows[i];

            board[row][0] = new Rook(color);
            board[row][1] = new Knight(color);
            board[row][2] = new Bishop(color);
            board[row][3] = new Queen(color);
            board[row][4] = new King(color);
            board[row][5] = new Bishop(color);
            board[row][6] = new Knight(color);
            board[row][7] = new Rook(color);
        }
    }

    public Piece getPiece(Position position) {
        readLock.lock();
        try {
            return board[position.getRow()][position.getCol()];
        } finally {
            readLock.unlock();
        }
    }

    public void setPiece(Position position, Piece piece) {
        writeLock.lock();
        try {
            board[position.getRow()][position.getCol()] = piece;
        } finally {
            writeLock.unlock();
        }
    }

    public boolean movePiece(Position from, Position to) {
        writeLock.lock();
        try {
            Piece piece = board[from.getRow()][from.getCol()];
            if (piece == null) {
                return false;
            }

            if (piece.canMove(from, to, this)) {
                board[to.getRow()][to.getCol()] = piece;
                board[from.getRow()][from.getCol()] = null;
                piece.setMoved();
                return true;
            }
            return false;
        } finally {
            writeLock.unlock();
        }
    }

    public Position findKing(Color color) {
        readLock.lock();
        try {
            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    Piece piece = board[row][col];
                    if (piece instanceof King && piece.getColor() == color) {
                        return new Position(row, col);
                    }
                }
            }
            return null;
        } finally {
            readLock.unlock();
        }
    }

    public boolean isSquareAttacked(Position position, Color byColor) {
        readLock.lock();
        try {
            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    Piece piece = board[row][col];
                    if (piece != null && piece.getColor() == byColor) {
                        if (piece.canMove(new Position(row, col), position, this)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        } finally {
            readLock.unlock();
        }
    }

    public List<Position> getAllPiecesOfColor(Color color) {
        readLock.lock();
        try {
            List<Position> pieces = new ArrayList<>();
            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    Piece piece = board[row][col];
                    if (piece != null && piece.getColor() == color) {
                        pieces.add(new Position(row, col));
                    }
                }
            }
            return pieces;
        } finally {
            readLock.unlock();
        }
    }

    public void displayBoard() {
        readLock.lock();
        try {
            System.out.println("  a b c d e f g h");
            for (int row = 0; row < 8; row++) {
                System.out.print((8 - row) + " ");
                for (int col = 0; col < 8; col++) {
                    Piece piece = board[row][col];
                    if (piece == null) {
                        System.out.print(". ");
                    } else {
                        char symbol = getPieceSymbol(piece);
                        System.out.print(symbol + " ");
                    }
                }
                System.out.println();
            }
        } finally {
            readLock.unlock();
        }
    }

    private char getPieceSymbol(Piece piece) {
        char symbol = switch (piece.getType()) {
            case KING ->
                    'K';
            case QUEEN ->
                    'Q';
            case ROOK ->
                    'R';
            case BISHOP ->
                    'B';
            case KNIGHT ->
                    'N';
            case PAWN ->
                    'P';
        };
        return piece.getColor() == Color.WHITE ? symbol : Character.toLowerCase(symbol);
    }
}

// Player class
abstract class Player {

    protected final Color color;
    protected final String name;

    public Player(Color color, String name) {
        this.color = color;
        this.name = name;
    }

    public Color getColor() {
        return color;
    }

    public String getName() {
        return name;
    }

    public abstract Move makeMove(Board board, Scanner scanner);
}

class HumanPlayer extends Player {

    public HumanPlayer(Color color, String name) {
        super(color, name);
    }

    @Override
    public Move makeMove(Board board, Scanner scanner) {
        System.out.println(name + " (" + color + "), enter your move (e.g., e2 e4): ");
        String input = scanner.nextLine();

        try {
            String[] parts = input.split(" ");
            if (parts.length != 2) {
                System.out.println("Invalid input format. Use format: e2 e4");
                return makeMove(board, scanner);
            }

            Position from = parsePosition(parts[0]);
            Position to = parsePosition(parts[1]);

            if (from == null || to == null) {
                System.out.println("Invalid position format. Use format like: e2");
                return makeMove(board, scanner);
            }

            Piece piece = board.getPiece(from);
            if (piece == null || piece.getColor() != color) {
                System.out.println("No piece of your color at that position");
                return makeMove(board, scanner);
            }

            return new Move(from, to, board.getPiece(to), false);

        } catch (Exception e) {
            System.out.println("Invalid move format. Try again.");
            return makeMove(board, scanner);
        }
    }

    private Position parsePosition(String pos) {
        if (pos.length() != 2) {
            return null;
        }

        char col = pos.charAt(0);
        char row = pos.charAt(1);

        if (col < 'a' || col > 'h' || row < '1' || row > '8') {
            return null;
        }

        return new Position(8 - (row - '0'), col - 'a');
    }
}

// Game class with concurrency support
class ChessGame {

    private final Board board;
    private final Player whitePlayer;
    private final Player blackPlayer;
    private Player currentPlayer;
    private GameStatus status;
    private final List<Move> moveHistory;
    private final ExecutorService executorService;
    private final ReentrantLock gameLock;

    public ChessGame(Player whitePlayer, Player blackPlayer) {
        this.board = new Board();
        this.whitePlayer = whitePlayer;
        this.blackPlayer = blackPlayer;
        this.currentPlayer = whitePlayer;
        this.status = GameStatus.ACTIVE;
        this.moveHistory = new ArrayList<>();
        this.executorService = Executors.newFixedThreadPool(2);
        this.gameLock = new ReentrantLock();
    }

    public void startGame() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Chess Game Started!");
        System.out.println("White: " + whitePlayer.getName());
        System.out.println("Black: " + blackPlayer.getName());

        while (status == GameStatus.ACTIVE) {
            gameLock.lock();
            try {
                board.displayBoard();

                Move move = currentPlayer.makeMove(board, scanner);

                if (isValidMove(move)) {
                    executeMove(move);
                    moveHistory.add(move);

                    // Check game end conditions
                    checkGameStatus();

                    // Switch players
                    currentPlayer = (currentPlayer == whitePlayer) ? blackPlayer : whitePlayer;
                } else {
                    System.out.println("Invalid move! Try again.");
                }
            } finally {
                gameLock.unlock();
            }
        }

        displayGameResult();
        scanner.close();
        executorService.shutdown();
    }

    private boolean isValidMove(Move move) {
        Piece piece = board.getPiece(move.getFrom());
        if (piece == null || piece.getColor() != currentPlayer.getColor()) {
            return false;
        }

        if (!piece.canMove(move.getFrom(), move.getTo(), board)) {
            return false;
        }

        // Check if move puts own king in check
        return !wouldMovePutKingInCheck(move);
    }

    private boolean wouldMovePutKingInCheck(Move move) {
        // Simulate the move
        Piece movingPiece = board.getPiece(move.getFrom());
        Piece capturedPiece = board.getPiece(move.getTo());

        // Execute move temporarily
        board.setPiece(move.getTo(), movingPiece);
        board.setPiece(move.getFrom(), null);

        // Check if king is in check
        Position kingPos = board.findKing(currentPlayer.getColor());
        boolean inCheck = kingPos != null && board.isSquareAttacked(kingPos,
                currentPlayer.getColor() == Color.WHITE ? Color.BLACK : Color.WHITE);

        // Undo the move
        board.setPiece(move.getFrom(), movingPiece);
        board.setPiece(move.getTo(), capturedPiece);

        return inCheck;
    }

    private void executeMove(Move move) {
        board.movePiece(move.getFrom(), move.getTo());
        System.out.println("Move executed: " + move);
    }

    private void checkGameStatus() {
        Color opponentColor = currentPlayer.getColor() == Color.WHITE ? Color.BLACK : Color.WHITE;

        if (isCheckmate(opponentColor)) {
            status = currentPlayer.getColor() == Color.WHITE ? GameStatus.WHITE_WIN : GameStatus.BLACK_WIN;
        } else if (isStalemate(opponentColor)) {
            status = GameStatus.STALEMATE;
        }
    }

    private boolean isCheckmate(Color color) {
        Position kingPos = board.findKing(color);
        if (kingPos == null) {
            return true;
        }

        // If king is not in check, it's not checkmate
        Color opponentColor = color == Color.WHITE ? Color.BLACK : Color.WHITE;
        if (!board.isSquareAttacked(kingPos, opponentColor)) {
            return false;
        }

        // Check if any move can get out of check
        return !hasAnyValidMove(color);
    }

    private boolean isStalemate(Color color) {
        Position kingPos = board.findKing(color);

        // If king is in check, it's not stalemate
        Color opponentColor = color == Color.WHITE ? Color.BLACK : Color.WHITE;
        if (board.isSquareAttacked(kingPos, opponentColor)) {
            return false;
        }

        // Check if any move is available
        return hasAnyLegalMove(color);
    }

    private boolean hasAnyLegalMove(Color color) {
        List<Position> pieces = board.getAllPiecesOfColor(color);

        for (Position piecePos : pieces) {
            Piece piece = board.getPiece(piecePos);
            List<Position> possibleMoves = piece.getPossibleMoves(piecePos, board);

            for (Position movePos : possibleMoves) {
                // Check if this move is legal (doesn't expose own king to check)
                if (wouldMovePutKingInCheck(new Move(piecePos, movePos, board.getPiece(movePos), false))) {
                    return true; // Found at least one legal move
                }
            }
        }

        return false; // No legal moves = stalemate (if not in check)
    }

    private boolean hasAnyValidMove(Color colorInCheck) {

        List<Position> pieces = board.getAllPiecesOfColor(colorInCheck);

        for (Position piecePos : pieces) {
            Piece piece = board.getPiece(piecePos);
            List<Position> possibleMoves = piece.getPossibleMoves(piecePos, board);

            for (Position movePos : possibleMoves) {
                // Simulate the move
                Piece capturedPiece = board.getPiece(movePos);
                board.setPiece(movePos, piece);
                board.setPiece(piecePos, null);

                // Check if king is still in check after this move
                Position kingPos = board.findKing(colorInCheck);
                Color attackingColor = (colorInCheck == Color.WHITE) ? Color.BLACK : Color.WHITE;
                boolean stillInCheck = board.isSquareAttacked(kingPos, attackingColor);

                // Undo the move
                board.setPiece(piecePos, piece);
                board.setPiece(movePos, capturedPiece);

                // If this move gets us out of check, we can escape
                if (!stillInCheck) {
                    return true;
                }
            }
        }

        return false; // No move can escape check = checkmate

    }

    private void displayGameResult() {
        System.out.println("\nGame Over!");
        board.displayBoard();

        switch (status) {
            case WHITE_WIN:
                System.out.println("White wins by checkmate!");
                break;
            case BLACK_WIN:
                System.out.println("Black wins by checkmate!");
                break;
            case STALEMATE:
                System.out.println("Game ends in stalemate!");
                break;
            case DRAW:
                System.out.println("Game ends in a draw!");
                break;
            default:
                System.out.println("Game ended.");
        }
    }

    public GameStatus getStatus() {
        return status;
    }

    public List<Move> getMoveHistory() {
        return new ArrayList<>(moveHistory);
    }
}

// Main class to run the game
public class ChessGameMain {

    public static void main(String[] args) {
        System.out.println("Welcome to Chess!");

        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter White player name: ");
        String whiteName = scanner.nextLine();

        System.out.print("Enter Black player name: ");
        String blackName = scanner.nextLine();

        Player whitePlayer = new HumanPlayer(Color.WHITE, whiteName);
        Player blackPlayer = new HumanPlayer(Color.BLACK, blackName);

        ChessGame game = new ChessGame(whitePlayer, blackPlayer);
        game.startGame();
    }
}

