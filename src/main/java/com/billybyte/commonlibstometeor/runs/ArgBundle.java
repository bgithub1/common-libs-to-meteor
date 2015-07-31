package com.billybyte.commonlibstometeor.runs;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.URISyntaxException;

import java.util.List;
import java.util.Map;

import org.java_websocket.WebSocket.READYSTATE;

import me.kutrumbos.DdpClient;

import com.billybyte.commonstaticmethods.LoggingUtils;
import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.ui.RedirectedConsoleForJavaProcess;
import com.billybyte.ui.RedirectedConsoleForJavaProcess.ConsoleType;
/**
 * 
 * @author bperlman1
 *
 */
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
	final public List<String> vmArgs;
	final public Boolean restart;
	final public DdpClient restartDdpClient;
	final public Boolean redirect;
	final public Integer redirectXloc;
	final public Integer redirectLength;
	final public Integer redirectYloc;
	final public Integer redirectWidth;
	final public Long millsBeforeRestart;
	final public String logPropertiesPath;
	final public LoggingUtils logger;
	
	private static final String DEF_ADMIN_EMAIL = "admin1@demo.com";
	private static final String DEF_ADMIN_PASS = "admin1";
	private static final String DEF_METURL = "localhost";
	private static final Integer DEF_PORT = 3000;
	private static final String DEF_ACC = "a1";
	private static final String DEF_STRAT = "s1";
	private static final String DEF_DSEPATH = "beans_DefaultDse.xml";
	private static final Integer DEF_RED_XLOC = 1;
	private static final Integer DEF_RED_YLOC = 1;
	private static final Integer DEF_RED_LENGTH = 800;
	private static final Integer DEF_RED_WIDTH = 400;
	private static final Long DEF_MILLSBEFORERESTART = 5000L;
	
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
		this.restart = argPairs.get("restart")==null ? false : new Boolean(argPairs.get("restart"));
		RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
		this.vmArgs = runtimeMxBean.getInputArguments();
		
		// get caller class name for logging utils and for restart
		Class<?> clazz = getCallerClassName();
		this.logPropertiesPath = argPairs.get("logPropertiesPath");
		if(this.logPropertiesPath==null){
			this.logger = new  LoggingUtils(clazz);
		}else{
			this.logger = new LoggingUtils(this.logPropertiesPath,null);
		}
		
		
		this.redirect = argPairs.get("redirect")==null ? false : new Boolean(argPairs.get("redirect"));
		if(this.redirect){
			this.redirectXloc = argPairs.get("redirectXloc")==null ? DEF_RED_XLOC : new Integer(argPairs.get("redirectXloc"));
			this.redirectYloc = argPairs.get("redirectYloc")==null ? DEF_RED_YLOC : new Integer(argPairs.get("redirectYloc"));
			this.redirectLength = argPairs.get("redirectLength")==null ? DEF_RED_LENGTH : new Integer(argPairs.get("redirectLength"));
			this.redirectWidth = argPairs.get("redirectWidth")==null ? DEF_RED_WIDTH : new Integer(argPairs.get("redirectWidth"));
			Utils.prtObMess(clazz, "redirecting console output at : " + "x:" + redirectXloc  + "y:" + redirectYloc  + "len:" + redirectLength  + "wid:" + redirectWidth );
			// redirect console to console gui and error
			// divide the length of each console into 2, so that you have separate views for error and console
			// error on top
			new RedirectedConsoleForJavaProcess(
					this.redirectWidth.intValue(), 
					this.redirectLength.intValue()/2, 
					this.redirectXloc, 
					this.redirectYloc, 
					clazz.getCanonicalName(),
					ConsoleType.SYSTEM_ERR,
					logger);
			new RedirectedConsoleForJavaProcess(
					this.redirectWidth.intValue(), 
					this.redirectLength.intValue()/2, 
					this.redirectXloc, 
					this.redirectYloc+this.redirectLength.intValue()/2, 
					clazz.getCanonicalName(),
					ConsoleType.SYSTEM_OUT,
					logger);
			
			
		}else{
			this.redirectXloc=-1;
			this.redirectYloc=-1;
			this.redirectLength=-1;
			this.redirectWidth=-1;

		}
		
		
		
		Utils.prtObMess(this.getClass(),
						"userId:"+this.userId + "," +
						"meteorUrl:"+this.meteorUrl + "," +
						"meteorPort:"+this.meteorPort + "," +
						"adminEmail:"+this.adminEmail + "," +
						"adminPass:"+this.adminPass + "," +
						"account:"+this.account + "," +
						"strategy:"+this.strategy  + "," +
						"dseXmlPath:"+this.dseXmlPath  + "," +
						"restart:"+this.restart  + "," +
						"redirect:"+this.redirect  + "," +
						"redirectXloc:"+this.redirectXloc  + "," +
						"redirectYloc:"+this.redirectYloc  + "," +
						"redirectLength:"+this.redirectLength  + "," +
						"redirectWidth:"+this.redirectWidth
		);
		
		Utils.prtObMess(this.getClass(), this.vmArgs.toArray(new String[]{}).toString());
		
		DdpClient tempDdpClient = null;
		this.millsBeforeRestart = argPairs.get("millsBeforeRestart")==null ? DEF_MILLSBEFORERESTART : new Long(argPairs.get("millsBeforeRestart"));
		if(restart){
			
			DdpRestartProcessObserver restartObserver = 
					new DdpRestartProcessObserver(clazz, vmArgs.toArray(new String[]{}), args,millsBeforeRestart);
			
			try {
				tempDdpClient = new DdpClient(meteorUrl, meteorPort);
				tempDdpClient.connect();
				try {
					Thread.sleep(500);
					if(tempDdpClient.getReadyState()!=READYSTATE.OPEN){
						Utils.prtObErrMess(clazz, "cam't connect DdpRestart to Meteor.  Attempting restart in 4 seconds");
						Thread.sleep(4000);
						new Thread(new NewProcessLauncher(clazz, vmArgs.toArray(new String[]{}), args)).start();
						throw Utils.IllState(clazz,"throwing exception to stop processing before restart");
					}
				} catch (InterruptedException e) {
					throw Utils.IllState(e);
				}
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			if(tempDdpClient!=null){
				tempDdpClient.addObserver(restartObserver);			
			}
		}
		this.restartDdpClient = tempDdpClient;
	}
	
    private Class<?> getCallerClassName() { 
        StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
        for (int i=1; i<stElements.length; i++) {
            StackTraceElement ste = stElements[i];
            if (!ste.getClassName().equals(this.getClass().getName()) && ste.getClassName().indexOf("java.lang.Thread")!=0) {
                try {
					return Class.forName(ste.getClassName());
				} catch (ClassNotFoundException e) {
					throw Utils.IllState(e);
				}
            }
        }
        return null;
     }
	
}
