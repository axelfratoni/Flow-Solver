package backEnd;

import frontEnd.*;
import controller.*;

public class AproxState implements State {
		SquareHill[][] board;
		int hashCode;
		
		public AproxState(SquareHill[][] board){
			this.board = board;
			this.hashCode = 0;
		}
		
		public int hashCode() {
			if (this.hashCode != 0) {
				return this.hashCode;
			}
			hashCode = 17;
			for (int i = 0; i < board.length; i++) {
				for (int j = 0; j < board[0].length; j++) {
					hashCode = 37 * (37 * hashCode + i) + j;
					if (board[i][j].getColor() != -1) {
						hashCode = 37 * (37 * hashCode) + board[i][j].getColor();
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

		public SquareHill[][] getBoard(){
			return board;
		}
		
		public boolean equals(Object o) {
			if (this == o)
				return true;
			return (o instanceof AproxState && hasSameBoard(((AproxState) o).board));
		}

		private boolean hasSameBoard(SquareHill[][] board2) {
			for (int i = 0; i < board.length; i++) {
				for (int j = 0; j < board[0].length; j++) {
					boolean isTheSame = ((board[i][j].getColor() == board2[i][j].getColor())
							&& !(board[i][j].isEndOfTrace() ^ board2[i][j].isEndOfTrace()));
					if (!isTheSame)
						return false;
				}
			}
			return true;
		}

		public Square[][] getInfo() {
			return board;
		}
	
		public static class AproxStateBuilder implements StateBuilder {

			SquareHill[][] board;

			public AproxStateBuilder(int rows, int cols) {
				board = new SquareHill[rows][cols];
			}

			public void setDot(int color, int row, int col) {

				board[row][col] = new SquareHill();
				board[row][col].elem = Element.DOT;
				if (color == -1) {
					board[row][col].elem = null;
					board[row][col].color = color;	
				} else {
					board[row][col].color = color - '0';
					board[row][col].setEndOfTrace();
				}
				board[row][col].dir1 = null;
				board[row][col].dir2 = null;
			}

			public AproxState build() {
				return new AproxState(board);
			}
		}

	}