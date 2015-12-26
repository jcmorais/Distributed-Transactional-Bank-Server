package Server;

import Bank.Bank;
import Bank.BankServer;

import java.rmi.Naming;

/**
 * Created by carlosmorais on 15/12/15.
 */
public class Server {
    public static void main(String[] args) throws Exception {
        //Start de Bank
        Bank bank = new Bank();
        Naming.rebind("myBank", bank);
        System.out.println("Bank start");

        //Start BankServer100
        BankServer bankServer100 = new BankServer(100);
        Naming.rebind("myBank100", bankServer100);
        System.out.println("BankServer100 start");

        //Start BankServer101
        BankServer bankServer101 = new BankServer(101);
        Naming.rebind("myBank101", bankServer101);
        System.out.println("BankServer101 start");
    }
}
