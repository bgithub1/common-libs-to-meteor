function runwatcherNewTab (){
	local processName=$1
	echo ' **************  'running $processName ' *****************'
	sh runInNewTab.sh ./ mvnexec.sh $processName "metUrl=localhost" "metPort=3000" "adminEmail=admin1@demo.com" "adminPass=admin1"  "dseXmlPath=beans_DseFromMongoBasedQm_EvalToday.xml" restart=true	
#	echo 'ok to continue (y/n)? '
#	read ok
	sleep 1
}

function runwatcherNewWin (){
	local windowName=$1
	local processName=$2
	
	echo ' **************  'running $processName ' *****************'
	sh runInNewWindow.sh $windowName sh mvnexec.sh $processName "metUrl=localhost" "metPort=3000" "adminEmail=admin1@demo.com" "adminPass=admin1"  "dseXmlPath=beans_DseFromMongoBasedQm_EvalToday.xml" restart=true	
#	echo 'ok to continue (y/n)? '
#	read ok
	sleep 1
}

runwatcherNewWin greeks com.billybyte.commonlibstometeor.runs.apps.greeks.RunGreeksFromMeteorPositionChanges 
runwatcherNewWin greeksInputs com.billybyte.commonlibstometeor.runs.apps.greeks.RunGreekInputsCsvFromMeteorPositionChanges 
runwatcherNewWin p_and_l com.billybyte.commonlibstometeor.runs.apps.pl.RunPandLFromMeteorPositionChanges 
runwatcherNewWin UnitVar com.billybyte.commonlibstometeor.runs.apps.var.RunUnitVarFromMeteorPositionChanges 
runwatcherNewWin McVaR com.billybyte.commonlibstometeor.runs.apps.var.RunMcVarFromRecalcRequest 
runwatcherNewWin CorrMatrix com.billybyte.commonlibstometeor.runs.apps.var.RunCorrMatrixfromMeteorPositionChanges 
