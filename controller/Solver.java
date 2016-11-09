package controller;

import frontEnd.*;
import backEnd.*;

public abstract class Solver {

	protected Drawer drawer;

	public abstract StateBuilder getNewBuilder(int i, int j);

	public abstract void solve(State initialState, int time, Mode mode);

	public void setDrawer(Drawer drawer) {
		this.drawer = drawer;
	}

}