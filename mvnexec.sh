#sh setTerminalTitle.sh $1
mvn exec:exec -e  -Dexec.executable="java" -Dexec.args="-Xmx1500m -Xms300m -cp %classpath  ${*:1}"

