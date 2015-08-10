package com.billybyte.commonlibstometeor.runs.apps;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import com.billybyte.commoncollections.Tuple;
import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commonlibstometeor.Position;
import com.billybyte.commonlibstometeor.PositionBaseItem;
import com.billybyte.dse.DerivativeSetEngine;
import com.billybyte.dse.outputs.DerivativeReturn;
import com.billybyte.dse.outputs.DerivativeSensitivityTypeInterface;
import com.billybyte.marketdata.SecDef;
import com.billybyte.marketdata.futures.UnderlyingShortNameFromOptionShortNameQuery;
import com.billybyte.meteorjava.MeteorListCallback;
import com.billybyte.meteorjava.MeteorListSendReceive;
import com.billybyte.meteorjava.TableChangedByUser;
import com.billybyte.commonstaticmethods.CollectionsStaticMethods;
import com.billybyte.commonstaticmethods.Utils;

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
public abstract  class ProcessMeteorPositionChanges<M extends PositionBaseItem> {
	
	public abstract List<M> aggregateMrecs(List<M> mRecPerPositionList);

	private final DerivativeSetEngine dse;
	private final String meteorUrl;
	private final Integer meteorPort;
	private final String adminEmail;
	private final String adminPass;
	private final Class<M> classOfM;
	protected final UnderlyingShortNameFromOptionShortNameQuery underlingQuery ;
	private final MeteorListSendReceive<TableChangedByUser> mlsrOfTableChangedByUser;
	private final MeteorListSendReceive<M> mlsrOfM;
	private final MeteorListSendReceive<Position> mlsrForCollectionRead;
	
	
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
			String adminPass, 
			final Class<M> classOfM) {
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
		this.mlsrForCollectionRead = 
				new MeteorListSendReceive<Position>(mlsrOfTableChangedByUser,Position.class);

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



	
	
	public MeteorListSendReceive<TableChangedByUser> getMlsrOfTableChangedByUser() {
		return mlsrOfTableChangedByUser;
	}




	public MeteorListSendReceive<M> getMlsrOfM() {
		return mlsrOfM;
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
	public void process(){
		// create a Callback to capture TableChangedByUser changes
		MeteorListCallback<TableChangedByUser> tableChangedByUserCallback = 
				new MeteorListCallback<TableChangedByUser>() {
					@Override
					public void onMessage(String messageType, String id,TableChangedByUser convertedMessage) {
						Utils.prtObMess(this.getClass(), "TableChangedByUser callback: "+messageType);
						Utils.prtObMess(this.getClass(), "recId: "+id+", record: " + (convertedMessage!=null ? convertedMessage.toString(): "null message"));
						if(messageType.compareTo("added")==0 || messageType.compareTo("changed")==0){
							String[] userIdAndCollection = id.split("_");
							String userId = userIdAndCollection[0];
							String collection = userIdAndCollection[1];
							if(collection==null)return;
							boolean check = collection.compareTo(Position.class.getCanonicalName())==0 || collection.compareTo(classOfM.getCanonicalName())==0;
							if(!check){
								return;
							}
							
							// Now get the List<?> of records of type clazz from Meteor that
							//  has been changed by a Meteor client.
							Map<String, String> selector = new HashMap<String, String>();
							selector.put("userId", userId);
							// Do the get here.
							List<Position> positionList = 
									mlsrForCollectionRead.getList(selector);
							// Put the list on the blocking queue that we originally sent back to
							//  the caller of subscribeToTableChangedByUser when we first started above (outside of this runnable).
							//  The caller should be waiting on this blockingqueue (take) for these List<?> items.
							Double errorValueToReturn = -11111111.0;
							try {
								// Create records of type M and send them to Meteor clients that had a position change.
								processMrecs(positionList, errorValueToReturn);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
		};
		// create a BlockingQueue<List<Position>>
		mlsrOfTableChangedByUser.subscribeToListDataWithCallback(tableChangedByUserCallback);
		final BlockingQueue<List<Position>> ret = new ArrayBlockingQueue<List<Position>>(100);
		Runnable r  = new ProcessBlockingQueueRunnable2(ret);
		// startup the blockingqueue taker
		new Thread(r).start();

		// subscribe to the callback

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
	private class ProcessBlockingQueueRunnable2 implements Runnable{
		private final BlockingQueue<List<Position>> blockingQueue;
		private ProcessBlockingQueueRunnable2(
				BlockingQueue<List<Position>> blockingQueue
				) {
			super();
			this.blockingQueue = blockingQueue;
			
		}



		@Override
		public void run() {
			boolean keepGoing = true;
			// Loop until there's an error.  Remember that the loop is done on another thread.
			while(keepGoing){
				try {
					List<Position> positionList = blockingQueue.take();
					// Make sure you got data.
					if(positionList.size()<1){
						continue;
					}
					// Get the class of the data.
						Double errorValueToReturn = -11111111.0;
						// Create records of type M and send them to Meteor clients that had a position change.
						processMrecs(positionList,  errorValueToReturn);
				}catch (InterruptedException e) {						
					e.printStackTrace();
					keepGoing=false;
				}
			}
			
		}
		
		
	}
		
	
	public void processMrecs(
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
			List<M> mListAggregated = aggregateMrecs(mList);
			// send PositionBaseItem extended Objects for this userId
//			sendMrecsToMeteor(userId,mlsrOfM,mList);
			sendMrecsToMeteor(userId,mlsrOfM,mListAggregated);
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
		CollectionsStaticMethods.prtListItems(receivedList);
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
	public Map<String,Tuple<List<String>, List<M>>> processSensitivitiesPerUserId(
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
				List<SecDef> underSds = dse.getQueryManager().getUnderlyingSecDefs(shortName, 1, TimeUnit.SECONDS);
				String under = underlingQuery.get(shortName, 1, TimeUnit.SECONDS);
				String under2 = underSds.get(0).getShortName();
				if(under == null || under2.compareTo(under)!=0){
					Utils.prtObErrMess(this.getClass(), "underlying form dse : " + under2 + " does not match under from UnderlyingShortNameFromOptionShortNameQuery : " + under);
				}
				
//				// only pass symbol
//				if(under==null){
//					under="";
//				}
//				
//				under = under.split("\\"+MarketDataComLib.DEFAULT_SHORTNAME_SEPARATOR)[0];
//				Tuple<List<String>,M> gdTuple = newby.positionBasedItemFromDerivativeReturn(p, sd, drSenseMap,underSds);
				Tuple<List<String>,List<M>> gdTuple = newby.positionBasedItemFromDerivativeReturn(p, sd, drSenseMap,underSds);
				problems.addAll(gdTuple.getT1_instance());
//				senseList.add(gdTuple.getT2_instance());
				senseList.addAll(gdTuple.getT2_instance());
			}
			// send Sensitivities for this userId
			Tuple<List<String> , List<M>> newTuple = 
					new Tuple<List<String>, List<M>>(problems,senseList);
			ret.put(userId, newTuple);
		}
		return ret;
	}
	
	
}
