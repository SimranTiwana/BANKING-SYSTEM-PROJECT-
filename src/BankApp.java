import java.io.*;
import java.util.*;

public class BankApp {

    public static final Scanner SCANNER = new Scanner(System.in);

    public static void main(String[] args) {
        String currentUser = null;

        while (true) {
            System.out.println("=== Welcome ===");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");
            String choice = SCANNER.nextLine();

            if (choice.equals("1")) {
                currentUser = loginUser();
                if (currentUser != null) {
                    while (true) {
                        System.out.println("\n--- Menu ---");
                        System.out.println("1. Deposit");
                        System.out.println("2. Withdraw");
                        System.out.println("3. Transfer");
                        System.out.println("4. Report");
                        System.out.println("5. Logout");
                        System.out.print("Choose an option: ");
                        String action = SCANNER.nextLine();

                        if (action.equals("1")) {
                            deposit(currentUser);
                        } else if (action.equals("2")) {
                            withdraw(currentUser);
                        } else if (action.equals("3")) {
                            transfer(currentUser);
                        } else if (action.equals("4")) {
                            report(currentUser);
                        } else if (action.equals("5")) {
                            currentUser = null;
                            System.out.println("Logged out.");
                            break;
                        } else {
                            System.out.println("Invalid option.");
                        }
                    }
                }

            } else if (choice.equals("2")) {
                registerUser();

            } else if (choice.equals("3")) {
                System.out.println("Goodbye!");
                break;

            } else {
                System.out.println("Invalid option.");
            }
        }
    }

    static void registerUser() {
        Scanner sc = SCANNER;

        System.out.print("Enter new username: ");
        String newUser = SCANNER.nextLine().trim();
        if (newUser.isEmpty()){
            System.out.println("Username cannot be empty.");
            return; 
        }
        System.out.print("Enter new password: ");
        String newPass = SCANNER.nextLine();
        if (newPass.isEmpty()){
            System.out.println("Password cannot be empty."); 
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader("users.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.split(",")[0].equals(newUser)) {
                    System.out.println("Username already exists.");
                    return;
                }
            }
        } catch (IOException e) {
        }

        try (PrintWriter pw = new PrintWriter(new FileWriter("users.csv", true))) {
            pw.println(newUser + "," + newPass);
            System.out.println("User registered.");
        } catch (IOException e) {
            System.out.println("Error writing users file.");
        }

        try (PrintWriter pw = new PrintWriter(new FileWriter("accounts.csv", true))) {
            pw.println(newUser + ",0");
        } catch (IOException e) {
            System.out.println("Error writing accounts file.");
        }
    }

    static String loginUser() {
        Scanner sc = SCANNER;

        System.out.print("Username: ");
        String user = sc.nextLine();
        System.out.print("Password: ");
        String pass = sc.nextLine();

        try (BufferedReader reader = new BufferedReader(new FileReader("users.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[0].equals(user) && parts[1].equals(pass)) {
                    System.out.println("Login successful.\n");
                    return user;
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading users file.");
        }

        System.out.println("Invalid credentials.");
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
        Scanner sc = SCANNER;

        System.out.print("Enter deposit amount: ");
       Double amount = safeParseAmount(sc.nextLine());
       if (amount == null) return;


        File inputFile = new File("accounts.csv");
        File tempFile = new File("accounts_temp.csv");

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             PrintWriter pw = new PrintWriter(new FileWriter(tempFile))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[0].equals(user)) {
                    double balance = Double.parseDouble(parts[1]) + amount;
                    pw.println(user + "," + balance);
                    System.out.println("Deposited " + amount + ". New balance: " + balance);
                } else {
                    pw.println(line);
                }
            }

        } catch (IOException e) {
            System.out.println("Error processing accounts file.");
            return;
        }

        inputFile.delete();
        tempFile.renameTo(inputFile);

        try (PrintWriter pw = new PrintWriter(new FileWriter("transactions.csv", true))) {
            pw.println(user + ",deposit," + amount);
        } catch (IOException e) {
            System.out.println("Error writing transactions file.");
        }
    }

    static void withdraw(String user) {
        Scanner sc = SCANNER;

        System.out.print("Enter withdraw amount: ");
        Double amount = safeParseAmount(sc.nextLine());
        if (amount == null) return;

        double currentBalance = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader("accounts.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[0].equals(user)) {
                    currentBalance = Double.parseDouble(parts[1]);
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading accounts file.");
            return;
        }

        if (currentBalance < amount) {
            System.out.println("Insufficient funds.");
            return;
        }

        File inputFileW = new File("accounts.csv");
        File tempFileW = new File("accounts_temp.csv");

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFileW));
             PrintWriter pw = new PrintWriter(new FileWriter(tempFileW))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[0].equals(user)) {
                    double balance = Double.parseDouble(parts[1]) - amount;
                    pw.println(user + "," + balance);
                    System.out.println("Withdrew " + amount + ". New balance: " + balance);
                } else {
                    pw.println(line);
                }
            }

        } catch (IOException e) {
            System.out.println("Error processing accounts file.");
            return;
        }

        inputFileW.delete();
        tempFileW.renameTo(inputFileW);

        try (PrintWriter pw = new PrintWriter(new FileWriter("transactions.csv", true))) {
            pw.println(user + ",withdraw," + amount);
        } catch (IOException e) {
            System.out.println("Error writing transactions file.");
        }
    }

    static void transfer(String user) {
    Scanner sc = SCANNER;

    System.out.print("Enter recipient username: ");
    String toUser = sc.nextLine().trim();

    if (toUser.equalsIgnoreCase(user)) {
        System.out.println("Cannot transfer to self.");
        return;
    }

    System.out.print("Enter transfer amount: ");
    Double amountObj = safeParseAmount(sc.nextLine());
    if (amountObj == null) return;
    double amount = amountObj;

    double fromBalance = 0, toBalance = 0;
    boolean toFound = false;
    boolean fromFound = false;

    try (BufferedReader reader = new BufferedReader(new FileReader("src/accounts.csv"))) {
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(",");
            if (parts.length < 2) continue;

            if (parts[0].equals(user)) {
                fromBalance = Double.parseDouble(parts[1]);
                fromFound = true;
            }
            if (parts[0].equals(toUser)) {
                toBalance = Double.parseDouble(parts[1]);
                toFound = true;
            }
        }
    } catch (IOException e) {
        System.out.println("Error reading accounts file.");
        return;
    }

    if (!toFound) {
        System.out.println("Recipient not found.");
        return;
    }

    if (!fromFound) {
        System.out.println("Sender account not found.");
        return;
    }

    if (fromBalance < amount) {
        System.out.println("Insufficient funds.");
        return;
    }

    File inputFileT = new File("src/accounts.csv");
    File tempFileT = new File("src/accounts_temp.csv");

    try (BufferedReader reader = new BufferedReader(new FileReader(inputFileT));
         PrintWriter pw = new PrintWriter(new FileWriter(tempFileT))) {

        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(",");
            if (parts.length < 2) {
                pw.println(line);
                continue;
            }

            if (parts[0].equals(user)) {
                double newBal = Double.parseDouble(parts[1]) - amount;
                pw.println(user + "," + newBal);
            } else if (parts[0].equals(toUser)) {
                double newBal = Double.parseDouble(parts[1]) + amount;
                pw.println(toUser + "," + newBal);
            } else {
                pw.println(line);
            }
        }

    } catch (IOException e) {
        System.out.println("Error processing accounts file.");
        return;
    }

    tempFileT.renameTo(inputFileT);

    System.out.println("Transferred " + amount + " to " + toUser);

    try (PrintWriter pw = new PrintWriter(new FileWriter("src/transactions.csv", true))) {
        pw.println(user + ",transfer," + amount + "," + toUser + "," + java.time.LocalDate.now());
    } catch (IOException e) {
        System.out.println("Error writing transactions file.");
    }
}


    static void report(String user) {
        System.out.println("--- Transactions ---");

        try (BufferedReader reader = new BufferedReader(new FileReader("transactions.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(user + ",") || line.contains("," + user + ",")) {
                    System.out.println(line);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading transactions file.");
        }
    }
}
