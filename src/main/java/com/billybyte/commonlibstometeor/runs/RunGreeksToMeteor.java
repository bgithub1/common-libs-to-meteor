package com.billybyte.commonlibstometeor.runs;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.billybyte.commoncollections.Tuple;
import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commonlibstometeor.GreeksData;
import com.billybyte.dse.DerivativeSetEngine;
import com.billybyte.dse.debundles.DerivativeSetEngineBuilder;
import com.billybyte.dse.outputs.DerivativeReturn;
import com.billybyte.dse.outputs.DerivativeSensitivityTypeInterface;
import com.billybyte.marketdata.SecDef;
import com.billybyte.marketdata.SecDefQueryAllMarkets;
import com.billybyte.meteorjava.MeteorListSendReceive;
import com.billybyte.meteorjava.MeteorTableModel;
import com.billybyte.meteorjava.MeteorValidator;
import com.billybyte.meteorjava.staticmethods.Utils;

public class RunGreeksToMeteor {
	private static final QueryInterface<String, SecDef> sdQuery = 
			new SecDefQueryAllMarkets();
	
	public static void main(String[] args) {
		ArgBundle ab = new ArgBundle(args);
		String dseXmlPath = ab.argPairs.get("dseXmlPath");
		// if dseXmlPath is null, then build a default engine for
		//   stocks from Yahoo only
		DerivativeSetEngine dse = buildDse(dseXmlPath);
		
		Double badRet = Double.NEGATIVE_INFINITY;
		Tuple<List<String>,List<GreeksData>> greeksTuple = 
				getGreeks(ab.userId,
						ab.account, 
						ab.strategy,
						dse, 
						getShortNameSet(), badRet);
		List<String> problems = greeksTuple.getT1_instance();
		for(String problem : problems){
			Utils.prtObErrMess(RunGreeksToMeteor.class, problem);
		}
		
		
		// first resend table models
		sendMeteorTableModels(ab.meteorUrl, 
				ab.meteorPort, 
				ab.adminEmail, 
				ab.adminPass);

		// got stuff, make a connection to Meteor and send greeks
		MeteorListSendReceive<GreeksData> mlsr=null;
		try {
			mlsr = new MeteorListSendReceive<GreeksData>(100, 
					GreeksData.class, ab.meteorUrl, ab.meteorPort, 
					ab.adminEmail,ab.adminPass,"", "", "tester");
		} catch (URISyntaxException e) {
			throw Utils.IllState(e);
		}
		


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
			result = mlsr.sendList(greeksTuple.getT2_instance());
		} catch (InterruptedException e) {
			throw Utils.IllState(e);
		}
		Utils.prt(Arrays.toString(result));

		
		
		mlsr.disconnect();

		
	}
	
	private static final Set<String> getShortNameSet(){
		Set<String> ret = new HashSet<String>();
		for(String shortName : new String[]{
				"IBM.OPT.SMART.USD.20170120.C.170.00",
				"IBM.OPT.SMART.USD.20170120.P.170.00",
				"MSFT.OPT.SMART.USD.20170120.C.45.00",
				"MSFT.OPT.SMART.USD.20170120.P.45.00",
				"AAPL.OPT.SMART.USD.20170120.C.140.00",
				"AAPL.OPT.SMART.USD.20170120.P.140.00",
				"GOOG.OPT.SMART.USD.20170120.C.550.00",
				"GOOG.OPT.SMART.USD.20170120.P.550.00",
				}){
			ret.add(shortName);
		}
		return ret;
	}
	
	private static final MeteorTableModel buildGreeksTableModel(){
		MeteorTableModel tableModel = 
				new MeteorTableModel(
						GreeksData.class,"Greeks",GreeksData.class.getName(), 
						GreeksData.buildMetColModelArray());
		return tableModel;
	}
	
	private static final DerivativeSetEngine buildDse(String dseXmlPath){
		if(dseXmlPath==null){
			return DerivativeSetEngineBuilder.dseForStocksUsingYahoo();
		}else{
			return com.billybyte.commonstaticmethods.Utils.springGetBean(
					DerivativeSetEngine.class, 
					dseXmlPath, 
					"dse");

//			return DerivativeSetEngineBuilder.dseForStocksUsingYahooAndSpring(null,dseXmlPath);
		}
	}
	
	/**
	 * All of the real work getting greeks is done here
	 * @param dse
	 * @param shortNameSet
	 * @param errorValueToReturn Double - what to return if you can't compute a greek
	 * @return Tuple<List<String>,Map<String,Map<DerivativeSensitivityTypeInterface,Double>>> tuple which has 
	 * 			a map of shortName to an inner map of  DerivativeSensitivityTypeInterface to Double (greek values)
	 * For any problems, the greek value with be errorValueToReturn.
	 */
	public static final Tuple<List<String>,List<GreeksData>> getGreeks(
			String userId,
			String account,String strategy,
			DerivativeSetEngine dse,
			Set<String> shortNameSet,
			Double errorValueToReturn){
		List<GreeksData> gdRet = new ArrayList<GreeksData>();
		// get all the greek types (e.g delta, gamma, etc) that you'll need
		//   for the MeteorTableModel of GreeksData.
		DerivativeSensitivityTypeInterface[] dseSenseArr = 
				GreeksData.buildDseSenseArray();
		
		List<String> problems = new ArrayList<String>();
		Map<String, List<DerivativeReturn[]>> drSenseMap = 
				new HashMap<String, List<DerivativeReturn[]>>();
		for(String shortName : shortNameSet){
			drSenseMap.put(shortName,new ArrayList<DerivativeReturn[]>());
		}
		for(DerivativeSensitivityTypeInterface sense : dseSenseArr){
			Map<String,DerivativeReturn[]> drArrMap = 
					dse.getSensitivity(sense, shortNameSet);
			for(String shortName : shortNameSet){
				DerivativeReturn[] drArr = drArrMap.get(shortName);
				drSenseMap.get(shortName).add(drArr);
			}
		}
		for(String shortName : shortNameSet){
			SecDef sd = sdQuery.get(shortName, 1, TimeUnit.SECONDS);

			Tuple<List<String>,GreeksData> gdTuple = GreeksData.fromDerivativeReturn(
					1.0,null, userId,account,strategy, sd, 
					errorValueToReturn, 4,drSenseMap.get(shortName));
			problems.addAll(gdTuple.getT1_instance());
			gdRet.add(gdTuple.getT2_instance());
		}

		return new Tuple<List<String>, List<GreeksData>>(problems, gdRet);
	}
	
	private static final void sendMeteorTableModels(
			String meteorUrl,
			Integer meteorPort,
			String adminEmail,
			String adminPass){
		MeteorTableModel tm = buildGreeksTableModel();

		List<MeteorTableModel> mtmList = 
				new ArrayList<MeteorTableModel>();
		mtmList.add(tm);

		MeteorListSendReceive<MeteorTableModel> mlsrTableModel=null;
		try {
			mlsrTableModel = new MeteorListSendReceive<MeteorTableModel>(100, 
					MeteorTableModel.class, meteorUrl, meteorPort, 
					adminEmail,adminPass,"", "", "tester");
		} catch (URISyntaxException e) {
			throw Utils.IllState(e);
		}
		// the mtmList only has one object, but I put in an iteration
		 //  for future use.
		for(MeteorTableModel mtm:mtmList){
			String tableId = mtm.getId();
			String error = mlsrTableModel.deleteMeteorTableModel( tableId);
			if(error!=null && error.compareTo("0")!=0){
				Utils.prtObErrMess(RunGreeksToMeteor.class, error);
			}
		}
		try {
			mlsrTableModel.sendTableModelList(mtmList);
		} catch (InterruptedException e) {
			throw Utils.IllState(e);
		}
		
		// send validator
		MeteorValidator greeksDataValidator = 
				GreeksData.buildValidator();
		greeksDataValidator.sendValidator(meteorUrl, meteorPort, adminEmail, adminPass);

	}
}
