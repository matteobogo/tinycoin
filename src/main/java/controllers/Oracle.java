package controllers;

import entities.TinyNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import protocols.TinyProtocol;
import protocols.TinyProtocolBaseMiner;
import init.Parameters;
import utilities.Utils;

public class Oracle implements Control {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private static final String PAR_DELAY = "delay_block_mined";
    private int delay_block_mined;
    public static int nMinersChosen = 0;
    public static int nSelfishPoolChosen = 0;

    public Oracle(String prefix) {
        delay_block_mined = Configuration.getInt(prefix + "." + PAR_DELAY);
    }

    @Override
    public boolean execute() {

        //select randomly a miner every K cycles
        long currentCycle = CommonState.getTime();
        
        if(currentCycle == 0) {
            return false;
        }

        if(currentCycle % delay_block_mined == 0) {
            String winnerMinerType = Parameters.getIstance().getRwcMinerTypePower().next();
            TinyNode winnerNode = null;
            TinyProtocolBaseMiner pMiner;
            
            switch(winnerMinerType) {

                case TinyProtocolBaseMiner._POWER_CPU:
                    winnerNode = Utils.getRandomElementFromList(Parameters.getIstance().getCpuNodes());
                    break;

                case TinyProtocolBaseMiner._POWER_GPU:
                    winnerNode = Utils.getRandomElementFromList(Parameters.getIstance().getGpuNodes());
                    break;

                case TinyProtocolBaseMiner._POWER_FPGA:
                    winnerNode = Utils.getRandomElementFromList(Parameters.getIstance().getFpgaNodes());
                    break;

                case TinyProtocolBaseMiner._POWER_ASIC:
                    winnerNode = Utils.getRandomElementFromList(Parameters.getIstance().getAsicNodes());
                    break;

                case TinyProtocol._NODE_TYPE_SELFISH_POOL:
                    winnerNode = Parameters.getIstance().getSelfishPoolNode();
                    nSelfishPoolChosen++;
                    break;
            }

            nMinersChosen++;
            
            if(winnerNode == null) {
                log.error("Error! Can't find a valid node");
                System.exit(1);
            }

            pMiner = (TinyProtocolBaseMiner) winnerNode.getProtocol(winnerNode.getCurrentProtocolId());

            //STATS
            log.info("[ID: {}][ProtocolID: {}][Type: {}][MinerType: {}] chosen by The Oracle",
                    winnerNode.getID(),winnerNode.getCurrentProtocolId(),pMiner.getNodeType(),winnerMinerType);


            pMiner.blockSolved(winnerNode, winnerNode.getCurrentProtocolId());

            if(winnerMinerType.equals(TinyProtocolBaseMiner._NODE_TYPE_SELFISH_POOL)) {

                //STATS
                log.info("---------- SELFISH POOL STATS ----------");
                log.info("[SELFISH_POOL][N_FORKS: {}][N_SWAPS: {}][MEMPOOL_SIZE: {}][BLOCKS_MINED: {}][PRIV_CHAIN_SIZE: {}]" +
                                "[MAIN_BRANCH_SIZE: {}]",
                        pMiner.getNForks(),pMiner.getBranchesSwaps(),pMiner.getMempoolSet().size(),pMiner.getBlocksMined(),
                        pMiner.getBlockChain().getPrivateHead().getHeight(),pMiner.getBlockChain().getHead().getHeight());
            }

        }
        return false;
    }
}