package models;

public class RemotePeerDetails {
    private String id;
    private String address;
    private String port;
    private int hasFile;
    private int index;

    public RemotePeerDetails(String id, String address, String port, int hasFile, int index) {
        this.id = id;
        this.address = address;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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
}
