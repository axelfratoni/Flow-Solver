public abstract class Solver {

	protected Drawer drawer;

	public abstract StateBuilder getNewBuilder(int i, int j);

	public abstract void solve(State initialState, int time);

	public void setDrawer(Drawer drawer) {
		this.drawer = drawer;
	}

}