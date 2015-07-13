package com.billybyte.commonlibstometeor.runs.initializers;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import com.billybyte.commonlibstometeor.GreeksData;
import com.billybyte.commonlibstometeor.runs.ArgBundle;
import com.billybyte.meteorjava.MeteorListSendReceive;
import com.billybyte.meteorjava.MeteorTableModel;
import com.billybyte.meteorjava.staticmethods.Utils;

public class RunInitializeMeteorGreeksData {
	
	public static void main(String[] args) {
		ArgBundle ab = new ArgBundle(args);
		String xmlOutPath = ab.argPairs.get("xmlOutPath");

		MeteorTableModel tm = new MeteorTableModel(GreeksData.class,"Greeks",GreeksData.class.getCanonicalName(), new GreeksData().buildColumnModelArray());
		try {
			tm.toXml(GreeksData.class,xmlOutPath);
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
