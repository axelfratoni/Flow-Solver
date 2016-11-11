package controller;

import backEnd.*;
import frontEnd.*;
import java.util.*;

public class ExactSolver extends Solver {

	public ExactSolver() {
	}

	public StateBuilder getNewBuilder(int rows, int cols) {
		return new NewByteState.NewByteStateBuilder(rows, cols);
	}

	public void solve(State initialState, int time, Mode mode) {

			if (! (initialState instanceof ExactState)) {
				throw new IllegalArgumentException("An ExactSolver must recieve an ExactState");
			}

			ExactState myExactState = (ExactState) initialState;

			myExactState.printBoard();
			Set<ExactState> seenStates = new HashSet<>();
			Deque<ExactState> nextStates = new LinkedList<>();
			seenStates.add(myExactState);
			nextStates.offer(myExactState);
			boolean foundSolution = false;
			ExactState solvedState = null;
			int solution = -1;
			long t = System.currentTimeMillis();
			while(!nextStates.isEmpty() && !foundSolution) {
				ExactState thisState = nextStates.poll();
				Set<ExactState> thisNextStates = thisState.getNextStates();
				if (thisNextStates == null || thisNextStates.isEmpty()) continue;
				for (ExactState current: thisNextStates) {
					if (!foundSolution) {
						int thisSolution = current.isSolution();
						if (thisSolution != -1) {
							if (solution == -1 || thisSolution < solution) {
								solvedState = current;
							}
							if (thisSolution == 0) {
								foundSolution = true;
							}
						} else {
							if (!seenStates.contains(current)) {
								nextStates.offer(current);
								seenStates.add(current);
							}
						}
					}
					if (mode == Mode.EXACT_PROGRESS) {
						try {
							Thread.currentThread().sleep(100);
						} catch (InterruptedException e) {
							// There's nothing I can do about it...
						}
						current.printBoard();
					}
					if (foundSolution) {
						break;
					}
				}
			}
			if (solvedState != null) {
				solvedState.printBoard();
				drawer.update(solvedState.getInfo());
			} else {
				System.out.println("No solution found");
			}
			System.out.println("Time elapsed: " + (System.currentTimeMillis() - t)/1000.0 + " seconds");
		
	}
}
