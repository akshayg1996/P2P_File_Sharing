# Computer Networks Course (CNT 5106C) Project 
### P2P File Sharing

- Implement BitTorrent protocol to construct a P2P file-sharing application in Java. 
- Distribute files with choking and unchoking mechanism between peers. 
- Establishing all operations using reliable protocol TCP.

Protocol:
1. In this project, we use TCP protocol to establish connection between peers wanting to share files with each other  
2. To share files, the peers first send a handshake message to each other, consisting of the header, zero bits and peer ID.
3. Then a stream of data messages are sent which consists of message length, type and payload.
4. There are various types of payloads like piece and bitfield. The types of messages are have, bitfield, choke, 
   unchoke, interested, not interested, request and piece
   
Working:
1. The peers are started by startRemotePeers in the order that is specified in the PeerInfo config file, 
   and the peerProcess takes the peer ID as a parameter.
2. The peer that just started is supposed to make a TCP with every peer that is participating in the file sharing 
   and has started before it.
3. All the peers also read the common configuration file that contains the details of the file to be shared, its size, 
   choking and unchoking intervals, and number of preferred neighbours.
4. The PeerInfo file specifies whether a peer has the complete file by bits 0 or 1. Once a peer gets the complete file,
   the PeerInfo.cfg is updated with bit 1 for the corresponding peer.
5. The first peer that is started, just listens on the port specified in the PeerInfo file as there are no other peers to connect to.
6. Also, we maintain a log every peer for when they establish a TCP connection to another peer, when they change their preferred neighbours, 
   when they change their optimistically unchoked neighbour, when they are choked or unchoked by another peer or when they receive have/interested/not interested messages 
   and when they finish downloading a piece or the complete file.

### File Sharing:

If peer requires a file, it issues a search for the file using its filename, or some keyword along with a hop count of 1

1. The request is issued in the overlay network to other peers that are at a distance of current hop count 
   or less from the requesting peer, and the search request expires after a pre-decided number of hop count seconds.
   The duplicate search requests are not allowed.
2. If a peer having the required file receives the search request, then it sends a response to the peer it received the request from. 
   If the peer initiating the request receives the response, it consumes it, otherwise forwards it to the peer that it received the request from.
3. If the requester gets the response, it collects all responses received until the expiry of the request, and those replies after it are just ignored.
4. The response from the peer that matches the required filename and piece index, is chosen, and the requesting peer then establishes a TCP connection with it.
   The requester peer then copies files from the sender peer, to its own directory and updates accordingly. Once it receives the file, the TCP connection is terminated.
5. If the search request terminated without success, then the peer should re-initiate the search after increasing the hop count by 1. It should continue increasing the hop count 
   until the search request succeeds, or the hop count exceeds a pre-defined hop count number.

### Terminating the Protocol

If the number of nodes exceeds the number of allowable hop counts, then their termination should be initiated. 
If a departing has just one neighbour, then it simply terminates the TCP connection with it else 
it chooses one of its neighbours as the neighbour of all its other neighbours (unless they aren't already neighbours).
Then, it should terminate all its TCP connections and ongoing file transfers.

### Project Details
Project Members:
Project Group - 35
1. Riyaz Shaik (UFID: 4360 - 0170)
2. Akshay Ganapathy (UFID: 3684 - 6922)
3. Kapish Yadav (UFID: 9882 - 6143)

Steps to Run the project:
1. Add tree.jpg file in 1001 folder and add Common.cfg and PeerInfo.cfg as needed 
2. Run make command to compile the code
2. run start remote peers and provide username and password to start peers in servers.

To Run process locally in intellij:
1. Go to peerProcess class and run main method
2. Open peerProcess configuration and in program arguments add some peer id 
   and run peerProcess configuration

