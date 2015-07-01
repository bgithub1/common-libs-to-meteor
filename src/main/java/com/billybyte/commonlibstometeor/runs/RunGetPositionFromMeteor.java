package com.billybyte.commonlibstometeor.runs;

import java.util.List;

import com.billybyte.commonlibstometeor.Position;
import com.billybyte.commonstaticmethods.CollectionsStaticMethods;

public class RunGetPositionFromMeteor {
	public static void main(String[] args) {
		ArgBundle ab = new ArgBundle(args);
		List<Position> pList = 
				Position.getPositionFromMeteor(ab.meteorUrl, ab.meteorPort, ab.adminEmail, ab.adminPass);
		CollectionsStaticMethods.prtListItems(pList);
	}
}
