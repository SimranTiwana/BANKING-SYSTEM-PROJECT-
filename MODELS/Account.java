// Account.java - represents a bank account (account number, owner, balance)
public class Account {
    private int accountNumber;
    private String userName;
    private double balance;

    public Account(int accountNumber, String userName, double balance) {
        this.accountNumber = accountNumber;
        this.userName = userName;
        this.balance = balance;
    }

    public int getAccountNumber() { return accountNumber; }
    public String getUserName() { return userName; }
    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }
}
