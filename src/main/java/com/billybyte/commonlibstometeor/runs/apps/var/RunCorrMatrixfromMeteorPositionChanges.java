package com.billybyte.commonlibstometeor.runs.apps.var;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import com.billybyte.commonlibstometeor.DummyPositionBased;
import com.billybyte.commonlibstometeor.Position;
import com.billybyte.commonlibstometeor.runs.ArgBundle;
import com.billybyte.commonlibstometeor.runs.apps.ProcessMeteorPositionChanges;
import com.billybyte.dse.DerivativeSetEngine;
import com.billybyte.dse.debundles.DerivativeSetEngineBuilder;
import com.billybyte.dse.inputs.diotypes.CorrDiot;
import com.billybyte.dse.queries.DseInputQuery;
import com.billybyte.marketdata.SecDef;
import com.billybyte.mathstuff.MathStuff;
import com.billybyte.meteorjava.MeteorCsvSendReceive;
import com.billybyte.queries.ComplexQueryResult;


/**
 * Process requests for correlation matrices of the all the underlying securities
 *   
 * @author sarahhartman
 *
 */
public class RunCorrMatrixfromMeteorPositionChanges {
	public static final String CORREL_TABLENAME = "CorrelationData";

	/**
	 * respond to position changes and recalc requests
	 * @author sarahhartman
	 *
	 */
	private static class ProcessMeteorCorrelationRequest extends ProcessMeteorPositionChanges<DummyPositionBased> {
		private final MeteorCsvSendReceive mcsvsr;
		private final DerivativeSetEngine dse;
		private final DseInputQuery<BigDecimal> corrQuery;
		private static final BigDecimal BADCORR = new BigDecimal("-1111111.0");
		
		public ProcessMeteorCorrelationRequest(DerivativeSetEngine dse,
				String meteorUrl, Integer meteorPort, String adminEmail,
				String adminPass) {
			super(dse, meteorUrl, meteorPort, adminEmail, adminPass, DummyPositionBased.class,
					new String[]{Position.class.getCanonicalName(),CORREL_TABLENAME});
			this.mcsvsr = new MeteorCsvSendReceive(this.getMlsrOfTableChangedByUser());
			this.dse = dse;
			this.corrQuery = 
					dse.getQueryManager().getQuery(new CorrDiot());;
		}
		
		@Override
		public void processMrecs(List<Position> positionFromMeteor,
				Double errorValueToReturn) {
			// TODO Auto-generated method stub
			String onlyUserId = getSingleUserId(positionFromMeteor);
			TreeSet<String> snSet = new TreeSet<String>();
			// get underlyings
			for(Position p : positionFromMeteor){
				List<SecDef> underSdList = 
						dse.getQueryManager().getUnderlyingSecDefs(p.getShortName(), 1, TimeUnit.SECONDS);
				for(SecDef underSd : underSdList){
					snSet.add(underSd.getShortName());	
				}
				
			}
			// get correlation cqr map of underlyings
			Map<String, ComplexQueryResult<BigDecimal>> corrMap = 
					corrQuery.get(snSet, 1, TimeUnit.SECONDS);
			Map<String,BigDecimal> correlMap = new HashMap<String, BigDecimal>();
			// make the map from ComplexQueryResult<BigDecimal> to BigDecimal
			for(Entry<String, ComplexQueryResult<BigDecimal>> entry:corrMap.entrySet()){
				ComplexQueryResult<BigDecimal> cqr = entry.getValue();
				BigDecimal corr = null;
				if(!cqr.isValidResult()){
					corr = BADCORR;
				}else{
					corr = cqr.getResult();
				}
				correlMap.put(entry.getKey(),corr);
			}
			// create printable correlation matrix
			int lineSize = snSet.size()+1;
 			List<String[]> outputCsv = 
 					MathStuff.buildCorrMatrixCsv(snSet, correlMap);
 			
 			// create a header
 			String[] header = new String[lineSize];
 			for(int i = 0;i<lineSize;i++){
 				header[i] = "c"+(i+1);
 			}
 			
 			outputCsv.add(0,header);
 			this.mcsvsr.sendCsvData(onlyUserId, CORREL_TABLENAME, outputCsv,true);
		}

		@Override
		public List<DummyPositionBased> aggregateMrecs(
				List<DummyPositionBased> mRecPerPositionList) {
			// do nothing
			return null;
		}
		
	}
	
	public static void main(String[] args) {
		ArgBundle ab = new ArgBundle(args);
		// Get the path of a Spring Xml file that will create a DerivativeSetEngine
		
		// Create main processor that turns Position objects
		//  into instances of GreeksData.
		// you don't really use yahoo to get dse
		final DerivativeSetEngine dse = 
				DerivativeSetEngineBuilder.dseForStocksUsingYahooAndSpring(null,ab.dseXmlPath);
		
		
		ProcessMeteorCorrelationRequest mainProcessor = 
				new ProcessMeteorCorrelationRequest(
						dse, 
						ab.meteorUrl, ab.meteorPort, ab.adminEmail, 
						ab.adminPass);
		
		// init table model for csv
		MeteorCsvSendReceive mcsvsr = new MeteorCsvSendReceive(mainProcessor.getMlsrOfTableChangedByUser());
		mcsvsr.sendCsvTableModel(CORREL_TABLENAME);

		// Start the loop that waits for changes in the collection
		//   com.billybyte.commonlibstometeor.Position because of an
		//   insert or delete by the client in that collection, and
		//   then sends a List<GreeksData> to Meteor for that client (userId).
		mainProcessor.process();
	}
}
