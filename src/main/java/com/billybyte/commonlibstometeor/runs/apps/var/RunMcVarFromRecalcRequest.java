package com.billybyte.commonlibstometeor.runs.apps.var;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.billybyte.commonlibstometeor.DummyPositionBased;
import com.billybyte.commonlibstometeor.Position;
import com.billybyte.commonlibstometeor.runs.ArgBundle;
import com.billybyte.commonlibstometeor.runs.apps.ProcessMeteorPositionChanges;
import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.dse.DerivativeSetEngine;
import com.billybyte.dse.debundles.DerivativeSetEngineBuilder;
import com.billybyte.dse.var.McVar;
import com.billybyte.meteorjava.MeteorCsvSendReceive;
/**
 * Execute McVar when it is requested by hitting the RecCalculate button on 
 *   the meteor table for McVar.
 * @author bill perlman
 *
 */
public class RunMcVarFromRecalcRequest {
	public static final String MCVAR_TABLENAME = "McVarData";

	private static class ProcecssMeteorMcVarRequest extends ProcessMeteorPositionChanges<DummyPositionBased> {
		private int trials = 10000;
		private final MeteorCsvSendReceive mcsvsr;
		private final McVar mcVar   = new McVar();
		
		
		public ProcecssMeteorMcVarRequest(DerivativeSetEngine dse,
				String meteorUrl, Integer meteorPort, String adminEmail,
				String adminPass) {
			super(dse, meteorUrl, meteorPort, adminEmail, adminPass, DummyPositionBased.class,
					new String[]{MCVAR_TABLENAME});
			this.mcsvsr = new MeteorCsvSendReceive(this.getMlsrOfTableChangedByUser());
			
		}

		@Override
		public List<DummyPositionBased> aggregateMrecs(
				List<DummyPositionBased> mRecPerPositionList) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void processMrecs(List<Position> positionFromMeteor,
				Double errorValueToReturn) {
			// create a map of shortNames and total qty's for that shortName
			Map<String, BigDecimal> snToQtyMap  = new HashMap<String, BigDecimal>();
			Set<String> userIdSet = new HashSet<String>(); // this should be only one element
			String onlyUserId = null;
			for(Position p : positionFromMeteor){
				onlyUserId = p.getUserId();
				if(onlyUserId!=null){
					userIdSet.add(onlyUserId);
				}
				String sn = p.getShortName();
				if(!snToQtyMap.containsKey(sn)){
					snToQtyMap.put(sn,BigDecimal.ZERO);
				}
				BigDecimal currQty = snToQtyMap.get(sn);
				BigDecimal newQty = currQty.add(p.getQty());
				snToQtyMap.put(sn,newQty);
			}
			if(userIdSet.size()>1){
				throw Utils.IllState(this.getClass(), "more than one userId is being processed when it shouldn't be");
			}
			if(onlyUserId==null){
				throw Utils.IllState(this.getClass(), "no userId is being processed when it should be");
			}
			List<String[]> outputCsv = new ArrayList<String[]>();
			double result = -1111111.0;
			try {
				result = mcVar.mcVar(snToQtyMap, trials, this.getDse());
			} catch (Exception e) {
				e.printStackTrace();
			}
			String[] oneLine = {"MonteCarlo VaR",new Double(result).toString()};
			outputCsv.add(oneLine);
			this.mcsvsr.sendCsvData(onlyUserId, MCVAR_TABLENAME, outputCsv,true);

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
		
		ProcecssMeteorMcVarRequest mainProcessor = 
				new ProcecssMeteorMcVarRequest(
						dse, 
						ab.meteorUrl, ab.meteorPort, ab.adminEmail, 
						ab.adminPass
						);
		MeteorCsvSendReceive mcsvsr = new MeteorCsvSendReceive(mainProcessor.getMlsrOfTableChangedByUser());
		mcsvsr.sendCsvTableModel(MCVAR_TABLENAME);
		// Start the loop that waits for changes in the collection
		//   com.billybyte.commonlibstometeor.Position because of an
		//   insert or delete by the client in that collection, and
		//   then sends a List<GreeksData> to Meteor for that client (userId).
		mainProcessor.process();
		
	}
}
