package bank;

public class Loan {
    private String customerId;
    private String targetAccountId; // Added to link a specific card
    private double principal;
    private double remainingDebt;
    private String status;

    public Loan(String customerId, String targetAccountId, double principal, int months) {
        this.customerId = customerId;
        this.targetAccountId = targetAccountId;
        this.principal = principal;
        // Calculation: Principal * (1 + 5% * months/12)
        this.remainingDebt = principal * (1 + 0.05 * (months / 12.0));
        this.status = "PENDING";
    }

    public Loan(String customerId, String targetAccountId, double principal, double remainingDebt, String status) {
        this.customerId = customerId;
        this.targetAccountId = targetAccountId;
        this.principal = principal;
        this.remainingDebt = remainingDebt;
        this.status = status;
    }

    public String getCustomerId() { return customerId; }
    public String getTargetAccountId() { return targetAccountId; }
    public double getPrincipal() { return principal; }
    public double getRemainingDebt() { return remainingDebt; }
    public void setRemainingDebt(double rem) { this.remainingDebt = rem; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String toString() {
        return customerId + "," + targetAccountId + "," + principal + "," + remainingDebt + "," + status;
    }
}