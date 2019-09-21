package init;

import entities.TinyNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import peersim.config.Configuration;
import peersim.core.*;
import protocols.TinyProtocol;
import protocols.TinyProtocolBaseMiner;
import protocols.TinyProtocolHonestMiner;
import protocols.TinyProtocolSelfishMiner;
import utilities.RandomWeightedCollection;

public class Initializer implements Control {

    /* Logging */
    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final String PAR_PROT_1 = "protocol1";
    private static final String PAR_PROT_2 = "protocol2";
    private static final String PAR_PROT_3 = "protocol3";
    private static final String PAR_TRANSPORT = "transport";
    private static final String PAR_MIN_AMOUNT = "min_amount";
    private static final String PAR_MAX_AMOUNT = "max_amount";
    private static final String PAR_SATOSHI_DECIMAL = "satoshi_decimal";
    private static final String PAR_MIN_WALLET = "min_wallet";
    private static final String PAR_MAX_WALLET = "max_wallet";
    private static final String PAR_TRANS_CONFIRMS = "trans_confirms";
    private static final String PAR_TRANS_PROBABILITY_THRESHOLD = "trans_prob_threshold";
    private static final String PAR_MAX_TRANS_PER_BLOCK = "max_trans_per_block";
    private static final String PAR_MIN_TRANS_PER_BLOCK = "min_trans_per_block";
    private static final String PAR_BLOCK_REWARD = "block_reward";
    private static final String PAR_TRANS_REWARD = "trans_reward";
    private static final String PAR_N_HONEST_MINERS = "percentage_honest_miners";
    private static final String PAR_N_SELFISH_MINERS = "percentage_selfish_miners";
    private static final String PAR_N_NORMAL_NODES = "percentage_normal_nodes";
    private static final String PAR_N_CPU = "percentage_cpu";
    private static final String PAR_N_GPU = "percentage_gpu";
    private static final String PAR_N_FPGA = "percentage_fpga";
    private static final String PAR_N_ASIC = "percentage_asic";
    private static final String PAR_MINER_CPU_POWER = "power_cpu";
    private static final String PAR_MINER_GPU_POWER = "power_gpu";
    private static final String PAR_MINER_FPGA_POWER = "power_fpga";
    private static final String PAR_MINER_ASIC_POWER = "power_asic";
    private static final String PAR_SELFISH_POOL_ACTIVE = "selfish_pool_active";
    private static final String PAR_SELFISH_POOL_POWER = "selfish_pool_power";

    public Initializer(String prefix) {

        /* Recover protocol name from external config file */

        //protocols
        Parameters.getIstance()._PROTOCOL_1_ID = Configuration.getPid(prefix + "." + PAR_PROT_1);
        Parameters.getIstance()._PROTOCOL_2_ID = Configuration.getPid(prefix + "." + PAR_PROT_2);
        Parameters.getIstance()._PROTOCOL_3_ID = Configuration.getPid(prefix + "." + PAR_PROT_3);
        Parameters.getIstance()._PROTOCOL_TRANSPORT_ID = Configuration.getPid(prefix + "." + PAR_TRANSPORT);

        //constants
        Parameters.getIstance()._TINYCOIN_MIN_VALUE = Configuration.getDouble(prefix + "." + PAR_MIN_AMOUNT);
        Parameters.getIstance()._TINYCOIN_MAX_VALUE = Configuration.getDouble(prefix + "." + PAR_MAX_AMOUNT);
        Parameters.getIstance()._MAX_DECIMAL_SATOSHI = Configuration.getInt(prefix + "." + PAR_SATOSHI_DECIMAL);
        Parameters.getIstance()._INITIAL_WALLET_MIN = Configuration.getDouble(prefix + "." + PAR_MIN_WALLET);
        Parameters.getIstance()._INITIAL_WALLET_MAX = Configuration.getDouble(prefix + "." + PAR_MAX_WALLET);
        Parameters.getIstance()._MIN_TRANSACTION_CONFIRMS = Configuration.getInt(prefix + "." + PAR_TRANS_CONFIRMS);
        Parameters.getIstance()._TRANSACTION_PROBABILITY_THRESHOLD = Configuration.getDouble(prefix + "." + PAR_TRANS_PROBABILITY_THRESHOLD);
        Parameters.getIstance()._MAX_BLOCK_TRANSACTIONS = Configuration.getInt(prefix + "." + PAR_MAX_TRANS_PER_BLOCK);
        Parameters.getIstance()._MIN_BLOCK_TRANSACTIONS = Configuration.getInt(prefix + "." + PAR_MIN_TRANS_PER_BLOCK);
        Parameters.getIstance()._BLOCK_REWARD = Configuration.getDouble(prefix + "." + PAR_BLOCK_REWARD);
        Parameters.getIstance()._TRANSACTION_REWARD = Configuration.getDouble(prefix + "." + PAR_TRANS_REWARD);

        //nodes types distribution
        Parameters.getIstance()._PERCENTAGE_HONEST_MINERS = Configuration.getDouble(prefix + "." + PAR_N_HONEST_MINERS);
        Parameters.getIstance()._PERCENTAGE_SELFISH_MINERS = Configuration.getDouble(prefix + "." + PAR_N_SELFISH_MINERS);
        Parameters.getIstance()._PERCENTAGE_NORMAL_NODES = Configuration.getDouble(prefix + "." + PAR_N_NORMAL_NODES);

        //hardware types distribution
        Parameters.getIstance()._PERCENTAGE_CPU = Configuration.getDouble(prefix + "." + PAR_N_CPU);
        Parameters.getIstance()._PERCENTAGE_GPU = Configuration.getDouble(prefix + "." + PAR_N_GPU);
        Parameters.getIstance()._PERCENTAGE_FPGA = Configuration.getDouble(prefix + "." + PAR_N_FPGA);
        Parameters.getIstance()._PERCENTAGE_ASIC = Configuration.getDouble(prefix + "." + PAR_N_ASIC);

        //hardware power distribution
        Parameters.getIstance()._POWER_CPU = Configuration.getDouble(prefix + "." + PAR_MINER_CPU_POWER);
        Parameters.getIstance()._POWER_GPU = Configuration.getDouble(prefix + "." + PAR_MINER_GPU_POWER);
        Parameters.getIstance()._POWER_FPGA = Configuration.getDouble(prefix + "." + PAR_MINER_FPGA_POWER);
        Parameters.getIstance()._POWER_ASIC = Configuration.getDouble(prefix + "." + PAR_MINER_ASIC_POWER);

        //selfish pool
        Parameters.getIstance()._SELFISH_POOL_ACTIVE = Configuration.getInt(prefix + "." + PAR_SELFISH_POOL_ACTIVE);
        Parameters.getIstance()._SELFISH_POOL_POWER = Configuration.getDouble(prefix + "." + PAR_SELFISH_POOL_POWER);
    }

    @Override
    public boolean execute() {

        log.info("- Initialize TinyCoin Base Nodes -");

        //validation
        if (Parameters.getIstance()._PERCENTAGE_HONEST_MINERS +
                Parameters.getIstance()._PERCENTAGE_SELFISH_MINERS +
                Parameters.getIstance()._PERCENTAGE_NORMAL_NODES != 1) {

            log.error("Wrong Nodes Type Params => check <{}>, <{}> and <{}> values",
                    PAR_N_HONEST_MINERS, PAR_N_SELFISH_MINERS, PAR_N_NORMAL_NODES);
            System.exit(1);
        }

        log.info("Nodes Types Distribution: {}% Honest | {}% Selfish | {}% Normal",
                Parameters.getIstance()._PERCENTAGE_HONEST_MINERS * 100,
                Parameters.getIstance()._PERCENTAGE_SELFISH_MINERS * 100,
                Parameters.getIstance()._PERCENTAGE_NORMAL_NODES * 100);

        double totalMiners = Parameters.getIstance()._PERCENTAGE_HONEST_MINERS + Parameters.getIstance()._PERCENTAGE_SELFISH_MINERS;

        //validation
        if (Parameters.getIstance()._PERCENTAGE_CPU +
                Parameters.getIstance()._PERCENTAGE_GPU +
                Parameters.getIstance()._PERCENTAGE_FPGA +
                Parameters.getIstance()._PERCENTAGE_ASIC != 1) {

            log.error("Wrong Miners Type Params => check <{}>, <{}>, <{}> and <{}> values",
                    PAR_N_CPU, PAR_N_GPU, PAR_N_FPGA, PAR_N_ASIC);
            System.exit(1);
        }

        log.info("Miners Types Distribution: {}% CPU | {}% GPU | {}% FPGA | {}% ASIC",
                Parameters.getIstance()._PERCENTAGE_CPU * 100,
                Parameters.getIstance()._PERCENTAGE_GPU * 100,
                Parameters.getIstance()._PERCENTAGE_FPGA * 100,
                Parameters.getIstance()._PERCENTAGE_ASIC * 100);

        //shuffle the network for randomize roles distribution
        //Network.shuffle();

        //initialize random weighted collections
        initializeRandomWeightedCollections();

        StringBuilder nodesLabels = new StringBuilder("\n- Nodes Information -\n");

        TinyNode node;
        int k = 0;

        if(Parameters.getIstance()._SELFISH_POOL_ACTIVE == 1) {

            log.info("Activated Single Selfish Pool with {}% Network Power",
                    Parameters.getIstance()._SELFISH_POOL_POWER * 100);

            node = (TinyNode) Network.get(0);
            TinyProtocolSelfishMiner pSelfish =
                    (TinyProtocolSelfishMiner) node.getProtocol(Parameters.getIstance()._PROTOCOL_3_ID);
            node.setCurrentProtocolId(Parameters.getIstance()._PROTOCOL_3_ID);

            pSelfish.init(
                    Parameters.getIstance()._PROTOCOL_3_ID,
                    Parameters.getIstance()._PROTOCOL_TRANSPORT_ID,
                    node.getID(),
                    TinyProtocol._NODE_TYPE_SELFISH_MINER);

            //add power pool
            int poolWeight = (int)
                    ((Parameters.getIstance()._SELFISH_POOL_POWER * Parameters.getIstance().rwcMinerTypePower.getTotal())
                            / (1 - Parameters.getIstance()._SELFISH_POOL_POWER));
            Parameters.getIstance().rwcMinerTypePower
                    .add(poolWeight,TinyProtocol._NODE_TYPE_SELFISH_POOL);

            Parameters.getIstance().selfishPoolNode = node;

            k = 1;
        }

        for (int i = k; i < Network.size(); ++i) {

            node = (TinyNode) Network.get(i);

            //choose node type
            String nodeType = Parameters.getIstance().rwcNodeType.next();

            switch (nodeType) {

                case TinyProtocol._NODE_TYPE_NORMAL:

                    TinyProtocol pNormal =
                            (TinyProtocol) node.getProtocol(Parameters.getIstance()._PROTOCOL_1_ID);

                    node.setCurrentProtocolId(Parameters.getIstance()._PROTOCOL_1_ID);

                    pNormal.init(
                            Parameters.getIstance()._PROTOCOL_1_ID,
                            Parameters.getIstance()._PROTOCOL_TRANSPORT_ID,
                            node.getID(),
                            TinyProtocol._NODE_TYPE_NORMAL);

                    break;

                case TinyProtocol._NODE_TYPE_HONEST_MINER:

                    TinyProtocolHonestMiner pHonest =
                            (TinyProtocolHonestMiner) node.getProtocol(Parameters.getIstance()._PROTOCOL_2_ID);

                    node.setCurrentProtocolId(Parameters.getIstance()._PROTOCOL_2_ID);

                    pHonest.init(
                            Parameters.getIstance()._PROTOCOL_2_ID,
                            Parameters.getIstance()._PROTOCOL_TRANSPORT_ID,
                            node.getID(),
                            TinyProtocol._NODE_TYPE_HONEST_MINER);

                    break;


                case TinyProtocol._NODE_TYPE_SELFISH_MINER:

                    TinyProtocolSelfishMiner pSelfish =
                            (TinyProtocolSelfishMiner) node.getProtocol(Parameters.getIstance()._PROTOCOL_3_ID);

                    node.setCurrentProtocolId(Parameters.getIstance()._PROTOCOL_3_ID);

                    pSelfish.init(
                            Parameters.getIstance()._PROTOCOL_3_ID,
                            Parameters.getIstance()._PROTOCOL_TRANSPORT_ID,
                            node.getID(),
                            TinyProtocol._NODE_TYPE_SELFISH_MINER);

                    break;
            }

            nodesLabels.append("[ID: ").append(node.getID()).append("][ProtocolID: ").append(node.getCurrentProtocolId())
                    .append("][Type: ").append(nodeType).append("]").append("[MinerType: ");

            //if node is a Miner (Honest or Selfish)
            if (node.getCurrentProtocolId() == Parameters.getIstance()._PROTOCOL_2_ID ||
                    node.getCurrentProtocolId() == Parameters.getIstance()._PROTOCOL_3_ID) {

                TinyProtocolBaseMiner minerProtocol =
                        (TinyProtocolBaseMiner) node.getProtocol(node.getCurrentProtocolId());

                //choose miner type
                String minerType = Parameters.getIstance().rwcMinerType.next();

                switch (minerType) {

                    case TinyProtocolBaseMiner._COMPUTATIONAL_TYPE_CPU:

                        minerProtocol.setComputationalType(TinyProtocolBaseMiner._COMPUTATIONAL_TYPE_CPU);
                        Parameters.getIstance().cpuNodes.add(node);
                        break;

                    case TinyProtocolBaseMiner._COMPUTATIONAL_TYPE_GPU:

                        minerProtocol.setComputationalType(TinyProtocolBaseMiner._COMPUTATIONAL_TYPE_GPU);
                        Parameters.getIstance().gpuNodes.add(node);
                        break;

                    case TinyProtocolBaseMiner._COMPUTATIONAL_TYPE_FPGA:

                        minerProtocol.setComputationalType(TinyProtocolBaseMiner._COMPUTATIONAL_TYPE_FPGA);
                        Parameters.getIstance().fpgaNodes.add(node);
                        break;

                    case TinyProtocolBaseMiner._COMPUTATIONAL_TYPE_ASIC:

                        minerProtocol.setComputationalType(TinyProtocolBaseMiner._COMPUTATIONAL_TYPE_ASIC);
                        Parameters.getIstance().asicNodes.add(node);
                        break;
                }

                nodesLabels.append(minerType);
            } else {

                nodesLabels.append("NS");
            }

            nodesLabels.append("]\n");
        }

        log.info("{}", nodesLabels);
        log.info("- Initialization End -");

        return false;
    }

    private static void initializeRandomWeightedCollections() {

        //weighted collection for Node Type
        RandomWeightedCollection<String> rwcNodeType = new RandomWeightedCollection<>(CommonState.r);

        rwcNodeType.add(
                Parameters.getIstance()._PERCENTAGE_HONEST_MINERS * 100,
                TinyProtocolBaseMiner._NODE_TYPE_HONEST_MINER);

        rwcNodeType.add(
                Parameters.getIstance()._PERCENTAGE_SELFISH_MINERS * 100,
                TinyProtocolBaseMiner._NODE_TYPE_SELFISH_MINER);

        rwcNodeType.add(
                Parameters.getIstance()._PERCENTAGE_NORMAL_NODES * 100,
                TinyProtocolBaseMiner._NODE_TYPE_NORMAL);

        Parameters.getIstance().rwcNodeType = rwcNodeType;

        //weighted collection for Miner Hardware Type
        RandomWeightedCollection<String> rwcMinerType = new RandomWeightedCollection<>(CommonState.r);

        rwcMinerType.add(
                Parameters.getIstance()._PERCENTAGE_CPU * 100,
                TinyProtocolBaseMiner._COMPUTATIONAL_TYPE_CPU);

        rwcMinerType.add(
                Parameters.getIstance()._PERCENTAGE_GPU * 100,
                TinyProtocolBaseMiner._COMPUTATIONAL_TYPE_GPU);

        rwcMinerType.add(
                Parameters.getIstance()._PERCENTAGE_FPGA * 100,
                TinyProtocolBaseMiner._COMPUTATIONAL_TYPE_FPGA);

        rwcMinerType.add(
                Parameters.getIstance()._PERCENTAGE_ASIC * 100,
                TinyProtocolBaseMiner._COMPUTATIONAL_TYPE_ASIC);

        Parameters.getIstance().rwcMinerType = rwcMinerType;

        //weighted collection for Miner Hardware Power
        RandomWeightedCollection<String> rwcMinerTypePower = new RandomWeightedCollection<>(CommonState.r);

        rwcMinerTypePower.add(
                Parameters.getIstance().get_POWER_CPU() * 100,
                TinyProtocolBaseMiner._POWER_CPU);

        rwcMinerTypePower.add(
                Parameters.getIstance().get_POWER_GPU() * 100,
                TinyProtocolBaseMiner._POWER_GPU);

        rwcMinerTypePower.add(
                Parameters.getIstance().get_POWER_FPGA() * 100,
                TinyProtocolBaseMiner._POWER_FPGA);

        rwcMinerTypePower.add(
                Parameters.getIstance().get_POWER_ASIC() * 100,
                TinyProtocolBaseMiner._POWER_ASIC);

        Parameters.getIstance().rwcMinerTypePower = rwcMinerTypePower;
    }
}