package Tests;

import Bank.RemoteBankServer;
import Client.Channel2PC;
import junit.framework.TestCase;
import org.junit.Test;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by carlosmorais on 14/01/16.
 */
public class TransferSuperTest extends TestCase{
    Channel2PC channelMonitor = new Channel2PC();
    AtomicInteger idA0;
    AtomicInteger idA1 ;
    AtomicInteger idA2 ;
    AtomicInteger idA3 ;
    AtomicInteger idB0 ;
    AtomicInteger idB1;
    Map<String, AtomicInteger> contas;




    @Test
    public void testB() throws MalformedURLException, NotBoundException, RemoteException {

        RemoteBankServer bank100 = (RemoteBankServer) Naming.lookup("//localhost/myBank100");
        RemoteBankServer bank101 = (RemoteBankServer) Naming.lookup("//localhost/myBank101");

        idA0 = new AtomicInteger(0);
        idA1 = new AtomicInteger(0);
        idA2 = new AtomicInteger(0);
        idA3 = new AtomicInteger(0);
        idB0 = new AtomicInteger(0);
        idB1 = new AtomicInteger(0);
        contas = new ConcurrentHashMap<String, AtomicInteger>();

        contas.put("1001000", idA0);
        contas.put("1001001", idA1);
        contas.put("1001002", idA2);
        contas.put("1001003", idA3);

        contas.put("1011000", idB0);
        contas.put("1011001", idB1);

        double amA0 = bank100.getAccountBalance("1001000");
        double amA1 = bank100.getAccountBalance("1001001");
        double amA2 = bank100.getAccountBalance("1001002");
        double amA3 = bank100.getAccountBalance("1001003");
        double amB0 = bank101.getAccountBalance("1011000");
        double amB1 = bank101.getAccountBalance("1011001");

        test();
        assertEquals(amA0+(idA0.get()*100), bank100.getAccountBalance("1001000"));
        assertEquals(amA1+(idA1.get()*100), bank100.getAccountBalance("1001001"));
        assertEquals(amA2+(idA2.get()*100), bank100.getAccountBalance("1001002"));
        assertEquals(amA3+(idA3.get()*100), bank100.getAccountBalance("1001003"));
        assertEquals(amB0+(idB0.get()*100), bank101.getAccountBalance("1011000"));
        assertEquals(amB1+(idB1.get()*100), bank101.getAccountBalance("1011001"));
    }


    public class ClientTest extends Thread{

        Map<String, AtomicInteger> contas;
        public ClientTest(Map<String, AtomicInteger> contas){
            this.contas = contas;
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

        public void run(){

            int amount = 100;

            this.contas.size();

            for(int count = 0; count < 100; count++ ) {
                for (int idBankA = 1001000; idBankA <= 1001003; idBankA++) {
                    for (int idBankB = 1011000; idBankB <= 1011001; idBankB++) {
                        int xid = channelMonitor.begin();
                        this.transfer(xid, "" + idBankA, "" + idBankB, amount);
                        boolean res = channelMonitor.commit(xid);
                        if(res){
                            if(this.contas.containsKey(""+idBankA))
                                contas.get(""+idBankA).getAndDecrement();
                            if(this.contas.containsKey(""+idBankB))
                                contas.get(""+idBankB).getAndIncrement();
                        }
                    }
                }

                for (int idBankB = 1011000; idBankB <= 1011001; idBankB++) {
                    for (int idBankA = 1001000; idBankA <= 1001003; idBankA++) {
                            int xid = channelMonitor.begin();
                            this.transfer(xid, "" + idBankB, "" + idBankA, amount);
                            boolean res = channelMonitor.commit(xid);
                            if(res){
                                if(contas.containsKey(""+idBankB))
                                    contas.get(""+idBankB).getAndDecrement();
                                if(contas.containsKey(""+idBankA))
                                    contas.get(""+idBankA).getAndIncrement();
                            }
                        }
                    }
            }


        }

    }

    public void test(){
        ClientTest c1 = new ClientTest(contas);
        ClientTest c2 = new ClientTest(contas);

        c1.start();
        c2.start();
        try {
            c1.join();
            c2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }




}
