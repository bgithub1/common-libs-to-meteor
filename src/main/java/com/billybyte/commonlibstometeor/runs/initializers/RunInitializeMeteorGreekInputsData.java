package com.billybyte.commonlibstometeor.runs.initializers;

import java.net.URISyntaxException;

import com.billybyte.commonlibstometeor.runs.ArgBundle;
import com.billybyte.commonlibstometeor.runs.apps.greeks.RunGreekInputsCsvFromMeteorPositionChanges;
import com.billybyte.meteorjava.MeteorCsvSendReceive;

public class RunInitializeMeteorGreekInputsData {
	
	public static void main(String[] args) {
		ArgBundle ab = new ArgBundle(args);
//		String xmlOutPath = ab.argPairs.get("xmlOutPath");
//
//		MeteorTableModel tm = new MeteorTableModel(GreekInputsData.class,"GreekInputs",GreekInputsData.class.getCanonicalName(), new GreekInputsData().buildColumnModelArray());
//		try {
//			tm.toXml(GreekInputsData.class,xmlOutPath);
//		} catch (IOException e1) {
//			e1.printStackTrace();
//		}
//		MeteorTableModel.sendMeteorTableModels(ab.meteorUrl,ab.meteorPort,
//				ab.adminEmail,
//				ab.adminPass, tm);
//		try {
//			Thread.sleep(3000);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}

		try {
			MeteorCsvSendReceive mcsvsr = new MeteorCsvSendReceive(
					ab.meteorUrl,
					ab.meteorPort.intValue(),
					ab.adminEmail,
					ab.adminPass,null,null, "tester");
			mcsvsr.sendCsvTableModel(RunGreekInputsCsvFromMeteorPositionChanges.GREEKINPUTSDATA_TABLENAME);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		System.exit(0);
				
	}
	
	
}
