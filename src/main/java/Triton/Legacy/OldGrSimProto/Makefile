protosrc:
	rm -rf protosrcs/*.java
	mkdir protosrcs
	protoc -I=proto --java_out=protosrcs proto/*.proto
	sed -i '1s/^/package Triton.Legacy.OldGrSimProto.protosrcs; /' protosrcs/*.java


clean:
	rm -rf protosrcs/*.java