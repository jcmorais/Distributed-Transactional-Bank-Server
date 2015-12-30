package Bank;

import DataBase.BankDAO;
import Transactional.MiniXid;
import org.apache.derby.jdbc.EmbeddedXADataSource;

import javax.sql.XAConnection;
import javax.transaction.xa.XAResource;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by carlosmorais on 21/12/15.
 */


public class BankServer extends UnicastRemoteObject implements RemoteBankServer{
    private int bankServerID;
    private BankDAO bankDAO;
    private XAConnection xac;
    private ThredBankServerResources myResourses;

    public BankServer(int bankID) throws RemoteException, SQLException {
        super();
        this.bankDAO = new BankDAO();
        this.bankServerID = bankID;
        this.initXAConnection(bankID);

        this.myResourses = new ThredBankServerResources();
        this.myResourses.start();
    }


    public void initXAConnection(int bankID){
        try {
            EmbeddedXADataSource ds = new EmbeddedXADataSource();
            this.bankDAO.GenerateDB(bankID, ds);
            this.xac = ds.getXAConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /*
    public String idGenerator() throws RemoteException {
        //this.idGen++;
        int idGen = 0;

        try {
            idGen = this.bankDAO.getNextIdGen(this.xac.getConnection());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //NAO FAZER ISTO ASSIM!!!
        if (idGen < 10)
            return new String(this.bankServerID + "000" + String.valueOf(idGen));
        else if (idGen < 100)
            return new String(this.bankServerID + "00" + String.valueOf(idGen));
        if (idGen < 1000)
            return new String(this.bankServerID + "0" + String.valueOf(idGen));
        else
            return new String(this.bankServerID + String.valueOf(idGen));
    }

*/
    @Override
    public void transfer(int xid, String idSource, String idDestiny, double amount) throws RemoteException {
        try {
            Connection con = xac.getConnection();
            XAResource xar = xac.getXAResource();
            MiniXid mxid = new MiniXid(xid);
            this.myResourses.addResouce(xid, xar);
            xar.start(mxid, XAResource.TMNOFLAGS);
            this.bankDAO.transfer(con, idSource, idDestiny, amount);
            xar.end(mxid, XAResource.TMSUCCESS);;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deposit(int xid, String idAccount, double amount) throws RemoteException {
        try {
            Connection con = xac.getConnection();
            XAResource xar = xac.getXAResource();
            MiniXid mxid = new MiniXid(xid);
            this.myResourses.addResouce(xid, xar);
            xar.start(mxid, XAResource.TMNOFLAGS);
            this.bankDAO.deposit(con, idAccount, amount);
            xar.end(mxid, XAResource.TMSUCCESS);;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void withdraw(int xid, String idAccount, double amount) throws RemoteException {
        try {
            Connection con = xac.getConnection();
            XAResource xar = xac.getXAResource();
            MiniXid mxid = new MiniXid(xid);
            this.myResourses.addResouce(xid, xar);
            xar.start(mxid, XAResource.TMNOFLAGS);
            this.bankDAO.withdraw(con, idAccount, amount);
            xar.end(mxid, XAResource.TMSUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public String getAllAccounts() throws RemoteException {
        StringBuilder res=new StringBuilder();
        try {
            List<Account> acs = this.bankDAO.getAllAccounts(xac.getConnection());
            for(Account c : acs)
                res.append(c.toString()+"\n");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return  res.toString();
    }

    @Override
    public Double getAccountBalance(String idAccount) throws RemoteException {
        try {
            return this.bankDAO.getBalance(xac.getConnection(),idAccount);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1.0; //something wrong?
    }


}