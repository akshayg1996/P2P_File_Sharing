import java.util.Comparator;

public class DownloadRateSorter implements Comparator<RemotePeerDetails> {

	private boolean firstInstanceGreater;

	// Default Constructor
	public DownloadRateSorter() {
		this.firstInstanceGreater = true;
	}

	// Parameterized Constructor
	public DownloadRateSorter(boolean constructor) {
		this.firstInstanceGreater = constructor;
	}
	
	public int compare(RemotePeerDetails rm1, RemotePeerDetails rm2) {
		if (rm1 == null && rm2 == null)
			return 0;

		if (rm1 == null)
			return 1;

		if (rm2 == null)
			return -1;

		if (rm1 instanceof Comparable) {
			if (firstInstanceGreater) {
				return rm1.compareTo(rm2);
			} else {
				return rm2.compareTo(rm1);
			}
		} 
		else {
			if (firstInstanceGreater) {
				return rm1.toString().compareTo(rm2.toString());
			} else {
				return rm2.toString().compareTo(rm1.toString());
			}
		}
	}
}
