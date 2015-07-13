package com.billybyte.commonlibstometeor.runs.apps.greeks;

import java.net.URISyntaxException;

import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commonlibstometeor.runs.ArgBundle;
import com.billybyte.dse.DerivativeSetEngine;
import com.billybyte.dse.debundles.DerivativeSetEngineBuilder;
import com.billybyte.marketdata.SecDef;
import com.billybyte.marketdata.SecDefQueryAllMarkets;
import com.billybyte.meteorjava.MeteorListSendReceive;
import com.billybyte.meteorjava.TableChangedByUser;
import com.billybyte.meteorjava.staticmethods.Utils;

public class RunGreeksToMeteorLoop {
	public static void main(String[] args) {
		// ********** basic initialization ********************************
		// Get all of the usual arguments that pertain to connecting to meteor
		ArgBundle ab = new ArgBundle(args);
		// Get the path of a Spring Xml file that will create a DerivativeSetEngine
		String dseXmlPath = ab.argPairs.get("dseXmlPath");
		// Create a SecDef query
		final QueryInterface<String, SecDef> sdQuery = new SecDefQueryAllMarkets();
		
		// you don't really use yahoo to get dse
		final DerivativeSetEngine dse = 
				DerivativeSetEngineBuilder.dseForStocksUsingYahooAndSpring(null,dseXmlPath);

		// Create a MeteorListSendReceive instance to control communication with Meteor
		//  You'll reuse this connection object to get other kinds of objects besides TableChangedByUser objects
		MeteorListSendReceive<TableChangedByUser> mlsr = null;
		try {
			mlsr = new MeteorListSendReceive<TableChangedByUser>(100, 
							TableChangedByUser.class, 
							ab.meteorUrl, ab.meteorPort, 
							ab.adminEmail,ab.adminPass,"", "", "tester");
			
		} catch (URISyntaxException e) {
			throw Utils.IllState(e);
		}
		
		
	}
}
