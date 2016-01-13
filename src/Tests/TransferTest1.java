package Tests;

import Bank.RemoteBankServer;
import Client.Channel2PC;
import junit.framework.TestCase;
import org.junit.Test;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

/**
 * Created by carlosmorais on 26/12/15.
 */

public class TransferTest1 extends TestCase {
    Channel2PC channelMonitor = new Channel2PC();

    @Test
    public void teste1() throws MalformedURLException, NotBoundException, RemoteException {

        RemoteBankServer bank100 = (RemoteBankServer) Naming.lookup("//localhost/myBank100");
        RemoteBankServer bank101 = (RemoteBankServer) Naming.lookup("//localhost/myBank101");

        aux1();
        assertEquals(500.0, bank100.getAccountBalance("1001000"));
        assertEquals(500.0, bank100.getAccountBalance("1001001"));
        assertEquals(250.0, bank100.getAccountBalance("1001002"));
        assertEquals(1750.0, bank100.getAccountBalance("1001003"));
        assertEquals(500.0, bank101.getAccountBalance("1011000"));
        assertEquals(500.0, bank101.getAccountBalance("1011001"));

    }



    public void transfer ( int xid, String idSource, String idDestiny,double amount){
        try {
            RemoteBankServer bankS = (RemoteBankServer) Naming.lookup("//localhost/myBank" + idSource.substring(0, 3));
            channelMonitor.sendMessage("AddServer_"+xid+"_"+"myBank"+idSource.substring(0, 3));
            RemoteBankServer bankD = (RemoteBankServer) Naming.lookup("//localhost/myBank" + idDestiny.substring(0, 3));
            channelMonitor.sendMessage("AddServer_"+xid+"_"+"myBank"+idDestiny.substring(0, 3));

            bankD.deposit(xid, idDestiny, amount);
            bankS.withdraw(xid, idSource, amount);

        } catch (NotBoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();

        }
    }

    public void aux1() throws RemoteException, NotBoundException, MalformedURLException {

        //T1
        int xid = channelMonitor.begin();
        System.out.println("Tenho o XID "+xid);
        this.transfer(xid,"1001000","1011000",250);
        boolean res = channelMonitor.commit(xid);
        System.out.println("Commit = "+res+"\n\n");

        //T2
        xid = channelMonitor.begin();
        System.out.println("Tenho o XID "+xid);
        this.transfer(xid,"1001001","1011001",250);
        res = channelMonitor.commit(xid);
        System.out.println("Commit = "+res+"\n\n");

        //T3
        xid = channelMonitor.begin();
        System.out.println("Tenho o XID "+xid);
        this.transfer(xid,"1001001","1011001",250);
        res = channelMonitor.commit(xid);
        System.out.println("Commit = "+res+"\n\n");

        //T4
        xid = channelMonitor.begin();
        System.out.println("Tenho o XID "+xid);
        this.transfer(xid,"1001000","1011000",250);
        res = channelMonitor.commit(xid);
        System.out.println("Commit = "+res+"\n\n");


        //T5
        xid = channelMonitor.begin();
        System.out.println("Tenho o XID "+xid);
        this.transfer(xid,"1011000","1001002",250);
        res = channelMonitor.commit(xid);
        System.out.println("Commit = "+res+"\n\n");

        //T6
        xid = channelMonitor.begin();
        System.out.println("Tenho o XID "+xid);
        this.transfer(xid,"1011001","1001003",250);
        res = channelMonitor.commit(xid);
        System.out.println("Commit = "+res+"\n\n");


        //T7
        xid = channelMonitor.begin();
        System.out.println("Tenho o XID "+xid);
        this.transfer(xid,"1001002","1011001",250);
        res = channelMonitor.commit(xid);
        System.out.println("Commit = "+res+"\n\n");

        //T8
        xid = channelMonitor.begin();
        System.out.println("Tenho o XID "+xid);
        this.transfer(xid,"1001003","1011000",250);
        res = channelMonitor.commit(xid);
        System.out.println("Commit = "+res+"\n\n");


        //T9
        xid = channelMonitor.begin();
        System.out.println("Tenho o XID "+xid);
        this.transfer(xid,"1001002","1001003",750);
        res = channelMonitor.commit(xid);
        System.out.println("Commit = "+res+"\n\n");


    }

}
