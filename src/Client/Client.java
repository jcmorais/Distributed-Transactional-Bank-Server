package Client;

import Bank.RemoteBankServer;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Scanner;

/**
 * Created by carlosmorais on 15/12/15.
 */
public class Client {

    public static class BankClient{
        public void transfer ( int xid, String idSource, String idDestiny,double amount){
        try {
            RemoteBankServer bankS = (RemoteBankServer) Naming.lookup("//localhost/myBank" + idSource.substring(0, 3));
            RemoteBankServer bankD = (RemoteBankServer) Naming.lookup("//localhost/myBank" + idDestiny.substring(0, 3));


            //Thread.sleep(500);
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
        public String listAllAccounts() throws RemoteException {
            StringBuilder res = new StringBuilder();

            try {
                RemoteBankServer bank1 = (RemoteBankServer) Naming.lookup("//localhost/myBank100");
                RemoteBankServer bank2 = (RemoteBankServer) Naming.lookup("//localhost/myBank101");

                res.append( bank1.getAllAccounts()+"\n");
                res.append( bank2.getAllAccounts()+"\n");
            } catch (NotBoundException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            return res.toString();
        }

    }




    public static void main(String[] args) throws Exception {
        Channel2PC channelMonitor = new Channel2PC();
        Scanner scanner = new Scanner(System.in);
        String opt;
        boolean end = false;

        //RemoteBank bank = (RemoteBank) Naming.lookup("//localhost/myBank");
        BankClient bank = new BankClient();
        System.out.println(bank.listAllAccounts());


        while(!end) {
            System.out.println("------------------------------\n1 - Nova Transferência\n2 - Sair\n------------------------------");
            opt = scanner.nextLine();
            System.out.println(opt);
            switch(opt) {
                case "1":
                    System.out.println("Insira a conta origem:");
                    String conta1 = scanner.nextLine();
                    System.out.println("Insira a conta destino:");
                    String conta2 = scanner.nextLine();
                    System.out.println("Insira a quantia:");
                    double quantia = Double.parseDouble(scanner.nextLine());

                    int xid = channelMonitor.begin();
                    bank.transfer(xid, conta1, conta2, quantia);
                    //Thread.sleep(2000);
                    boolean res = channelMonitor.commit(xid);
                    if (res) System.out.println("Transferência bem-sucedida.");
                    else System.out.println("Transferência mal-sucedida.");
                    System.out.println(bank.listAllAccounts());
                    break;
                case "2":
                    end = true;
                    break;
                default:
                    System.out.println("Comando não reconhecido.");
                    break;
            }
        }
        //T1
        /*int xid = channelMonitor.begin();
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

        /*
        RemoteBank bank = (RemoteBank) Naming.lookup("//localhost/myBank");
        System.out.println(bank.listAllAccounts());
        bank.tranfer(1,"1001000","1011000",1000);
        bank.deposit(2,"1001001",235);
        bank.withdraw(3,"1011000",235);
        System.out.println(bank.listAllAccounts());

        Monitor2PC monitor = new Monitor2PC();

        // init()
        int xid = monitor.begin();

        // run()
        bank.tranfer(xid,"1001000","1011000",1000);

        // commit
        monitor.commit(xid);
        */

        /*
        RemoteBank bank = (RemoteBank) Naming.lookup("//localhost/myBank100");
        RemoteBank bank2 = (RemoteBank) Naming.lookup("//localhost/myBank101");

        //String myId1 = bank.openAccount(); //open accountId 1
        //String myId2 = bank2.openAccount();

        String myId1 = "1001002";
        String myId2 = "1011001";

        RemoteAccount a = bank.getAccount(myId1);
        RemoteAccount b = bank2.getAccount(myId2);

        a.deposit(100.0);
        a.withdraw(25.5);
        a.tranfer(myId2,25);

        //Thread.sleep(1000);
        System.out.println("AccoudId = "+a.getId()+", balance = "+a.getBalance());
        System.out.println("AccoudId = "+b.getId()+", balance = "+b.getBalance());
        */

    }
}
