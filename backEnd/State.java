package backEnd;

import java.util.Set;

interface State {
	
	Set<State> nextStates();
	
	// color = -1 means empty square
	void setDot(int color, int i, int j);
	
	// Returns -1 if it's not solution or the amount of empty squares if it is solution
	int isSolution();
	
	Square[][] getInfo();
}