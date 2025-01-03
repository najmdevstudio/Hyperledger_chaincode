package main.java.com.bankchaincode;

import main.java.com.bankchaincode.model.Account;
import main.java.com.bankchaincode.model.Customer;
import main.java.com.bankchaincode.model.TransactionRecord;
import org.hyperledger.fabric.contract.*;
import org.hyperledger.fabric.contract.annotation.*;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.*;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.Instant;
import java.util.*;

@Contract(name ="BankchainCode")
public class BankChaincode implements ContractInterface{

    private final Gson gson = new GsonBuilder().create();

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void addCustomer(Context ctx, String customerId, String firstName, String lastName, String dateOfBirth, String bankCollection) {
        ChaincodeStub stub = ctx.getStub();
        String customerKey = "CUSTOMER_" + customerId;

        // Retrieve the data from the private collection
        byte[] existingCustomerBytes = stub.getPrivateData(bankCollection, customerKey);
        if (existingCustomerBytes != null && existingCustomerBytes.length > 0) {
            throw new RuntimeException("Customer already exists with ID: " + customerId);
        }

        Customer customer = new Customer();
        customer.setCustomerId(customerId);
        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        customer.setDateOfBirth(dateOfBirth);

        String customerJson = gson.toJson(customer);

        // Store customer data in the private collection
        stub.putPrivateData(bankCollection, customerKey, customerJson);
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void addAccount(Context ctx, String accountId, String customerId, String ifscCode, double balance, String bankCollection) {
        ChaincodeStub stub = ctx.getStub();
        String accountKey = "ACCOUNT_" + accountId;
        String customerKey = "CUSTOMER_" + customerId;

        // Retrieve the account data from the private collection
        byte[] existingAccountBytes = stub.getPrivateData(bankCollection, accountKey);
        if (existingAccountBytes != null && existingAccountBytes.length > 0) {
            throw new RuntimeException("Account already exists with ID: " + accountId);
        }

        // Verify customer existence in the private collection
        byte[] existingCustomerBytes = stub.getPrivateData(bankCollection, customerKey);
        if (existingCustomerBytes == null || existingCustomerBytes.length == 0) {
            throw new RuntimeException("Customer does not exist with ID: " + customerId);
        }

        Account account = new Account();
        account.setAccountId(accountId);
        account.setCustomerId(customerId);
        account.setIfscCode(ifscCode);
        account.setBalance(balance);

        String accountJson = gson.toJson(account);

        // Store account data in the private collection
        stub.putPrivateData(bankCollection, accountKey, accountJson);
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void makeTransaction(Context ctx, String fromAccountId, String toAccountId, double amount) {
        ChaincodeStub stub = ctx.getStub();
        String fromAccountKey = "ACCOUNT_" + fromAccountId;
        String toAccountKey = "ACCOUNT_" + toAccountId;

        String fromAccountJson = stub.getStringState(fromAccountKey);
        String toAccountJson = stub.getStringState(toAccountKey);

        if (fromAccountJson.isEmpty()) {
            throw new RuntimeException("From account does not exist: " + fromAccountId);
        }

        Account fromAccount = gson.fromJson(fromAccountJson, Account.class);

        if (toAccountJson.isEmpty()) {
            // Inter-bank transaction handling
            handleInterBankTransaction(ctx, fromAccount, toAccountId, amount);
        } else {
            // Same-bank transaction
            Account toAccount = gson.fromJson(toAccountJson, Account.class);
            if (!fromAccount.getIfscCode().equals(toAccount.getIfscCode())) {
                throw new RuntimeException("Inter-bank transactions must use the settlement method.");
            }

            if (fromAccount.getBalance() < amount) {
                throw new RuntimeException("Insufficient balance in account: " + fromAccountId);
            }

            // Update balances
            fromAccount.setBalance(fromAccount.getBalance() - amount);
            toAccount.setBalance(toAccount.getBalance() + amount);

            // Save updated accounts
            stub.putStringState(fromAccountKey, gson.toJson(fromAccount));
            stub.putStringState(toAccountKey, gson.toJson(toAccount));

            logTransaction(ctx, fromAccountId, fromAccount.getIfscCode(), toAccountId, toAccount.getIfscCode(), amount);
        }
    }

    private void handleInterBankTransaction(Context ctx, Account fromAccount, String toAccountId, double amount) {
        if (fromAccount.getBalance() < amount) {
            throw new RuntimeException("Insufficient balance for inter-bank transaction.");
        }

        fromAccount.setBalance(fromAccount.getBalance() - amount);
        ctx.getStub().putStringState("ACCOUNT_" + fromAccount.getAccountId(), gson.toJson(fromAccount));

        // Log inter-bank transaction
        logTransaction(ctx, fromAccount.getAccountId(), fromAccount.getIfscCode(), toAccountId, "UNKNOWN", amount);
    }

    private void logTransaction(Context ctx, String fromAccountId, String fromIfsc, String toAccountId, String toIfsc, double amount) {
        TransactionRecord transaction = new TransactionRecord();
        transaction.setTransactionId(UUID.randomUUID().toString());
        transaction.setFromAccountId(fromAccountId);
        transaction.setFromIfscCode(fromIfsc);
        transaction.setToAccountId(toAccountId);
        transaction.setToIfscCode(toIfsc);
        transaction.setAmount(amount);
        transaction.setTransactionTime(Instant.now().toString());

        String transactionKey = "TRANSACTION_" + transaction.getTransactionId();
        ctx.getStub().putStringState(transactionKey, gson.toJson(transaction));
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String getTransactionById(Context ctx, String transactionId) {
        ChaincodeStub stub = ctx.getStub();
        String transactionKey = "TRANSACTION_" + transactionId;

        String transactionJson = stub.getStringState(transactionKey);
        if (transactionJson.isEmpty()) {
            throw new RuntimeException("Transaction not found: " + transactionId);
        }

        return transactionJson;
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String queryTransactions(Context ctx, String startDate, String endDate, String ifscCode) {
        ChaincodeStub stub = ctx.getStub();

        String queryString = String.format(
                "{\"selector\":{\"transactionTime\":{\"$gte\":\"%s\",\"$lte\":\"%s\"},\"$or\":[{\"fromIfscCode\":\"%s\"},{\"toIfscCode\":\"%s\"}]}}",
                startDate, endDate, ifscCode, ifscCode);

        QueryResultsIterator<KeyValue> results = stub.getQueryResult(queryString);
        List<TransactionRecord> transactions = new ArrayList<>();

        for (KeyValue result : results) {
            TransactionRecord transaction = gson.fromJson(result.getStringValue(), TransactionRecord.class);
            transactions.add(transaction);
        }

        return gson.toJson(transactions);
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String queryInterBankTransactions(Context ctx, String startDate, String endDate) {
        ChaincodeStub stub = ctx.getStub();

        String queryString = String.format(
                "{\"selector\":{\"transactionTime\":{\"$gte\":\"%s\",\"$lte\":\"%s\"},\"toIfscCode\":\"UNKNOWN\"}}",
                startDate, endDate);

        QueryResultsIterator<KeyValue> results = stub.getQueryResult(queryString);
        List<TransactionRecord> transactions = new ArrayList<>();

        for (KeyValue result : results) {
            TransactionRecord transaction = gson.fromJson(result.getStringValue(), TransactionRecord.class);
            transactions.add(transaction);
        }

        return gson.toJson(transactions);
    }
}
