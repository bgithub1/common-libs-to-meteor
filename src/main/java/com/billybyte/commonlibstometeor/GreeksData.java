package com.billybyte.commonlibstometeor;

import java.math.BigDecimal;
import java.util.ArrayList;
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
import com.billybyte.marketdata.MarketDataComLib;
import com.billybyte.marketdata.SecDef;
import com.billybyte.meteorjava.MeteorColumnModel;

public class GreeksData extends PositionBaseItem{
	static final DerivativeSensitivityTypeInterface deltaDerSen = new DeltaDerSen();
	static final DerivativeSensitivityTypeInterface gammaDerSen = new GammaDerSen();
	static final DerivativeSensitivityTypeInterface vegaDerSen = new VegaDerSen();
	static final DerivativeSensitivityTypeInterface thetaDerSen = new ThetaDerSen();
	static final DerivativeSensitivityTypeInterface rhoDerSen = new RhoDerSen();
	static final 		Double badRet = -1111111.0;


	private final String type;
	private final String exch;
	private final String underlying;
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
	
	public GreeksData(){
		this(
				null,null,null,null,null,null,
				null,null,null,null,null,null,
				null,null,null,null,null,null,null);
	}
	
	public GreeksData(
			String _id, 
			String userId, 
			String account, 
			String strategy,
			String type,
			String exch,
			String underlying,
			String symbol, 
			String curr,
			Integer year,
			Integer month,
			Integer day, 
			String putCall, 
			BigDecimal strike, 
			Double delta,
			Double gamma, Double vega, Double theta, Double rho) {
		super(_id, userId,account,strategy);
		this.type = type;
		this.exch = exch;
		this.underlying = underlying;
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

	public String getUnderlying() {
		return underlying;
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
		return super.toString() + "," +
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
	
	
	

	@Override
	public <M extends PositionBaseItem> Tuple<List<String>, M> positionBasedItemFromDerivativeReturn(
			Position p,
			SecDef sd,
			Map<DerivativeSensitivityTypeInterface, DerivativeReturn[]> drSenseMap,
			String underlying) {
		
		List<String> problems = new ArrayList<String>();
		String type = sd.getSymbolType().toString();
		String exch = sd.getExchange().toString();
		String symbol = sd.getSymbol();
		String curr = sd.getCurrency().toString();
		int year = sd.getContractYear();
		int month = sd.getContractMonth();
		Integer day = sd.getContractDay();
		day = day==null ? 0 : day;
		String putCall = sd.getRight();
		BigDecimal strike = sd.getStrike();
		
		Double delta = getSense(drSenseMap, deltaDerSen);
		Double gamma = getSense(drSenseMap, gammaDerSen);
		Double vega = getSense(drSenseMap, vegaDerSen);
		Double theta = getSense(drSenseMap, thetaDerSen);
		Double rho = getSense(drSenseMap, deltaDerSen);
		double qty = p.getQty().doubleValue();
		String _id = p.get_id();
		String userId = p.getUserId();
		String account = p.getAccount();
		String strategy = p.getStrategy();
		delta = delta * qty;
		gamma = gamma * qty;
		vega = vega * qty;
		theta = theta * qty;
		rho = rho * qty;

		String under=underlying;
		// only pass symbol
		if(under==null){
			under="";
		}
		
		under = under.split("\\"+MarketDataComLib.DEFAULT_SHORTNAME_SEPARATOR)[0];

		M ret = 
				(M)new GreeksData(_id, userId, account, strategy, type, 
						exch,underlying, symbol, curr, 
						year, month, day, putCall, strike, 
						delta, gamma, vega, theta, rho);
		
		return new Tuple<List<String>, M>(problems, ret);
		
	}
	
	private Double getSense(Map<DerivativeSensitivityTypeInterface, DerivativeReturn[]> drSenseMap, DerivativeSensitivityTypeInterface sense){
		DerivativeReturn[] drArr = drSenseMap.get(sense);
		Double senseValue=badRet;
		if(drArr!=null && drArr.length>0 && drArr[0].isValidReturn()){
			senseValue = drArr[0].getValue().doubleValue();
		}
		return senseValue;
	}
	

	@Override
	public DerivativeSensitivityTypeInterface[] getDseSenseArray(){
		DerivativeSensitivityTypeInterface[] ret = {
				deltaDerSen,gammaDerSen,vegaDerSen,thetaDerSen,rhoDerSen
		};
		return ret;
	}

	@Override
	public MeteorColumnModel[] buildColumnModelArray() {
		MeteorColumnModel accountCm = 
				new MeteorColumnModel("account","account","account",null);
		MeteorColumnModel strategyCm = 
				new MeteorColumnModel("strategy","strategy","strategy",null);
		MeteorColumnModel underlyingCm = 
				new MeteorColumnModel("underlying","underlying","underlying",null);
		MeteorColumnModel symbolCm = 
				new MeteorColumnModel("symbol","symbol","symbol",null);
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
				underlyingCm,symbolCm,
				yearCm,monthCm,dayCm,putCallCm,strikeCm,
				deltaCm,gammaCm,vegaCm,thetaCm,rhoCm
		};
		return ret;
	}

	
}
