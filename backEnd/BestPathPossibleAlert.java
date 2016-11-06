package backEnd;

public class BestPathPossibleAlert extends Throwable {
	
	public State bestPathPossible;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public BestPathPossibleAlert(State bestPathPossible){ 
		super();
		this.bestPathPossible = bestPathPossible;
	}
}
