package com.billybyte.commonlibstometeor;

import java.util.List;
import java.util.Map;

import com.billybyte.commoncollections.Tuple;
import com.billybyte.dse.outputs.DeltaDerSen;
import com.billybyte.dse.outputs.DerivativeReturn;
import com.billybyte.dse.outputs.DerivativeSensitivityTypeInterface;
import com.billybyte.marketdata.SecDef;
import com.billybyte.meteorjava.MeteorColumnModel;

public class UnitVar extends PositionBaseItem {
	private static final DerivativeSensitivityTypeInterface deltaDerSen = new DeltaDerSen();
//	private static final 		Double badRet = -1111111.0;
	private final String underlying;
	private final Double var;
	
	public UnitVar(){
		this(null,null,null,null,null,null);
	}
	
	public UnitVar(String _id, String userId, String account, String strategy,
			String underlying, Double var) {
		super(_id, userId, account, strategy);
		this.underlying = underlying;
		this.var = var;
	}
	
	

	public String getUnderlying() {
		return underlying;
	}



	public Double getVar() {
		return var;
	}



	@Override
	public MeteorColumnModel[] buildColumnModelArray() {
		MeteorColumnModel accountCm = 
				new MeteorColumnModel("account","account","account",null);
		MeteorColumnModel strategyCm = 
				new MeteorColumnModel("strategy","strategy","strategy",null);
		MeteorColumnModel underlyingCm = 
				new MeteorColumnModel("underlying","underlying","underlying",null);
		MeteorColumnModel varCm = 
				new MeteorColumnModel("var","var","var",new String[]{"var"});
		MeteorColumnModel[] ret = {
				accountCm,strategyCm,
				underlyingCm,varCm
		};
		return ret;
	}

	@Override
	public DerivativeSensitivityTypeInterface[] getDseSenseArray() {
		DerivativeSensitivityTypeInterface[] ret = {
				deltaDerSen
		};
		return ret;
	}

	// !!!! this is not used right now because the method ProcessMeteorPositionChanges<UnitVar>.processSensitivitiesPerUserId
	//        is overwritten in the static class RunUnitVarFromMeteorPositionChange.ProcessUnitVarFromMeteorPositionChanges
	@Override
//	public <M extends PositionBaseItem> Tuple<List<String>, M> positionBasedItemFromDerivativeReturn(
	public <M extends PositionBaseItem> Tuple<List<String>, List<M>> positionBasedItemFromDerivativeReturn(
			Position p,
			SecDef sd,
			Map<DerivativeSensitivityTypeInterface, DerivativeReturn[]> drSenseMap,
			List<SecDef> underlyingSds) {
		return null;
	}
	
	
	


}
