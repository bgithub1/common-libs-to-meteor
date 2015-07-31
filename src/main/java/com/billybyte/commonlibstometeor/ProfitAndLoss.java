package com.billybyte.commonlibstometeor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.billybyte.commoncollections.Tuple;
import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.dse.outputs.DerivativeReturn;
import com.billybyte.dse.outputs.DerivativeSensitivityTypeInterface;
import com.billybyte.dse.outputs.OptPriceDerSen;
import com.billybyte.marketdata.MarketDataComLib;
import com.billybyte.marketdata.SecDef;
import com.billybyte.meteorjava.MeteorColumnModel;

/**
 * Calculate P & L from a position
 * @author bperlman1
 *
 */
public class ProfitAndLoss extends Position {
	private static final BigDecimal BAD_RET_VALUE = new BigDecimal("-111111111");
	private final BigDecimal pl;
	private final String underlying;
	private final static DerivativeSensitivityTypeInterface optPriceDerSen = 
			new OptPriceDerSen();
	
	public ProfitAndLoss(){
		super(null,null,null,null,
				null,null,null,null,
				null,null,null,null,
				null,null,null);
		this.pl = null;
		this.underlying = null;
	}
	
	public ProfitAndLoss(String _id, String userId, String account,
			String strategy, String type, String exch, String symbol,
			String curr, Integer year, Integer month, Integer day,
			String putCall, BigDecimal strike, BigDecimal price, BigDecimal qty, BigDecimal pl,String underlying) {
		super(_id, userId, account, strategy, type, exch, symbol, curr, year,
				month, day, putCall, strike, price,qty);
		this.pl = pl;
		this.underlying = underlying;
	}
	
	

	public BigDecimal getPl() {
		return pl;
	}



	public String getUnderlying() {
		return underlying;
	}

	@Override
	public String toString() {
		return getShortName() +  ", " +
				getAccount() + ", " + 
				getStrategy() + ", "+ 
				pl + ", " + 
				getQty() + ", "  + 
				get_id() + ", " + 
				getUserId();
	}


	/**
	 * Build an array of MeteorColumnModel to display in Meteor
	 * @return MeteorColumnModel[]
	 */
	private static final MeteorColumnModel[] buildProfitAndLossColModelArray(){

		MeteorColumnModel accountCm = 
				new MeteorColumnModel("account","account","account",null);
		MeteorColumnModel strategyCm = 
				new MeteorColumnModel("strategy","strategy","strategy",null);
		MeteorColumnModel underCm = 
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
		MeteorColumnModel plCm = 
				new MeteorColumnModel("pl","pl","pl",new String[]{"pl"});
	
		MeteorColumnModel[] ret = {
				accountCm,strategyCm,
				underCm,symbolCm,
				yearCm,monthCm,dayCm,putCallCm,strikeCm,
				plCm
		};
		return ret;
	}
	

	@Override
	public MeteorColumnModel[] buildColumnModelArray() {
		return buildProfitAndLossColModelArray();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <M extends PositionBaseItem> Tuple<List<String>, M> positionBasedItemFromDerivativeReturn(
			Position p,
			SecDef sd,
			Map<DerivativeSensitivityTypeInterface, DerivativeReturn[]> drSenseMap,
			List<SecDef> underlyingSds) {
		
		List<String> problems = new ArrayList<String>(); // init list of problems
		DerivativeReturn[] drArr= drSenseMap.get(optPriceDerSen); // get option price
		M m = null;
		// init p & l
		BigDecimal pl = BAD_RET_VALUE;
		boolean allgood = true;
		// initial check for bad dse return
		if(drArr==null || drArr.length<1){
			problems.add(sd.getShortName() + " has no DerivativeReturn array from DerivativeSetEngine");
			allgood = false;
		}else if(!drArr[0].isValidReturn()){
			problems.add(sd.getShortName() + " : " + drArr[0].getException().getMessage());
			allgood = false;
		}
		
		try {
			Double drDoubleValue = drArr[0].getValue().doubleValue();
			if(Double.isNaN(drDoubleValue) || Double.isInfinite(drDoubleValue)){
				problems.add(sd.getShortName() + " has dse price value that is NaN or infinate. Value="+drArr[0].toString());
				allgood = false;
			}
		} catch (Exception e) {
			problems.add(sd.getShortName() + " has dse price value that is NaN or infinate. Value="+drArr[0].toString());
			allgood = false;
		}
		
		
		BigDecimal price = p.getPrice();
		if(price==null){
			problems.add(p.toString() +  " has no price field");
			price = BAD_RET_VALUE;
			allgood = false;
		}
		
		String under="null";
		// check underlyings
		if(underlyingSds==null || underlyingSds.size()<1){
			problems.add(sd.getShortName() + " no underlyings passed to positionBasedItemFromDerivativeReturn");
			allgood = false;
		}else{
			// only pass symbol
			SecDef underSds0 =underlyingSds.get(0);
			if(underSds0==null){
				problems.add(sd.getShortName() + " no underlyings passed to positionBasedItemFromDerivativeReturn");
				allgood = false;
			}else{
				under = underSds0.getShortName();
				under = under.split("\\"+MarketDataComLib.DEFAULT_SHORTNAME_SEPARATOR)[0];
				
			}
			
		}
		
		if(allgood){
			pl = new BigDecimal(drArr[0].getValue().doubleValue()).subtract(price);
			pl = pl.multiply(p.getQty());			
		}else{
			for(String problem :problems){
				Utils.prtObErrMess(this.getClass(), problem);				
			}
		}
		
		
		
		m = (M) new ProfitAndLoss(p.get_id(), p.getUserId(), 
				p.getAccount(), p.getStrategy(), p.getType(), 
				p.getExch(), p.getSymbol(), p.getCurr(), p.getYear(),
				p.getMonth(), p.getDay(), p.getPutCall(), p.getStrike(), 
				p.getPrice(),p.getQty(), pl,under);
		
		return new Tuple<List<String>, M>(problems, m);
	}

	@Override
	public DerivativeSensitivityTypeInterface[] getDseSenseArray() {
		DerivativeSensitivityTypeInterface[] ret = 
		 super.getDseSenseArray();
		return ret;
	}



	
	
}
