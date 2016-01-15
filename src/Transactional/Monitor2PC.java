package Transactional;

import org.zeromq.ZMQ;

import javax.transaction.xa.XAResource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
                String sub = new String(b), reply="";
                int xid=-1;
                String[] tokens = sub.split("_");
                log(sub);
                String server="";

                if(tokens.length>2){
                    xid = Integer.parseInt(tokens[1]);
                    server = tokens[2];
                }

                switch (tokens[0]){
                    case "AddRes":
                        this.monitor.addResouce(xid, server);
                        reply = "added bro!"; //XD
                        break;
                    case "Recover":
                        reply= this.monitor.recover(xid, server);
                        break;
                    default:
                        break;
                }

                socket.send(reply);
            }
        }

        public void log(String s){
            System.out.println(s);
        }

    }

    public static class ChannelClient extends Thread{
        private ZMQ.Context context;
        private ZMQ.Socket socket;
        Monitor monitor;

        public ChannelClient(Monitor monitor){
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
                else if(req.length() > 5 && req.substring(0,6).equals("COMMIT")) {
                    String[] tokens = req.split("_");
                    int xid = Integer.parseInt(tokens[1]);

                    boolean commit = this.monitor.commit(xid);
                    if (commit)
                        reply = "true";
                    else
                        reply = "false";
                }
                else {
                    String[] tokens = req.split("_");
                    if(tokens[0].equals("AddServer") && tokens.length==3) {
                        int xid = Integer.parseInt(tokens[1]);
                        String server = tokens[2];
                        log("Add_Server " + xid + " " + server);
                        this.monitor.addServerToXID(xid, server);
                    }
                }

                socket.send(reply);
            }
        }

        public void log(String s){
            System.out.println(s);
        }
    }


    public static class ChannelBankServer{
        private ZMQ.Context context;
        private ZMQ.Socket socketPUB;
        private ZMQ.Socket socketREP;
        private final static int REQUEST_TIMEOUT = 1500;

        public ChannelBankServer() {
            this.context = ZMQ.context(1);

            this.socketPUB = context.socket(ZMQ.PUB);
            this.socketPUB.bind("tcp://*:55555");

            this.socketREP = context.socket(ZMQ.REP);
            this.socketREP.bind("tcp://*:55556");
        }


        public int sendPrepare(Integer xid, String server){
            this.socketPUB.send(server+"_PREPARE_"+xid);

            ZMQ.PollItem items[] = {new ZMQ.PollItem(socketREP, ZMQ.Poller.POLLIN)};
            int rc = ZMQ.poll(items, REQUEST_TIMEOUT);

            if (items[0].isReadable()) {
                byte[] b = this.socketREP.recv();
                this.socketREP.send("Thank's LOL");
                return Integer.parseInt(new String(b));
            }
            else{
                log("sem resposta do servidor");
                return -1; //something wrong
            }
        }

        public void sendCommit(Integer xid, String server){
            this.socketPUB.send(server+"_COMMIT_"+xid);
        }

        public void sendRollBack(Integer xid , String server){
            this.socketPUB.send(server+"_ROLLBACK_"+xid);
        }

        public void log(String s){
            System.out.println(s);
        }
    }


    public static class Monitor {
        private Map<Integer, List<Resource>> resources;
        private int xid;
        private ChannelBankServer commitResource;
        /* If a BankServer fail, but not add your resource, the monitor needs to know
        that a server not added their resource and do rollback */
        private Map<Integer, Set<String>> totalServers; //

        public Monitor() {
            this.resources = new ConcurrentHashMap<Integer, List<Resource>>();
            this.totalServers = new ConcurrentHashMap<Integer, Set<String>>();
            this.xid = 1;
            this.commitResource = new ChannelBankServer();
        }

        //add a new Resource to XID
        public synchronized void addResouce(int xid, String server) {
            Resource sr = new Resource(xid, server);
            boolean flag = true;

            for(Resource aux: this.resources.get(xid)){
                if(server.equals(aux.getServer())){ flag = false; }
            }

            if (flag) {
                this.resources.get(xid).add(sr);
            }
        }

        public synchronized int begin() {
            this.resources.put(this.xid, new ArrayList<Resource>());
            this.totalServers.put(this.xid, new HashSet<String>());
            return this.xid++;
        }

        public boolean commit(int xid) {
            synchronized (this.commitResource) {
                boolean doCommit = true;

                //phase1: preparare for all resources with the xid
                for (Resource r : this.resources.get(xid)) {
                    if(r.isRoolback()){
                        doCommit = false;
                    }
                    else {
                        int res = this.commitResource.sendPrepare(xid, r.getServer());
                        log("Prepare " + r.getServer() + " has result " + res);
                        r.setPrepare(res);
                        if (!((res == XAResource.XA_OK) || (res == XAResource.XA_RDONLY))) {
                            doCommit = false; //if one prepare fail, don't do the commit!
                        }
                    }
                }

                /*
                //testeC BankServer falha depois do prepare
                try {
                    log("vou adormecer 20s");
                    Thread.sleep(20000);
                    log("acordei");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                */

                if((doCommit) && (this.totalServers.get(xid).size() != this.resources.get(xid).size() )){
                    doCommit = false;
                    log("not all servers added your resources");
                }

                if(doCommit) log("doCommit "+xid);
                else log("doRollback "+xid);

                //phase2: commit or roolback for all resources with the xid
                for (Resource r : this.resources.get(xid)) {
                    if (r.getPrepare() == XAResource.XA_OK) {
                        log("phase2 to " + r.getServer());
                        if (doCommit) {
                            r.setCommit(true);
                            this.commitResource.sendCommit(xid, r.getServer());
                        }
                        else {
                            this.commitResource.sendRollBack(xid, r.getServer());
                            r.setRoolback(true);
                        }
                    }
                }
                return doCommit;
            }
        }

        public String recover(int xid, String server){
            String res="";
            boolean doRollback=false;

            log("start recover for "+xid);
            for (Resource r : this.resources.get(xid)) {
                if (r.getServer().equals(server)){
                    if(r.isRoolback()){
                        res = "ROLLBACK";
                    }
                    else if(r.isCommit()){
                        res = "COMMIT";
                    }
                    else if((r.getPrepare() == XAResource.XA_OK)) {
                        res = "PREPARE";
                    }
                    else{
                        res = "ROLLBACK";
                        doRollback=true;
                        log("Problem, the transaction with " +xid +" will be rollbacked");
                    }
                }
            }

            if(doRollback){
                for (Resource r : this.resources.get(xid)) {
                    r.setRoolback(true);
                }
            }

            log("End recover for "+xid+" "+res);
            return res;
        }

        public synchronized void addServerToXID(int xid, String server){
            this.totalServers.get(xid).add(server);
        }

        public void log(String s){
            System.out.println(s);
        }
    }

    public static void main(String[] args){
        Monitor monitor = new Monitor();
        new ChannelClient(monitor).start();
        new ChannelToResourse(monitor).start();
        while(true);
    }
}
