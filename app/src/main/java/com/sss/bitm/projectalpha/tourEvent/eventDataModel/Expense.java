package com.sss.bitm.projectalpha.tourEvent.eventDataModel;

import java.io.Serializable;

public class Expense implements Serializable {
    private String expenseId;
    private String expenseName;
    private double expenseCost;

    public Expense() {
    }

    public Expense(String expenseId, String expenseName, Double expenseCost) {
        this.expenseId = expenseId;
        this.expenseName = expenseName;
        this.expenseCost = expenseCost;
    }

    public String getExpenseId() {
        return expenseId;
    }

    public void setExpenseId(String expenseId) {
        this.expenseId = expenseId;
    }

    public String getExpenseName() {
        return expenseName;
    }

    public void setExpenseName(String expenseName) {
        this.expenseName = expenseName;
    }

    public Double getExpenseCost() {
        return expenseCost;
    }

    public void setExpenseCost(Double expenseCost) {
        this.expenseCost = expenseCost;
    }
}
