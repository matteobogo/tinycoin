# TinyCoin: Simulating fraudolent mining strategies in a simplied Bitcoin Network.

# Introduction
We hereby present an implementation of a simplified version of the Bitcoin technology called TinyCoin. In this work the Bitcoin standard mining process is evaluated in comparison to a fraudulent version named Selfish Mining. The simulation has been made through the Peersim Simulator (<http://peersim.sourceforge.net/>).

# Configuration
The default configuration file can be found in `resources/tinycoin.conf`.\
A custom configuration can be made by tweaking the following parameters.

- SEED: the seed for generating the random number sequence.
- SIZE: the number of nodes in the network.
- CYCLES: the number of simulation cycles between two mined blocks.
- SIMULATION_END: the total number of simulation cycles.
- K_FACTOR: the degree of the nodes.
- BLOCK_DELAY: the base latency delay for a block.
- TRANS_DELAY: the base latency delay for a transaction.
- TRANS_PROBABILITY_THRESHOLD: the probability threshold for a transaction.
- TRANS_EXTRACTION: the number of simulation cycles before trying a transaction.
- MAX_TRANS_PER_BLOCK: the maximum number of transactions per block. (Affects Blocks propagation)
- MIN_TRANS_PER_BLOCK: the minimum number of transaction per block.
- SELFISH_POOL_ACTIVE: the number of selfish pools active.
- SELFISH_POOL_POWER: the computation power (%) of the selfish pool.
- SELFISH_MINERS: % of selfish miners nodes. (0 if at least a selfish pool is active)
- HONEST_MINERS: % of honest miners nodes.
- NORMAL_NODES: % of normal miners nodes.
- CPU: % of nodes with CPU power.
- GPU: % of nodes with GPU power.
- FPGA: % of nodes with FPGA power.
- ASIC: % of nodes with ASIC power.
- POWER_CPU: % of power for CPU.
- POWER_GPU: % of power for GPU.
- POWER_FPGA: % of power for FPGA.
- POWER_ASIC: % of power for ASIC.

Blocks propagation = BLOCK_DELAY * MAX_TRANS_PER_BLOCK

# Usage

## Docker

`docker build -t tinycoin .` \
`docker run --rm tinycoin`

### Activate debugging
`docker run --rm -e DEBUG=true tinycoin`

### Custom configuration
`docker run --rm tinycoin /path/to/config`