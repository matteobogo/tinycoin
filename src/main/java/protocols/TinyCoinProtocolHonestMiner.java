package protocols;

import entities.TinyCoinBlock;
import entities.TinyCoinMessage;
import entities.TinyCoinTransaction;
import init.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import peersim.core.Node;

public class TinyCoinProtocolHonestMiner extends TinyCoinProtocolBaseMiner {

    /** Logging */
    private final Logger log = LoggerFactory.getLogger(getClass());

    public TinyCoinProtocolHonestMiner(String prefix) {

        super(prefix);
    }

    @Override
    public Object clone() {

        return super.clone();
    }

    @Override
    protected void receivedBlock(Node node, int pid, TinyCoinBlock block) {

        TinyCoinBlock oldMainChainHead = blockChain.getHead();

        super.receivedBlock(node,pid,block);

        //mining on next block if a new block has been added to main branch
        if(!blockChain.getHead().equals(oldMainChainHead))
            mining(blockChain.getHead());
    }

    @Override
    protected void receivedTransaction(Node node, int pid, TinyCoinTransaction transaction) {

        super.receivedTransaction(node,pid,transaction);

        //if i have almost _MIN_BLOCK_TRANSACTIONS in mempool then i start mining
        if(!isMining && mempoolSet.size() >= Parameters.getIstance().get_MIN_BLOCK_TRANSACTIONS())
            mining(blockChain.getHead());
    }

    @Override
    public void blockSolved(Node node, int pid) {

        if(!isMining) {

            log.warn("Honest Node {} not mining!",nodeAddress);
            return;
        }

        //add block to main branch
        blockChain.put(currentMiningBlock);
        blockChain.setHead(currentMiningBlock);

        //transactions for me inside block?
        currentMiningBlock.getTransactions()
                .stream()
                .filter(t -> t.getReceiverAddress() == nodeAddress)
                .forEach(t -> {

                    if(!unconfirmedTransactionMap.containsKey(t))
                        unconfirmedTransactionMap.put(t,0);
                });

        //increment confirmation counter for my unconfirmed transactions
        unconfirmedTransactionMap.replaceAll((k, v) -> v + 1);
        //check if i have unconfirmed transactions with almost K confirmations
        checkMyUnconfirmedTransactionsAndUpdateWallet();

        //remove from mempool, transactions inside mined block
        this.mempoolSet.removeAll(this.currentMiningBlock.getTransactions());

        //broadcast mined block to neighbors
        this.lastSender = -1;
        this.broadcast(
                node,
                pid,
                new TinyCoinMessage(
                        this.nodeAddress,
                        TinyCoinMessage._BLOCK,
                        this.currentMiningBlock)
        );

        //stats
        blocksMined++;

        //mine next block
        mining(blockChain.getHead());
    }
}