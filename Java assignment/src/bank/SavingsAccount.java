package bank;

/**
 * Savings account: balance cannot go below zero.
 */
public class SavingsAccount extends Account{

    public SavingsAccount(){
        super();
        setAccountType("Savings");
    }

    public SavingsAccount(String accountId, double balance, String customerId, String status){
        super(accountId, "Savings", balance, customerId, status);
    }

    public void withdraw(double amount) throws Exception{
        if ("FROZEN".equals(getStatus())){
            throw new Exception("Account Frozen");
        }
        if (amount <= 0){
            throw new Exception("Invalid amount");
        }
        if (getBalance() - amount < 0){
            throw new Exception("Insufficient balance");
        }
        setBalance(getBalance() - amount);
    }

    public String toString(){
        return "SavingsAccount[id = " + getAccountId() + ", balance = " + getBalance()
                + ", customerId = " + getCustomerId() + ", status = " + getStatus() + "]";
    }
}
