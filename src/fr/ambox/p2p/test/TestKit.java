package fr.ambox.p2p.test;

import java.io.IOException;
import java.util.HashMap;

import fr.ambox.p2p.App;
import fr.ambox.p2p.chat.ChatService;
import fr.ambox.p2p.configuration.ConfigurationService;
import fr.ambox.p2p.configuration.IdentityService;
import fr.ambox.p2p.peers.FriendshipService;
import fr.ambox.p2p.peers.PeersService;

public class TestKit {
	private static App[] apps;
	
	public static void createApps(int n) {
		TestKit.apps = new App[n];
		for (int i=0; i<n; i++) {
			App a = new App("app"+i);
			IdentityService identity = (IdentityService) a.getService("identity");
			identity.getMyId().setNickname("nick_app"+i);
			ConfigurationService configuration = (ConfigurationService) a.getService("configuration");
			configuration.setOption("routerPort", String.valueOf(10000 + (i*100)));
			configuration.setOption("httpPort", String.valueOf(10000 + (i*100) + 80));
			TestKit.apps[i] = a;
		}
		for (App a : TestKit.apps) {
			(new Thread(a)).start();
		}
	}
	
	private static void makeFullFriends(int a1, int a2) {
		TestKit.makeFriend(a1, a2);
		TestKit.makeFriend(a2, a1);
	}
	
	private static void makeFriend(int a1, int a2) {
		FriendshipService friendship = (FriendshipService) TestKit.apps[a1].getService("friendship");
		
		ConfigurationService configuration = (ConfigurationService) TestKit.apps[a2].getService("configuration");
		String hostandport = "127.0.0.1:"+configuration.getOption("routerPort");
		
		try {
			IdentityService identity = (IdentityService) TestKit.apps[a2].getService("identity");
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("address", hostandport);
			params.put("identity", identity.getMyId().toBase64());
			friendship.apiPOST(params);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void printFriends() {
		System.out.println("=== friends ===");
		for (int i=0; i<TestKit.apps.length; i++) {
			FriendshipService friendship = (FriendshipService) TestKit.apps[i].getService("friendship");
			System.out.println("app"+i+": "+friendship.apiGET().getBodyAsString());
		}
		System.out.println();
	}
	
	private static void printPeers() {
		System.out.println("=== peers ===");
		for (int i=0; i<TestKit.apps.length; i++) {
			PeersService peers = (PeersService) TestKit.apps[i].getService("peers");
			System.out.println("app"+i+": "+peers.apiGET().getBodyAsString());
		}
		System.out.println();
	}
	
	private static void publicChat(int a, String string) {
		ChatService chat = (ChatService) TestKit.apps[a].getService("chat");
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("text", string);
		params.put("range", "public");
		chat.apiPOST(params);
	}
	
	private static void privateChat(int from, int to, String string) {
		ChatService chat = (ChatService) TestKit.apps[from].getService("chat");
		IdentityService identity = (IdentityService) TestKit.apps[to].getService("identity");
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("text", string);
		params.put("range", "private");
		params.put("id", identity.getMyId().getId());
		chat.apiPOST(params);
	}
	
	private static void printPublicChat() {
		System.out.println("=== chat public ===");
		for (int i=0; i<TestKit.apps.length; i++) {
			ChatService chat = (ChatService) TestKit.apps[i].getService("chat");
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("range", "public");
			System.out.println("app"+i+": "+chat.apiGET(params).getBodyAsString());
		}
		System.out.println();
	}
	
	private static void printPrivateChat(int a1, int a2) {
		ChatService chat = (ChatService) TestKit.apps[a1].getService("chat");
		IdentityService identity = (IdentityService) TestKit.apps[a2].getService("identity");
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("range", "private");
		params.put("id", identity.getMyId().getId());
		System.out.println("app"+a1+" privé avec app"+a2+": "+chat.apiGET(params).getBodyAsString());
	}
	
	private static void printApps() {
		System.out.println("=== apps ===");
		for (int i=0; i<TestKit.apps.length; i++) {
			IdentityService identity = (IdentityService) TestKit.apps[i].getService("identity");
			System.out.println("app"+i+": "+identity.getMyId());
		}
		System.out.println();
	}
	
	private static void sleep(int t) {
		System.out.print("Sleeping");
		int interval = 200;
		for (int i=0; i<(t*1000); i+=interval) {
			System.out.print(".");
			try { Thread.sleep(interval); } 
			catch (InterruptedException e) { e.printStackTrace(); }
		}
		System.out.println();
	}

	// =========== TESTS ===========
	public static void main(String[] args) throws IOException, InterruptedException  {
		//test1();
		test2();
	}
	
	public static void test1() throws IOException, InterruptedException {
		// testing message forwarding with private chat
		
		TestKit.createApps(3);
		TestKit.printApps();
		
		TestKit.makeFullFriends(0, 1);
		TestKit.makeFullFriends(1, 2);
		
		TestKit.printFriends();
		
		TestKit.sleep(2);
		
		TestKit.publicChat(2, "discover me!");
		
		TestKit.sleep(2);
		
		TestKit.privateChat(0, 2, "message privée de app0 à app2");
		TestKit.privateChat(1, 2, "message privée de app1 à app2");

		TestKit.sleep(2);
		
		TestKit.printPeers();
		TestKit.printPublicChat();
		
		TestKit.printPrivateChat(2, 0);
		TestKit.printPrivateChat(2, 1);
	}
	
	public static void test2() throws IOException, InterruptedException {
		// testing message loop
		TestKit.createApps(4);
		TestKit.printApps();
		
		/*
		  +---+
		  | 0 +-+
		  +---+ |
		        |
		        |
		      +-+-+    +---+
		      | 1 +----+ 2 |
		      +-+-+    +-+-+
		        |        |
		        |        |
		        | +---+  |
		        +-+ 3 +--+
		          +---+
		*/
		
		TestKit.makeFullFriends(0, 1);
		TestKit.makeFullFriends(1, 2);
		TestKit.makeFullFriends(1, 3);
		TestKit.makeFullFriends(2, 3);
		
		TestKit.printFriends();
		
		TestKit.sleep(2);
		
		TestKit.publicChat(3, "bonjour de test");
		
		TestKit.sleep(2);
		
		TestKit.printPeers();
		TestKit.printPublicChat();
	}
}
