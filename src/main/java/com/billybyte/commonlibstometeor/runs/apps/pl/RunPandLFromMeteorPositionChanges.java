package com.billybyte.commonlibstometeor.runs.apps.pl;


//import com.billybyte.commonlibstometeor.ProfitAndLoss;
import com.billybyte.commonlibstometeor.ProfitAndLoss;
import com.billybyte.commonlibstometeor.runs.ArgBundle;
import com.billybyte.commonlibstometeor.runs.apps.ProcessMeteorPositionChanges;
import com.billybyte.dse.DerivativeSetEngine;
import com.billybyte.dse.debundles.DerivativeSetEngineBuilder;


/**
 * Run ProfitAndLoss reactively when a meteor client adds or removes a Position record.
 * @author bperlman1
 *
 */
public class RunPandLFromMeteorPositionChanges {
	private static class ProcessPlFromMeteorPositionChanges extends ProcessMeteorPositionChanges<ProfitAndLoss>{

		public ProcessPlFromMeteorPositionChanges(DerivativeSetEngine dse,
				String meteorUrl, Integer meteorPort, String adminEmail,
				String adminPass, Class<ProfitAndLoss> classOfM) {
			super(dse, meteorUrl, meteorPort, adminEmail, adminPass, classOfM);
			// TODO Auto-generated constructor stub
		}
	
	}
	
	
	public static void main(String[] args) {
		// Get all of the usual arguments that pertain to connecting to meteor
		ArgBundle ab = new ArgBundle(args);
		// Get the path of a Spring Xml file that will create a DerivativeSetEngine
		
		
		// you don't really use yahoo to get dse
		final DerivativeSetEngine dse = 
				DerivativeSetEngineBuilder.dseForStocksUsingYahooAndSpring(null,ab.dseXmlPath);
		// Create main processor that turns Position objects
		//  into instances of ProfitAndLoss.
		ProcessPlFromMeteorPositionChanges mainProcessor = 
				new ProcessPlFromMeteorPositionChanges(
						dse, 
						ab.meteorUrl, ab.meteorPort, ab.adminEmail, 
						ab.adminPass, ProfitAndLoss.class
						);
		
		// Start the loop that waits for changes in the collection
		//   com.billybyte.commonlibstometeor.Position because of an
		//   insert or delete by the client in that collection, and
		//   then sends a List<ProfitAndLoss> to Meteor for that client (userId).
		mainProcessor.process();
	}
}
