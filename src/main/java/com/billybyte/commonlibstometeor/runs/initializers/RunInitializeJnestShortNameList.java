package com.billybyte.commonlibstometeor.runs.initializers;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.mongo.MongoDatabaseNames;
import com.billybyte.mongo.MongoXml;

/**
 * Create the shortNames.txt file that holds a list of shortNames that will be used to generate jnest objects
 *   that are used to validate user entry fields for the Meteor position table.
 *   THIS MUST BE RUN WITH VIRTUAL MACHINE ARGS -Xmx1500m -Xms300m to pre-allocate
 *   enough memory to read in entire database of ImpliedVolDb.
 *   
 * @author bperlman1
 *
 */
public class RunInitializeJnestShortNameList {
	private static final String MONGO_IP = "mongoIp";
	private static final String MONGO_PORT = "mongoPort";
	private static final String DEF_MONGO_IP = "127.0.0.1";
	private static final Integer DEF_MONGO_PORT = 27017;
	private static final String OUTPUT_PATH = "outputPath";
	private static final String DEF_OUTPUT_PATH = "shortNames.txt";

	
	/**
	 * Read all of the shortNames in the mongo collection ImpliedVolColl in
	 *   the db ImpliedVolDb.
	 * @param args  String mongoIp, Integer mongoPort
	 */
	public static void main(String[] args) {
		Map<String, String> argPairs = 
				Utils.getArgPairsSeparatedByChar(args, "=");
		String mongoIp = argPairs.get(MONGO_IP)==null ? DEF_MONGO_IP : argPairs.get(MONGO_IP);
		Integer mongoPort = 
				argPairs.get(MONGO_PORT)==null ? 
						DEF_MONGO_PORT : 
							new Integer(argPairs.get(MONGO_PORT)); 
		MongoXml<BigDecimal> impliedVolCollection = 
				new MongoXml<BigDecimal>(mongoIp, mongoPort, 
						MongoDatabaseNames.IMPLIEDVOL_DB, MongoDatabaseNames.IMPLIEDVOL_CL);
		Map<String, BigDecimal> allRecs = 
				impliedVolCollection.getAll();
		List<String> shortNamesToWrite = 
				new ArrayList<String>(new TreeSet<String>(allRecs.keySet()));
		// Create the text files
		String outputPath = 
				argPairs.get(OUTPUT_PATH)==null ? DEF_OUTPUT_PATH : argPairs.get(OUTPUT_PATH);
		try {
			Utils.writeLineData(shortNamesToWrite, outputPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
