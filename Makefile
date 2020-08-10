.PHONY: server client

server:
	cd Server; javac *.java; java Server
	
tcpclient:
	javac -cp Client/lib/protobuf-java-3.11.4.jar Client/*.java
	java -cp Client/lib/protobuf-java-3.11.4.jar:. Client.TCPClient 0 8950
	java -cp Client/lib/protobuf-java-3.11.4.jar:. Client.TCPClient 1 8951
	java -cp Client/lib/protobuf-java-3.11.4.jar:. Client.TCPClient 2 8952
	java -cp Client/lib/protobuf-java-3.11.4.jar:. Client.TCPClient 3 8953
	java -cp Client/lib/protobuf-java-3.11.4.jar:. Client.TCPClient 4 8954
	java -cp Client/lib/protobuf-java-3.11.4.jar:. Client.TCPClient 5 8955

mcclient:
	javac -cp Client/lib/protobuf-java-3.11.4.jar Client/*.java
	java -cp Client/lib/protobuf-java-3.11.4.jar:. Client.MCClient

udpclient:
	javac *.java
	java UDPClient