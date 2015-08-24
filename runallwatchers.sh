function runwatcher (){
	local processName=$1
	echo ' **************  'running $processName ' *****************'
	sh runInNewTab.sh ./ mvnexec.sh $processName "metUrl=localhost" "metPort=3000" "adminEmail=admin1@demo.com" "adminPass=admin1"  "dseXmlPath=beans_DseFromMongoBasedQm_EvalToday.xml" restart=true	
	echo 'ok to continue (y/n)? '
	read ok

}

runwatcher com.billybyte.commonlibstometeor.runs.apps.greeks.RunGreeksFromMeteorPositionChanges 
runwatcher com.billybyte.commonlibstometeor.runs.apps.greeks.RunGreekInputsCsvFromMeteorPositionChanges 
runwatcher com.billybyte.commonlibstometeor.runs.apps.pl.RunPandLFromMeteorPositionChanges 
runwatcher com.billybyte.commonlibstometeor.runs.apps.var.RunUnitVarFromMeteorPositionChanges 
runwatcher com.billybyte.commonlibstometeor.runs.apps.var.RunMcVarFromRecalcRequest 
runwatcher com.billybyte.commonlibstometeor.runs.apps.var.RunCorrMatrixfromMeteorPositionChanges 
