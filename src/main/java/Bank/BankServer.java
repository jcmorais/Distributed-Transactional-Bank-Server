package Bank;

import Client.Channel2PC;
import DataBase.BankDAO;
import Transactional.MiniXid;
import org.apache.derby.jdbc.EmbeddedXADataSource;

import javax.sql.XAConnection;
import javax.transaction.xa.XAException;
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
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by carlosmorais on 21/12/15.
 */


public class BankServer extends UnicastRemoteObject implements RemoteBankServer{
    private int bankServerID;
    private String myName;
    private BankDAO bankDAO;
    private EmbeddedXADataSource ds;
    private ThredBankServerResources myResourses;
    private int lastXID;
    private ReentrantLock lock;

    public BankServer(int bankID, int load) throws RemoteException, SQLException {
        super();
        this.bankDAO = new BankDAO();
        this.bankServerID = bankID;
        this.myName = "myBank"+bankID;
        this.initXAConnection(bankID, load);
        this.myResourses = new ThredBankServerResources(this.myName);
        this.myResourses.start();
        this.lastXID=0;
        this.lock = new ReentrantLock();
    }


    public void initXAConnection(int bankID, int load){
        try {
            ds = new EmbeddedXADataSource();
            if (load == 1)
                this.bankDAO.LoadDB(bankID, ds);
            else
                this.bankDAO.GenerateDB(bankID, ds);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public  void deposit(int xid, String idAccount, double amount) throws RemoteException {
        try {
            XAConnection xac = ds.getXAConnection();
            Connection con = xac.getConnection();
            Xid mxid = new MiniXid(xid);
            XAResource xar = xac.getXAResource();
            lock.lock();
            try{
                if(xid < this.lastXID) {
                    return;
                }
                this.lastXID = xid;
            if(this.myResourses.hasXID(xid)){
                xar.start(mxid, XAResource.TMJOIN);
            }
            else{
                this.myResourses.addResouce(xid, xar);
                xar.start(mxid, XAResource.TMNOFLAGS);
            }
            this.bankDAO.deposit(con, idAccount, amount);
            xar.end(mxid, XAResource.TMSUCCESS);
            }
            finally {
                lock.unlock();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public  void withdraw(int xid, String idAccount, double amount) throws RemoteException {
        /*
        //TesteA Servidor falha operação e ainda não pediu para adicionar o Recurso
        try {
            log("vou adormecer 10s");
            Thread.sleep(10000);
            log("acordei");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        */
        try {
            XAConnection xac = ds.getXAConnection();
            Connection con = xac.getConnection();
            Xid mxid = new MiniXid(xid);
            XAResource xar = xac.getXAResource();
            lock.lock();
            try{
                if(xid < this.lastXID) {
                    return;
                }
                this.lastXID = xid;
            if(this.myResourses.hasXID(xid)){
                xar.start(mxid, XAResource.TMJOIN);
            }
            else{
                this.myResourses.addResouce(xid, xar);
                xar.start(mxid, XAResource.TMNOFLAGS);
            }
            this.bankDAO.withdraw(con, idAccount, amount);
            xar.end(mxid, XAResource.TMSUCCESS);
            }
            finally {
                lock.unlock();
            }

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
            return -1.0;
        }
    }

    public void recover(){
        try {
            XAResource xar = ds.getXAConnection().getXAResource();
            Xid[] xid = xar.recover(XAResource.TMSTARTRSCAN);

            for(Xid aux: xid){
                log("Try recover xid = "+aux.getFormatId());
                this.myResourses.recover(aux.getFormatId(), xar);
            }
        } catch (XAException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    public static void main(String[] args) throws RemoteException, SQLException, MalformedURLException {
        Scanner scanner = new Scanner(System.in);
        BankServer bankServer;
        Channel2PC channelMonitor = new Channel2PC();
        boolean flag = true;
        String[] tokens = new String[0];
        String read;
        int id = 0;

        while(flag){
            System.out.println("How star the Server?\n- start \"id\"\n- load \"id\"\n- recover \"id\"");
            read = scanner.nextLine();
            tokens = read.split(" ");

            if(tokens.length >= 2) {
                try {
                    id = Integer.parseInt(tokens[1]);
                    flag = false;
                } catch (NumberFormatException e) {
                    System.out.println("not a INT...");
                }
            }
            else
                System.out.println("wrong...");
        }


        switch (tokens[0]){
            case "start": {
                bankServer = new BankServer(id, 0);
                Naming.rebind("myBank" + id, bankServer);
                bankServer.log("BankServer"+id+" start");
                break;
            }
            case  "load": {
                bankServer = new BankServer(id, 1);
                Naming.rebind("myBank" + id, bankServer);
                bankServer.log("BankServer"+id+" load");
                break;
            }
            case "recover": {
                bankServer = new BankServer(id, 1);
                bankServer.log("Start Recover");
                bankServer.recover();
                bankServer.log("End Recover");
                Naming.rebind("myBank" + id, bankServer);
                bankServer.log("BankServer"+id+" load");
                break;
            }
        }
    }

    public void log(String s){
        System.out.println("Bank: " + s);
    }

}