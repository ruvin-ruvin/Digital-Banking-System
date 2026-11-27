package bank;

/**
 * Admin user role.
 */
public class Admin extends User {

    public Admin() {
        super();
        setRole("ADMIN");
    }

    public Admin(String userId, String password, String fullName, String phoneNumber, String icNumber, String email, String customerId, String status) {
        super(userId, password, fullName, phoneNumber, icNumber, email, "ADMIN", customerId, status);
    }

    @Override
    public String toString() {
        return "Admin[userId=" + getUserId() + ", fullName=" + getFullName()
                + ", customerId=" + getCustomerId() + ", status=" + getStatus() + "]";
    }
}