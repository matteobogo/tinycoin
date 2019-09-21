package protocols;

import entities.*;
import init.Parameters;
import lombok.AccessLevel;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import peersim.cdsim.CDProtocol;
import peersim.config.FastConfig;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import utilities.ForkStats;
import utilities.Utils;
import java.util.*;
import java.util.stream.Collectors;

public class TinyProtocol implements CDProtocol, EDProtocol {

    public static final String _NODE_TYPE_NORMAL = "NORMAL";
    public static final String _NODE_TYPE_HONEST_MINER = "HONEST_MINER";
    public static final String _NODE_TYPE_SELFISH_MINER = "SELFISH_MINER";
    public static final String _NODE_TYPE_SELFISH_POOL = "SELFISH_POOL";

    /* Logging */
    private final Logger log = LoggerFactory.getLogger(getClass());

    /* PeerSim Params */
    private int protocolId;
    private int transportId;

    /* Statistics */
    @Getter(AccessLevel.PUBLIC) private long nForks;
    @Getter(AccessLevel.PUBLIC) private long nTransactions;
    @Getter(AccessLevel.PUBLIC) private long branchesSwaps;
    @Getter(AccessLevel.PUBLIC) private int confirmedTransactions;
    @Getter(AccessLevel.PUBLIC) private int lostTransactions;
    @Getter(AccessLevel.PUBLIC) private Map<String,ForkStats> forks;

    /* Node Params */
    @Getter(AccessLevel.PUBLIC) protected long nodeAddress;
    @Getter(AccessLevel.PUBLIC) protected double wallet;
    @Getter(AccessLevel.PUBLIC) private String nodeType;

    /* TinyCoin Data Structures */
    @Getter(AccessLevel.PUBLIC) protected BlockChain blockChain;
    @Getter(AccessLevel.PUBLIC) protected Set<Transaction> mempoolSet;
    @Getter(AccessLevel.PUBLIC) protected Set<Block> orphanPool;
    @Getter(AccessLevel.PUBLIC) protected Map<Transaction,Integer> unconfirmedTransactionMap;

    long lastSender;

    public TinyProtocol(String prefix) {}

    @Override
    public Object clone() {

        TinyProtocol tiny = null;

        try {

            tiny = (TinyProtocol) super.clone();

        }catch(CloneNotSupportedException e) {
            //
        }
        return tiny;
    }

    @Override
    public void nextCycle(Node localNode, int i) {

        /* Every simulation cycle generate a probability to create a transaction */

        //got enough money?
        if(wallet > Parameters.getIstance().get_TINYCOIN_MIN_VALUE()) {

            //probability to make a transaction
            double probability = Parameters.getRandomBTCInRange(0, 1);
            if (probability < Parameters.getIstance().get_TRANSACTION_PROBABILITY_THRESHOLD()) {

                //randomly choose a transaction amount in range with my wallet
                double amount = Parameters.getRandomBTCInRange(Parameters.getIstance().get_TINYCOIN_MIN_VALUE(), wallet);

                Node receiver;
                do {

                    receiver = Utils.getRandomNodeFromNetwork();

                }while(receiver == localNode);  //don't choose myself

                //make transaction
                makeTransaction(localNode,i,receiver.getID(),amount);
            }
        }
    }

    @Override
    public void processEvent(Node node, int pid, Object o) {

        Message message = (Message) o;

        this.lastSender = message.getSender();
        switch (message.getType()) {

            case Message._BLOCK:

                receivedBlock(
                        node,
                        pid,
                        (Block) message.getPayload());

                break;

            case Message._TRANSACTION:

                receivedTransaction(
                        node,
                        pid,
                        (Transaction) message.getPayload());

                break;
        }
    }

    public void init(int pid, int tid, long address, String nodeType) {

        this.protocolId = pid;
        this.transportId = tid;
        this.nodeAddress = address;
        this.nodeType = nodeType;

        /* Genesis Block */
        Block genesis = new Block(
                Parameters.getIstance().get_GENESIS_BLOCK_ID(),
                null,
                new ArrayList<>(),
                0);

        /* Init BlockChain */
        blockChain = new BlockChain(genesis);

        initDataStructures();
        initWallet();
        initStats();
    }

    private void initDataStructures() {

        this.unconfirmedTransactionMap = new HashMap<>();
        this.mempoolSet = new HashSet<>();
        this.orphanPool = new HashSet<>();
    }

    private void initWallet() {

        this.wallet = Parameters.getRandomBTCInRange(
                Parameters.getIstance().get_INITIAL_WALLET_MIN(),
                Parameters.getIstance().get_INITIAL_WALLET_MAX());
    }

    void initStats() {

        this.nForks = 0;
        this.nTransactions = 0;
        this.branchesSwaps = 0;
        this.confirmedTransactions = 0;
        this.lostTransactions = 0;

        this.forks = new HashMap<>();
    }

    private void makeTransaction(Node localNode, int pid, long receiverAddress, double amount) {

        if(amount > 0 && amount <= wallet) {

            //update wallet
            wallet = wallet - amount;

            //make transaction
            Transaction transaction = new Transaction(
                    nodeAddress,
                    receiverAddress,
                    amount);

            //send transaction to neighbors
            this.lastSender = -1;

            broadcast(
                    localNode,
                    pid,
                    new Message(nodeAddress, Message._TRANSACTION, transaction));

            mempoolSet.add(transaction);

            //stats
            this.nTransactions++;
        }
    }

    private boolean validateTransaction(Transaction transaction) {

        //check sender address, receiver address, transaction amount in legal money range, if we have already a
        //matching transaction in mempool or in a block inside the main branch
        return transaction.getSenderAddress() > -1 &&
                transaction.getReceiverAddress() > -1 &&
                transaction.getAmount() >= Parameters.getIstance().get_TINYCOIN_MIN_VALUE() &&
                transaction.getAmount() <= Parameters.getIstance().get_TINYCOIN_MAX_VALUE() &&
                !mempoolSet.contains(transaction) &&
                !blockChain.findTransactionInsideBlockChain(transaction);
    }

    private void verifyIfTransactionIsForMe(Transaction transaction) {

        if(transaction.getReceiverAddress() == nodeAddress && !unconfirmedTransactionMap.containsKey(transaction))
            unconfirmedTransactionMap.put(transaction,0);
    }

    void receivedTransaction(
            Node node, int pid, Transaction transaction) {

        //transaction validation
        if(!validateTransaction(transaction)) return;

        //add to mempool
        mempoolSet.add(transaction);

        //is transaction for me?
        verifyIfTransactionIsForMe(transaction);

        //broadcast to neighbors
        broadcast(
                node,
                pid,
                new Message(nodeAddress, Message._TRANSACTION, transaction));
    }

    protected boolean validateBlock(Block block) {

        return block.getTransactions() != null &&
                !block.getTransactions().isEmpty() &&
                block.getTransactions().size() <= Parameters.getIstance().get_MAX_BLOCK_TRANSACTIONS() &&
                block.getTransactions().get(0).getAmount() == computeBlockReward(block);
    }

    private double computeBlockReward(Block block) {

        return Parameters.getIstance().get_BLOCK_REWARD() + ((block.getTransactions().size()-1) *
                Parameters.getIstance().get_TRANSACTION_REWARD());  //coinbase excluded
    }

    private void verifyIfBlockContainsTransactionsForMe(Block block) {

        int oldSize = unconfirmedTransactionMap.size();
        block.getTransactions()
                .stream()
                .filter(t -> t.getReceiverAddress() == this.nodeAddress)
                .forEach(transaction -> {

                    if(!unconfirmedTransactionMap.containsKey(transaction)) {
                        unconfirmedTransactionMap.put(transaction, 1);

                    }
                });
    }

    void receivedBlock(Node node, int pid, Block block) {

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

        //manage blockchain with last block received
        boolean handled = handleBlockChain(block);

        if(!handled) return;

        //broadcast block to neighbors
        broadcast(
                node,
                pid,
                new Message(nodeAddress, Message._BLOCK, block));

        //manage orphans
        handleOrphans(node,pid,block);
    }

    boolean handleBlockChain(Block block) {

        Block previous = blockChain.get(block.getPreviousBlockId());
        Block currentHead = blockChain.getHead();

        //where is the previous block of received block?
        if(previous == null) {

            //orphan block
            orphanPool.add(block);
            return true;
        }

        //is there another block after the previous block?
        Optional<Block> isNewFork = blockChain.isNewFork(previous.getCurrentBlockId());

        //put block in the blockchain and update his height
        block.setHeight(previous.getHeight()+1);
        blockChain.put(block);

        //previous is the last block of the main branch
        if(currentHead.equals(previous)) {

            blockChain.setHead(block);

            //add +1 confirmation to all my unconfirmed transactions
            unconfirmedTransactionMap.replaceAll((k, v) -> v + 1);

            //if received block contains transactions for me then store with +1 confirm
            verifyIfBlockContainsTransactionsForMe(block);

            //check my unconfirmed transactions pool for confirmed transaction (with confirms >= threshold)
            //then update the wallet
            checkMyUnconfirmedTransactionsAndUpdateWallet();

            //remove from mempool transactions inside block
            mempoolSet.removeAll(block.getTransactions());

        }
        //block connects to somewhere other then the last block of main branch (forks)
        else {

            /* Statistics */
            if(isNewFork.isPresent()) {

                //new fork
                ForkStats forkStats = new ForkStats(
                        blockChain.get(block.getPreviousBlockId()),
                        block);
                forkStats.setCycleStart(CommonState.getTime());
                forks.put(block.getPreviousBlockId(),forkStats);

                nForks++;
            }
            else {

                for(ForkStats forkStats : forks.values()) {
                    if(forkStats.getLastElement().getCurrentBlockId().equals(block.getPreviousBlockId())) {
                        forkStats.setLastElement(block);
                        forkStats.increaseSize();
                    }
                }
            }

            //find the forked block
            Block forkedBlock = blockChain.findForkedBlock(block,currentHead);

            if(forkedBlock == null) {
                log.error("Node {} Error! No forked block found!", nodeAddress);
                return false;
            }

            //check fork height compare to main branch height and to last block added
            int subMainBranchHeight = currentHead.getHeight() - forkedBlock.getHeight();
            int subForkBranchHeight = block.getHeight() - forkedBlock.getHeight();

            //swap branches?
            if(subForkBranchHeight > subMainBranchHeight) {

                handleChainSwap(currentHead,block,forkedBlock);
                blockChain.setHead(block);

                /* Statistics */
                branchesSwaps++;

                Optional<ForkStats> forkStats = forks.values()
                        .stream()
                        .filter(e -> e.getLastElement().equals(block))
                        .findFirst();

                forkStats.ifPresent(f -> {

                    f.setCycleResolved(CommonState.getTime());
                    f.setResolved(true);
                });
            }
        }
        return true;
    }

    private void handleChainSwap(Block oldHead, Block newHead, Block forkedBlock) {

        //handle old main sub-chain
        //iterate main sub-chain from the head to the forked block
        Block mainChainPointer = oldHead;

        while(!mainChainPointer.equals(forkedBlock)) {

            //block transactions to mempool (copy references if aren't already present)
            mempoolSet.addAll(mainChainPointer.getTransactions());

            //iterate back
            mainChainPointer = blockChain.get(mainChainPointer.getPreviousBlockId());
        }

        //decrease confirmation counters from my unconfirmed transactions pool
        unconfirmedTransactionMap.replaceAll((k, v) -> v - (oldHead.getHeight() - forkedBlock.getHeight()));

        //remove transactions from my unconfirmed transactions map if confirmation counter go below 1
        checkMyUnconfirmedTransactionsAndRemove();

        //increase confirmation counters for unconfirmed transactions outside sub-chains
        //(transactions survived from previous pruning)
        unconfirmedTransactionMap.replaceAll((k,v) -> v + (newHead.getHeight() - forkedBlock.getHeight()));

        //handle new main sub-chain
        //iterate fork sub-chain from the head to the forked block
        Block sideChainPointer = newHead;

        while(!sideChainPointer.equals(forkedBlock)) {

            //if block contains transactions for me then put in my unconfirmed transaction pool
            //with +1 confirm for each block from the head
            int sideChainPointerHeight = sideChainPointer.getHeight();
            sideChainPointer.getTransactions()
                    .stream()
                    .filter(t -> t.getReceiverAddress() == nodeAddress)
                    .forEach(t -> {

                        int heightDiff = newHead.getHeight() - sideChainPointerHeight;
                        unconfirmedTransactionMap.put(t, heightDiff + 1);
                        lostTransactions =- 1;
                    });

            //remove block transactions from mempool
            mempoolSet.removeAll(sideChainPointer.getTransactions());

            //iterate back
            sideChainPointer = blockChain.get(sideChainPointer.getPreviousBlockId());
        }

        //check if my unconfirmed transactions map contains confirmed transactions
        checkMyUnconfirmedTransactionsAndUpdateWallet();
    }

    private void checkMyUnconfirmedTransactionsAndRemove() {

        int oldSize = unconfirmedTransactionMap.size();
        unconfirmedTransactionMap
                .values()
                .removeIf(v -> v < 1);

        int transactionsRemoved = oldSize - unconfirmedTransactionMap.size();
    }

    void checkMyUnconfirmedTransactionsAndUpdateWallet() {

        List<Transaction> confirmed = new ArrayList<>();
        unconfirmedTransactionMap
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue() == Parameters.getIstance().get_MIN_TRANSACTION_CONFIRMS())
                .forEach(e -> {

                    //update wallet
                    wallet += e.getKey().getAmount();

                    confirmed.add(e.getKey());

                    //stats
                    confirmedTransactions++;
                });

        confirmed.forEach(c -> unconfirmedTransactionMap.remove(c));
    }

    private List<TinyNode> getNeighbors(Node localNode, int pid) {

        //obtain neighbors
        Linkable link = (Linkable) localNode.getProtocol(FastConfig.getLinkable(pid));

        //no neighbors
        if(link.degree() == 0) return null;

        List<TinyNode> neighbors = new ArrayList<>();
        for(int i = 0; i < link.degree(); i++)
            neighbors.add((TinyNode) link.getNeighbor(i));

        return neighbors;
    }

    void broadcast(Node localNode, int pid, Message message) {

       List<TinyNode> neighbors = getNeighbors(localNode,pid);
       if(neighbors == null) {

           log.error("Node {} doesn't have any neighbors",nodeAddress);
           return;
       }

        //change sender
        message.setSender(nodeAddress);

        //transport protocol
        TinyTransport transport = (TinyTransport) localNode.getProtocol(transportId);

        neighbors
                .stream()
                .filter(neighbor -> neighbor.getID() != this.lastSender)
                .forEach(neighbor -> {

                    double latency = (long) transport.getBaseTransactionDelay();

                    //latency simulation for block propagation (K * nr. of transactions inside block)
                    if(message.getType() == Message._BLOCK) {

                        int nTransactions = ((Block) message.getPayload()).getTransactions().size();
                        latency = transport.getBaseBlockDelay() * nTransactions;
                    }

                    transport.setDynamicLatency((long)latency);
                    transport.send(localNode, neighbor, message, neighbor.getCurrentProtocolId());
                });
    }

    void handleOrphans(Node node, int pid, Block block) {

        //is there any orphan block that have the last block added as parent?
        List<Block> orphans = orphanPool
                .stream()
                .filter(v -> v.getPreviousBlockId().equals(block.getCurrentBlockId()))
                .collect(Collectors.toList());

        orphanPool.removeAll(orphans);

        //manage orphans
        if(!orphans.isEmpty()) {

            //recursive call
            orphans.forEach(b -> receivedBlock(node, pid, b));
        }
    }
}