package Client;

import org.zeromq.ZMQ;

/**
 * Created by carlosmorais on 26/12/15.
 */


/**
 *  Comunication Channel for de Transational Manager responsible for sending the begin() and commit()
 */

public class Channel2PC {
    private ZMQ.Context context;
    private ZMQ.Socket socket;

    public Channel2PC(){
        this.context = ZMQ.context(1);
        this.socket = context.socket(ZMQ.REQ);
        socket.connect("tcp://localhost:88888");
    }

    public synchronized int begin(){
        String request = "BEGIN";
        socket.send(request); //send a request to start a new Transaction
        byte[] b = socket.recv(); //receives a XID of the requested transaction
        return Integer.parseInt(new String(b));
    }

    public synchronized boolean commit(int xid){
        String request = "COMMIT"+"_"+xid;
        socket.send(request); //send a request to commit the transaction xid
        byte[] b = socket.recv();

        if((new String(b)).equals("true"))
            return true;
        else
            return false;
    }

    public synchronized void sendMessage(String m){
        socket.send(m);
        socket.recv();
    }
}
