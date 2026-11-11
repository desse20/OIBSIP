package com.oasisinfobyte.atm;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * Central bank that manages all accounts.
 *
 * Responsibilities:
 *  - Store and retrieve accounts.
 *  - Authenticate a user by userId + PIN.
 *  - Execute transfers between two accounts atomically.
 *
 * The ATM never accesses Account objects directly for sensitive operations;
 * it always goes through the Bank.
 */
public class Bank {

    // -------------------------------------------------------------------------
    // Attributes
    // -------------------------------------------------------------------------

    /** Name of this bank, displayed in the ATM welcome screen. */
    private final String name;

    /** All registered accounts. */
    private final ArrayList<Account> accounts;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Creates a new Bank instance.
     *
     * @param name display name of the bank
     */
    public Bank(String name) {
        this.name     = name;
        this.accounts = new ArrayList<>();
    }

    // -------------------------------------------------------------------------
    // Account management
    // -------------------------------------------------------------------------

    /**
     * Registers a new account with the bank.
     *
     * @param account the Account to add
     */
    public void addAccount(Account account) {
        accounts.add(account);
    }

    /**
     * Looks up an account by its userId.
     *
     * @param userId the identifier to search for
     * @return the matching Account, or null if not found
     */
    public Account findAccount(String userId) {
        for (Account account : accounts) {
            if (account.getUserId().equals(userId)) {
                return account;
            }
        }
        return null;   // account does not exist
    }

    // -------------------------------------------------------------------------
    // Authentication
    // -------------------------------------------------------------------------

    /**
     * Authenticates a user by userId and PIN.
     *
     * Combines lookup + PIN check in one step so the ATM receives
     * the ready-to-use Account object on success.
     *
     * @param userId   account identifier entered by the user
     * @param pin      PIN entered by the user
     * @return the authenticated Account, or null if credentials are invalid
     */
    public Account authenticate(String userId, String pin) {
        Account account = findAccount(userId);

        // findAccount returns null if the userId does not exist
        if (account == null) {
            return null;
        }

        // checkPin compares without exposing the stored PIN
        if (account.checkPin(pin)) {
            return account;
        }

        return null;
    }

    // -------------------------------------------------------------------------
    // Transfer
    // -------------------------------------------------------------------------

    /**
     * Transfers an amount from one account to another.
     *
     * Pre-conditions (checked before calling this method by ATM):
     *  - amount > 0
     *  - sender has sufficient balance
     *  - recipientId != sender's userId
     *
     * @param sender      the authenticated Account sending money
     * @param recipientId userId of the destination account
     * @param amount      positive amount to transfer
     * @return a TransferResult describing success or the reason for failure
     */
    public TransferResult transfer(Account sender, String recipientId, BigDecimal amount) {

        // 1. Prevent self-transfer
        if (sender.getUserId().equals(recipientId)) {
            return TransferResult.SELF_TRANSFER;
        }

        // 2. Find the recipient
        Account recipient = findAccount(recipientId);
        if (recipient == null) {
            return TransferResult.RECIPIENT_NOT_FOUND;
        }

        // 3. Check sender's balance
        if (sender.getBalance().compareTo(amount) < 0) {
            return TransferResult.INSUFFICIENT_FUNDS;
        }

        // 4. Execute the transfer — both sides recorded in their own history
        String descOut = "Transfer to   " + recipientId;
        String descIn  = "Transfer from " + sender.getUserId();

        sender.withdraw(amount, descOut);
        recipient.deposit(amount, descIn);

        return TransferResult.SUCCESS;
    }

    // -------------------------------------------------------------------------
    // Getter
    // -------------------------------------------------------------------------

    /** @return the bank's display name */
    public String getName() {
        return name;
    }

    // -------------------------------------------------------------------------
    // Transfer result enum (inner enum — lives inside Bank, used by ATM)
    // -------------------------------------------------------------------------

    /**
     * Represents the outcome of a transfer attempt.
     * The ATM uses this to display the right error message to the user.
     */
    public enum TransferResult {
        SUCCESS,
        RECIPIENT_NOT_FOUND,
        INSUFFICIENT_FUNDS,
        SELF_TRANSFER
    }
}
