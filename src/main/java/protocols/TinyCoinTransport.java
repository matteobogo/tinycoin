package protocols;

import peersim.config.Configuration;
import peersim.core.Node;
import peersim.edsim.EDSimulator;
import peersim.transport.Transport;

public class TinyCoinTransport implements Transport {

    private static final String PARAM_TRANS = "base_transaction_delay";
    private static final String PARAM_BLOCK = "base_block_delay";

    private final double baseTransactionDelay;
    private final double baseBlockDelay;

    private long dynamicLatency;

    public TinyCoinTransport(String prefix) {

        this.baseTransactionDelay = Configuration.getDouble(prefix + "." + PARAM_TRANS);
        this.baseBlockDelay = Configuration.getDouble(prefix + "." + PARAM_BLOCK);
    }

    @Override
    public void send(Node senderNode, Node receiverNode, Object object, int pid) {

        EDSimulator.add(dynamicLatency,object,receiverNode,pid);
    }

    public void setDynamicLatency(long latency) {

        this.dynamicLatency = latency;
    }

    @Override
    public long getLatency(Node node, Node node1) {

        return dynamicLatency;
    }

    public double getBaseTransactionDelay() { return this.baseTransactionDelay; }
    public double getBaseBlockDelay() { return this.baseBlockDelay; }

    @Override
    public Object clone() {
        return this;
    }
}