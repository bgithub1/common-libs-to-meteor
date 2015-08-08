package com.billybyte.commonlibstometeor.runs.apps.greeks;


import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.billybyte.commonlibstometeor.GreeksData;
import com.billybyte.commonlibstometeor.runs.ArgBundle;
import com.billybyte.commonlibstometeor.runs.NewProcessLauncher;
import com.billybyte.commonlibstometeor.runs.apps.ProcessMeteorPositionChanges;
import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.dse.DerivativeSetEngine;
import com.billybyte.dse.debundles.DerivativeSetEngineBuilder;
import com.billybyte.meteorjava.MeteorListCallback;
import com.billybyte.meteorjava.MeteorListSendReceive;


/**
 * Run greeks reactively when a meteor client adds or removes a Position record.
 * @author bperlman1
 *
 */
public class RunGreeksFromMeteorPositionChanges {
	private static class ProcessGreeksFromMeteorPositionChanges extends ProcessMeteorPositionChanges<GreeksData>{

		public ProcessGreeksFromMeteorPositionChanges(DerivativeSetEngine dse,
				String meteorUrl, Integer meteorPort, String adminEmail,
				String adminPass, Class<GreeksData> classOfM) {
			super(dse, meteorUrl, meteorPort, adminEmail, adminPass, classOfM);
		}

		@Override
		public List<GreeksData> aggregateMrecs(
				List<GreeksData> mRecPerPositionList) {
			/// nothing to do
			return mRecPerPositionList;
		}
	
	}
	
	
	public static void main(String[] args) {
		// Get all of the usual arguments that pertain to connecting to meteor
		ArgBundle ab = new ArgBundle(args);
		// Get the path of a Spring Xml file that will create a DerivativeSetEngine
		
		// Create main processor that turns Position objects
		//  into instances of GreeksData.
		// you don't really use yahoo to get dse
		final DerivativeSetEngine dse = 
				DerivativeSetEngineBuilder.dseForStocksUsingYahooAndSpring(null,ab.dseXmlPath);
		final ProcessGreeksFromMeteorPositionChanges mainProcessor = 
				new ProcessGreeksFromMeteorPositionChanges(
						dse, 
						ab.meteorUrl, ab.meteorPort, ab.adminEmail, 
						ab.adminPass, GreeksData.class);
		
		// Start the loop that waits for changes in the collection
		//   com.billybyte.commonlibstometeor.Position because of an
		//   insert or delete by the client in that collection, and
		//   then sends a List<GreeksData> to Meteor for that client (userId).
		mainProcessor.process();
		final String[] finalArgs = args;
		final List<String> finalVmArgs = ab.vmArgs;
		final Class<?> clazz = RunGreeksFromMeteorPositionChanges.class;
		// do silent death restart
		final MeteorListCallback<String> heartBeatCallback = 
				new MeteorListCallback<String>() {
					
					@Override
					public void onMessage(String messageType, String id, String convertedMessage) {
						Integer intId = new Integer(id);
						if(intId==0){
							Utils.prtObErrMess(this.getClass(), "detected heartbeat drop, restarting meteor");
							try {
								Thread.sleep(5000);
							} catch (InterruptedException e) {
								throw Utils.IllState(e);
							}
							new Thread(new NewProcessLauncher(RunGreeksFromMeteorPositionChanges.class, 
									finalVmArgs.toArray(new String[]{}), finalArgs)).start();
							throw Utils.IllState(clazz,"throwing exception to stop processing before restart");
						}else{
							Utils.prtObErrMess(this.getClass(), "Problem detecting heartbeat drop, aborting meteor listener");
							throw Utils.IllState(clazz,"throwing exception to stop processing after error in detecting heartbeat");
						}
						
					}
				};
		// start up a new heartbeat detector
//		new MeteorListSendReceive<String>(mainProcessor.getMlsrOfTableChangedByUser(),String.class).heartBeatAlertStart(heartBeatCallback, 20);
//		final AtomicBoolean keepRunning = new AtomicBoolean(true);
		final long secondsToWait = 20;
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				while(true){
					try {
						Integer zeroIfGood = mainProcessor.getMlsrOfTableChangedByUser().getHeartbeat();
						Utils.prtObMess(this.getClass(), "heartbeat: " + (zeroIfGood==null ? "null" : zeroIfGood.toString()));
						if(zeroIfGood==null){
							heartBeatCallback.onMessage("No Heartbeat", "0", "no heartbeat detected from meteor");
							break;
						}
						Thread.sleep(secondsToWait*1000);
					} catch (InterruptedException e) {
						break;
					}
				}
			}
		}).start();

		
	}
	
	
	
}
