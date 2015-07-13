package com.billybyte.commonlibstometeor.runs.initializers;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import com.billybyte.commonlibstometeor.Position;
import com.billybyte.commonlibstometeor.runs.ArgBundle;
import com.billybyte.meteorjava.MeteorTableModel;
import com.billybyte.meteorjava.MeteorValidator;

public class RunInitializeMeteorPosition {
	public static void main(String[] args) {
		ArgBundle ab = new ArgBundle(args);
		Map<String, String> argPairs = ab.argPairs;

		String buildTableModel = argPairs.get("buildTableModel");
		Boolean buildIt = buildTableModel==null ? true : new Boolean(buildTableModel);
		if(buildIt){
			MeteorTableModel tm = new MeteorTableModel(
					Position.class,"Position",Position.class.getCanonicalName(), new Position().buildColumnModelArray());
			try {
				tm.toXml(Position.class, null);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			MeteorTableModel.sendMeteorTableModels(ab.meteorUrl,ab.meteorPort,
					ab.adminEmail,
					ab.adminPass, tm);
		}
		
		String snPath = argPairs.get("shortNamePath");
		if(snPath==null){
			snPath = "shortNames.txt";
		}
		Set<String> shortNameSet = 
				com.billybyte.meteorjava.staticmethods.Utils.readSetData(snPath);
		MeteorValidator posValidator = 
				Position.buildPositionValidator(shortNameSet);
		posValidator.sendValidator(ab.meteorUrl, ab.meteorPort,
				ab.adminEmail, ab.adminPass);
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.exit(0);
	}
}
