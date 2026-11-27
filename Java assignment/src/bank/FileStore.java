package bank;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Load and save bank data using simple text files and arrays (no collections).
 */
public final class FileStore {

    public static final String USERS_FILE = "users.txt";
    public static final String ACCOUNTS_FILE = "accounts.txt";
    public static final String TRANSACTIONS_FILE = "transactions.txt";
    public static final String LOANS_FILE = "loans.txt";
    public static final String FD_FILE = "fixed_deposits.txt";
    private static final String DEFAULT_ADMIN_LINE =
            "admin,admin123,Super Admin,+60123456789,000000000000,admin@bank.com,ADMIN,A-000,APPROVED";
    private FileStore() {
    }

    // Trim every cell after split
    private static void trimParts(String[] parts) {
        int i;
        for (i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }
    }

    /**
     * Build one User object from a CSV line.
     */
    public static User parseUserLine(String line) {
        String[] parts = line.split(",");
        trimParts(parts);
        if (parts.length < 9) { // NOW EXPECTING 9 PARTS
            return null;
        }
        String userId = parts[0];
        String password = parts[1];
        String fullName = parts[2];
        String phoneNumber = parts[3];
        String icNumber = parts[4];
        String email = parts[5];
        String role = parts[6];
        String customerId = parts[7];
        String status = parts[8];
        
        if ("ADMIN".equalsIgnoreCase(role)) {
            return new Admin(userId, password, fullName, phoneNumber, icNumber, email, customerId, status);
        }
        return new Customer(userId, password, fullName, phoneNumber, icNumber, email, customerId, status);
    }

    /**
     * Build one Account object from a CSV line.
     */
    public static Account parseAccountLine(String line) {
        String[] parts = line.split(",");
        trimParts(parts);
        if (parts.length < 5) {
            return null;
        }
        String accountId = parts[0];
        String accountType = parts[1];
        double balance = 0.0;
        try {
            balance = Double.parseDouble(parts[2]);
        } catch (Exception e) {
            balance = 0.0;
        }
        String customerId = parts[3];
        String status = parts[4];
        double overdraft = 500.0;
        if (parts.length >= 6) {
            try {
                overdraft = Double.parseDouble(parts[5]);
            } catch (Exception e) {
                overdraft = 500.0;
            }
        }
        if ("Current".equalsIgnoreCase(accountType)) {
            return new CurrentAccount(accountId, balance, customerId, status, overdraft);
        }
        return new SavingsAccount(accountId, balance, customerId, status);
    }

    /**
     * Ensure users file exists; if missing or empty, write default admin.
     */
    public static void ensureDefaultUsersFile() throws IOException {
        File f = new File(USERS_FILE);
        if (!f.exists()) {
            BufferedWriter bw = new BufferedWriter(new FileWriter(f));
            bw.write(DEFAULT_ADMIN_LINE);
            bw.newLine();
            bw.close();
        } else {
            BufferedReader br = new BufferedReader(new FileReader(f));
            String first = br.readLine();
            br.close();
            if (first == null || first.trim().length() == 0) {
                BufferedWriter bw = new BufferedWriter(new FileWriter(f));
                bw.write(DEFAULT_ADMIN_LINE);
                bw.newLine();
                bw.close();
            }
        }
    }

    /**
     * Load users into array; return count used.
     */
    public static int loadUsers(User[] out) throws IOException {
        ensureDefaultUsersFile();
        // Read line by line; no collections, only array fill
        BufferedReader br = new BufferedReader(new FileReader(USERS_FILE));
        int n = 0;
        String line;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.length() == 0) {
                continue;
            }
            User u = parseUserLine(line);
            if (u != null && n < out.length) {
                out[n] = u;
                n = n + 1;
            }
        }
        br.close();
        return n;
    }

    /**
     * Save users[0..count-1] to file (overwrite).
     */
public static void saveUsers(User[] users, int count) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(USERS_FILE));
        int i;
        for (i = 0; i < count; i++) {
            User u = users[i];
            if (u == null) continue;

            bw.write(u.getUserId() + "," + u.getPassword() + "," + u.getFullName() + ","
                    + u.getPhoneNumber() + "," + u.getIcNumber() + "," + u.getEmail() + ","
                    + u.getRole() + "," + u.getCustomerId() + "," + u.getStatus());
            bw.newLine();
        }
        bw.close();
    }

    /**
     * Load accounts into array; return count used.
     */
    public static int loadAccounts(Account[] out) throws IOException {
        File f = new File(ACCOUNTS_FILE);
        if (!f.exists()) {
            return 0;
        }
        BufferedReader br = new BufferedReader(new FileReader(f));
        int n = 0;
        String line;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.length() == 0) {
                continue;
            }
            Account a = parseAccountLine(line);
            if (a != null && n < out.length) {
                out[n] = a;
                n = n + 1;
            }
        }
        br.close();
        return n;
    }

    /**
     * Save accounts[0..count-1] to file (overwrite).
     */
    public static void saveAccounts(Account[] accounts, int count) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(ACCOUNTS_FILE));
        int i;
        for (i = 0; i < count; i++) {
            Account a = accounts[i];
            if (a == null) {
                continue;
            }
            if (a instanceof CurrentAccount) {
                CurrentAccount c = (CurrentAccount) a;
                bw.write(a.getAccountId() + "," + a.getAccountType() + "," + a.getBalance() + ","
                        + a.getCustomerId() + "," + a.getStatus() + "," + c.getOverdraftLimit());
            } else {
                bw.write(a.getAccountId() + "," + a.getAccountType() + "," + a.getBalance() + ","
                        + a.getCustomerId() + "," + a.getStatus() + ",0");
            }
            bw.newLine();
        }
        bw.close();
    }

    /**
     * Append one transaction line (append mode).
     */
    public static void appendTransaction(Transaction tx) throws IOException {
        FileWriter fw = new FileWriter(TRANSACTIONS_FILE, true);
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(tx.toString());
        bw.newLine();
        bw.close();
    }

    /**
     * Create timestamp string for a new transaction row.
     */
    public static String nowTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date());
    }

    public static int loadLoans(Loan[] out) throws IOException {
        File f = new File("loans.txt");
        if (!f.exists()) return 0;
        BufferedReader br = new BufferedReader(new FileReader(f));
        int n = 0;
        String line;
        while ((line = br.readLine()) != null) {
            String[] p = line.split(",");
        // Now expecting 5 parts: cid, aid, principal, remaining, status
            if (p.length >= 5 && n < out.length) {
                out[n] = new Loan(p[0].trim(), p[1].trim(), 
                              Double.parseDouble(p[2].trim()), 
                              Double.parseDouble(p[3].trim()), 
                              p[4].trim());
                n++;
            }
        }
    br.close();
    return n;
    }

    public static void saveLoans(Loan[] loans, int count) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(LOANS_FILE));
        for (int i = 0; i < count; i++) {
            Loan L = loans[i];
            if (L == null) {
                continue;
            }
            bw.write(L.toString());
            bw.newLine();
        }
        bw.close();
    }

    public static int loadFDs(FixedDeposit[] out) throws IOException {
        File f = new File(FD_FILE);
        if (!f.exists()) return 0;
        BufferedReader br = new BufferedReader(new FileReader(f));
        int n = 0;
        String line;
        while ((line = br.readLine()) != null) {
            String[] p = line.split(",");
            if (p.length >= 6 && n < out.length) {
                out[n] = new FixedDeposit(p[0].trim(), p[1].trim(), p[2].trim(),
                                          Double.parseDouble(p[3].trim()),
                                          Integer.parseInt(p[4].trim()), 
                                          p[5].trim());
                n++;
            }
        }
        br.close();
        return n;
    }

    public static void saveFDs(FixedDeposit[] fds, int count) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(FD_FILE));
        for (int i = 0; i < count; i++) {
            if (fds[i] != null) {
                bw.write(fds[i].toString());
                bw.newLine();
            }
        }
        bw.close();
    }
}
