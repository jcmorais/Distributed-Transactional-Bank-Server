package Bank;

import Transactional.MiniXid;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;

/**
 * Created by carlosmorais on 22/12/15.
 */

//Resource Entity Object
public class ResourceEO {
    private int xid;
    private XAResource xar;

    public ResourceEO(int xid, XAResource xar) {
        this.xid = xid;
        this.xar = xar;
    }

    public int getXid() {
        return xid;
    }

    public XAResource getXar() {
        return xar;
    }

    public int prepare(){
        synchronized (xar) {
            try {
                int res = this.xar.prepare(new MiniXid((this.xid)));
                log(xid +" prepare");
                return res;
            } catch (XAException e) {
                e.printStackTrace();
            }
            return -1; //something wrong!
        }
    }

    public void commit(){
        synchronized (xar) {
            try {
                this.xar.commit(new MiniXid(this.xid), false);
                log(xid +" commit");
            } catch (XAException e) {
                e.printStackTrace();
            }
        }
    }

    public void rollback() {
        synchronized (xar) {
            try {
                this.xar.rollback(new MiniXid(this.xid));
                log(xid +" rollback");
            } catch (XAException e) {
                e.printStackTrace();
            }
        }
    }

    public void log(String s){
        System.out.println(s);
    }
}
