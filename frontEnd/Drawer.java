package frontEnd;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.stage.Stage;
import backEnd.Direction;
import backEnd.Element;
import backEnd.Square;
import javafx.scene.layout.Pane;
import javafx.scene.image.Image;


public class Drawer  {
	Stage stage;
	Pane board;
	Canvas [][] canvasMatrix;
	
	public Drawer(Stage stage,Square[][] matrix){
		canvasMatrix= new Canvas[matrix.length][matrix[0].length];
		this.stage=stage;
		board= new Pane();
		board.setPrefSize(Dimensions.BOARD_HEIGHT, Dimensions.BOARD_WIDTH);
		stage.setScene(new Scene(board));

	}
	
	@Deprecated
	public static Square[][] sample (){
		Square[][] matrix = new Square[10][10];
		
		matrix[0][0]= new Square();
		matrix[1][0]= new Square();
		matrix[1][1]= new Square();
		
		matrix[0][0].color=0;
		matrix[1][0].color=0;
		matrix[1][1].color=0;
		
		matrix[0][0].elem=Element.DOT;
		matrix[1][0].elem=Element.LINE;
		matrix[1][1].elem=Element.DOT;
		
		matrix[0][0].dir1=Direction.RIGHT;
		matrix[1][0].dir1=Direction.DOWN;
		matrix[1][1].dir1=Direction.UP;
		
		matrix[1][0].dir2=Direction.LEFT;
		
		return matrix;
		
		
	}
	
	@Deprecated
	public static void setBoard(Pane board){
		board.setPrefSize(Dimensions.BOARD_HEIGHT, Dimensions.BOARD_WIDTH);
	}
		
	public void update(Square[][] matrix){
		double offset;
		if(matrix[0].length>matrix.length){
			offset=Dimensions.BOARD_HEIGHT/ (matrix[0].length);
		}else{
			offset=Dimensions.BOARD_HEIGHT/matrix.length;
		}
		for(int i=0; i< matrix.length; i++){
			for(int j=0; j<matrix[0].length; j++){
				board.getChildren().remove(canvasMatrix[i][j]);
				canvasMatrix[i][j]= new Canvas(offset,offset);
				drawIamge(canvasMatrix[i][j],matrix[i][j]);
				canvasMatrix[i][j].setTranslateX(j*offset);
				canvasMatrix[i][j].setTranslateY(i*offset);
				board.getChildren().add(canvasMatrix[i][j]);
			}
		}
		stage.show();
	}
	
	public static void drawIamge(Canvas canvas, Square square){
		
		if(square == null || square.elem == null) {
			return;
		}
		
		String name="Assets/";
		int rotate=0;
		String colors[]= {"Pink","Green","Blue","Purple","Red","Orange","Yellow","Black","LightBlue","Grey"};
		
		
		name+=colors[square.color];
		if(square.elem==Element.DOT){
			name+="Dot";
			if(square.dir1 != null && square.dir2==null || square.dir2!=null && square.dir1==null){
				name += "WithLine";
				if(square.dir1== Direction.UP || square.dir2== Direction.UP){
					rotate=0;
				}else if(square.dir1== Direction.RIGHT || square.dir2== Direction.RIGHT){
					rotate=90;
				}else if(square.dir1== Direction.DOWN || square.dir2== Direction.DOWN){
					rotate=180;
				}else if(square.dir1== Direction.LEFT || square.dir2== Direction.LEFT){
					rotate=270;
				}
			}
		}else if (square.elem==Element.LINE){
			if((square.dir1==Direction.LEFT && square.dir2==Direction.RIGHT) || (square.dir1==Direction.RIGHT && square.dir2==Direction.LEFT)){
				rotate=90;
				name+="Line";
			}else if(square.dir1==Direction.UP && square.dir2==Direction.DOWN ||(square.dir1==Direction.DOWN && square.dir2==Direction.UP ) ){
				rotate=0;
				name+="Line";
			}else if(square.dir1==Direction.UP && square.dir2==Direction.RIGHT || square.dir1==Direction.RIGHT && square.dir2==Direction.UP){
				rotate=0;
				name+="L";
			}else if(square.dir1==Direction.RIGHT && square.dir2==Direction.DOWN || square.dir1==Direction.DOWN && square.dir2==Direction.RIGHT){
				rotate=90;
				name+="L";
			}else if(square.dir1==Direction.DOWN && square.dir2==Direction.LEFT || square.dir1==Direction.LEFT && square.dir2==Direction.DOWN){
				rotate=180;
				name+="L";
			}else if(square.dir1==Direction.LEFT && square.dir2==Direction.UP || square.dir1==Direction.UP && square.dir2==Direction.LEFT){
				rotate=270;
				name+="L";
			}else if(square.dir1==Direction.UP && square.dir2==null || square.dir1==null && square.dir2==Direction.UP){
				rotate=0;
				name+="Stick";
			}else if(square.dir1==Direction.RIGHT && square.dir2==null || square.dir1==null && square.dir2==Direction.RIGHT){
				rotate=90;
				name+="Stick";
			}else if(square.dir1==Direction.DOWN && square.dir2==null || square.dir1==null && square.dir2==Direction.DOWN){
				rotate=180;
				name+="Stick";
			}else if(square.dir1==Direction.LEFT && square.dir2==null || square.dir1==null && square.dir2==Direction.LEFT){
				rotate=270;
				name+="Stick";
			} else {
				System.err.println("Oh oh! Algo salio mal");	
			}
			
		}else{
			System.err.println("Oh oh! Algo salio mal");
		}
		name+=".png";
		
		canvas.getGraphicsContext2D().drawImage(new Image(name), 0 , 0 ,canvas.getHeight(),canvas.getWidth());
		canvas.setRotate(rotate);
		return;
	}
	
}
