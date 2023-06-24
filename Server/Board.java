package practica4;

public class Board {

	private char[][] boxes;
	private boolean isFirstMove;
	
	public Board() { setEmptyBoard(); }
	
	public char[][] getBoxes() { return boxes; }
	
	public boolean getIsFirstMove() { return isFirstMove; }
	
	public void setEmptyBoard() {
		char[][] boxes = {{' ', ' ', ' '},
	 		  			  {' ', ' ', ' '},
	 		  			  {' ', ' ', ' '}};
		this.boxes = boxes;
		isFirstMove = true;
	}
	
	public void setBox(int row, int col, char piece) {
		boxes[row][col] = piece;
		if (isFirstMove && piece == 'X') isFirstMove = false;
	}
	
	public boolean checkWinner(char piece) {
		for (int i = 0; i <= 2; i++) {
			if (boxes[i][0] + boxes[i][1] + boxes[i][2] == piece * 3) return true;
			if (boxes[0][i] + boxes[1][i] + boxes[2][i] == piece * 3) return true;
		}
		if (boxes[0][0] + boxes[1][1] + boxes[2][2] == piece * 3) return true;
		if (boxes[0][2] + boxes[1][1] + boxes[2][0] == piece * 3) return true;
		return false;
	}

}
