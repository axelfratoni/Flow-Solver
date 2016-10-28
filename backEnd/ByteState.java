package backEnd;

import java.util.Set;
import java.util.HashSet;
import java.io.*;

public class ByteState implements State {
	
	private static byte BINARY_UP = 0b00000001;
	private static byte BINARY_RIGHT = 0b00000010;
	private static byte BINARY_DOWN = 0b00000100;
	private static byte BINARY_LEFT = 0b00001000;

	private static byte BINARY_DOT = 0b00001111;

	private int emptySquares, inconexDots;
	private byte[][] board;
	
	public ByteState(int i, int j) {
		board = new byte[i][j];
		inconexDots = 0;
		emptySquares = i*j;
	}

	private ByteState(byte[][] copy, int inconexDots, int emptySquares) {
		this.board = copy;
		this.inconexDots = inconexDots;
		this.emptySquares = emptySquares;
	}
	
	public void setDot(int color, int i, int j) {
		if (color == -1) {
			board[i][j] = 0;
		}
		else {
			board[i][j] = (byte) ((color << 4) | 0xF);
			inconexDots++;
			emptySquares--;
		}
	}

	public Set<State> getNextStates() {
		Set<State> result = new HashSet<>();
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[0].length; j++) {
				int lb = 0;
				if ((board[i][j] & 0x0F) != 0 && (lb = lowerBitsOn(board[i][j])) != 3 && lb != 2) {	// Square is not empty and it's not a connected dot and it's not a full line
					boolean added = false;
					added |= addNextStates(i, j, -1, 0, BINARY_UP, BINARY_DOWN, result);
					added |= addNextStates(i, j, 1, 0, BINARY_DOWN, BINARY_UP, result);
					added |= addNextStates(i, j, 0, -1, BINARY_LEFT, BINARY_RIGHT, result);
					added |= addNextStates(i, j, 0, 1, BINARY_RIGHT, BINARY_LEFT, result);
					if (!added) {
						return null;
					}
				}
			}
		}
		return result;
	}

	private boolean addNextStates(int x, int y, int i, int j, byte mask, byte opposite_mask, Set<State> result) {
		boolean inRange = isInRange(x+i, y+j);
		if (inRange && ((board[x][y] & 0x0F) == BINARY_DOT || (lowerBitsOn(board[x][y]) == 1 && ((board[x][y]^mask) & 0x0F) != 0))) {	// It's a non-connected dot or a non-maskwards line
			boolean added = false;
			if ((board[x+i][y+j] & 0x0F) == 0) {	// Masker square is empty
				byte[][] newBoard = new byte[board.length][];
				for (int k = 0; k < board.length; k++) {
					newBoard[k] = board[k].clone();
				}
				newBoard[x][y] ^= mask;
				newBoard[x+i][y+j] = (byte) ((opposite_mask) + (newBoard[x][y] & 0xF0));
				result.add(new ByteState(newBoard, inconexDots, emptySquares - 1));
				added = true;
			} else if (board[x+i][y+j] >> 4 == board[x][y] >> 4) {	// Masker square has same color
				int lb = lowerBitsOn(board[x+i][y+j]);
				if (lb == 4) {			// Masker square has a dot and it's not connected
					byte[][] newBoard = new byte[board.length][];
					for (int k = 0; k < board.length; k++) {
						newBoard[k] = board[k].clone();
					}
					newBoard[x][y] ^= mask;	// Add mask-line to current dot
					newBoard[x+i][y+j] ^= opposite_mask;	// Add opposite_mask-line to upper dot
					
					ByteState newState = new ByteState(newBoard, inconexDots - 2, emptySquares);
					if (newState.isSolvable()) {
						result.add(newState);
						added = true;
					}
				} else if (lb == 1) {	// Masker square has a one-way line
					byte[][] newBoard = new byte[board.length][];
					for (int k = 0; k < board.length; k++) {
						newBoard[k] = board[k].clone();
					}
					newBoard[x][y] ^= mask;	// Add mask-line to current dot
					newBoard[x+i][y+j] ^= opposite_mask;	// Add opposite_mask-line to upper dot
					ByteState newState = new ByteState(newBoard, inconexDots - 2, emptySquares);
					if (newState.isSolvable()) {
						result.add(newState);
						added = true;
					}
				}
			}
			return added;
		}
		if (!inRange || ((lowerBitsOn(board[x][y]) == 1 && ((board[x][y]^mask) & 0x0F) == 0))) {	// It's not in range or it's a maskward line
			return false;
		} else {
			return true;
		}
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
					info[i][j].color = board[i][j] >> 4;
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

	public int isSolution() {
		if (inconexDots > 0) {
			return -1;
		} else if (inconexDots == 0) {
			return emptySquares;
		} else {
			throw new AssertionError("Error4");
		}
	}

	// Missing implementation
	@Deprecated
	private boolean isSolvable() {
		return true;
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

	private int lowerBitsOn(byte b) {
		int result = 0;
		for (int i = 0; i < 4; i++) {
			if (b%2 == 1) {
				result++;
			}
			b = (byte) (b/2);
		}
		return result;
	}

	private boolean isInRange(int i, int j) {
		return (i >= 0 && j >= 0 && i < board.length && j < board[0].length);
	}

	public int hashCode() {
		int result = 0;
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[0].length; j++) {
				result ^= (board[i][j] << (i % 4));
			}
		}
		return result;
	}

	public boolean equals(Object o) {
		if (this == o) 
			return true;
		return (o instanceof ByteState && hasSameBoard(((ByteState) o).board));
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
		/*
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[0].length; j++) {
				System.out.print(board[i][j] + " ");
			}
			System.out.println();
		}
		System.out.println();
		*/
		
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[0].length; j++) {
				if (board[i][j] == 0) {
					System.out.print(" ");
				} else if ((board[i][j] & 0x0F) == BINARY_DOT || lowerBitsOn(board[i][j]) == 3) {
					System.out.print(board[i][j] >> 4);
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
					throw new AssertionError();
				}
			}
			System.out.println();
		}
		System.out.println();
	}

}