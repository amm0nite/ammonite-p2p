package fr.ambox.f2f.connexion;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import fr.ambox.f2f.Service;
import fr.ambox.f2f.configuration.ConfigurationService;

public class ConnexionReceiver extends Service {
	private ServerSocket socket;

	public ConnexionReceiver() {
		
	}

	public void run() {
		ConfigurationService configuration = (ConfigurationService) this.getService("configuration");
		int port = Integer.valueOf(configuration.getOption("routerPort"));
		
		try {
			this.socket = new ServerSocket(port);
			this.log("messageServer listenning on port "+port);
			
			try {
				while (true) {
					Socket clientSocket = this.socket.accept();
					
					this.log("connection from "+clientSocket.getInetAddress().getHostAddress());
					ConnexionHandler messageHandler = new ConnexionHandler(clientSocket);
					
					messageHandler.bindService("receiver", this);
					messageHandler.bindService("logger", this.getService("logger"));
					messageHandler.bindService("identity", this.getService("identity"));
					messageHandler.bindService("friendship", this.getService("friendship"));
					messageHandler.bindService("peers", this.getService("peers"));
					messageHandler.bindService("watcher", this.getService("watcher"));
					messageHandler.bindService("emitter", this.getService("emitter"));
					messageHandler.bindService("chat", this.getService("chat"));
					
					messageHandler.start();
				}
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException();
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}
}
