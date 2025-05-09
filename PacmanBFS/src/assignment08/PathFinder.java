package assignment08;

import java.io.*;
import java.util.*;

/**
 * Author - Jose Bonilla
 * The PathFinder class provides a utility to solve mazes using graph-based pathfinding.
 * It reads a maze from an input file, finds the shortest path from the start (S) to the goal (G),
 * and writes the solved maze with the path marked to an output file.
 */
public class PathFinder {

    /**
     * Solves the maze specified in the input file and writes the solved maze to the output file.
     * The solved maze includes the shortest path from the start (S) to the goal (G),
     * marked with '.' characters. If no path exists, the output matches the input.
     *
     * @param inputFile  The path to the input file containing the maze.
     * @param outputFile The path to the output file where the solved maze will be written.
     */
    public static void solveMaze(String inputFile, String outputFile) {
        try {
            // Read the maze from the input file
            Maze maze = Maze.readFromFile(inputFile);


            // Find the shortest path using BFS
            List<int[]> path = maze.findShortestPath();

            // Write the solved maze to the output file
            maze.writeToFile(outputFile, path);

        } catch (IOException e) {
            // Handle file-related errors
            System.err.println("Error reading or writing file: " + e.getMessage());
        }
    }
}

/**
 * The Maze class represents a rectangular maze and provides methods to find
 * the shortest path from a starting point (S) to a goal point (G).
 */
class Maze {
    private final int height;        // Number of rows in the maze
    private final int width;         // Number of columns in the maze
    private final char[][] grid;     // The maze grid represented as a 2D character array
    private final int[] start;       // Coordinates of the start point (S)
    private final int[] goal;        // Coordinates of the goal point (G)

    /**
     * Constructs a Maze object with the given dimensions, grid, start, and goal points.
     *
     * @param height The height of the maze (number of rows).
     * @param width  The width of the maze (number of columns).
     * @param grid   A 2D character array representing the maze layout.
     * @param start  The coordinates of the start point (S) as [row, col].
     * @param goal   The coordinates of the goal point (G) as [row, col].
     */
    public Maze(int height, int width, char[][] grid, int[] start, int[] goal) {
        this.height = height;
        this.width = width;
        this.grid = grid;
        this.start = start;
        this.goal = goal;
    }

    /**
     * Reads a maze from a text file and constructs a Maze object.
     *
     * @param inputFile The path to the input file containing the maze.
     * @return A Maze object representing the maze.
     * @throws IOException If an error occurs while reading the file.
     */
    public static Maze readFromFile(String inputFile) throws IOException {
        try (Scanner scanner = new Scanner(new File(inputFile))) {
            // Parse the dimensions from the first line
            String[] dimensions = scanner.nextLine().split(" ");
            int height = Integer.parseInt(dimensions[0]);
            int width = Integer.parseInt(dimensions[1]);
            char[][] grid = new char[height][width];

            int[] start = null;
            int[] goal = null;

            // Parse the maze grid and locate the start (S) and goal (G) points
            for (int i = 0; i < height; i++) {
                String line = scanner.nextLine();
                grid[i] = line.toCharArray();
                if (line.contains("S")) {
                    start = new int[]{i, line.indexOf('S')};
                }
                if (line.contains("G")) {
                    goal = new int[]{i, line.indexOf('G')};
                }
            }

            return new Maze(height, width, grid, start, goal);
        }
    }

    /**
     * Finds the shortest path from the start point (S) to the goal point (G) using BFS.
     *
     * @return A list of coordinates representing the shortest path, or null if no path exists.
     */
    public List<int[]> findShortestPath() {
        Queue<int[]> queue = new LinkedList<>();
        Map<String, String> parentMap = new HashMap<>();
        boolean[][] visited = new boolean[height][width];

        // Initialize BFS with the start point
        queue.add(start);
        visited[start[0]][start[1]] = true;

        // Direction vectors for movement (up, down, left, right)
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        //BFS traversal
        while (!queue.isEmpty()) {
            int[] current = queue.poll();


            // If the goal is reached, reconstruct and return the path
            if (current[0] == goal[0] && current[1] == goal[1]) {
                return reconstructPath(parentMap, current);
            }

            // Explore each valid neighbor
            for (int[] dir : directions) {
                int newRow = current[0] + dir[0]; //Calculates the new row by adding the row offset
                int newCol = current[1] + dir[1]; //Calculates the new column by adding the column offset

                if (isValidMove(newRow, newCol, visited)) {
                    visited[newRow][newCol] = true;
                    queue.add(new int[]{newRow, newCol});
                    parentMap.put(newRow + "," + newCol, current[0] + "," + current[1]);
                    //Stores the parent (where we came from) for each visited cell
                }
            }
        }

        // Return null if no path is found
        return null;
    }

    /**
     * Checks if a move to the specified coordinates is valid.
     *
     * @param row     The row index of the move.
     * @param col     The column index of the move.
     * @param visited A 2D array indicating which cells have been visited.
     * @return True if the move is valid; false otherwise.
     */
    private boolean isValidMove(int row, int col, boolean[][] visited) {
        return row >= 0 && row < height && col >= 0 && col < width
                && grid[row][col] != 'X' && !visited[row][col];
    }

    /**
     * Reconstructs the path from the goal point to the start point using the parent map.
     *
     * @param parentMap A map linking each node to its parent node.
     * @param end       The goal point as [row, col].
     * @return A list of coordinates representing the shortest path.
     */
    private List<int[]> reconstructPath(Map<String, String> parentMap, int[] end) {
        List<int[]> path = new ArrayList<>();
        String currentKey = end[0] + "," + end[1];

        while (currentKey != null) {
            String[] parts = currentKey.split(",");
            path.add(new int[]{Integer.parseInt(parts[0]), Integer.parseInt(parts[1])});
            currentKey = parentMap.get(currentKey); //Retrieves the parent of the current cell from the parentMap
        }

        Collections.reverse(path);

        return path;
    }

    /**
     * Writes the maze with the shortest path to a file.
     *
     * @param outputFile The path to the output file.
     * @param path       The shortest path as a list of coordinates.
     * @throws IOException If an error occurs while writing the file.
     */
    public void writeToFile(String outputFile, List<int[]> path) throws IOException {
        // Mark the path on the maze grid
        if (path != null) {
            for (int[] coords : path) {
                int row = coords[0];
                int col = coords[1];
                if (grid[row][col] == ' ') {
                    grid[row][col] = '.';
                }
            }
        }

        // Write the updated maze to the output file
        try (PrintWriter output = new PrintWriter(new FileWriter(outputFile))) {
            output.println(height + " " + width);
            for (char[] row : grid) {
                output.println(new String(row));
            }

        }
    }
}
