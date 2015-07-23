package com.billybyte.commonlibstometeor.runs.apps.greeks;


import java.util.List;

import com.billybyte.commonlibstometeor.GreeksData;
import com.billybyte.commonlibstometeor.runs.ArgBundle;
import com.billybyte.commonlibstometeor.runs.apps.ProcessMeteorPositionChanges;
import com.billybyte.dse.DerivativeSetEngine;
import com.billybyte.dse.debundles.DerivativeSetEngineBuilder;


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

		ProcessGreeksFromMeteorPositionChanges mainProcessor = 
				new ProcessGreeksFromMeteorPositionChanges(
						dse, 
						ab.meteorUrl, ab.meteorPort, ab.adminEmail, 
						ab.adminPass, GreeksData.class
						);
		
		// Start the loop that waits for changes in the collection
		//   com.billybyte.commonlibstometeor.Position because of an
		//   insert or delete by the client in that collection, and
		//   then sends a List<GreeksData> to Meteor for that client (userId).
		mainProcessor.process();
	}
}
