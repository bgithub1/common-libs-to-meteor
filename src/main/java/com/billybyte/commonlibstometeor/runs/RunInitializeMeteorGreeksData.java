package com.billybyte.commonlibstometeor.runs;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import com.billybyte.commonlibstometeor.GreeksData;
import com.billybyte.meteorjava.MeteorListSendReceive;
import com.billybyte.meteorjava.MeteorTableModel;
import com.billybyte.meteorjava.MeteorValidator;
import com.billybyte.meteorjava.staticmethods.Utils;

public class RunInitializeMeteorGreeksData {
	
	public static void main(String[] args) {
		ArgBundle ab = new ArgBundle(args);
		
		// Send table models
		sendMeteorTableModels(ab.meteorUrl, 
				ab.meteorPort, 
				ab.adminEmail, 
				ab.adminPass);
	}
	
	
	private static final MeteorTableModel buildGreeksTableModel(){
		MeteorTableModel tableModel = 
				new MeteorTableModel(
						GreeksData.class,"Greeks",GreeksData.class.getName(), 
						GreeksData.buildMetColModelArray());
		return tableModel;
	}
	
	
	private static final void sendMeteorTableModels(
			String meteorUrl,
			Integer meteorPort,
			String adminEmail,
			String adminPass){
		MeteorTableModel tm = buildGreeksTableModel();

		List<MeteorTableModel> mtmList = 
				new ArrayList<MeteorTableModel>();
		mtmList.add(tm);

		MeteorListSendReceive<MeteorTableModel> mlsrTableModel=null;
		try {
			mlsrTableModel = new MeteorListSendReceive<MeteorTableModel>(100, 
					MeteorTableModel.class, meteorUrl, meteorPort, 
					adminEmail,adminPass,"", "", "tester");
		} catch (URISyntaxException e) {
			throw Utils.IllState(e);
		}
		// the mtmList only has one object, but I put in an iteration
		 //  for future use.
		for(MeteorTableModel mtm:mtmList){
			String tableId = mtm.getId();
			String error = mlsrTableModel.deleteMeteorTableModel( tableId);
			if(error!=null && error.compareTo("0")!=0){
				Utils.prtObErrMess(RunInitializeMeteorGreeksData.class, error);
			}
		}
		try {
			mlsrTableModel.sendTableModelList(mtmList);
			mlsrTableModel.disconnect();
		} catch (InterruptedException e) {
			throw Utils.IllState(e);
		}
		

	}
	
}
