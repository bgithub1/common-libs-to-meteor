package com.billybyte.commonlibstometeor.runs.initializers;

import java.net.URISyntaxException;

import com.billybyte.commonlibstometeor.runs.ArgBundle;
import com.billybyte.meteorjava.MeteorListSendReceive;
import com.billybyte.meteorjava.MeteorTableModel;
import com.billybyte.meteorjava.staticmethods.Utils;

public class RunInitializeDeleteAllTableModels {
	public static void main(String[] args) {
		ArgBundle ab = new ArgBundle(args);
		
		
//		@SuppressWarnings("unchecked")
//		List<MeteorTableModel> mtmList = Utils.getFromXml(List.class, SetupTableModelsAndSendReceiveLists.class, "positionMeteorTableModels.xml");
		
		MeteorListSendReceive<MeteorTableModel> mlsrTableModel=null;
		try {
			mlsrTableModel = new MeteorListSendReceive<MeteorTableModel>(100, 
					MeteorTableModel.class, ab.meteorUrl, ab.meteorPort, 
					ab.adminEmail,ab.adminPass,"", "", "tester");
		} catch (URISyntaxException e) {
			throw Utils.IllState(e);
		}
		mlsrTableModel.deleteAllTableModels();
		
		System.exit(0);

	}
}
