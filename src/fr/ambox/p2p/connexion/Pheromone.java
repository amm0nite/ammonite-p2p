package fr.ambox.f2f.connexion;

import fr.ambox.f2f.peers.Friend;

public class Pheromone {
	private Friend friend;
	private long time;
	private int hops;
	
	public Pheromone(Friend friend, int hops) {
		this.friend = friend;
		this.hops = hops;
		this.time = System.currentTimeMillis();
	}

	public int getHops() {
		return this.hops;
	}

	public Friend getFriend() {
		return this.friend;
	}

	public long getTime() {
		return this.time;
	}
}
