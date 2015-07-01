package com.billybyte.commonlibstometeor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.billybyte.marketdata.SecDef;
import com.billybyte.marketdata.SecDefQueryAllMarkets;
import com.billybyte.meteorjava.JsonNestedList;
import com.billybyte.meteorjava.MeteorBaseListItem;
import com.billybyte.meteorjava.MeteorColumnModel;
import com.billybyte.meteorjava.MeteorTableModel;
import com.billybyte.meteorjava.MeteorValidator;

public class Position extends MeteorBaseListItem {
	private static final SecDefQueryAllMarkets sdQuery = 
			new SecDefQueryAllMarkets();
	
	/**
	 * 
	 * @param _id
	 * @param userId
	 * @param account
	 * @param strategy
	 * @param symbol
	 * @param year
	 * @param monthDay
	 * @param putCall
	 * @param strike
	 * @param qty
	 */
	public Position(String _id, String userId, String account, String strategy,
			String symbol, Integer year, Integer monthDay, String putCall,
			BigDecimal strike, BigDecimal qty) {
		super(_id, userId);
		this.account = account;
		this.strategy = strategy;
		this.symbol = symbol;
		this.year = year;
		this.monthDay = monthDay;
		this.putCall = putCall;
		this.strike = strike;
		this.qty = qty;
	}
	private final String account;
	private final String strategy;
	private final String symbol;
	private final Integer year;
	private final Integer monthDay;
	private final String putCall;
	private final BigDecimal strike;
	private final BigDecimal qty;
	public String getAccount() {
		return account;
	}
	public String getStrategy() {
		return strategy;
	}
	public String getSymbol() {
		return symbol;
	}
	public Integer getYear() {
		return year;
	}
	public Integer getMonthDay() {
		return monthDay;
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
	
	/**
	 * Build an array of MeteorColumnModel to display in Meteor
	 * @return MeteorColumnModel[]
	 */
	public static final MeteorColumnModel[] buildMetColModelArray(){

//		MeteorColumnModel idCm = 
//				new MeteorColumnModel("_id","_id","_id",null);
//		MeteorColumnModel userIdCm = 
//				new MeteorColumnModel("userId","userId","userId",null);
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
				arr[3] = "-";
				arr[4] = "-";
				arr[5] = "-";
				arr[6] = "-";
				arr[7] = "-";
				arr[8] = "-";
			}else if(type.compareTo("FUT")==0){
				arr = new String[9];
				arr[0]=type;
				arr[1] = exch;
				arr[2] = prod;
				arr[3] = snParts[3];
				arr[4] = snParts[4].substring(0,4);
				arr[5] = snParts[4].substring(4,6);
				arr[6] = "-";
				arr[7] = "-";
				arr[8] = "-";
			}else if(type.compareTo("OPT")==0 || type.compareTo("FOP")==0  ){
//				SecDef sd = sdQuery.get(sn, 1, TimeUnit.SECONDS);
				
				arr = new String[9];
				arr[0]=type;
				arr[1] = exch;
				arr[2] = prod;
				arr[3] = snParts[3];
				arr[4] = snParts[4].substring(0,4);
				arr[5] = snParts[4].substring(4,6);
//				if(sd.getContractDay()==null){
//					arr[6] = sd.getExpiryDay()+"";
//				}else{
//					arr[6] = sd.getContractDay().toString();
//				}
				arr[6] = "01";
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

}
