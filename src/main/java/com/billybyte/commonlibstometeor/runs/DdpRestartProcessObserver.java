package com.billybyte.commonlibstometeor.runs;

import java.util.Observable;
import java.util.Observer;


import me.kutrumbos.DdpClient;

public class DdpRestartProcessObserver implements Observer {

	private final Class<?> clazz;
	private final String[] vmArgs; 
	private final String[] args;
	private final static String ON_CONN_CLOSE_MSG = DdpClient.getConnectionClosedMsg();

	public DdpRestartProcessObserver(Class<?> clazz, String[] vmArgs,
			String[] args) {
		super();
		this.clazz = clazz;
		this.vmArgs = vmArgs;
		this.args = args;
	}

	@Override
	public void update(Observable observable, Object data) {
		if(data instanceof String) {
			String msg = (String) data;
			if(msg.contains(ON_CONN_CLOSE_MSG)){
				new Thread(new NewProcessLauncher(clazz, vmArgs, args)).start();
			}
//			System.out.println(msg);
		}
	}

}
