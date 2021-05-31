#!/bin/bash

export PATH=${PWD}/../bin:$PATH
export FABRIC_CFG_PATH=${PWD}/configtx
export VERBOSE=false

. network/utils.sh

#  Called with down opiton 
function clearContainers() {
    infoln "Removing remaining containers"
    docker rm -f $(docker ps -aq --filter label=sevice=hyperlegher-fabric) 2>/dev/null || true
    docker rm -f $(docker ps -aq --filter name='dev-peer*') 2>/dev/null || true
}

#  Called with down opiton 
function removeUnwantedImages() {
    infoln "Removing generated chaincode docker images"
    docker image rm -f $(docker image -aq --filter )
}

function networkUp(){
    infoln "NetworkUp"
}

function networkDown(){
    infoln "NetworkDown"
}

function deployCC(){
    infoln "Deploy chain code"
}

## Parse mode
if [[ $# -lt 1 ]] ; then
  printHelp
  exit 0
else
  MODE=$1
  shift
fi

# parse a createChannel subcommand if used
if [[ $# -ge 1 ]] ; then
    key="$1"
    if [[ "$key" == "createChannel" ]]; then
        export MODE="createChannel"
        shift   
    fi
fi

CHANNEL_NAME="MyChannel"
# parse flags
while [[ $# -ge 1 ]] ; do 
    key="$1"
    case $key in
    -h )
        printHelp $MODE
        exit 0
        ;;
    -c )
        CHANNEL_NAME="$2"
        infoln "Using chanel name = $CHANNEL_NAME"
        shift
        ;;
    -ca ) 
        CRYPTO="Certificate Authorities"
        infoln "Using cerfificate AUthorities"
        ;;
    -r ) 
        MAX_RETRY="$2"
        infoln "Using max restry = $MAX_RETRY"
        shift
        ;;
    -d )
        CLI_DELAY="$2"
        infoln "Using CLI delay = $CLI_DELAY"
        shift
        ;;
    -s )
        DATABASE="$2"
        infoln "Using database = $DATABASE"
        shift
        ;;
    -ccl )
        CC_SRC_LANGUAGE="$2"
        infoln "Using cc src language = $CC_SRC_LANGUAGE"
        shift
        ;;
    -ccn )
        CC_NAME="$2"
        infoln "Using using cc name = $CC_NAME"
        shift
        ;;
    -ccv )
        CC_VERSION="$2"
        infoln "Using cc version = $CC_VERSION"
        shift
        ;;
    -ccs )
        CC_SEQUENCE="$2"
        infoln "Using cc sequence = $CC_SEQUENCE"
        shift
        ;;
    -ccp )
        CC_SCR_PATH="$2"
        infoln "Using cc src path = $CC_SCR_PATH"
        shift
        ;;
    -ccep )
        CC_END_POLICY="$2"
        infoln "Using cc end policy = $CC_END_POLICY"
        shift
        ;;
    -cccg )
        CC_COLL_CONFIG="$2"
        infoln "Using cc coll config = $CC_COLL_CONFIG"
        shift
        ;;
    -cci )
        CC_INIT_FCN="$2"
        infoln "Using cc init fcn= $CC_INIT_FCN"
        shift
        ;;
    -verbose )
        VERBOSE=true
        infoln "Verbose"
        shift
        ;;
    * )
        errorln "Unknown flag: $key"
        printHelp
        exit 1
        ;;
    esac 
    shift
done

# Are we generating crypto material with this co and?
if [ ! -d "organizations/peerOrganizations" ]; then 
    CRYPTO_MODE="with crypto from '${CRPYTO}'"
else 
    CRYPTO_MODE=""
fi

# Determine mode of operation and printing out what we asked for 
if [ "$MODE" == "up" ]; then
    infoln "Starting nodes with CLI timeout of '${MAX_RETRY}' tries and CLI delay of '${CLI_DELAY}' seconds and using database '${DATABASE}' ${CRYPTO_MODE}"
elif [ "$MODE" == "createChannel" ]; then 
    infoln "Creating channel '${CHANNEL_NAME}'."
    infoln "If network is not up, starting nodes with CLI timeout of '${MAX_RETRY}' tries and CLI delay of '${CLI_DELAY}' seconds adn using database '${DATABASE} ${CRYPTO_MODE}"
elif [ "$MODE" == "down"]; then 
    infoln "Stopping network"
elif [ "$MODE" == "restart"]; then 
    infoln "Restarting network" 
elif [ "$MODE" == "deployCC"]; then 
    infoln "deploying chaincode on channel '${CHANNEL_NAME}'"
else 
    printHelp
    exit 1
fi

if [ "$MODE" == "up" ]; then 
    networkUp
elif [ "$MODE" == "createChannel" ]; then 
    createChannel
elif ["$MODE" == "deployCC" ]; then 
    networkDown
else 
    printHelp 
    exit 1
fi