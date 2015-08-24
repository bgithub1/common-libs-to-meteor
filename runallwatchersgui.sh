sleeptime=$1
if [ -z $sleeptime ]; then
	sleeptime=5
fi

sh runInNewTab.sh ./ mvnexec.sh com.billybyte.commonlibstometeor.runs.apps.greeks.RunGreeksFromMeteorPositionChanges "metUrl=localhost" "metPort=3000" "adminEmail=admin1@demo.com" "adminPass=admin1"  "dseXmlPath=beans_DseFromMongoBasedQm_EvalToday.xml" restart=true redirect=true redirectXloc=1 redirectYloc=1 redirectLength=400 redirectWidth=700 
sleep $sleeptime
sh runInNewTab.sh ./ mvnexec.sh com.billybyte.commonlibstometeor.runs.apps.greeks.RunGreekInputsCsvFromMeteorPositionChanges "metUrl=localhost" "metPort=3000" "adminEmail=admin1@demo.com" "adminPass=admin1"  "dseXmlPath=beans_DseFromMongoBasedQm_EvalToday.xml" restart=true redirect=true redirectXloc=1 redirectYloc=401 redirectLength=400 redirectWidth=700
sleep $sleeptime
sh runInNewTab.sh ./ mvnexec.sh com.billybyte.commonlibstometeor.runs.apps.pl.RunPandLFromMeteorPositionChanges "metUrl=localhost" "metPort=3000" "adminEmail=admin1@demo.com" "adminPass=admin1"  "dseXmlPath=beans_DseFromMongoBasedQm_EvalToday.xml" restart=true redirect=true redirectXloc=701 redirectYloc=1 redirectLength=400 redirectWidth=700
sleep $sleeptime
sh runInNewTab.sh ./ mvnexec.sh com.billybyte.commonlibstometeor.runs.apps.var.RunUnitVarFromMeteorPositionChanges "metUrl=localhost" "metPort=3000" "adminEmail=admin1@demo.com" "adminPass=admin1"  "dseXmlPath=beans_DseFromMongoBasedQm_EvalToday.xml" restart=true redirect=true redirectXloc=701 redirectYloc=401 redirectLength=400 redirectWidth=700
sleep $sleeptime
sh runInNewTab.sh ./ mvnexec.sh com.billybyte.commonlibstometeor.runs.apps.var.RunMcVarFromRecalcRequest "metUrl=localhost" "metPort=3000" "adminEmail=admin1@demo.com" "adminPass=admin1"  "dseXmlPath=beans_DseFromMongoBasedQm_EvalToday.xml" restart=true redirect=true redirectXloc=701 redirectYloc=401 redirectLength=400 redirectWidth=700
sleep $sleeptime
sh runInNewTab.sh ./ mvnexec.sh com.billybyte.commonlibstometeor.runs.apps.var.RunCorrMatrixfromMeteorPositionChanges "metUrl=localhost" "metPort=3000" "adminEmail=admin1@demo.com" "adminPass=admin1"  "dseXmlPath=beans_DseFromMongoBasedQm_EvalToday.xml" restart=true redirect=true redirectXloc=701 redirectYloc=401 redirectLength=400 redirectWidth=700

