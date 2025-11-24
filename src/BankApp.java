import java.io.*;
import java.util.*;
import java.time.LocalDate;

public class BankApp {

    public static final Scanner SCANNER = new Scanner(System.in);
    static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9_]{3,15}$");
    static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{6,}$");

    public static void main(String[] args) {
        String currentUser = null;

        while (true) {
            System.out.println("=== Welcome ===");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");
            String choice = SCANNER.nextLine().trim();

            if (choice.equals("1")) {
                currentUser = login();
                if (currentUser != null) {
                    System.out.println("Login successful. Welcome, " + currentUser + "!");
                    while (currentUser != null) {
                        System.out.println("\n--- Banking Menu ---");
                        System.out.println("1. Deposit");
                        System.out.println("2. Withdraw");
                        System.out.println("3. Transfer");
                        System.out.println("4. Report");
                        System.out.println("5. Logout");
                        System.out.print("Choose: ");
                        String action = SCANNER.nextLine().trim();
                        if (action.equals("1")) deposit(currentUser);
                        else if (action.equals("2")) withdraw(currentUser);
                        else if (action.equals("3")) transfer(currentUser);
                        else if (action.equals("4")) report(currentUser);
                        else if (action.equals("5")) { currentUser = null; System.out.println("Logged out."); }
                        else System.out.println("Invalid choice.");
                    }
                } else {
                    System.out.println("Login failed.");
                }
            } else if (choice.equals("2")) {
                registerUser();
            } else if (choice.equals("3")) {
                System.out.println("Goodbye.");
                break;
            } else {
                System.out.println("Invalid option.");
            }
        }
    }

    static void registerUser() {
        System.out.print("Enter new username: ");
        String newUser = SCANNER.nextLine().trim();
        if (newUser.isEmpty()) { System.out.println("Username cannot be empty."); return; }
        if (!isValidUsername(newUser)) { System.out.println("Invalid username. Only letters, digits and '_' allowed, length 3-15."); return; }

        System.out.print("Enter new password: ");
        String newPass = SCANNER.nextLine();
        if (!isValidPassword(newPass)) { System.out.println("Password must be at least 6 chars and include a digit and letter."); return; }

        // check if username exists
        try (BufferedReader reader = new BufferedReader(new FileReader("users.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 1 && parts[0].equals(newUser)) {
                    System.out.println("Username already exists.");
                    return;
                }
            }
        } catch (IOException e) {  }

        int next = 1;
        try (BufferedReader reader = new BufferedReader(new FileReader("users.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    String ac = parts[2].trim();
                    if (ac.startsWith("AC")) {
                        try {
                            int num = Integer.parseInt(ac.substring(2));
                            if (num >= next) next = num + 1;
                        } catch (NumberFormatException ex) {}
                    }
                }
            }
        } catch (IOException e) { }

        String accountNumber = String.format("AC%03d", next);

        try (PrintWriter pw = new PrintWriter(new FileWriter("users.csv", true))) {
            pw.println(newUser + "," + newPass + "," + accountNumber);
        } catch (IOException e) { System.out.println("Error writing users file."); return; }

        try (PrintWriter pw = new PrintWriter(new FileWriter("accounts.csv", true))) {
            pw.println(accountNumber + "," + newUser + ",0.0");
        } catch (IOException e) { System.out.println("Error writing accounts file."); }

        System.out.println("User registered with account number: " + accountNumber);
    }

    static String login() {
        System.out.print("Enter username: ");
        String user = SCANNER.nextLine().trim();
        System.out.print("Enter password: ");
        String pass = SCANNER.nextLine();

        try (BufferedReader reader = new BufferedReader(new FileReader("users.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2 && parts[0].equals(user) && parts[1].equals(pass)) {
                 
                    return user;
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading users file.");
        }
        return null;
    }

 
    static String getAccountNumber(String username) {
        try (BufferedReader reader = new BufferedReader(new FileReader("users.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 3 && parts[0].equals(username)) return parts[2];
            }
        } catch (IOException e) {}
        return null;
    }

    static String getUsernameByAccount(String accountNumber) {
        try (BufferedReader reader = new BufferedReader(new FileReader("accounts.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2 && parts[0].equals(accountNumber)) return parts[1];
            }
        } catch (IOException e) {}
        return null;
    }

    static Double safeParseAmount(String input) {
        try {
            double v = Double.parseDouble(input.trim());
            if (v <= 0) {
                System.out.println("Amount must be greater than 0.");
                return null;
            }
            return v;
        } catch (NumberFormatException e) {
            System.out.println("Invalid number. Please enter a valid number.");
            return null;
        }
    }

    static void deposit(String user) {
        String acct = getAccountNumber(user);
        if (acct == null) { System.out.println("Account not found."); return; }
        System.out.print("Enter amount to deposit: ");
        String s = SCANNER.nextLine();
        Double amount = safeParseAmount(s);
        if (amount == null) return;

        File inputFile = new File("accounts.csv");
        File tempFile = new File("accounts_temp.csv");

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             PrintWriter writer = new PrintWriter(new FileWriter(tempFile))) {
            String line;
            boolean found = false;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 3 && parts[0].equals(acct)) {
                    double bal = Double.parseDouble(parts[2]);
                    bal += amount;
                    writer.println(parts[0] + "," + parts[1] + "," + bal);
                    found = true;
                } else {
                    writer.println(line);
                }
            }
            if (!found) {
                writer.println(acct + "," + user + "," + amount);
            }
        } catch (IOException e) {
            System.out.println("Error processing accounts file.");
            return;
        }
        tempFile.renameTo(inputFile);

        try (PrintWriter pw = new PrintWriter(new FileWriter("transactions.csv", true))) {
            pw.println(acct + ",deposit," + amount + "," + LocalDate.now());
        } catch (IOException e) {
            System.out.println("Error writing transactions file.");
        }
        System.out.println("Deposited " + amount);
    }

    static void withdraw(String user) {
        String acct = getAccountNumber(user);
        if (acct == null) { System.out.println("Account not found."); return; }
        System.out.print("Enter amount to withdraw: ");
        String s = SCANNER.nextLine();
        Double amount = safeParseAmount(s);
        if (amount == null) return;

        File inputFile = new File("accounts.csv");
        File tempFile = new File("accounts_temp.csv");

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             PrintWriter writer = new PrintWriter(new FileWriter(tempFile))) {
            String line;
            boolean found = false;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 3 && parts[0].equals(acct)) {
                    double bal = Double.parseDouble(parts[2]);
                    if (bal < amount) {
                        System.out.println("Insufficient balance.");
                        writer.println(line); 
                        while ((line = reader.readLine()) != null) {
                            writer.println(line);
                        }
                        tempFile.renameTo(inputFile);
                        return;
                    }
                    bal -= amount;
                    writer.println(parts[0] + "," + parts[1] + "," + bal);
                    found = true;
                } else {
                    writer.println(line);
                }
            }
            if (!found) {
                System.out.println("Account not found in accounts file.");
            }
        } catch (IOException e) {
            System.out.println("Error processing accounts file.");
            return;
        }
        tempFile.renameTo(inputFile);

        try (PrintWriter pw = new PrintWriter(new FileWriter("transactions.csv", true))) {
            pw.println(acct + ",withdraw," + amount + "," + LocalDate.now());
        } catch (IOException e) {
            System.out.println("Error writing transactions file.");
        }
        System.out.println("Withdrew " + amount);
    }

    static void transfer(String user) {
        String acctFrom = getAccountNumber(user);
        if (acctFrom == null) { System.out.println("Account not found."); return; }
        System.out.print("Enter recipient username or account number: ");
        String target = SCANNER.nextLine().trim();
        String acctTo = target;
   
        if (!acctTo.startsWith("AC")) {
            String maybe = getAccountNumber(target);
            if (maybe != null) acctTo = maybe;
        }
        if (acctTo == null || acctTo.isEmpty()) { System.out.println("Recipient not found."); return; }
        System.out.print("Enter amount to transfer: ");
        String s = SCANNER.nextLine();
        Double amount = safeParseAmount(s);
        if (amount == null) return;

        File inputFile = new File("accounts.csv");
        File tempFile = new File("accounts_temp.csv");

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             PrintWriter writer = new PrintWriter(new FileWriter(tempFile))) {
            String line;
            double fromBal = -1, toBal = -1;
            String fromUser = null, toUser = null;
            List<String> allLines = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                allLines.add(line);
            }
            for (String ln : allLines) {
                String[] parts = ln.split(",");
                if (parts.length < 3) continue;
                if (parts[0].equals(acctFrom)) {
                    fromUser = parts[1];
                    fromBal = Double.parseDouble(parts[2]);
                }
                if (parts[0].equals(acctTo)) {
                    toUser = parts[1];
                    toBal = Double.parseDouble(parts[2]);
                }
            }
            if (fromBal < 0) { System.out.println("Sender account not found."); tempFile.delete(); return; }
            if (toBal < 0) { System.out.println("Recipient account not found."); tempFile.delete(); return; }
            if (fromBal < amount) { System.out.println("Insufficient balance."); tempFile.delete(); return; }
         
            for (String ln : allLines) {
                String[] parts = ln.split(",");
                if (parts.length < 3) { writer.println(ln); continue; }
                if (parts[0].equals(acctFrom)) {
                    writer.println(parts[0] + "," + parts[1] + "," + (fromBal - amount));
                } else if (parts[0].equals(acctTo)) {
                    writer.println(parts[0] + "," + parts[1] + "," + (toBal + amount));
                } else {
                    writer.println(ln);
                }
            }
        } catch (IOException e) {
            System.out.println("Error processing accounts file.");
            return;
        }
        tempFile.renameTo(inputFile);

        try (PrintWriter pw = new PrintWriter(new FileWriter("transactions.csv", true))) {
            pw.println(acctFrom + ",transfer," + amount + "," + LocalDate.now() + "," + acctTo);
        } catch (IOException e) {
            System.out.println("Error writing transactions file.");
        }
        System.out.println("Transferred " + amount + " to " + acctTo);
    }

    static void report(String user) {
        String acct = getAccountNumber(user);
        if (acct == null) { System.out.println("Account not found."); return; }
        System.out.println("--- Account Report for " + user + " (" + acct + ") ---");
        // print current balance
        try (BufferedReader reader = new BufferedReader(new FileReader("accounts.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 3 && parts[0].equals(acct)) {
                    System.out.println("Current balance: " + parts[2]);
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading accounts file.");
        }
        System.out.println("Transactions:");
        try (BufferedReader reader = new BufferedReader(new FileReader("transactions.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(acct + ",") || line.contains("," + acct + ",") || line.endsWith(","+acct)) {
                    System.out.println(line);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading transactions file.");
        }
    }

    static boolean isValidUsername(String u) {
        if (u == null) return false;
        Matcher m = USERNAME_PATTERN.matcher(u);
        return m.matches();
    }

    static boolean isValidPassword(String p) {
        if (p == null) return false;
        Matcher m = PASSWORD_PATTERN.matcher(p);
        return m.matches();
    }
}
