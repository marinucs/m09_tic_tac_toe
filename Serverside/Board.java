package practica5;

public class Board {

	private char[][] boxes;
	
	public Board() {
		setEmptyBoard();
	}
	
	public void setEmptyBoard() {
		char[][] boxes = {{' ', ' ', ' '},
	 		  			  {' ', ' ', ' '},
	 		  			  {' ', ' ', ' '}};
		this.boxes = boxes;
	}
	
	public void setBox(int row, int col, char piece) {
		boxes[row][col] = piece;
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
