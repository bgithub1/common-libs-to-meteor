package com.billybyte.commonlibstometeor;

import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import misc.PosClDetailed;

import com.billybyte.marketdata.MarketDataComLib;
import com.billybyte.marketdata.SecDef;
import com.billybyte.marketdata.SecDefQueryAllMarkets;
import com.billybyte.meteorjava.JsonNestedList;
import com.billybyte.meteorjava.MeteorBaseListItem;
import com.billybyte.meteorjava.MeteorColumnModel;
import com.billybyte.meteorjava.MeteorListSendReceive;
import com.billybyte.meteorjava.MeteorTableModel;
import com.billybyte.meteorjava.MeteorValidator;
import com.billybyte.meteorjava.staticmethods.Utils;

public class Position extends MeteorBaseListItem {
	private final String account;
	private final String strategy;
	private final String type;
	private final String exch;
	private final String symbol;
	private final String curr;
	private final Integer year;
	private final Integer month;
	private final Integer day;
	private final String putCall;
	private final BigDecimal strike;
	private final BigDecimal qty;

	
	
	private static final SecDefQueryAllMarkets sdQuery = 
			new SecDefQueryAllMarkets();
	

	
	public Position(String _id, String userId, String account, String strategy,
			String type, String exch, String symbol, String curr, Integer year,
			Integer month, Integer day, String putCall, BigDecimal strike,
			BigDecimal qty) {
		super(_id, userId);
		this.account = account;
		this.strategy = strategy;
		this.type = type;
		this.exch = exch;
		this.symbol = symbol;
		this.curr = curr;
		this.year = year;
		this.month = month;
		this.day = day;
		this.putCall = putCall;
		this.strike = strike;
		this.qty = qty;
	}

	
	
	public String getAccount() {
		return account;
	}



	public String getStrategy() {
		return strategy;
	}



	public String getType() {
		return type;
	}



	public String getExch() {
		return exch;
	}



	public String getSymbol() {
		return symbol;
	}



	public String getCurr() {
		return curr;
	}



	public Integer getYear() {
		return year;
	}



	public Integer getMonth() {
		return month;
	}



	public Integer getDay() {
		return day;
	}



	public String getPutCall() {
		return putCall;
	}



	public BigDecimal getStrike() {
		return strike;
	}



	public BigDecimal getQty() {
		return qty;
	}

	public String getShortName(){
		String sep = MarketDataComLib.DEFAULT_SHORTNAME_SEPARATOR;
		String pc = getPutCall();
		BigDecimal strike = getStrike();
		if(pc!=null){
			if(pc.compareTo("F")==0 || pc.compareTo("S")==0) {
				pc=null;
				strike = BigDecimal.ZERO;
			}
			
		}
		return getSymbol() + sep +
				getType() + sep +
				getExch() + sep +
				getCurr() + sep +
				(getYear()*100+getMonth()) +
				(strike.compareTo(BigDecimal.ZERO)>0 ?  sep +getPutCall()+sep+getStrike() : "");
	}

	/**
	 * Build an array of MeteorColumnModel to display in Meteor
	 * @return MeteorColumnModel[]
	 */
	public static final MeteorColumnModel[] buildMetColModelArray(){

		MeteorColumnModel accountCm = 
				new MeteorColumnModel("account","account","account",null);
		MeteorColumnModel strategyCm = 
				new MeteorColumnModel("strategy","strategy","strategy",null);
		MeteorColumnModel typeCm = 
				new MeteorColumnModel("type","type","type",null);
		MeteorColumnModel exchCm = 
				new MeteorColumnModel("exch","exch","exch",null);
		MeteorColumnModel symbolCm = 
				new MeteorColumnModel("symbol","symbol","symbol",null);
		MeteorColumnModel currCm = 
				new MeteorColumnModel("curr","curr","curr",null);
		MeteorColumnModel yearCm = 
				new MeteorColumnModel("year","year","year",null);
		MeteorColumnModel monthCm = 
				new MeteorColumnModel("month","month","month",null);
		MeteorColumnModel dayCm = 
				new MeteorColumnModel("day","day","day",null);
		MeteorColumnModel putCallCm = 
				new MeteorColumnModel("putCall","putCall","putCall",null);
		MeteorColumnModel strikeCm = 
				new MeteorColumnModel("strike","strike","strike",null);
		MeteorColumnModel qtyCm = 
				new MeteorColumnModel("qty","qty","qty",new String[]{"qty"});
	
		MeteorColumnModel[] ret = {
				accountCm,strategyCm,
				typeCm,exchCm,symbolCm,currCm,
				yearCm,monthCm,dayCm,putCallCm,strikeCm,
				qtyCm
		};
		return ret;
	}
	
	@SuppressWarnings("rawtypes")
	public static Map buildNestedMapFromShortNames(Set<String> shortNameSet){
		List<String[]> snList = new ArrayList<String[]>();
		Map<String,SecDef> sdMap = new HashMap<String, SecDef>();
		
		for(String sn : shortNameSet){
			String[] snParts = sn.split("\\.");
			String type = snParts[1];
			String exch = snParts[2];
			String prod = snParts[0];
			String[] arr = null;
			if(type.compareTo("STK")==0){
				arr = new String[9];
				arr[0]=type;
				arr[1] = exch;
				arr[2] = prod;
				arr[3] = "-";// curr
				arr[4] = "0";//year
				arr[5] = "0";//month
				arr[6] = "0";//day
				arr[7] = "S";//pc
				arr[8] = "0";//strike
			}else if(type.compareTo("FUT")==0){
				arr = new String[9];
				arr[0]=type;
				arr[1] = exch;
				arr[2] = prod;
				arr[3] = snParts[3];
				arr[4] = snParts[4].substring(0,4);
				arr[5] = snParts[4].substring(4,6);
				String sdMapKey = arr[0]+"."+arr[1]+"."+arr[2]+"."+arr[3]+"."+arr[4]+"."+arr[5];
				SecDef sd = sdMap.get(sdMapKey);
				if(sd==null){
					sd = sdQuery.get(sn, 1, TimeUnit.SECONDS);
					sdMap.put(sdMapKey, sd);
				}
				
				if(sd.getContractDay()==null){
					arr[6] = sd.getExpiryDay()+"";
				}else{
					arr[6] = sd.getContractDay().toString();
				}
//				arr[6] = "01";
				arr[7] = "F";
				arr[8] = "0";
			}else if(type.compareTo("OPT")==0 || type.compareTo("FOP")==0  ){				
				arr = new String[9];
				arr[0]=type;
				arr[1] = exch;
				arr[2] = prod;
				arr[3] = snParts[3];
				arr[4] = snParts[4].substring(0,4);
				arr[5] = snParts[4].substring(4,6);
				String sdMapKey = arr[0]+"."+arr[1]+"."+arr[2]+"."+arr[3]+"."+arr[4]+"."+arr[5];
				SecDef sd = sdMap.get(sdMapKey);
				if(sd==null){
					sd = sdQuery.get(sn, 1, TimeUnit.SECONDS);
					sdMap.put(sdMapKey, sd);
				}
				
				if(sd.getContractDay()==null){
					arr[6] = sd.getExpiryDay()+"";
				}else{
					arr[6] = sd.getContractDay().toString();
				}
//				arr[6] = "01";
				arr[7] = snParts[5];
				arr[8] = snParts[6]+ (snParts.length>7 ? "."+snParts[7] : "");
			}
			if(arr!=null){
				snList.add(arr);
			}
		}
		
		
		Map nestedMap = JsonNestedList.buildJnestMap(snList);
		return nestedMap;
	}

	public static final MeteorValidator buildValidator(Set<String> shortNameSet){
		@SuppressWarnings("rawtypes")
		Map jnestMap = buildNestedMapFromShortNames(shortNameSet);
		Class<?> classOfDataToBeValidated = Position.class;
		List<String> dependentFieldValidationOrderList = 
				Arrays.asList(new String[]{
						"type","exch","symbol","curr","year","month","day","putCall","strike"
				});
		Map<String, List<String>> independentFields = new HashMap<String, List<String>>();
		
		List<String> freeFields = Arrays.asList(new String[]{
				"account","strategy","qty"
		});
		
		MeteorValidator ret = 
				new MeteorValidator(
						classOfDataToBeValidated, jnestMap, 
						dependentFieldValidationOrderList, 
						independentFields, freeFields);
		return ret;
	}
	
	public static final MeteorTableModel buildPositionTableModel(){
		MeteorTableModel tableModel = 
				new MeteorTableModel(
						Position.class,"Position",Position.class.getName(), 
						Position.buildMetColModelArray());
		return tableModel;
	}

	public static final List<Position> getPositionFromMeteor(
			String meteorUrl,
			Integer meteorPort,
			String adminEmail,
			String adminPass,
			Map<String,String> selectorMap){
		MeteorListSendReceive<Position> mlsr = null;
		try {
			mlsr =
					new MeteorListSendReceive<Position>(100, 
							Position.class, meteorUrl, meteorPort, 
							adminEmail,adminPass,"", "", "tester");
		} catch (URISyntaxException e) {
			throw Utils.IllState(e);
		}

		List<Position> positionList = mlsr.getList(selectorMap);
		return positionList;
		
	}

	

	@Override
	public String toString() {
		return super.toString() + "," +account + ", " + strategy + ", " + type + ", " + exch + ", "
				+ symbol + ", " + curr + ", " + year + ", " + month + ", "
				+ day + ", " + putCall + ", " + strike + ", " + qty;
	}

	
	
	
	
}
