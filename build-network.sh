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

function createOrgs(){
    if [ -d "organizations/peerOrganizations" ]; then 
        rm -Rf organizations/peerOrganizations && rm -Rf organizations/ordererOrganizations
    fi

    if [ "$CRYPTO" == "cryptogen"]; then 
        which cryptogen
        if ["$?" -ne 0]; then
            fatalln "cryptogen tool not found. exiting"
        fi 
        infoln "Generating certificates using cryptogen tool"

        infoln "Creating Org1 Identites"

        set -x
        cryptogen generate --config=./organizations/cryptogen/crypto-config-org1.yaml --output="organizations"
        res=$?
        { set +x; } 2>/dev/null
        if [ $res -ne 0]; then 
            fatalln "Failed to generate certificates..."
        fi

        infoln "Creating Org2 Identities"

        set -x
        cryptogen generate --config=./organizations/cryptogen/crypto-config-org2.yaml --output="organizations"
        res=$?
        { set +x; } 2>/dev/null
        if [ $res -ne 0]; then 
            fatalln "Failed to generate certificates..."
        fi 


        infoln "Creating Ordered Org Identites"

        set -x 
        cryptogen generate --config=./organizations/cryptogen/crypto-config-orderer.yaml --output="organizations"
        res=$?
        { set +x; } 2>/dev/null
        if [ $res -ne 0 ]; then 
            fatalln "Failed to generate certificates..."
        fi
    fi

    #Create crrypto material using Fabric CA

    if [ "$CRYPTO" == "Certificate Authorites" ]; then 
        infoln "Generating certificates using Fabric CA"
        docker-compose -f $COMPOSE_FILE_CA up -d 2>&1

        . organizations/fabric-ca/registerEnroll.sh

    while : 
        do
            if [ ! -f "organizations/fabric-ca/org1/tls-cert.pem" ]; then
                sleep 1
            else 
                break
            fi 
        done

        infoln "Creating Org1 Identites"

        createOrg1

        infoln "Creating Org2 Identities"
        
        createOrg2

        infoln "Creating Orgerer Org Identities"

        createOrderer

    fi

    infoln "Generating CCP files for Org1 and Org2"
    ./organizations/ccp-generate.sh
        
}

function networkUp(){
    if [ ! -d "organizations/peerOrganizations" ]; then 
        createOrgs
    fi 

    COMPOSE_FILES="-f ${COMPOSE_FILE_BASE}"

    if [ "${DATABASE}" == "couchdb" ]; then 
        COMPOSE_FILES="${COMPOSE_FIES} -f ${COMPOSE_FILE_COUCH}"
    fi

    docker-compose ${COMPOSE_FILES} up -d 2>&1

    docker ps -a 
    if [ $? -ne 0]; then 
        fatalln "Unable to start network"
    fi
}

function createChannel() {
    if [ ! -d "organizations/peerOrganizations" ]; then 
        infoln "Bringing network up"
        networkUp
    fi

    scripts/createChannel.sh $CHANNEL_NAME $CLI_DELAY $MAX_RETRY $VERBOSE
}

function deployCC(){
    scripts/deployCC.sh $CHANNEL_NAME $CC_NAME $CC_SCR_PATH $CC_SRC_LANGUAGE $CC_VERSION $CC_SEQUENCE $CC_INIT_FCN $CC_END_POLICY $CC_COLL_CONFIG $CLI_DELAY $MAX_RETRY $VERBOSE

    if [ $? -ne 0 ]; then
        fatalln "Deploying chaincode failes"
    fi 
}


function networkDown(){
    docker-compose -f $COMPOSE_FILE_BASE -f $COMPOSE_FILE_COUCH -f $COMPOSE_FILE_CA down --volumes --remove-orphans
    docker-compose -f $COMPOSE_FILE_COUCH_ORG3 -f $COMPOSE_FILE_ORG3 down --volumes --remove-orphans

  if [ "$MODE" != "restart" ]; then
    # Bring down the network, deleting the volumes
    #Cleanup the chaincode containers
    clearContainers
    #Cleanup images
    removeUnwantedImages
    # remove orderer block and other channel configuration transactions and certs
    docker run --rm -v "$(pwd):/data" busybox sh -c 'cd /data && rm -rf system-genesis-block/*.block organizations/peerOrganizations organizations/ordererOrganizations'
    ## remove fabric ca artifacts
    docker run --rm -v "$(pwd):/data" busybox sh -c 'cd /data && rm -rf organizations/fabric-ca/org1/msp organizations/fabric-ca/org1/tls-cert.pem organizations/fabric-ca/org1/ca-cert.pem organizations/fabric-ca/org1/IssuerPublicKey organizations/fabric-ca/org1/IssuerRevocationPublicKey organizations/fabric-ca/org1/fabric-ca-server.db'
    docker run --rm -v "$(pwd):/data" busybox sh -c 'cd /data && rm -rf organizations/fabric-ca/org2/msp organizations/fabric-ca/org2/tls-cert.pem organizations/fabric-ca/org2/ca-cert.pem organizations/fabric-ca/org2/IssuerPublicKey organizations/fabric-ca/org2/IssuerRevocationPublicKey organizations/fabric-ca/org2/fabric-ca-server.db'
    docker run --rm -v "$(pwd):/data" busybox sh -c 'cd /data && rm -rf organizations/fabric-ca/ordererOrg/msp organizations/fabric-ca/ordererOrg/tls-cert.pem organizations/fabric-ca/ordererOrg/ca-cert.pem organizations/fabric-ca/ordererOrg/IssuerPublicKey organizations/fabric-ca/ordererOrg/IssuerRevocationPublicKey organizations/fabric-ca/ordererOrg/fabric-ca-server.db'
    docker run --rm -v "$(pwd):/data" busybox sh -c 'cd /data && rm -rf addOrg3/fabric-ca/org3/msp addOrg3/fabric-ca/org3/tls-cert.pem addOrg3/fabric-ca/org3/ca-cert.pem addOrg3/fabric-ca/org3/IssuerPublicKey addOrg3/fabric-ca/org3/IssuerRevocationPublicKey addOrg3/fabric-ca/org3/fabric-ca-server.db'
    # remove channel and script artifacts
    docker run --rm -v "$(pwd):/data" busybox sh -c 'cd /data && rm -rf channel-artifacts log.txt *.tar.gz'
  fi
}

CRYPTO="cryptogen"
MAX_RETRY=5
CLI_DELAY=3
CHANNEL_NAME="myChannel"
CC_NAME="NA"
CC_SCR_PATH="NA"
CC_END_POLICY="NA"
CC_COLL_CONFIG="NA"
CC_INIT_FCN="NA"
COMPOSE_FILE_BASE=docker/docker-compose-test-net.yaml
COMPOSE_FILE_COUCH=docker/docker-compose-couch.yaml
COMPOSE_FILE_CA=docker/docker-compose-ca.yaml
COMPOSE_FILE_COUCH_ORG3=addOrg3/docker/docker-compose-couch-org3.yaml
COMPOSE_FILE_ORG3=addOrg3/docker/docker-compose-org3.yams

CC_SCR_LANGUAGE="NA"
CC_VERSION="1.0"
CC_SEQUENCE=1
DATABASE="leveldb"


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