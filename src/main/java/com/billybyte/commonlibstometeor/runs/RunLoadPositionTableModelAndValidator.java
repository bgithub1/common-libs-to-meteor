package com.billybyte.commonlibstometeor.runs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.billybyte.commonlibstometeor.Position;
import com.billybyte.meteorjava.MeteorTableModel;
import com.billybyte.meteorjava.MeteorValidator;

public class RunLoadPositionTableModelAndValidator {
	public static void main(String[] args) {
		ArgBundle ab = new ArgBundle(args);
		Map<String, String> argPairs = ab.argPairs;
		MeteorTableModel tm = Position.buildPositionTableModel();

//		List<MeteorTableModel> mtmList = 
//				new ArrayList<MeteorTableModel>();
//		mtmList.add(tm);
//
//		MeteorTableModel.sendMeteorTableModels(ab.meteorUrl,ab.meteorPort,
//				ab.adminEmail,
//				ab.adminPass, tm);
		
		String snPath = argPairs.get("shortNamePath");
		if(snPath==null){
			snPath = "shortNames.txt";
		}
		Set<String> shortNameSet = 
				com.billybyte.meteorjava.staticmethods.Utils.readSetData(snPath);
		MeteorValidator posValidator = 
				Position.buildValidator(shortNameSet);
		posValidator.sendValidator(ab.meteorUrl, ab.meteorPort,
				ab.adminEmail, ab.adminPass);
	}
}
