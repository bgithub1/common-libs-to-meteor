package com.billybyte.commonlibstometeor.runs.examples;

import java.net.URISyntaxException;

import com.billybyte.commonlibstometeor.Position;
import com.billybyte.commonlibstometeor.runs.ArgBundle;
import com.billybyte.meteorjava.MeteorListCallback;
import com.billybyte.meteorjava.MeteorListSendReceive;
import com.billybyte.meteorjava.staticmethods.Utils;

/**
 * Listen for position changes, and execute analytics such as greeks and VaR
 * 
 * @author bperlman1
 *
 */
public class RunPositionAnalytics {
	public static void main(String[] args) {
		ArgBundle ab = new ArgBundle(args);
		MeteorListSendReceive<Position> mlsr = null;
		try {
			mlsr = new MeteorListSendReceive<Position>(100, 
					Position.class, 
							ab.meteorUrl, ab.meteorPort, 
							ab.adminEmail,ab.adminPass,"", "", "tester");

			final MeteorListCallback<Position> callback = 
					new  MeteorListCallback<Position>() {
						
						@Override
						public void onMessage(String messageType, String id,
								Position convertedMessage) {
							Utils.prt("messageType: "+messageType);
							Utils.prt("id: "+id);
							if(convertedMessage!=null){
								Utils.prt("convertedMessage: "+convertedMessage.toString());
							}else{
								Utils.prt("convertedMessage is null");
							}
						}
					};
			
			mlsr.subscribeToListDataWithCallback(callback);
			
		} catch (URISyntaxException e) {
			throw Utils.IllState(e);
		}
		}
}
