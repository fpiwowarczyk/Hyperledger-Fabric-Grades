# Chaincode 
To run network
``` Bash
# Cleanup if some old containers and other are left
./network.sh down
# Run netwok 
./network.sh up
./network.sh createChannel
# OR 
./network.sh up createChannel
```


To deploy chaincode in java
``` Bash
cd ../network
# Deploy ChainCode to peers from chaincode-java
./network.sh deployCC -ccn basic -ccp ../asset-transfer-basic/chaincode-java -ccl java
```

# Interacting with network
To interact with network as Org1
``` Bash
cd ../network
export PATH=${PWD}/../bin:$PATH
export FABRIC_CFG_PATH=$PWD/../config/

# Environment variables for Org1

export CORE_PEER_TLS_ENABLED=true
export CORE_PEER_LOCALMSPID="Org1MSP"
export CORE_PEER_TLS_ROOTCERT_FILE=${PWD}/organizations/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt
export CORE_PEER_MSPCONFIGPATH=${PWD}/organizations/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp
export CORE_PEER_ADDRESS=localhost:7051

#Or run script in network file 
. ./asOrg1.sh
```