package entities;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class Message {

    public static final int _BLOCK = 0;
    public static final int _TRANSACTION = 1;

    @Getter(AccessLevel.PUBLIC) @Setter(AccessLevel.PUBLIC) private long sender;
    @Getter(AccessLevel.PUBLIC) private final int type;
    @Getter(AccessLevel.PUBLIC) private final Object payload;

    public Message(long sender, int type, Object payload) {
        this.sender = sender;
        this.type = type;
        this.payload = payload;
    }
}