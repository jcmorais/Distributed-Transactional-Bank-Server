package Transactional;

/**
 * Created by carlosmorais on 05/01/16.
 */
public class Resource {

    private int xid;
    private String server;
    private int prepare; //quando um server reneiciar, só precisa de saber este valor para saber o que fazer???

    public Resource(int xid, String server){
        this.xid = xid;
        this.server = server;
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
