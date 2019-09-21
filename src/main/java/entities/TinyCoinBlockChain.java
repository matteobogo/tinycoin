package entities;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

public class TinyCoinBlockChain {

    /** BlockChain Params */
    @Getter(AccessLevel.PUBLIC) private final LinkedHashMap<String,TinyCoinBlock> blocks;
    @Getter(AccessLevel.PUBLIC) private final TinyCoinBlock genesis;
    @Setter(AccessLevel.PUBLIC) @Getter(AccessLevel.PUBLIC) private TinyCoinBlock head;

    /** Selfish Params */
    @Setter(AccessLevel.PUBLIC) @Getter(AccessLevel.PUBLIC) private TinyCoinBlock privateHead;
    @Getter(AccessLevel.PUBLIC) private int privateChainLength;
    @Setter(AccessLevel.PUBLIC) @Getter(AccessLevel.PUBLIC) private TinyCoinBlock lastPublishedBlock;

    public TinyCoinBlockChain(TinyCoinBlock genesis) {

        blocks = new LinkedHashMap<>();
        blocks.put(genesis.getCurrentBlockId(),genesis);
        this.genesis = genesis;
        this.head = genesis;
        this.privateHead = genesis;
        this.privateChainLength = 0;
        this.lastPublishedBlock = genesis;
    }

    public void put(TinyCoinBlock block) {

        blocks.put(block.getCurrentBlockId(),block);
    }

    public TinyCoinBlock get(String id) {

        return blocks.get(id);
    }

    public TinyCoinBlock findForkedBlock(TinyCoinBlock newHead, TinyCoinBlock oldHead) {

        TinyCoinBlock mainChainPointer = oldHead;
        TinyCoinBlock newChainPointer = newHead;

        while(!mainChainPointer.equals(newChainPointer)) {
            if(mainChainPointer.getHeight() > newChainPointer.getHeight())
                mainChainPointer = blocks.get(mainChainPointer.getPreviousBlockId());
            else
                newChainPointer = blocks.get(newChainPointer.getPreviousBlockId());
        }
        return mainChainPointer;
    }

    public boolean findTransactionInsideBlockChain(TinyCoinTransaction transaction) {

        boolean found = false;
        TinyCoinBlock cursor = this.head;

        while(!cursor.equals(genesis) && !found) {

            if(cursor.getTransactions().contains(transaction))
                found = true;

            cursor = blocks.get(cursor.getPreviousBlockId());
        }

        return found;
    }

    public List<TinyCoinBlock> getPublicChain() {

        List<TinyCoinBlock> publicChain = new ArrayList<>();
        TinyCoinBlock pubPointer = this.head;

        while(!pubPointer.equals(this.genesis)) {

            publicChain.add(pubPointer);

            pubPointer = blocks.get(pubPointer.getPreviousBlockId());
        }

        publicChain.add(this.genesis);

        return publicChain;
    }

    public Optional<TinyCoinBlock> isNewFork(String forkedBlockId) {

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

    public List<TinyCoinBlock> getAllPrivateChainBlocksToFork() {

        List<TinyCoinBlock> privateBlocks = new ArrayList<>();

        TinyCoinBlock headPointer = this.getHead();
        TinyCoinBlock privHeadPointer = this.privateHead;

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

    public TinyCoinBlock getFirstUnpublishedBlock() {

        TinyCoinBlock cursor = this.privateHead;
        TinyCoinBlock firstUnpublishedBlock = null;

        while(!cursor.equals(this.lastPublishedBlock)) {

            firstUnpublishedBlock = cursor;
            cursor = blocks.get(cursor.getPreviousBlockId());
        }

        return firstUnpublishedBlock;
    }
}