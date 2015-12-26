package Transactional;

import org.zeromq.ZMQ;

import javax.transaction.xa.XAResource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by carlosmorais on 19/12/15.
 */
public class Monitor2PC {
    public static class ChannelToResourse extends Thread{
        private ZMQ.Context context;
        private ZMQ.Socket socket;
        Monitor monitor;

        public ChannelToResourse(Monitor monitor){
            this.context = ZMQ.context(1);
            this.socket = context.socket(ZMQ.REP);
            socket.bind("tcp://*:77777");
            this.monitor = monitor;
        }

        public void run(){
            while(true){
                byte[] b = socket.recv();
                String sub = new String(b);
                int xid=-1;
                String[] tokens = sub.split("_");
                System.out.println("reveives a SUB to Resource:");

                if(tokens.length>1)
                    xid = Integer.parseInt(tokens[1]);

                String rKey = this.monitor.addResouce(xid);
                socket.send(rKey);
            }
        }

    }

    public static class ChannelToClient extends Thread{
        private ZMQ.Context context;
        private ZMQ.Socket socket;
        Monitor monitor;

        public ChannelToClient(Monitor monitor){
            this.context = ZMQ.context(1);
            this.socket = context.socket(ZMQ.REP);
            socket.bind("tcp://*:88888");
            this.monitor = monitor;
        }

        public void run(){
            while(true){
                byte[] b = socket.recv();
                String req = new String(b), reply="";
                System.out.println("Received " + req);

                if(req.equals("BEGIN")) {
                    int xid = this.monitor.begin();
                    reply = String.valueOf(xid);
                }
                if(req.length() > 5 && req.substring(0,6).equals("COMMIT")) {
                    String[] tokens = req.split("_");
                    int xid = Integer.parseInt(tokens[1]);

                    boolean commit = this.monitor.commit(xid);
                    if (commit)
                        reply = "true";
                    else
                        reply = "false";
                }
                socket.send(reply);
            }
        }
    }


    public static class ChannelCommitResource{
        private ZMQ.Context context;
        private ZMQ.Socket socketPUB;

        //to receive the result of prepare() -> tenho de aranjar maneira de fazer isto de outra maniera (ou nao?)
        private ZMQ.Socket socketREP;

        public ChannelCommitResource() {
            this.context = ZMQ.context(1);

            this.socketPUB = context.socket(ZMQ.PUB);
            this.socketPUB.bind("tcp://*:55555");

            this.socketREP = context.socket(ZMQ.REP);
            this.socketREP.bind("tcp://*:55556");
        }

        public int sendPrepare(String xar){
            this.socketPUB.send("PREPARE_"+xar);

            byte[] b = this.socketREP.recv();
            this.socketREP.send("Thank's LOL");
            return Integer.parseInt(new String(b));
        }

        public void sendCommit(String xar){
            this.socketPUB.send("COMMIT_"+xar);
        }

        public void sendRollBack(String xar){
            this.socketPUB.send("ROLLBACK_"+xar);
        }
    }


    public static class Monitor {
        private Map<Integer, List<Resource>> resouces;
        private int xid;
        private ChannelCommitResource commitResource;

        public Monitor() {
            this.resouces = new HashMap<Integer, List<Resource>>();
            this.xid = 1;
            this.commitResource = new ChannelCommitResource();
        }

        //add a new Resource to XID
        public synchronized String addResouce(int xid) {
            int size = this.resouces.get(xid).size();
            String rKey = xid+":"+(size+1);
            Resource r = new Resource(xid,  rKey);
            this.resouces.get(xid).add(r);
            return rKey;
        }

        public synchronized int begin() {
            this.resouces.put(this.xid, new ArrayList<Resource>());
            return this.xid++;
        }

        public synchronized boolean commit(int xid) {
            boolean doCommit = true;

            //phase1: preparare for all resources with the xid
            for(Resource r : this.resouces.get(xid)){
                int res = this.commitResource.sendPrepare(r.getSequenceXAR());
                System.out.println("Prepare "+r.getSequenceXAR()+" has result "+res);
                r.setPrepare(res);

                if (!((res == XAResource.XA_OK) || (res == XAResource.XA_RDONLY))) {
                    doCommit = false; //if one prepare fail, don't do the commit!
                }
            }

            //phase2: commit or roolback for all resources with the xid
            for(Resource r : this.resouces.get(xid)){
                if (r.getPrepare() == XAResource.XA_OK) {
                    System.out.println("phase2 to "+r.getSequenceXAR());
                    if (doCommit)
                        this.commitResource.sendCommit(r.getSequenceXAR());
                    else
                        this.commitResource.sendRollBack(r.getSequenceXAR());
                }
            }
            return doCommit;
        }
    }


    public static void main(String[] args){
        Monitor monitor = new Monitor();
        new ChannelToClient(monitor).start();
        new ChannelToResourse(monitor).start();
        while(true);
    }
}
