package com.billybyte.commonlibstometeor;
// 14:29 - expected 12 pm tomorrow (9 hours total 5 today and 4 tomorrow)
import java.util.List;
import java.util.Map;

import com.billybyte.commoncollections.Tuple;
import com.billybyte.dse.outputs.DerivativeReturn;
import com.billybyte.dse.outputs.DerivativeSensitivityTypeInterface;
import com.billybyte.marketdata.SecDef;
import com.billybyte.meteorjava.MeteorColumnModel;

public class GreekInputsData extends PositionBaseItem{


	private final String input01;
	private final String input02;
	private final String input03;
	private final String input04;
	private final String input05;
	private final String input06;
	private final String input07;
	private final String input08;
	private final String input09;
	private final String input10;
	private final String input11;
	private final String input12;
	private final String input13;
	private final String input14;
	private final String input15;
	
	
	public GreekInputsData(String _id, String userId, String account,
			String strategy, String[] inputs) {
		super(_id, userId, account, strategy);
		this.input01 = inputsFromConstructor(inputs,0);
		this.input02 = inputsFromConstructor(inputs,1);
		this.input03 = inputsFromConstructor(inputs,2);
		this.input04 = inputsFromConstructor(inputs,3);
		this.input05 = inputsFromConstructor(inputs,4);
		this.input06 = inputsFromConstructor(inputs,5);
		this.input07 = inputsFromConstructor(inputs,6);
		this.input08 = inputsFromConstructor(inputs,7);
		this.input09 = inputsFromConstructor(inputs,8);
		this.input10 = inputsFromConstructor(inputs,9);
		this.input11 = inputsFromConstructor(inputs,10);
		this.input12 = inputsFromConstructor(inputs,11);
		this.input13 = inputsFromConstructor(inputs,12);
		this.input14 = inputsFromConstructor(inputs,13);
		this.input15 = inputsFromConstructor(inputs,14);
	}



	public GreekInputsData(){
		this(null,null,null,null,null);
	}
	

	
	private static final String inputsFromConstructor(String[] inputs,int index){
		if(inputs==null)return null;
		if(inputs.length<0)return null;
		if(inputs.length<index+1)return null;
		return inputs[index];
	}

	
	
	

	public String getInput01() {
		return input01;
	}



	public String getInput02() {
		return input02;
	}



	public String getInput03() {
		return input03;
	}



	public String getInput04() {
		return input04;
	}



	public String getInput05() {
		return input05;
	}



	public String getInput06() {
		return input06;
	}



	public String getInput07() {
		return input07;
	}



	public String getInput08() {
		return input08;
	}



	public String getInput09() {
		return input09;
	}



	public String getInput10() {
		return input10;
	}



	public String getInput11() {
		return input11;
	}



	public String getInput12() {
		return input12;
	}



	public String getInput13() {
		return input13;
	}



	public String getInput14() {
		return input14;
	}



	public String getInput15() {
		return input15;
	}



//	@Override
//	public <M extends PositionBaseItem> Tuple<List<String>, M> positionBasedItemFromDerivativeReturn(
//			Position p,
//			SecDef sd,
//			Map<DerivativeSensitivityTypeInterface, DerivativeReturn[]> drSenseMap,
//			List<SecDef> underlyingSds) {
//		
//		return null;
//		
//	}

	@Override
	public <M extends PositionBaseItem> Tuple<List<String>, List<M>> positionBasedItemFromDerivativeReturn(
			Position p,
			SecDef sd,
			Map<DerivativeSensitivityTypeInterface, DerivativeReturn[]> drSenseMap,
			List<SecDef> underlyingSds) {
		
		return null;
		
	}
	
	
	

	@Override
	public DerivativeSensitivityTypeInterface[] getDseSenseArray(){
		DerivativeSensitivityTypeInterface[] ret = {
				GreeksData.deltaDerSen
		};
		return ret;
	}

	@Override
	public MeteorColumnModel[] buildColumnModelArray() {
		MeteorColumnModel input01 = new MeteorColumnModel("input01","input01","input01",null);
		MeteorColumnModel input02 = new MeteorColumnModel("input02","input02","input02",null);
		MeteorColumnModel input03 = new MeteorColumnModel("input03","input03","input03",null);
		MeteorColumnModel input04 = new MeteorColumnModel("input04","input04","input04",null);
		MeteorColumnModel input05 = new MeteorColumnModel("input05","input05","input05",null);
		MeteorColumnModel input06 = new MeteorColumnModel("input06","input06","input06",null);
		MeteorColumnModel input07 = new MeteorColumnModel("input07","input07","input07",null);
		MeteorColumnModel input08 = new MeteorColumnModel("input08","input08","input08",null);
		MeteorColumnModel input09 = new MeteorColumnModel("input09","input09","input09",null);
		MeteorColumnModel input10 = new MeteorColumnModel("input10","input10","input10",null);
		MeteorColumnModel input11 = new MeteorColumnModel("input11","input11","input11",null);
		MeteorColumnModel input12 = new MeteorColumnModel("input12","input12","input12",null);
		MeteorColumnModel input13 = new MeteorColumnModel("input13","input13","input13",null);
		MeteorColumnModel input14 = new MeteorColumnModel("input14","input14","input14",null);
		MeteorColumnModel input15 = new MeteorColumnModel("input15","input15","input15",null);
	
		MeteorColumnModel[] ret = {
				input01,input02,input03,input04,input05,
				input06,input07,input08,input09,input10,
				input11,input12,input13,input14,input15
				};
		return ret;
	}

	
}
