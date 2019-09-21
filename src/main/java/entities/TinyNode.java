package entities;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import peersim.core.GeneralNode;

public class TinyNode extends GeneralNode {

    @Getter(AccessLevel.PUBLIC)
    @Setter(AccessLevel.PUBLIC)
    private int currentProtocolId;

    public TinyNode(String s) {
        super(s);
    }
}