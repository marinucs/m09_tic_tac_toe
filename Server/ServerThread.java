package practica4;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ServerThread extends Thread {
	
	private ObjectOutputStream output;
	private ObjectInputStream input;
	private Socket socket;
	private Board board;
	private boolean end;
	
	public ServerThread(Socket socket) throws IOException {
		this.socket = socket;
		input = new ObjectInputStream(socket.getInputStream());
		output = new ObjectOutputStream(socket.getOutputStream());
		board = new Board();
		end = false;
	}
	
	@Override
	public void run() {
		try {
			do {
	            String clientMove = (String) input.readObject();
	            if (clientMove.equals("jugar")) {
	            	board.setEmptyBoard();
	            	output.writeObject("ack");
	    			output.flush();
	            } else movePieces(clientMove);
			} while (!end);
			input.close();
    		output.close();
    		socket.close();
        } catch (EOFException e) {
        	System.out.println("El cliente finalizó la conexión.");
        	end = true;
        	System.exit(0);
        } catch (Exception e) { e.printStackTrace(); }
	}
  
	public void movePieces(String clientMove) throws IOException {
		if (clientMovesAndWins(clientMove))
			return;
		serverMoves();
	}
  
	public boolean clientMovesAndWins(String clientMove) throws IOException {
		int row = Integer.parseInt(clientMove.substring(0, 1));
		int col = Integer.parseInt(clientMove.substring(1, 2));
		board.setBox(row, col, 'O');
		if (board.checkWinner('O')) {
			output.writeObject("ganador");
			output.flush();
			return true;
		}
		return false;
	}
  
	public void serverMoves() throws IOException {
		String serverMove = findBestMove();
		if (serverMove.isBlank()) {
			output.writeObject("empate");
			output.flush();
			return;
		}
		int row = Integer.parseInt(serverMove.substring(0, 1));
		int col = Integer.parseInt(serverMove.substring(1, 2));
		board.setBox(row, col, 'X');
		if (board.checkWinner('X')) {
			output.writeObject("perdedor-" + serverMove);
			output.flush();
			return;
		}
		output.writeObject(serverMove);
		output.flush();
	}
  
	public String findBestMove() {
		char[][] boxes = board.getBoxes();
		if (board.getIsFirstMove()) {
			if (boxes[1][1] == 'O') return "00";
			if (boxes[0][0] + boxes[0][2] + boxes[2][0] + boxes[2][2] != ' ' * 4) {
				for (int i = 0; i <= 2; i += 2) {
					for (int j = 0; j <= 2; j += 2) {
						if (boxes[i][j] == 'O') return j == 0 ? "12" : "10";
					}
				}
			}
			for (int i = 0; i <= 2; i++) {
				for (int j = i % 2 == 0 ? 1 : 0; j <= 2; j += 2) {
					if (boxes[i][j] == 'O') return i + j == 1 ? "00" : "22";
				}
			}
		}
		String serverWin = almostWin('X');
		if (!serverWin.isBlank()) return serverWin;
		String blockClient = almostWin('O');
		if (!blockClient.isBlank()) return blockClient;
		return randomMove();
	}
	
	public String almostWin(char piece) {
		char[][] boxes = board.getBoxes();
		for (int i = 0; i <= 2; i++) {
			if (boxes[i][0] + boxes[i][1] + boxes[i][2] == piece * 2 + ' ') return findEmpty('r', i);
			if (boxes[0][i] + boxes[1][i] + boxes[2][i] == piece * 2 + ' ') return findEmpty('c', i);
		}
		if (boxes[0][0] + boxes[1][1] + boxes[2][2] == piece * 2 + ' ') return findEmpty('d', 1);
		if (boxes[0][2] + boxes[1][1] + boxes[2][0] == piece * 2 + ' ') return findEmpty('d', -1);
		return "";
	}
	
	private String findEmpty(char mode, int pos) {
		char[][] boxes = board.getBoxes();
		for (int i = 0; i <= 2; i++) {
			if (mode == 'r' && boxes[pos][i] == ' ') return "" + pos + i;
			else if (mode == 'c' && boxes[i][pos] == ' ') return "" + i + pos;
			else if (mode == 'd' && pos == 1 && boxes[i][i] == ' ') return "" + i + i;
			else if (mode == 'd' && pos == -1 && boxes[i][2 - i] == ' ') return "" + i + (2 - i);
		}
		return "";
	}
	
	public String randomMove() {
		for (int i = 0; i <= 2; i++) {
			for (int j = 0; j <= 2; j++) {
				if (board.getBoxes()[i][j] == ' ') return "" + i + j;
			}
		}
		return "";
	}
	
}
