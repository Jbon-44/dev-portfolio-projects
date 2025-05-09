package assignment08;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PathFinderTest {

    @Test
    void testTinyMaze() throws IOException {
        String inputFile = "tinyMaze.txt";
        String outputFile = "tinyMazeOutput.txt";

        PathFinder.solveMaze(inputFile, outputFile);

        // Correct expected output
        List<String> expected = List.of(
                "7 9",
                "XXXXXXXXX",
                "X       X",
                "XXXX XX X",
                "X....   X",
                "X.XX.XX X",
                "XGX ..S X",
                "XXXXXXXXX"
        );

        // Read actual output
        List<String> actual = Files.readAllLines(Paths.get(outputFile));

        // Compare expected and actual output
        assertEquals(expected, actual);
    }

    @Test
    void testBigMaze() throws IOException {

        String inputFile = "bigMaze.txt";
        String outputFile = "bigMazeOutput.txt";

        PathFinder.solveMaze(inputFile, outputFile);
        List<String> expected = List.of(
        "37 37",
        "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
        "X       X X X  .......  X   X     X X",
        "X XXXXXXX X XXX.X XXX.XXX XXXXXXX X X",
        "X       X  .....X X  .  X     X X   X",
        "XXXXX XXXXX.XXX X X X.XXX XXXXX X XXX",
        "X   X X X X.  X X X X.  X X   X X   X",
        "X XXX X X X.XXX XXXXX.XXX X XXX XXX X",
        "X       X  ...X   X  .X     X X X...X",
        "XXX XXXXXXXXX.XXXXXXX.XXX XXX X X.X.X",
        "X  ...........X.......X X   X.....X.X",
        "X X.XXXXX X XXX.X X XXX X XXX.XXX X.X",
        "X X.X     X X X.X X     X   X.X X X.X",
        "X X.X XXXXXXX X.XXXXXXXXX XXX.X XXX.X",
        "X X.X X     X  .X     X     X.  X  .X",
        "XXX.XXX X XXXXX.XXXXX XXX XXX.XXXXX.X",
        "X  .  X X X  ...X X     X X...X X X.X",
        "X X.X X X XXX.XXX XXX XXX X.X X X X.X",
        "X X.X X X    .............X.X X.....X",
        "XXX.XXXXXXX X X XXXXX XXX.X.XXX.XXXXX",
        "X  ...  X X X X     X   X...  X.X   X",
        "XXXXX.X X XXXXXXXXX XXXXXXXXXXX.X XXX",
        "X   X.X           X X     X...X.X   X",
        "X XXX.XXXXX XXXXXXXXX XXXXX.X.X.XXX X",
        "X X...X      X .......X.....X...    X",
        "X X.X XXXXX XXX.X X X.X.XXXXXXXXXXXXX",
        "X X.X   X     X.X X X...    X   X X X",
        "X X.XXX XXX X X.X XXXXXXXXX XXX X X X",
        "X X...X X   X X.X   X X   X X X     X",
        "X XXX.XXX XXXXX.XXX X X XXXXX X XXXXX",
        "X  ...  X   X  ...X X     X   X X   X",
        "XXX.X XXXXX XXXXX.XXX XXX X XXX X XXX",
        "X X.X X X X X X...  X X   X X...X X X",
        "X X.XXX X X X X.XXXXXXXXX X X.X.X X X",
        "X...X   X   X  ...............X.....X",
        "X.X X X XXX XXX XXXXXXX XXX XXX XXX.X",
        "XGX X X       X   X       X   X X  SX",
        "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"

        );
    }

    @Test
    void testSolveMazeWithClearPath() throws IOException {
        String inputFile = "testMaze1.txt";
        String outputFile = "testMaze1Output.txt";

        // Create a simple maze with a clear path
        String mazeContent =
                """
                5 5
                XXXXX
                XS  X
                X   X
                X  GX
                XXXXX
                """;
        Files.writeString(new File(inputFile).toPath(), mazeContent);

        // Solve the maze
        PathFinder.solveMaze(inputFile, outputFile);

        // Read the output
        List<String> outputLines = Files.readAllLines(new File(outputFile).toPath());
        String expectedOutput = """
                5 5
                XXXXX
                XS  X
                X.  X
                X..GX
                XXXXX
                """;

        assertEquals(expectedOutput.trim(), String.join("\n", outputLines).trim());
    }


    //Ensures the program correctly identifies when no path exists
    @Test
    void testSolveMazeWithNoPath() throws IOException {
        String inputFile = "testMazeNoPath.txt";
        String outputFile = "testMazeNoPathOutput.txt";

        // maze where no path exists
        String mazeContent = """
                5 5
                XXXXX
                XS XX
                X   X
                X XXG
                XXXXX
                """;
        Files.writeString(new File(inputFile).toPath(), mazeContent);

        // Solve the maze
        PathFinder.solveMaze(inputFile, outputFile);

        // Read the output
        List<String> outputLines = Files.readAllLines(new File(outputFile).toPath());

        // The output should match the input (no path exists)
        assertEquals(mazeContent.trim(), String.join("\n", outputLines).trim());
    }

    @Test
    void testSolveMazeWithLargeInput() throws IOException {
        String inputFile = "testLargeMaze.txt";
        String outputFile = "testLargeMazeOutput.txt";

        // Create a larger maze
        StringBuilder mazeBuilder = new StringBuilder();
        mazeBuilder.append("10 10\n");
        mazeBuilder.append("XXXXXXXXXX\n");
        for (int i = 0; i < 8; i++) {
            mazeBuilder.append("X        X\n");
        }
        mazeBuilder.append("XS      GX\n");
        mazeBuilder.append("XXXXXXXXXX\n");

        Files.writeString(new File(inputFile).toPath(), mazeBuilder.toString());

        // Solve the maze
        PathFinder.solveMaze(inputFile, outputFile);

        // Read the output
        List<String> outputLines = Files.readAllLines(new File(outputFile).toPath());

        // Ensures the output includes a path from S to G
        assertTrue(outputLines.stream().anyMatch(line -> line.contains(".")));
    }


}
