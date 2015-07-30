sh runInNewTab.sh ./ mvnexec.sh com.billybyte.commonlibstometeor.runs.apps.greeks.RunGreeksFromMeteorPositionChanges "metUrl=localhost" "metPort=3000" "adminEmail=admin1@demo.com" "adminPass=admin1"  "dseXmlPath= beans_DseFromMongoBasedQm_EvalToday.xml" restart=true 
sleep 2
sh runInNewTab.sh ./ mvnexec.sh com.billybyte.commonlibstometeor.runs.apps.greeks.RunGreekInputsFromMeteorPositionChanges "metUrl=localhost" "metPort=3000" "adminEmail=admin1@demo.com" "adminPass=admin1"  "dseXmlPath= beans_DseFromMongoBasedQm_EvalToday.xml" restart=true
sleep 2
sh runInNewTab.sh ./ mvnexec.sh com.billybyte.commonlibstometeor.runs.apps.pl.RunPandLFromMeteorPositionChanges "metUrl=localhost" "metPort=3000" "adminEmail=admin1@demo.com" "adminPass=admin1"  "dseXmlPath= beans_DseFromMongoBasedQm_EvalToday.xml" restart=true
sleep 2
sh runInNewTab.sh ./ mvnexec.sh com.billybyte.commonlibstometeor.runs.apps.var.RunUnitVarFromMeteorPositionChange "metUrl=localhost" "metPort=3000" "adminEmail=admin1@demo.com" "adminPass=admin1"  "dseXmlPath= beans_DseFromMongoBasedQm_EvalToday.xml" restart=true

