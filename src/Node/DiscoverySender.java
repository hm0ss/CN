package Node;

import java.util.ArrayList;

public class DiscoverySender extends Thread {
    private ArrayList<Node> cluster;

    public DiscoverySender(ArrayList<Node> n) {
        cluster = n;
    }

    @Override
    public void run() {
        try {
            sendDiscovery();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    String createMessage() {
        String ans = "DIS";

        //Add yourself
        cluster.add(new Node(Node.ip,Node.udpPort,Node.name));
        for (int i = 0; i < cluster.size(); i++)
            ans = ans + cluster.get(i).getName() + "|" + cluster.get(i).getIp() + "p" + cluster.get(i).getUDPPort() + "\n";

        cluster.remove(cluster.size()-1);



        return ans;

    }


    void sendDiscovery() throws Exception {
        String msg = createMessage();

        for (int i = 0; i < cluster.size(); i++)
            Node.sendUDPSignal(cluster.get(i).getIp(), cluster.get(i).getUDPPort(), msg);
    }
}