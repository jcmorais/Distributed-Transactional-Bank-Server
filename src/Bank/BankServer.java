package Bank;

import DataBase.BankDAO;
import Transactional.MiniXid;
import org.apache.derby.jdbc.EmbeddedXADataSource;

import javax.sql.XAConnection;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

/**
 * Created by carlosmorais on 21/12/15.
 */


public class BankServer extends UnicastRemoteObject implements RemoteBankServer{
    private int bankServerID;
    private String myName;
    private BankDAO bankDAO;
    private EmbeddedXADataSource ds;
    private ThredBankServerResources myResourses;

    public BankServer(int bankID) throws RemoteException, SQLException {
        super();
        this.bankDAO = new BankDAO();
        this.bankServerID = bankID;
        this.myName = "myBank"+bankID;
        this.initXAConnection(bankID);

        this.myResourses = new ThredBankServerResources(this.myName);
        this.myResourses.start();
    }


    public void initXAConnection(int bankID){
        try {
            ds = new EmbeddedXADataSource();
            this.bankDAO.GenerateDB(bankID, ds);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    /*
    * só para TESTE!!!
    * */
    @Override
    public void transfer(int xid, String idSource, String idDestiny, double amount) throws RemoteException {
        try {

            XAConnection xac = ds.getXAConnection();
            XAResource xar = xac.getXAResource();
            Connection con = xac.getConnection();


            Xid mxid = new MiniXid(xid);
            this.myResourses.addResouce(xid, xar);
            xar.start(mxid, XAResource.TMNOFLAGS);
            this.bankDAO.withdraw(con, idSource,  amount);
            xar.end(mxid, XAResource.TMSUCCESS);

            Connection con2 = xac.getConnection();
            XAResource xar2 = xac.getXAResource();
            Xid mxid2 = new MiniXid(xid);
            this.myResourses.addResouce(xid, xar);
            xar2.start(mxid2, XAResource.TMJOIN);
            this.bankDAO.deposit(con2, idDestiny,  amount);
            xar2.end(mxid, XAResource.TMSUCCESS);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deposit(int xid, String idAccount, double amount) throws RemoteException {
        try {
            XAConnection xac = ds.getXAConnection();
            Connection con = xac.getConnection();
            XAResource xar = xac.getXAResource();
            Xid mxid = new MiniXid(xid);
            if(this.myResourses.hasXID(xid)){
                xar.start(mxid, XAResource.TMJOIN);
            }
            else{
                this.myResourses.addResouce(xid, xar);
                xar.start(mxid, XAResource.TMNOFLAGS);
            }
            this.bankDAO.deposit(con, idAccount, amount);
            xar.end(mxid, XAResource.TMSUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void withdraw(int xid, String idAccount, double amount) throws RemoteException {
        try {

            XAConnection xac = ds.getXAConnection();
            Connection con = xac.getConnection();
            XAResource xar = xac.getXAResource();
            Xid mxid = new MiniXid(xid);
            if(this.myResourses.hasXID(xid)){
                xar.start(mxid, XAResource.TMJOIN);
            }
            else{
                this.myResourses.addResouce(xid, xar);
                xar.start(mxid, XAResource.TMNOFLAGS);
            }
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
            XAConnection xac = ds.getXAConnection();
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
            XAConnection xac = ds.getXAConnection();
            return this.bankDAO.getBalance(xac.getConnection(),idAccount);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1.0; //something wrong?
    }


    public static void main(String[] args) throws RemoteException, SQLException, MalformedURLException {
        Scanner in = new Scanner(System.in);
        System.out.println("Qual o identificador do Servidor a arrancar?");
        int id = in.nextInt();
        BankServer bankServer = new BankServer(id);
        Naming.rebind("myBank"+id, bankServer);
        System.out.println("BankServer"+id+" start");
    }

}