package com.oasisinfobyte.atm;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a single immutable banking transaction.
 *
 * Once created, a Transaction cannot be modified — its fields are all final.
 * This guarantees the integrity of the transaction history.
 */
public class Transaction {

    // -------------------------------------------------------------------------
    // Transaction types
    // -------------------------------------------------------------------------

    /**
     * Enum listing every possible transaction type.
     * Using an enum instead of a plain String prevents typos and
     * makes switch-case exhaustive checking possible.
     */
    public enum Type {
        DEPOSIT,
        WITHDRAWAL,
        TRANSFER_OUT,   // money sent to another account
        TRANSFER_IN     // money received from another account
    }

    // -------------------------------------------------------------------------
    // Formatter shared by all Transaction instances (thread-safe in Java 8+)
    // -------------------------------------------------------------------------

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    // -------------------------------------------------------------------------
    // Attributes (all final → immutable after construction)
    // -------------------------------------------------------------------------

    /** Category of this transaction. */
    private final Type type;

    /** Amount involved (always positive). */
    private final BigDecimal amount;

    /** Human-readable description (e.g. "Transfer to U002"). */
    private final String description;

    /** Exact date and time the transaction was recorded. */
    private final LocalDateTime dateTime;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Creates a new Transaction stamped with the current date and time.
     *
     * @param type        category of the transaction
     * @param amount      positive monetary amount
     * @param description short label explaining the operation
     */
    public Transaction(Type type, BigDecimal amount, String description) {
        this.type        = type;
        this.amount      = amount;
        this.description = description;
        this.dateTime    = LocalDateTime.now();   // snapshot of the current moment
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    /** @return transaction type */
    public Type getType() {
        return type;
    }

    /** @return transaction amount */
    public BigDecimal getAmount() {
        return amount;
    }

    /** @return human-readable description */
    public String getDescription() {
        return description;
    }

    /** @return exact date and time of the transaction */
    public LocalDateTime getDateTime() {
        return dateTime;
    }

    // -------------------------------------------------------------------------
    // Display
    // -------------------------------------------------------------------------

    /**
     * Returns a formatted, single-line representation of this transaction.
     * Used when printing the transaction history in the ATM menu.
     *
     * Example output:
     *   [11/11/2026 13:05:00]  DEPOSIT       +   500.00  | ATM Deposit
     *   [11/11/2026 13:06:00]  WITHDRAWAL    -   200.00  | ATM Withdrawal
     */
    @Override
    public String toString() {
        // Choose a sign prefix based on whether money came in or went out
        String sign;
        switch (type) {
            case DEPOSIT:
            case TRANSFER_IN:
                sign = "+";
                break;
            default:
                sign = "-";
                break;
        }

        return String.format("[%s]  %-14s %s %9.2f  | %s",
                dateTime.format(FORMATTER),
                type,
                sign,
                amount,
                description);
    }
}
