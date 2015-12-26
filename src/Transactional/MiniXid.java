package Transactional;

/**
 * Created by carlosmorais on 19/12/15.
 */
import javax.transaction.xa.Xid;

public class MiniXid implements Xid {
    private String xid;

    public MiniXid(int xid) {
        this.xid = "txn"+(xid);
    }

    public byte[] getBranchQualifier() {
        return new byte[0];
    }

    @Override
    public int getFormatId() {
        return 0;
    }

    @Override
    public byte[] getGlobalTransactionId() {
        return xid.getBytes();
    }
}
