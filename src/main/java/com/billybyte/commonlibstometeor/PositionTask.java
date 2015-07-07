package com.billybyte.commonlibstometeor;

import com.billybyte.meteorjava.MeteorBaseListItem;
import com.billybyte.meteorjava.MeteorColumnModel;
import com.billybyte.meteorjava.MeteorTableModel;
import com.billybyte.meteorjava.MeteorValidator;

public class PositionTask extends MeteorBaseListItem{
	private final String taskId;

	public PositionTask(String userId, String taskId) {
		super(userId+"_"+taskId,userId);
		this.taskId = taskId;
	}

	public String getTaskId() {
		return taskId;
	}

	
	public static final MeteorColumnModel[] buildMetColModelArray(){

		MeteorColumnModel idCm = 
				new MeteorColumnModel("_id","_id","_id",null);
		MeteorColumnModel[] ret = {
				idCm
		};
		return ret;

	}
	public static MeteorTableModel buildTableModel() {
		MeteorTableModel tableModel = 
				new MeteorTableModel(
						PositionTask.class,"PositionTask",PositionTask.class.getName(), 
						PositionTask.buildMetColModelArray());
		return tableModel;
	}

	public static  MeteorValidator buildValidator() {
		return null;
	}

	
	
}
