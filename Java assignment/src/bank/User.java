package bank;

/**
 * Abstract bank user (customer or admin).
 */
public abstract class User {

    private String userId;
    private String password;
    private String fullName;
    private String phoneNumber; 
    private String icNumber;    
    private String email;       
    private String role;
    private String customerId;
    private String status;

    public User() {
        this.userId = "";
        this.password = "";
        this.fullName = "";
        this.phoneNumber = "";
        this.icNumber = "";
        this.email = "";
        this.role = "";
        this.customerId = "";
        this.status = "";
    }

    public User(String userId, String password, String fullName, String phoneNumber, String icNumber, String email, String role, String customerId, String status) {
        this.userId = userId;
        this.password = password;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.icNumber = icNumber;
        this.email = email;
        this.role = role;
        this.customerId = customerId;
        this.status = status;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getIcNumber() { return icNumber; }
    public void setIcNumber(String icNumber) { this.icNumber = icNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User otherUser = (User) obj;
        return this.userId.equals(otherUser.userId);
    }
}