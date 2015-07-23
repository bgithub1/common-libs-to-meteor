package com.billybyte.commonlibstometeor.runs.initializers;

import java.io.IOException;

import com.billybyte.commonlibstometeor.UnitVar;
import com.billybyte.commonlibstometeor.runs.ArgBundle;
import com.billybyte.meteorjava.MeteorTableModel;

public class RunInitializeMeteorUnitVar {
	public static void main(String[] args) {
		ArgBundle ab = new ArgBundle(args);
		String xmlOutPath = ab.argPairs.get("xmlOutPath");

		MeteorTableModel tm = new MeteorTableModel(UnitVar.class,"UnitVar",UnitVar.class.getCanonicalName(), new UnitVar().buildColumnModelArray());
		try {
			tm.toXml(UnitVar.class,xmlOutPath);
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
