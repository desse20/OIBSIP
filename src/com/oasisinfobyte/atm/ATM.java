package com.oasisinfobyte.atm;

import java.util.Scanner;

/**
 * ATM — Console interface layer.
 * Step 1: Authentication with MAX_ATTEMPTS limit.
 */
public class ATM {

    private static final int MAX_ATTEMPTS = 3;

    private final Bank bank;
    private final Scanner scanner;

    public ATM(Bank bank) {
        this.bank    = bank;
        this.scanner = new Scanner(System.in);
    }

    /** Starts the ATM session. */
    public void start() {
        printBanner();
        Account account = authenticate();
        if (account != null) {
            System.out.println("  [Menu to be implemented...]");
        }
        scanner.close();
    }

    /**
     * Prompts for User ID and PIN.
     * Blocks access after MAX_ATTEMPTS failed tries.
     */
    private Account authenticate() {
        System.out.println();
        System.out.println("  " + "-".repeat(50));
        System.out.println("  Please insert your card (enter credentials)");
        System.out.println("  " + "-".repeat(50));

        int attempts = 0;

        while (attempts < MAX_ATTEMPTS) {
            if (attempts > 0) {
                int remaining = MAX_ATTEMPTS - attempts;
                System.out.println("  [!] Invalid credentials. "
                        + remaining + " attempt(s) remaining.");
            }

            System.out.print("  User ID : ");
            String userId = scanner.nextLine().trim();

            System.out.print("  PIN     : ");
            String pin = scanner.nextLine().trim();

            Account account = bank.authenticate(userId, pin);

            if (account != null) {
                System.out.println();
                System.out.println("  Welcome, " + account.getUserId() + "!");
                System.out.printf("  Current balance: $ %.2f%n", account.getBalance());
                return account;
            }

            attempts++;
        }

        System.out.println();
        System.out.println("  [X] Card blocked after " + MAX_ATTEMPTS
                + " failed attempts. Please contact your bank.");
        return null;
    }

    private void printBanner() {
        System.out.println();
        System.out.println("  " + "=".repeat(50));
        System.out.println("  " + bank.getName().toUpperCase() + " — ATM Interface");
        System.out.println("  Oasis Infobyte — Java Development Task 3");
        System.out.println("  " + "=".repeat(50));
    }
}
