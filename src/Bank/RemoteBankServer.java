package Bank;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by carlosmorais on 21/12/15.
 */
public interface RemoteBankServer extends Remote {
    void deposit(int idx, String idAccount, double amount) throws RemoteException;
    void withdraw(int idx, String idAccount, double amount) throws RemoteException;

    //Auxiliares & Extras
    String getAllAccounts() throws RemoteException;
    Double getAccountBalance(String idAccount) throws RemoteException;
}
