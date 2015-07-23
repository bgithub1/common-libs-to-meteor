package com.billybyte.commonlibstometeor.runs.initializers;

import java.io.IOException;

import com.billybyte.commonlibstometeor.GreekInputsData;
import com.billybyte.commonlibstometeor.runs.ArgBundle;
import com.billybyte.meteorjava.MeteorTableModel;

public class RunInitializeMeteorGreekInputsData {
	
	public static void main(String[] args) {
		ArgBundle ab = new ArgBundle(args);
		String xmlOutPath = ab.argPairs.get("xmlOutPath");

		MeteorTableModel tm = new MeteorTableModel(GreekInputsData.class,"GreekInputs",GreekInputsData.class.getCanonicalName(), new GreekInputsData().buildColumnModelArray());
		try {
			tm.toXml(GreekInputsData.class,xmlOutPath);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		MeteorTableModel.sendMeteorTableModels(ab.meteorUrl,ab.meteorPort,
				ab.adminEmail,
				ab.adminPass, tm);
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.exit(0);
				
	}
	
	
}
