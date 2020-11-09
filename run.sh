###
 # @Author: Neil
 # @Date: 2020-11-08 21:32:49
 # @LastEditTime: 2020-11-09 13:46:37
 # @LastEditors: Please set LastEditors
 # @Description: In User Settings Edit
 # @FilePath: /SimuBot/run.sh
### 
#!/bin/bash

echo Welcome $USER! You are currently running on $HOSTNAME.
path=$(pwd)

echo Starting grSim:
$path/../grSim/bin/grSim &
echo grSim is open!

echo Starting RC-Core:
cd $path/RC-Core

if [ "$1" == "-i" ]; then
  mvn clean install
fi
mvn exec:java &
echo RC-Core is open!

osascript -e 'tell app "Terminal"
    do script "echo hello"
end tell'

# open new terminals for new processes
echo Staring CPP group code: not found!
