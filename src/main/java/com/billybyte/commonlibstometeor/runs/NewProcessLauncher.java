package com.billybyte.commonlibstometeor.runs;

import java.util.concurrent.CountDownLatch;

import com.billybyte.commonstaticmethods.Reflection.ProcessLauncher;

public class NewProcessLauncher implements Runnable {

	private final Class<?> clazz;
	private final String[] vmArgs; 
	private final String[] args;
	
	public NewProcessLauncher(Class<?> clazz, String[] vmArgs, String[] args) {
		super();
		this.clazz = clazz;
		this.vmArgs = vmArgs;
		this.args = args;
	}

	@Override
	public void run() {
		
		CountDownLatch latch = new CountDownLatch(1);
		ProcessLauncher psl = new ProcessLauncher(clazz, vmArgs, args, null, latch); 
		
		new Thread(psl).start();
		
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		System.exit(1);
		
	}

}
