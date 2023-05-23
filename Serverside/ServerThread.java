package practica5;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class ServerThread extends Thread {
	
	static ArrayList<ObjectOutputStream> outputs = new ArrayList<>();
	static boolean starting = false;
	private char player;
	private ObjectOutputStream output;
	private ObjectOutputStream enemyOutput;
	private ObjectInputStream input;
	private int outputPosition;
	private Socket socket;
	private Board board;
	private boolean end;
	
	public ServerThread(Socket socket) throws IOException {
		this.socket = socket;
		input = new ObjectInputStream(socket.getInputStream());
		output = new ObjectOutputStream(socket.getOutputStream());
		outputs.add(output);
		outputPosition = outputs.size() - 1;
		board = new Board();
		end = false;
	}
	
	@Override
	public void run() {
		try {
			do {
	            String clientMove = (String) input.readObject();
	            if (clientMove.equals("jugar")) {
	            	if (outputs.size() != 2) {
	            		output.writeObject("nack");
		    			output.flush();
	            	} else if (!starting) {
	            		player = 'X';
	            		starting = true;
	            		enemyOutput = outputs.get(outputPosition == 0 ? 1 : 0);
	            	} else {
	            		player = 'O';
	            		enemyOutput = outputs.get(outputPosition == 0 ? 1 : 0);
	            		board.setEmptyBoard();
	            		enemyOutput.writeObject("ack");
	            		enemyOutput.flush();
	            	}
	            } else {
	            	movePiece(clientMove);
	            }
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
  
	public void movePiece(String clientMove) throws IOException {
		int row = Integer.parseInt(clientMove.substring(0, 1));
		int col = Integer.parseInt(clientMove.substring(1, 2));
		board.setBox(row, col, player);
		if (board.checkWinner(player)) {
			output.writeObject("ganador");
			output.flush();
			enemyOutput.writeObject("perdedor-" + clientMove);
			enemyOutput.flush();
			starting = false;
		} else {
			enemyOutput.writeObject(clientMove);
    		enemyOutput.flush();
		}
	}
	
}
