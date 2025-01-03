package main.java.com.bankchaincode.model;
import com.google.gson.annotations.SerializedName;

public class TransactionRecord{
    @SerializedName("transactionId")
    private String transactionId;

    @SerializedName("fromAccountId")
    private String fromAccountId;

    @SerializedName("fromIfscCode")
    private String fromIfscCode;

    @SerializedName("toAccountId")
    private String toAccountId;

    @SerializedName("toIfscCode")
    private String toIfscCode;

    @SerializedName("amount")
    private double amount;

    @SerializedName("transactionTime")
    private String transactionTime;

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getFromAccountId() {
        return fromAccountId;
    }

    public void setFromAccountId(String fromAccountId) {
        this.fromAccountId = fromAccountId;
    }

    public String getFromIfscCode() {
        return fromIfscCode;
    }

    public void setFromIfscCode(String fromIfscCode) {
        this.fromIfscCode = fromIfscCode;
    }

    public String getToAccountId() {
        return toAccountId;
    }

    public void setToAccountId(String toAccountId) {
        this.toAccountId = toAccountId;
    }

    public String getToIfscCode() {
        return toIfscCode;
    }

    public void setToIfscCode(String toIfscCode) {
        this.toIfscCode = toIfscCode;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getTransactionTime() {
        return transactionTime;
    }

    public void setTransactionTime(String transactionTime) {
        this.transactionTime = transactionTime;
    }

    public TransactionRecord(String transactionId, String fromAccountId, String fromIfscCode, String toAccountId, String toIfscCode, double amount, String transactionTime) {
        this.transactionId = transactionId;
        this.fromAccountId = fromAccountId;
        this.fromIfscCode = fromIfscCode;
        this.toAccountId = toAccountId;
        this.toIfscCode = toIfscCode;
        this.amount = amount;
        this.transactionTime = transactionTime;
    }

    public TransactionRecord() {
    }
}
