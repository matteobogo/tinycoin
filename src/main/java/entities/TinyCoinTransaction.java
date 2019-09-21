package entities;

import lombok.AccessLevel;
import lombok.Getter;

@Getter(AccessLevel.PUBLIC)
public class TinyCoinTransaction {

    private final long senderAddress;
    private final long receiverAddress;
    private final double amount;

    public TinyCoinTransaction(long senderAddress, long receiverAddress, double amount) {
        this.senderAddress = senderAddress;
        this.receiverAddress = receiverAddress;
        this.amount = amount;
    }
}