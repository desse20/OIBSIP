package com.oasisinfobyte.atm;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a bank account.
 *
 * Responsibilities:
 *  - Store account credentials (userId, PIN) and balance.
 *  - Perform deposit and withdrawal operations.
 *  - Maintain a history of all transactions.
 *
 * Encapsulation: all fields are private; access is through methods only.
 */
public class Account {

    // -------------------------------------------------------------------------
    // Attributes
    // -------------------------------------------------------------------------

    /** Unique identifier for this account (e.g. "U001"). */
    private final String userId;

    /**
     * Account PIN stored as a String.
     * Never printed in logs or transaction history.
     */
    private final String pin;

    /** Current account balance. BigDecimal ensures exact decimal arithmetic. */
    private BigDecimal balance;

    /** Ordered list of all transactions performed on this account. */
    private final ArrayList<Transaction> transactions;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Creates a new Account with an initial balance.
     *
     * @param userId         unique account identifier
     * @param pin            secret PIN code
     * @param initialBalance starting balance (must be >= 0)
     */
    public Account(String userId, String pin, BigDecimal initialBalance) {
        this.userId       = userId;
        this.pin          = pin;
        this.balance      = initialBalance;
        this.transactions = new ArrayList<>();
    }

    // -------------------------------------------------------------------------
    // PIN verification
    // -------------------------------------------------------------------------

    /**
     * Verifies the provided PIN against the stored PIN.
     * The PIN itself is never returned or exposed.
     *
     * @param inputPin PIN entered by the user
     * @return true if the PIN matches, false otherwise
     */
    public boolean checkPin(String inputPin) {
        return this.pin.equals(inputPin);
    }

    // -------------------------------------------------------------------------
    // Banking operations
    // -------------------------------------------------------------------------

    /**
     * Deposits an amount into the account.
     * Validation (amount > 0) must be done by the caller (ATM).
     *
     * @param amount     positive amount to deposit
     * @param description short label for the transaction record
     */
    public void deposit(BigDecimal amount, String description) {
        balance = balance.add(amount);
        transactions.add(new Transaction(Transaction.Type.DEPOSIT, amount, description));
    }

    /**
     * Withdraws an amount from the account.
     * Validation (amount > 0 and sufficient funds) must be done by the caller.
     *
     * @param amount      positive amount to withdraw
     * @param description short label for the transaction record
     */
    public void withdraw(BigDecimal amount, String description) {
        balance = balance.subtract(amount);
        transactions.add(new Transaction(Transaction.Type.WITHDRAWAL, amount, description));
    }

    // -------------------------------------------------------------------------
    // Getters (read-only access to private fields)
    // -------------------------------------------------------------------------

    /** @return the account's unique identifier */
    public String getUserId() {
        return userId;
    }

    /** @return the current balance */
    public BigDecimal getBalance() {
        return balance;
    }

    /**
     * Returns the full transaction history as an unmodifiable view.
     * Callers can read but not add/remove entries directly.
     *
     * @return read-only list of transactions
     */
    public List<Transaction> getTransactionHistory() {
        return java.util.Collections.unmodifiableList(transactions);
    }
}
