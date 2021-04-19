import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.*;

public class PrefNeighbors extends TimerTask {
    public void run() {
        int countInterested = 0;
        StringBuilder preferredNeighbors = new StringBuilder();
        //updates remotePeerInfoHash
        peerProcess.updateOtherPeerDetails();
        Set<String> remotePeerIDs = peerProcess.remotePeerDetailsMap.keySet();
        for (String key : remotePeerIDs) {
            RemotePeerDetails remotePeerDetails = peerProcess.remotePeerDetailsMap.get(key);
            if (!key.equals(peerProcess.currentPeerID)) {
                if (remotePeerDetails.getIsComplete() == 0 && remotePeerDetails.getIsHandShaked() == 1) {
                    countInterested++;
                } else if (remotePeerDetails.getIsComplete() == 1) {
                    peerProcess.preferredNeighboursMap.remove(key);
                }
            }
        }

        if (countInterested > CommonConfiguration.numberOfPreferredNeighbours) {
            if (!peerProcess.preferredNeighboursMap.isEmpty())
                peerProcess.preferredNeighboursMap.clear();
            List<RemotePeerDetails> pv = new ArrayList(peerProcess.remotePeerDetailsMap.values());
            Collections.sort(pv, new DownloadRateSorter(false));
            int count = 0;
            for (int i = 0; i < pv.size(); i++) {
                if (count > CommonConfiguration.numberOfPreferredNeighbours - 1)
                    break;
                if (pv.get(i).getIsHandShaked() == 1 && !pv.get(i).getId().equals(peerProcess.currentPeerID)
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
            remotePeerIDs = peerProcess.remotePeerDetailsMap.keySet();
            for (String key : remotePeerIDs) {
                RemotePeerDetails remotePeerDetails = peerProcess.remotePeerDetailsMap.get(key);
                if (!key.equals(peerProcess.currentPeerID)) {
                    if (remotePeerDetails.getIsComplete() == 0 && remotePeerDetails.getIsHandShaked() == 1) {
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

    private static void sendUnChokedMessage(Socket socket, String remotePeerID) {
        logAndShowInConsole(peerProcess.currentPeerID + " sending a UNCHOKE message to Peer " + remotePeerID);
        Message message = new Message(MessageConstants.MESSAGE_UNCHOKE);
        SendMessageToSocket(socket, Message.convertMessageToByteArray(message));
    }

    private static void sendHaveMessage(Socket socket, String remotePeerID) {
        byte[] bitFieldMessageBytes = peerProcess.bitFieldMessage.getBytes();
        logAndShowInConsole(peerProcess.currentPeerID + " sending HAVE message to Peer " + remotePeerID);
        Message d = new Message(MessageConstants.MESSAGE_HAVE, bitFieldMessageBytes);
        SendMessageToSocket(socket, Message.convertMessageToByteArray(d));
    }

    private static void SendMessageToSocket(Socket socket, byte[] messageInBytes) {
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
