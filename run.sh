###
 # @Author: Neil
 # @Date: 2020-11-08 21:32:49
 # @LastEditTime: 2020-11-08 07:18:52
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
mvn clean
if [ "$1" == "-i" ]; then
  mvn install
fi
mvn exec:java &
echo RC-Core is open!

echo Staring CPP group code: not found!
