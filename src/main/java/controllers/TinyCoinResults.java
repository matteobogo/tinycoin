package controllers;

import entities.TinyCoinBlock;
import entities.TinyCoinNode;
import init.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import protocols.TinyCoinProtocol;
import protocols.TinyCoinProtocolBaseMiner;
import protocols.TinyCoinProtocolSelfishMiner;

import java.text.DecimalFormat;
import java.util.List;

public class TinyCoinResults implements Control {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private static final String PAR_END_SIM = "simulation_end";
    private long simulationEnd;
    private int nTransactions = 0;
    private int nForks = 0;
    private int nBranchesSwaps = 0;
    private int nSelfishMinersLosses = 0;
    private int nSelfishMinersTies = 0;
    private int nSelfishMinersWins = 0;
    private int nResolvedForks = 0;
    private int nForkLength = 0;
    private int nCycles = 0;
    private int nUnresolvedForks = 0;

    public TinyCoinResults(String prefix) {
        this.simulationEnd = Configuration.getLong(prefix + "." + PAR_END_SIM);
    }

    private void printStats(Node node, TinyCoinProtocol p) {
        log.info("-----------------------------");
        log.info("Node {} STATS", node.getID());
        log.info("-----------------------------");

        DecimalFormat formatter = new DecimalFormat("#.########");
        log.info("Wallet: {}", formatter.format(p.getWallet()));
        log.info("Transactions made: {}", p.getNTransactions());
        nTransactions += p.getNTransactions();
        log.info("Main Branch size: {}", p.getBlockChain().getHead().getHeight());
        log.info("Forks: {}", p.getNForks());
        nForks += p.getNForks();
        log.info("Convergences (Side => Main): {}", p.getBranchesSwaps());
        nBranchesSwaps += p.getBranchesSwaps();
    }

    @Override
    public boolean execute() {
        if(CommonState.getTime() < simulationEnd - 1) return false;

        log.info("- TinyCoin Statistics -");
        log.info("-----------------------------");

        TinyCoinNode node;
        TinyCoinProtocol protocol;

        for(int i = 0; i < Network.size(); ++i) {
            node = (TinyCoinNode) Network.get(i);
            protocol = (TinyCoinProtocol) node.getProtocol(node.getCurrentProtocolId());

            printStats(node,protocol);

            /* Miners */
            String nodeType = protocol.getNodeType();

            if(!nodeType.equals(TinyCoinProtocol._NODE_TYPE_NORMAL)) {

                TinyCoinProtocolBaseMiner pMiner = (TinyCoinProtocolBaseMiner) protocol;

                log.info("Blocks Mined: {}",pMiner.getBlocksMined());

                switch (nodeType) {

                    case TinyCoinProtocol._NODE_TYPE_HONEST_MINER:

                        break;

                    case TinyCoinProtocol._NODE_TYPE_SELFISH_MINER:

                        TinyCoinProtocolSelfishMiner pSelfish = (TinyCoinProtocolSelfishMiner) protocol;

                        log.info("Selfish Mining losses: {}", pSelfish.getNSelfishMinerLosses());
                        nSelfishMinersLosses += pSelfish.getNSelfishMinerLosses();
                        log.info("Selfish Mining Ties: {}", pSelfish.getNSelfishMinerTies());
                        nSelfishMinersTies += pSelfish.getNSelfishMinerTies();
                        log.info("Selfish Mining Wins {}", pSelfish.getNSelfishMinerWins());
                        nSelfishMinersWins += pSelfish.getNSelfishMinerWins();

                        break;
                }
            }
        }

        /* General Statistics */
        log.info("-----------------------------");

        log.info("- TinyCoin Simulation Total Results -");
        log.info("-----------------------------");
        log.info("Total Transactions made: {}", nTransactions);
        log.info("Total Number of Forks: {}", nForks);
        log.info("Average Number of Forks: {}", nForks / Network.size());
        log.info("Average Number of Branches Swaps: {}", nBranchesSwaps / Network.size());
        log.info("Total Selfish Miners Losses: {}", nSelfishMinersLosses);
        log.info("Total Selfish Miners Ties: {}", nSelfishMinersTies);
        log.info("Total Selfish Miners Wins: {}", nSelfishMinersWins);;
        log.info("-----------------------------");

        if(Parameters.getIstance().get_SELFISH_POOL_ACTIVE() == 1) {

            TinyCoinNode poolNode = Parameters.getIstance().getSelfishPoolNode();
            TinyCoinProtocolSelfishMiner pSelfish =
                    (TinyCoinProtocolSelfishMiner) poolNode.getProtocol(poolNode.getCurrentProtocolId());

            List<TinyCoinBlock> blocksPublished = pSelfish.getPrivateBlockPublished();

            List<TinyCoinBlock> publicChain = pSelfish.getBlockChain().getPublicChain();

            int counter = 0;
            for (TinyCoinBlock privBlockPublished : blocksPublished)
                if (publicChain.contains(privBlockPublished))
                    ++counter;


            log.info("- Selfish Pool Results -");
            log.info("-----------------------------");
            log.info("- Parameters -");
            log.info("Power {}%",Parameters.getIstance().get_SELFISH_POOL_POWER() * 100);

            log.info("-----------------------------");

            log.info("Main Branch Height: {}",pSelfish.getBlockChain().getHead().getHeight());
            log.info("Main Branch Size: {}",publicChain.size()-1);
            log.info("Private Chain Height: {}",pSelfish.getBlockChain().getPrivateHead().getHeight());
            log.info("Blocks Store in Map: {}",pSelfish.getBlockChain().getBlocks().size());
            log.info("Mempool Size: {}",pSelfish.getMempoolSet().size());
            log.info("Private Blocks Published: {}",blocksPublished.size());
            log.info("Private Blocks inside Main Branch: {}",counter);
            log.info("Selfish pool chosen {} times of {} total choices", TinyCoinOracle.nSelfishPoolChosen, TinyCoinOracle.nMinersChosen);
            log.info("Forks: {}", pSelfish.getNForks());
            log.info("Swaps: {}", pSelfish.getBranchesSwaps());

            log.info("-----------------------------");

        }

        /* Forks */
        int i;
        int net_size = Network.size();
        if(Parameters.getIstance().get_SELFISH_POOL_ACTIVE() == 1) {
            i = 1;
            net_size--;
        }
        else
            i = 0;

        nForks = 0;
        nForkLength = 0;

        /* Forks */
        while(i < Network.size()) {

            node = (TinyCoinNode) Network.get(i);
            protocol = (TinyCoinProtocol) node.getProtocol(node.getCurrentProtocolId());

            protocol.getForks().values()
                    .forEach(e -> {

                        nForks++;
                        nForkLength += e.getSize();

                        if(e.isResolved()) {

                            nResolvedForks++;
                            nCycles += (e.getCycleResolved() - e.getCycleStart());
                        }
                    });

            nUnresolvedForks = nForks - nResolvedForks;
            ++i;
        }

        log.info("-----------------------------");
        log.info("- Forks Statistics -");
        log.info("-----------------------------");
        log.info("Total Forks: {}",nForks);
        double avg_fork = nForks / net_size;
        log.info("Average Forks per Node: {}", avg_fork);
        log.info("Total Resolved Forks: {}", nResolvedForks);
        log.info("Total Unresolved Forks: {}", nUnresolvedForks);
        double avf_fork_len = nForkLength / nForks;
        log.info("Average Fork Length: {}", avf_fork_len);
        double avg_cycle = nCycles / nResolvedForks;
        log.info("Average Cycles for resolving a Fork: {}", avg_cycle);

        return false;
    }
}