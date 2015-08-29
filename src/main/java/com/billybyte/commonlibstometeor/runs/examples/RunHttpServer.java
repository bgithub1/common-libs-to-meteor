package com.billybyte.commonlibstometeor.runs.examples;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import com.billybyte.clientserver.httpserver.HttpCsvQueryServer;
import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commoninterfaces.SettlementDataInterface;
import com.billybyte.commonlibstometeor.runs.ArgBundle;
import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.dse.DerivativeSetEngine;
import com.billybyte.dse.debundles.DerivativeSetEngineBuilder;
import com.billybyte.dse.inputs.InBlk;
import com.billybyte.dse.inputs.diotypes.AtmDiot;
import com.billybyte.dse.inputs.diotypes.SettlePriceDiot;
import com.billybyte.dse.inputs.diotypes.VolDiot;
import com.billybyte.dse.outputs.DerivativeReturn;
import com.billybyte.dse.outputs.OptPriceDerSen;
import com.billybyte.queries.ComplexQueryResult;

public class RunHttpServer {
	public static void main(String[] args) {
		
		// Get all of the usual arguments that pertain to connecting to meteor
		ArgBundle ab = new ArgBundle(args);
		// Get the path of a Spring Xml file that will create a DerivativeSetEngine
		
		int httpPort = new Integer(ab.argPairs.get("httpPort"));
		String httpPath = ab.argPairs.get("httpPath");
		
		// Create main processor that turns Position objects
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
		httpServerQuery.addAlternativePath("/futpricevol", new MyFutPriceVolQuery(dse));
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

		private final DerivativeSetEngine dse;
		private MyFutPriceVolQuery(DerivativeSetEngine dse){
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
			List<String[]> outputCsv = new ArrayList<String[]>();
			String[] header = {"shortName","settle","settleVol"};
			outputCsv.add(header);
    		for(String sn : derivativeShortNameSet){
    			ComplexQueryResult<InBlk> inBlkCqr = inputBlkCqrMap.get(sn);
    			String[] newLine = new String[3];
				newLine[0] = sn;
    			if(inBlkCqr!=null && inBlkCqr.isValidResult()){
    				InBlk inBlk = inBlkCqr.getResult();
    				SettlementDataInterface settle = inBlk.getMainInputList(new SettlePriceDiot());
    				if(settle!=null && settle.getPrice()!=null){
    					newLine[1] = settle.getPrice().toString();
    				}
    				BigDecimal vol = inBlk.getMainInputList(new VolDiot());
    				if(vol!=null){
    					newLine[2] = vol.toString();
    				}
    			}
    			outputCsv.add(newLine);
    		}
    		
			return outputCsv;
		}
		
	}

}
