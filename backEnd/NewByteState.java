package backEnd;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

public class NewByteState implements ExactState {
	
	private static byte BINARY_UP = 0b00000001;
	private static byte BINARY_RIGHT = 0b00000010;
	private static byte BINARY_DOWN = 0b00000100;
	private static byte BINARY_LEFT = 0b00001000;

	private static byte BINARY_DOT = 0b00001111;

	private int emptySquares, inconexDots;
	private byte[][] board;
	private int[] colors;
	private int index;

	private int hashCode;

	private NewByteState(int i, int j) {
		board = new byte[i][j];
		inconexDots = 0;
		emptySquares = i*j;
		hashCode = 0;
	}

	private NewByteState(byte[][] copy, int inconexDots, int emptySquares, int[] colors, int index) {
		this.board = copy;
		this.inconexDots = inconexDots;
		this.emptySquares = emptySquares;
		this.colors = colors;
		// Adjust index
		if (inconexDots != 0) {
			for (; colors[index] == 0; index++);
		}
		this.index = index;
		hashCode = 0;
	}

	public Set<ExactState> getNextStates() {
		Set<ExactState> paths;
		try {
			paths = getNextPathStates(board, (colors[index] >>> 24) & 0x000000FF, (colors[index] >>> 16) & 0x000000FF, emptySquares);
		} catch (BestPathPossibleAlert e) {
			paths =  new HashSet<ExactState>();
			paths.add(e.bestPathPossible);
			return paths;
		}
		return paths;
	}

	private Set<ExactState> getNextPathStates(byte[][] board, int i, int j, int emptySpaces) throws BestPathPossibleAlert {

		// new NewByteState(board, inconexDots, emptySquares, colors, index).printBoard();

		boolean possibleBlock = false;
		if ((board[i][j] & 0x0F) != BINARY_DOT) {
			if (((board[i][j] & 0x0F) == BINARY_DOWN && (i == 0 || lowerBitsOn(board[i-1][j]) != 0)) || ((board[i][j] & 0x0F) == BINARY_UP && (i == board.length-1 || lowerBitsOn(board[i+1][j]) != 0)) || ((board[i][j] & 0x0F) == BINARY_RIGHT && (j == 0 || lowerBitsOn(board[i][j-1]) != 0)) || ((board[i][j] & 0x0F) == BINARY_LEFT && (j == board[0].length-1 || lowerBitsOn(board[i][j+1]) != 0))) {	// If I'm facnig a border or a filled square
				
				// System.out.println("Not empty in front: ");
				// if ((board[i][j] & 0x0F) == BINARY_UP) System.out.println("UP");
				// if ((board[i][j] & 0x0F) == BINARY_DOWN) System.out.println("DOWN");
				// if ((board[i][j] & 0x0F) == BINARY_LEFT) System.out.println("LEFT");
				// if ((board[i][j] & 0x0F) == BINARY_RIGHT) System.out.println("RIGHT");
				
				possibleBlock = true;
			} else if (i != 0 && (board[i-1][j] & 0x0F) == BINARY_DOT) {	// There's a dot upwards
				possibleBlock = true;
			} else if (i != board.length-1 && (board[i+1][j] & 0x0F) == BINARY_DOT) {	// There's a dot downwards
				possibleBlock = true;
			} else if (j != 0 && (board[i][j-1] & 0x0F) == BINARY_DOT) {	// There's a dot leftwards
				possibleBlock = true;
			} else if (j != board[0].length-1 && (board[i][j+1] & 0x0F) == BINARY_DOT) {	// There's a dot rightwards
				possibleBlock = true;
			}
		}
		Set<ExactState> result = new HashSet<>();
		if (possibleBlock && hasBlockedPaths(board)) {
			return result;
		}
		getNextPathStatesDir(board, i, j, -1, 0, emptySpaces, BINARY_UP, BINARY_DOWN, result);
		getNextPathStatesDir(board, i, j, 1, 0, emptySpaces, BINARY_DOWN, BINARY_UP, result);
		getNextPathStatesDir(board, i, j, 0, -1, emptySpaces, BINARY_LEFT, BINARY_RIGHT, result);
		getNextPathStatesDir(board, i, j, 0, 1, emptySpaces, BINARY_RIGHT, BINARY_LEFT, result);
		return result;
	}

	private void getNextPathStatesDir(byte[][] board, int x, int y, int i, int j, int emptySpaces, byte mask, byte opposite_mask, Set<ExactState> bag) throws BestPathPossibleAlert {
		
		if (isInRange(x+i, y+j)) {
			if (board[x+i][y+j] == 0) {
				byte[][] newBoard = copyBoard(board);
				newBoard[x][y] = (byte) ((newBoard[x][y] & 0xFF) ^ mask);
				newBoard[x+i][y+j] = (byte) ((opposite_mask) + (newBoard[x][y] & 0xF0));
				bag.addAll(getNextPathStates(newBoard, x+i, y+j, emptySpaces - 1));
			} else {
				if ((board[x+i][y+j] & 0xFF) >>> 4 == (board[x][y] & 0xFF) >>> 4 && lowerBitsOn(board[x+i][y+j]) == 4) {	// Same color dot
					byte[][] newBoard = copyBoard(board);
					newBoard[x][y] ^= mask;
					newBoard[x+i][y+j] ^= opposite_mask;
					if (emptySpaces == 0) {
						throw new BestPathPossibleAlert(new NewByteState(newBoard, inconexDots-2, emptySpaces, colors, index+1));
					}
					bag.add(new NewByteState(newBoard, inconexDots-2, emptySpaces, colors, index+1));
				}
			}
		}
	}

	private boolean hasBlockedPaths(byte[][] board) {
		// System.out.println("Looking for blocked paths...");
		for (int i = index + 1; i < colors.length; i++) {
			if (colors[i] == 0) continue;
				byte[][] colorBoard = newColorBoard(board, i);
				// printColorBoard(colorBoard);
				if (!thereIsAPath(colorBoard, (colors[i] >>> 24) & 0xFF, (colors[i] >>> 16) & 0xFF)) {
					return true;
				}
				// System.out.println("Color " + i + " is reachable");
		}
		return false;
	}

	private void printColorBoard(byte[][] colorBoard) {
		for (int i = 0; i < colorBoard.length; i++) {
			for (int j = 0; j < colorBoard[0].length; j++) {
				System.out.print(colorBoard[i][j]);
			}
			System.out.println();
		}
		System.out.println();
	}

	private boolean thereIsAPath(byte[][] colorBoard, int i, int j) {
		if (colorBoard[i][j] == 3) {
			return true;
		}
		if (colorBoard[i][j] == 1) {
			return false;
		}
		colorBoard[i][j] = 1;	// Paint it
		if (i != 0 && thereIsAPath(colorBoard, i-1, j)) {
			return true;
		}
		if (i != colorBoard.length-1 && thereIsAPath(colorBoard, i+1, j)) {
			return true;
		}
		if (j != 0 && thereIsAPath(colorBoard, i, j-1)) {
			return true;
		}
		if (j != colorBoard[0].length-1 && thereIsAPath(colorBoard, i, j+1)) {
			return true;
		}
		return false;
	}

	private byte[][] copyBoard(byte[][] board) {
		byte[][] newBoard = new byte[board.length][];
		for (int k = 0; k < board.length; k++) {
			newBoard[k] = board[k].clone();
		}
		return newBoard;
	}

	private byte[][] newColorBoard(byte[][] board, int k) {
		byte[][] colorBoard = new byte[board.length][board[0].length];
		for (int i = 0; i < colorBoard.length; i++) {
			for (int j = 0; j < colorBoard[0].length; j++) {
				if (board[i][j] == 0) {
					colorBoard[i][j] = 0;	// 0 for empty square
				} else if ((board[i][j] & 0xFF) >>> 4 == k) {
					colorBoard[i][j] = 2;	// 2 for same color square
				} else {
					colorBoard[i][j] = 1;	// 1 for different color square
				}
			}
		}
		
		//colorBoard[(colors[k] >>> 24) & 0xFF][(colors[k] >>> 16) & 0xFF] = 2;
		colorBoard[(colors[k] >>> 8) & 0xFF][colors[k] & 0xFF] = 3;	// 3 for destination
		return colorBoard;
	}

	public Square[][] getInfo() {
		Square[][] info = new Square[board.length][board[0].length];
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[0].length; j++) {
				int lb = lowerBitsOn(board[i][j]);
				if (lb == 0) {
					info[i][j] = null;
				} else {
					info[i][j] = new Square();
					info[i][j].color = (board[i][j] & 0xFF) >>> 4;
					switch(lb) {
						case 1:
							info[i][j].elem = Element.LINE;
							info[i][j].dir1 = getOneLineDirection(board[i][j]);					
							info[i][j].dir2 = null;
							break;
						case 2:
							info[i][j].elem = Element.LINE;
							switch(board[i][j] & 0x0F) {
								case 0b0011:
									info[i][j].dir1 = Direction.UP;
									info[i][j].dir2 = Direction.RIGHT;
									break;
								case 0b0101:
									info[i][j].dir1 = Direction.UP;
									info[i][j].dir2 = Direction.DOWN;
									break;
								case 0b1001:
									info[i][j].dir1 = Direction.UP;
									info[i][j].dir2 = Direction.LEFT;
									break;
								case 0b0110:
									info[i][j].dir1 = Direction.DOWN;
									info[i][j].dir2 = Direction.RIGHT;
									break;
								case 0b1010:
									info[i][j].dir1 = Direction.LEFT;
									info[i][j].dir2 = Direction.RIGHT;
									break;
								case 0b1100:
									info[i][j].dir1 = Direction.DOWN;
									info[i][j].dir2 = Direction.LEFT;
									break;
								default:
									throw new AssertionError("Error 1");
							}
							break;
						case 3:
							info[i][j].elem = Element.DOT;
							switch(board[i][j] & 0x0F) {
								case 0b1110:
									info[i][j].dir1 = Direction.UP;
									break;
								case 0b1101:
									info[i][j].dir1 = Direction.RIGHT;
									break;
								case 0b1011:
									info[i][j].dir1 = Direction.DOWN;
									break;
								case 0b0111:
									info[i][j].dir1 = Direction.LEFT;
									break;
								default:
									throw new AssertionError("Error 2");
							}
							info[i][j].dir2 = null;
						break;
						case 4:
							info[i][j].elem = Element.DOT;
							info[i][j].dir1 = null;
							info[i][j].dir2 = null;
							break;
						default:
							throw new AssertionError("Error 3");
					}
				}
			}
		}
		return info;
	}

	private Direction getOneLineDirection(byte oneLine) {
		switch(oneLine & 0x0F) {
			case 1:
				return Direction.UP;
			case 2:
				return Direction.RIGHT;
			case 4:
				return Direction.DOWN;
			case 8:
				return Direction.LEFT;
		}
		throw new IllegalArgumentException("Invalid direction");
	}

	public int isSolution() {
		if (inconexDots > 0) {
			return -1;
		} else if (inconexDots == 0) {
			return emptySquares;
		} else {
			throw new AssertionError("Error4");
		}
	}

	private int lowerBitsOn(byte b) {
		int result = 0;
		int intB = (b & 0xFF);
		for (int i = 0; i < 4; i++) {
			if (intB%2 == 1) {
				result++;
			}
			intB = (intB/2);
		}
		return result;
	}

	private boolean isInRange(int i, int j) {
		return (i >= 0 && j >= 0 && i < board.length && j < board[0].length);
	}

	public int hashCode() {
		
		if (hashCode == 0) {
			for (int i = 0; i < board.length; i++) {
				for (int j = 0; j < board[0].length; j++) {
					hashCode ^= (board[i][j] << 4*(i % 4));
				}
			}
		}
		return hashCode;
	}

	public boolean equals(Object o) {
		if (this == o) 
			return true;
		return (o instanceof NewByteState && hasSameBoard(((NewByteState) o).board));
	}

	private boolean hasSameBoard(byte[][] otherBoard) {
		if (board.length != otherBoard.length || board[0].length != otherBoard[0].length)
			return false;
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[0].length; j++) {
				if (board[i][j] != otherBoard[i][j]) 
					return false;
			}
		}
		return true;
	}

	public void printBoard() {
			
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[0].length; j++) {
				if (board[i][j] == 0) {
					System.out.print(" ");
				} else if ((board[i][j] & 0x0F) == BINARY_DOT || lowerBitsOn(board[i][j]) == 3) {
					System.out.print((board[i][j] >>> 4) & 0xF);
				} else if ((board[i][j] & 0x0F) == BINARY_UP) {
					System.out.print("'");
				} else if ((board[i][j] & 0x0F) == BINARY_DOWN) {
					System.out.print(",");
				} else if ((board[i][j] & 0x0F) == BINARY_LEFT) {
					System.out.print(">");
				} else if ((board[i][j] & 0x0F) == BINARY_RIGHT) {
					System.out.print("<");
				} else if ((board[i][j] & 0x0F) == (BINARY_DOWN^BINARY_LEFT)) {
					System.out.print("┐");
				} else if ((board[i][j] & 0x0F) == (BINARY_DOWN^BINARY_RIGHT)) {
					System.out.print("┌");
				} else if ((board[i][j] & 0x0F) == (BINARY_UP^BINARY_LEFT)) {
					System.out.print("┘");
				} else if ((board[i][j] & 0x0F) == (BINARY_UP^BINARY_RIGHT)) {
					System.out.print("└");
				} else if ((board[i][j] & 0x0F) == (BINARY_UP^BINARY_DOWN)) {
					System.out.print("|");
				} else if ((board[i][j] & 0x0F) == (BINARY_LEFT^BINARY_RIGHT)) {
					System.out.print("—");
				} else {
					throw new AssertionError("Invalid byte: " + board[i][j] + " at (" + i + ", " + j + "). lowerBitsOn yields " + lowerBitsOn(board[i][j]));
				}
			}
			System.out.println();
		}
		System.out.println();
	}


	public static class NewByteStateBuilder implements StateBuilder {

		private byte[][] board;
		private int inconexDots, emptySquares;
		private Map<Integer, Integer> checkMap;
		private int[] colors;

		public NewByteStateBuilder(int n, int m) {
			board = new byte[n][m];
			inconexDots = 0;
			emptySquares = n*m;
			checkMap = new HashMap<Integer, Integer>();
			colors = new int[10];
			for (int i = 0; i < colors.length; i++) {
				colors[i] = 0;
			}
		}

		public void setDot(int color, int i, int j) {
			if (color < -1 || color - '0' > 9) {
				throw new IllegalArgumentException("Invalid color " + color);
			}
			if (color == -1) {
				board[i][j] = 0;
			} else {
				color = color - '0';
				board[i][j] = (byte) ((color << 4) | 0xF);
				inconexDots++;
				emptySquares--;
				if (checkMap.containsKey(color)) {
					checkMap.put(color, checkMap.get(color) + 1);
					colors[color] |= (i << 8) | j;
				} else {
					checkMap.put(color, 1);
					colors[color] = (i << 24) | (j << 16);
				}
			}
		}

		public ExactState build() {
			if (!isBoardValid(board)) {
				throw new IllegalStateException();
			}
			return new NewByteState(board, inconexDots, emptySquares, colors.clone(), 0);
		}

		private boolean isBoardValid(byte[][] board) {
			for (Integer color: checkMap.keySet()) {
				int reps = checkMap.get(color);
				if (reps != 2) {
					return false;
				}
			}
			return true;
		}
	}
}