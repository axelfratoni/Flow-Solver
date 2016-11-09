package backEnd;

public class BestPathPossibleAlert extends Throwable {
	
	public ExactState bestPathPossible;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public BestPathPossibleAlert(ExactState bestPathPossible){ 
		super();
		this.bestPathPossible = bestPathPossible;
	}
}
