package backEnd;

class ByteState implements State {
	
	private byte[][] board;
	
	ByteState(int i, int j) {
		board = new byte[i][j];
	}
	
	void setDot(int color, int i, int j) {
		if (color == -1) {
			board[i][j] = 0;
		}
		board[i][j] = (byte) ((color << 4) | 0xF);
	}
	
	
	
}