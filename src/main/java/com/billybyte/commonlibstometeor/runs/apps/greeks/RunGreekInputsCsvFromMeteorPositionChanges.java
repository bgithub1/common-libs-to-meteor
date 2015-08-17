package com.billybyte.commonlibstometeor.runs.apps.greeks;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.billybyte.commoncollections.Tuple;
import com.billybyte.commonlibstometeor.Position;
import com.billybyte.commonlibstometeor.PositionBaseItem;
import com.billybyte.commonlibstometeor.runs.ArgBundle;
import com.billybyte.commonlibstometeor.runs.apps.ProcessMeteorPositionChanges;
import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.dse.DerivativeSetEngine;
import com.billybyte.dse.debundles.DerivativeSetEngineBuilder;
import com.billybyte.dse.inputs.InBlk;
import com.billybyte.dse.outputs.DerivativeReturn;
import com.billybyte.dse.outputs.DerivativeSensitivityTypeInterface;
import com.billybyte.marketdata.SecDef;
import com.billybyte.meteorjava.MeteorColumnModel;
import com.billybyte.meteorjava.MeteorCsvSendReceive;
import com.billybyte.queries.ComplexQueryResult;


/**
 * Get greek inputs reactively when a meteor client adds or removes a Position record.
 * This class overrides processSensitivitiesPerUserId b/c it does not return a record per position per userId
 * @author bperlman1
 *
 */
public class RunGreekInputsCsvFromMeteorPositionChanges {
	public static final String GREEKINPUTSDATA_TABLENAME = "GreekInputsData";
	
	private class DummyPositionBased extends PositionBaseItem{

		public DummyPositionBased() {
			super(null,null,null,null);
			// not used
		}

		@Override
		public MeteorColumnModel[] buildColumnModelArray() {
			return null;
		}

		@Override
		public DerivativeSensitivityTypeInterface[] getDseSenseArray() {
			return null;
		}

		@Override
		public <M extends PositionBaseItem> Tuple<List<String>, List<M>> positionBasedItemFromDerivativeReturn(
				Position p,
				SecDef sd,
				Map<DerivativeSensitivityTypeInterface, DerivativeReturn[]> drSenseMap,
				List<SecDef> underlyingSds) {
			return null;
		}
		
	}
	
	
	private static class ProcessGreekInputsFromMeteorPositionChanges extends ProcessMeteorPositionChanges<DummyPositionBased>{
		private final MeteorCsvSendReceive mcsvsr;
		private final String tableName;
		public ProcessGreekInputsFromMeteorPositionChanges(
				String tableName,
				DerivativeSetEngine dse,
				String meteorUrl, Integer meteorPort, String adminEmail,
				String adminPass) {
			super(dse, meteorUrl, meteorPort, adminEmail, adminPass, DummyPositionBased.class,new String[]{tableName});
			this.tableName = tableName;
			this.mcsvsr = new MeteorCsvSendReceive(this.getMlsrOfTableChangedByUser());
		}


		@Override
		public List<DummyPositionBased> aggregateMrecs(List<DummyPositionBased> mRecPerPositionList) {
			// not used
			return null;
		}
		
		@Override
		public void processMrecs(List<Position> positionFromMeteor,
				Double errorValueToReturn) {
			Set<String> derivativeShortNameSet = new HashSet<String>();
			Set<String> userIdSet = new HashSet<String>(); // this should be only one element
			String onlyUserId = null;
			for(Position p : positionFromMeteor){
				derivativeShortNameSet.add(p.getShortName());
				onlyUserId = p.getUserId();
				if(onlyUserId!=null){
					userIdSet.add(onlyUserId);
				}
			}
			if(userIdSet.size()>1){
				throw Utils.IllState(this.getClass(), "more than one userId is being processed when it shouldn't be");
			}
			if(onlyUserId==null){
				throw Utils.IllState(this.getClass(), "no userId is being processed when it should be");
			}
			
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
			this.mcsvsr.sendCsvData(onlyUserId, tableName, outputCsv);
			
//			Map<String, Tuple<List<String>, List<String[]>>> outputPerUserid =
//					processCsvPerUserId(positionFromMeteor,-1111111.00);
//			
//			for(Entry<String, Tuple<List<String>, List<String[]>>> entry : outputPerUserid.entrySet()){
//				String userId = entry.getKey();
//				Tuple<List<String>, List<String[]>> tuple = entry.getValue();
//				if(tuple.getT2_instance()!=null){
//					List<String[]> csv = tuple.getT2_instance();
//					mcsvsr.sendCsvData(userId, tableName, csv);
//				}else{
//					List<String> errors = tuple.getT1_instance();
//					for(String error : errors){
//						Utils.prtObErrMess(this.getClass(), error);
//					}
//				}
//			}
		}

		
//		public Map<String, Tuple<List<String>, List<String[]>>> processCsvPerUserId(
//				List<Position> positionFromMeteor, Double errorValueToReturn) {
//			// create return object
//			Map<String, Tuple<List<String>, List<String[]>>> ret = 
//					new HashMap<String, Tuple<List<String>,List<String[]>>>();
//			// get shortNames per userId, and all shortNames 
//			// userIdToShortNameListMap is a map that maps userId's to lists of shortNames
//			Map<String,List<String>> userIdToShortNameListMap = new HashMap<String, List<String>>();
//			// derivativeShortNameSet is the set of all shortNames for all userIds
//			Set<String> derivativeShortNameSet = new HashSet<String>();
//			for(Position p : positionFromMeteor){
//				derivativeShortNameSet.add(p.getShortName());
//				String userId = p.getUserId();
//				List<String> snList = userIdToShortNameListMap.get(userId);
//				if(snList==null){
//					 snList = new ArrayList<String>();
//					 userIdToShortNameListMap.put(userId,snList);
//				}
//				snList.add(p.getShortName());
//			}
//			
//			// inputBlkCqrMap is the Dse inputs blk
//			Map<String, ComplexQueryResult<InBlk>> inputBlkCqrMap = 
//					getDse().getInputs(derivativeShortNameSet);
//			
//			// inputCsvList is a formated list of inputs
//			List<String[]> inputCsvList =
//					InBlk.getCsvListFromInBlkMap(getDse(), inputBlkCqrMap, 4);
//			
//			// sort the inputCsvList into map of shortName/List<String[]>
//			Map<String, String[]> shortNameToInputCsvList = new HashMap<String, String[]>();
//			String[] header = inputCsvList.get(0);
//			for(int i = 1;i<inputCsvList.size();i++){
//				String[] inputCsv = inputCsvList.get(i);
//				String shortName = inputCsv[0];
//				shortNameToInputCsvList.put(shortName,inputCsv);
//			}
//			
//			// dummy problems list - problems aren't implemented yet so just pass empty list
//			List<String> problems = new ArrayList<String>();
//			
//			
//			// iterate thru userIdToShortNameListMap to get results per userId			
//			for(Entry<String, List<String>> entry : userIdToShortNameListMap.entrySet()){
//				// get the userId
//				String userId = entry.getKey();
//				// see if we have already created a return tuple for this userId
//				Tuple<List<String>, List<String[]>> tuple  = ret.get(userId);
//				if(tuple==null){
//					// no, create new tuple with empty String[] csv list
//					tuple = new Tuple<List<String>, List<String[]>>(problems, new ArrayList<String[]>());
//					// put it back in the ret map
//					ret.put(userId, tuple);
//				}
//				
//				// get the List<String[]> for this userId
//				List<String[]> greekInputList = tuple.getT2_instance();
//				// put the header returned by getCsvListFromInBlkMap in that list
//				greekInputList.add(header);
//				// put the csv inputs for the shortNames that this userId used in the List<GreekInputsData> of this tuple entry
//				for(String shortName : entry.getValue()){
//					String[] csvInputs = shortNameToInputCsvList.get(shortName);
//					greekInputList.add(csvInputs);
//				}
//			}
//			
//			// return it
//			return ret;
//		}
//

		
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
