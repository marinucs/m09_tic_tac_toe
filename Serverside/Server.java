package practica5;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
  
	private ServerSocket serverSocket;
  
	public Server() throws IOException {
		serverSocket = new ServerSocket(4000);
		System.out.println("Establecido servidor");
		System.out.println("Esperando conexiones de clientes...");
	}
	
	private void start() {
		try {
			Socket client1 = serverSocket.accept();
			System.out.println("Conexión aceptada de Jugador 1: " + client1.getInetAddress());
			new ServerThread(client1).start();
			Socket client2 = serverSocket.accept();
			System.out.println("Conexión aceptada de Jugador 2: " + client2.getInetAddress());
			new ServerThread(client2).start();
		} catch (Exception e) { e.printStackTrace(); }
	}

	public static void main(String[] args) throws Exception {
		Server server = new Server();
		server.start();
	}

}
