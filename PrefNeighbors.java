import java.io.*;
import java.net.*;
import java.util.*;

public class PrefNeighbors extends TimerTask {
    public void run() 
    {
        //updates remotePeerInfoHash
        peerProcess.updateOtherPeerDetails();
        Enumeration<String> keys = peerProcess.remotePeerDetailsMap.keys();
        int countInterested = 0;
        String strPref = "";
        while(keys.hasMoreElements())
        {
            String key = (String)keys.nextElement();
            RemotePeerDetails pref = peerProcess.remotePeerDetailsMap.get(key);
            if(key.equals(peerProcess.currentPeerID))continue;
            if (pref.getIsComplete() == 0 && pref.getIsHandShaked() == 1)
            {
                countInterested++;
            } 
            else if(pref.getIsComplete() == 1)
            {
                try
                {
                    peerProcess.preferredNeighboursMap.remove(key);
                }
                catch (Exception e) {
                }
            }
        }
        if(countInterested > CommonConfiguration.numberOfPreferredNeighbours)
        {
            boolean flag = peerProcess.preferredNeighboursMap.isEmpty();
            if(!flag)
                peerProcess.preferredNeighboursMap.clear();
            List<RemotePeerDetails> pv = new ArrayList<RemotePeerDetails>(peerProcess.remotePeerDetailsMap.values());
            Collections.sort(pv, new DownloadRateSorter(false));
            int count = 0;
            for (int i = 0; i < pv.size(); i++) 
            {
                if (count > CommonConfiguration.numberOfPreferredNeighbours - 1)
                    break;
                if(pv.get(i).getIsHandShaked() == 1 && !pv.get(i).getId().equals(peerProcess.currentPeerID) 
                        && peerProcess.remotePeerDetailsMap.get(pv.get(i).getId()).getIsComplete() == 0)
                {
                    peerProcess.remotePeerDetailsMap.get(pv.get(i).getId()).setIsPreferredNeighbor(1);
                    peerProcess.preferredNeighboursMap.put(pv.get(i).getId(), peerProcess.remotePeerDetailsMap.get(pv.get(i).getId()));
                    
                    count++;
                    
                    strPref = strPref + pv.get(i).getId() + ", ";
                    
                    if (peerProcess.remotePeerDetailsMap.get(pv.get(i).getId()).getIsChoked() == 1)
                    {
                        peerProcess.sendUnChokeMessage(peerProcess.peerToSocketMap.get(pv.get(i).getId()), pv.get(i).getId());
                        peerProcess.remotePeerDetailsMap.get(pv.get(i).getId()).setIsChoked(0);
                        peerProcess.sendHaveMessage(peerProcess.peerToSocketMap.get(pv.get(i).getId()), pv.get(i).getId());
                        peerProcess.remotePeerDetailsMap.get(pv.get(i).getId()).setPeerState(3);
                    }
                }
            }
        }
        else
        {
            keys = peerProcess.remotePeerDetailsMap.keys();
            while(keys.hasMoreElements())
            {
                String key = (String)keys.nextElement();
                RemotePeerDetails pref = peerProcess.remotePeerDetailsMap.get(key);
                if(key.equals(peerProcess.currentPeerID)) continue;
                
                if (pref.getIsComplete() == 0 && pref.getIsHandShaked() == 1)
                {
                    if(!peerProcess.preferredNeighboursMap.containsKey(key))
                    {
                        strPref = strPref + key + ", ";
                        peerProcess.preferredNeighboursMap.put(key, peerProcess.remotePeerDetailsMap.get(key));
                        peerProcess.remotePeerDetailsMap.get(key).setIsPreferredNeighbor(1);
                    }
                    if (pref.getIsChoked() == 1)
                    {
                        peerProcess.sendUnChokeMessage(peerProcess.peerToSocketMap.get(key), key);
                        peerProcess.remotePeerDetailsMap.get(key).setIsChoked(0);
                        peerProcess.sendHaveMessage(peerProcess.peerToSocketMap.get(key), key);
                        peerProcess.remotePeerDetailsMap.get(key).setPeerState(3);
                    }
                    
                } 
                
            }
        }
        if (strPref != "")
            logAndShowInConsole(peerProcess.currentPeerID + " has selected the preferred neighbors - " + strPref);
    }
    private static void logAndShowInConsole(String message) {
        LogHelper.logAndShowInConsole(message);
    }
}
