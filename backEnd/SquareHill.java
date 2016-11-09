package backEnd;

public class SquareHill extends Square {
	
	
	//private Square prev, next;
	private boolean isEndOfTrace;
	
	public SquareHill(){
		this(-1);
	}
	
	public SquareHill(int color){
		this.color = color;
		isEndOfTrace = false;
	}
	
	public SquareHill(int color, boolean isEndOfTrace){
		this.color = color;
		this.isEndOfTrace = isEndOfTrace;
		if(color == -1){
			this.isEndOfTrace = false;
		}
	}
	
	public SquareHill clone(){
		return new SquareHill(color, isEndOfTrace);
	}
	
	public void setColor(int color){
		this.color = color;
		if(color != -1){
			isEndOfTrace = true;
		}else{
			isEndOfTrace = false;
		}
		
	}
	
	public void removeColor(){
		color = -1;
		isEndOfTrace = false;
	}
	
	public void toMiddleOfTrace(){
		isEndOfTrace = false;
	}
	
	public boolean isEndOfTrace(){
		return isEndOfTrace;
	}
	
	public void setEndOfTrace(){
		isEndOfTrace = true;
	}
	
	public int getColor(){
		return color;
	}

	public boolean isEmpty(){
		return color == -1;
	}
}
