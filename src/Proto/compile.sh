#!/bin/sh
mkdir -p ../Protobuf
protoc --java_out=../Protobuf *.proto
for f in ../Protobuf/*.java; do echo 'package Protobuf;\n' | cat - $f > temp && mv temp $f; done