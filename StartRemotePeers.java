/*
 *                     CEN5501C Project2
 * This is the program starting remote processes.
 * This program was only tested on CISE SunOS environment.
 * If you use another environment, for example, linux environment in CISE
 * or other environments not in CISE, it is not guaranteed to work properly.
 * It is your responsibility to adapt this program to your running environment.
 */

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;

/*
 * The StartRemotePeers class begins remote peer processes.
 * It reads configuration file PeerInfo.cfg and starts remote peer processes.
 * You must modify this program a little bit if your peer processes are written in C or C++.
 * Please look at the lines below the comment saying IMPORTANT.
 * It uses jsch library to setup ssh connection.
 */
public class StartRemotePeers {

    public Vector<RemotePeerDetails> peerInfoVector;
    public static String path = System.getProperty("user.dir");

    public void getConfiguration() {
        String st;
        int i1;
        peerInfoVector = new Vector();
        try {
            List<String> lines = Files.readAllLines(Paths.get("PeerInfo.cfg"));
            for (int i = 0; i < lines.size(); i++) {
                String[] properties = lines.get(i).split("\\s+");
                peerInfoVector.addElement(new RemotePeerDetails(properties[0], properties[1], properties[2], Integer.parseInt(properties[3]), i));
            }
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
    }

    /**
     * @param args
     */
    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        try {
            StartRemotePeers myStart = new StartRemotePeers();
            myStart.getConfiguration();
            Session session = null;
            ChannelExec channel = null;

            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter username: ");
            String username = scanner.next();

            System.out.println();
            System.out.print("Enter password: ");
            String password = scanner.next();

            // start clients at remote hosts
            for (int i = 0; i < myStart.peerInfoVector.size(); i++) {
                RemotePeerDetails pInfo = (RemotePeerDetails) myStart.peerInfoVector.elementAt(i);

                System.out.println("Start remote peer " + pInfo.getId() + " at " + pInfo.getHostAddress());

                session = new JSch().getSession(username,  pInfo.getHostAddress() , 22);
                session.setPassword(password);
                session.setConfig("StrictHostKeyChecking", "no");
                session.connect();

                channel = (ChannelExec) session.openChannel("exec");

                if(i==0) {
                    channel.setCommand("cd " + path + "; make peerProcess.class");
                    ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
                    channel.setOutputStream(responseStream);
                    channel.connect();
                    Thread.sleep(3000);
                }

                channel.setCommand("cd " + path + "; java peerProcess " + pInfo.getId());
                ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
                channel.setOutputStream(responseStream);
                channel.connect();
                Thread.sleep(3000);
            }
            System.out.println("Starting all remote peers has done.");
            System.exit(0);
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }
}
