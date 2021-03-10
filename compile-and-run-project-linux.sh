#!/usr/bin/env bash

echo "Compiling all the classes in the project"

javac BitFieldMessage.java
javac CommonConfiguration.java
javac ConversionUtils.java
javac FilePiece.java
javac HandshakeMessage.java
javac LogFormatter.java
javac LogHelper.java
javac Message.java
javac MessageConstants.java
javac MessageDetails.java
javac MessageQueue.java
javac P2PProcess.java
javac PeerMessageHandler.java
javac PeerMessageProcessingHandler.java
javac PeerServerHandler.java
javac RemotePeerDetails.java

echo "Done compiling"

echo "Running the project"

java P2PProcess 1001
