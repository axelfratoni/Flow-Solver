package frontEnd;

import backEnd.*;
import controler.*;
import java.io.*;
import java.util.Map;
import java.util.HashMap;
import java.util.StringTokenizer;

import javafx.application.Application;
import javafx.stage.Stage;

public class Flow extends Application {

	private static Mode mode;
	private static int time;
	private static State initialState;

	public static void main(String[] args) {
		launch(args);
	}
	
	public void start(Stage primaryStage) throws Exception {
		String[] args = this.getParameters().getRaw().toArray(new String[1]);
		
		int error;
		error = readParameters(args);
		if (error != 0) {
			primaryStage.close();
			System.exit(1);
		}
		error = readInput(args[0]);
		if (error != 0) {
			primaryStage.close();
			System.exit(1);
		}
		
		Drawer drawer = new Drawer(primaryStage,initialState.getInfo());
		Solver solver = new Solver(initialState, drawer);
		solver.solve(mode);

	}
	
	private static int readParameters(String[] args) {

		if (args.length < 2 || args.length > 3) {
			System.err.println("Wrong number of parameters:");
			showUsage();
			return 1;
		}

		if (args[1].equals("exact")) {
			mode = Mode.EXACT;
			if (args.length == 3) {
				if (args[2].equals("progress")) {
					mode = Mode.EXACT_PROGRESS;
				} else {
					System.err.println("Unknown mode: exact " + args[2]);
					showUsage();
					return 2;
				}
			}
		} else if (args[1].equals("aprox")) {
			mode = Mode.APROX;
			if (args.length != 3) {
				System.err.println("Wrong number of parameters");
				showUsage();
				return 3;
			}
			try {
				time = Integer.parseInt(args[2]);
			} catch(NumberFormatException e) {
				System.err.println("Invalid numeric format for time: " + args[2]);
				showUsage();
				return 4;
			}
			if (time <= 0) {
				System.err.println("Time must be a positive integer (representing seconds)");
				showUsage();
				return 5;
			}
		} else {
			System.err.println("Unknown method: " + args[1]);
			showUsage();
			return 6;
		}

		return 0;
	}

	private static int readInput(String fileName) {

		try (FileInputStream inputFile = new FileInputStream(fileName)) {
			
			// Read rown and columns
			System.setIn(inputFile);
			BufferedReader myReader = new BufferedReader(new InputStreamReader(System.in));
			String line = myReader.readLine();
			if (line == null) {
				System.err.println("File ended abruptly");
				return 3;
			}
			StringTokenizer st = new StringTokenizer(line, ",", false);
			int rows = 0, cols = 0;
			if (!st.hasMoreTokens()) {
				System.err.println("Number of rows missing");
				return 4;
			}
			try {
				rows = Integer.parseInt(st.nextToken());
				if (rows <= 0) throw new NumberFormatException();
			} catch (NumberFormatException e) {
				System.err.println("Invalid number of rows");
				return 5;
			}
			if (!st.hasMoreTokens()) {
				System.err.println("Number of columns missing");
				return 6;
			}
			try {
				cols = Integer.parseInt(st.nextToken());
				if (cols <= 0) throw new NumberFormatException();
			} catch (NumberFormatException e) {
				System.err.println("Invalid number of columns");
				return 7;
			}
			if (st.hasMoreTokens()) {
				System.err.println("Error in the input file, extra info on the first line: " + st.nextToken());
				return 8;
			}
			st = null; // Release tokenizer for garbage collection

			// Read board
			
//			StateBuilder builder = new ByteState.ByteStateBuilder(rows, cols);
			StateBuilder builder = new NewSquareState.NewSquareStateBuilder(rows, cols);
			for (int i = 0; i < rows; i++) {
				line = myReader.readLine();
				if (line == null) {
					System.err.println("File ended abruptly");
					return 9;
				}
				if (line.length() != cols) {
					System.err.println("Invalid row " + line);
					System.err.println("Ecpected a " + cols + " characters long line, but got a " + line.length() + " one");
					return 10;
				}
				for (int j = 0; j < cols; j++) {
					char c = line.charAt(j);
					if (!isVaild(c)) {
						System.err.println("Invalid character found: " + c);
						return 11;
					}
					if (c == ' ') {
						builder.setDot(-1, i, j);
					} else {
						builder.setDot((int) c, i, j);
					}
				}
			}
			line = myReader.readLine();
			if (line != null) {
				System.err.println("Invalid input after board: " + line);
				return 12;
			}
			try {
				initialState = builder.build();
			} catch (IllegalStateException e) {
				System.err.println("An invalid board was entered");
				return 13;
			}


		} catch (FileNotFoundException e) {
			System.err.println("Unable to open file: " + fileName);
			return 1;
		} catch (IOException e) {
			System.err.println("An error ocurred during file reading");
			return 2;
		}

		return 0;
	}

	private static boolean isVaild(char c) {
		if (c == ' ' || (c >= '0' && c <= '9')) {
			return true;
		}
		return false;
	}

	private static void showUsage() {
		System.err.println("Usage:");
		System.err.println("java -jar tpe.jar <file> exact");
		System.err.println("java -jar tpe.jar <file> exact progress");
		System.err.println("java -jar tpe.jar <file> aprox <time>");
	}
	
}
