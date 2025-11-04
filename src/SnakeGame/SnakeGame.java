package SnakeGame;
import java.io.*;
import java.nio.Buffer;
import java.util.*;
import java.util.concurrent.*;

enum Direction {
    UP, DOWN, LEFT, RIGHT
}
class Position {
    private final int row, col;

    public Position(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() { return row; }
    public int getCol() { return col; }

    public Position move(Direction dir) {
        return switch (dir) {
            case UP -> new Position(row - 1, col);
            case DOWN -> new Position(row + 1, col);
            case LEFT -> new Position(row, col - 1);
            case RIGHT -> new Position(row, col + 1);
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Position)) return false;
        Position pos = (Position) o;
        return row == pos.row && col == pos.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }
}
class FoodItem {// we can make this class abstract create penalty or multiple food times with points
    protected int row, column; // Position of the food item
    protected int points; // Points awarded when consumed
    // Constructor to initialize food item position
    public FoodItem(int row, int column) {
        this.row = row;
        this.column = column;
        this.points = 1;
    }
    // Getter methods to retrieve food item properties
    public int getRow() { return row; }
    public int getCol() { return column; }
    public int getPoints() { return points; }
}

class Snake {
    private final Deque<Position> body;
    private Direction direction;
    Snake(Position start) {
        this.body = new LinkedList<>();
        body.addFirst(start);
        this.direction = Direction.RIGHT;
    }
    public Position getHead() {
        return body.peekFirst();
    }

    public Direction getDirection() {
        return direction;
    }

    public List<Position> getBody() {
        return new ArrayList<>(body);
    }
    public void setDirection(Direction dir) {
        // prevent reversing direction
        if ((dir == Direction.UP && direction != Direction.DOWN) ||
                (dir == Direction.DOWN && direction != Direction.UP) ||
                (dir == Direction.LEFT && direction != Direction.RIGHT) ||
                (dir == Direction.RIGHT && direction != Direction.LEFT)) {
            this.direction = dir;
        }
    }

    public Position move(boolean grow) {
        Position newHead = getHead().move(direction);
        body.addFirst(newHead);
        if (!grow) {
            body.removeLast();
        }
        return newHead;
    }
    public boolean isCollision(Position pos) {
        Position tail = this.body.peekLast();
        for (Position p : body) {
            if (p.equals(pos) && p!= tail)
                return true;
        }
        return false;
    }
}

class Board {
    private final int rows, cols;
    private FoodItem food;
    private final Random random = new Random();

    Board(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.food = null;
    }

    public boolean isInside(Position pos) {
        return pos.getRow() >= 0 && pos.getRow() < rows &&
                pos.getCol() >= 0 && pos.getCol() < cols;
    }

    public FoodItem getFood() {
        return food;
    }

    public void spawnFood(Set<Position> occupied) {
        while (true) {
            int r = random.nextInt(rows);
            int c = random.nextInt(cols);
            Position foodPos = new Position(r, c);
            if (!occupied.contains(foodPos)) {
                this.food = new FoodItem(r, c);
                break;
            }
        }
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }
}

class Game {
    private final Snake snake;
    private final Board board;
    private boolean isOver = false;
    private int score = 0;
    private final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    private final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));

    public Game(int rows, int cols){
        board = new Board(rows, cols);
        snake = new Snake(new Position(rows/2, cols/2));
        board.spawnFood(new HashSet<>(snake.getBody()));
    }

    public void start(){
//        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
//        executor.scheduleAtFixedRate(this::gameTick, 0, 500, TimeUnit.MILLISECONDS);
        try {
            while (!isOver) {
                if (reader.ready()) { // only read if input is available
                    String input = reader.readLine().trim().toLowerCase();
                    writer.write("Read input " + input);
                    switch (input) {
                        case "w" -> snake.setDirection(Direction.UP);
                        case "s" -> snake.setDirection(Direction.DOWN);
                        case "a" -> snake.setDirection(Direction.LEFT);
                        case "d" -> snake.setDirection(Direction.RIGHT);
                    }
                    gameTick();
                }

            }

//            executor.shutdown();
            writer.write("Game Over. Final Score: " + score + "\n");
            writer.flush();
            writer.close();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    private void gameTick() {
        Position nextPos = snake.getHead().move(snake.getDirection());
        if(!board.isInside(nextPos) || snake.isCollision(nextPos)){
            this.isOver = true;
            try {
                writer.write("Game is Over. Met Conditions" +  "\n");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return;
        }

        boolean grow = this.board.getFood()!= null && nextPos.getRow() == this.board.getFood().getRow() && nextPos.getCol() == this.board.getFood().getCol();
        snake.move(grow);
        if(grow){
            score += board.getFood().getPoints();
            board.spawnFood(new HashSet<>(snake.getBody()));
        }
        try {
            this.render();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void render() throws IOException{
        int rows = board.getRows();
        int cols = board.getCols();
        char[][] grid = new char[rows][cols];

        // Fill board with '.'
        for (int r = 0; r < rows; r++) {
            Arrays.fill(grid[r], '.');
        }

        // Draw food
        FoodItem food = board.getFood();
        if (food != null) {
            grid[food.getRow()][food.getCol()] = 'F';
        }

        // Draw snake body
        for (Position p : snake.getBody()) {
            grid[p.getRow()][p.getCol()] = 'O';
        }

        // Mark head
        Position head = snake.getHead();
        grid[head.getRow()][head.getCol()] = 'H';

        // Use StringBuilder for fast console output
        StringBuilder sb = new StringBuilder();
        sb.append("\n--- Game Board ---\n");

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                sb.append(grid[r][c]).append(' ');
            }
            sb.append('\n');
        }

        sb.append("Score: ").append(score).append('\n');
        writer.write(sb.toString());
        writer.flush();

    }
}
public class SnakeGame {
    public static void main(String[] args) {
        Game game = new Game(10, 10);
        game.start();
    }
}
