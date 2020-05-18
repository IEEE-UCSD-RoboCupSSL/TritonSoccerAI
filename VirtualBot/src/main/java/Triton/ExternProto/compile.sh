#!/bin/sh
protoc --java_out=./ *.proto
for f in *.java; do echo 'package Triton.ExternProto;\n' | cat - $f > temp && mv temp $f; done