package backEnd;

public class BestPathPossibleException extends Throwable {
	
	public State bestPathPossible;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public BestPathPossibleException(State bestPathPossible){ 
		super();
		this.bestPathPossible = bestPathPossible;
	}
}
