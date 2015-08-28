package com.billybyte.commonlibstometeor.runs.examples;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.billybyte.clientserver.httpserver.HttpCsvQueryServer;
import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commonlibstometeor.runs.ArgBundle;
import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.dse.DerivativeSetEngine;
import com.billybyte.dse.debundles.DerivativeSetEngineBuilder;
import com.billybyte.dse.inputs.InBlk;
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
				new MyQuery(dse);
		HttpCsvQueryServer httpServerQuery = null;
		try {
			httpServerQuery = 
					new HttpCsvQueryServer(httpPort, httpPath, csvQuery,
							10,TimeUnit.SECONDS,"greeks.csv");
		} catch (IOException e) {
			throw Utils.IllState(e);
		}
		httpServerQuery.start();
	}
	
	static class MyQuery implements QueryInterface<String, List<String[]>> {

		private final DerivativeSetEngine dse;
		private MyQuery(DerivativeSetEngine dse){
			this.dse = dse;
		}
		@Override
		public List<String[]> get(String qparams, int timeoutValue,
				TimeUnit timeUnitType) {
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
}
