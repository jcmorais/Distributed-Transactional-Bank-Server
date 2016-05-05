package Bank;

import Transactional.MiniXid;
import org.zeromq.ZMQ;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by carlosmorais on 21/12/15.
 */
public class ThredBankServerResources extends Thread{
    private Map<Integer, ResourceEO> myResources;
    private ZMQ.Context context;
    private ZMQ.Socket socketReqResource; //socket to Request a Resource
    private ZMQ.Socket socketSUB; //socket to SUBescrive PREPARE COMMIT and ROLLBACK of a Resource
    private ZMQ.Socket socketReqSub; //socket to respond to a notification which was SUBscribed !

    private String myName;

    public ThredBankServerResources(String myName){
        this.myName = myName;

        this.myResources = new ConcurrentHashMap<Integer, ResourceEO>();
        this.context = ZMQ.context(1);

        this.socketReqResource = context.socket(ZMQ.REQ);
        socketReqResource.connect("tcp://localhost:77777");

        this.socketSUB = context.socket(ZMQ.SUB);
        socketSUB.connect("tcp://localhost:55555");

        this.socketReqSub = context.socket(ZMQ.REQ);
        socketReqSub.connect("tcp://localhost:55556");
    }

    public synchronized void recover(int xid, XAResource xar){
        //Monitor, what should I do for this xid?
        String req = "Recover" + "_" + xid+"_"+this.myName, rep;
        socketReqResource.send(req);
        byte[] b =  socketReqResource.recv();
        rep = new String(b);
        log("recover "+xid+" = "+rep);

        switch (rep){
            case "ROLLBACK":
                try {
                    log("rollbacked"+xid);
                    xar.rollback(new MiniXid(xid));
                } catch (XAException e) {
                    e.printStackTrace();
                }
                break;
            case "COMMIT":
                try {
                    log("committed"+xid);
                    xar.commit(new MiniXid(xid), false);
                } catch (XAException e) {
                    e.printStackTrace();
                }
                break;
            case "PREPARE":
                if(!this.myResources.containsKey(xid)){
                    log("wait for commit or rollback"+xid);
                    this.myResources.put(xid, new ResourceEO(xid, xar));
                }
                this.socketSUB.subscribe((this.myName+"_COMMIT_"+xid).getBytes()); //subscrive commit of the Resource
                this.socketSUB.subscribe((this.myName+"_ROLLBACK_"+xid).getBytes()); //subscrive rollback of the Resource
                break;
            default: break;
        }

    }

    //adds a new Resourse and send it to Transational Manager
    public synchronized void addResouce(int xid, XAResource xar){
        String req = "AddRes" + "_" + xid+"_"+this.myName, rep;
        log("Thread: "+req);
        socketReqResource.send(req);
        byte[] b =  socketReqResource.recv(0);
        rep = new String(b);

        if(!this.myResources.containsKey(xid)){
            this.myResources.put(xid, new ResourceEO(xid, xar));
        }

        this.socketSUB.subscribe((this.myName+"_PREPARE_"+xid).getBytes()); //subscrive prepare of the Resource
        this.socketSUB.subscribe((this.myName+"_COMMIT_"+xid).getBytes()); //subscrive commit of the Resource
        this.socketSUB.subscribe((this.myName+"_ROLLBACK_"+xid).getBytes()); //subscrive rollback of the Resource
    }

    public boolean hasXID(int xid){
        return this.myResources.containsKey(xid);
    }


    public void run(){
        while(true){
            byte[] b = this.socketSUB.recv();
            String sub = new String(b);
            System.out.println("Receive a SUB "+sub);
            String[] tokens = sub.split("_");
            int xid = Integer.parseInt(tokens[2]);

            if(tokens.length>2 && tokens[1].equals("PREPARE")) {
                int prepare = -1; //-1 = something wrong!
                if(this.myResources.containsKey(xid)){
                    prepare = this.myResources.get(xid).prepare();
                }
                this.socketReqSub.send(String.valueOf(prepare));
                this.socketReqSub.recv(); //just to swap XD
            }
            else if(tokens.length>2 && tokens[1].equals("COMMIT")) {
                if(this.myResources.containsKey(xid)){
                    this.myResources.get(xid).commit();
                }
            }
            else if(tokens.length>2 && tokens[1].equals("ROLLBACK")){
                if(this.myResources.containsKey(xid)){
                    this.myResources.get(xid).rollback();
                }
            }
        }
    }

    public void log(String s){
        System.out.println("Bank_Thread"+": "+s);
    }

}
