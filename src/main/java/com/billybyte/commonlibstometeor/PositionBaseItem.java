package com.billybyte.commonlibstometeor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import com.billybyte.commoncollections.Tuple;
import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.dse.outputs.DerivativeReturn;
import com.billybyte.dse.outputs.DerivativeSensitivityTypeInterface;
import com.billybyte.marketdata.SecDef;
import com.billybyte.meteorjava.MeteorBaseListItem;
import com.billybyte.meteorjava.MeteorColumnModel;
import com.billybyte.meteorjava.MeteorTableModel;

public abstract class PositionBaseItem extends MeteorBaseListItem{
	public abstract MeteorColumnModel[] buildColumnModelArray();
	public abstract DerivativeSensitivityTypeInterface[] getDseSenseArray();
	/**
	 * Create an extended instance of PositionBaseItem using the specific implementation in the extended class.
	 *   For an example, see GreeksData.pbiFromDerivativeReturn.
	 *   
	 * @param p Position object
	 * @param sd SecDef
	 * @param drSenseMap 
	 * @return Map<DerivativeSensitivityTypeInterface,DerivativeReturn[]>  map of sensitivity to DerivativeReturn array.
	 */
//	public abstract <M extends PositionBaseItem> Tuple<List<String>,M> positionBasedItemFromDerivativeReturn(
//			Position p,SecDef sd, Map<DerivativeSensitivityTypeInterface,DerivativeReturn[]> drSenseMap, List<SecDef> underlyingSds);
	public abstract <M extends PositionBaseItem> Tuple<List<String>,List<M>> positionBasedItemFromDerivativeReturn(
			Position p,SecDef sd, Map<DerivativeSensitivityTypeInterface,DerivativeReturn[]> drSenseMap, List<SecDef> underlyingSds);
	
	private final String account;
	private final String strategy;
	public PositionBaseItem(
			String _id, String userId, String account,
			String strategy) {
		super(_id, userId);
		this.account = account;
		this.strategy = strategy;
	}
	
	
	public String getAccount() {
		return account;
	}
	public String getStrategy() {
		return strategy;
	}
	@Override
	public String toString() {
		return super.toString() + "," +  account + ", " + strategy;
	}
	
	public static <M extends PositionBaseItem> M newInstance(Class<M> classOfNewInstance){
		Exception eFinal=null;
		try {
			Constructor<M> noArgConstructor = 
					classOfNewInstance.getConstructor();
			
			try {
				return noArgConstructor.newInstance();
			} catch (IllegalArgumentException e) {
				eFinal = e;
			} catch (InstantiationException e) {
				eFinal = e;
			} catch (IllegalAccessException e) {
				eFinal = e;
			} catch (InvocationTargetException e) {
				eFinal = e;
			}
		} catch (SecurityException e) {
			eFinal = e;
		} catch (NoSuchMethodException e) {
			eFinal = e;
		}
		eFinal.printStackTrace();
		throw Utils.IllState(eFinal);
	}

	
	public static <M extends PositionBaseItem> MeteorTableModel buildTableModel(Class<M> classOfNewInstance,String tableNameToDisplay){
		PositionBaseItem newby = newInstance(classOfNewInstance);
		MeteorColumnModel[] colModelArr = newby.buildColumnModelArray();
		String tableName = tableNameToDisplay==null ? classOfNewInstance.getSimpleName() : tableNameToDisplay;
		MeteorTableModel tableModel = 
				new MeteorTableModel(
						classOfNewInstance,tableName,classOfNewInstance.getName(), 
						colModelArr);
		return tableModel;
	}
	
	

}
