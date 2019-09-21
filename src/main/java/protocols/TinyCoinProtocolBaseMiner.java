package protocols;

import entities.TinyCoinBlock;
import entities.TinyCoinTransaction;
import init.Parameters;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import peersim.core.Node;
import utilities.Utils;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Getter(AccessLevel.PUBLIC)
public abstract class TinyCoinProtocolBaseMiner extends TinyCoinProtocol {

    /** Logging */
    private final Logger log = LoggerFactory.getLogger(getClass());

    public static final String _COMPUTATIONAL_TYPE_CPU = "CPU";
    public static final String _COMPUTATIONAL_TYPE_GPU = "GPU";
    public static final String _COMPUTATIONAL_TYPE_FPGA = "FPGA";
    public static final String _COMPUTATIONAL_TYPE_ASIC = "ASIC";

    public static final String _POWER_CPU = "POWER_CPU";
    public static final String _POWER_GPU = "POWER_GPU";
    public static final String _POWER_FPGA = "POWER_FPGA";
    public static final String _POWER_ASIC = "POWER_ASIC";

    /** Miner Params */
    @Setter(AccessLevel.PUBLIC) protected String nodeType;
    @Setter(AccessLevel.PUBLIC) protected String computationalType;

    /** Statistics */
    @Getter(AccessLevel.PUBLIC) protected int blocksMined;

    protected TinyCoinBlock currentMiningBlock;
    protected boolean isMining;

    TinyCoinProtocolBaseMiner(String prefix) {
        super(prefix);
    }

    @Override
    public void init(int pid, int tid, long address, String nodeType) {

        super.init(pid,tid,address,nodeType);

        this.isMining = false;
        this.computationalType = "NS";
        this.currentMiningBlock = null;
        this.nodeType = nodeType;
    }

    @Override
    void initStats() {

        super.initStats();
        this.blocksMined = 0;
    }

    void mining(TinyCoinBlock headBlock) {

        //no transactions in mempool
        if(mempoolSet.isEmpty()) {

            this.isMining = false;
            return;
        }

        this.isMining = true;

        //take transactions from mempool
        List<TinyCoinTransaction> prioritizedTransactions = this.mempoolSet
                .stream()
                .sorted(Comparator.comparing(TinyCoinTransaction::getAmount).reversed())
                .limit(Parameters.getIstance().get_MAX_BLOCK_TRANSACTIONS()-1) //reserved 1 slot for coinbase
                .collect(Collectors.toList());

        //remove from mempool transactions added to candidate block
        this.mempoolSet.removeAll(prioritizedTransactions);

        //calculate reward for computing block
        double reward =
                Parameters.getIstance().get_BLOCK_REWARD() +
                        (Parameters.getIstance().get_TRANSACTION_REWARD() * prioritizedTransactions.size());

        //generate coinbase transaction
        TinyCoinTransaction coinbase = new TinyCoinTransaction(
                this.nodeAddress,
                this.nodeAddress,
                reward);

        //add coinbase in the head of transactions list
        prioritizedTransactions.add(0,coinbase);

        //candidate block
        this.currentMiningBlock = new TinyCoinBlock(
                Utils.getRandomUniqueID(),
                headBlock.getCurrentBlockId(),
                prioritizedTransactions,
                headBlock.getHeight() + 1);
    }

    public abstract void blockSolved(Node node, int pid);
}