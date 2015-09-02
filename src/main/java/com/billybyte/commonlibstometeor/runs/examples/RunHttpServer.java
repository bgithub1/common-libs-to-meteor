package com.billybyte.commonlibstometeor.runs.examples;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
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
import com.billybyte.marketdata.MarketDataComLib;
import com.billybyte.marketdata.SecDef;
import com.billybyte.marketdata.SecEnums.SecCurrency;
import com.billybyte.marketdata.SecEnums.SecExchange;
import com.billybyte.marketdata.SecEnums.SecSymbolType;
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
		httpServerQuery.addAlternativePath("/shortnames", new MyShortNameQuery(mongoXmlVol));
		//MySpotSettleVolQuery
		httpServerQuery.addAlternativePath("/spotfutpricevol", new MySpotSettleVolQuery(mongoXmlSettle,mongoXmlVol,dse.getSdQuery(),Calendar.getInstance()));
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
		private final MongoXml<BigDecimal> mongoXmlVol;
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
        				if(!outputMap.containsKey(sn)){
        					outputMap.put(sn, new String[]{sn,"",""});
        				}
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
	
	static class MyShortNameQuery  implements QueryInterface<String, List<String[]>> {
		private final MongoXml<BigDecimal> mongoXmlVol;

		private MyShortNameQuery(
				MongoXml<BigDecimal> mongoXmlVol){
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
			Set<String> outputSet = 
					new TreeSet<String>();
			for(String regexName : regexDerivNames){
    			Map<String, BigDecimal> vols = 
    					mongoXmlVol.getByRegex(regexName);
    			for(String sn:vols.keySet()){
    				outputSet.add(sn);
    			}
			}
			outputCsv.add(new String[]{bigAppend(outputSet)});
			return outputCsv;
		}
		
		private String bigAppend(Set<String> stringSet){
			
			StringBuffer sb = new StringBuffer(40*stringSet.size());
			for(String s : stringSet){
				sb.append(s+"\n");
			}
			return sb.toString();
		}
	}
	
	
	// get settles and vols for the spot futures contract
	static class MySpotSettleVolQuery  implements QueryInterface<String, List<String[]>> {
		private final QueryInterface<String, SecDef> sdQuery;
		private final MyFutPriceVolQuery mySettleVolQuery;
		private final Calendar businessDay;
		
		private MySpotSettleVolQuery(
				MongoXml<SettlementDataInterface> mongoXmlSettle,
				MongoXml<BigDecimal> mongoXmlVol,
				QueryInterface<String, SecDef> sdQuery,
				Calendar businessDay){
			this.sdQuery = sdQuery;
			this.mySettleVolQuery = new MyFutPriceVolQuery(mongoXmlSettle, mongoXmlVol);
			this.businessDay = businessDay;
		}
		
		@Override
		/**
		 * qparams is a string like p1=NG.FUT.NYMEX&p2=CL.FUT.NYMEX&p3=ES.FUT.GLOBEX
		 *   that you receive from HttpCsvQueryServer
		 */
		public List<String[]> get(String qparams, int timeoutValue,
				TimeUnit timeUnitType) {
			// get type of request, which is in first param
			// the qparams string has the http parameters.
			//   
    		String[] matches = qparams.split("&");
    		Map<String, String> partialSnToSpotSn = new TreeMap<String, String>();
    		for(String match: matches){
    			String[] pair = match.split("=");
    			if(pair==null || pair.length<2){
    				// no partial name
    				Utils.prtObErrMess(this.getClass(), "param: "+pair[0]+" only has is missing the partial shortName");
    				continue;
    			}
    			String partialSn = pair[1];
    			String[] parts = partialSn.split("\\.");
    			if(parts.length<3){
    				Utils.prtObErrMess(this.getClass(), "partial shortName: "+partialSn+" only has 2 parts, not 3.  It needs to have symbol.type.exchange");
    				partialSnToSpotSn.put(partialSn, null);
    				continue;
    			}
    			String symbol = parts[0];
    			
    			SecSymbolType type;
				SecExchange exchange;
				SecCurrency currency;
				try {
					type = SecSymbolType.fromString(parts[1]);
					exchange = SecExchange.fromString(parts[2]);
					currency = SecCurrency.fromString("USD");
					if(parts.length>3){
						currency = SecCurrency.fromString(parts[3]);
					}
				} catch (Exception e) {					
					e.printStackTrace();
					continue;
				}
    			
    			SecDef spotSd = 
    					MarketDataComLib.getSpotContractPerBusinessDay(sdQuery, symbol, type, exchange, currency, null, null, businessDay);
    			if(spotSd==null){
    				partialSnToSpotSn.put(partialSn, null);
    			}else{
    				partialSnToSpotSn.put(partialSn, spotSd.getShortName());
    			}
    			
    		}
    		
			String qparamForFutVolQuery = "";
			
			int i = 1;
			int len = partialSnToSpotSn.size();
			for(Entry<String, String> entry : partialSnToSpotSn.entrySet()){
				String sn = entry.getValue();
				if(sn!=null){
					qparamForFutVolQuery += "p" + i + "="+sn;
				}
				if(i<len){
					qparamForFutVolQuery += "&";
				}
				i +=1;
			}
			return mySettleVolQuery.get(qparamForFutVolQuery, timeoutValue, timeUnitType);
		}

	}


}
