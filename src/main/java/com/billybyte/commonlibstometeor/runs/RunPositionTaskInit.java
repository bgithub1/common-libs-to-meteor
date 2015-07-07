package com.billybyte.commonlibstometeor.runs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.billybyte.commonlibstometeor.Position;
import com.billybyte.commonlibstometeor.PositionTask;
import com.billybyte.meteorjava.MeteorBaseListItem;
import com.billybyte.meteorjava.MeteorTableModel;

public class RunPositionTaskInit {
	public static void main(String[] args) {
		ArgBundle ab = new ArgBundle(args);
		Map<String, String> argPairs = ab.argPairs;

		String buildTableModel = argPairs.get("buildTableModel");
		Boolean buildIt = buildTableModel==null ? true : new Boolean(buildTableModel);
		if(buildIt){
			MeteorTableModel tm = PositionTask.buildTableModel();
			List<MeteorTableModel> mtmList = 
					new ArrayList<MeteorTableModel>();
			mtmList.add(tm);

			MeteorTableModel.sendMeteorTableModels(ab.meteorUrl,ab.meteorPort,
					ab.adminEmail,
					ab.adminPass, tm);
		}
		// add basic tasks
		List<Position> pList = 
				Position.getPositionFromMeteor(ab.meteorUrl, ab.meteorPort, ab.adminEmail, ab.adminPass,null);
		Set<String> userIdSet = new HashSet<String>();
		for(Position p : pList){
			userIdSet.add(p.getUserId());
		}
		String tasksPath = argPairs.get("tasksPath");
		if(tasksPath==null){
			tasksPath = "tasks.txt";
		}
		Set<String> taskSet = 
				com.billybyte.meteorjava.staticmethods.Utils.readSetData(tasksPath);
		List<PositionTask> ptaskList = new ArrayList<PositionTask>();
		for(String taskId : taskSet){
			for(String userId : userIdSet){
				PositionTask ptask = new PositionTask(userId, taskId);
				ptaskList.add(ptask);
			}
		}
		
		MeteorBaseListItem.sendItemsToMeteor(PositionTask.class, 
				ab.meteorUrl, ab.meteorPort, ab.adminEmail, ab.adminPass, ptaskList);
	}
}
