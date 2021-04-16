import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RemotePeerDetails {
    private String id;
    private String hostAddress;
    private String port;
    private int hasFile;
    private int index;
    private int peerState = -1;
    private int previousPeerState = -1;
    private int isPreferredNeighbor = 0;
    private BitFieldMessage bitFieldMessage;
    private int isOptimisticallyUnchockedNeighbor;
    private int isInterested;
    private int isHandShaked;
    private int isChoked;
    private int isComplete;
    private Date startTime;
    private Date endTime;
    private double dataRate;

    public RemotePeerDetails(String id, String hostAddress, String port, int hasFile, int index) {
        this.id = id;
        this.hostAddress = hostAddress;
        this.port = port;
        this.hasFile = hasFile;
        this.index = index;
        this.dataRate = 0;
        this.isOptimisticallyUnchockedNeighbor = 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHostAddress() {
        return hostAddress;
    }

    public void setHostAddress(String hostAddress) {
        this.hostAddress = hostAddress;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public int getHasFile() {
        return hasFile;
    }

    public void setHasFile(int hasFile) {
        this.hasFile = hasFile;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getIsPreferredNeighbor() {
        return this.isPreferredNeighbor;
    }

    public void setIsPreferredNeighbor(int isPreferredNeighbor) {
        this.isPreferredNeighbor = isPreferredNeighbor;
    }

    public int getPeerState() {
        return peerState;
    }

    public void setPeerState(int peerState) {
        this.peerState = peerState;
    }

    public BitFieldMessage getBitFieldMessage() {
        return bitFieldMessage;
    }

    public void setBitFieldMessage(BitFieldMessage bitFieldMessage) {
        this.bitFieldMessage = bitFieldMessage;
    }

    public int getIsInterested() {
        return isInterested;
    }

    public void setIsInterested(int isInterested) {
        this.isInterested = isInterested;
    }

    public int getIsHandShaked() {
        return isHandShaked;
    }

    public void setIsHandShaked(int isHandShaked) {
        this.isHandShaked = isHandShaked;
    }

    public int getIsChoked() {
        return isChoked;
    }

    public void setIsChoked(int isChoked) {
        this.isChoked = isChoked;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public double getDataRate() {
        return dataRate;
    }

    public void setDataRate(double dataRate) {
        this.dataRate = dataRate;
    }

    public int getIsComplete() {
        return isComplete;
    }

    public void setIsComplete(int isComplete) {
        this.isComplete = isComplete;
    }

    public int getIsOptimisticallyUnchockedNeighbor() {
        return isOptimisticallyUnchockedNeighbor;
    }

    public void setIsOptimisticallyUnchockedNeighbor(int isOptimisticallyUnchockedNeighbor) {
        this.isOptimisticallyUnchockedNeighbor = isOptimisticallyUnchockedNeighbor;
    }

    public int getPreviousPeerState() {
        return previousPeerState;
    }

    public void setPreviousPeerState(int previousPeerState) {
        this.previousPeerState = previousPeerState;
    }

    public void updatePeerDetails(String currentPeerID, int hasFile) throws IOException {
        Path path = Paths.get("PeerInfo.cfg");
        Stream<String> lines = Files.lines(path);

        List<String> newLines = lines.map(line ->
                {
                    String newLine = line;
                    String[] tokens = line.trim().split("\\s+");
                    if (tokens[0].equals(currentPeerID)) {
                        newLine = tokens[0] + " " + tokens[1] + " " + tokens[2] + " " + hasFile;
                    }

                    return newLine;
                }
        ).collect(Collectors.toList());
        Files.write(path, newLines);
        lines.close();
    }

    public int compareTo(RemotePeerDetails o1) {

        if (this.dataRate > o1.dataRate)
            return 1;
        else if (this.dataRate == o1.dataRate)
            return 0;
        else
            return -1;
    }
}
