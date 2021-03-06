package controller;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import static backEnd.Element.*;
import static backEnd.Direction.*;

import frontEnd.*;
import backEnd.*;

public class AproxSolver extends Solver {
	
	private SquareHill[][] board;
	private int boardWidth, boardHeight;
	private Deque<AproxState> previousDecisions;
	private Set<AproxState> decisionsMade;
	private boolean hasToStop;
	
	
	public AproxSolver(){
		
		previousDecisions = new LinkedList<AproxState>();
		decisionsMade = new HashSet<AproxState>();
	}

	public void solve(State initialState, int time, Mode mode) {
		
		long startTime = System.currentTimeMillis();
		AproxSolution bestSolution = null;
		board = ((AproxState) initialState).getBoard();
		boardHeight = board.length;
		boardWidth = board[0].length;
		hasToStop = false;
		if(mode == Mode.APROX_PROGRESS){
			hasToStop = true;
		}
	
		printBoard();
		boolean canKeepRunning = true;
		while(canKeepRunning){
			boolean movementDone = true;
			while(movementDone && (((System.currentTimeMillis() - startTime)/1000L) < time)){
				solveImplicitSquares();

				movementDone = solveNearSquares();
				
				if(!movementDone){
					movementDone = solveDecisions();
				}
				else if(hasToStop){
					printAndWait();
				}
				if(!movementDone && hasEOTs() && !previousDecisions.isEmpty()){
					board = previousDecisions.pollFirst().getBoard();
					movementDone = true;
				}
				else if(hasToStop){
					printAndWait();
				}
				
			}
			if(((System.currentTimeMillis() - startTime)/1000) > time || previousDecisions.isEmpty()){
				canKeepRunning = false;
			}
			else{
				if(!hasEOTs()){
					completeCurve();
					int blanks = getBlanks();
					if(bestSolution == null || bestSolution.blanks > blanks){
						
						bestSolution = new AproxSolution(new AproxState(board), ((System.currentTimeMillis() - startTime)/1000.0), blanks);
					}
					if(bestSolution != null && bestSolution.blanks == 0){
						canKeepRunning = false;
					}
					else{
						board = previousDecisions.pollFirst().getBoard();
					}
				}	
			}
		}
		if(bestSolution != null){
			drawer.update((bestSolution.state).getInfo());
			board = bestSolution.state.getBoard();
			printBoard();
			System.out.println("Time elapsed for best solution: " + bestSolution.time);
			System.out.println("Empty spaces left: " + bestSolution.blanks);
		}else{
			System.out.println("No solution found");
		}

	}

	public int getBlanks(){
		int count = 0;
		for(int j = 0; j < boardWidth; j++){
			for(int i = 0; i < boardHeight; i++){
				if(board[i][j].getColor() == -1){
					count++;
				}
			}
		}
		return count;
	}

	
	public StateBuilder getNewBuilder(int rows, int cols) {
		return new AproxState.AproxStateBuilder(rows, cols);
	}

	private boolean hasEOTs(){
		for(int j = 0; j < boardWidth; j++){
			for(int i = 0; i < boardHeight; i++){
				if(board[i][j].isEndOfTrace())
					return true;
			}
		}
		return false;
	}
	
	private void solveImplicitSquares(){
		boolean movementDone;
		
		do{
			movementDone = false;
			
			for(int j = 0; j < boardWidth; j++){
				for(int i = 0; i < boardHeight; i++){
					
					//si es color no completo y es el final de una linea lo analizo
					if( board[i][j].isEndOfTrace() ){
						//trato de mover en las direcciones de los costados
						List<Point> freeDir = getFree(i,j);
						if( freeDir.size() == 1){
							movementDone = true;
							move(i, j, freeDir.get(0));
							if(hasToStop){
								printAndWait();
							}
						}
					}
				}
			}			
		}while(movementDone);
	}

	private boolean solveNearSquares(){ 
		
		boolean movementDone = false;
		
		for(int j = 0; j < boardWidth; j++){
			for(int i = 0; i < boardHeight; i++){
				//me muevo para arriba
				if( isValidMovement(i, j-1) && checkIfCanConect( i, j-1, board[i][j].getColor()) && canMakeDecision(i, j, new Point(-1, 0))){
					//System.out.println("Pos " + i + " " + j + " " + "dir " + -1 + " " + 0);
					AproxState decision = new AproxState(clone());
					previousDecisions.offerFirst(decision); // Guarda en el stack el estado del tablero antes de decidir
					move(i, j, new Point(-1, 0));
					decision = new AproxState(clone()); // Guarda en el set el estado del tablero despues de decidir
					decisionsMade.add(decision);
					movementDone = true;
				}
				//me muevo para la derecha
				if( !movementDone && isValidMovement(i+1, j) && checkIfCanConect( i+1, j, board[i][j].getColor()) && canMakeDecision(i, j, new Point(0, 1))){
					//System.out.println("Pos " + i + " " + j + " " + "dir " + 0 + " " + -1);
					AproxState decision = new AproxState(clone());
					previousDecisions.offerFirst(decision); // Guarda en el stack el estado del tablero antes de decidir
					move(i, j, new Point(0, 1));
					decision = new AproxState(clone()); // Guarda en el set el estado del tablero despues de decidir
					decisionsMade.add(decision);
					movementDone = true;

				}
				//me muevo para abajo
				if( !movementDone && isValidMovement(i, j+1) && checkIfCanConect( i, j+1, board[i][j].getColor()) && canMakeDecision(i, j, new Point(1, 0))){
					//System.out.println("Pos " + i + " " + j + " " + "dir " + 0 + " " + -1);
					AproxState decision = new AproxState(clone());
					previousDecisions.offerFirst(decision); // Guarda en el stack el estado del tablero antes de decidir
					move(i, j, new Point(1, 0));
					decision = new AproxState(clone()); // Guarda en el set el estado del tablero despues de decidir
					decisionsMade.add(decision);
					movementDone = true;
				}
				//me muevo para la izquierda
				if( !movementDone && isValidMovement(i-1, j) && checkIfCanConect( i-1, j, board[i][j].getColor()) && canMakeDecision(i, j, new Point(0, -1))){
					//System.out.println("Pos " + i + " " + j + " " + "dir " + 0 + " " + -1);
					AproxState decision = new AproxState(clone());
					previousDecisions.offerFirst(decision); // Guarda en el stack el estado del tablero antes de decidir
					move(i, j, new Point(0, -1));
					decision = new AproxState(clone()); // Guarda en el set el estado del tablero despues de decidir
					decisionsMade.add(decision);
					movementDone = true;
				}
			}
		}
		return movementDone;
	}
	
	private boolean solveDecisions(){
		Set<InstrumentedEOT> endsOfTrace = new TreeSet<InstrumentedEOT>(new Comparator<InstrumentedEOT>(){

			@Override
			public int compare(InstrumentedEOT l1, InstrumentedEOT l2) {
				if(l1.size == l2.size){
					return l1.hashCode() - l2.hashCode(); 
				}
				return l1.size - l2.size;
			}
			
		});
		for(int j = 0; j < boardWidth; j++){
			for(int i = 0; i < boardHeight; i++){
				if(board[i][j].isEndOfTrace()){
					endsOfTrace.add(new InstrumentedEOT(getFreeDecision(i,j), new Point(j,i)));
				}
			}
		}
		
		int lastCant = 5;
		List<InstrumentedEOT> bestEOT = new ArrayList<InstrumentedEOT>();
		
		for(InstrumentedEOT e : endsOfTrace){
			if(lastCant >= e.size){
				lastCant = e.size;
				bestEOT.add(e);
			}
			else
				break;
		}
		
		if(bestEOT.size() == 0)
			return false;
		
		InstrumentedEOT bestEnd = bestEOT.get(new Random().nextInt(bestEOT.size()));
		
		if(bestEnd != null && bestEnd.size != 0){
			if(bestEnd.bestDir == -1){
				for(int i = 0; i < bestEnd.size && bestEnd.bestDir == -1; i++){
					if(canMakeDecision(bestEnd.position.y, bestEnd.position.x,bestEnd.dir.get(i))){
						bestEnd.bestDir = i;
					}
				}
				
			}
			
			AproxState decision = new AproxState(clone());
			previousDecisions.offerFirst(decision); // Guarda en el stack el estado del tablero antes de decidir
			move(bestEnd.position.y, bestEnd.position.x, bestEnd.dir.get(bestEnd.bestDir));
			decision = new AproxState(clone()); // Guarda en el set el estado del tablero despues de decidir
			decisionsMade.add(decision);

			return true;
		}
		return false;
		
	}

	private void completeCurve(){
		boolean movementDone;
		do{
			movementDone = false;
			for(int i = 0; i < boardHeight; i++){
				for(int j = 0; j < boardWidth; j++){
					Direction dir = canMakeCurve(i, j);
					if(dir != null){
						moveCurve(i, j, dir);
						movementDone = true;
						if(hasToStop){
							printAndWait();
						}
					}
				}
			}
		}while(movementDone);	
	}
	
	private void moveCurve(int i, int j, Direction dir){
		Point inc, inc2, mov1, mov2;
		Direction dirA, dirB;
		if(dir == Direction.UP){
			inc = new Point(0,1);
			inc2 = new Point(1,0);
			mov1 = new Point(0,-1);
			mov2 = new Point(1,0);
			dirA = Direction.RIGHT;
			dirB = Direction.LEFT;
		}
		else if(dir == Direction.DOWN){
			inc = new Point(0,-1);
			inc2 = new Point(1,0);
			mov1 = new Point(0,1);
			mov2 = new Point(1,0);
			dirA = Direction.RIGHT;
			dirB = Direction.LEFT;
		}
		else if(dir == Direction.RIGHT){
			inc = new Point(-1,0);
			inc2 = new Point(0,1);
			mov1 = new Point(1,0);
			mov2 = new Point(0,1);
			dirA = Direction.DOWN;
			dirB = Direction.UP;
		}
		else{
			inc = new Point(1,0);
			inc2 = new Point(0,1);
			mov1 = new Point(-1,0);
			mov2 = new Point(0,1);
			dirA = Direction.DOWN;
			dirB = Direction.UP;
		}
		
		board[i+inc.y][j+inc.x].setEndOfTrace();
		if(board[i+inc.y][j+inc.x].dir2 == dirA){
			board[i+inc.y][j+inc.x].dir2 = null;
		}else{
			board[i+inc.y][j+inc.x].dir1 = board[i+inc.y][j+inc.x].dir2;
			board[i+inc.y][j+inc.x].dir2 = null;
		}
		board[i+inc.y+inc2.y][j+inc.x+inc2.x].setEndOfTrace();
		if(board[i+inc.y+inc2.y][j+inc.x+inc2.x].dir2 == dirB){
			board[i+inc.y+inc2.y][j+inc.x+inc2.x].dir2 = null;
		}else{
			board[i+inc.y+inc2.y][j+inc.x+inc2.x].dir1 = board[i+inc.y+inc2.y][j+inc.x+inc2.x].dir2;
			board[i+inc.y+inc2.y][j+inc.x+inc2.x].dir2 = null;
		}
		move(i+inc.y, j+inc.x, mov1);
		move(i, j, mov2);
		
	}
	
	private Direction canMakeCurve(int i, int j){
		if(!isValidMovement(i, j))
			return null;
		
		if(isValidMovement(i, j+1)){
			if(isInRange(i-1, j) && isInRange(i-1, j+1)){
				if(board[i-1][j].getColor() == board[i-1][j+1].getColor()){
					if(board[i-1][j].dir1 == Direction.RIGHT || board[i-1][j].dir2 == Direction.RIGHT){
						if(board[i-1][j+1].dir1 == Direction.LEFT || board[i-1][j+1].dir2 == Direction.LEFT){
							return Direction.DOWN;//down
						}
					}
				}
			}
			
			if(isInRange(i+1, j) && isInRange(i+1, j+1)){
				if(board[i+1][j].getColor() == board[i+1][j+1].getColor()){
					if(board[i+1][j].dir1 == Direction.RIGHT || board[i+1][j].dir2 == Direction.RIGHT){
						if(board[i+1][j+1].dir1 == Direction.LEFT || board[i+1][j+1].dir2 == Direction.LEFT){
							return Direction.UP;//up
						}
					}
				}
			}
			
		}
		
		
		if(isValidMovement(i+1, j)){
			if(isInRange(i, j-1) && isInRange(i+1, j-1)){
				if(board[i][j-1].getColor() == board[i+1][j-1].getColor()){
					if(board[i][j-1].dir1 == Direction.DOWN || board[i][j-1].dir2 == Direction.DOWN){
						if(board[i+1][j-1].dir1 == Direction.UP || board[i+1][j-1].dir2 == Direction.UP){
							return Direction.RIGHT;
						}
					}
				}
			}
			
			if(isInRange(i, j+1) && isInRange(i+1, j+1)){
				if(board[i][j+1].getColor() == board[i+1][j+1].getColor()){
					if(board[i][j+1].dir1 == Direction.DOWN || board[i][j+1].dir2 == Direction.DOWN){
						if(board[i+1][j+1].dir1 == Direction.UP || board[i+1][j+1].dir2 == Direction.UP){
							return Direction.LEFT;
						}
					}
				}
			}
			
		}
		return null;
	}

	
	private boolean canMakeDecision(int i, int j, Point dir){
		if(!checkIfClogging(i, j, dir)){
			SquareHill[][] aux = clone();
			move(i, j, dir);
			if(!decisionsMade.contains(new AproxState(board))){
				board = aux;
				return true;
			}
			board = aux;
		}
		return false;
	}

	
	private List<Point> getFreeDecision(int i, int j){
		List<Point> checked = new ArrayList<Point>();
		for(Point p : getFree(i, j)){
			if(canMakeDecision(i, j, p)){
				checked.add(p);
			}
		}
		return checked;
	}
	
	private List<Point> getFree(int i, int j){
		if( !board[i][j].isEndOfTrace() )
			throw new IllegalArgumentException();
		
		List<Point> dir = new ArrayList<Point>();
		//me muevo para arriba
		if( isValidMovement(i, j-1) && checkIfThereIsAPath( i, j, new Point(-1, 0))){
			dir.add(new Point(-1, 0));
		}
		//me muevo para la derecha
		if( isValidMovement(i+1, j) && checkIfThereIsAPath( i, j, new Point(0, 1))){
			dir.add(new Point(0, 1));
		}
		//me muevo para abajo
		if( isValidMovement(i, j+1) && checkIfThereIsAPath( i, j, new Point(1, 0))){
			dir.add(new Point(1, 0));
		}
		//me muevo para la izquierda
		if( isValidMovement(i-1, j) && checkIfThereIsAPath( i, j, new Point(0, -1))){
			dir.add(new Point(0, -1));
		}
		return dir;
	}

	private boolean checkIfCanConect(int i, int j, int color){
		int count = 0;
		for(int incI = -1; incI <= 1; incI++){
			for(int incJ = -1; incJ <= 1; incJ++){
				if(!(incI == 0 && incJ == 0 || !(incI != 0 && incJ == 0 || incJ != 0 && incI == 0) )){
					if(isInRange(i+incI, j+incJ) && board[i+incI][j+incJ].isEndOfTrace() && board[i+incI][j+incJ].getColor() == color){
						count++;
					}
					if(count == 2){
						return true;
					}
				}
			}
		}
		return false;
	}
	
	private boolean isValidMovement(int i, int j){
		return isInRange(i,j) && board[i][j].isEmpty();
	}
	
	private boolean isInRange(int i, int j){
		if(i < 0 || i >= boardWidth || j < 0 || j >= boardHeight){
			return false;
		}
		return true;
	}
	
	private void move(int posI, int posJ, Point dir){

		board[posI][posJ].toMiddleOfTrace();
		if(board[posI][posJ].elem == Element.DOT){
			board[posI][posJ].dir1 = getDirection(dir);
		}else{
			board[posI][posJ].dir2 = getDirection(dir);
		}
		
		board[posI + dir.y][posJ + dir.x].setColor(board[posI][posJ].getColor());
		board[posI + dir.y][posJ + dir.x].dir1 = getOpDirection(dir);
		board[posI + dir.y][posJ + dir.x].elem = Element.LINE;
		
		//veo si complete un trace
		checkIsCompleteTrace(posI + dir.y, posJ + dir.x);
	}

	private void printAndWait(){
		printBoard();
		long startTime = System.currentTimeMillis();
		while(System.currentTimeMillis()-startTime < 100);

	}
	
	private Point getPoint(Direction dir){
		if(dir == Direction.UP)
			return new Point(0,-1);
		if(dir == Direction.DOWN)
			return new Point(0,1);
		if(dir == Direction.RIGHT)
			return new Point(1,0);
		if(dir == Direction.LEFT)
			return new Point(-1,0);
		throw new RuntimeException();
	}

	private Direction getDirection(Point dir){
		if(dir.x == 1 && dir.y == 0)
			return Direction.RIGHT;
		if(dir.x == -1 && dir.y == 0)
			return Direction.LEFT;
		if(dir.x == 0 && dir.y == 1)
			return Direction.DOWN;
		if(dir.x == 0 && dir.y == -1)
			return Direction.UP;
		throw new RuntimeException();
		
	}
	
	private Direction getOpDirection(Point dir){
		if(dir.x == 1 && dir.y == 0)
			return Direction.LEFT;
		if(dir.x == -1 && dir.y == 0)
			return Direction.RIGHT;
		if(dir.x == 0 && dir.y == 1)
			return Direction.UP;
		if(dir.x == 0 && dir.y == -1)
			return Direction.DOWN;
		throw new RuntimeException();
	}
	
	private void checkIsCompleteTrace(int posI, int posJ){
		for(int incI = -1; incI <= 1; incI ++){
			for(int incJ = -1; incJ <= 1; incJ ++){
				if(incI == 0 && incJ == 0 || !(incI != 0 && incJ == 0 || incJ != 0 && incI == 0) ){
					continue;
				}
				//si esta en rango, es endOfTrace, y es del mismo color entonces es un complteTrace
				if( isInRange(posI+incI, posJ+incJ) && board[posI+incI][posJ+incJ].isEndOfTrace() && board[posI+incI][posJ+incJ].getColor() == board[posI][posJ].getColor() ){
					
					//le pongo a los dos que son complete trace
					board[posI][posJ].toMiddleOfTrace();
					board[posI][posJ].dir2 = getDirection(new Point(incJ, incI));
					board[posI+incI][posJ+incJ].toMiddleOfTrace();
					if(board[posI+incI][posJ+incJ].elem == Element.DOT || board[posI+incI][posJ+incJ].dir1 == null){
						board[posI+incI][posJ+incJ].dir1 = getOpDirection(new Point(incJ, incI));
					}else{
						board[posI+incI][posJ+incJ].dir2 = getOpDirection(new Point(incJ, incI));
					}
					return;
				}
				
			}
		}
		
	}
	
	private boolean checkIfClogging(int i, int j, Point dir){
		board[i+dir.y][j+dir.x].setColor(11);
		List<Integer> checked = new ArrayList<Integer>();
		checked.add(11);
		checked.add(board[i][j].getColor());
		for(int x = 0; x < boardWidth; x++){
			for(int y = 0; y < boardHeight; y++){
				if( board[y][x].isEndOfTrace() && !checked.contains(board[i][j].getColor())){
					if(!checkIfThereIsAPath(y, x, new Point(0,0))){
						board[i+dir.y][j+dir.x].setColor(-1);
						return true;
					}
					checked.add(board[i][j].getColor());
				}
			}
		}
		board[i+dir.y][j+dir.x].setColor(-1);
		return false;
	}
	
	private boolean checkIfThereIsAPath(int i, int j, Point dir){
		byte[][] miniBoard = createMiniBoard(board[i][j].getColor());
		if(dir.x != 0 || dir.y != 0){
			miniBoard[i][j] = 0;
			miniBoard[i+dir.y][j+dir.x] = 2;
		}
		boolean resp = checkIfThereIsAPathR(miniBoard, i+dir.y, j+dir.x, true);
		return resp;
	}
	
	private boolean checkIfThereIsAPathR(byte[][] miniBoard, int i, int j, boolean isTheFirstCall) {
		if(miniBoard[i][j] == 0){
			return false;
		}else if (miniBoard[i][j] == 2 && !isTheFirstCall){
			return true;
		}
		miniBoard[i][j] = 0;
		boolean thereIsAPath = false;
		if(isInRange(i+1,j)){
			thereIsAPath = checkIfThereIsAPathR(miniBoard,i+1,j,false);
			if(thereIsAPath)
				return true;
		}
		if(isInRange(i-1,j)){
			thereIsAPath = checkIfThereIsAPathR(miniBoard,i-1,j,false);
			if(thereIsAPath)
				return true;
		}
		if(isInRange(i,j+1)){
			thereIsAPath = checkIfThereIsAPathR(miniBoard,i,j+1,false);
			if(thereIsAPath)
				return true;
		}
		if(isInRange(i,j-1)){
			thereIsAPath = checkIfThereIsAPathR(miniBoard,i,j-1,false);
			if(thereIsAPath)
				return true;
		}
		return false;
	}

	private byte[][] createMiniBoard(Integer color) {
		byte[][] miniBoard = new byte[board.length][board[0].length];
		for(int k = 0; k < miniBoard.length; k++){
			for(int j = 0; j < miniBoard[0].length; j++){
				if(board[k][j].getColor() == -1){
					miniBoard[k][j] = 1;
				} else if(board[k][j].isEndOfTrace() && board[k][j].getColor() == color){
					miniBoard[k][j] = 2;
				} else {
					miniBoard[k][j] = 0;
				}
			}
		}
		return miniBoard;
	}
	
	public void printBoard() {
			
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[0].length; j++) {
				if (board[i][j].elem == null) {
					System.out.print(" ");
				} else if (board[i][j].elem == DOT) {
					System.out.print(board[i][j].color);
				} else if (board[i][j].dir2 == null) {
					switch(board[i][j].dir1) {
						case UP:
							System.out.print("'");
							break;
						case DOWN:
							System.out.print(",");
							break;
						case LEFT:
							System.out.print(">");
							break;
						case RIGHT:
							System.out.print("<");
							break;
					}
				} else {
					if ((board[i][j].dir1 == UP && board[i][j].dir2 == DOWN) || (board[i][j].dir2 == UP && board[i][j].dir1 == DOWN)) {
						System.out.print("|");
					} else if ((board[i][j].dir1 == LEFT && board[i][j].dir2 == RIGHT) || (board[i][j].dir2 == LEFT && board[i][j].dir1 == RIGHT)) {
						System.out.print("—");
					} else if ((board[i][j].dir1 == UP && board[i][j].dir2 == RIGHT) || (board[i][j].dir2 == UP && board[i][j].dir1 == RIGHT)) {
						System.out.print("└");
					} else if ((board[i][j].dir1 == DOWN && board[i][j].dir2 == RIGHT) || (board[i][j].dir2 == DOWN && board[i][j].dir1 == RIGHT)) {
						System.out.print("┌");
					} else if ((board[i][j].dir1 == DOWN && board[i][j].dir2 == LEFT) || (board[i][j].dir2 == DOWN && board[i][j].dir1 == LEFT)) {
						System.out.print("┐");
					} else if ((board[i][j].dir1 == UP && board[i][j].dir2 == LEFT) || (board[i][j].dir2 == UP && board[i][j].dir1 == LEFT)) {
						System.out.print("┘");
					}
				}
			}
			System.out.println();
		}
		System.out.println();
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

	public void printEOT(int i){
		System.out.print("      ");
		for(int j = 0; j < boardHeight; j++){
			if(board[i][j].isEndOfTrace())
				System.out.print("x");
			else
				System.out.print("-");
		}
	}

	public SquareHill[][] clone(){
		SquareHill[][] copy = new SquareHill[boardWidth][boardHeight];
		for(int j = 0; j < boardWidth; j++){
			for(int i = 0; i < boardHeight; i++){
				copy[i][j] = board[i][j].clone();
			}
		}
		return copy;
	}
	
	private static class InstrumentedEOT implements Comparable<InstrumentedEOT>{
		List<Point> dir;
		Point position;
		int bestDir;
		int fitness;
		int size;
		
		public InstrumentedEOT(List<Point> dir, Point position){
			this.dir = dir;
			this.position = position;
			this.fitness = 0;
			this.size = dir.size();
			this.bestDir = -1;
		}

		@Override
		public int compareTo(InstrumentedEOT o) {
			return this.fitness - o.fitness;
		}
		 public String toString(){
			 return " Position: " + position + " Direction: " + dir + " Best: " + bestDir;
		 }
	}

	private static class AproxSolution{
		AproxState state;
		int blanks;
		double time;

		public AproxSolution(AproxState state, double time, int blanks){
			this.state = state;
			this.time = time;
			this.blanks = blanks;
		}
	}
	
}
