package Transactional;

/**
 * Created by carlosmorais on 19/12/15.
 */
import javax.transaction.xa.Xid;

public class MiniXid implements Xid {
    protected int formatId;

    public MiniXid()
    {
    }
    public MiniXid(int formatId )
    {
        this.formatId = formatId;
    }
    public int getFormatId()
    {
        return formatId;
    }

    public byte[] getBranchQualifier() {
        return new byte[0];
    }

    @Override
    public byte[] getGlobalTransactionId() {
        return ("tx"+formatId).getBytes();
    }
}
