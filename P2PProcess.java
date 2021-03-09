import models.CommonConfiguration;
import models.RemotePeerDetails;
import utils.LogFormatter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import static utils.LogFormatter.getFormattedMessage;

public class P2PProcess {
    public static Logger log = Logger.getLogger(P2PProcess.class.getName());
    public FileHandler fileHandler;
    public static String currentPeerID;

    public static volatile HashMap<String, RemotePeerDetails> remotePeerDetailsMap = new HashMap();
    public static volatile HashMap<String, RemotePeerDetails> preferredNeighboursMap = new HashMap();

    public static void main(String[] args) {
        P2PProcess process = new P2PProcess();
        currentPeerID = args[0];

        try {
            //initialize logger and show started message in log file and console
            process.initializeLogger();
            logAndShowInConsole(currentPeerID + " is started");

            //read Common.cfg
            readCommonConfiguration();

            //read Peerinfo.cfg
            readPeerConfiguration();

            //initialize preferred neighbours
            initializePreferredNeighbours();


        } catch (Exception e) {
            System.out.println("Error occured while running peer process - " + e.getMessage());
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

    public static void logAndShowInConsole(String message) {
        log.info(message);
        System.out.println(getFormattedMessage(message));
    }

    public void initializeLogger() {
        try {
            fileHandler = new FileHandler("log_peer_" + currentPeerID + ".log");
            fileHandler.setFormatter(new LogFormatter());
            log.addHandler(fileHandler);
            log.setUseParentHandlers(false);
        } catch (IOException e) {
            System.out.println("Error occured while creating log file - " + e.getMessage());
            e.printStackTrace();
        }
    }
}
