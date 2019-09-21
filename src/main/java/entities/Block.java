package entities;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter(AccessLevel.PUBLIC)
public class Block {

    private final String currentBlockId;
    private final String previousBlockId;
    private final List<Transaction> transactions;
    @Setter(AccessLevel.PUBLIC) private int height;

    public Block(
            String currentBlockId,
            String previousBlockId,
            List<Transaction> transactions,
            int height) {

        this.currentBlockId = currentBlockId;
        this.previousBlockId = previousBlockId;
        this.transactions = transactions;
        this.height = height;
    }
}