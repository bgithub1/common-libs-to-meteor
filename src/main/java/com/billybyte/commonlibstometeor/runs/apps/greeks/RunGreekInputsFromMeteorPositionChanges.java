package com.billybyte.commonlibstometeor.runs.apps.greeks;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.billybyte.commoncollections.Tuple;
import com.billybyte.commonlibstometeor.GreekInputsData;
import com.billybyte.commonlibstometeor.Position;
import com.billybyte.commonlibstometeor.runs.ArgBundle;
import com.billybyte.commonlibstometeor.runs.apps.ProcessMeteorPositionChanges;
import com.billybyte.dse.DerivativeSetEngine;
import com.billybyte.dse.debundles.DerivativeSetEngineBuilder;
import com.billybyte.dse.inputs.InBlk;
import com.billybyte.queries.ComplexQueryResult;


/**
 * Get greek inputs reactively when a meteor client adds or removes a Position record.
 * This class overrides processSensitivitiesPerUserId b/c it does not return a record per position per userId
 * @author bperlman1
 *
 */
public class RunGreekInputsFromMeteorPositionChanges {
	private static class ProcessGreekInputsFromMeteorPositionChanges extends ProcessMeteorPositionChanges<GreekInputsData>{

		public ProcessGreekInputsFromMeteorPositionChanges(DerivativeSetEngine dse,
				String meteorUrl, Integer meteorPort, String adminEmail,
				String adminPass) {
			super(dse, meteorUrl, meteorPort, adminEmail, adminPass, GreekInputsData.class);
		}

		@Override
		public List<GreekInputsData> aggregateMrecs(
				List<GreekInputsData> mRecPerPositionList) {
			/// nothing to do
			return mRecPerPositionList;
		}
	
		@Override
		public Map<String, Tuple<List<String>, List<GreekInputsData>>> processSensitivitiesPerUserId(
				List<Position> positionFromMeteor, Double errorValueToReturn) {
			// create return object
			Map<String, Tuple<List<String>, List<GreekInputsData>>> ret = 
					new HashMap<String, Tuple<List<String>,List<GreekInputsData>>>();
			// get shortNames per userId, and all shortNames 
			// userIdToShortNameListMap is a map that maps userId's to lists of shortNames
			Map<String,List<String>> userIdToShortNameListMap = new HashMap<String, List<String>>();
			// derivativeShortNameSet is the set of all shortNames for all userIds
			Set<String> derivativeShortNameSet = new HashSet<String>();
			for(Position p : positionFromMeteor){
				derivativeShortNameSet.add(p.getShortName());
				String userId = p.getUserId();
				List<String> snList = userIdToShortNameListMap.get(userId);
				if(snList==null){
					 snList = new ArrayList<String>();
					 userIdToShortNameListMap.put(userId,snList);
				}
				snList.add(p.getShortName());
			}
			
			// inputBlkCqrMap is the Dse inputs blk
			Map<String, ComplexQueryResult<InBlk>> inputBlkCqrMap = 
					getDse().getInputs(derivativeShortNameSet);
			
			// inputCsvList is a formated list of inputs
			List<String[]> inputCsvList =
					InBlk.getCsvListFromInBlkMap(getDse(), inputBlkCqrMap, 4);
			
			// sort the inputCsvList into map of shortName/List<String[]>
			Map<String, String[]> shortNameToInputCsvList = new HashMap<String, String[]>();
			String[] header = inputCsvList.get(0);
			for(int i = 1;i<inputCsvList.size();i++){
				String[] inputCsv = inputCsvList.get(i);
				String shortName = inputCsv[0];
				shortNameToInputCsvList.put(shortName,inputCsv);
			}
			
			// dummy problems list - problems aren't implemented yet so just pass empty list
			List<String> problems = new ArrayList<String>();
			
			
			// iterate thru userIdToShortNameListMap to get results per userId			
			for(Entry<String, List<String>> entry : userIdToShortNameListMap.entrySet()){
				// get the userId
				String userId = entry.getKey();
				// see if we have already created a return tuple for this userId
				Tuple<List<String>, List<GreekInputsData>> tuple  = ret.get(userId);
				if(tuple==null){
					// no, create new tuple with empty GreekInputsData list
					tuple = new Tuple<List<String>, List<GreekInputsData>>(problems, new ArrayList<GreekInputsData>());
					// put it back in the ret map
					ret.put(userId, tuple);
				}
				
				// get the greekInputList for this userId
				List<GreekInputsData> greekInputList = tuple.getT2_instance();
				// put the header returned by getCsvListFromInBlkMap in that list
				GreekInputsData gidHeader = 
						new GreekInputsData(header[0], userId, "", "", header);
				greekInputList.add(gidHeader);
				// put the csv inputs for the shortNames that this userId used in the List<GreekInputsData> of this tuple entry
				for(String shortName : entry.getValue()){
					String[] csvInputs = shortNameToInputCsvList.get(shortName);
					GreekInputsData gid = 
							new GreekInputsData(csvInputs[0], userId, "", "", csvInputs);
					greekInputList.add(gid);
				}
			}
			return ret;
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
						dse, 
						ab.meteorUrl, ab.meteorPort, ab.adminEmail, 
						ab.adminPass
						);
		
		// Start the loop that waits for changes in the collection
		//   com.billybyte.commonlibstometeor.Position because of an
		//   insert or delete by the client in that collection, and
		//   then sends a List<GreeksData> to Meteor for that client (userId).
		mainProcessor.process();
	}
}
