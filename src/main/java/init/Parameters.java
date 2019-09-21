package init;

import entities.TinyCoinNode;
import lombok.AccessLevel;
import lombok.Getter;
import peersim.core.CommonState;
import utilities.RandomWeightedCollection;
import utilities.Utils;

import java.util.ArrayList;
import java.util.List;

@Getter(AccessLevel.PUBLIC)
public class Parameters {

    private static Parameters istance = new Parameters();

    double _TINYCOIN_MIN_VALUE, _TINYCOIN_MAX_VALUE;
    int _MAX_DECIMAL_SATOSHI;
    double _INITIAL_WALLET_MIN, _INITIAL_WALLET_MAX;
    int _MIN_TRANSACTION_CONFIRMS;
    double _TRANSACTION_PROBABILITY_THRESHOLD;
    int _MAX_BLOCK_TRANSACTIONS, _MIN_BLOCK_TRANSACTIONS;
    double _BLOCK_REWARD, _TRANSACTION_REWARD;

    int _PROTOCOL_1_ID, _PROTOCOL_2_ID, _PROTOCOL_3_ID;
    int _PROTOCOL_TRANSPORT_ID;
    String _GENESIS_BLOCK_ID = Utils.getRandomUniqueID();
    double _PERCENTAGE_HONEST_MINERS, _PERCENTAGE_SELFISH_MINERS, _PERCENTAGE_NORMAL_NODES;
    double _PERCENTAGE_CPU, _PERCENTAGE_GPU, _PERCENTAGE_FPGA, _PERCENTAGE_ASIC;
    double _POWER_CPU, _POWER_GPU, _POWER_FPGA, _POWER_ASIC;

    int _SELFISH_POOL_ACTIVE;
    double _SELFISH_POOL_POWER;
    TinyCoinNode selfishPoolNode;

    int _TRANS_EXT_PERIOD;

    List<TinyCoinNode> cpuNodes = new ArrayList<>();
    List<TinyCoinNode> gpuNodes = new ArrayList<>();
    List<TinyCoinNode> fpgaNodes = new ArrayList<>();
    List<TinyCoinNode> asicNodes = new ArrayList<>();

    int[] miningPowers;

    RandomWeightedCollection<String> rwcNodeType;
    RandomWeightedCollection<String> rwcMinerType;
    RandomWeightedCollection<String> rwcMinerTypePower;

    private Parameters() { }

    public static Parameters getIstance() { return istance; }

    public static double getRandomBTCInRange(double min, double max) {

        double range = max - min;
        return Math.floor(((CommonState.r.nextDouble() * range) + min) *
                Parameters.getIstance()._MAX_DECIMAL_SATOSHI) / Parameters.getIstance()._MAX_DECIMAL_SATOSHI;
    }
}