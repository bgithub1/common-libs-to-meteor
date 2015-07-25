sh runInNewTab.sh ./ mvnexec.sh com.billybyte.commonlibstometeor.runs.initializers.RunInitializeMeteorPosition "metUrl=localhost" "metPort=3000" "adminEmail=admin1@demo.com" "adminPass=admin1"
sleep 4
sh runInNewTab.sh ./ mvnexec.sh com.billybyte.commonlibstometeor.runs.initializers.RunInitializeJnestShortNameList "metUrl=localhost" "metPort=3000" "adminEmail=admin1@demo.com" "adminPass=admin1"  "dseXmlPath= beans_DseFromMongoBasedQm_EvalToday.xml"
sleep 4
sh runInNewTab.sh ./ mvnexec.sh com.billybyte.commonlibstometeor.runs.initializers.RunInitializeMeteorGreeksData "metUrl=localhost" "metPort=3000" "adminEmail=admin1@demo.com" "adminPass=admin1"
sleep 4
sh runInNewTab.sh ./ mvnexec.sh com.billybyte.commonlibstometeor.runs.initializers.RunInitializeMeteorProfitAndLoss "metUrl=localhost" "metPort=3000" "adminEmail=admin1@demo.com" "adminPass=admin1"
sleep 4
sh runInNewTab.sh ./ mvnexec.sh com.billybyte.commonlibstometeor.runs.initializers.RunInitializeMeteorUnitVar "metUrl=localhost" "metPort=3000" "adminEmail=admin1@demo.com" "adminPass=admin1"
sleep 4
sh runInNewTab.sh ./ mvnexec.sh com.billybyte.commonlibstometeor.runs.initializers.RunInitializeMeteorGreekInputsData "metUrl=localhost" "metPort=3000" "adminEmail=admin1@demo.com" "adminPass=admin1"

