package main.java.com.bankchaincode.model;
import com.google.gson.annotations.SerializedName;

public class Account{
    @SerializedName("accountId")
    private String accountId;

    @SerializedName("customerId")
    private String customerId;

    @SerializedName("ifscCode")
    private String ifscCode;

    @SerializedName("balance")
    private double balance;

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getIfscCode() {
        return ifscCode;
    }

    public void setIfscCode(String ifscCode) {
        this.ifscCode = ifscCode;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public Account(String accountId, String customerId, String ifscCode, double balance) {
        this.accountId = accountId;
        this.customerId = customerId;
        this.ifscCode = ifscCode;
        this.balance = balance;
    }

    public Account() {
    }
}
