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
    public void testA() throws MalformedURLException, NotBoundException, RemoteException {

        RemoteBankServer bank100 = (RemoteBankServer) Naming.lookup("//localhost/myBank100");
        RemoteBankServer bank101 = (RemoteBankServer) Naming.lookup("//localhost/myBank101");

        double amA0 = bank100.getAccountBalance("1001000");
        double amA1 = bank100.getAccountBalance("1001001");
        double amA2 = bank100.getAccountBalance("1001002");
        double amA3 = bank100.getAccountBalance("1001003");
        double amB0 = bank101.getAccountBalance("1011000");
        double amB1 = bank101.getAccountBalance("1011001");

        aux1();
        assertEquals(amA0-500.0, bank100.getAccountBalance("1001000"));
        assertEquals(amA1-500.0, bank100.getAccountBalance("1001001"));
        assertEquals(amA2-750.0, bank100.getAccountBalance("1001002"));
        assertEquals(amA3+750.0, bank100.getAccountBalance("1001003"));
        assertEquals(amB0+500.0, bank101.getAccountBalance("1011000"));
        assertEquals(amB1+500.0, bank101.getAccountBalance("1011001"));

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
        this.transfer(xid,"1001000","1011000",250);
        boolean res = channelMonitor.commit(xid);

        //T2
        xid = channelMonitor.begin();
        this.transfer(xid,"1001001","1011001",250);
        res = channelMonitor.commit(xid);

        //T3
        xid = channelMonitor.begin();
        this.transfer(xid,"1001001","1011001",250);
        res = channelMonitor.commit(xid);

        //T4
        xid = channelMonitor.begin();
        this.transfer(xid,"1001000","1011000",250);
        res = channelMonitor.commit(xid);


        //T5
        xid = channelMonitor.begin();
        this.transfer(xid,"1011000","1001002",250);
        res = channelMonitor.commit(xid);

        //T6
        xid = channelMonitor.begin();
        this.transfer(xid,"1011001","1001003",250);
        res = channelMonitor.commit(xid);

        //T7
        xid = channelMonitor.begin();
        this.transfer(xid,"1001002","1011001",250);
        res = channelMonitor.commit(xid);

        //T8
        xid = channelMonitor.begin();
        this.transfer(xid,"1001003","1011000",250);
        res = channelMonitor.commit(xid);

        //T9
        xid = channelMonitor.begin();
        this.transfer(xid,"1001002","1001003",750);
        res = channelMonitor.commit(xid);

    }

}
