package com.billybyte.commonlibstometeor.runs;

import java.util.HashMap;
import java.util.Map;

import com.billybyte.meteorjava.staticmethods.Utils;

public class ArgBundle {
	final public Map<String, String> argPairs;
	final public String userId ;
	final public String meteorUrl;
	final public Integer meteorPort;
	final public String adminEmail ;
	final public String adminPass ;
	final public String account ;
	final public String strategy ;
	final public String dseXmlPath ;
	
	private static final String DEF_ADMIN_EMAIL = "admin1@demo.com";
	private static final String DEF_ADMIN_PASS = "admin1";
	private static final String DEF_METURL = "localhost";
	private static final Integer DEF_PORT = 3000;
	private static final String DEF_ACC = "a1";
	private static final String DEF_STRAT = "s1";
	private static final String DEF_DSEPATH = "beans_DefaultDse.xml";

	public ArgBundle(String[] args){
		this.argPairs = 
				Utils.getArgPairsSeparatedByChar(args, "=");
		String uid = argPairs.get("userId");
		this.userId = uid==null ? DEF_ADMIN_EMAIL : uid ;
		String murl = argPairs.get("metUrl");;
		this.meteorUrl = murl==null ? DEF_METURL : murl;
		String mp = argPairs.get("metPort");
		this.meteorPort = mp==null ? DEF_PORT : new Integer(mp);
		String adem = argPairs.get("adminEmail");
		this.adminEmail = adem==null ? DEF_ADMIN_EMAIL : adem;
		String adp = argPairs.get("adminPass");
		this.adminPass = adp==null ? DEF_ADMIN_PASS : adp;
		this.account = argPairs.get("account")==null ? DEF_ACC : argPairs.get("account");
		this.strategy = argPairs.get("strategy")==null ? DEF_STRAT : argPairs.get("strategy");
		this.dseXmlPath = argPairs.get("dseXmlPath")==null ? DEF_DSEPATH : argPairs.get("dseXmlPath");
		
		Utils.prt(
				"userId:"+this.userId + "," +
						"userId:"+this.userId + "," +
						"meteorUrl:"+this.meteorUrl + "," +
						"meteorPort:"+this.meteorPort + "," +
						"adminEmail:"+this.adminEmail + "," +
						"adminPass:"+this.adminPass + "," +
						"account:"+this.account + "," +
						"strategy:"+this.strategy  + "," +
						"dseXmlPath:"+this.dseXmlPath 
		);
	}
	
}
