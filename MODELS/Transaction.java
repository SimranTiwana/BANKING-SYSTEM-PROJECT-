// Transaction.java - represents a banking transaction record
public class Transaction {
    private int accountNumber;
    private String type;
    private double amount;
    private String date;

    public Transaction(int accountNumber, String type, double amount, String date) {
        this.accountNumber = accountNumber;
        this.type = type;
        this.amount = amount;
        this.date = date;
    }

    public int getAccountNumber() { return accountNumber; }
    public String getType() { return type; }
    public double getAmount() { return amount; }
    public String getDate() { return date; }
}
