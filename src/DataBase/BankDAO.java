package DataBase;

import Bank.Account;
import org.apache.derby.jdbc.EmbeddedXADataSource;

import java.rmi.RemoteException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by carlosmorais on 17/12/15.
 */
public class BankDAO {
    private static String DATABASE_NAME = "bankAccounts";

    public BankDAO() {}

    public void GenerateDB(int bankID, EmbeddedXADataSource ds) throws SQLException {
        String dbName = DATABASE_NAME+bankID;
        ds.setDatabaseName(dbName);
        ds.setCreateDatabase("create");
        Statement st = ds.getConnection().createStatement();

        try {
            st.executeUpdate("DROP TABLE accounts");
            st.executeUpdate("DROP TABLE dbInfo");
        }
        catch (SQLSyntaxErrorException sqle){ sqle.printStackTrace();}

        st.executeUpdate("create table accounts (id varchar(10) PRIMARY KEY, balance double)");
        st.executeUpdate("create table dbInfo(idGen int)");

        if(bankID==100) {
            st.executeUpdate("insert into accounts values('1001000',1000.0), ('1001001',1000.0), ('1001002',1000.0), ('1001003',1000.0)");
            st.executeUpdate("insert into dbInfo values (1004)");
        }
        else if(bankID==101) {
            st.executeUpdate("insert into accounts values ('1011000',0.0), ('1011001',0.0)");
            st.executeUpdate("insert into dbInfo values (1002)");
        }
        else
            st.executeUpdate("insert into dbInfo values (1000)");
    }

    public void transfer(Connection connection, String idS, String idD, double amount) throws SQLException, RemoteException {
        String updateStatement = "UPDATE accounts SET balance = balance - ? WHERE id = ? ";
        PreparedStatement statement = connection.prepareStatement(updateStatement);
        statement.setDouble(1,amount);
        statement.setString(2,idS);
        statement.execute();
        updateStatement = "UPDATE accounts SET balance = balance + ? WHERE id = ? ";
        statement = connection.prepareStatement(updateStatement);
        statement.setDouble(1,amount);
        statement.setString(2,idD);
        statement.execute();
        statement.close();
        connection.close();
    }

    public void deposit(Connection connection, String id, double amount) throws SQLException, RemoteException {
        String updateStatement = "UPDATE accounts SET balance = balance + ? WHERE id = ? ";
        PreparedStatement statement = connection.prepareStatement(updateStatement);
        statement.setDouble(1,amount);
        statement.setString(2,id);
        statement.execute();
        statement.close();
        connection.close();
    }


    public void withdraw(Connection connection, String id, double amount) throws SQLException, RemoteException {
        String updateStatement = "UPDATE accounts SET balance = balance - ? WHERE id = ? ";
        PreparedStatement statement = connection.prepareStatement(updateStatement);
        statement.setDouble(1,amount);
        statement.setString(2,id);
        statement.execute();
        statement.close();
        connection.close();
    }


    public double getBalance(Connection connection, String id) throws SQLException {
        String updateStatement = "SELECT balance FROM accounts WHERE id = ?";
        PreparedStatement statement = connection.prepareStatement(updateStatement);
        statement.setString(1,id);
        double balance=0.0;
        ResultSet rs = statement.executeQuery();

        if(rs.next()){
            balance = rs.getDouble(1);
        }
        //else exception??
        statement.close();
        connection.close();
        return balance;
    }



    public Account getAccount(Connection connection, String id) throws RemoteException, SQLException {
        Account account = null;
        String updateStatement = "SELECT * FROM accounts WHERE id = ?";
        PreparedStatement statement = connection.prepareStatement(updateStatement);
        statement.setString(1,id);
        ResultSet rs = statement.executeQuery();

        if(rs.next()){
            account = new Account(rs.getString(1), rs.getDouble(2));
        }

        statement.close();
        connection.close();
        return account;
    }


    public List<Account> getAllAccounts(Connection connection) throws RemoteException, SQLException {
        List<Account> accounts = new ArrayList<Account>();
        String updateStatement = "SELECT * FROM accounts";
        PreparedStatement statement = connection.prepareStatement(updateStatement);
        ResultSet rs = statement.executeQuery();

        while(rs.next()){
            Account account = new Account(rs.getString(1), rs.getDouble(2));
            accounts.add(account);
        }

        statement.close();
        connection.close();
        return accounts;
    }

}
