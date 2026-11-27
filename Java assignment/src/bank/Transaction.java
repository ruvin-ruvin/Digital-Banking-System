package bank;

/**
 * Enhanced Transaction model with Reference ID.
 */
public class Transaction{

    private String referenceId; // Unique transaction number
    private String timestamp;
    private String type;
    private double amount;
    private String targetAccountId;

    public Transaction(){
        this("", "", "", 0.0, "");
    }

    public Transaction(String referenceId, String timestamp, String type, double amount, String targetAccountId){
        this.referenceId = referenceId;
        this.timestamp = timestamp;
        this.type = type;
        this.amount = amount;
        this.targetAccountId = targetAccountId;
    }

    public String getReferenceId(){
        return referenceId;
    }

    public String getTimestamp(){
        return timestamp;
    }

    public String getType(){
        return type;
    }

    public double getAmount(){
        return amount; 
    }

    public String getTargetAccountId(){
        return targetAccountId; 
    }

    public void setReferenceId(String referenceId){
        this.referenceId = referenceId;
    }
 
    public void setTimestamp(String timeStamp){
        this.timestamp = timeStamp; 
    }
  
    public void setType(String type){
        this.type = type; 
    }

    public void setAmount(double amount){
        this.amount = amount; 
    }

    public void setTargetAccountId(String targetAccountId){
        this.targetAccountId = targetAccountId;
    }

    public String toString(){
        // Formatted for CSV storage in transactions.txt
        return referenceId + "," + timestamp + "," + type + "," + amount + "," + targetAccountId;
    }
}