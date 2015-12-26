package Bank;

import org.zeromq.ZMQ;

import javax.transaction.xa.XAResource;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by carlosmorais on 21/12/15.
 */
public class ThredBankServerResources extends Thread{
    private Map<String, ResourceEO> myResources; // The Resources of the Thread/BankServer
    private ZMQ.Context context;
    private ZMQ.Socket socketReqResource; //socket to Request a Resource
    private ZMQ.Socket socketSUB; //socket to SUBescrive PREPARE COMMIT and ROLLBACK of a Resource
    private ZMQ.Socket socketReqSub; //socket to respond to a notification which was SUBscribed !

    public ThredBankServerResources(){
        this.myResources = new HashMap<String, ResourceEO>();
        this.context = ZMQ.context(1);

        this.socketReqResource = context.socket(ZMQ.REQ);
        socketReqResource.connect("tcp://localhost:77777");

        this.socketSUB = context.socket(ZMQ.SUB);
        socketSUB.connect("tcp://localhost:55555");

        this.socketReqSub = context.socket(ZMQ.REQ);
        socketReqSub.connect("tcp://localhost:55556");
    }

    //adds a new Resourse and send it to Transational Manager
    public synchronized void addResouce(int xid, XAResource xar){
        String req = "AddRes" + "_" + xid, rep;
        socketReqResource.send(req);
        byte[] b =  socketReqResource.recv();
        rep = new String(b);
        this.myResources.put(rep, new ResourceEO(xid, rep, xar));
        this.socketSUB.subscribe(("PREPARE_"+rep).getBytes()); //subscrive prepare of the Resource
        this.socketSUB.subscribe(("COMMIT_"+rep).getBytes()); //subscrive commit of the Resource
        this.socketSUB.subscribe(("ROLLBACK_"+rep).getBytes()); //subscrive rollback of the Resource
        System.out.println("Add new Resourece: "+rep);
    }


    //to test phase1
    public boolean phase1(int id){
        for(ResourceEO aux : this.myResources.values()) {
            if(aux.getXid()==id)
                aux.prepare();
        }

        return true;
    }


    //to test phase2
    public boolean phase2(int xid){
        for(ResourceEO aux : this.myResources.values())
            if(aux.getXid()==xid)
                aux.commit();

        return true;
    }


    public void run(){
        while(true){
            byte[] b = this.socketSUB.recv();
            String sub = new String(b);
            System.out.println("Receive a SUB "+sub);
            String[] tokens = sub.split("_");
            if(tokens.length>1 && tokens[0].equals("PREPARE")) {
                int prepare = this.myResources.get(tokens[1]).prepare();
                this.socketReqSub.send(String.valueOf(prepare));
                this.socketReqSub.recv(); //just to swap XD
            }
            else if(tokens.length>1 && tokens[0].equals("COMMIT")) {
                this.myResources.get(tokens[1]).commit();
            }
            else if(tokens.length>1 && tokens[0].equals("ROLLBACK")){
                this.myResources.get(tokens[1]).rollback();
            }
        }
    }

}
