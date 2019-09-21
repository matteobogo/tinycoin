package protocols;

import entities.*;
import init.Parameters;
import lombok.AccessLevel;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import peersim.core.Node;

import java.util.ArrayList;
import java.util.List;

public class TinyProtocolSelfishMiner extends TinyProtocolBaseMiner {

    /** Logging */
    private final Logger log = LoggerFactory.getLogger(getClass());

    /** Statistics */
    @Getter(AccessLevel.PUBLIC) private int nSelfishMinerLosses;
    @Getter(AccessLevel.PUBLIC) private int nSelfishMinerWins;
    @Getter(AccessLevel.PUBLIC) private int nSelfishMinerTies;
    @Getter(AccessLevel.PUBLIC) private List<Block> privateBlockPublished;

    public TinyProtocolSelfishMiner(String prefix) { super(prefix); }

    @Override
    public Object clone() {

        return super.clone();
    }

    @Override
    void initStats() {

        super.initStats();
        nSelfishMinerLosses = 0;
        nSelfishMinerWins = 0;
        nSelfishMinerTies = 0;

        this.privateBlockPublished = new ArrayList<>();
    }

    @Override
    protected void receivedBlock(Node node, int pid, Block block) {

        //validations
        if(!validateBlock(block)) {
            log.warn("Node {} not validated Block {}",nodeAddress,block.getCurrentBlockId());
            return;
        }

        //duplicates
        if(blockChain.get(block.getCurrentBlockId()) != null || orphanPool.contains(block)) {
            //log.debug("Node {} rejects Block {} as duplicated",nodeAddress,block.getCurrentBlockId());
            return;
        }

        int delta = blockChain.getPrivateHead().getHeight() - blockChain.getHead().getHeight();

        //manage blockchain with last block received
        boolean handled = handleBlockChain(block);
        if(!handled) return;

        /* Selfish Strategy */
        //check if last block received is on the head of public chain
        if(blockChain.getHead().equals(block)) {

            if(delta < 0) {

                log.error("Selfish Node {} => Delta cannot go below 0", nodeAddress);
                return;
            }

            switch (delta) {

                //others nodes win: public chain has one more block then our private chain
                case 0:

                    blockChain.setPrivateHead(blockChain.getHead());
                    blockChain.setLastPublishedBlock(blockChain.getHead());
                    blockChain.resetPrivateChainCounter();

                    //broadcast block to neighbors
                    broadcast(
                            node,
                            pid,
                            new Message(nodeAddress, Message._BLOCK, block));

                    //stats
                    nSelfishMinerLosses++;

                    break;

                //is a tie: public chain has recovered the disadvantage, now public chain has the same length
                //of our private chain. Selfish miner yields a toss-up (try the fate) and hopes that a portion
                //of the network will choose is block instead of the other one
                case 1:

                    broadcast(
                            node,
                            pid,
                            new Message(nodeAddress, Message._BLOCK, blockChain.getPrivateHead()));

                    //stats
                    nSelfishMinerTies++;
                    privateBlockPublished.add(blockChain.getPrivateHead());

                    break;

                //selfish miner wins due to the lead of 1: broadcast to neighbors all private chain blocks
                case 2:

                    List<Block> privateBlocks = blockChain.getAllPrivateChainBlocksToFork();
                    for (Block privBlock : privateBlocks) {

                        broadcast(
                                node,
                                pid,
                                new Message(nodeAddress, Message._BLOCK, privBlock));

                        privateBlockPublished.add(privBlock);

                    }

                    blockChain.setHead(blockChain.getPrivateHead());
                    blockChain.setLastPublishedBlock(blockChain.getPrivateHead());
                    blockChain.resetPrivateChainCounter();

                    //stats
                    nSelfishMinerWins++;

                    break;

                //selfish miner wins due to a lead grater then 1
                default:

                    Block firstUnpublishedBlock = blockChain.getFirstUnpublishedBlock();

                    if(firstUnpublishedBlock == null) {

                        log.error("Cannot find the first unpublished block");
                        return;
                    }

                    //broadcast first unpublished block
                    broadcast(
                            node,
                            pid,
                            new Message(nodeAddress, Message._BLOCK, firstUnpublishedBlock));

                    blockChain.setLastPublishedBlock(firstUnpublishedBlock);

                    break;

            }

            mining(blockChain.getPrivateHead());
        }
        //received block is inside forks or orphan (broadcast the public block to neighbors)
        else {

            broadcast(
                    node,
                    pid,
                    new Message(nodeAddress, Message._BLOCK, block));
        }

        //manage orphans
        handleOrphans(node,pid,block);
    }

    private boolean findTransactionInsidePrivateChain(Transaction transaction) {

        List<Block> privateBlocks = blockChain.getAllPrivateChainBlocksToFork();

        int j = 0;
        boolean found = false;
        while (j < privateBlocks.size() && !found) {
            if (privateBlocks.get(j).getTransactions().contains(transaction))
                found = true;
            ++j;
        }

        return found;
    }

    @Override
    protected void receivedTransaction(Node node, int pid, Transaction transaction) {

        //is transaction inside private chain?
        if(findTransactionInsidePrivateChain(transaction)) {

            return;
        }

        super.receivedTransaction(node,pid,transaction);

        //check if node isn't mining and there are almost _MIN_BLOCK_TRANSACTIONS inside mempool
        if(!isMining && mempoolSet.size() >= Parameters.getIstance().get_MIN_BLOCK_TRANSACTIONS())
            mining(blockChain.getPrivateHead());
    }

    @Override
    public void blockSolved(Node node, int pid) {

        if(!isMining) {

            log.warn("Selfish Node {} not mining!",nodeAddress);
            return;
        }

        int delta = blockChain.getPrivateHead().getHeight() - blockChain.getHead().getHeight();
        blockChain.put(this.currentMiningBlock);
        blockChain.setPrivateHead(this.currentMiningBlock);
        blockChain.increasePrivateChainCounter();

        //selfish miner wins
        if(delta == 0 && blockChain.getPrivateChainLength() == 2) {

            List<Block> privateBlocks = blockChain.getAllPrivateChainBlocksToFork();
            for (Block privateBlock : privateBlocks) {

                //block contains transactions for me?
                privateBlock
                        .getTransactions()
                        .stream()
                        .filter(t -> t.getReceiverAddress() == nodeAddress)
                        .forEach(t -> {

                            if (!unconfirmedTransactionMap.containsKey(t))
                                unconfirmedTransactionMap.put(t, 0);
                        });

                //increment confirmation counter for my unconfirmed transactions
                unconfirmedTransactionMap.replaceAll((k, v) -> v + 1);

                //check if i have unconfirmed transactions with almost K confirmations
                checkMyUnconfirmedTransactionsAndUpdateWallet();

                //remove from mempool, transactions inside mined block
                this.mempoolSet.removeAll(this.currentMiningBlock.getTransactions());

                broadcast(
                        node,
                        pid,
                        new Message(nodeAddress, Message._BLOCK, privateBlock));

                privateBlockPublished.add(privateBlock);
            }

            blockChain.resetPrivateChainCounter();
            blockChain.setHead(blockChain.getPrivateHead());
            blockChain.setLastPublishedBlock(blockChain.getPrivateHead());
        }

        //stats
        blocksMined++;

        mining(blockChain.getPrivateHead());
    }
}