package com.billybyte.commonlibstometeor.runs;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import com.billybyte.commoncollections.Tuple;
import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commonlibstometeor.GreeksData;
import com.billybyte.commonlibstometeor.Position;
import com.billybyte.dse.DerivativeSetEngine;
import com.billybyte.dse.debundles.DerivativeSetEngineBuilder;
import com.billybyte.dse.outputs.DerivativeReturn;
import com.billybyte.dse.outputs.DerivativeSensitivityTypeInterface;

import com.billybyte.marketdata.SecDef;
import com.billybyte.marketdata.SecDefQueryAllMarkets;
import com.billybyte.meteorjava.MeteorListSendReceive;
import com.billybyte.meteorjava.TableChangedByUser;
import com.billybyte.meteorjava.staticmethods.Utils;

/**
 * Run greeks reactively when a meteor client adds or removes a Position record.
 * @author bperlman1
 *
 */
public class RunGreeksFromMeteorPositionChanges {
	public static void main(String[] args) {
		// Get all of the usual arguments that pertain to connecting to meteor
		ArgBundle ab = new ArgBundle(args);
		// Get the path of a Spring Xml file that will create a DerivativeSetEngine
		String dseXmlPath = ab.argPairs.get("dseXmlPath");
		// Create a SecDef query
		final QueryInterface<String, SecDef> sdQuery = new SecDefQueryAllMarkets();
		
		// you don't really use yahoo to get dse
		final DerivativeSetEngine dse = 
				DerivativeSetEngineBuilder.dseForStocksUsingYahooAndSpring(null,dseXmlPath);

		// Create a MeteorListSendReceive instance to control communication with Meteor
		//  You'll reuse this connection object to get other kinds of objects besides TableChangedByUser objects
		MeteorListSendReceive<TableChangedByUser> mlsr = null;
		try {
			mlsr = new MeteorListSendReceive<TableChangedByUser>(100, 
							TableChangedByUser.class, 
							ab.meteorUrl, ab.meteorPort, 
							ab.adminEmail,ab.adminPass,"", "", "tester");
			
		} catch (URISyntaxException e) {
			throw Utils.IllState(e);
		}
		
		// Create a sub-mlsr to use to get and put GreeksData lists, by reusing the mlsr
		//   for TableChangedByUser changes.
		final MeteorListSendReceive<GreeksData> mlsrGreeksData = 
				new MeteorListSendReceive<GreeksData>(mlsr, GreeksData.class);
		// Only accept changes for the Position.class collection. 
		Set<String> collectionsToWatchFor = 
				new HashSet<String>(Arrays.asList(new String[]{Position.class.getCanonicalName()}));
		// Get a blocking queue from the mlsr that you can use in the new Runnable and new Thread that
		//  you create below.  This method will also subscribe to changes in the TableChangeByUser collection
		//  so that you can track changes in any collection on the client side, when the client adds or deletes
		//  records.
		final BlockingQueue<List<?>> blockingQueue = 
				mlsr.subscribeToTableChangedByUser(-1,collectionsToWatchFor);
		// Create a new anonymous Runnable instance that responds to List<Position> that the mlsr puts
		//   on blockingQueue
		Runnable r = new ProcessBlockingQueue(blockingQueue, mlsrGreeksData,dse);
		
		// startup the blockingqueue taker
		new Thread(r).run();
		
	}
	
	private static class ProcessBlockingQueue implements Runnable{
		private final BlockingQueue<List<?>> blockingQueue;
		private final MeteorListSendReceive<GreeksData> mlsrGreeksData;
		private final DerivativeSetEngine dse;
		private final QueryInterface<String, SecDef> sdQuery = 
				new SecDefQueryAllMarkets();
		
		private ProcessBlockingQueue(
				BlockingQueue<List<?>> blockingQueue,
				MeteorListSendReceive<GreeksData> mlsrGreeksData,
				DerivativeSetEngine dse) {
			super();
			this.blockingQueue = blockingQueue;
			this.mlsrGreeksData = mlsrGreeksData;
			this.dse = dse;
			
		}



		@Override
		public void run() {
			boolean keepGoing = true;
			// Loop until there's an error.  Remember that the loop is done on another thread.
			while(keepGoing){
				try {
					List<?> dataFromCollection = blockingQueue.take();
					// Make sure you got data.
					if(dataFromCollection.size()<1){
						continue;
					}
					// Get the class of the data.
					Class<?> classOfData = dataFromCollection.get(0).getClass();
					// Make sure it's of type Position.
					if(Position.class.isAssignableFrom(classOfData)){
						// This is a position list, so process it
						List<Position> positionList = 
								new ArrayList<Position>();
						for(Object o : dataFromCollection){
							positionList.add((Position)o);
						}
						Double errorValueToReturn = -11111111.0;
						// Create greeks and send them to Meteor clients that had a position change.
						processGreeks(mlsrGreeksData, positionList, dse, sdQuery, errorValueToReturn);
					}
				} catch (InterruptedException e) {						
					e.printStackTrace();
					keepGoing=false;
				}
			}
			
		}
		
	}
	
	private static void processGreeks(
			MeteorListSendReceive<GreeksData> mlsr,
			List<Position> positionFromMeteor,
			DerivativeSetEngine dse,
			QueryInterface<String, SecDef> sdQuery,
			Double errorValueToReturn){
		Map<String,SecDef> sdMap = new HashMap<String, SecDef>();
		Map<String, List<Position>> userIdToPosition = new HashMap<String, List<Position>>();
		// Build map of userId to position list
		for(Position p : positionFromMeteor){
			String userId = p.getUserId();
			String sn = p.getShortName();
			if(!sdMap.containsKey(sn)){
				SecDef sd = sdQuery.get(sn, 1, TimeUnit.SECONDS);
				if(sd==null){
					Utils.prtObErrMess(RunGreeksFromMeteorPositionChanges.class, "cannot get SecDef for shortName: " + sn);
				}else{
					sdMap.put(sn, sd);
				}
			}
			
			List<Position> positionsForThisUser = 
					userIdToPosition.get(userId);
			if(positionsForThisUser==null){
				positionsForThisUser = new ArrayList<Position>();
				userIdToPosition.put(userId,positionsForThisUser);
			}
			positionsForThisUser.add(p);
		}
		// get all the greek types (e.g delta, gamma, etc) that you'll need
		//   for the MeteorTableModel of GreeksData.
		DerivativeSensitivityTypeInterface[] dseSenseArr = 
				GreeksData.buildDseSenseArray();
		// get greeks for all shortNames
		List<String> problems = new ArrayList<String>();
		Map<String, List<DerivativeReturn[]>> drSenseMap = 
				new HashMap<String, List<DerivativeReturn[]>>();
		for(String shortName : sdMap.keySet()){
			drSenseMap.put(shortName,new ArrayList<DerivativeReturn[]>());
		}
		for(DerivativeSensitivityTypeInterface sense : dseSenseArr){
			Map<String,DerivativeReturn[]> drArrMap = 
					dse.getSensitivity(sense, sdMap.keySet());
			for(String shortName : sdMap.keySet()){
				DerivativeReturn[] drArr = drArrMap.get(shortName);
				drSenseMap.get(shortName).add(drArr);
			}
		}
		
		// do greeks for each user
		for(String userId : userIdToPosition.keySet()){
			List<GreeksData> greeksList = new ArrayList<GreeksData>();
			List<Position> plist = userIdToPosition.get(userId);
			// Populate  gdRet
			for(Position p : plist){
				String shortName = p.getShortName();
				SecDef sd = sdMap.get(shortName);
				double qty = p.getQty().doubleValue();
				Tuple<List<String>,GreeksData> gdTuple = GreeksData.fromDerivativeReturn(
						qty,null, userId,p.getAccount(),p.getStrategy(), sd, errorValueToReturn, 4,drSenseMap.get(shortName));
				problems.addAll(gdTuple.getT1_instance());
				greeksList.add(gdTuple.getT2_instance());
			}
			// send greeks for this userId
			sendGreeksToMeteor(mlsr,greeksList);
		}
	}
	
	private static String[]  sendGreeksToMeteor(
			MeteorListSendReceive<GreeksData> mlsr,
			List<GreeksData> greeksList){
		Map<String, String> mongoSelectors = null;
		List<GreeksData> receivedList = 
				mlsr.getList(mongoSelectors);
		Utils.prtListItems(receivedList);
		List<String> idList = new ArrayList<String>();
		for(GreeksData t : receivedList){
			idList.add(t.get_id());
		}
		
		String[] errors = mlsr.removeListItems(idList);
		if(errors!=null && errors.length>0)
		Utils.prtObMess(RunGreeksToMeteor.class, Arrays.toString(errors));
		
		
		String[] result={};
		try {
			result = mlsr.sendList(greeksList);
		} catch (InterruptedException e) {
			throw Utils.IllState(e);
		}
		Utils.prt(Arrays.toString(result));
		return result;
	}
}
