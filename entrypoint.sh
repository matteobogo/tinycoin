#!/bin/bash

DEBUG_FLAG=""
if [ "${DEBUG^^}" = "TRUE" ]; then
    echo "DEBUG mode activated."
    DEBUG_FLAG=-Dorg.slf4j.simpleLogger.defaultLogLevel=DEBUG
fi

java $DEBUG_FLAG -cp /tinycoin/target/tinycoin.jar peersim.Simulator $1