package com.billybyte.commonlibstometeor.runs.initializers;

import java.io.IOException;
import com.billybyte.commonlibstometeor.ProfitAndLoss;
import com.billybyte.commonlibstometeor.runs.ArgBundle;
import com.billybyte.meteorjava.MeteorTableModel;

public class RunInitializeMeteorProfitAndLoss {
	
	public static void main(String[] args) {
		ArgBundle ab = new ArgBundle(args);
		String xmlOutPath = ab.argPairs.get("xmlOutPath");

		MeteorTableModel tm = new MeteorTableModel(ProfitAndLoss.class,"ProfitAndLoss",ProfitAndLoss.class.getCanonicalName(), new ProfitAndLoss().buildColumnModelArray());
		try {
			tm.toXml(ProfitAndLoss.class,xmlOutPath);
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
