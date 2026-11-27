package bank;

import java.util.Scanner;

/**
 * Program entry: console digital bank.
 */
public class Main {

    public static void main(String[] args) {
        // Console input
        Scanner scanner = new Scanner(System.in);
        try {
            BankApp app = new BankApp(scanner);
            app.run();
        } finally {
            scanner.close();
        }
    }
}
