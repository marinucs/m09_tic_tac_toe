package practica4;

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
			while (true) {
				Socket client = serverSocket.accept();
				System.out.println("Conexión aceptada de: " + client.getInetAddress());
				new ServerThread(client).start();
			}
		} catch (Exception e) { e.printStackTrace(); }
	}

	public static void main(String[] args) throws Exception {
		Server server = new Server();
		server.start();
	}

}
