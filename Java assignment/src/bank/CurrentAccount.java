package bank;

/**
 * Current account: can use overdraft limit.
 */
public class CurrentAccount extends Account{

    private double overdraftLimit;

    public CurrentAccount(){
        super();
        setAccountType("Current");
        this.overdraftLimit = 500.0;
    }

    public CurrentAccount(String accountId, double balance, String customerId, String status, double overdraftLimit){
        super(accountId, "Current", balance, customerId, status);
        this.overdraftLimit = overdraftLimit;
    }

    public double getOverdraftLimit(){
        return overdraftLimit;
    }

    public void setOverdraftLimit(double overdraftLimit){
        this.overdraftLimit = overdraftLimit;
    }

    public void withdraw(double amount) throws Exception{
        if ("FROZEN".equals(getStatus())){
            throw new Exception("Account Frozen");
        }
        if (amount <= 0){
            throw new Exception("Invalid amount");
        }

        double minBalance = 0.0 - overdraftLimit;

        if (getBalance() - amount < minBalance){
            throw new Exception("Insufficient balance");
        }
        setBalance(getBalance() - amount);
    }

    public String toString(){
        return "CurrentAccount[id = " + getAccountId() + ", balance = " + getBalance()
                + ", customerId = " + getCustomerId() + ", status = " + getStatus()
                + ", overdraft = " + overdraftLimit + "]";
    }
}
