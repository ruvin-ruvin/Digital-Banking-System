package bank;

public class FixedDeposit {
    private String fdId;
    private String customerId;
    private String sourceAccountId; // The account to return money to
    private double principal;
    private int months;
    private String status; // "ACTIVE" or "CLOSED"

    public FixedDeposit(String fdId, String customerId, String sourceAccountId, double principal, int months, String status) {
        this.fdId = fdId;
        this.customerId = customerId;
        this.sourceAccountId = sourceAccountId;
        this.principal = principal;
        this.months = months;
        this.status = status;
    }

    public String getFdId() { return fdId; }
    public String getCustomerId() { return customerId; }
    public String getSourceAccountId() { return sourceAccountId; }
    public double getPrincipal() { return principal; }
    public int getMonths() { return months; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String toString() {
        return fdId + "," + customerId + "," + sourceAccountId + "," + principal + "," + months + "," + status;
    }
}