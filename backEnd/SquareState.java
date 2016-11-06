package backEnd;

import java.util.HashSet;
import java.util.Set;
import static backEnd.Element.*;
import static backEnd.Direction.*;

public class SquareState implements State {

	
	private Square[][] board;
	private int incompletePaths, emptySpaces;
	private int index;
	
	
	
	public SquareState(int width, int length){
		board = new Square[width][length];
		for(int i = 0; i < board.length ; i++){
			for(int j = 0; j < board[0].length; j++){
				Square sq = new Square();
				sq.elem = null;
				sq.dir1 = null;
				sq.dir2 = null;
				sq.color = -1;
				board[i][j] = sq;
			}
		}
		
	}
	
	public SquareState(Square[][] board, int incompletePaths, int emptySpaces){
		this.board = board;
		this.incompletePaths = incompletePaths;
		this.emptySpaces = emptySpaces;
	}
	
	public Set<State> getNextStates() {
		Set<State> states = new HashSet<State>();
		for(int i = 0; i < board.length ; i++){
			for(int j = 0; j < board[0].length ; j++){
				if(hasPossibleNextStates(board[i][j])){
					states.addAll(getNextStates(i,j));
				}
			}
		}
		return states;
	}

	private Set<State> getNextStates(int i, int j) {
		Set<State> states = new HashSet<State>();
		if(board[i][j].elem == DOT){
			states.addAll(addStates(i,j,true));
		} else {
			states.addAll(addStates(i,j,false));
		}
		return states;
	}
	
	private Set<State> addStates(int i, int j, boolean firstDir) {
		Set<State> nextStates = new HashSet<State>();
		Square sq = board[i][j];
		if(firstDir || sq.dir1 != UP){
			if (isInRange(i-1,j) && isViable(i-1, j, i, j)){
				if(board[i-1][j].color == board[i][j].color || (board[i-1][j].elem == null)){
					Square[][] newBoard = copyBoard();
					int newAmountOfIncompletePaths = 0;
					int newAmountOfEmptySpaces = 0;
					if(firstDir){
						newBoard[i][j].dir1 = UP;
					} else {
						newBoard[i][j].dir2 = UP;
					}
					if(board[i-1][j].elem == null){
						newAmountOfEmptySpaces = -1;
						newBoard[i-1][j].elem = LINE;
						newBoard[i-1][j].color = board[i][j].color;
						newBoard[i-1][j].dir1 = DOWN;
					} else if(board[i-1][j].elem == DOT){
						newAmountOfIncompletePaths = -2;
						newBoard[i-1][j].dir1 = DOWN;
					} else {
						newAmountOfIncompletePaths = -2;
						newBoard[i-1][j].dir2 = DOWN;
					}
					nextStates.add(new SquareState(newBoard, incompletePaths + newAmountOfIncompletePaths, emptySpaces + newAmountOfEmptySpaces));
				}
			}
		}
		
		if(firstDir || sq.dir1 != DOWN){
			if(isInRange(i+1,j) && isViable(i+1, j, i, j)){
				if(board[i+1][j].color == board[i][j].color || (board[i+1][j].elem == null)){
					Square[][] newBoard = copyBoard();
					if(firstDir){
						newBoard[i][j].dir1 = DOWN;
					} else {
						newBoard[i][j].dir2 = DOWN;
					}				
					int newAmountOfIncompletePaths = 0;
					int newAmountOfEmptySpaces = 0;
					if(board[i+1][j].elem == null){
						newAmountOfEmptySpaces = -1;
						newBoard[i+1][j].elem = LINE;
						newBoard[i+1][j].color = board[i][j].color;
						newBoard[i+1][j].dir1 = UP;
					} else if(board[i+1][j].elem == DOT){
						newAmountOfIncompletePaths = -2;
						newBoard[i+1][j].dir1 = UP;
					} else {
						newAmountOfIncompletePaths = -2;
						newBoard[i+1][j].dir2 = UP;
					}
					nextStates.add(new SquareState(newBoard, incompletePaths + newAmountOfIncompletePaths, emptySpaces + newAmountOfEmptySpaces));
				}
				}
		}
		
		if(firstDir || sq.dir1 != LEFT){
			if(isInRange(i ,j-1) && isViable(i, j-1, i, j)){
				if(board[i][j-1].color == board[i][j].color || (board[i][j-1].elem == null)){
					Square[][] newBoard = copyBoard();
					if(firstDir){
						newBoard[i][j].dir1 = LEFT;
					} else {
						newBoard[i][j].dir2 = LEFT;
					}
					int newAmountOfIncompletePaths = 0;
					int newAmountOfEmptySpaces = 0;
					if(board[i][j-1].elem == null){
						newAmountOfEmptySpaces = -1;
						newBoard[i][j-1].elem = LINE;
						newBoard[i][j-1].color = board[i][j].color;
						newBoard[i][j-1].dir1 = RIGHT;
					} else if(board[i][j-1].elem == DOT){
						newAmountOfIncompletePaths = -2;
						newBoard[i][j-1].dir1 = RIGHT;
					} else {
						newAmountOfIncompletePaths = -2;
						newBoard[i][j-1].dir2 = RIGHT;
					}
					nextStates.add(new SquareState(newBoard, incompletePaths + newAmountOfIncompletePaths, emptySpaces + newAmountOfEmptySpaces));
				}
			}
		}
		
		if(firstDir || sq.dir1 != RIGHT){
			if(isInRange(i,j+1) && isViable(i, j+1, i, j)){
				if(board[i][j+1].color == board[i][j].color || (board[i][j+1].elem == null)){
					Square[][] newBoard = copyBoard();
					if(firstDir){
						newBoard[i][j].dir1 = RIGHT;
					} else {
						newBoard[i][j].dir2 = RIGHT;
					}
					int newAmountOfIncompletePaths = 0;
					int newAmountOfEmptySpaces = 0;
					if(board[i][j+1].elem == null){
						newAmountOfEmptySpaces = -1;
						newBoard[i][j+1].elem = LINE;
						newBoard[i][j+1].color = board[i][j].color;
						newBoard[i][j+1].dir1 = LEFT;
					} else if(board[i][j+1].elem == DOT){
						newAmountOfIncompletePaths = -2;
						newBoard[i][j+1].dir1 = LEFT;
					} else {
						newAmountOfIncompletePaths = -2;
						newBoard[i][j+1].dir2 = LEFT;
					}
					nextStates.add(new SquareState(newBoard, incompletePaths + newAmountOfIncompletePaths, emptySpaces + newAmountOfEmptySpaces));
				}
			}
		}
		
		return nextStates;
	}
	
	private boolean isViable(int i, int j, int y, int x) {
		if(spotIsAvailable(i,j,y,x)){
			int acum = 0;
			if (isInRange(i+1,j) && spotIsAvailable(i+1,j,y,x)){
				acum+=1;
			}
			if (isInRange(i-1,j) && spotIsAvailable(i-1,j,y,x)){
				acum+=1;
			}
			if (isInRange(i,j+1) && spotIsAvailable(i,j+1,y,x)){
				acum+=1;
			}
			if (isInRange(i,j-1) && spotIsAvailable(i,j-1,y,x)){
				acum+=1;
			}
			if(!(board[i][j].elem == DOT && board[y][x].elem == DOT)){
				acum--;
			}
			return acum!=0;
		}
		return false;
	}
	
	private boolean spotIsAvailable(int i, int j, int y, int x) {
		return  board[i][j].elem == null || ((board[i][j].color == board[y][x].color) && (spotIsDotWithoutDir1(i,j) || spotIsLineWithoutDir2(i,j)));
	}

	private boolean spotIsLineWithoutDir2(int i, int j){
		return (board[i][j].elem == LINE && board[i][j].dir2 == null);
	}
	
	private boolean spotIsDotWithoutDir1(int i, int j){
		return (board[i][j].elem == DOT  && board[i][j].dir1 == null);
	}
	
	private boolean isInRange(int i, int j) {
		return i >= 0 && j >= 0 && i < board.length && j < board[0].length;
	}
	
	
	private Square[][] copyBoard() {
		Square[][] newBoard = new Square[board.length][board[0].length];
		for(int i = 0; i < board.length; i++){
			for(int j = 0; j < board[0].length; j++){
				Square sq = new Square();
				sq.elem = board[i][j].elem;
				sq.color = board[i][j].color;
				sq.dir1 = board[i][j].dir1;
				sq.dir2 = board[i][j].dir2;
				newBoard[i][j] = sq;
			}
		}
		return newBoard;
	}
	
	
	private boolean hasPossibleNextStates(Square sq) {
		// Es cuadrado con algo Y (Tiene dir1 y no dir 2 (Es linea incompleta) O Es dot sin direccion)
		return sq.elem != null && ((sq.elem == LINE && sq.dir1 != null && sq.dir2 == null) || (sq.elem == DOT && sq.dir1 == null));
	}
	
	@Deprecated
	public void setDot(int color, int i, int j) {
		if (color == -1){
			emptySpaces++;
		} else {
			board[i][j].elem = DOT;
			board[i][j].color = color - '0';
			incompletePaths++;
		}
	}

	@Override
	public int isSolution() {
		return (incompletePaths > 0)? -1 : emptySpaces;
	}

	@Override
	public Square[][] getInfo() {
		return board;
	}
	
	public int hashCode(){
		int hash = 0;
		for(int i = 0; i < board.length ; i++){
			for(int j = 0; j < board[0].length; j++){
				if(board[i][j].elem == null){
					hash+=2;
				} else {
					switch(board[i][j].elem){
						case DOT:
							if(board[i][j].dir1 == null){
								hash += 3;
							} else {
								hash += 5;
							}
							break;
						case LINE:
							if(board[i][j].dir2 == null){
								hash+= 7;
							} else {
								hash+= 11;
							}
							break;
					}
				}
			}
		}
		
		return hash;
	}
	public boolean equals(Object o) {
		if (this == o) 
			return true;
		return (o instanceof SquareState && hasSameBoard(((SquareState) o).board));
	}

	private boolean hasSameBoard(Square[][] board2) {
		for(int i = 0; i < board.length ; i++){
			for(int j = 0; j < board[0].length; j++){
				boolean isTheSame = (board[i][j].elem == board2[i][j].elem) && (board[i][j].color == board2[i][j].color) && (board[i][j].dir1 == board2[i][j].dir1) && (board[i][j].dir2 == board2[i][j].dir2);
				if(!isTheSame)
					return false;
			}
		}
		return true;
	}
	
	public String toString(){
		printBoard();
		return " ";
	}
	public void printBoard(){
		System.out.println("EmptySpaces: "+ emptySpaces + " IncompletePaths: " + incompletePaths/2 + " isSolution: " + isSolution());
		System.out.println("Printing board:");
		for(int i = 0; i < board.length ; i++){
			for(int j = 0; j < board[0].length; j++){
				String s = new String();
				if(board[i][j].elem == null){
					s+=("----");
				} else {
					switch(board[i][j].elem){
						case DOT:
							s+=(Integer.toString(board[i][j].color));
							if(board[i][j].dir1 == null){
								s += "d  ";
							} else {
								s+=("d" + dirToString(board[i][j].dir1) + " ");
							}
							break;
						case LINE:
							s+=(Integer.toString(board[i][j].color));
							if(board[i][j].dir2 == null){
								s+=(dirToString(board[i][j].dir1) + "  ");
							} else {
								s+=(dirToString(board[i][j].dir1) + dirToString(board[i][j].dir2) + " ");
							}
							break;
					}
				}
				System.out.print(s);
			}
			System.out.println();
		}
	}
	private String dirToString(Direction dir){
		if(dir == null)
			return "null";
		switch(dir){
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
	
	
}
