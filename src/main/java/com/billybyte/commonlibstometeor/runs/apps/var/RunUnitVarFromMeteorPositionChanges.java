package com.billybyte.commonlibstometeor.runs.apps.var;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import com.billybyte.commoncollections.Tuple;
import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commonlibstometeor.Position;
import com.billybyte.commonlibstometeor.UnitVar;
import com.billybyte.commonlibstometeor.runs.ArgBundle;
import com.billybyte.commonlibstometeor.runs.apps.ProcessMeteorPositionChanges;
import com.billybyte.commonstaticmethods.Dates;
import com.billybyte.dse.DerivativeSetEngine;
import com.billybyte.dse.debundles.DerivativeSetEngineBuilder;
import com.billybyte.dse.outputs.DirectionalVarDerSen;
//import com.billybyte.dse.inputs.diotypes.CorrDiot;
import com.billybyte.dse.outputs.PortfolioVarCalculatorDeltaGamma;
//import com.billybyte.dse.queries.DseInputQuery;
import com.billybyte.marketdata.SecDef;
import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.portfolio.PositionData;
import com.billybyte.portfolio.PositionData.TradeTypeEnum;
import com.billybyte.queries.ComplexQueryResult;

public class RunUnitVarFromMeteorPositionChanges {
	private static class ProcessUnitVarFromMeteorPositionChanges extends ProcessMeteorPositionChanges<UnitVar>{
		private final PortfolioVarCalculatorDeltaGamma varCalculator;

		
		public ProcessUnitVarFromMeteorPositionChanges(DerivativeSetEngine dse,
				String meteorUrl, Integer meteorPort, String adminEmail,
				String adminPass, Class<UnitVar> classOfM, double daysPerVar,
				double confidence, double daysPerYear) {
			super(dse, meteorUrl, meteorPort, adminEmail, adminPass, classOfM);
			this.varCalculator = new PortfolioVarCalculatorDeltaGamma(dse);
		}

		

		@Override
		public Map<String, Tuple<List<String>, List<UnitVar>>> processSensitivitiesPerUserId(
				List<Position> positionFromMeteor, Double errorValueToReturn) {
			Map<String,Tuple<List<String>, List<UnitVar>>> ret = new HashMap<String, Tuple<List<String>,List<UnitVar>>>();
			QueryInterface<String, SecDef> sdQuery = this.getDse().getSdQuery();
			Map<String,SecDef> sdMap = new HashMap<String, SecDef>();
			// build various position aggregation maps that will be used to generate DeltaNormal vars for 
			//   various aggregates of userId/account/strategy
			// First the whole position per userId
			Map<String, List<Position>> userIdToPosition = new HashMap<String, List<Position>>();
			// Second Var by userId__account
			Map<String, List<Position>> userIdToAccountToPosition = new HashMap<String, List<Position>>();
			// Second Var by userId__account
			Map<String, List<Position>> userIdToAccountToStrategyToPosition = new HashMap<String, List<Position>>();

			
			// Build maps of userId to position list
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
				
				// build userIdToPosition
				List<Position> positionsForThisUser = 
						userIdToPosition.get(userId);
				if(positionsForThisUser==null){
					positionsForThisUser = new ArrayList<Position>();
					userIdToPosition.put(userId,positionsForThisUser);
				}
				positionsForThisUser.add(p);
				
				// build userIdToAccountToPosition
				String userIdToAccount = userId + "__" + p.getAccount();
				List<Position> positionsForThisUserToAcct = 
						userIdToAccountToPosition.get(userIdToAccount);
				if(positionsForThisUserToAcct==null){
					positionsForThisUserToAcct = new ArrayList<Position>();
					userIdToAccountToPosition.put(userIdToAccount,positionsForThisUserToAcct);
				}
				positionsForThisUserToAcct.add(p);
				
				// build userIdToAccountToStrategyToPosition
				String userIdToAccountToStrategy = userId + "__" + p.getAccount() + "__" + p.getStrategy();
				List<Position> positionsForThisUserIdToAcctToStrategy = 
						userIdToAccountToStrategyToPosition.get(userIdToAccountToStrategy);
				if(positionsForThisUserIdToAcctToStrategy==null){
					positionsForThisUserIdToAcctToStrategy = new ArrayList<Position>();
					userIdToAccountToStrategyToPosition.put(userIdToAccountToStrategy,positionsForThisUserIdToAcctToStrategy);
				}
				positionsForThisUserIdToAcctToStrategy.add(p);
				
			}
			
			// for per userId
			// all vars
			List<UnitVar> allVarList = new ArrayList<UnitVar>(); // create all UnitVars, then sort them by userId
			allVarList.addAll(getVarPerPositionGroup(userIdToPosition));
			allVarList.addAll(getVarPerPositionGroup(userIdToAccountToPosition));
			allVarList.addAll(getVarPerPositionGroup(userIdToAccountToStrategyToPosition));
			
			// now sort all by userId and return to caller
			for(UnitVar uv : allVarList){
				String userId = uv.getUserId();
				if(!ret.containsKey(userId)){
					ret.put(userId, new Tuple<List<String>, List<UnitVar>>(new ArrayList<String>(), new ArrayList<UnitVar>()));
				}
				Tuple<List<String>, List<UnitVar>> tuple = 
						ret.get(userId);
				tuple.getT2_instance().add(uv);
				if(uv.getVar()==null){
					tuple.getT1_instance().add("can't calculateVarFor " + uv.get_id());
				}
			}
			
			
			
			return ret;
		}
		
		private List<UnitVar> getVarPerPositionGroup(Map<String, List<Position>> group){
			List<UnitVar> ret = new ArrayList<UnitVar>();
			for(Entry<String, List<Position>> entry : group.entrySet()){
				ComplexQueryResult<BigDecimal> varCqr = varPerPositionList(entry.getValue());
				String[] userIdAcctStrat = entry.getKey().split("__");
				String userId = userIdAcctStrat[0];
				String account =  "TOTAL";
				String strategy = "";
				String underlying  = ""; // don't total at this level
				if(userIdAcctStrat.length>1){
					account = userIdAcctStrat[1];
				}
				if(userIdAcctStrat.length>2){
					strategy = userIdAcctStrat[2];
				}
				Double var = null;
				if(varCqr.isValidResult()){
					var = varCqr.getResult().doubleValue();
				}else{
					Utils.prtObErrMess(this.getClass(), varCqr.getException().getMessage());
				}
				String _id = userId+"_"+account+"_"+strategy+"_"+underlying;
				UnitVar uv = new UnitVar(_id, userId, account, strategy, underlying, var);
				ret.add(uv);
			}
			return ret;
		}
		
		private ComplexQueryResult<BigDecimal> varPerPositionList(List<Position> pList){
			List<PositionData> pdList = new ArrayList<PositionData>();
			Long yyyyMmDd = Dates.getYyyyMmDdFromCalendar(getDse().getEvaluationDate());
			for(Position p : pList){
				PositionData pd = 
						new PositionData(
								p.getShortName(), p.getAccount(), p.getStrategy(), 
								TradeTypeEnum.POSITION, p.getPrice(), p.getPrice(), 
								yyyyMmDd, p.getQty());
				pdList.add(pd);
			}
			return varCalculator.getVar(new DirectionalVarDerSen(),pdList, 20, TimeUnit.SECONDS);
		}



		@Override
		public List<UnitVar> aggregateMrecs(List<UnitVar> mRecPerPositionList) {
			// TODO - re factor matrix multiply of correlations times aggregated vars here
			//  several steps
			//  1.  put back logic to get dse values into the unit vars in UnitVar.java
			//  2.  comment out the internal implementation of  processSensitivitiesPerUserId in this class
			//  3.  add logic to sum up UnitVars per underlying, create 3 distinct groups like I did in the local processSensitivitiesPerUserId,
			//        and then do matrix multiply to get std per grouping, and 
			//  4.  create a UnitVar aggregate for each grouping from the resultant std * confidence * days
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
		double daysOfVar = 1.0;
		double confidence = .01;
		double tradingDaysPerYear = 252.0;
		ProcessUnitVarFromMeteorPositionChanges mainProcessor = 
				new ProcessUnitVarFromMeteorPositionChanges(
						dse, 
						ab.meteorUrl, ab.meteorPort, ab.adminEmail, 
						ab.adminPass, UnitVar.class,daysOfVar, 
						confidence, tradingDaysPerYear);
		
		// Start the loop that waits for changes in the collection
		//   com.billybyte.commonlibstometeor.Position because of an
		//   insert or delete by the client in that collection, and
		//   then sends a List<GreeksData> to Meteor for that client (userId).
		mainProcessor.process();
		
	}
}
