package Bank;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Created by carlosmorais on 15/12/15.
 */
public class Bank extends UnicastRemoteObject implements RemoteBank {

    public Bank() throws RemoteException{
        super();
    }

    @Override
    public void transfer(int xid, String idSource, String idDestiny, double amount) {
        //Vale a pena ter Threads para as transferencias???
        //e ter um maps dos servers? nao funciona se um reiniciar?
        new Thread(() -> {
            try {
                RemoteBankServer bankS = (RemoteBankServer) Naming.lookup("//localhost/myBank"+idSource.substring(0,3));
                RemoteBankServer bankD = (RemoteBankServer) Naming.lookup("//localhost/myBank"+idDestiny.substring(0,3));
                String s = idSource.substring(0,3);
                String d = idDestiny.substring(0,3);
                System.out.println(idSource + " " + idDestiny);
                if(s.equals(d)) {
                    bankS.transfer(xid, idSource, idDestiny, amount);
                } else {
                    bankS.withdraw(xid, idSource, amount);
                    bankD.deposit(xid, idDestiny, amount);
                }
            } catch (NotBoundException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public void deposit(int xid, String idAccount, double amount) throws RemoteException {
        try {
            RemoteBankServer bank = (RemoteBankServer) Naming.lookup("//localhost/myBank" + idAccount.substring(0, 3));
            bank.deposit(xid, idAccount, amount);
        } catch (NotBoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void withdraw(int xid, String idAccount, double amount) throws RemoteException {
        try {
            RemoteBankServer bank = (RemoteBankServer) Naming.lookup("//localhost/myBank" + idAccount.substring(0, 3));
            bank.withdraw(xid, idAccount, amount);
        } catch (NotBoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }


    @Override
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

    @Override
    public Double getAccountBalance(String idAccount) throws RemoteException {
        try {
            RemoteBankServer bank = (RemoteBankServer) Naming.lookup("//localhost/myBank" + idAccount.substring(0, 3));
            return bank.getAccountBalance(idAccount);
        } catch (NotBoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

}
