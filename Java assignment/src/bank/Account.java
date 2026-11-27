package bank;

/**
 * Abstract bank account (polymorphic withdraw).
 */
public abstract class Account{

    private String accountId;
    private String accountType;
    private double balance;
    private String customerId;
    // "PENDING", "APPROVED", or "FROZEN"
    private String status;

    public Account(){
        this("", "", 0.0, "", ""); 
    }

    public Account(String accountId, String accountType, double balance, String customerId, String status){
        this.accountId = accountId;
        this.accountType = accountType;
        this.balance = balance;
        this.customerId = customerId;
        this.status = status;
    }

    public String getAccountId(){
        return accountId;
    }

    public String getAccountType(){
        return accountType;
    }

    public double getBalance(){
        return balance;
    }

    public String getCustomerId(){
        return customerId;
    }

    public String getStatus(){
        return status;
    }

    public void setAccountId(String accountId){
        this.accountId = accountId;
    }

    public void setAccountType(String accountType){
        this.accountType = accountType;
    }

    public void setBalance(double balance){
        this.balance = balance;
    }

    public void setCustomerId(String customerId){
        this.customerId = customerId;
    }

    public void setStatus(String status){
        this.status = status;
    }

    // Credit is always allowed (including FROZEN accounts)
    public void deposit(double amount) throws Exception{
        if (amount <= 0){
            throw new Exception("Invalid amount");
        }
        this.balance = this.balance + amount;
    }

    // Subclasses implement rules (overdraft, frozen, etc.)
    public abstract void withdraw(double amount) throws Exception;

    public boolean equals(Object obj){
        if (this == obj)
             return true;
        if (obj == null || getClass() != obj.getClass())
             return false;
        Account otherAccount = (Account) obj;
        return this.accountId.equals(otherAccount.accountId);
    }
}
