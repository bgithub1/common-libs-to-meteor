package com.billybyte.commonlibstometeor.runs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.billybyte.commonlibstometeor.Position;
import com.billybyte.commonstaticmethods.CollectionsStaticMethods;
import com.billybyte.commonstaticmethods.Utils;

public class RunGetPositionFromMeteor {
	public static void main(String[] args) {
		ArgBundle ab = new ArgBundle(args);
		List<Position> pList = 
				Position.getPositionFromMeteor(ab.meteorUrl, ab.meteorPort, ab.adminEmail, ab.adminPass,null);
		Utils.prt("get all positions:");
		CollectionsStaticMethods.prtListItems(pList);
		String userid = ab.userId;
		Utils.prt("get positions with userId = "+userid);
		Map<String, String> selectorMap = new HashMap<String, String>();
		selectorMap.put("userId",userid);
		pList = 
				Position.getPositionFromMeteor(ab.meteorUrl, ab.meteorPort, ab.adminEmail, ab.adminPass,selectorMap);		Utils.prt("get positions:");
		CollectionsStaticMethods.prtListItems(pList);
		
	}
	
	
}
