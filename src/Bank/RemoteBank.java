package Bank;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by carlosmorais on 15/12/15.
 */
public interface RemoteBank extends Remote {
    void tranfer(int xid, String idSource, String idDesteny, double amount) throws RemoteException;

    //Auxilliares & Extras
    void deposit(int xid, String idAccount, double amout) throws RemoteException;
    void withdraw(int xid, String idAccount, double amout) throws RemoteException;
    String listAllAccounts() throws RemoteException ;
    Double getAccountBalance(String idAccount) throws RemoteException ;
}
