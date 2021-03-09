package utils;

import handlers.PeerMessageHandler;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import static utils.LogFormatter.getFormattedMessage;

public class LogHelper {
    public FileHandler fileHandler;

    public static Logger log = Logger.getLogger(LogHelper.class.getName());

    public void initializeLogger(String currentPeerID) {
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

    public static void logAndShowInConsole(String message) {
        log.info(message);
        System.out.println(getFormattedMessage(message));
    }

}
