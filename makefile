JCC = javac
JAVA = java
JFLAGS = -g
REMOTE_PEER_OPTS = -cp .:jsch.jar

default: peerProcess.class

peerProcess.class: peerProcess.java
	$(JCC) $(JFLAGS) peerProcess.java

RemotePeerDetails.class: RemotePeerDetails.java
	$(JCC) $(JFLAGS) RemotePeerDetails.java

PeerServerHandler.class: PeerServerHandler.java
	$(JCC) $(JFLAGS) PeerServerHandler.java

PeerMessageProcessingHandler.class: PeerMessageProcessingHandler.java
	$(JCC) $(JFLAGS) PeerMessageProcessingHandler.java

PeerMessageHandler.class: PeerMessageHandler.java
	$(JCC) $(JFLAGS) PeerMessageHandler.java

MessageQueue.class: MessageQueue.java
	$(JCC) $(JFLAGS) MessageQueue.java

MessageDetails.class: MessageDetails.java
	$(JCC) $(JFLAGS) MessageDetails.java

MessageConstants.class: MessageConstants.java
	$(JCC) $(JFLAGS) MessageConstants.java

Message.class: Message.java
	$(JCC) $(JFLAGS) Message.java

LogHelper.class: LogHelper.java
	$(JCC) $(JFLAGS) LogHelper.java

LogFormatter.class: LogFormatter.java
	$(JCC) $(JFLAGS) LogFormatter.java

HandshakeMessage.class: HandshakeMessage.java
	$(JCC) $(JFLAGS) HandshakeMessage.java

FilePiece.class: FilePiece.java
	$(JCC) $(JFLAGS) FilePiece.java

PeerProcessUtils.class: PeerProcessUtils.java
	$(JCC) $(JFLAGS) PeerProcessUtils.java

CommonConfiguration.class: CommonConfiguration.java
	$(JCC) $(JFLAGS) CommonConfiguration.java

BitFieldMessage.class: BitFieldMessage.java
	$(JCC) $(JFLAGS) BitFieldMessage.java

peerProcess: peerProcess.class
	$(JAVA) peerProcess 1001

StartRemotePeers.class: StartRemotePeers.java
	$(JCC) $(JFLAGS) $(REMOTE_PEER_OPTS) StartRemotePeers.java

StartRemotePeers: StartRemotePeers.class
	$(JAVA) $(REMOTE_PEER_OPTS) StartRemotePeers

clean:
	$(RM) *.class
