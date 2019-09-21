package entities;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import peersim.core.GeneralNode;

public class TinyCoinNode extends GeneralNode {

    @Getter(AccessLevel.PUBLIC)
    @Setter(AccessLevel.PUBLIC)
    private int currentProtocolId;

    public TinyCoinNode(String s) {
        super(s);
    }
}