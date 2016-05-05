package Bank;

import java.rmi.RemoteException;

/**
 * Created by carlosmorais on 15/12/15.
 */
public class Account  {
    private String id;
    private double balance;

    public Account(String id, double balance) throws RemoteException {
        this.id = id;
        this.balance = balance;
    }

    public String getId() {
        return id;
    }

    public double getBalance() {
        return balance;
    }

    @Override
    public String toString() {
        return "Account{" +
                "id='" + id + '\'' +
                ", balance=" + balance +
                '}';
    }

}
