import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class peerProcess {
    public Thread serverThread;
    public ServerSocket serverSocket = null;
    public static String currentPeerID;
    public static int peerIndex;
    public static int currentPeerPort;
    public static int currentPeerHasFile;
    public static BitFieldMessage bitFieldMessage = null;
    public static Thread messageProcessor;
    public static boolean isDownloadComplete = false;
    public static Vector<Thread> peerThreads = new Vector();
    public static Vector<Thread> serverThreads = new Vector();
    public static volatile Timer timerPreferredNeighbors;
    public static volatile Timer timerOptimisticUnchokedNeighbors;
    public static volatile ConcurrentHashMap<String, RemotePeerDetails> remotePeerDetailsMap = new ConcurrentHashMap();
    public static volatile ConcurrentHashMap<String, RemotePeerDetails> preferredNeighboursMap = new ConcurrentHashMap();
    public static volatile ConcurrentHashMap<String, Socket> peerToSocketMap = new ConcurrentHashMap();
    public static volatile ConcurrentHashMap<String, RemotePeerDetails> optimisticUnchokedNeighbors = new ConcurrentHashMap();

    public Thread getServerThread() {
        return serverThread;
    }

    public void setServerThread(Thread serverThread) {
        this.serverThread = serverThread;
    }

    @SuppressWarnings("deprecation")
    public static void main(String[] args) {
        peerProcess process = new peerProcess();
        currentPeerID = args[0];

        try {
            //initialize logger and show started message in log file and console
            LogHelper logHelper = new LogHelper();
            logHelper.initializeLogger(currentPeerID);
            LogHelper.logAndShowInConsole(currentPeerID + " is started");

            //read Common.cfg
            initializePeerConfiguration();

            //read Peerinfo.cfg
            addOtherPeerDetails();

            //initialize preferred neighbours
            setPreferredNeighbours();

            boolean isFirstPeer = false;
            Set<String> remotePeerIDs = remotePeerDetailsMap.keySet();

            for (String peerID : remotePeerIDs) {
                RemotePeerDetails remotePeerDetails = remotePeerDetailsMap.get(peerID);
                if (remotePeerDetails.getId().equals(currentPeerID)) {
                    process.currentPeerPort = Integer.parseInt(remotePeerDetails.getPort());
                    process.peerIndex = remotePeerDetails.getIndex();
                    if (remotePeerDetails.getHasFile() == 1) {
                        isFirstPeer = true;
                        currentPeerHasFile = remotePeerDetails.getHasFile();
                        break;
                    }
                }
            }

            //initialize bit field message
            bitFieldMessage = new BitFieldMessage();
            bitFieldMessage.setPieceDetails(currentPeerID, currentPeerHasFile);

            messageProcessor = new Thread(new PeerMessageProcessingHandler());
            messageProcessor.start();

            if (isFirstPeer) {
                startMessageProcessingThread(process);
            } else {
                createNewFile();

                Set<String> remotePeerDetailsKeys = remotePeerDetailsMap.keySet();
                for (String peerID : remotePeerDetailsKeys) {
                    RemotePeerDetails remotePeerDetails = remotePeerDetailsMap.get(peerID);

                    if (process.peerIndex > remotePeerDetails.getIndex()) {
                        Thread tempThread = new Thread(new PeerMessageHandler(
                                remotePeerDetails.getHostAddress(), Integer
                                .parseInt(remotePeerDetails.getPort()), 1,
                                peerID));
                        peerThreads.add(tempThread);
                        tempThread.start();
                    }
                }
                startMessageProcessingThread(process);
            }

            determineOptimisticallyUnchockedNeighbours();

            while (true) {
                isDownloadComplete = hasDownloadCompleted();
                if (isDownloadComplete) {
                    logAndShowInConsole("All peers have completed downloading the file.");
                    timerPreferredNeighbors.cancel();
                    timerOptimisticUnchokedNeighbors.cancel();

                    try {
                        Thread.currentThread();
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        logAndShowInConsole("Error occured while interrupting thread");
                    }

                    if (process.getServerThread().isAlive()) {
                        process.getServerThread().stop();
                    }

                    if (messageProcessor.isAlive()) {
                        messageProcessor.stop();
                    }

                    for (Thread thread : peerThreads) {
                        if (thread.isAlive()) {
                            thread.stop();
                        }
                    }

                    for (Thread thread : serverThreads) {
                        if (thread.isAlive()) {
                            thread.stop();
                        }
                    }

                    break;
                } else {
                    try {
                        Thread.currentThread();
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        logAndShowInConsole("Error occured while interrupting thread");
                    }
                }
            }

        } catch (Exception e) {
            logAndShowInConsole("Error occured while running peer process - " + e.getMessage());
            e.printStackTrace();
        } finally {
            logAndShowInConsole(currentPeerID + " Peer process is exiting..");
            System.exit(0);
        }
    }

    public static void determineOptimisticallyUnchockedNeighbours() {
        timerOptimisticUnchokedNeighbors = new Timer();
        timerOptimisticUnchokedNeighbors.schedule(new OptimisticallyUnchockedNeighbors(),
                CommonConfiguration.optimisticUnchokingInterval * 1000 * 0,
                CommonConfiguration.optimisticUnchokingInterval * 1000
        );
    }

    public static void startMessageProcessingThread(peerProcess process) {
        try {
            process.serverSocket = new ServerSocket(currentPeerPort);
            process.serverThread = new Thread(new PeerServerHandler(process.serverSocket, currentPeerID));
            process.serverThread.start();
        } catch (SocketTimeoutException e) {
            logAndShowInConsole(currentPeerID + " Socket Gets Timed out Error - " + e.getMessage());
            e.printStackTrace();
            System.exit(0);
        } catch (IOException e) {
            logAndShowInConsole(currentPeerID + " Error Occured while starting server Thread - " + e.getMessage());
            e.printStackTrace();
            System.exit(0);
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

    public static void setPreferredNeighbours() {
        Set<String> remotePeerIDs = remotePeerDetailsMap.keySet();
        for (String peerID : remotePeerIDs) {
            RemotePeerDetails remotePeerDetails = remotePeerDetailsMap.get(peerID);
            if (remotePeerDetails != null && !peerID.equals(currentPeerID)) {
                preferredNeighboursMap.put(peerID, remotePeerDetails);
            }
        }
    }

    public static void addOtherPeerDetails() throws IOException {
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

    public static synchronized boolean hasDownloadCompleted() {
        boolean isDownloadCompleted = true;
        try {
            List<String> lines = Files.readAllLines(Paths.get("PeerInfo.cfg"));
            for (int i = 0; i < lines.size(); i++) {
                String[] properties = lines.get(i).split("\\s+");
                if (Integer.parseInt(properties[3]) == 0) {
                    isDownloadCompleted = false;
                    break;
                }
            }
        } catch (IOException e) {
            logAndShowInConsole("Error occured while reading peer configuration - " + e.getMessage());
            isDownloadCompleted = false;
        }

        return isDownloadCompleted;
    }

    public static void initializePeerConfiguration() throws IOException {
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

    private static void logAndShowInConsole(String message) {
        LogHelper.logAndShowInConsole(message);
    }

    public static void updateOtherPeerDetails() {
        try {
            List<String> lines = Files.readAllLines(Paths.get("PeerInfo.cfg"));
            for (int i = 0; i < lines.size(); i++) {
                String[] properties = lines.get(i).split("\\s+");
                String peerID = properties[0];
                int isCompleted = Integer.parseInt(properties[3]);
                if (isCompleted == 1) {
                    remotePeerDetailsMap.get(peerID).setIsComplete(1);
                    remotePeerDetailsMap.get(peerID).setIsInterested(0);
                    remotePeerDetailsMap.get(peerID).setIsChoked(0);
                }
            }
        } catch (IOException e) {
            logAndShowInConsole("Error occured while reading peer configuration - " + e.getMessage());
        }
    }

    public static void sendUnChokeMessage(Socket socket, String remotePeerID) {
        logAndShowInConsole(currentPeerID + " is sending UNCHOKE message to remote Peer " + remotePeerID);
        Message d = new Message(MessageConstants.MESSAGE_UNCHOKE);
        byte[] msgByte = Message.convertMessageToByteArray(d);
        SendDataStream(socket, msgByte);
    }

    public static void sendHaveMessage(Socket socket, String remotePeerID) {
        byte[] encodedBitField = bitFieldMessage.getBytes();
        logAndShowInConsole(currentPeerID + " sending HAVE message to Peer " + remotePeerID);
        Message d = new Message(MessageConstants.MESSAGE_HAVE, encodedBitField);
        SendDataStream(socket,Message.convertMessageToByteArray(d));
        encodedBitField = null;
    }

    public static int SendDataStream(Socket socket, byte[] encodedBitField) {
        try {
            OutputStream out = socket.getOutputStream();
            out.write(encodedBitField);
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
        return 1;
    }
}
