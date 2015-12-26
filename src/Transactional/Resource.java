package Transactional;

/**
 * Created by carlosmorais on 21/12/15.
 */
public class Resource {
    private int xid;
    private String sequenceXAR;
    private int prepare;

    public Resource(int xid, String sequenceXAR){
        this.xid = xid;
        this.sequenceXAR = sequenceXAR;
    }

    public String getSequenceXAR() {
        return sequenceXAR;
    }

    public int getXid() {
        return xid;
    }

    public int getPrepare(){ return this.prepare; }

    public void setPrepare(int prepare){ this.prepare = prepare; }

}
