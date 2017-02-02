package fr.ambox.p2p;

import fr.ambox.p2p.chat.ChatService;
import fr.ambox.p2p.configuration.ConfigurationService;
import fr.ambox.p2p.configuration.IdentityService;
import fr.ambox.p2p.connexion.ConnexionEmitter;
import fr.ambox.p2p.connexion.ConnexionReceiver;
import fr.ambox.p2p.connexion.ConnexionWatcher;
import fr.ambox.p2p.http.HttpReceiver;
import fr.ambox.p2p.peers.FriendshipService;
import fr.ambox.p2p.peers.PeersService;
import fr.ambox.p2p.utils.Logger;

public class App extends Service {
	public App(String appid) {
		
		ConfigurationService configurationService = new ConfigurationService();
		IdentityService identityService = new IdentityService();
		Logger loggerService = new Logger(appid);
		HttpReceiver httpReceiverService = new HttpReceiver();
		ConnexionWatcher connexionWatcherService = new ConnexionWatcher();
		ConnexionReceiver connexionReceiverService = new ConnexionReceiver();
		ConnexionEmitter connexionEmitterService = new ConnexionEmitter();
		FriendshipService friendshipService = new FriendshipService();
		PeersService peersService = new PeersService();
		ChatService chatService = new ChatService();
		
		this.bindService("logger", loggerService);
		this.bindService("configuration", configurationService);
		this.bindService("identity", identityService);
		this.bindService("friendship", friendshipService);
		this.bindService("peers", peersService);
		this.bindService("receiver", connexionReceiverService);
		this.bindService("watcher", connexionWatcherService);
		this.bindService("emitter", connexionEmitterService);
		this.bindService("http", httpReceiverService);
		this.bindService("chat", chatService);
		
		friendshipService.bindService("logger", loggerService);
		friendshipService.bindService("peers", peersService);
		
		httpReceiverService.bindService("logger", loggerService);
		httpReceiverService.bindService("identity", identityService);
		httpReceiverService.bindService("configuration", configurationService);
		httpReceiverService.bindService("friendship", friendshipService);
		httpReceiverService.bindService("peers", peersService);
		httpReceiverService.bindService("chat", chatService);
		
		chatService.bindService("logger", loggerService);
		chatService.bindService("identity", identityService);
		chatService.bindService("emitter", connexionEmitterService);
		chatService.bindService("peers", peersService);
		
		connexionReceiverService.bindService("logger", loggerService);
		connexionReceiverService.bindService("identity", identityService);
		connexionReceiverService.bindService("configuration", configurationService);
		connexionReceiverService.bindService("friendship", friendshipService);
		connexionReceiverService.bindService("peers", peersService);
		connexionReceiverService.bindService("watcher", connexionWatcherService);
		connexionReceiverService.bindService("emitter", connexionEmitterService);
		connexionReceiverService.bindService("chat", chatService);
		
		connexionEmitterService.bindService("logger", loggerService);
		connexionEmitterService.bindService("identity", identityService);
		connexionEmitterService.bindService("friendship", friendshipService);
	}

	@Override
	public void run() {
		for (Service s : this.getServices()) {
			s.start();
		}

		this.log("app is running");
		
		for (Service s : this.getServices()) {
			try { s.join(); } 
			catch (InterruptedException e) { e.printStackTrace(); }
		}
	}
}