package com.billybyte.commonlibstometeor.runs.initializers;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;

import com.billybyte.commonlibstometeor.runs.ArgBundle;
import com.billybyte.meteorjava.MeteorListSendReceive;
import com.billybyte.meteorjava.MeteorTableModel;
import com.billybyte.meteorjava.runs.SetupTableModelsAndSendReceiveLists;
import com.billybyte.meteorjava.staticmethods.Utils;

public class RunInitializeDeleteAllTableModels {
	public static void main(String[] args) {
		ArgBundle ab = new ArgBundle(args);
		
		
		@SuppressWarnings("unchecked")
		List<MeteorTableModel> mtmList = Utils.getFromXml(List.class, SetupTableModelsAndSendReceiveLists.class, "positionMeteorTableModels.xml");
		
		MeteorListSendReceive<MeteorTableModel> mlsrTableModel=null;
		try {
			mlsrTableModel = new MeteorListSendReceive<MeteorTableModel>(100, 
					MeteorTableModel.class, ab.meteorUrl, ab.meteorPort, 
					ab.adminEmail,ab.adminPass,"", "", "tester");
		} catch (URISyntaxException e) {
			throw Utils.IllState(e);
		}
		mlsrTableModel.deleteAllTableModels();
//		// the mtmList only has one object, but I put in an iteration
//		 //  for future use.
//		mtmList = mlsrTableModel.getList(new HashMap<String, String>());
//		for(MeteorTableModel mtm:mtmList){
//			String tableId = mtm.getId();
//			String error = mlsrTableModel.deleteMeteorTableModel( tableId);
//			if(error!=null && error.compareTo("0")!=0){
//				Utils.prtObErrMess(MeteorTableModel.class, error);
//			}
//		}
		
		System.exit(0);

	}
}
