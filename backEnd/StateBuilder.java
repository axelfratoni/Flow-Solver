package backEnd;

public interface StateBuilder {

	public State build();

	// Color -1 means empty square
	public void setDot(int color, int i, int j);
}