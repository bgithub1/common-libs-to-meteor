package com.billybyte.commonlibstometeor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.billybyte.commoncollections.Tuple;
import com.billybyte.dse.outputs.DeltaDerSen;
import com.billybyte.dse.outputs.DerivativeReturn;
import com.billybyte.dse.outputs.DerivativeSensitivityTypeInterface;
import com.billybyte.dse.outputs.GammaDerSen;
import com.billybyte.dse.outputs.RhoDerSen;
import com.billybyte.dse.outputs.ThetaDerSen;
import com.billybyte.dse.outputs.VegaDerSen;
import com.billybyte.marketdata.SecDef;
import com.billybyte.meteorjava.MeteorBaseListItem;
import com.billybyte.meteorjava.MeteorColumnModel;
import com.billybyte.meteorjava.MeteorValidator;

public class GreeksData extends MeteorBaseListItem{
	private static final DerivativeSensitivityTypeInterface deltaDerSen = new DeltaDerSen();
	private static final DerivativeSensitivityTypeInterface gammaDerSen = new GammaDerSen();
	private static final DerivativeSensitivityTypeInterface vegaDerSen = new VegaDerSen();
	private static final DerivativeSensitivityTypeInterface thetaDerSen = new ThetaDerSen();
	private static final DerivativeSensitivityTypeInterface rhoDerSen = new RhoDerSen();
	

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
	private final Double delta;
	private final Double gamma;
	private final Double vega;
	private final Double theta;
	private final Double rho;
	
	
	public GreeksData(String _id, String userId, 
			String account, 
			String strategy,
			String type,
			String exch,
			String symbol, 
			String curr,
			Integer year,
			Integer month,
			Integer day, 
			String putCall, 
			BigDecimal strike, 
			Double delta,
			Double gamma, Double vega, Double theta, Double rho) {
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
		this.delta = delta;
		this.gamma = gamma;
		this.vega = vega;
		this.theta = theta;
		this.rho = rho;
				
	}

	public String getSymbol() {
		return symbol;
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

	public Double getDelta() {
		return delta;
	}

	public Double getGamma() {
		return gamma;
	}

	public Double getVega() {
		return vega;
	}

	public Double getTheta() {
		return theta;
	}

	public Double getRho() {
		return rho;
	}

	@Override
	public String toString() {
		return account + "," + 
				strategy + "," + 
				type + "," + 
				exch + "," + 
				symbol + "," + 
				curr+ "," + 
				year+ "," + 
				month + "," + 
				day + "," + 
				putCall + "," + 
				strike + "," + 
				delta + ","
				+ gamma + "," + vega + "," + theta + ","
				+ rho;
	}
	
	
	public static final Tuple<List<String>,GreeksData> fromDerivativeReturn(
			double qty,
			String _id,
			String userId,
			String account,
			String strategy,
			SecDef sd,
			Double badRet,
			int precision,
			List<DerivativeReturn[]> drArrList){
		
		double multiplier = Math.pow(10,precision);
		List<String> problems = new ArrayList<String>();
		String type = sd.getSymbolType().toString();
		String exch = sd.getExchange().toString();
		String symbol = sd.getSymbol();
		String curr = sd.getCurrency().toString();
		int year = sd.getContractYear();
		int month = sd.getContractMonth();
		Integer day = sd.getContractDay();
		day = day==null ? 0 : day;
//		int monthDay = month*100+day;
		String putCall = sd.getRight();
		BigDecimal strike = sd.getStrike();
		Double delta=badRet;
		Double gamma=badRet;
		Double vega=badRet;
		Double theta=badRet;
		Double rho=badRet;
		for(DerivativeReturn[] drArr : drArrList){
			if(drArr==null || drArr.length<1){
				problems.add(sd.getShortName()+" null return from Dse");
				continue;
			}
			DerivativeReturn dr = drArr[0];
			if(!dr.isValidReturn()){
				problems.add(sd.getShortName()+" : " + dr.getException().getMessage());
				continue;
			}
			DerivativeSensitivityTypeInterface sense = dr.getSensitivityId();
			if(sense.compareTo(deltaDerSen)==0){
				delta = dr.getValue().doubleValue();
				delta = Math.round(delta*multiplier)/multiplier;
			}
			if(sense.compareTo(gammaDerSen)==0){
				gamma = dr.getValue().doubleValue();
				gamma = Math.round(gamma*multiplier)/multiplier;
			}
			if(sense.compareTo(vegaDerSen)==0){
				vega = dr.getValue().doubleValue();
				vega = Math.round(vega*multiplier)/multiplier;
			}
			if(sense.compareTo(thetaDerSen)==0){
				theta = dr.getValue().doubleValue();
				theta = Math.round(theta*multiplier)/multiplier;
			}
			if(sense.compareTo(rhoDerSen)==0){
				rho = dr.getValue().doubleValue();
				rho = Math.round(rho*multiplier)/multiplier;
			}
		}
		delta = delta * qty;
		gamma = gamma * qty;
		vega = vega * qty;
		theta = theta * qty;
		rho = rho * qty;
		
		GreeksData ret = 
				new GreeksData(_id, userId, account, strategy, type, exch, symbol, curr, year, month, day, putCall, strike, delta, gamma, vega, theta, rho);
		return new Tuple<List<String>, GreeksData>(problems, ret);
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
		MeteorColumnModel deltaCm = 
				new MeteorColumnModel("delta","delta","delta",new String[]{"delta"});
		MeteorColumnModel gammaCm = 
				new MeteorColumnModel("gamma","gamma","gamma",new String[]{"gamma"});
		MeteorColumnModel vegaCm = 
				new MeteorColumnModel("vega","vega","vega",new String[]{"vega"});
		MeteorColumnModel thetaCm = 
				new MeteorColumnModel("theta","theta","theta",new String[]{"theta"});
		MeteorColumnModel rhoCm = 
				new MeteorColumnModel("rho","rho","rho",new String[]{"rho"});
	
		MeteorColumnModel[] ret = {
				accountCm,strategyCm,
				typeCm,exchCm,symbolCm,currCm,
				yearCm,monthCm,dayCm,putCallCm,strikeCm,
				deltaCm,gammaCm,vegaCm,thetaCm,rhoCm
		};
		return ret;
	}
	
	
	@SuppressWarnings("rawtypes")
	public static final MeteorValidator buildValidator(){
		Map jnestMap = null;
		Class<?> classOfDataToBeValidated = GreeksData.class;
		List<String> dependentFieldValidationOrderList = new ArrayList<String>();
		Map<String, List<String>> independentFields = new HashMap<String, List<String>>();
		List<String> freeFields = new ArrayList<String>();
		MeteorValidator ret = 
				new MeteorValidator(
						classOfDataToBeValidated, jnestMap, 
						dependentFieldValidationOrderList, 
						independentFields, freeFields);
		return ret;
		
	}
	
	
	
	public static final DerivativeSensitivityTypeInterface[] buildDseSenseArray(){
		DerivativeSensitivityTypeInterface[] ret = {
				deltaDerSen,gammaDerSen,vegaDerSen,thetaDerSen,rhoDerSen
		};
		return ret;
	}

	
}
