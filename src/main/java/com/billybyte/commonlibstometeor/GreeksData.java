package com.billybyte.commonlibstometeor;

import java.math.BigDecimal;
import java.text.DecimalFormat;
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
	static final DecimalFormat INTDF = new DecimalFormat("0000");


	private final String type;
	private final String exch;
	private final String underlying;
//	private final String symbol;
	private final String curr;
	private final Integer year;
	private final Integer month;
	private final Integer day;
//	private final String putCall;
//	private final BigDecimal strike;
	private final Double delta;
	private final Double gamma;
	private final Double vega;
	private final Double theta;
	private final Double rho;
	
//	public GreeksData(){
//		this(
//				null,null,null,null,null,null,
//				null,null,null,null,null,null,
//				null,null,null,null,null,null,null);
//	}

	public GreeksData(){
		this(
				null,null,null,null,null,null,
				null,null,null,null,null,null,
				null,null,null,null);
	}

	
	public GreeksData(
			String _id, 
			String userId, 
			String account, 
			String strategy,
			String type,
			String exch,
			String underlying,
//			String symbol, 
			String curr,
			Integer year,
			Integer month,
			Integer day, 
//			String putCall, 
//			BigDecimal strike, 
			Double delta,
			Double gamma, Double vega, Double theta, Double rho) {
		super(_id, userId,account,strategy);
		this.type = type;
		this.exch = exch;
		this.underlying = underlying;
//		this.symbol = symbol;
		this.curr = curr;
		this.year = year;
		this.month = month;
		this.day = day;
//		this.putCall = putCall;
//		this.strike = strike;
		this.delta = delta;
		this.gamma = gamma;
		this.vega = vega;
		this.theta = theta;
		this.rho = rho;
				
	}

	public String getUnderlying() {
		return underlying;
	}

//	public String getSymbol() {
//		return symbol;
//	}

	public Integer getYear() {
		return year;
	}

	public Integer getMonth() {
		return month;
	}

	public Integer getDay() {
		return day;
	}

//	public String getPutCall() {
//		return putCall;
//	}
//
//	public BigDecimal getStrike() {
//		return strike;
//	}

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
//				symbol + "," + 
				curr+ "," + 
				year+ "," + 
				month + "," + 
				day + "," + 
//				putCall + "," + 
//				strike + "," + 
				delta + ","
				+ gamma + "," + vega + "," + theta + ","
				+ rho;
	}
	
	
	

	@Override
//	public <M extends PositionBaseItem> Tuple<List<String>, M> positionBasedItemFromDerivativeReturn(
	public <M extends PositionBaseItem> Tuple<List<String>, List<M>> positionBasedItemFromDerivativeReturn(
			Position p,
			SecDef sd,
			Map<DerivativeSensitivityTypeInterface, DerivativeReturn[]> drSenseMap,
			List<SecDef> underlyingSds) {
		
		List<String> problems = new ArrayList<String>();
		List<M> retList = new ArrayList<M>();
		double qty = p.getQty().doubleValue();
		String _id = p.get_id();
		String userId = p.getUserId();
		String account = p.getAccount();
		String strategy = p.getStrategy();
		Double[] delta = getSense(underlyingSds,drSenseMap, deltaDerSen,qty);
		Double[] gamma = getSense(underlyingSds,drSenseMap, gammaDerSen,qty);
		Double[] vega = getSense(underlyingSds,drSenseMap, vegaDerSen,qty);
		Double[] theta = getSense(underlyingSds,drSenseMap, thetaDerSen,qty);
		Double[] rho = getSense(underlyingSds,drSenseMap, deltaDerSen,qty);

		for(int i = 0;i<underlyingSds.size();i++){
			SecDef sdUnder = underlyingSds.get(i);
			String type = sdUnder.getSymbolType().toString();
			String exch = sdUnder.getExchange().toString();
			String symbol = sdUnder.getSymbol();
			String under = symbol;
			String curr = sdUnder.getCurrency().toString();
			int year = sdUnder.getContractYear();
			int month = sdUnder.getContractMonth();
			Integer day = sdUnder.getContractDay();
			day = day==null ? 0 : day;
			String putCall = sdUnder.getRight();
			BigDecimal strike = sdUnder.getStrike();
			_id = _id+INTDF.format(i);
			
//			M ret = 
//					(M)new GreeksData(_id, userId, account, strategy, type, 
//							exch,under, symbol, curr, 
//							year, month, day, putCall, strike, 
//							delta[i], gamma[i], vega[i], theta[i], rho[i]);
			M ret = 
					(M)new GreeksData(_id, userId, account, strategy, type, 
							exch,under, curr, 
							year, month, day,  
							delta[i], gamma[i], vega[i], theta[i], rho[i]);
			retList.add(ret);
 
		}
		
		return new Tuple<List<String>, List<M>>(problems, retList);
		
	}
	
	private Double[] getSense(List<SecDef> underlyingSds,
			Map<DerivativeSensitivityTypeInterface, DerivativeReturn[]> drSenseMap, 
			DerivativeSensitivityTypeInterface sense,
			double qty){
		DerivativeReturn[] drArr = drSenseMap.get(sense);
		Double[] senseValue= null;
		if(drArr!=null && drArr.length>0){
			senseValue = new Double[drArr.length];
			for(int i = 0;i<drArr.length;i++){
				senseValue[i] = badRet;
				if(drArr[i].isValidReturn()){
					senseValue[i] = drArr[i].getValue().doubleValue()*qty;
				}
			}
		}

		// if any of the above sensitivities have only one array element, while the number
		//   of underlyings is greater than 1, divide that sensitivity by the number of underlyings and create a
		//   psuedo-sensitivity for each underlying.  This is especially true of things like theta, in which the dse
		//   returns only one value for multiple underlyings
		int underLength = underlyingSds.size();
		if(senseValue.length<underLength){
			// first sum up old sensevalues
			Double totalSenseValue = 0.0;
			for(Double partialSenseValue : senseValue){
				totalSenseValue += partialSenseValue;
			}
			// now redistribute the total over all underlyings
			senseValue = new Double[underLength];
			for(int i = 0;i<underLength;i++){
				senseValue[i] = totalSenseValue/underLength;
			}
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
//		MeteorColumnModel symbolCm = 
//				new MeteorColumnModel("symbol","symbol","symbol",null);
		MeteorColumnModel yearCm = 
				new MeteorColumnModel("year","year","year",null);
		MeteorColumnModel monthCm = 
				new MeteorColumnModel("month","month","month",null);
		MeteorColumnModel dayCm = 
				new MeteorColumnModel("day","day","day",null);
//		MeteorColumnModel putCallCm = 
//				new MeteorColumnModel("putCall","putCall","putCall",null);
//		MeteorColumnModel strikeCm = 
//				new MeteorColumnModel("strike","strike","strike",null);
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
	
//		MeteorColumnModel[] ret = {
//				accountCm,strategyCm,
//				underlyingCm,symbolCm,
//				yearCm,monthCm,dayCm,putCallCm,strikeCm,
//				deltaCm,gammaCm,vegaCm,thetaCm,rhoCm
//		};
		MeteorColumnModel[] ret = {
				accountCm,strategyCm,
				underlyingCm,
				yearCm,monthCm,dayCm,
				deltaCm,gammaCm,vegaCm,thetaCm,rhoCm
		};
		return ret;
	}

	
}
