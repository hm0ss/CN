package Node;

import com.sun.istack.internal.NotNull;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class TCPReceiver extends Thread {
    private Socket socket;
    private String destinationIP, requestedFileName;
    private int destinationPort;
    private OutputStream output;
    private DataOutputStream dos;
    private ServerSocket commonSocket;
    private String command, hostName;
    private InputStream input;
    private BufferedReader br;
    private PrintWriter bp;
    private DataInputStream dis;
    private Node node;
    private long time;


    public TCPReceiver(String fName, Node n, long t) {
        requestedFileName = fName;
        node = n;
        time = t;
    }

    @Override
    public void run() {
        while (System.currentTimeMillis() - time <= Node.requestWaitPeriod) ;

        if (UDPBroadcast.responders.size() > 0) {
            Node.unfinishedFiles.add(requestedFileName);
            getFile();
            System.out.println("Successfully received file \n\n");

            for (int i = 0; i < Node.unfinishedFiles.size(); i++)
                if (Node.unfinishedFiles.get(i).equals(requestedFileName)) {
                    Node.unfinishedFiles.remove(i);
                    break;
                }
        } else {
            System.out.println("No node responded \n\n");
            Node.responded = false;
        }

        //BASED ON YOUR ASSUMPTION
        UDPBroadcast.responders = new Vector<>();
    }

    void getFile() {
        try {
            findBestCandid();
            socket = new Socket(destinationIP, destinationPort);
            output = socket.getOutputStream();
            input = socket.getInputStream();
            bp = new PrintWriter(new OutputStreamWriter(this.output));
            br = new BufferedReader(new InputStreamReader(this.input));
            identify();
            requestFile(requestedFileName);
            bp.close();
            br.close();
            input.close();
            output.close();
            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    void findBestCandid() {
        boolean first = true;
        long m = 0;
        Respond r = null;

        for (int i = 0; i < UDPBroadcast.responders.size(); i++) {
            if (first == true || UDPBroadcast.responders.get(i).getResponseMilliSeconds() < m) {
                first = false;
                r = UDPBroadcast.responders.get(i);
            }
        }

        //In fact, we used tcp port as input when adding a responder in UDPBroadcast class. So this value is tcp port indeed.
        destinationPort = r.getNode().getUDPPort();
        destinationIP = r.getNode().getIp();

        boolean exist = false;

        for (int i = 0; i < UDPBroadcast.nodesAlreadyGotFileFrom.size(); i++)
            if (UDPBroadcast.nodesAlreadyGotFileFrom.get(i).name.equals(hostName)) {
                exist = true;
                break;
            }

//        synchronized (this) {
//            if (!exist) {
//                Node dn = new Node(destinationIP, destinationPort, hostName);
//
//
//                UDPBroadcast.nodesAlreadyGotFileFrom.add(dn);
//                System.out.println("DECL::");
//                for (int i = 0; i < UDPBroadcast.nodesAlreadyGotFileFrom.size(); i++)
//                    System.out.println(UDPBroadcast.nodesAlreadyGotFileFrom.get(i).name);
//
//
//                System.out.println("SIZE = " + UDPBroadcast.nodesAlreadyGotFileFrom.size() + "\n\n");
//            }
//        }
        System.out.println("I'm going to establish a TCP connection with ip " + destinationIP + " and port " + destinationPort + " to get this file.\n\n");

    }

    private void identify() {
        bp.println(requestedFileName);
        bp.flush();

        try {
            hostName = this.br.readLine();
            System.out.println("HOSTNAME IS " + hostName);


            Node dn = new Node(destinationIP, destinationPort, hostName);
            UDPBroadcast.nodesAlreadyGotFileFrom.add(dn);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void requestFile(String fileName) throws Exception {
        BufferedInputStream bis = new BufferedInputStream(this.input);
        DataInputStream dis = new DataInputStream(bis);


        File file = new File(node.nestFile.getAbsolutePath() + "\\" + fileName);


        long fileLength = dis.readLong();


        FileOutputStream fos = new FileOutputStream(file);
        BufferedOutputStream bos = new BufferedOutputStream(fos);

        for (int j = 0; (long) j < fileLength; ++j) {
            bos.write(bis.read());
        }

        bos.close();


        dis.close();
    }


}
