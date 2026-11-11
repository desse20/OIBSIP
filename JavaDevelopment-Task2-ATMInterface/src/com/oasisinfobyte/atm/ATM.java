package com.oasisinfobyte.atm;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

/**
 * ATM — Console interface layer.
 *
 * Responsibilities:
 *  - Greet the user and handle authentication (max 3 attempts).
 *  - Display the main menu and dispatch each option.
 *  - Validate all user input before passing it to Bank / Account.
 *  - Never perform banking logic directly — always delegates to Bank.
 *
 * Separation of concerns:
 *   ATM  →  what the user sees and types
 *   Bank →  what actually happens to the data
 */
public class ATM {

    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------

    /** Maximum number of failed login attempts before the card is blocked. */
    private static final int MAX_ATTEMPTS = 3;

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    /** The bank that holds all accounts and business logic. */
    private final Bank bank;

    /**
     * Single Scanner instance for the whole session.
     * Closed only when the application exits (Quit option).
     */
    private final Scanner scanner;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Creates an ATM connected to the given bank.
     *
     * @param bank the Bank instance managing all accounts
     */
    public ATM(Bank bank) {
        this.bank    = bank;
        this.scanner = new Scanner(System.in);
    }

    // -------------------------------------------------------------------------
    // Entry point
    // -------------------------------------------------------------------------

    /**
     * Starts the ATM session loop.
     */
    public void start() {
        printBanner();

        boolean running = true;
        while (running) {
            Account account = authenticate();

            if (account != null) {
                showMenu(account);
            }

            System.out.println();
            System.out.print("  New session? (yes / no): ");
            String answer = scanner.nextLine().trim().toLowerCase();
            if (!answer.equals("yes") && !answer.equals("y")) {
                running = false;
            }
        }

        System.out.println();
        System.out.println("  Thank you for using " + bank.getName() + ". Goodbye!");
        System.out.println("  " + "=".repeat(50));
        scanner.close();
    }

    // -------------------------------------------------------------------------
    // Authentication — max 3 attempts
    // -------------------------------------------------------------------------

    /**
     * Prompts the user for their User ID and PIN.
     * Allows up to MAX_ATTEMPTS tries before blocking access.
     *
     * @return the authenticated Account, or null if all attempts failed
     */
    private Account authenticate() {
        System.out.println();
        System.out.println("  " + "-".repeat(50));
        System.out.println("  Please insert your card (enter credentials)");
        System.out.println("  " + "-".repeat(50));

        int attempts = 0;

        while (attempts < MAX_ATTEMPTS) {
            int remaining = MAX_ATTEMPTS - attempts;

            if (attempts > 0) {
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

    // -------------------------------------------------------------------------
    // Main menu
    // -------------------------------------------------------------------------

    /**
     * Displays the main menu and processes the user's choice.
     * Loops until the user selects option 5 (Quit).
     *
     * @param account the currently authenticated account
     */
    private void showMenu(Account account) {
        boolean sessionActive = true;

        while (sessionActive) {
            printMenu();
            System.out.print("  Your choice: ");
            String input = scanner.nextLine().trim();

            System.out.println();

            switch (input) {
                case "1":
                    showHistory(account);
                    break;
                case "2":
                    handleWithdraw(account);
                    break;
                case "3":
                    handleDeposit(account);
                    break;
                case "4":
                    handleTransfer(account);
                    break;
                case "5":
                    sessionActive = false;
                    System.out.println("  Session ended. Card ejected.");
                    break;
                default:
                    System.out.println("  [!] Invalid option. Please enter a number from 1 to 5.");
            }
        }
    }

    // -------------------------------------------------------------------------
    // Option 1 — Transaction History
    // -------------------------------------------------------------------------

    private void showHistory(Account account) {
        List<Transaction> history = account.getTransactionHistory();

        System.out.println("  " + "=".repeat(70));
        System.out.println("  TRANSACTION HISTORY — " + account.getUserId());
        System.out.println("  " + "=".repeat(70));

        if (history.isEmpty()) {
            System.out.println("  No transactions found for this account.");
        } else {
            for (Transaction t : history) {
                System.out.println("  " + t);
            }
        }

        System.out.println("  " + "=".repeat(70));
        System.out.printf("  Current balance: $ %.2f%n", account.getBalance());
        System.out.println("  " + "=".repeat(70));
    }

    // -------------------------------------------------------------------------
    // Option 2 — Withdraw
    // -------------------------------------------------------------------------

    private void handleWithdraw(Account account) {
        System.out.println("  --- WITHDRAWAL ---");
        System.out.printf("  Available balance: $ %.2f%n", account.getBalance());

        BigDecimal amount = readAmount("  Amount to withdraw: $ ");
        if (amount == null) return;

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("  [!] Amount must be greater than zero.");
            return;
        }

        if (account.getBalance().compareTo(amount) < 0) {
            System.out.println("  [!] Insufficient Funds.");
            return;
        }

        account.withdraw(amount, "ATM Withdrawal");
        System.out.printf("  [✓] $ %.2f withdrawn successfully.%n", amount);
        System.out.printf("  Remaining balance: $ %.2f%n", account.getBalance());
    }

    // -------------------------------------------------------------------------
    // Option 3 — Deposit
    // -------------------------------------------------------------------------

    private void handleDeposit(Account account) {
        System.out.println("  --- DEPOSIT ---");

        BigDecimal amount = readAmount("  Amount to deposit: $ ");
        if (amount == null) return;

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("  [!] Amount must be greater than zero.");
            return;
        }

        account.deposit(amount, "ATM Deposit");
        System.out.printf("  [✓] $ %.2f deposited successfully.%n", amount);
        System.out.printf("  New balance: $ %.2f%n", account.getBalance());
    }

    // -------------------------------------------------------------------------
    // Option 4 — Transfer
    // -------------------------------------------------------------------------

    private void handleTransfer(Account account) {
        System.out.println("  --- TRANSFER ---");
        System.out.printf("  Available balance: $ %.2f%n", account.getBalance());

        System.out.print("  Recipient User ID: ");
        String recipientId = scanner.nextLine().trim();

        if (recipientId.isEmpty()) {
            System.out.println("  [!] Recipient ID cannot be empty.");
            return;
        }

        BigDecimal amount = readAmount("  Amount to transfer: $ ");
        if (amount == null) return;

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("  [!] Amount must be greater than zero.");
            return;
        }

        Bank.TransferResult result = bank.transfer(account, recipientId, amount);

        switch (result) {
            case SUCCESS:
                System.out.printf("  [✓] $ %.2f transferred to %s successfully.%n",
                        amount, recipientId);
                System.out.printf("  Remaining balance: $ %.2f%n", account.getBalance());
                break;
            case RECIPIENT_NOT_FOUND:
                System.out.println("  [!] Account " + recipientId + " not found.");
                break;
            case INSUFFICIENT_FUNDS:
                System.out.println("  [!] Insufficient Funds.");
                break;
            case SELF_TRANSFER:
                System.out.println("  [!] You cannot transfer money to your own account.");
                break;
            default:
                System.out.println("  [!] Transfer failed. Please try again.");
        }
    }

    // -------------------------------------------------------------------------
    // Helper methods
    // -------------------------------------------------------------------------

    /**
     * Reads a monetary amount from the user.
     * Uses BigDecimal(String) to avoid floating-point imprecision.
     *
     * @param prompt message displayed before the input cursor
     * @return the parsed BigDecimal, or null if the input is not a valid number
     */
    private BigDecimal readAmount(String prompt) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        try {
            return new BigDecimal(input);
        } catch (NumberFormatException e) {
            System.out.println("  [!] Invalid amount. Please enter a numeric value (e.g. 100.00).");
            return null;
        }
    }

    private void printBanner() {
        System.out.println();
        System.out.println("  " + "=".repeat(50));
        System.out.println("  " + bank.getName().toUpperCase() + " — ATM Interface");
        System.out.println("  Oasis Infobyte — Java Development Task 3");
        System.out.println("  " + "=".repeat(50));
    }

    private void printMenu() {
        System.out.println();
        System.out.println("  " + "-".repeat(30));
        System.out.println("  MAIN MENU");
        System.out.println("  " + "-".repeat(30));
        System.out.println("  1. Transaction History");
        System.out.println("  2. Withdraw");
        System.out.println("  3. Deposit");
        System.out.println("  4. Transfer");
        System.out.println("  5. Quit");
        System.out.println("  " + "-".repeat(30));
    }
}
