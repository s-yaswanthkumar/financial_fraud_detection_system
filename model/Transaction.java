package model;

import java.time.LocalDateTime;

public class Transaction {
    private String txId;
    private String senderId;
    private String receiverId;
    private double amount;
    private String memo;
    private LocalDateTime timestamp;
    private double riskScore;
    private String status;

    public Transaction() {}

    public Transaction(String txId, String senderId, String receiverId, double amount, String memo, 
                       LocalDateTime timestamp, double riskScore, String status) {
        this.txId = txId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.amount = amount;
        this.memo = memo;
        this.timestamp = timestamp;
        this.riskScore = riskScore;
        this.status = status;
    }

    public String getTxId() {
        return txId;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public double getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(double riskScore) {
        this.riskScore = riskScore;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "txId='" + txId + '\'' +
                ", senderId='" + senderId + '\'' +
                ", receiverId='" + receiverId + '\'' +
                ", amount=" + amount +
                ", memo='" + memo + '\'' +
                ", timestamp=" + timestamp +
                ", riskScore=" + riskScore +
                ", status='" + status + '\'' +
                '}';
    }
}
