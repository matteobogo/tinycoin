package entities;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

public class BlockChain {

    /** BlockChain Params */
    @Getter(AccessLevel.PUBLIC) private final LinkedHashMap<String,Block> blocks;
    @Getter(AccessLevel.PUBLIC) private final Block genesis;
    @Setter(AccessLevel.PUBLIC) @Getter(AccessLevel.PUBLIC) private Block head;

    /** Selfish Params */
    @Setter(AccessLevel.PUBLIC) @Getter(AccessLevel.PUBLIC) private Block privateHead;
    @Getter(AccessLevel.PUBLIC) private int privateChainLength;
    @Setter(AccessLevel.PUBLIC) @Getter(AccessLevel.PUBLIC) private Block lastPublishedBlock;

    public BlockChain(Block genesis) {

        blocks = new LinkedHashMap<>();
        blocks.put(genesis.getCurrentBlockId(),genesis);
        this.genesis = genesis;
        this.head = genesis;
        this.privateHead = genesis;
        this.privateChainLength = 0;
        this.lastPublishedBlock = genesis;
    }

    public void put(Block block) {

        blocks.put(block.getCurrentBlockId(),block);
    }

    public Block get(String id) {

        return blocks.get(id);
    }

    public Block findForkedBlock(Block newHead, Block oldHead) {

        Block mainChainPointer = oldHead;
        Block newChainPointer = newHead;

        while(!mainChainPointer.equals(newChainPointer)) {
            if(mainChainPointer.getHeight() > newChainPointer.getHeight())
                mainChainPointer = blocks.get(mainChainPointer.getPreviousBlockId());
            else
                newChainPointer = blocks.get(newChainPointer.getPreviousBlockId());
        }
        return mainChainPointer;
    }

    public boolean findTransactionInsideBlockChain(Transaction transaction) {

        boolean found = false;
        Block cursor = this.head;

        while(!cursor.equals(genesis) && !found) {

            if(cursor.getTransactions().contains(transaction))
                found = true;

            cursor = blocks.get(cursor.getPreviousBlockId());
        }

        return found;
    }

    public List<Block> getPublicChain() {

        List<Block> publicChain = new ArrayList<>();
        Block pubPointer = this.head;

        while(!pubPointer.equals(this.genesis)) {

            publicChain.add(pubPointer);

            pubPointer = blocks.get(pubPointer.getPreviousBlockId());
        }

        publicChain.add(this.genesis);

        return publicChain;
    }

    public Optional<Block> isNewFork(String forkedBlockId) {

        return blocks.values()
                .stream()
                .filter(f -> !f.equals(genesis) && f.getPreviousBlockId().equals(forkedBlockId))
                .findAny();
    }

    /** Selfish Methods */
    public void increasePrivateChainCounter() {
        ++this.privateChainLength;
    }

    public void resetPrivateChainCounter() {
        this.privateChainLength = 0;
    }

    public List<Block> getAllPrivateChainBlocksToFork() {

        List<Block> privateBlocks = new ArrayList<>();

        Block headPointer = this.getHead();
        Block privHeadPointer = this.privateHead;

        while(!headPointer.equals(privHeadPointer)) {

            if(headPointer.getHeight() > privHeadPointer.getHeight())
                headPointer = getBlocks().get(headPointer.getPreviousBlockId());
            else {
                privateBlocks.add(privHeadPointer);
                privHeadPointer = getBlocks().get(privHeadPointer.getPreviousBlockId());
            }
        }

        return privateBlocks;
    }

    public Block getFirstUnpublishedBlock() {

        Block cursor = this.privateHead;
        Block firstUnpublishedBlock = null;

        while(!cursor.equals(this.lastPublishedBlock)) {

            firstUnpublishedBlock = cursor;
            cursor = blocks.get(cursor.getPreviousBlockId());
        }

        return firstUnpublishedBlock;
    }
}