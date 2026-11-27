package bank;

import java.util.Scanner;

/**
 * Console menus and business logic using only arrays.
 */
public class BankApp {

    private static final int MAX_USERS = 100;
    private static final int MAX_ACCOUNTS = 100;

    private final User[] users;
    private int userCount;
    private final Account[] accounts;
    private int accountCount;
    private final Scanner scanner;
    private User loggedInUser;
    private final Loan[] loans;
    private int loanCount;
    private final FixedDeposit[] fds;
    private int fdCount;
    private double globalSavingsRate = 0.02;

   public BankApp(Scanner scanner) {
        this.scanner = scanner;
        this.users = new User[MAX_USERS];
        this.userCount = 0;
        this.accounts = new Account[MAX_ACCOUNTS];
        this.accountCount = 0;
        this.fds = new FixedDeposit[100];
        this.fdCount = 0;
        // Add initialization for loans
        this.loans = new Loan[100]; 
        this.loanCount = 0;
        
        this.loggedInUser = null;
    }

  // Find user index by login id, or -1
    private int indexOfUserId(String userId) {
        int i;
        for (i = 0; i < userCount; i++) {
            if (users[i] != null && users[i].getUserId().equals(userId)) {
                return i;
            }
        }
        return -1;
    }

    // Find account index by account id, or -1
    private int indexOfAccountId(String accountId) {
        int i;
        for (i = 0; i < accountCount; i++) {
            if (accounts[i] != null && accounts[i].getAccountId().equals(accountId)) {
                return i;
            }
        }
        return -1;
    }

    // Reload from disk
    private void loadAll() {    
        try {
            FileStore.ensureDefaultUsersFile();
            userCount = FileStore.loadUsers(users);
            accountCount = FileStore.loadAccounts(accounts);
            // Add load loans
            loanCount = FileStore.loadLoans(loans);
            fdCount = FileStore.loadFDs(fds); 
            
        } catch (Exception e) {
            System.out.println("Load error: " + e.getMessage());
        }
    }

    // Save users and accounts
    private void saveAll() {
        try {
            FileStore.saveUsers(users, userCount);
            FileStore.saveAccounts(accounts, accountCount);
            // Add save loans
            FileStore.saveLoans(loans, loanCount);
    
            FileStore.saveFDs(fds, fdCount); 
            
        } catch (Exception e) {
            System.out.println("Save error: " + e.getMessage());
        }
    }
    // Log one transaction line (append file)
    private void logTx(String type, double amount, String accountId) {
        try {
            String ts = FileStore.nowTimestamp();
            String refId = "TXN-" + System.currentTimeMillis(); 
            Transaction tx = new Transaction(refId, ts, type, amount, accountId);
            FileStore.appendTransaction(tx);
        } catch (Exception e) {
            System.out.println("Log error: " + e.getMessage());
        }
    }

    // Next customer id like A-001
    private String nextCustomerId() {
        int maxNum = 0;
        int i;
        for (i = 0; i < userCount; i++) {
            if (users[i] == null) {
                continue;
            }
            String cid = users[i].getCustomerId();
            if (cid == null || cid.length() == 0) {
                continue;
            }
            int dash = cid.indexOf('-');
            if (dash < 0 || dash + 1 >= cid.length()) {
                continue;
            }
            try {
                int num = Integer.parseInt(cid.substring(dash + 1));
                if (num > maxNum) {
                    maxNum = num;
                }
            } catch (Exception e) {
                // skip bad id
            }
        }
        int next = maxNum + 1;
        if (next < 1) {
            next = 1;
        }
        // Format A-001 style
        String suffix;
        if (next < 10) {
            suffix = "00" + next;
        } else if (next < 100) {
            suffix = "0" + next;
        } else {
            suffix = Integer.toString(next);
        }
        return "A-" + suffix;
    }

    // Next account id ACC-001 (for extra card requests)
    private String nextAccountId() {
        int maxNum = 0;
        int i;
        for (i = 0; i < accountCount; i++) {
            if (accounts[i] == null) {
                continue;
            }
            String aid = accounts[i].getAccountId();
            if (aid == null || !aid.startsWith("ACC-")) {
                continue;
            }
            try {
                int num = Integer.parseInt(aid.substring(4));
                if (num > maxNum) {
                    maxNum = num;
                }
            } catch (Exception e) {
                // skip
            }
        }
        int next = maxNum + 1;
        String suffix;
        if (next < 10) {
            suffix = "00" + next;
        } else if (next < 100) {
            suffix = "0" + next;
        } else {
            suffix = Integer.toString(next);
        }
        return "ACC-" + suffix;
    }

    /**
     * Main program loop.
     */
    public void run() {
        System.out.println("======================= Digital Bank (Console) ================================");
        // Read users and accounts from text files into arrays
        loadAll();
        boolean running = true;
        // Top-level menu loop
        while (running) {

            System.out.println("1 Register");
            System.out.println("2 Login");
            System.out.println("0 Exit");
            System.out.print("Choose: ");
            String choice = scanner.nextLine().trim();
            if ("1".equals(choice)) {
                // New customer + pending account, then save arrays to disk
                registerCustomer();
                saveAll();
            } else if ("2".equals(choice)) {
                if (loginUser()) {
                    // Route by role (polymorphism on User reference)
                    if (loggedInUser instanceof Admin) {
                        adminMenu();
                    } else {
                        customerMenu();
                    }
                }
                loggedInUser = null;
                // Reload so file changes from admin are visible
                loadAll();
            } else if ("0".equals(choice)) {
                running = false;
            } else {
                System.out.println("Invalid choice.");
            }
        }
        System.out.println("Goodbye.");
    }

    // Register new customer + one pending account
// Register new customer + one pending account
    private void registerCustomer() {
        if (userCount >= MAX_USERS || accountCount >= MAX_ACCOUNTS) {
            System.out.println("System full.");
            return;
        }
        System.out.print("(Press enter for exit)New user id: ");
        String uid = scanner.nextLine().trim();
        if (uid.length() == 0) {
            System.out.println("Empty id. Returnning to login page.");
            return;
        }
        if (indexOfUserId(uid) >= 0) {
            System.out.println("User id already exists.");
            return;
        }
        System.out.print("Password: ");
        String pw = scanner.nextLine();
    
        System.out.print("Full name: ");
        String name = scanner.nextLine().trim();
        if (name.length() == 0) {
            name = uid;
        }

        // --- NEW: STRICT VALIDATION LOOPS ---
        String phone = "";
        while (true) {
            System.out.print("Phone Number (Exactly 9 digits): +60 ");
            phone = scanner.nextLine().trim();
            if (phone.matches("^\\d{9}$")) {
                phone = "+60" + phone;
                break;
            } else {
                System.out.println("Error: Phone number MUST be exactly 9 digits. Please try again.");
            }
        }

        String ic = "";
        while (true) {
            System.out.print("IC Number (Exactly 12 digits): ");
            ic = scanner.nextLine().trim();
            if (ic.matches("^\\d{12}$")) { 
                break;
            } else {
                System.out.println("Error: IC Number MUST be exactly 12 digits without '-'. Please try again.");
            }
        }

        String email = "";
        while (true) {
            System.out.print("Email Address: ");
            email = scanner.nextLine().trim();
            if (email.matches("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+)\\.[A-Za-z]{2,}$")) {
                break;
            } else {
                System.out.println("Error: Invalid Email format. Please try again.");
            }
        }

        // --- Selection & Error Handling ---
        System.out.println("Select Preferred Account Type:");
        System.out.println("1. Savings Account");
        System.out.println("2. Current Account");
        System.out.print("Enter choice (1 or 2): ");
        String choice = scanner.nextLine().trim();

        if (!"1".equals(choice) && !"2".equals(choice)) {
            System.out.println("Error: Invalid selection. Registration cancelled.");
            return;
        }

        // Only create objects AFTER validation passes
        String newCid = nextCustomerId();
        Customer c = new Customer(uid, pw, name, phone, ic, email, newCid, "PENDING");
        
        Account acc;
        if ("1".equals(choice)) {
            acc = new SavingsAccount(newCid, 0.0, newCid, "PENDING");
        } else {
            acc = new CurrentAccount(newCid, 0.0, newCid, "PENDING", 500.0);
        }

        users[userCount] = c;
        userCount = userCount + 1;
        accounts[accountCount] = acc;
        accountCount = accountCount + 1;
        
        System.out.println("Registered as PENDING. Wait for admin approval.");
    }

    // Login; sets loggedInUser
    private boolean loginUser() {
        System.out.print("(Press enter for exit)User id: ");
        String uid = scanner.nextLine().trim();
        System.out.print("Password: ");
        String pw = scanner.nextLine();
        int idx = indexOfUserId(uid);
        if (idx < 0) {
            System.out.println("User not found. Returnning to login page");
            return false;
        }
        User u = users[idx];
        if (!u.getPassword().equals(pw)) {
            System.out.println("Wrong password.");
            return false;
        }
        if (u instanceof Customer) {
            if ("PENDING".equals(u.getStatus())) {
                System.out.println("Your account is PENDING. Cannot login.");
                return false;
            }
        }
        loggedInUser = u;
        System.out.println("Welcome, " + u.getFullName());
        return true;
    }

    // Customer submenu
    private void customerMenu() {
        boolean go = true;
        while (go) {
            System.out.println();
            System.out.println("-- Customer Menu --");
            System.out.println("1 List my accounts");
            System.out.println("2 Deposit");
            System.out.println("3 Withdraw");
            System.out.println("4 Transfer");
            System.out.println("5 Apply for extra card (pending)");
            System.out.println("6 Forex Calculator"); 
            System.out.println("7 Apply for Loan");  
            System.out.println("8 Repay Loan");
            System.out.println("9 Open Fixed Deposit");
            System.out.println("10 Withdraw Fixed Deposit");  
            System.out.println("11 View Transaction History");
            System.out.println("12 Generate Monthly Statement");    
            System.out.println("0 Logout");
            System.out.print("Choose: ");
            String c = scanner.nextLine().trim();
            
            if ("1".equals(c)) {
                listCustomerAccounts();
            } else if ("2".equals(c)) {
                doDeposit();
            } else if ("3".equals(c)) {
                doWithdraw();
            } else if ("4".equals(c)) {
                doTransfer();
            } else if ("5".equals(c)) {
                applyExtraCard();
            } else if ("6".equals(c)) {
                doForex();
            } else if ("7".equals(c)) {
                applyLoan(); // Call loan application method
            } else if ("8".equals(c)) {
                repayLoan(); // Call loan repayment method
            } else if ("9".equals(c)) {
                openFixedDeposit();
            } else if ("10".equals(c)) {
                withdrawFixedDeposit();
            } else if ("11".equals(c)) {  
                viewMyTransactions();
            } else if ("12".equals(c)) {
                generateMonthlyStatement();      
            }else if ("0".equals(c)) {
                go = false;
            }
        }
    }

    private void listCustomerAccounts() {
        Customer cu = (Customer) loggedInUser;
        String cid = cu.getCustomerId();
        System.out.println("Your accounts (APPROVED only for banking):");
        int i;
        for (i = 0; i < accountCount; i++) {
            Account a = accounts[i];
            if (a == null) {
                continue;
            }
            if (!a.getCustomerId().equals(cid)) {
                continue;
            }
            System.out.println(a.toString());
        }
    }

    private Account selectAccountFromList(String customerId, Class<?> typeFilter) {
        Account[] temp = new Account[MAX_ACCOUNTS];
        int count = 0;

        System.out.println("\n--- Select an Account ---");
        for (int i = 0; i < accountCount; i++) {
            Account a = accounts[i];
            if (a != null && a.getCustomerId().equals(customerId) && "APPROVED".equals(a.getStatus())) {
                if (typeFilter == null || typeFilter.isInstance(a)) {
                    temp[count++] = a;
                    System.out.println((count) + ". [" + a.getAccountId() + "] Type: " + a.getAccountType() + " | Balance: RM " + String.format("%.2f", a.getBalance()));
                }
            }
        }

        if (count == 0) {
            System.out.println("No eligible accounts found.");
            return null;
        }

        System.out.print("Enter choice (1-" + count + ") or 0 to cancel: ");
        try {
            int choice = Integer.parseInt(scanner.nextLine().trim());
            if (choice > 0 && choice <= count) {
                return temp[choice - 1];
            }
        } catch (Exception e) {
            System.out.println("Invalid input.");
        }
        return null;
    }

    // Account must belong to customer and be APPROVED
    private Account findApprovedAccountForCustomer(String accountId, String customerId) {
        int idx = indexOfAccountId(accountId);
        if (idx < 0) {
            return null;
        }
        Account a = accounts[idx];
        if (!a.getCustomerId().equals(customerId)) {
            return null;
        }
        if (!"APPROVED".equals(a.getStatus())) {
            return null;
        }
        return a;
    }

    private void doDeposit() {
            Customer cu = (Customer) loggedInUser;
            Account a = selectAccountFromList(cu.getCustomerId(), null); // Let user pick
            if (a == null) return;

            System.out.print("Enter amount to deposit: RM ");
            try {
                double amt = Double.parseDouble(scanner.nextLine().trim());
                a.deposit(amt);
                logTx("DEPOSIT", amt, a.getAccountId());
                saveAll();
                System.out.println("Success! New balance for " + a.getAccountId() + ": RM " + a.getBalance());
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
        
    private void doWithdraw() {
            Customer cu = (Customer) loggedInUser;
            Account a = selectAccountFromList(cu.getCustomerId(), null); // Let user pick
            if (a == null) return;

            System.out.print("Enter amount to withdraw: RM ");
            try {
                double amt = Double.parseDouble(scanner.nextLine().trim());
                a.withdraw(amt);
                logTx("WITHDRAW", amt, a.getAccountId());
                saveAll();
                System.out.println("Success! New balance for " + a.getAccountId() + ": RM " + a.getBalance());
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }

    private void doTransfer() {
        Customer cu = (Customer) loggedInUser;
        System.out.print("From your account id: ");
        String fromId = scanner.nextLine().trim();
        System.out.print("To target account id: ");
        String toId = scanner.nextLine().trim();
        
        // 1. Verify 'From' account (must be owned by the logged-in customer and APPROVED)
        Account from = findApprovedAccountForCustomer(fromId, cu.getCustomerId());
        if (from == null) {
            System.out.println("Error: 'From' account must be yours and APPROVED.");
            return;
        }
        
        // 2. Prevent transferring to the same account
        if (fromId.equals(toId)) {
            System.out.println("Error: Cannot transfer to the exact same account.");
            return;
        }

        // 3. Verify 'To' account globally (does not have to be owned by this customer)
        Account to = null;
        int toIdx = indexOfAccountId(toId);
        if (toIdx >= 0) {
            to = accounts[toIdx];
            if (!"APPROVED".equals(to.getStatus())) {
                to = null; // Cannot transfer to PENDING or FROZEN accounts
            }
        }
        
        if (to == null) {
            System.out.println("Error: Target account does not exist or is not APPROVED.");
            return;
        }

        // 4. Process transfer amount
        System.out.print("Enter Transfer Amount: RM ");
        try {
            double amt = Double.parseDouble(scanner.nextLine().trim());
            
            // Execute withdrawal first
            from.withdraw(amt); 
            
            // Try to deposit to the target account
            try {
                to.deposit(amt);
            } catch (Exception e2) {
                // Transaction Rollback: Refund the sender if deposit fails
                from.deposit(amt);
                throw new Exception("Deposit failed, money refunded. " + e2.getMessage());
            }
            
            // 5. Log transaction for both parties
            logTx("TRANSFER_OUT", amt, fromId);
            logTx("TRANSFER_IN", amt, toId);
            saveAll();
            
            System.out.println("Success! Transferred RM " + amt + " to Account " + toId);
            
        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid amount format.");
        } catch (Exception e) {
            System.out.println("Transfer Error: " + e.getMessage());
        }
    }

    // Extra pending account (new card)
    private void applyExtraCard() {
     if (accountCount >= MAX_ACCOUNTS) {
        System.out.println("System full.");
        return;
     }
    
        Customer cu = (Customer) loggedInUser;
            if (!"APPROVED".equals(cu.getStatus())) {
            System.out.println("Not approved.");
            return;
        } 


        System.out.println("Select Account Type:");
        System.out.println("1. Savings Account");
        System.out.println("2. Current Account");
        System.out.print("Enter choice (1 or 2): ");
        String choice = scanner.nextLine().trim();

        String newId = nextAccountId();
        Account acc = null;

        if ("1".equals(choice)) {

            acc = new SavingsAccount(newId, 0.0, cu.getCustomerId(), "PENDING");
        } else if ("2".equals(choice)) {

            acc = new CurrentAccount(newId, 0.0, cu.getCustomerId(), "PENDING", 500.0);
        } else {

            System.out.println("Invalid choice! Application cancelled.");
         return; 
        }
 
        accounts[accountCount] = acc;
        accountCount = accountCount + 1;
        saveAll();
        System.out.println("Request submitted. New pending account id: " + newId);
}

    // Admin submenu
    private void adminMenu() {
        boolean go = true;
        while (go) {
            System.out.println();
            System.out.println("------------------ Admin Menu ------------------");
            System.out.println("1 List pending customers");
            System.out.println("2 Approve customer by user id");
            System.out.println("3 Reject customer by user id (Delete)");
            System.out.println("4 Freeze account by id");
            System.out.println("5 Unfreeze account by id");
            System.out.println("6 Approve Loan Application");
            System.out.println("7 Reject Loan Application (Delete)");  
            System.out.println("8 Set Global Interest Rate");     
            System.out.println("9 Distribute Savings Interest");  
            System.out.println("0 Logout");
            System.out.print("Choose: ");
            String c = scanner.nextLine().trim();

            if ("1".equals(c)) {
                listPendingCustomers();
            } else if ("2".equals(c)) {
                approveCustomer();
            } else if ("3".equals(c)) {
                rejectCustomer();
            } else if ("4".equals(c)) {
                freezeAccount(true);
            } else if ("5".equals(c)) {
                freezeAccount(false);
            } else if ("6".equals(c)) {
                approveLoan();
            } else if ("7".equals(c)) {
                rejectLoan();   
            } else if ("8".equals(c)) {
                setGlobalInterestRate(); 
            } else if ("9".equals(c)) {
                distributeSavingsInterest(); 
            } else if ("0".equals(c)) {
                go = false;
            } else {
                System.out.println("Invalid.");
            }
        }
    }

private void listPendingCustomers() {
        System.out.println("\n===== ADMIN PENDING REQUESTS =====");
        
        System.out.println("[ 1. Users Waiting for Initial Approval ]");
        boolean userFound = false;
        for (int i = 0; i < userCount; i++) {
            User u = users[i];
            if (u != null && u instanceof Customer && "PENDING".equals(u.getStatus())) {
                System.out.println("User ID: " + u.getUserId() + " | Name: " + u.getFullName() + " | CID: " + u.getCustomerId());
                userFound = true;
            }
        }
        if (!userFound) System.out.println("No new user registrations.");

        System.out.println("\n[ 2. Accounts/Cards Waiting for Approval ]");
        boolean accFound = false;
        for (int i = 0; i < accountCount; i++) {
            Account a = accounts[i];
            if (a != null && "PENDING".equals(a.getStatus())) {
                System.out.println("Account ID: " + a.getAccountId() + 
                                   " | Type: " + a.getAccountType() + 
                                   " | Owner CID: " + a.getCustomerId());
                accFound = true;
            }
        }
        if (!accFound) System.out.println("No pending account applications.");

        System.out.println("\n[ 3. Loans Waiting for Approval ]");
        boolean loanFound = false;
        for (int i = 0; i < loanCount; i++) {
            Loan L = loans[i];
            if (L != null && "PENDING".equals(L.getStatus())) {
                System.out.println("Target Account ID: " + L.getTargetAccountId() + 
                                   " | Principal: RM " + L.getPrincipal() + 
                                   " | Owner CID: " + L.getCustomerId());
                loanFound = true;
            }
        }
        if (!loanFound) System.out.println("No pending loan applications.");
        
        System.out.println("====================================");
    }

    private void approveCustomer() {
    System.out.print("Enter User ID to approve: ");
    String uid = scanner.nextLine().trim();
    int uIdx = indexOfUserId(uid);

    if (uIdx >= 0) {
        User u = users[uIdx];
        u.setStatus("APPROVED");
        
        // Approve all pending accounts belonging to this customer
        String cid = u.getCustomerId();
        for (int i = 0; i < accountCount; i++) {
            if (accounts[i].getCustomerId().equals(cid) && "PENDING".equals(accounts[i].getStatus())) {
                accounts[i].setStatus("APPROVED");
            }
        }
        
        saveAll();
        System.out.println("User and associated accounts have been APPROVED.");
    } else {
        System.out.println("User not found.");
    }
}

    private void freezeAccount(boolean freeze) {
        System.out.print("Account id: ");
        String aid = scanner.nextLine().trim();
        int idx = indexOfAccountId(aid);
        if (idx < 0) {
            System.out.println("Not found.");
            return;
        }
        if (freeze) {
            accounts[idx].setStatus("FROZEN");
        } else {
            accounts[idx].setStatus("APPROVED");
        }
        saveAll();
        System.out.println("Updated.");
    }

    private void doForex() {
    System.out.println("\n--- Simple Forex Calculator (Base: MYR) ---");
    System.out.println("Available: USD, EUR, RMB, SGD, JPY");
    
    System.out.print("Enter amount in MYR: ");
    String inputAmt = scanner.nextLine().trim();
    double amount;
    
    try {
        amount = Double.parseDouble(inputAmt);
    } catch (Exception e) {
        System.out.println("Invalid number.");
        return;
    }

    System.out.print("Enter target currency: ");
    String currency = scanner.nextLine().trim().toUpperCase();

    double rate = 0.0;
    

    if (currency.equals("USD")) {
        rate = 0.25;
    } else if (currency.equals("EUR")) {
        rate = 0.22;
    } else if (currency.equals("RMB")) {
        rate = 1.72;
    } else if (currency.equals("SGD")) {
        rate = 0.32;
    } else if (currency.equals("JPY")) {
        rate = 40.26;
    } else {
        System.out.println("Currency not supported.");
        return;
    }

    double result = amount * rate;
    System.out.println("Conversion Result: " + amount + " MYR = " + result + " " + currency);
}


    private Loan selectLoanFromList(String customerId) {
        Loan[] temp = new Loan[100];
        int count = 0;

        System.out.println("\n--- Your Active Loans ---");
        for (int i = 0; i < loanCount; i++) {
            Loan L = loans[i];
            if (L != null && L.getCustomerId().equals(customerId) && "APPROVED".equals(L.getStatus())) {
                temp[count++] = L;
                System.out.println((count) + ". [Target Account: " + L.getTargetAccountId() + 
                                   "] Remaining Debt: RM " + String.format("%.2f", L.getRemainingDebt()));
            }
        }

        if (count == 0) {
            System.out.println("You have no active loans to repay.");
            return null;
        }

        System.out.print("Select loan to repay (1-" + count + ") or 0 to cancel: ");
        try {
            int choice = Integer.parseInt(scanner.nextLine().trim());
            if (choice > 0 && choice <= count) {
                return temp[choice - 1];
            }
        } catch (Exception e) {
            System.out.println("Invalid selection.");
        }
        return null;
    }

private void applyLoan() {
        Customer cu = (Customer) loggedInUser;
        
        System.out.println("Select the account to receive the loan funds:");
        Account targetAcc = selectAccountFromList(cu.getCustomerId(), null);
        
        if (targetAcc == null) {
            System.out.println("Loan application cancelled.");
            return;
        }
        String aid = targetAcc.getAccountId();

        System.out.println("Select Loan Amount:");
        System.out.println("1. 1000 | 2. 2000 | 3. 3000 | 4. 4000 | 5. 5000");
        System.out.print("Choice (1-5): ");
        
        double amount = 0;
        try {
            String choice = scanner.nextLine().trim();
            if (choice.equals("1")) amount = 1000;
            else if (choice.equals("2")) amount = 2000;
            else if (choice.equals("3")) amount = 3000;
            else if (choice.equals("4")) amount = 4000;
            else if (choice.equals("5")) amount = 5000;
            else {
                System.out.println("Invalid selection.");
                return;
            }
        } catch (Exception e) {
            System.out.println("Input error. Please enter a valid number.");
            return;
        }

        int months = (amount < 3000) ? 6 : 12;
        System.out.println("System assigned duration: " + months + " months.");
        loans[loanCount++] = new Loan(cu.getCustomerId(), aid, amount, months);
        
        saveAll();
        System.out.println("Loan application for " + aid + " submitted (PENDING).");
    }

 private void approveLoan() {
        System.out.println("\n--- Select a Loan to Approve ---");
        Loan[] tempPending = new Loan[100];
        int count = 0;
        
  
        for (int i = 0; i < loanCount; i++) {
            Loan L = loans[i];
            if (L != null && "PENDING".equals(L.getStatus())) {
                tempPending[count++] = L;
                System.out.println((count) + ". [Target Account: " + L.getTargetAccountId() + 
                                   "] Principal: RM " + L.getPrincipal());
            }
        }
        
        if (count == 0) {
            System.out.println("No pending loan applications found.");
            return;
        }
        
        System.out.print("Enter choice (1-" + count + ") or 0 to cancel: ");
        try {
            int choice = Integer.parseInt(scanner.nextLine().trim());
            if (choice == 0) {
                System.out.println("Operation cancelled.");
                return;
            }
            if (choice > 0 && choice <= count) {
                Loan targetLoan = tempPending[choice - 1];
                String targetAccId = targetLoan.getTargetAccountId();
                
           
                Account targetAcc = null;
                for (int i = 0; i < accountCount; i++) {
                    if (accounts[i] != null && accounts[i].getAccountId().equals(targetAccId)) {
                        targetAcc = accounts[i];
                        break;
                    }
                }
                
                if (targetAcc != null) {
                    targetAcc.deposit(targetLoan.getPrincipal()); 
                    targetLoan.setStatus("APPROVED");             
                    
                    logTx("LOAN_DISBURSED", targetLoan.getPrincipal(), targetAccId);
                    saveAll();
                    
                    System.out.println("Success! Loan approved. RM " + String.format("%.2f", targetLoan.getPrincipal()) + " disbursed to " + targetAccId);
                } else {
                    System.out.println("Error: The target account (" + targetAccId + ") does not exist or was deleted.");
                }
            } else {
                System.out.println("Invalid selection.");
            }
        } catch (Exception e) {
            System.out.println("Input error. Please enter a valid number.");
        }
    }

    // Customer repays a portion of the loan manually
private void repayLoan() {
        Customer cu = (Customer) loggedInUser;
        
        Loan myLoan = selectLoanFromList(cu.getCustomerId());
        if (myLoan == null) return;

        System.out.println("Current Loan Debt: RM " + String.format("%.2f", myLoan.getRemainingDebt()));

        try {
            System.out.print("Enter amount to repay: ");
            double inputAmt = Double.parseDouble(scanner.nextLine().trim());
            
            if (inputAmt <= 0) {
                System.out.println("Invalid amount. Must be greater than 0.");
                return;
            }

            double actualPayAmt = inputAmt;
            if (actualPayAmt > myLoan.getRemainingDebt()) {
                actualPayAmt = myLoan.getRemainingDebt();
                System.out.println("Note: Amount capped to actual debt (RM " + String.format("%.2f", actualPayAmt) + ").");
            }


            System.out.println("Select Account to pay from:");
            Account acc = selectAccountFromList(cu.getCustomerId(), null);
            
            if (acc == null) {
                System.out.println("Repayment cancelled.");
                return;
            }

            String aid = acc.getAccountId();

            if (acc.getBalance() < actualPayAmt) {
                System.out.println("Error: Insufficient funds in account " + aid);
                return;
            }


            acc.withdraw(actualPayAmt);
            myLoan.setRemainingDebt(myLoan.getRemainingDebt() - actualPayAmt); 
            
            logTx("LOAN_REPAY", actualPayAmt, aid);
            
            if (myLoan.getRemainingDebt() <= 0.01) {
                myLoan.setStatus("PAID");
                System.out.println("Success! Loan fully settled.");
            } else {
                System.out.println("Payment successful. Remaining debt: RM " + String.format("%.2f", myLoan.getRemainingDebt()));
            }
            
            saveAll(); 

        } catch (NumberFormatException e) {
            System.out.println("Input error: Please enter a numeric value.");
        } catch (Exception e) {
            System.out.println("Error during repayment: " + e.getMessage());
        }
    }

private void openFixedDeposit() {
        Customer cu = (Customer) loggedInUser;
        
        Account acc = selectAccountFromList(cu.getCustomerId(), null);
        if (acc == null) {
            System.out.println("Fixed Deposit application cancelled.");
            return;
        }

        System.out.print("Enter Principal Amount (Minimum RM 500): ");
        double principal = 0;
        try {
            principal = Double.parseDouble(scanner.nextLine().trim());
            if (principal < 500) {
                System.out.println("Error: Minimum amount is RM 500. Application cancelled.");
                return;
            }

            if (acc.getBalance() < principal) {
                System.out.println("Error: Insufficient balance in account " + acc.getAccountId());
                return;
            }
        } catch (Exception e) {
            System.out.println("Error: Invalid input. Please enter numeric values only.");
            return;
        }

        System.out.println("\nSelect Duration & Interest Rate:");
        System.out.println("1. 3 Months  (3.0% p.a.)");
        System.out.println("2. 6 Months  (3.5% p.a.)");
        System.out.println("3. 12 Months (4.0% p.a.)");
        System.out.print("Choice (1-3): ");
        
        int months = 0;
        String choice = scanner.nextLine().trim();
        if ("1".equals(choice)) months = 3;
        else if ("2".equals(choice)) months = 6;
        else if ("3".equals(choice)) months = 12;
        else {
            System.out.println("Error: Invalid selection. Application cancelled.");
            return;
        }

        try {
            acc.withdraw(principal); 
              
            String fdId = "FD-" + (fdCount + 100); 
            fds[fdCount++] = new FixedDeposit(fdId, cu.getCustomerId(), acc.getAccountId(), principal, months, "ACTIVE");
        
            logTx("FD_PLACEMENT", principal, acc.getAccountId());
            saveAll();
            System.out.println("Success! Fixed Deposit " + fdId + " created using account " + acc.getAccountId());
        } catch (Exception e) {
            System.out.println("Transaction failed: " + e.getMessage());
        }
    }

    private void withdrawFixedDeposit() {
        Customer cu = (Customer) loggedInUser;
        
        // List active FDs for this customer
        System.out.println("\n--- Your Active Fixed Deposits ---");
        boolean hasActive = false;
        for (int i = 0; i < fdCount; i++) {
            FixedDeposit fd = fds[i];
            if (fd != null && fd.getCustomerId().equals(cu.getCustomerId()) && "ACTIVE".equals(fd.getStatus())) {
                System.out.println("ID: " + fd.getFdId() + " | Principal: RM " + fd.getPrincipal() + " | Term: " + fd.getMonths() + " months");
                hasActive = true;
            }
        }

        if (!hasActive) {
            System.out.println("You have no active Fixed Deposits.");
            return;
        }

        System.out.print("Enter FD ID to withdraw (Simulate Maturity): ");
        String fdId = scanner.nextLine().trim();

        for (int i = 0; i < fdCount; i++) {
            FixedDeposit fd = fds[i];
            if (fd != null && fd.getFdId().equals(fdId) && fd.getCustomerId().equals(cu.getCustomerId()) && "ACTIVE".equals(fd.getStatus())) {
                
                // Calculate Interest based on duration
                double rate = 0;
                if (fd.getMonths() == 3) rate = 0.03;
                else if (fd.getMonths() == 6) rate = 0.035;
                else if (fd.getMonths() == 12) rate = 0.04;

                double interest = fd.getPrincipal() * rate * (fd.getMonths() / 12.0);
                double totalReturn = fd.getPrincipal() + interest;

                // Return money to original source account
                int accIdx = indexOfAccountId(fd.getSourceAccountId());
                if (accIdx >= 0) {
                    try {
                        accounts[accIdx].deposit(totalReturn);
                        fd.setStatus("CLOSED");
                        logTx("FD_WITHDRAWAL", totalReturn, fd.getSourceAccountId());
                        saveAll();
                        System.out.println("Success! Principal + Interest (RM " + totalReturn + ") returned to " + fd.getSourceAccountId());
                    } catch (Exception e) {
                        System.out.println("System error returning funds.");
                    }
                } else {
                    System.out.println("Error: Original source account no longer exists.");
                }
                return;
            }
        }
        System.out.println("Error: Invalid FD ID.");
    }

private void setGlobalInterestRate() {
        System.out.println("\n--- Set Global Savings Interest Rate ---");
       
        System.out.println("Current Interest Rate: " + String.format("%.2f", globalSavingsRate * 100) + "%");
        
        System.out.print("Enter new annual rate in percentage (e.g., enter 3.5 for 3.5%): ");
        try {
            double inputRate = Double.parseDouble(scanner.nextLine().trim());
            
            if (inputRate < 0) {
                System.out.println("Error: Interest rate cannot be negative.");
                return;
            }
            
        
            globalSavingsRate = inputRate / 100.0;
            

            System.out.println("Success! Savings rate updated to " + String.format("%.2f", globalSavingsRate * 100) + "%");
        } catch (Exception e) {
            System.out.println("Invalid input. Please enter a valid number.");
        }
    }

        // Admin Function: Distribute interest to ALL approved Savings Accounts
private void distributeSavingsInterest() {
        System.out.println("\nStarting interest distribution (Rate: " + String.format("%.2f", globalSavingsRate * 100) + "%)...");
        
        int accountsPaid = 0;
        double totalDisbursed = 0;
        
        for (int i = 0; i < accountCount; i++) {
            Account acc = accounts[i];
            if (acc != null && acc instanceof SavingsAccount && "APPROVED".equals(acc.getStatus())) {
                
                double interest = acc.getBalance() * globalSavingsRate; 
                
                if (interest > 0) {
                    try {
                        acc.deposit(interest); 
                        accountsPaid++;
                        totalDisbursed += interest;
                        
                        logTx("INTEREST_PAID", interest, acc.getAccountId());
                    } catch (Exception e) {

                        System.out.println("Error paying interest to " + acc.getAccountId() + ": " + e.getMessage());
                    }
                }
            }
        }
        
        saveAll();
        System.out.println("Distribution Complete!");
        System.out.println("Total Accounts Paid: " + accountsPaid);
        System.out.println("Total Interest Disbursed: RM " + String.format("%.2f", totalDisbursed));
    }

    // Admin rejects a registration request (Permanently deletes the pending info)
    private void rejectCustomer() {
        System.out.print("Enter User ID to reject: ");
        String uid = scanner.nextLine().trim();
        int uIdx = indexOfUserId(uid);

        if (uIdx >= 0) {
            User u = users[uIdx];
            
            // Check if user is already approved (Decision 3: Intercept)
            if ("APPROVED".equals(u.getStatus())) {
                System.out.println("Error: This user is already APPROVED and cannot be rejected.");
                return;
            }

            // Decision 1: Delete user and their pending account from arrays
            String cid = u.getCustomerId();
            for (int i = 0; i < accountCount; i++) {
                if (accounts[i] != null && accounts[i].getCustomerId().equals(cid)) {
                    accounts[i] = null; 
                }
            }
            users[uIdx] = null; 
            
            saveAll();
            System.out.println("Success! Registration request for " + uid + " has been rejected and deleted.");
        } else {
            System.out.println("User not found.");
        }
    }

    // Admin rejects a loan request (Permanently deletes the pending loan entry)
    private void rejectLoan() {
        System.out.println("\n--- Select a Loan to Reject ---");
        Loan[] tempPending = new Loan[100];
        int count = 0;
        
        for (int i = 0; i < loanCount; i++) {
            Loan L = loans[i];
            if (L != null && "PENDING".equals(L.getStatus())) {
                tempPending[count++] = L;
              
                System.out.println((count) + ". [Target Account: " + L.getTargetAccountId() + 
                                   "] Principal: RM " + L.getPrincipal());
            }
        }
        
        if (count == 0) {
            System.out.println("No pending loan applications found.");
            return;
        }

        System.out.print("Enter choice (1-" + count + ") or 0 to cancel: ");
        try {
            int choice = Integer.parseInt(scanner.nextLine().trim());
            if (choice == 0) {
                System.out.println("Operation cancelled.");
                return;
            }
            if (choice > 0 && choice <= count) {

                Loan targetLoan = tempPending[choice - 1];
                
                targetLoan.setStatus("REJECTED");
                saveAll();
                
                System.out.println("Success! Loan application for " + targetLoan.getTargetAccountId() + " has been REJECTED.");
            } else {
                System.out.println("Invalid selection.");
            }
        } catch (Exception e) {
            System.out.println("Input error. Please enter a valid number.");
        }
    }

    private void viewMyTransactions() {
            Customer cu = (Customer) loggedInUser;
            System.out.println("\n--- YOUR TRANSACTION HISTORY ---");
            System.out.printf("%-18s | %-20s | %-15s | %-10s | %-10s\n", 
                            "Ref ID", "Timestamp", "Type", "Amount", "Account");
            System.out.println("---------------------------------------------------------------------------------------");

            try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(FileStore.TRANSACTIONS_FILE))) {
                String line;
                boolean found = false;
                while ((line = br.readLine()) != null) {
                    String[] p = line.split(",");
                    if (p.length >= 5) {
                        String aid = p[4].trim();
                        // Verify if this account belongs to the current user
                        if (findApprovedAccountForCustomer(aid, cu.getCustomerId()) != null) {
                            System.out.printf("%-18s | %-20s | %-15s | RM %-7.2f | %-10s\n", 
                                            p[0], p[1], p[2], Double.parseDouble(p[3]), p[4]);
                            found = true;
                        }
                    }
                }
                if (!found) System.out.println("No transactions found.");
            } catch (Exception e) {
                System.out.println("No transaction history file found yet.");
            }
        }

 private void generateMonthlyStatement() {
        Customer cu = (Customer) loggedInUser;
        System.out.print("Enter month to export (e.g., 2026-04): ");
        String month = scanner.nextLine().trim();


        if (!month.matches("^\\d{4}-(0[1-9]|1[0-2])$")) {
            System.out.println("Error: Invalid date format. Please exactly use YYYY-MM (e.g., 2026-04).");
            return;
        }

        String fileName = cu.getUserId() + "_Statement_" + month + ".txt";

        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(FileStore.TRANSACTIONS_FILE));
             java.io.BufferedWriter bw = new java.io.BufferedWriter(new java.io.FileWriter(fileName))) {
            
            // "B" Style: Exquisite Layout with Headers
            bw.write("============================================================\n");
            bw.write("                DIGITAL BANK MONTHLY STATEMENT               \n");
            bw.write("============================================================\n");
            bw.write("Customer: " + cu.getFullName() + "\n");
            bw.write("User ID:  " + cu.getUserId() + "\n");
            bw.write("Period:   " + month + "\n");
            bw.write("Generated Date: " + FileStore.nowTimestamp() + "\n");
            bw.write("------------------------------------------------------------\n");
            bw.write(String.format("%-18s | %-15s | %-10s | %-10s\n", "Ref ID", "Type", "Amount", "Account"));
            bw.write("------------------------------------------------------------\n");

            String line;
            boolean found = false;
            while ((line = br.readLine()) != null) {
                // Now 'month' is guaranteed to be a valid YYYY-MM string
                if (line.contains(month)) {
                    String[] p = line.split(",");
                    if (p.length >= 5 && findApprovedAccountForCustomer(p[4].trim(), cu.getCustomerId()) != null) {
                        bw.write(String.format("%-18s | %-15s | RM %-7.2f | %-10s\n", 
                                 p[0], p[2], Double.parseDouble(p[3]), p[4]));
                        found = true;
                    }
                }
            }
            
            bw.write("------------------------------------------------------------\n");
            bw.write("            Thank you for banking with Digital Bank         \n");
            bw.write("============================================================\n");

            if (found) {
                System.out.println("Success! Your statement has been saved as: " + fileName);
            } else {
                System.out.println("No records found for the month: " + month);
                // (Optional) Delete the empty file if no records found
                new java.io.File(fileName).delete();
            }
        } catch (Exception e) {
            System.out.println("Error reading transactions: " + e.getMessage());
        }
    }


}
