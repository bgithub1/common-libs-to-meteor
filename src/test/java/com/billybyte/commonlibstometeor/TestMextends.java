package com.billybyte.commonlibstometeor;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.billybyte.commonstaticmethods.CollectionsStaticMethods;
import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.dse.DerivativeSetEngine;
import com.billybyte.dse.queries.SerialOptUnderQuery;
import com.billybyte.marketdata.SecDef;

import junit.framework.TestCase;

public class TestMextends extends TestCase {
	static abstract class ClassMain {
		abstract <M extends ClassMain> M myMethod(int i);
		int myInt;
	}
	static class ClassSub extends ClassMain{

		@Override
		ClassSub myMethod(int i) {
			ClassSub cs = new ClassSub();
			System.out.print("i = " + i);
			return cs;
		}
		
	}
	public void test1(){
		ClassSub cs = new ClassSub();
		cs.myMethod(10);
	}
	
	public void testSerialOptUnderQueries(){
		SerialOptUnderQuery sBSerialOptUnderQuery = Utils.springGetBean(SerialOptUnderQuery.class, 
				"beans_MongoBasedQueryManager.xml", "sbUnderQuery");
		assertNotNull(sBSerialOptUnderQuery);
		List<SecDef> sbSdList = 
				sBSerialOptUnderQuery.get("SB.FOP.NYBOT.USD.201511.C.12.00", 1, TimeUnit.SECONDS);
		assertNotNull(sbSdList);
		assertTrue(sbSdList.size()>0);
		CollectionsStaticMethods.prtListItems(sbSdList);

		SerialOptUnderQuery ojSerialOptUnderQuery = Utils.springGetBean(SerialOptUnderQuery.class, 
				"beans_MongoBasedQueryManager.xml", "ojUnderQuery");
		assertNotNull(ojSerialOptUnderQuery);
		List<SecDef> ojSdList = 
				ojSerialOptUnderQuery.get("OJ.FOP.NYBOT.USD.201510.C.130.00", 1, TimeUnit.SECONDS);
		assertNotNull(ojSdList);
		assertTrue(ojSdList.size()>0);
		CollectionsStaticMethods.prtListItems(ojSdList);
	
	}
	
	public void testSerialQueriesUsingQm(){
		DerivativeSetEngine dse = Utils.springGetBean(DerivativeSetEngine.class, 
				"beans_DseFromMongoBasedQm_EvalToday.xml", "dse");
		assertNotNull(dse);
		List<SecDef> sbSdList = dse.getQueryManager().getUnderlyingSecDefs("SB.FOP.NYBOT.USD.201511.C.12.00", 1, TimeUnit.SECONDS);
		assertNotNull(sbSdList);
		assertTrue(sbSdList.size()>0);
		CollectionsStaticMethods.prtListItems(sbSdList);
		
		List<SecDef> ojSdList = dse.getQueryManager().getUnderlyingSecDefs("OJ.FOP.NYBOT.USD.201510.C.130.00", 1, TimeUnit.SECONDS);
		assertNotNull(ojSdList);
		assertTrue(ojSdList.size()>0);
		CollectionsStaticMethods.prtListItems(ojSdList);
	}
}
