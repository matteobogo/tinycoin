package utilities;

import entities.Block;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class ForkStats {

    @Getter(AccessLevel.PUBLIC) private final Block forkedNode;
    @Getter(AccessLevel.PUBLIC) @Setter(AccessLevel.PUBLIC) private long cycleStart;
    @Getter(AccessLevel.PUBLIC) @Setter(AccessLevel.PUBLIC) private long cycleResolved;
    @Getter(AccessLevel.PUBLIC) @Setter(AccessLevel.PUBLIC) private boolean isResolved;
    @Getter(AccessLevel.PUBLIC) @Setter(AccessLevel.PUBLIC) private Block lastElement;
    @Getter(AccessLevel.PUBLIC) @Setter(AccessLevel.PUBLIC) private int size;

    public ForkStats(Block forkedNode, Block lastElement) {

        this.forkedNode = forkedNode;
        this.cycleStart = -1;
        this.cycleResolved = -1;
        this.isResolved = false;
        this.lastElement = lastElement;
        this.size = 1;
    }

    public void increaseSize() { this.size++; }
}