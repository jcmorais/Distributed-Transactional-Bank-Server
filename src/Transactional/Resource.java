package Transactional;

/**
 * Created by carlosmorais on 05/01/16.
 */
public class Resource {

    private int xid;
    private String server;
    private int prepare;
    private boolean roolback;
    private boolean commit;

    public Resource(int xid, String server){
        this.xid = xid;
        this.server = server;
        this.roolback = false;
        this.commit = false;
        this.prepare=-1;
    }

    public boolean isRoolback() {
        return roolback;
    }

    public void setRoolback(boolean roolback) {
        this.roolback = roolback;
    }

    public boolean isCommit() {
        return commit;
    }

    public void setCommit(boolean commit) {
        this.commit = commit;
    }

    public String getServer() {
        return this.server;
    }

    public int getXid() {
        return xid;
    }

    public int getPrepare(){ return this.prepare; }

    public void setPrepare(int prepare){ this.prepare = prepare; }

}
