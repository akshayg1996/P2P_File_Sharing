public class FilePiece {

    private int isPresent;
    private String fromPeerID;
    private byte[] content;
    private int pieceIndex;

    public FilePiece() {
        content = new byte[CommonConfiguration.pieceSize];
        pieceIndex = -1;
        isPresent = 0;
        fromPeerID = null;
    }

    public int getIsPresent() {
        return isPresent;
    }

    public void setIsPresent(int isPresent) {
        this.isPresent = isPresent;
    }

    public String getFromPeerID() {
        return fromPeerID;
    }

    public void setFromPeerID(String fromPeerID) {
        this.fromPeerID = fromPeerID;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public int getPieceIndex() {
        return pieceIndex;
    }

    public void setPieceIndex(int pieceIndex) {
        this.pieceIndex = pieceIndex;
    }

}