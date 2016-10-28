package controler;

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

	public void solve(Mode mode) {

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
			if (thisState.getNextStates() == null) continue;
			for (State current: thisState.getNextStates()) {
				if (!foundSolution) {
					int thisSolution = current.isSolution();
					if (thisSolution != -1) {
						if (solution == -1 || thisSolution < solution) {
							solvedState = current;
						}
						if (thisSolution == 0) {
							foundSolution = true;
						}
						current.printBoard();
						drawer.update(solvedState.getInfo());			
					} else {
						if (!seenStates.contains(current)) {
							nextStates.offer(current);
							seenStates.add(current);
							current.printBoard();
						}
					}
				}
			}
		}
		if (solvedState != null) {
			drawer.update(solvedState.getInfo());
		}
		System.out.println("Time elapsed: " + (System.currentTimeMillis() - t)/1000.0 + " seconds");
	}
}