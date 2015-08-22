package com.billybyte.commonlibstometeor.runs.apps.greeks;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.billybyte.commonlibstometeor.DummyPositionBased;
import com.billybyte.commonlibstometeor.Position;
import com.billybyte.commonlibstometeor.runs.ArgBundle;
import com.billybyte.commonlibstometeor.runs.apps.ProcessMeteorPositionChanges;
import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.dse.DerivativeSetEngine;
import com.billybyte.dse.debundles.DerivativeSetEngineBuilder;
import com.billybyte.dse.inputs.InBlk;
import com.billybyte.meteorjava.MeteorCsvSendReceive;
import com.billybyte.queries.ComplexQueryResult;


/**
 * Get greek inputs reactively when a meteor client adds or removes a Position record,
 *   or when she hits the recalc button.
 * This class overrides processSensitivitiesPerUserId b/c it does not return a record per position per userId
 * @author bperlman1
 *
 */
public class RunGreekInputsCsvFromMeteorPositionChanges {
	public static final String GREEKINPUTSDATA_TABLENAME = "GreekInputsData";
	
	
	/**
	 * Private extension of ProcessMeteorPositionChanges that overrides processMrecs.
	 *   Normally, you would implement methods in the generic class of that would
	 *     return "per position record" results.  However, don't want to return a the
	 *     same greeks input record for several positions that hold the same shortName. 
	 * @author bill perlman
	 *
	 */
	private static class ProcessGreekInputsFromMeteorPositionChanges extends ProcessMeteorPositionChanges<DummyPositionBased>{
		private final MeteorCsvSendReceive mcsvsr;
		private final String tableName;
		public ProcessGreekInputsFromMeteorPositionChanges(
				String tableName,
				DerivativeSetEngine dse,
				String meteorUrl, Integer meteorPort, String adminEmail,
				String adminPass) {
			super(dse, meteorUrl, meteorPort, adminEmail, adminPass, DummyPositionBased.class,new String[]{Position.class.getCanonicalName(),tableName});
			this.tableName = tableName;
			this.mcsvsr = new MeteorCsvSendReceive(this.getMlsrOfTableChangedByUser());
		}


		@Override
		public List<DummyPositionBased> aggregateMrecs(List<DummyPositionBased> mRecPerPositionList) {
			// not used
			return null;
		}
		
		/**
		 * Override of processMrecs to aggregate results by shortName, and return one
		 *   record per shortName with all of the inputs that go into the DerivativeSetEngine
		 *   calcs for that shortName.
		 */
		@Override
		public void processMrecs(List<Position> positionFromMeteor,
				Double errorValueToReturn) {
			Set<String> derivativeShortNameSet = new HashSet<String>();
			Set<String> userIdSet = new HashSet<String>(); // this should be only one element
			// ***** !!! all of the records in positionFromMeteor should be for the same 
			//    userId.  
			String onlyUserId = null;
			for(Position p : positionFromMeteor){
				derivativeShortNameSet.add(p.getShortName());
				onlyUserId = p.getUserId();
				if(onlyUserId!=null){
					userIdSet.add(onlyUserId);
				}
			}
			// throw exceptions if no or multiple users are being processed.
			if(userIdSet.size()>1){
				throw Utils.IllState(this.getClass(), "more than one userId is being processed when it shouldn't be");
			}
			if(onlyUserId==null){
				throw Utils.IllState(this.getClass(), "no userId is being processed when it should be");
			}
			
			// get inputs from dse.
			Map<String, ComplexQueryResult<InBlk>> inputBlkCqrMap = 
					getDse().getInputs(derivativeShortNameSet);
			
			// inputCsvList is a formated list of inputs
			List<String[]> inputCsvList =
					InBlk.getCsvListFromInBlkMap(getDse(), inputBlkCqrMap, 4);
			List<String[]> outputCsv = new ArrayList<String[]>();
			for(int i = 0;i<inputCsvList.size();i++){
				String[] line = inputCsvList.get(i);
				String[] newLine = new String[line.length];
				for(int j = 0;j<line.length;j++){
					String value = line[j];
					if(value==null){
						value = "";
					}
					newLine[j]  = value;
				}
				outputCsv.add(newLine);
			}
	
			this.mcsvsr.sendCsvData(onlyUserId, tableName, outputCsv,true);
			
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
		
		ProcessGreekInputsFromMeteorPositionChanges mainProcessor = 
				new ProcessGreekInputsFromMeteorPositionChanges(
						GREEKINPUTSDATA_TABLENAME,
						dse, 
						ab.meteorUrl, ab.meteorPort, ab.adminEmail, 
						ab.adminPass
						);
		MeteorCsvSendReceive mcsvsr = new MeteorCsvSendReceive(mainProcessor.getMlsrOfTableChangedByUser());
		mcsvsr.sendCsvTableModel(GREEKINPUTSDATA_TABLENAME);
		// Start the loop that waits for changes in the collection
		//   com.billybyte.commonlibstometeor.Position because of an
		//   insert or delete by the client in that collection, and
		//   then sends a List<GreeksData> to Meteor for that client (userId).
		mainProcessor.process();
	}
}
