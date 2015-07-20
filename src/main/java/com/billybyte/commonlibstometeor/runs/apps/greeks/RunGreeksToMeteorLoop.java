package com.billybyte.commonlibstometeor.runs.apps.greeks;

import java.net.URISyntaxException;
import java.util.List;

import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commonlibstometeor.GreeksData;
import com.billybyte.commonlibstometeor.Position;
import com.billybyte.commonlibstometeor.runs.ArgBundle;
import com.billybyte.commonlibstometeor.runs.apps.ProcessMeteorPositionChanges;
import com.billybyte.commonstaticmethods.CollectionsStaticMethods;
import com.billybyte.dse.DerivativeSetEngine;
import com.billybyte.dse.debundles.DerivativeSetEngineBuilder;
import com.billybyte.marketdata.SecDef;
import com.billybyte.marketdata.SecDefQueryAllMarkets;
import com.billybyte.meteorjava.MeteorListSendReceive;
import com.billybyte.meteorjava.TableChangedByUser;
import com.billybyte.meteorjava.staticmethods.Utils;

public class RunGreeksToMeteorLoop {
	private static class ProcessGreeksFromMeteorPositionChanges extends ProcessMeteorPositionChanges<GreeksData>{

		public ProcessGreeksFromMeteorPositionChanges(DerivativeSetEngine dse,
				String meteorUrl, Integer meteorPort, String adminEmail,
				String adminPass) {
			super(dse, meteorUrl, meteorPort, adminEmail, adminPass, GreeksData.class);
		}
	
	}

	public static void main(String[] args) {
		// ********** basic initialization ********************************
		// Get all of the usual arguments that pertain to connecting to meteor
		ArgBundle ab = new ArgBundle(args);
		// Get the path of a Spring Xml file that will create a DerivativeSetEngine
		String dseXmlPath = ab.argPairs.get("dseXmlPath");
		// Create a SecDef query
		
		// you don't really use yahoo to get dse
		final DerivativeSetEngine dse = 
				DerivativeSetEngineBuilder.dseForStocksUsingYahooAndSpring(null,dseXmlPath);

		// Create a MeteorListSendReceive instance to control communication with Meteor
		//  You'll reuse this connection object to get other kinds of objects besides TableChangedByUser objects
		MeteorListSendReceive<Position> mlsr = null;
		try {
			mlsr = new MeteorListSendReceive<Position>(100, 
					Position.class, 
							ab.meteorUrl, ab.meteorPort, 
							ab.adminEmail,ab.adminPass,"", "", "tester");
			
		} catch (URISyntaxException e) {
			throw Utils.IllState(e);
		}
		
		ProcessGreeksFromMeteorPositionChanges processGreeks = 
				new ProcessGreeksFromMeteorPositionChanges(dse, ab.meteorUrl, ab.meteorPort, 
						ab.adminEmail,ab.adminPass);
		
		boolean keepGoing = true;
		while(keepGoing){
			List<Position> positionFromMeteor = mlsr.getList(null);
			CollectionsStaticMethods.prtListItems(positionFromMeteor);
			processGreeks.processMrecs(positionFromMeteor, -111111.0);
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
				break;
			}
		}
	}
}
