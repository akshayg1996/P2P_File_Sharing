public class RemotePeerDetails {
    private String id;
    private String hostAddress;
    private String port;
    private int hasFile;
    private int index;
    private int peerState;
    private BitFieldMessage bitFieldMessage;
    public int isInterested;
    public int isHandShaked;
    public int isChoked;

    public RemotePeerDetails(String id, String hostAddress, String port, int hasFile, int index) {
        this.id = id;
        this.hostAddress = hostAddress;
        this.port = port;
        this.hasFile = hasFile;
        this.index = index;
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
}
