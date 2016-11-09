package backEnd;

import java.util.Set;

public interface ExactState extends State {
	
	Set<ExactState> getNextStates();
	
	// Returns -1 if it's not solution or the amount of empty squares if it is solution
	int isSolution();
	
	Square[][] getInfo();

	void printBoard();
}