package backEnd;

import static backEnd.Element.*;
import static backEnd.Direction.*;

import java.awt.Point;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class NewSquareState implements ExactState {
	private Square[][] board;
	private int incompletePaths;
	private Map<Integer, Point> colors;
	private int index, hashCode, emptySpaces;
	private NewSquareState auxiliarState;

	public NewSquareState(Square[][] board, int incompletePaths, int nextIndex, int emptySpaces, Map<Integer, Point> colors) {
		this.hashCode = -1;
		this.board = board;
		this.incompletePaths = incompletePaths;
		this.emptySpaces = emptySpaces;

		while (!colors.containsKey(nextIndex)) {
			nextIndex++;
			if (nextIndex > 9) {
				break;
			}
		}
		this.colors = colors;
		index = nextIndex;
	}

	@Override
	public Set<State> getNextStates() {
		Set<State> paths = new HashSet<State>();
		try {
			paths.addAll(getFirstPathStates(colors.get(index).y, colors.get(index).x, board, emptySpaces));
		} catch (BestPathPossibleAlert e) {
			paths = new HashSet<State>();
			paths.add(auxiliarState);
		}
		// System.out.println(paths);
		return paths;
	}

	private Set<State> getFirstPathStates(int i, int j, Square[][] currentBoard, int eS) throws BestPathPossibleAlert {
		Set<State> result = new HashSet<>();
		int iNtS = imNextToSomething(currentBoard, i, j);
		if (iNtS != 0) {
			NewSquareState nss = new NewSquareState(currentBoard, incompletePaths - 2, index + 1, eS, colors);
			if (nss.hasBlockedPaths()) {
				return result;
			}
		}
		int c = connects(i, j, currentBoard);
		if (c != 0) {
			Square[][] newBoard = copyBoard(currentBoard);
			switch (c) {
			case 1:
				decideWhich(newBoard[i][j], DOWN);
				newBoard[i + 1][j].dir1 = UP;
				break;
			case 2:
				decideWhich(newBoard[i][j], UP);
				newBoard[i - 1][j].dir1 = DOWN;
				break;
			case 3:
				decideWhich(newBoard[i][j], RIGHT);
				newBoard[i][j + 1].dir1 = LEFT;
				break;
			case 4:
				decideWhich(newBoard[i][j], LEFT);
				newBoard[i][j - 1].dir1 = RIGHT;
				break;
			}
			NewSquareState nss = new NewSquareState(newBoard, incompletePaths - 2, index + 1, eS, colors);
			
			result.add(nss);
			if (nss.isSolution() == 0) {
				this.auxiliarState = nss;
				throw new BestPathPossibleAlert(nss);
			}
			if (!hasOtherPaths(newBoard, c, i, j)) {
				return result;
			}
		}

		if (isInRange(i + 1, j) && currentBoard[i + 1][j].elem == null) {
			Square[][] newBoard = copyBoard(currentBoard);
			newBoard[i + 1][j].elem = LINE;
			newBoard[i + 1][j].color = index;
			newBoard[i + 1][j].dir1 = UP;
			decideWhich(newBoard[i][j], DOWN);
			if(dotIsReachable(newBoard,i+1,j))
				result.addAll(getFirstPathStates(i + 1, j, newBoard, eS -1));
		}
		if (isInRange(i - 1, j) && currentBoard[i - 1][j].elem == null) {
			Square[][] newBoard = copyBoard(currentBoard);
			newBoard[i - 1][j].elem = LINE;
			newBoard[i - 1][j].color = index;
			newBoard[i - 1][j].dir1 = DOWN;
			decideWhich(newBoard[i][j], UP);
			if(dotIsReachable(newBoard,i-1,j))
				result.addAll(getFirstPathStates(i - 1, j, newBoard, eS -1));
		}
		if (isInRange(i, j + 1) && currentBoard[i][j + 1].elem == null) {
			Square[][] newBoard = copyBoard(currentBoard);
			newBoard[i][j + 1].elem = LINE;
			newBoard[i][j + 1].color = index;
			newBoard[i][j + 1].dir1 = LEFT;
			decideWhich(newBoard[i][j], RIGHT);
			if(dotIsReachable(newBoard,i,j+1))
				result.addAll(getFirstPathStates(i, j + 1, newBoard, eS -1));
		}
		if (isInRange(i, j - 1) && currentBoard[i][j - 1].elem == null) {
			Square[][] newBoard = copyBoard(currentBoard);
			newBoard[i][j - 1].elem = LINE;
			newBoard[i][j - 1].color = index;
			newBoard[i][j - 1].dir1 = RIGHT;
			decideWhich(newBoard[i][j], LEFT);
			if(dotIsReachable(newBoard,i,j-1))
				result.addAll(getFirstPathStates(i, j - 1, newBoard, eS -1));
		}
		return result;
	}

	private boolean dotIsReachable(Square[][] newBoard, int i, int j) {
		byte[][] miniBoard = createSpecialMiniBoard(newBoard);
		miniBoard[i][j] = 2;
		miniBoard[colors.get(index).y][colors.get(index).x] = 0;
		return checkIfThereIsAPath(miniBoard, i, j, true);
	}


	private int imNextToSomething(Square[][] currentBoard, int i, int j) {
		if ((isInRange(i + 1, j) && currentBoard[i + 1][j].elem != null && currentBoard[i + 1][j].color != index)
				|| (!isInRange(i + 1, j))) {
			return 1;
		}
		if ((isInRange(i - 1, j) && currentBoard[i - 1][j].elem != null && currentBoard[i - 1][j].color != index)
				|| (!isInRange(i - 1, j))) {
			return 2;
		}
		if ((isInRange(i, j + 1) && currentBoard[i][j + 1].elem != null && currentBoard[i][j + 1].color != index)
				|| (!isInRange(i, j + 1))) {
			return 3;
		}
		if ((isInRange(i, j - 1) && currentBoard[i][j - 1].elem != null && currentBoard[i][j - 1].color != index)
				|| (!isInRange(i, j - 1))) {
			return 4;
		}
		return 0;
	}


	private void decideWhich(Square sq, Direction dir) {
		if (sq.elem == DOT)
			sq.dir1 = dir;
		else
			sq.dir2 = dir;
	}

	private int connects(int i, int j, Square[][] currentBoard) {
		if (isInRange(i + 1, j) && currentBoard[i + 1][j].elem == DOT && currentBoard[i + 1][j].color == index
				&& currentBoard[i + 1][j].dir1 == null) {
			return 1;
		}
		if (isInRange(i - 1, j) && currentBoard[i - 1][j].elem == DOT && currentBoard[i - 1][j].color == index
				&& currentBoard[i - 1][j].dir1 == null) {
			return 2;
		}
		if (isInRange(i, j + 1) && currentBoard[i][j + 1].elem == DOT && currentBoard[i][j + 1].color == index
				&& currentBoard[i][j + 1].dir1 == null) {
			return 3;
		}
		if (isInRange(i, j - 1) && currentBoard[i][j - 1].elem == DOT && currentBoard[i][j - 1].color == index
				&& currentBoard[i][j - 1].dir1 == null) {
			return 4;
		}
		return 0;
	}

	private boolean hasBlockedPaths() {
		Iterator<Entry<Integer, Point>> it = colors.entrySet().iterator();

		boolean pathAvailable = true;
		while (it.hasNext()) {
			Entry<Integer, Point> en = it.next();
			Integer i = en.getKey();
			Point p = en.getValue();
			if (i >= index) {
				byte[][] miniBoard = createMiniBoard(i);
				pathAvailable = checkIfThereIsAPath(miniBoard, p.y, p.x, true);
				if (!pathAvailable) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean hasOtherPaths(Square[][] currentBoard, int whereFrom, int i, int j) {
		int a = 0;
		int b = 0;
		int c = 0;
		int d = 0;
		if (whereFrom == 1 || whereFrom == 2) {
			if (isInRange(i, j + 1))
				a = 1;
			if (isInRange(i, j - 1))
				b = -1;
		} else {
			if (isInRange(i + 1, j))
				c = 1;
			if (isInRange(i - 1, j))
				d = -1;
		}
		byte[][] miniBoard = createSpecialMiniBoard(currentBoard);
		boolean pathAvailable = (checkIfThereIsAPath(miniBoard, i + c, j + a, true)
				|| checkIfThereIsAPath(miniBoard, i + d, j + b, true));
		return pathAvailable;
	}

	private byte[][] createSpecialMiniBoard(Square[][] currentBoard) {
		byte[][] miniBoard = new byte[currentBoard.length][currentBoard[0].length];
		for (int i = 0; i < currentBoard.length; i++) {
			for (int j = 0; j < currentBoard[0].length; j++) {
				if (currentBoard[i][j].elem == null) {
					miniBoard[i][j] = 1;
				} else if (currentBoard[i][j].elem == DOT && currentBoard[i][j].color == index) {
					miniBoard[i][j] = 2;
				} else if (currentBoard[i][j].elem == LINE) {
					miniBoard[i][j] = 0;
				}
			}
		}
		return miniBoard;
	}

	// private void printMiniBoard(byte[][] miniBoard) {
	// for(int i = 0; i < board.length ; i++){
	// for(int j = 0; j < board[0].length; j++){
	// System.out.print(miniBoard[i][j]);
	// }
	// System.out.println();
	// }
	// }

	private boolean checkIfThereIsAPath(byte[][] miniBoard, int i, int j, boolean isTheFirstCall) {
		if (miniBoard[i][j] == 0) {
			return false;
		} else if (miniBoard[i][j] == 2 && !isTheFirstCall) {
			return true;
		}
		miniBoard[i][j] = 0;
		boolean thereIsAPath = false;
		if (isInRange(i + 1, j)) {
			thereIsAPath = checkIfThereIsAPath(miniBoard, i + 1, j, false);
			if (thereIsAPath)
				return true;
		}
		if (isInRange(i - 1, j)) {
			thereIsAPath = checkIfThereIsAPath(miniBoard, i - 1, j, false);
			if (thereIsAPath)
				return true;
		}
		if (isInRange(i, j + 1)) {
			thereIsAPath = checkIfThereIsAPath(miniBoard, i, j + 1, false);
			if (thereIsAPath)
				return true;
		}
		if (isInRange(i, j - 1)) {
			thereIsAPath = checkIfThereIsAPath(miniBoard, i, j - 1, false);
			if (thereIsAPath)
				return true;
		}
		return false;
	}

	private byte[][] createMiniBoard(Integer i) {
		byte[][] miniBoard = new byte[board.length][board[0].length];
		for (int k = 0; k < miniBoard.length; k++) {
			for (int j = 0; j < miniBoard[0].length; j++) {
				if (board[k][j].elem == null) {
					miniBoard[k][j] = 1;
				} else if (board[k][j].elem == DOT && board[k][j].color == i) {
					miniBoard[k][j] = 2;
				} else {
					miniBoard[k][j] = 0;
				}
			}
		}
		return miniBoard;
	}

	private boolean isInRange(int i, int j) {
		return i >= 0 && j >= 0 && i < board.length && j < board[0].length;
	}

	public void setDot(int color, int i, int j) {
		if (color == -1) {
		} else {
			color = color - '0';
			if (color < index)
				this.index = color;
			if (!colors.containsKey(color)) {
				colors.put(color, new Point(j, i));
			}
			board[i][j].elem = DOT;
			board[i][j].color = color;
			incompletePaths++;
		}
	}

	@Override
	public int isSolution() {
		return (incompletePaths > 0) ? -1 : emptySpaces;
	}


	private Square[][] copyBoard(Square[][] boardToCopy) {
		Square[][] newBoard = new Square[boardToCopy.length][boardToCopy[0].length];
		for (int i = 0; i < boardToCopy.length; i++) {
			for (int j = 0; j < boardToCopy[0].length; j++) {
				Square sq = new Square();
				sq.elem = boardToCopy[i][j].elem;
				sq.color = boardToCopy[i][j].color;
				sq.dir1 = boardToCopy[i][j].dir1;
				sq.dir2 = boardToCopy[i][j].dir2;
				newBoard[i][j] = sq;
			}
		}
		return newBoard;
	}

	@Override
	public Square[][] getInfo() {
		return board;
	}

	public int hashCode() {
		if (this.hashCode != -1) {
			return this.hashCode;
		}
		hashCode = 17;
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[0].length; j++) {
				hashCode = 37 * (37 * hashCode + i) + j;
				if (board[i][j].elem != null) {
					hashCode = 37 * (37 * hashCode + board[i][j].elem.hashCode()) + board[i][j].color;
				}
				if (board[i][j].dir1 != null) {
					hashCode = 37 * hashCode + board[i][j].dir1.hashCode();
				}
				if (board[i][j].dir2 != null) {
					hashCode = 37 * hashCode + board[i][j].dir2.hashCode();
				}
			}
		}
		return hashCode;
	}

	/*
	 * public int hashCode(){ if(this.hashCode != -1){ return this.hashCode; }
	 * int hash = 0; for(int i = 0; i < board.length ; i++){ for(int j = 0; j <
	 * board[0].length; j++){ if(board[i][j].elem == null){ hash+=2; } else {
	 * switch(board[i][j].elem){ case DOT: if(board[i][j].dir1 == null){ hash +=
	 * 3; } else { hash += 5; } break; case LINE: if(board[i][j].dir2 == null){
	 * hash+= 7; } else { hash+= 11; } break; } } } } this.hashCode = hash;
	 * return hash; }
	 */

	public String toString() {
		printBoard();
		return " ";
	}

	public void printBoard() {
		System.out.println(
				"EmptySpaces: - " + " IncompletePaths: " + incompletePaths / 2 + " isSolution: " + isSolution());
		System.out.println("Printing board:");
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[0].length; j++) {
				String s = new String();
				if (board[i][j].elem == null) {
					s += ("----");
				} else {
					switch (board[i][j].elem) {
					case DOT:
						s += (Integer.toString(board[i][j].color));
						if (board[i][j].dir1 == null) {
							s += "d  ";
						} else {
							s += ("d" + dirToString(board[i][j].dir1) + " ");
						}
						break;
					case LINE:
						s += (Integer.toString(board[i][j].color));
						if (board[i][j].dir2 == null) {
							s += (dirToString(board[i][j].dir1) + "  ");
						} else {
							s += (dirToString(board[i][j].dir1) + dirToString(board[i][j].dir2) + " ");
						}
						break;
					}
				}
				System.out.print(s);
			}
			System.out.println();
		}
	}

	private String dirToString(Direction dir) {
		if (dir == null)
			return "null";
		switch (dir) {
		case UP:
			return "U";
		case DOWN:
			return "D";
		case RIGHT:
			return "R";
		case LEFT:
			return "L";
		}
		return null;
	}

	public boolean equals(Object o) {
		if (this == o)
			return true;
		return (o instanceof NewSquareState && hasSameBoard(((NewSquareState) o).board));
	}

	private boolean hasSameBoard(Square[][] board2) {
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[0].length; j++) {
				boolean isTheSame = (board[i][j].elem == board2[i][j].elem) && (board[i][j].color == board2[i][j].color)
						&& (board[i][j].dir1 == board2[i][j].dir1) && (board[i][j].dir2 == board2[i][j].dir2);
				if (!isTheSame)
					return false;
			}
		}
		return true;
	}

	
	
	public static class NewSquareStateBuilder implements StateBuilder {

		private Square[][] board;
		private int incompletePaths, emptySpaces, index;
		private Map<Integer, Integer> checkMap;
		private Map<Integer, Point> colors;
		
		public NewSquareStateBuilder(int length, int width) {
			board = new Square[length][width];
			incompletePaths = 0;
			index = 9;
			emptySpaces= length * width;
			colors = new HashMap<Integer, Point>();
			for (int i = 0; i < board.length; i++) {
				for (int j = 0; j < board[0].length; j++) {
					Square sq = new Square();
					sq.elem = null;
					sq.dir1 = null;
					sq.dir2 = null;
					sq.color = -1;
					board[i][j] = sq;
				}
			}
			checkMap = new HashMap<Integer, Integer>();
		}

		public void setDot(int color, int i, int j) {
			if (color == -1) {
			} else {
				color = color - '0';
				if(!checkMap.containsKey(Integer.valueOf(color))){
					checkMap.put(color, 1);
				}else{
					checkMap.put(color,checkMap.get(color)+1);
				}
				if (color < index)
					this.index = color;
				if (!colors.containsKey(color)) {
					colors.put(color, new Point(j, i));
				}
				board[i][j].elem = DOT;
				board[i][j].color = color;
				incompletePaths++;
				emptySpaces--;
			}
		}

		public State build() {
			if (!isBoardValid(board)) {
				throw new IllegalStateException();
			}
			return new NewSquareState(board, incompletePaths, index, emptySpaces, colors);
		}

		private boolean isBoardValid(Square[][] board) {
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
