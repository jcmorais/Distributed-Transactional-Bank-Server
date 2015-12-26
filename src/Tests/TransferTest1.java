package Tests;

import Bank.RemoteBank;
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
    RemoteBank bank;

    @Test
    public void teste1() throws MalformedURLException, NotBoundException, RemoteException {

        aux1();
        assertEquals(500.0, bank.getAccountBalance("1001000"));
        assertEquals(500.0, bank.getAccountBalance("1001001"));
        assertEquals(1000.0, bank.getAccountBalance("1001002"));
        assertEquals(1000.0, bank.getAccountBalance("1001003"));
        assertEquals(500.0, bank.getAccountBalance("1011000"));
        assertEquals(500.0, bank.getAccountBalance("1011001"));


    }



    public void aux1() throws RemoteException, NotBoundException, MalformedURLException {


        bank = (RemoteBank) Naming.lookup("//localhost/myBank");

        System.out.println(bank.listAllAccounts());

        //T1
        int xid = channelMonitor.begin();
        System.out.println("Tenho o XID "+xid);
        bank.tranfer(xid,"1001000","1011000",250);
        boolean res = channelMonitor.commit(xid);
        System.out.println("Commit = "+res+"\n\n");

        //T2
        xid = channelMonitor.begin();
        System.out.println("Tenho o XID "+xid);
        bank.tranfer(xid,"1001001","1011001",250);
        res = channelMonitor.commit(xid);
        System.out.println("Commit = "+res+"\n\n");

        //T3
        xid = channelMonitor.begin();
        System.out.println("Tenho o XID "+xid);
        bank.tranfer(xid,"1001001","1011001",250);
        res = channelMonitor.commit(xid);
        System.out.println("Commit = "+res+"\n\n");

        //T4
        xid = channelMonitor.begin();
        System.out.println("Tenho o XID "+xid);
        bank.tranfer(xid,"1001000","1011000",250);
        res = channelMonitor.commit(xid);
        System.out.println("Commit = "+res+"\n\n");


        //T5
        xid = channelMonitor.begin();
        System.out.println("Tenho o XID "+xid);
        bank.tranfer(xid,"1011000","1001002",250);
        res = channelMonitor.commit(xid);
        System.out.println("Commit = "+res+"\n\n");

        //T6
        xid = channelMonitor.begin();
        System.out.println("Tenho o XID "+xid);
        bank.tranfer(xid,"1011001","1001003",250);
        res = channelMonitor.commit(xid);
        System.out.println("Commit = "+res+"\n\n");


        //T7
        xid = channelMonitor.begin();
        System.out.println("Tenho o XID "+xid);
        bank.tranfer(xid,"1001002","1011001",250);
        res = channelMonitor.commit(xid);
        System.out.println("Commit = "+res+"\n\n");

        //T8
        xid = channelMonitor.begin();
        System.out.println("Tenho o XID "+xid);
        bank.tranfer(xid,"1001003","1011000",250);
        res = channelMonitor.commit(xid);
        System.out.println("Commit = "+res+"\n\n");


        System.out.println(bank.listAllAccounts());

    }

}
