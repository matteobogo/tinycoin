package entities;

import lombok.AccessLevel;
import lombok.Getter;

@Getter(AccessLevel.PUBLIC)
public class Transaction {

    private final long senderAddress;
    private final long receiverAddress;
    private final double amount;

    public Transaction(long senderAddress, long receiverAddress, double amount) {
        this.senderAddress = senderAddress;
        this.receiverAddress = receiverAddress;
        this.amount = amount;
    }
}