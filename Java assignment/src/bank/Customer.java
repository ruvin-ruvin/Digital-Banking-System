package bank;

/**
 * Customer user role.
 */
public class Customer extends User {

    public Customer() {
        super();
        setRole("CUSTOMER");
    }

    public Customer(String userId, String password, String fullName, String phoneNumber, String icNumber, String email, String customerId, String status) {
        super(userId, password, fullName, phoneNumber, icNumber, email, "CUSTOMER", customerId, status);
    }

    public String toString() {
        return "Customer[userId=" + getUserId() + ", fullName=" + getFullName()
                + ", phone=" + getPhoneNumber() + ", customerId=" + getCustomerId() + "]";
    }
}