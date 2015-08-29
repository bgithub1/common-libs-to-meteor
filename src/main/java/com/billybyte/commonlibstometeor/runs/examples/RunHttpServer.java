package com.billybyte.commonlibstometeor.runs.examples;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import com.billybyte.clientserver.httpserver.HttpCsvQueryServer;
import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commoninterfaces.SettlementDataInterface;
import com.billybyte.commonlibstometeor.runs.ArgBundle;
import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.dse.DerivativeSetEngine;
import com.billybyte.dse.debundles.DerivativeSetEngineBuilder;
import com.billybyte.dse.inputs.InBlk;
import com.billybyte.mongo.MongoDatabaseNames;
import com.billybyte.mongo.MongoXml;
import com.billybyte.queries.ComplexQueryResult;

public class RunHttpServer {
	/**
	 * 
	 * runtime params 
mongoPriceVolUr=127.0.0.1 mongoPriceVolPort=27017 mongoSpanlUrl=127.0.0.1 
mongoSpanPort=27022 
mongoXmlSettleDbName=settleDb 
mongoXmlSettleCollName=settleColl  
mongoXmlVolDbName=ImpliedVolDb 
mongoXmlVolCollName=ImpliedVolColl 
		  
		 

	 * @param args
	 */
	public static void main(String[] args) {
		
		// Get all of the usual arguments that pertain to connecting to meteor
		ArgBundle ab = new ArgBundle(args);
		
		// Get the path of a Spring Xml file that will create a DerivativeSetEngine
		
		int httpPort = new Integer(ab.argPairs.get("httpPort"));
		String httpPath = ab.argPairs.get("httpPath");
		
		
		
		// ****************** Create stuff for main csv query (getDseInputs) **********
		//  into instances of GreeksData.
		// you don't really use yahoo to get dse
		
		final DerivativeSetEngine dse = 
				DerivativeSetEngineBuilder.dseForStocksUsingYahooAndSpring(null,ab.dseXmlPath);
	
		
		QueryInterface<String, List<String[]>> csvQuery = 
				new MyGreeksInputQuery(dse);
		HttpCsvQueryServer httpServerQuery = null;
		try {
			httpServerQuery = 
					new HttpCsvQueryServer(httpPort, httpPath, csvQuery,
							10,TimeUnit.SECONDS,"greeks.csv");
		} catch (IOException e) {
			throw Utils.IllState(e);
		}
		
		// ******************** now create stuff fomr MyFutPriceVolQuery *************
		String mongoPricVolUrl = ab.argPairs.get("mongoPriceVolUrl");
		String mongoPriceVolPortString = ab.argPairs.get("mongoPriceVolPort");
		int mongoPriceVolPort = mongoPriceVolPortString == null ? 27017 : new Integer(mongoPriceVolPortString);
		String mongoSpanlUrl = ab.argPairs.get("mongoSpanlUrl");
		String mongoSpanPortString = ab.argPairs.get("mongoSpanPort");
		int mongoSpanPort = mongoPriceVolPortString == null ? 27017 : new Integer(mongoSpanPortString);

		// mongoXmlSettle
		String mongoXmlSettleDbName = ab.argPairs.get("mongoXmlSettleDbName");
		if(mongoXmlSettleDbName==null){
			mongoXmlSettleDbName = MongoDatabaseNames.SETTLEMENT_DB;
		}
		
		String mongoXmlSettleCollName = ab.argPairs.get("mongoXmlSettleCollName");
		if(mongoXmlSettleCollName==null){
			mongoXmlSettleCollName = MongoDatabaseNames.SETTLEMENT_CL;
		}
		MongoXml<SettlementDataInterface> mongoXmlSettle = 
				new MongoXml<SettlementDataInterface>(mongoPricVolUrl, mongoPriceVolPort, mongoXmlSettleDbName, mongoXmlSettleCollName);
		// end mongoXmlSettle

		// mongoXmlVol
		String mongoXmlVolDbName = ab.argPairs.get("mongoXmlVolDbName");
		if(mongoXmlVolDbName==null){
			mongoXmlVolDbName = MongoDatabaseNames.IMPLIEDVOL_DB;
		}
		
		String mongoXmlVolCollName = ab.argPairs.get("mongoXmlVolCollName");
		if(mongoXmlVolCollName==null){
			mongoXmlVolCollName = MongoDatabaseNames.IMPLIEDVOL_CL;
		}
		MongoXml<BigDecimal> mongoXmlVol = 
				new MongoXml<BigDecimal>(mongoPricVolUrl, mongoPriceVolPort, mongoXmlVolDbName, mongoXmlVolCollName);
		// end mongoXmlSettle
		httpServerQuery.addAlternativePath("/futpricevol", new MyFutPriceVolQuery(mongoXmlSettle,mongoXmlVol));
		
		httpServerQuery.start();
	}
	
	static class MyGreeksInputQuery implements QueryInterface<String, List<String[]>> {
		private final DerivativeSetEngine dse;
		private MyGreeksInputQuery(DerivativeSetEngine dse){
			this.dse = dse;
		}
		@Override
		public List<String[]> get(String qparams, int timeoutValue,
				TimeUnit timeUnitType) {
			
			// get type of request, which is in first param
			// the qparams string has the http parameters.
			//   
    		String[] matches = qparams.split("&");
    		Set<String> derivativeShortNameSet = new HashSet<String>();
    		for(String match: matches){
    			String[] pair = match.split("=");
    			derivativeShortNameSet.add(pair[1]);
    		}

    		Map<String, ComplexQueryResult<InBlk>> inputBlkCqrMap = 
					dse.getInputs(derivativeShortNameSet);
			List<String[]> inputCsvList =
					InBlk.getCsvListFromInBlkMap(dse, inputBlkCqrMap, 4);
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
    		
			return outputCsv;
		}
		
	}
	
	
	static class MyFutPriceVolQuery implements QueryInterface<String, List<String[]>> {
		private final MongoXml<SettlementDataInterface> mongoXmlSettle;
		MongoXml<BigDecimal> mongoXmlVol;
		private MyFutPriceVolQuery(MongoXml<SettlementDataInterface> mongoXmlSettle,
				MongoXml<BigDecimal> mongoXmlVol){
			this.mongoXmlSettle = mongoXmlSettle;
			this.mongoXmlVol = mongoXmlVol;
		}
		@Override
		public List<String[]> get(String qparams, int timeoutValue,
				TimeUnit timeUnitType) {
			
			// get type of request, which is in first param
			// the qparams string has the http parameters.
			//   
    		String[] matches = qparams.split("&");
    		Set<String> regexDerivNames = new TreeSet<String>();
    		for(String match: matches){
    			String[] pair = match.split("=");
    			regexDerivNames.add(pair[1]);
    		}
			List<String[]> outputCsv = new ArrayList<String[]>();
			String[] header = {"shortName","settle","settleVol"};
			outputCsv.add(header);
			Map<String, String[]> outputMap = 
					new TreeMap<String, String[]>();
			
			for(String regexName : regexDerivNames){
    			Map<String, SettlementDataInterface> settles = 
    					mongoXmlSettle.getByRegex(regexName);
    			Map<String, BigDecimal> vols = 
    					mongoXmlVol.getByRegex(regexName);
    			for(Entry<String, SettlementDataInterface> entry : settles.entrySet()){
    				String sn = entry.getKey();
    				if(!outputMap.containsKey(sn)){
    					outputMap.put(sn, new String[]{sn,"",""});
    				}
    				SettlementDataInterface settle = 
    						entry.getValue();
    				if(settle!=null && settle.getPrice()!=null){
    					String[] output = outputMap.get(sn);
    					output[1] = settle.getPrice().toString();
    				}
    			}
    			for(Entry<String, BigDecimal> entry : vols.entrySet()){
    				String sn = entry.getKey();
    				BigDecimal vol = entry.getValue();
    				if(vol!=null){
    					String[] output = outputMap.get(sn);
    					output[2] = vol.toString();
    				}
    			}
				
			}
			
			for(String sn : outputMap.keySet()){
				outputCsv.add(outputMap.get(sn));
			}
    		
			return outputCsv;
		}
		
	}

}
