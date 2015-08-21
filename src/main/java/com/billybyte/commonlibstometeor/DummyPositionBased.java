package com.billybyte.commonlibstometeor;

import java.util.List;
import java.util.Map;

import com.billybyte.commoncollections.Tuple;
import com.billybyte.dse.outputs.DerivativeReturn;
import com.billybyte.dse.outputs.DerivativeSensitivityTypeInterface;
import com.billybyte.marketdata.SecDef;
import com.billybyte.meteorjava.MeteorColumnModel;

public class DummyPositionBased  extends PositionBaseItem{

	public DummyPositionBased() {
		super(null,null,null,null);
		// not used
	}

	@Override
	public MeteorColumnModel[] buildColumnModelArray() {
		return null;
	}

	@Override
	public DerivativeSensitivityTypeInterface[] getDseSenseArray() {
		return null;
	}

	@Override
	public <M extends PositionBaseItem> Tuple<List<String>, List<M>> positionBasedItemFromDerivativeReturn(
			Position p,
			SecDef sd,
			Map<DerivativeSensitivityTypeInterface, DerivativeReturn[]> drSenseMap,
			List<SecDef> underlyingSds) {
		return null;
	}
	
}
