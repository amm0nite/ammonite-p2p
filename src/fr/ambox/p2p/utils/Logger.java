package fr.ambox.f2f.utils;

import fr.ambox.f2f.Service;

public class Logger extends Service {
	private String appid;

	public Logger(String appid) {
		this.appid = appid;
	}
	
	public void log(String msg) {
		//StackTraceElement[] elements = Thread.currentThread().getStackTrace(); 
		
		//Throwable t = new Throwable();
		//StackTraceElement[] elements = t.getStackTrace();

		//String calleeMethod = elements[0].getMethodName();
		//String callerMethodName = elements[1].getMethodName();
		//String callerClassName = elements[1].getClassName(); 
		
		//System.out.println("["+this.appid+"] "+callerClassName+" | "+msg);
		//System.out.println("["+this.appid+"] "+callerClassName.substring(callerClassName.lastIndexOf('.')+1)+" | "+msg);
		
		System.out.println("["+this.appid+"] "+msg);
	}

	@Override
	public void run() {
		
	}
}
