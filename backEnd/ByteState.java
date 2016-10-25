package backEnd;

import java.util.Set;
import java.util.HashSet;

class ByteState implements State {
	
	private static byte BINARY_UP = 0b00000001;
	private static byte BINARY_RIGHT = 0b00000010;
	private static byte BINARY_DOWN = 0b00000100;
	private static byte BINARY_LEFT = 0b00001000;

	private static byte BINARY_DOT = 0b00001111;

	private byte[][] board;
	
	ByteState(int i, int j) {
		board = new byte[i][j];
	}

	private ByteState(byte[][] copy) {
		this.board = copy;
	}
	
	public void setDot(int color, int i, int j) {
		if (color == -1) {
			board[i][j] = 0;
		}
		else {
			board[i][j] = (byte) ((color << 4) | 0xF);
		}
	}

	public Set<State> nextStates() {
		Set<State> result = new HashSet<>();
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[0].length; j++) {
				addNextStates(i, j, 0, -1, BINARY_UP, BINARY_DOWN, result);
				addNextStates(i, j, 0, 1, BINARY_DOWN, BINARY_UP, result);
				addNextStates(i, j, -1, 0, BINARY_LEFT, BINARY_RIGHT, result);
				addNextStates(i, j, 1, 0, BINARY_RIGHT, BINARY_LEFT, result);
			}
		}
		return result;
	}

	private void addNextStates(int x, int y, int i, int j, byte mask, byte opposite_mask, Set<State> result) {
		if (isInRange(x+i, y+j) && ((board[x][y] & 0x0F) == BINARY_DOT || (lowerBitsOn(board[x][y]) == 1 && (board[x][y]^mask) != 0))) {	// It's a non-connected dot or a non-maskwards line
			if ((board[x+i][y+j] & 0x0F) == 0) {	// Masker square is empty
				byte[][] newBoard = new byte[board.length][];
				for (int k = 0; k < board.length; j++) {
					newBoard[k] = board[k].clone();
				}
				newBoard[x][y] = (byte) ((newBoard[x][y] & 0xF0) + ((newBoard[x][y] | 0xF0)^mask));
				newBoard[x+i][y+j] = (opposite_mask);
				result.add(new ByteState(newBoard));
			} else if (board[x+i][y+j] >> 4 == board[x][y] >> 4) {	// Masker square has same color
				int lb = lowerBitsOn(board[x+i][y+j]);
				if (lb == 4) {			// Masker square has a dot and it's not connected
					byte[][] newBoard = new byte[board.length][];
					for (int k = 0; k < board.length; j++) {
						newBoard[k] = board[k].clone();
					}
					newBoard[x][y] = (byte) ((newBoard[x][y] & 0xF0) + ((newBoard[x][y] | 0xF0)^mask));	// Add mask-line to current dot
					newBoard[x+i][y+j] = (byte) ((newBoard[x+i][y+j] & 0xF0) + ((newBoard[x+i][y+j] | 0xF0)^opposite_mask));	// Add opposite_mask-line to upper dot
					result.add(new ByteState(newBoard));
				} else if (lb == 1) {	// Masker square has a one-way line
					byte[][] newBoard = new byte[board.length][];
					for (int k = 0; k < board.length; j++) {
						newBoard[k] = board[k].clone();
					}
					newBoard[x][y] = (byte) ((newBoard[x][y] & 0xF0) + ((newBoard[x][y] | 0xF0)^mask));	// Add mask-line to current dot
					newBoard[x+i][y+j] = (byte) ((newBoard[x+i][y+j] & 0xF0) + ((newBoard[x+i][y+j] | 0xF0)^opposite_mask));	// Add opposite_mask-line to upper dot
					result.add(new ByteState(newBoard));
				}
			}
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
									throw new AssertionError();
							}
						case 3:
							info[i][j].elem = Element.DOT;
							switch(board[i][j]) {
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
									throw new AssertionError();
							}
							info[i][j].dir2 = null;
						break;
						case 4:
							info[i][j].elem = Element.DOT;
							info[i][j].dir1 = null;
							info[i][j].dir2 = null;
							break;
						default:
							throw new AssertionError();
					}
				}
			}
		}
		return info;
	}

	public int isSolution() {
		return -1;
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
}