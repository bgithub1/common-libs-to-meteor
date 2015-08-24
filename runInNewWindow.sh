osascript <<EOF
  	tell app "Terminal" to do script "cd \"`pwd`\";sh setTerminalTitle.sh $1;${*:2}" 
EOF