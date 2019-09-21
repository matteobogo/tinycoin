package entities;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter(AccessLevel.PUBLIC)
public class TinyCoinBlock {

    private final String currentBlockId;
    private final String previousBlockId;
    private final List<TinyCoinTransaction> transactions;
    @Setter(AccessLevel.PUBLIC) private int height;

    public TinyCoinBlock(
            String currentBlockId,
            String previousBlockId,
            List<TinyCoinTransaction> transactions,
            int height) {

        this.currentBlockId = currentBlockId;
        this.previousBlockId = previousBlockId;
        this.transactions = transactions;
        this.height = height;
    }
}