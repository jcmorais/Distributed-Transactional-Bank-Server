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
        private static final String bankPrefix = "myBank";
        private static final String bankHost = "//localhost/";
        private Channel2PC monitor;

        public BankClient(Channel2PC monitor){
            this.monitor = monitor;
        }

        public void transfer ( int xid, String idSource, String idDestiny,double amount){
        try {
            String serverS =  bankPrefix + idSource.substring(0, 3), serverD = bankPrefix + idDestiny.substring(0, 3);
            RemoteBankServer bankS = (RemoteBankServer) Naming.lookup(bankHost+serverS);
            this.monitor.sendMessage("AddServer_"+xid+"_"+serverS);
            RemoteBankServer bankD = (RemoteBankServer) Naming.lookup(bankHost+serverD);
            this.monitor.sendMessage("AddServer_"+xid+"_"+serverD);

            bankD.deposit(xid, idDestiny, amount);
            bankS.withdraw(xid, idSource, amount);

            /*
            //testeB BankServer falha antes do prepare
            try {
                System.out.println("vou adormecer esperar 25s");
                Thread.sleep(25000);
                System.out.println("acordei!!!");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            */

        } catch (NotBoundException e) {
        } catch (MalformedURLException e) {
        } catch (RemoteException e) { }
        }
        public String listAllAccounts() throws RemoteException {
            StringBuilder res = new StringBuilder();

            try {
                RemoteBankServer bank1 = (RemoteBankServer) Naming.lookup("//localhost/myBank100");
                RemoteBankServer bank2 = (RemoteBankServer) Naming.lookup("//localhost/myBank101");

                try {
                    res.append(bank1.getAllAccounts() + "\n");
                }
                catch (Exception e ) { }

                try {
                    res.append(bank2.getAllAccounts() + "\n");
                }
                catch (Exception e ) { }


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
        BankClient bank = new BankClient(channelMonitor);
        //System.out.println(bank.listAllAccounts());

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
                    boolean res = channelMonitor.commit(xid);
                    if (res) System.out.println("Transferência bem-sucedida.");
                    else System.out.println("Transferência mal-sucedida.");
                    //System.out.println(bank.listAllAccounts());
                    break;
                case "2":
                    end = true;
                    break;
                default:
                    System.out.println("Comando não reconhecido.");
                    break;
            }
        }
    }
}
