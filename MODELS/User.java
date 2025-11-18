// User.java - stores a user’s credentials and linked account number
public class User {
    private String username;
    private String password;
    private int accountNumber;
    
    public User(String username, String password, int accountNumber) {
        this.username = username;
        this.password = password;
        this.accountNumber = accountNumber;
    }
    
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public int getAccountNumber() { return accountNumber; }
}
