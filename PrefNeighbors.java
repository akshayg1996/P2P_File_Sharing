import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.*;

/**
 * This class is used to determine preferred neighbors from a list of choked neighbors
 */
public class PrefNeighbors extends TimerTask {
    /**
     * This method is run everytime PrefNeighbors timer task is invoked.
     * For a peer which has file it determines preferred neighbors randomly.
     * For a peer which doesnt have the file, It determines neighbors based to download rate of peers.
     */
    public void run() {
        int countInterested = 0;
        StringBuilder preferredNeighbors = new StringBuilder();
        //updates remotePeerInfo lost
        peerProcess.updateOtherPeerDetails();
        //
        Set<String> remotePeerIDs = peerProcess.remotePeerDetailsMap.keySet();
        for (String key : remotePeerIDs) {
            RemotePeerDetails remotePeerDetails = peerProcess.remotePeerDetailsMap.get(key);
            if (!key.equals(peerProcess.currentPeerID)) {
                if (remotePeerDetails.getIsComplete() == 0 && remotePeerDetails.getIsInterested() == 1) {
                    countInterested++;
                } else if (remotePeerDetails.getIsComplete() == 1) {
                    peerProcess.preferredNeighboursMap.remove(key);
                }
            }
        }

        if (countInterested > CommonConfiguration.numberOfPreferredNeighbours) {
            //If there are more number of interested neighbors than needed, add the first 'CommonConfiguration.numberOfPreferredNeighbours'
            // number of interested neighbors to preferred neighbors to list
            if (!peerProcess.preferredNeighboursMap.isEmpty())
                peerProcess.preferredNeighboursMap.clear();
            List<RemotePeerDetails> pv = new ArrayList(peerProcess.remotePeerDetailsMap.values());
            int isCompleteFilePresent = peerProcess.remotePeerDetailsMap.get(peerProcess.currentPeerID).getIsComplete();
            if (isCompleteFilePresent == 1) {
                Collections.shuffle(pv);
            } else {
                Collections.sort(pv, new DownloadRateSorter(false));
            }
            int count = 0;
            for (int i = 0; i < pv.size(); i++) {
                if (count > CommonConfiguration.numberOfPreferredNeighbours - 1)
                    break;
                if (pv.get(i).getIsInterested() == 1 && !pv.get(i).getId().equals(peerProcess.currentPeerID)
                        && peerProcess.remotePeerDetailsMap.get(pv.get(i).getId()).getIsComplete() == 0) {
                    peerProcess.remotePeerDetailsMap.get(pv.get(i).getId()).setIsPreferredNeighbor(1);
                    peerProcess.preferredNeighboursMap.put(pv.get(i).getId(), peerProcess.remotePeerDetailsMap.get(pv.get(i).getId()));

                    count++;

                    preferredNeighbors.append(pv.get(i).getId()).append(",");
                    if (peerProcess.remotePeerDetailsMap.get(pv.get(i).getId()).getIsChoked() == 1) {
                        sendUnChokedMessage(peerProcess.peerToSocketMap.get(pv.get(i).getId()), pv.get(i).getId());
                        peerProcess.remotePeerDetailsMap.get(pv.get(i).getId()).setIsChoked(0);
                        sendHaveMessage(peerProcess.peerToSocketMap.get(pv.get(i).getId()), pv.get(i).getId());
                        peerProcess.remotePeerDetailsMap.get(pv.get(i).getId()).setPeerState(3);
                    }
                }
            }
        } else {
            //add all the interested neighbors to list
            remotePeerIDs = peerProcess.remotePeerDetailsMap.keySet();
            for (String key : remotePeerIDs) {
                RemotePeerDetails remotePeerDetails = peerProcess.remotePeerDetailsMap.get(key);
                if (!key.equals(peerProcess.currentPeerID)) {
                    if (remotePeerDetails.getIsComplete() == 0 && remotePeerDetails.getIsInterested() == 1) {
                        if (!peerProcess.preferredNeighboursMap.containsKey(key)) {
                            preferredNeighbors.append(key).append(",");
                            peerProcess.preferredNeighboursMap.put(key, peerProcess.remotePeerDetailsMap.get(key));
                            peerProcess.remotePeerDetailsMap.get(key).setIsPreferredNeighbor(1);
                        }
                        if (remotePeerDetails.getIsChoked() == 1) {
                            sendUnChokedMessage(peerProcess.peerToSocketMap.get(key), key);
                            peerProcess.remotePeerDetailsMap.get(key).setIsChoked(0);
                            sendHaveMessage(peerProcess.peerToSocketMap.get(key), key);
                            peerProcess.remotePeerDetailsMap.get(key).setPeerState(3);
                        }
                    }
                }
            }
        }

        if (preferredNeighbors.length() != 0) {
            preferredNeighbors.deleteCharAt(preferredNeighbors.length() - 1);
            logAndShowInConsole(peerProcess.currentPeerID + " has selected the preferred neighbors - " + preferredNeighbors.toString());
        }
    }

    /**
     * This method is used to send UNCHOKE message to socket
     * @param socket - socket in which the message to be sent
     * @param remotePeerID - peerID to which the message should be sent
     */
    private static void sendUnChokedMessage(Socket socket, String remotePeerID) {
        logAndShowInConsole(peerProcess.currentPeerID + " sending a UNCHOKE message to Peer " + remotePeerID);
        Message message = new Message(MessageConstants.MESSAGE_UNCHOKE);
        SendMessageToSocket(socket, Message.convertMessageToByteArray(message));
    }


    /**
     * This method is used to send HAVE message to socket
     * @param socket - socket in which the message to be sent
     * @param peerID - peerID to which the message should be sent
     */
    private void sendHaveMessage(Socket socket, String peerID) {
        //logAndShowInConsole(peerProcess.currentPeerID + " sending HAVE message to Peer " + peerID);
        byte[] bitFieldInBytes = peerProcess.bitFieldMessage.getBytes();
        Message message = new Message(MessageConstants.MESSAGE_HAVE, bitFieldInBytes);
        SendMessageToSocket(socket, Message.convertMessageToByteArray(message));

        bitFieldInBytes = null;
    }

    /**
     * This method is used to write a message to socket
     * @param socket - socket in which the message to be sent
     * @param messageInBytes - message to be sent
     */
    private static void SendMessageToSocket(Socket socket, byte[] messageInBytes) {
        try {
            OutputStream out = socket.getOutputStream();
            out.write(messageInBytes);
        } catch (IOException e) {
        }
    }

    /**
     * This method is used to log a message in a log file and show it in console
     * @param message - message to be logged and showed in console
     */
    private static void logAndShowInConsole(String message) {
        LogHelper.logAndShowInConsole(message);
    }
}
