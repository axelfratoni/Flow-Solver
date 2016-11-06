package controller;

import backEnd.*;
import frontEnd.*;
import java.util.*;

public class Solver {

	private State initialState;
	private Drawer drawer;

	public Solver(State initialState, Drawer drawer) {
		this.initialState = initialState;
		this.drawer = drawer;
	}

	public void solve(Mode mode, int time) {

		if (mode == Mode.EXACT || mode == Mode.EXACT_PROGRESS) {
			initialState.printBoard();
			//drawer.update(initialState.getInfo());
			Set<State> seenStates = new HashSet<>();
			Deque<State> nextStates = new LinkedList<>();
			seenStates.add(initialState);
			nextStates.offer(initialState);
			boolean foundSolution = false;
			State solvedState = null;
			int solution = -1;
			long t = System.currentTimeMillis();
			while(!nextStates.isEmpty() && !foundSolution) {
				State thisState = nextStates.poll();
				Set<State> thisNextStates = thisState.getNextStates();
				if (thisNextStates == null || thisNextStates.isEmpty()) continue;
				for (State current: thisNextStates) {
					if (!foundSolution) {
						int thisSolution = current.isSolution();
						if (thisSolution != -1) {
							if (solution == -1 || thisSolution < solution) {
								solvedState = current;
							}
							if (thisSolution == 0) {
								foundSolution = true;
							}
							// current.printBoard();
						} else {
							if (!seenStates.contains(current)) {
								nextStates.offer(current);
								seenStates.add(current);
								// current.printBoard();
							}
						}
					}
					if (mode == Mode.EXACT_PROGRESS) {
						try {
							Thread.currentThread().sleep(100);
						} catch (InterruptedException e) {
							System.err.println("What you wake me up for, biatch?");
						}
						current.printBoard();
					}
					if (foundSolution) {
						break;
					}
				}
			}
			if (solvedState != null) {
				drawer.update(solvedState.getInfo());
			} else {
				System.out.println("No solution found");
			}
			System.out.println("Time elapsed: " + (System.currentTimeMillis() - t)/1000.0 + " seconds");
		}
	}
}