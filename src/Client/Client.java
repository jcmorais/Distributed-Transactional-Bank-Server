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

            bankD.deposit(xid, idDestiny, amount);
            bankS.withdraw(xid, idSource, amount);

            /*
            //teste XXXX
            try {
                System.out.println("vou adormecer esperar 25s");
                Thread.sleep(25000);
                System.out.println("acordei!!!");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            */

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
    }
}
