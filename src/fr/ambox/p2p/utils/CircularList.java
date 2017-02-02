package fr.ambox.p2p.utils;

import java.util.concurrent.LinkedBlockingDeque;

public class CircularList<T> {
	private LinkedBlockingDeque<T> list;
	private int size;
	
	public CircularList(int size) {
		this.list = new LinkedBlockingDeque<T>();
		this.size = size;
	}
	
	public void add(T e) {
		if (this.list.size() > this.size) {
			this.list.removeFirst();	
		}
		this.list.add(e);
	}
	
	public Object[] values() {
		return this.list.toArray();
	}
}
