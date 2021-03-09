import handlers.PeerMessageHandler;
import models.CommonConfiguration;
import models.RemotePeerDetails;
import utils.LogHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import static utils.LogHelper.logAndShowInConsole;

public class P2PProcess {
    public static String currentPeerID;
    public int peerIndex;
    public Thread listeningThread;

    public static volatile ConcurrentHashMap<String, RemotePeerDetails> remotePeerDetailsMap = new ConcurrentHashMap();
    public static volatile ConcurrentHashMap<String, RemotePeerDetails> preferredNeighboursMap = new ConcurrentHashMap();
    public static volatile ConcurrentHashMap<String, RemotePeerDetails> unchokedNeighboursMap = new ConcurrentHashMap();

    public static Vector<Thread> receivingThread = new Vector<Thread>();

    public static void main(String[] args) {
        P2PProcess process = new P2PProcess();
        currentPeerID = args[0];

        try {
            //initialize logger and show started message in log file and console
            LogHelper logHelper = new LogHelper();
            logHelper.initializeLogger(currentPeerID);
            logAndShowInConsole(currentPeerID + " is started");

            //read Common.cfg
            readCommonConfiguration();

            //read Peerinfo.cfg
            readPeerConfiguration();

            //initialize preferred neighbours
            initializePreferredNeighbours();

            boolean isFirstPeer = false;
            Enumeration<String> peerKeys = remotePeerDetailsMap.keys();

            if (isFirstPeer) {

            }
            else {
                createNewFile();

                Set<String> remotePeerDetailsKeys = remotePeerDetailsMap.keySet();
                for (String peerID : remotePeerDetailsKeys) {
                    RemotePeerDetails remotePeerDetails = remotePeerDetailsMap.get(peerID);

                    if (process.peerIndex > remotePeerDetails.getIndex()) {
                        Thread tempThread = new Thread(new PeerMessageHandler(
                                remotePeerDetails.getHostAddress(), Integer
                                .parseInt(remotePeerDetails.getPort()), 1,
                                peerID));
                        receivingThread.add(tempThread);
                        tempThread.start();
                    }
                }
            }


        } catch (Exception e) {
            System.out.println("Error occured while running peer process - " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void createNewFile() {
        try {
            File dir = new File(currentPeerID);
            dir.mkdir();

            File newfile = new File(currentPeerID, CommonConfiguration.fileName);
            OutputStream os = new FileOutputStream(newfile, true);
            byte b = 0;

            for (int i = 0; i < CommonConfiguration.fileSize; i++)
                os.write(b);
            os.close();
        } catch (Exception e) {
            logAndShowInConsole(currentPeerID + " ERROR in creating the file : " + e.getMessage());
            e.printStackTrace();
        }

    }

    public static void initializePreferredNeighbours() {
        Set<String> remotePeerIDs = remotePeerDetailsMap.keySet();
        for (String peerID : remotePeerIDs) {
            RemotePeerDetails remotePeerDetails = remotePeerDetailsMap.get(peerID);
            if (remotePeerDetails != null && !peerID.equals(currentPeerID)) {
                preferredNeighboursMap.put(peerID, remotePeerDetails);
            }
        }
    }

    public static void readPeerConfiguration() throws IOException {
        try {
            List<String> lines = Files.readAllLines(Paths.get("PeerInfo.cfg"));
            for (int i = 0; i < lines.size(); i++) {
                String[] properties = lines.get(i).split("\\s+");
                remotePeerDetailsMap.put(properties[0],
                        new RemotePeerDetails(properties[0], properties[1], properties[2], Integer.parseInt(properties[3]), i));
            }
        } catch (IOException e) {
            logAndShowInConsole("Error occured while reading peer configuration - " + e.getMessage());
            throw e;
        }
    }

    public static void readCommonConfiguration() throws IOException {
        try {
            List<String> lines = Files.readAllLines(Paths.get("Common.cfg"));
            for (String line : lines) {
                String[] properties = line.split("\\s+");
                if (properties[0].equalsIgnoreCase("NumberOfPreferredNeighbors")) {
                    CommonConfiguration.numberOfPreferredNeighbours = Integer.parseInt(properties[1]);
                } else if (properties[0].equalsIgnoreCase("UnchokingInterval")) {
                    CommonConfiguration.unchockingInterval = Integer.parseInt(properties[1]);
                } else if (properties[0].equalsIgnoreCase("OptimisticUnchokingInterval")) {
                    CommonConfiguration.optimisticUnchokingInterval = Integer.parseInt(properties[1]);
                } else if (properties[0].equalsIgnoreCase("FileName")) {
                    CommonConfiguration.fileName = properties[1];
                } else if (properties[0].equalsIgnoreCase("FileSize")) {
                    CommonConfiguration.fileSize = Integer.parseInt(properties[1]);
                } else if (properties[0].equalsIgnoreCase("PieceSize")) {
                    CommonConfiguration.pieceSize = Integer.parseInt(properties[1]);
                }
            }
        } catch (IOException e) {
            logAndShowInConsole("Error occured while reading common configuration - " + e.getMessage());
            throw e;
        }
    }
}
