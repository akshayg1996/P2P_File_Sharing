import java.io.*;
import java.net.Socket;
import java.util.Date;
import java.util.Set;

public class PeerMessageProcessingHandler implements Runnable {

    private static String currentPeerID;
    private RandomAccessFile randomAccessFile;

    public PeerMessageProcessingHandler(String peerID) {
        currentPeerID = peerID;
    }

    public PeerMessageProcessingHandler() {
        currentPeerID = null;
    }

    @Override
    public void run() {
        MessageDetails messageDetails;
        Message message;
        String messageType;
        String remotePeerID;

        while (true) {
            messageDetails = MessageQueue.getMessageFromQueue();
            while (messageDetails == null) {
                Thread.currentThread();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    logAndShowInConsole(currentPeerID + " ERROR occured while thread is interrupted  : " + e.getMessage());
                    e.printStackTrace();
                }
                messageDetails = MessageQueue.getMessageFromQueue();
            }
            message = messageDetails.getMessage();
            messageType = message.getType();
            remotePeerID = messageDetails.getFromPeerID();
            int peerState = peerProcess.remotePeerDetailsMap.get(remotePeerID).getPeerState();

            if (messageType.equals(MessageConstants.MESSAGE_HAVE) && peerState != 14) {
                logAndShowInConsole(currentPeerID + " received HAVE message from Peer " + remotePeerID);
                if (isPeerInterested(message, remotePeerID)) {
                    sendInterestedMessage(peerProcess.peerToSocketMap.get(remotePeerID), remotePeerID);
                    peerProcess.remotePeerDetailsMap.get(remotePeerID).setPeerState(9);
                } else {
                    sendNotInterestedMessage(peerProcess.peerToSocketMap.get(remotePeerID), remotePeerID);
                    peerProcess.remotePeerDetailsMap.get(remotePeerID).setPeerState(13);
                }
            } else {
                switch (peerState) {
                    case 2:
                        if (messageType.equals(MessageConstants.MESSAGE_BITFIELD)) {
                            logAndShowInConsole(currentPeerID + " received a BITFIELD message from Peer " + remotePeerID);
                            sendBitFieldMessage(peerProcess.peerToSocketMap.get(remotePeerID), remotePeerID);
                            peerProcess.remotePeerDetailsMap.get(remotePeerID).setPeerState(3);
                        }
                        break;
                    case 3:
                        if (messageType.equals(MessageConstants.MESSAGE_INTERESTED)) {
                            logAndShowInConsole(currentPeerID + " receieved an INTERESTED message from Peer " + remotePeerID);
                            peerProcess.remotePeerDetailsMap.get(remotePeerID).setIsInterested(1);
                            peerProcess.remotePeerDetailsMap.get(remotePeerID).setIsHandShaked(1);

                            if (isNotPreferredAndUnchokedNeighbour(remotePeerID)) {
                                sendChokedMessage(peerProcess.peerToSocketMap.get(remotePeerID), remotePeerID);
                                peerProcess.remotePeerDetailsMap.get(remotePeerID).setIsChoked(1);
                                peerProcess.remotePeerDetailsMap.get(remotePeerID).setPeerState(6);
                            } else {
                                sendUnChokedMessage(peerProcess.peerToSocketMap.get(remotePeerID), remotePeerID);
                                peerProcess.remotePeerDetailsMap.get(remotePeerID).setIsChoked(0);
                                peerProcess.remotePeerDetailsMap.get(remotePeerID).setPeerState(4);
                            }
                        } else if (messageType.equals(MessageConstants.MESSAGE_NOT_INTERESTED)) {
                            logAndShowInConsole(currentPeerID + " receieved an NOT INTERESTED message from Peer " + remotePeerID);
                            peerProcess.remotePeerDetailsMap.get(remotePeerID).setIsInterested(0);
                            peerProcess.remotePeerDetailsMap.get(remotePeerID).setIsHandShaked(1);
                            peerProcess.remotePeerDetailsMap.get(remotePeerID).setPeerState(5);
                        }
                        break;
                    case 4:
                        if (messageType.equals(MessageConstants.MESSAGE_REQUEST)) {
                            sendFilePiece(peerProcess.peerToSocketMap.get(remotePeerID), message, remotePeerID);

                            if (isNotPreferredAndUnchokedNeighbour(remotePeerID)) {
                                sendChokedMessage(peerProcess.peerToSocketMap.get(remotePeerID), remotePeerID);
                                peerProcess.remotePeerDetailsMap.get(remotePeerID).setIsChoked(1);
                                peerProcess.remotePeerDetailsMap.get(remotePeerID).setPeerState(6);
                            }
                        }
                        break;
                    case 8:
                        if (messageType.equals(MessageConstants.MESSAGE_BITFIELD)) {
                            if (isPeerInterested(message, remotePeerID)) {
                                sendInterestedMessage(peerProcess.peerToSocketMap.get(remotePeerID), remotePeerID);
                                peerProcess.remotePeerDetailsMap.get(remotePeerID).setPeerState(9);
                            } else {
                                sendNotInterestedMessage(peerProcess.peerToSocketMap.get(remotePeerID), remotePeerID);
                                peerProcess.remotePeerDetailsMap.get(remotePeerID).setPeerState(13);
                            }
                        }
                        break;
                    case 9:
                        if (messageType.equals(MessageConstants.MESSAGE_CHOKE)) {
                            logAndShowInConsole(currentPeerID + " is CHOKED by Peer " + remotePeerID);
                            peerProcess.remotePeerDetailsMap.get(remotePeerID).setIsChoked(1);
                            peerProcess.remotePeerDetailsMap.get(remotePeerID).setPeerState(14);
                        } else if (messageType.equals(MessageConstants.MESSAGE_UNCHOKE)) {
                            logAndShowInConsole(currentPeerID + " is UNCHOKED by Peer " + remotePeerID);
                            int firstDifferentPieceIndex = getFirstDifferentPieceIndex(remotePeerID);
                            if (firstDifferentPieceIndex == -1) {
                                peerProcess.remotePeerDetailsMap.get(remotePeerID).setPeerState(13);
                            } else {
                                sendRequestMessage(peerProcess.peerToSocketMap.get(remotePeerID), firstDifferentPieceIndex, remotePeerID);
                                peerProcess.remotePeerDetailsMap.get(remotePeerID).setPeerState(11);
                                peerProcess.remotePeerDetailsMap.get(remotePeerID).setStartTime(new Date());
                            }
                        }
                        break;
                    case 11:
                        if (messageType.equals(MessageConstants.MESSAGE_CHOKE)) {
                            logAndShowInConsole(currentPeerID + " is CHOKED by Peer " + remotePeerID);
                            peerProcess.remotePeerDetailsMap.get(remotePeerID).setIsChoked(1);
                            peerProcess.remotePeerDetailsMap.get(remotePeerID).setPeerState(14);
                        } else if (messageType.equals(MessageConstants.MESSAGE_PIECE)) {
                            byte[] payloadInBytes = message.getPayload();
                            peerProcess.remotePeerDetailsMap.get(remotePeerID).setEndTime(new Date());
                            long totalTime = peerProcess.remotePeerDetailsMap.get(remotePeerID).getEndTime().getTime()
                                    - peerProcess.remotePeerDetailsMap.get(remotePeerID).getStartTime().getTime();
                            double dataRate = ((double) (payloadInBytes.length + MessageConstants.MESSAGE_LENGTH + MessageConstants.MESSAGE_TYPE) / (double) totalTime) * 100;
                            peerProcess.remotePeerDetailsMap.get(remotePeerID).setDataRate(dataRate);
                            FilePiece filePiece = FilePiece.convertByteArrayToFilePiece(payloadInBytes);
                            peerProcess.bitFieldMessage.updateBitFieldInformation(remotePeerID, filePiece);
                            int firstDifferentPieceIndex = getFirstDifferentPieceIndex(remotePeerID);
                            if (firstDifferentPieceIndex == -1) {
                                peerProcess.remotePeerDetailsMap.get(remotePeerID).setPeerState(13);
                            } else {
                                sendRequestMessage(peerProcess.peerToSocketMap.get(remotePeerID), firstDifferentPieceIndex, remotePeerID);
                                peerProcess.remotePeerDetailsMap.get(remotePeerID).setPeerState(11);
                                peerProcess.remotePeerDetailsMap.get(remotePeerID).setStartTime(new Date());
                            }

                            peerProcess.updateOtherPeerDetails();
                            Set<String> remotePeerDetailsKeys = peerProcess.remotePeerDetailsMap.keySet();
                            for (String key : remotePeerDetailsKeys) {
                                RemotePeerDetails peerDetails = peerProcess.remotePeerDetailsMap.get(key);
                                if (!key.equals(peerDetails.getId()) && hasPeerHandShaked(peerDetails)) {
                                    sendHaveMessage(peerProcess.peerToSocketMap.get(key), key);
                                    peerProcess.remotePeerDetailsMap.get(key).setPeerState(3);
                                }
                            }
                        }
                        break;
                    case 14:
                        if (messageType.equals(MessageConstants.MESSAGE_HAVE)) {
                            if (isPeerInterested(message, remotePeerID)) {
                                sendInterestedMessage(peerProcess.peerToSocketMap.get(remotePeerID), remotePeerID);
                                peerProcess.remotePeerDetailsMap.get(remotePeerID).setPeerState(9);
                            } else {
                                sendNotInterestedMessage(peerProcess.peerToSocketMap.get(remotePeerID), remotePeerID);
                                peerProcess.remotePeerDetailsMap.get(remotePeerID).setPeerState(13);
                            }
                        } else if (messageType.equals(MessageConstants.MESSAGE_UNCHOKE)) {
                            logAndShowInConsole(currentPeerID + " is UNCHOKED by Peer " + remotePeerID);
                            peerProcess.remotePeerDetailsMap.get(remotePeerID).setPeerState(14);
                        }
                        break;
                }
            }
        }
    }

    private void sendDownloadedMessage(Socket socket, String remotePeerID) {
        byte[] bitFieldInBytes = peerProcess.bitFieldMessage.getBytes();
        Message message = new Message(MessageConstants.MESSAGE_DOWNLOADED, bitFieldInBytes);
        SendMessageToSocket(socket, Message.convertMessageToByteArray(message));
    }

    private void sendHaveMessage(Socket socket, String peerID) {
        logAndShowInConsole(peerProcess.currentPeerID + " sending HAVE message to Peer " + peerID);
        byte[] bitFieldInBytes = peerProcess.bitFieldMessage.getBytes();
        Message message = new Message(MessageConstants.MESSAGE_HAVE, bitFieldInBytes);
        SendMessageToSocket(socket, Message.convertMessageToByteArray(message));
    }

    private boolean hasPeerHandShaked(RemotePeerDetails remotePeerDetails) {
        return remotePeerDetails.getIsComplete() == 0 &&
                remotePeerDetails.getIsChoked() == 0 && remotePeerDetails.getIsHandShaked() == 1;
    }

    private int getFirstDifferentPieceIndex(String peerID) {
        return peerProcess.bitFieldMessage.getFirstDifferentPieceIndex(peerProcess.remotePeerDetailsMap.get(peerID).getBitFieldMessage());
    }

    private void sendRequestMessage(Socket socket, int pieceIndex, String remotePeerID) {
        int pieceIndexLength = MessageConstants.PIECE_INDEX_LENGTH;
        byte[] pieceInBytes = new byte[pieceIndexLength];
        for (int i = 0; i < pieceIndexLength; i++) {
            pieceInBytes[i] = 0;
        }

        byte[] pieceIndexInBytes = PeerProcessUtils.convertIntToByteArray(pieceIndex);
        System.arraycopy(pieceIndexInBytes, 0, pieceInBytes, 0, pieceIndexInBytes.length);
        Message message = new Message(MessageConstants.MESSAGE_REQUEST, pieceIndexInBytes);
        SendMessageToSocket(socket, Message.convertMessageToByteArray(message));
    }

    private void sendFilePiece(Socket socket, Message message, String remotePeerID) {
        byte[] pieceIndexInBytes = message.getPayload();
        int pieceIndex = PeerProcessUtils.convertByteArrayToInt(pieceIndexInBytes);
        int pieceSize = CommonConfiguration.pieceSize;
        logAndShowInConsole(currentPeerID + " sending a PIECE message for piece " + pieceIndex + " to peer " + remotePeerID);

        byte[] bytesRead = new byte[pieceSize];
        int numberOfBytesRead = 0;
        File file = new File(currentPeerID, CommonConfiguration.fileName);
        try {
            randomAccessFile = new RandomAccessFile(file, "r");
            randomAccessFile.seek(pieceIndex * pieceSize);
            numberOfBytesRead = randomAccessFile.read(bytesRead, 0, pieceSize);
            if (numberOfBytesRead < 0) {
                logAndShowInConsole(currentPeerID + " ERROR occured file " + CommonConfiguration.fileName + " could not be read properly");
            } else if (numberOfBytesRead == 0) {
                logAndShowInConsole(currentPeerID + " ERROR occured zero bytes read from file " + CommonConfiguration.fileName);
            }

            byte[] buffer = new byte[numberOfBytesRead + MessageConstants.PIECE_INDEX_LENGTH];
            System.arraycopy(pieceIndexInBytes, 0, buffer, 0, MessageConstants.PIECE_INDEX_LENGTH);
            System.arraycopy(bytesRead, 0, buffer, MessageConstants.PIECE_INDEX_LENGTH, numberOfBytesRead);

            Message messageToBeSent = new Message(MessageConstants.MESSAGE_PIECE, buffer);
            SendMessageToSocket(socket, Message.convertMessageToByteArray(messageToBeSent));
            randomAccessFile.close();

            buffer = null;
            bytesRead = null;
            pieceIndexInBytes = null;
            messageToBeSent = null;
        } catch (FileNotFoundException e) {
            logAndShowInConsole(currentPeerID + " ERROR occured while reading the file " + CommonConfiguration.fileName);
            e.printStackTrace();
        } catch (IOException e) {
            logAndShowInConsole(currentPeerID + " ERROR occured while sending message data " + CommonConfiguration.fileName);
            e.printStackTrace();
        }
    }

    private boolean isNotPreferredAndUnchokedNeighbour(String remotePeerId) {
        return !peerProcess.preferredNeighboursMap.containsKey(remotePeerId) && !peerProcess.optimisticUnchokedNeighbors.containsKey(remotePeerId);
    }

    private void sendChokedMessage(Socket socket, String remotePeerID) {
        logAndShowInConsole(currentPeerID + " sending a CHOKE message to Peer " + remotePeerID);
        Message message = new Message(MessageConstants.MESSAGE_CHOKE);
        byte[] messageInBytes = Message.convertMessageToByteArray(message);
        SendMessageToSocket(socket, messageInBytes);
    }

    private void sendUnChokedMessage(Socket socket, String remotePeerID) {
        logAndShowInConsole(currentPeerID + " sending a UNCHOKE message to Peer " + remotePeerID);
        Message message = new Message(MessageConstants.MESSAGE_UNCHOKE);
        byte[] messageInBytes = Message.convertMessageToByteArray(message);
        SendMessageToSocket(socket, messageInBytes);
    }

    private void sendNotInterestedMessage(Socket socket, String remotePeerID) {
        logAndShowInConsole(currentPeerID + " sending a NOT INTERESTED message to Peer " + remotePeerID);
        Message message = new Message(MessageConstants.MESSAGE_NOT_INTERESTED);
        byte[] messageInBytes = Message.convertMessageToByteArray(message);
        SendMessageToSocket(socket, messageInBytes);
    }

    private void sendInterestedMessage(Socket socket, String remotePeerID) {
        logAndShowInConsole(currentPeerID + " sending an INTERESTED message to Peer " + remotePeerID);
        Message message = new Message(MessageConstants.MESSAGE_INTERESTED);
        byte[] messageInBytes = Message.convertMessageToByteArray(message);
        SendMessageToSocket(socket, messageInBytes);
    }

    private void sendBitFieldMessage(Socket socket, String remotePeerID) {
        logAndShowInConsole(currentPeerID + " sending a BITFIELD message to Peer " + remotePeerID);
        byte[] bitFieldMessageInByteArray = peerProcess.bitFieldMessage.getBytes();
        Message message = new Message(MessageConstants.MESSAGE_BITFIELD, bitFieldMessageInByteArray);
        byte[] messageInBytes = Message.convertMessageToByteArray(message);
        SendMessageToSocket(socket, messageInBytes);
    }

    private boolean isPeerInterested(Message message, String remotePeerID) {
        BitFieldMessage bitField = BitFieldMessage.decodeMessage(message.getPayload());
        peerProcess.remotePeerDetailsMap.get(remotePeerID).setBitFieldMessage(bitField);

        if (peerProcess.bitFieldMessage.containsInterestingPieces(bitField))
            return true;
        return false;
    }

    private void SendMessageToSocket(Socket socket, byte[] messageInBytes) {
        try {
            OutputStream out = socket.getOutputStream();
            out.write(messageInBytes);
        } catch (IOException e) {
            logAndShowInConsole("Error occured - " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void logAndShowInConsole(String message) {
        LogHelper.logAndShowInConsole(message);
    }
}
