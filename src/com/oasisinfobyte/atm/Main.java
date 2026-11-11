package com.oasisinfobyte.atm;

import java.math.BigDecimal;

/**
 * Entry point of the ATM Interface application.
 */
public class Main {

    public static void main(String[] args) {
        Bank bank = new Bank("Oasis National Bank");
        bank.addAccount(new Account("U001", "1234", new BigDecimal("1500.00")));
        bank.addAccount(new Account("U002", "5678", new BigDecimal("3200.50")));

        ATM atm = new ATM(bank);
        atm.start();
    }
}
