package com.billybyte.commonlibstometeor.runs.apps;

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

import com.billybyte.commonlibstometeor.Position;
import com.billybyte.commonlibstometeor.PositionBaseItem;
import com.billybyte.dse.DerivativeSetEngine;
import com.billybyte.dse.outputs.DerivativeReturn;
import com.billybyte.dse.outputs.DerivativeSensitivityTypeInterface;

import com.billybyte.marketdata.MarketDataComLib;
import com.billybyte.marketdata.SecDef;
import com.billybyte.marketdata.futures.UnderlyingShortNameFromOptionShortNameQuery;
import com.billybyte.meteorjava.MeteorListSendReceive;
import com.billybyte.meteorjava.TableChangedByUser;
import com.billybyte.meteorjava.staticmethods.Utils;

/**
 * 	Start a loop that waits for changes in the collection
 * 	 com.billybyte.commonlibstometeor.Position because of an
 *   insert or delete by the client in that collection, and
 *   then sends a List<M> to Meteor for that client (userId).
 *  M is a class that extends MeteorBaseListItem.
 *  
 * @author bperlman1
 *
 */
public  class ProcessMeteorPositionChanges<M extends PositionBaseItem> {
	private final DerivativeSetEngine dse;
	private final String meteorUrl;
	private final Integer meteorPort;
	private final String adminEmail;
	private final String adminPass;
	private final Class<M> classOfM;
	private final UnderlyingShortNameFromOptionShortNameQuery underlingQuery ;
	private final MeteorListSendReceive<TableChangedByUser> mlsrOfTableChangedByUser;
	private final MeteorListSendReceive<M> mlsrOfM;

	
	
	/**
	 * 
	 * @param dse
	 * @param meteorUrl
	 * @param meteorPort
	 * @param adminEmail
	 * @param adminPass
	 * @param classOfM
	 */
	public ProcessMeteorPositionChanges(DerivativeSetEngine dse,
			String meteorUrl, Integer meteorPort, String adminEmail,
			String adminPass, Class<M> classOfM) {
		super();
		this.dse = dse;
		this.meteorUrl = meteorUrl;
		this.meteorPort = meteorPort;
		this.adminEmail = adminEmail;
		this.adminPass = adminPass;
		this.classOfM = classOfM;
		this.underlingQuery = new UnderlyingShortNameFromOptionShortNameQuery(dse.getSdQuery(), dse.getEvaluationDate());
		try {
			this.mlsrOfTableChangedByUser = new MeteorListSendReceive<TableChangedByUser>(100, 
							TableChangedByUser.class, 
							meteorUrl, meteorPort, 
							adminEmail,adminPass,"", "", "tester");
			
		} catch (URISyntaxException e) {
			throw Utils.IllState(e);
		}
		this.mlsrOfM = 
				new MeteorListSendReceive<M>(mlsrOfTableChangedByUser, classOfM);		
	
	}
	
	

	public DerivativeSetEngine getDse() {
		return dse;
	}



	public String getMeteorUrl() {
		return meteorUrl;
	}



	public Integer getMeteorPort() {
		return meteorPort;
	}



	public String getAdminEmail() {
		return adminEmail;
	}



	public String getAdminPass() {
		return adminPass;
	}



	public Class<M> getClassOfM() {
		return classOfM;
	}



	/**
	 * Create a MeteorSendReceive<TableChangedByUser> instance to connect to Meteor,
	 *   and subscribe to changes in TableChangedByUser records from Meteor.
	 *   When an "added" change occurs, the MeteorSendReceive<TableChangedByUser> instance  
	 *   will fetch List<Position> from Meteor and put that List<Position> on the blocking queue
	 *   that the run method in ProcessBlockingQueueRunnable is waiting on.  The run method will call
	 *   processMrecs so that  MeteorSendReceive<M> instance to send a List<M> to Meteor of things
	 *   like greeks, p&L, unit VaR, etc.
	 */
	public  void process() {
		
		
		// Create a MeteorListSendReceive instance to control communication with Meteor
		//  You'll reuse this connection object to get other kinds of objects besides TableChangedByUser objects
//		MeteorListSendReceive<TableChangedByUser> mlsrOfTableChangedByUser = null;
//		try {
//			mlsrOfTableChangedByUser = new MeteorListSendReceive<TableChangedByUser>(100, 
//							TableChangedByUser.class, 
//							meteorUrl, meteorPort, 
//							adminEmail,adminPass,"", "", "tester");
//			
//		} catch (URISyntaxException e) {
//			throw Utils.IllState(e);
//		}
		
//		// Create a sub-mlsr to use to get and put Lists of java instances that extend PositionBaseItem, by reusing the mlsr
//		//   for TableChangedByUser changes.
//		final MeteorListSendReceive<M> mlsrOfM = 
//				new MeteorListSendReceive<M>(mlsrOfTableChangedByUser, classOfM);
		// Only accept changes for the Position.class collection. 
		Set<String> collectionsToWatchFor = 
				new HashSet<String>(Arrays.asList(new String[]{Position.class.getCanonicalName()}));
		// Get a blocking queue from the mlsr that you can use in the new Runnable and new Thread that
		//  you create below.  This method will also subscribe to changes in the TableChangeByUser collection
		//  so that you can track changes in any collection on the client side, when the client adds or deletes
		//  records.
		final BlockingQueue<List<?>> blockingQueue = 
				mlsrOfTableChangedByUser.subscribeToTableChangedByUser(-1,collectionsToWatchFor);
		// Create a new anonymous Runnable instance that responds to List<Position> that the mlsr puts
		//   on blockingQueue
//		Runnable r = new ProcessBlockingQueueRunnable(blockingQueue, mlsrOfM);
		Runnable r = new ProcessBlockingQueueRunnable(blockingQueue);
		
		// startup the blockingqueue taker
		new Thread(r).run();
		
	}
	
	/**
	 * ProcessBlockingQueueRunnable will loop in it's run method, waiting on
	 *   List<Position> lists to appear on its BlockingQueue<List<?>>.
	 *   It will do some initial checking of the list, and then call the
	 *   method processMrecs to process all of the Position records that it
	 *   receives from the blockingQueue.  It is possible to receive
	 *   other types of records besides Position records from the blocking queue,
	 *   so, it is necessary to make the blocking queue of type List<?>  
	 * @author bperlman1
	 *
	 */
	private class ProcessBlockingQueueRunnable implements Runnable{
		private final BlockingQueue<List<?>> blockingQueue;
//		private final MeteorListSendReceive<M> mlsrMrecData;
		private ProcessBlockingQueueRunnable(
				BlockingQueue<List<?>> blockingQueue
//				MeteorListSendReceive<M> mlsrMrecData
				) {
			super();
			this.blockingQueue = blockingQueue;
//			this.mlsrMrecData = mlsrMrecData;
			
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
						// Create records of type M and send them to Meteor clients that had a position change.
//						processMrecs(mlsrMrecData,positionList,  errorValueToReturn);
						processMrecs(positionList,  errorValueToReturn);
					}
				} catch (InterruptedException e) {						
					e.printStackTrace();
					keepGoing=false;
				}
			}
			
		}
		
//		/**
//		 * Process the List<M> that you fetch from Meteor, for specific userId's.
//		 * @param mlsr
//		 * @param positionFromMeteor
//		 * @param errorValueToReturn
//		 */
//		public void processMrecs(
//				MeteorListSendReceive<M> mlsr,
//				List<Position> positionFromMeteor,
//				Double errorValueToReturn
//				){
//			
//			QueryInterface<String, SecDef> sdQuery = dse.getSdQuery();
//			Map<String,SecDef> sdMap = new HashMap<String, SecDef>();
//			Map<String, List<Position>> userIdToPosition = new HashMap<String, List<Position>>();
//			// Build map of userId to position list
//			for(Position p : positionFromMeteor){
//				String userId = p.getUserId();
//				String sn = p.getShortName();
//				if(!sdMap.containsKey(sn)){
//					SecDef sd = sdQuery.get(sn, 1, TimeUnit.SECONDS);
//					if(sd==null){
//						Utils.prtObErrMess(ProcessMeteorPositionChanges.class, "cannot get SecDef for shortName: " + sn);
//					}else{
//						sdMap.put(sn, sd);
//					}
//				}
//				
//				List<Position> positionsForThisUser = 
//						userIdToPosition.get(userId);
//				if(positionsForThisUser==null){
//					positionsForThisUser = new ArrayList<Position>();
//					userIdToPosition.put(userId,positionsForThisUser);
//				}
//				positionsForThisUser.add(p);
//			}
//			//processSensitivitiesPerUserId
//			Map<String,Tuple<List<String>, List<M>>> userIdToMrecs = 
//					processSensitivitiesPerUserId(positionFromMeteor, errorValueToReturn);
//			
//			// Create an instance of type M for each Position object for each user
//			for(String userId : userIdToMrecs.keySet()){
//				List<M> mList = userIdToMrecs.get(userId).getT2_instance();
//				List<String> problems = userIdToMrecs.get(userId).getT1_instance();
//				// send PositionBaseItem extended Objects for this userId
//				sendMrecsToMeteor(mlsr,mList);
//				// print out problems
//				for(String problem : problems){
//					Utils.prtObErrMess(ProcessMeteorPositionChanges.class, problem);	
//				}
//				
//			}
//		}
		
	}
	
	
	public void processMrecs(
//			MeteorListSendReceive<M> mlsr,
			List<Position> positionFromMeteor,
			Double errorValueToReturn
			){
		
		QueryInterface<String, SecDef> sdQuery = dse.getSdQuery();
		Map<String,SecDef> sdMap = new HashMap<String, SecDef>();
		Map<String, List<Position>> userIdToPosition = new HashMap<String, List<Position>>();
		// Build map of userId to position list
		for(Position p : positionFromMeteor){
			String userId = p.getUserId();
			String sn = p.getShortName();
			if(!sdMap.containsKey(sn)){
				SecDef sd = sdQuery.get(sn, 1, TimeUnit.SECONDS);
				if(sd==null){
					Utils.prtObErrMess(ProcessMeteorPositionChanges.class, "cannot get SecDef for shortName: " + sn);
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
		//processSensitivitiesPerUserId
		Map<String,Tuple<List<String>, List<M>>> userIdToMrecs = 
				processSensitivitiesPerUserId(positionFromMeteor, errorValueToReturn);
		
		// Create an instance of type M for each Position object for each user
		for(String userId : userIdToMrecs.keySet()){
			List<M> mList = userIdToMrecs.get(userId).getT2_instance();
			List<String> problems = userIdToMrecs.get(userId).getT1_instance();
			// send PositionBaseItem extended Objects for this userId
//			sendMrecsToMeteor(mlsr,mList);
			sendMrecsToMeteor(userId,mlsrOfM,mList);
			// print out problems
			for(String problem : problems){
				Utils.prtObErrMess(ProcessMeteorPositionChanges.class, problem);	
			}
			
		}
	}

	
	private  String[]  sendMrecsToMeteor(
			String userId,
			MeteorListSendReceive<M> mlsr,
			List<M> mList){
		Map<String, String> mongoSelectors = new HashMap<String, String>();
		mongoSelectors.put("userId",userId);
		List<M> receivedList = 
				mlsr.getList(mongoSelectors);
		Utils.prtListItems(receivedList);
		List<String> idList = new ArrayList<String>();
		for(M t : receivedList){
			if(t.getUserId().compareTo(userId)==0){
				idList.add(t.get_id());
			}
		}
		
		String[] errors = mlsr.removeListItems(idList);
		if(errors!=null && errors.length>0)
		Utils.prtObMess(ProcessMeteorPositionChanges.class, Arrays.toString(errors));
		
		
		String[] result={};
		try {
			result = mlsr.sendList(mList);
		} catch (InterruptedException e) {
			throw Utils.IllState(e);
		}
		Utils.prt(Arrays.toString(result));
		return result;
	}
	
	/**
	 * Create a Map per userId of Tuples where each tuple has a List<M> 
	 *   that comes from running DerivativeSetEngine and a List<String> that comes
	 *   from accumulating all of the error returns from DerivativeSetEngine.
	 *    
	 * @param positionFromMeteor List<Position>
	 * @param dse
	 * @param sdQuery
	 * @param errorValueToReturn - a value to use for each sensitivity that can't be processed
	 * @return Map<String,Tuple<List<String>, List<M>>> 
	 */
	private Map<String,Tuple<List<String>, List<M>>> processSensitivitiesPerUserId(
			List<Position> positionFromMeteor,
			Double errorValueToReturn){
		
		Map<String,Tuple<List<String>, List<M>>> ret = new HashMap<String, Tuple<List<String>,List<M>>>();
		QueryInterface<String, SecDef> sdQuery = dse.getSdQuery();
		Map<String,SecDef> sdMap = new HashMap<String, SecDef>();
		Map<String, List<Position>> userIdToPosition = new HashMap<String, List<Position>>();
		// Build map of userId to position list
		for(Position p : positionFromMeteor){
			String userId = p.getUserId();
			String sn = p.getShortName();
			if(!sdMap.containsKey(sn)){
				SecDef sd = sdQuery.get(sn, 1, TimeUnit.SECONDS);
				if(sd==null){
					Utils.prtObErrMess(ProcessMeteorPositionChanges.class, "cannot get SecDef for shortName: " + sn);
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
		// get all the sensitivity types (e.g delta, gamma, etc) that you'll need
		//   for the MeteorTableModel for Class that extends PositionBaseItem.java.
		M newby = PositionBaseItem.newInstance(classOfM);
		DerivativeSensitivityTypeInterface[] dseSenseArr = 
				newby.getDseSenseArray();
		// get Sensitivities for all shortNames
		Map<String, Map<DerivativeSensitivityTypeInterface,DerivativeReturn[]>> shortNameToDrSenseMap = 
				new HashMap<String, Map<DerivativeSensitivityTypeInterface,DerivativeReturn[]>>();
		for(String shortName : sdMap.keySet()){
			shortNameToDrSenseMap.put(shortName,new HashMap<DerivativeSensitivityTypeInterface,DerivativeReturn[]>());
		}
		for(DerivativeSensitivityTypeInterface sense : dseSenseArr){
			Map<String,DerivativeReturn[]> drArrMap = 
					dse.getSensitivity(sense, sdMap.keySet());
			for(String shortName : sdMap.keySet()){
				DerivativeReturn[] drArr = drArrMap.get(shortName);
				shortNameToDrSenseMap.get(shortName).put(sense,drArr);
			}
		}
		
		// do sensitivity for each user
		for(String userId : userIdToPosition.keySet()){
			List<String> problems = new ArrayList<String>();
			List<M> senseList = new ArrayList<M>();
			List<Position> plist = userIdToPosition.get(userId);
			// Populate  gdRet
			for(Position p : plist){
				String shortName = p.getShortName();
				SecDef sd = sdMap.get(shortName);
				Map<DerivativeSensitivityTypeInterface,DerivativeReturn[]> drSenseMap = 
						shortNameToDrSenseMap.get(shortName);
				
				// !!!!!!!!!!!!!!!!!!!! IMPORTANT - HERE IS THE MAIN CALL THAT CREATES INSTANCES OF TYPE <M> !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
				// Get underlying
				String under = underlingQuery.get(shortName, 1, TimeUnit.SECONDS);
				// only pass symbol
				if(under==null){
					under="";
				}
				
				under = under.split("\\"+MarketDataComLib.DEFAULT_SHORTNAME_SEPARATOR)[0];
				Tuple<List<String>,M> gdTuple = newby.positionBasedItemFromDerivativeReturn(p, sd, drSenseMap,under);
				problems.addAll(gdTuple.getT1_instance());
				senseList.add(gdTuple.getT2_instance());
			}
			// send Sensitivities for this userId
			Tuple<List<String> , List<M>> newTuple = 
					new Tuple<List<String>, List<M>>(problems,senseList);
			ret.put(userId, newTuple);
		}
		return ret;
	}
	
}
