# Simple network

Repository for system for managing students grades based on blockchain private
network on Hyperledger Fabric.

This is prototype of network without any additional mechanism, 
it is used to compare with another network with consent mechanism. 

Look into parallel to this project folder in repo to see network with 
consent mechanism 

## How to run
It is based on test-network from hyperledger https://hyperledger-fabric.readthedocs.io/en/release-2.2/test_network.html.
If you want to run this app, do what they order to run test network to have all dependecies.

After that
```Bash
# Run network
cd test-network 
./network.sh run createChannel -c mychannel -ca
./network.sh deployCC -ccn grades -ccp ../chaincode-java -ccl java 
# Important to be in test-network dir. Now you pretend to be org1
export PATH=${PWD}/../bin:$PATH
export FABRIC_CFG_PATH=$PWD/../config/
export CORE_PEER_TLS_ENABLED=true
export CORE_PEER_LOCALMSPID="Org1MSP"
export CORE_PEER_TLS_ROOTCERT_FILE=${PWD}/organizations/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt
export CORE_PEER_MSPCONFIGPATH=${PWD}/organizations/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp
export CORE_PEER_ADDRESS=localhost:7051

#Add some initial grades into chain
peer chaincode invoke -o localhost:7050
--ordererTLSHostnameOverride orderer.example.com
--tls --cafile ${PWD}/organizations/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem 
-C mychannel -n grades --peerAddresses localhost:7051
--tlsRootCertFiles ${PWD}/organizations/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt 
--peerAddresses localhost:9051 --tlsRootCertFiles ${PWD}/organizations/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt 
-c '{"function":"initGrades","Args":[]}'
```
Now you can go to `localhost:8080/swagger-ui.html` and test endpoint that interact with blockchain network.
To make some calls first you need to add wallet for organization, it is 
set of users for organization. After that add some user and log as him.
Then you will be able to query blockchain.

## Endpoints 

GET /grades/{gradeId} - get one grade with Id \
PUT /grades/{gradeId} - update grade with Id \
DELETE /grades/{gradeId} - delete grade with id \
GET /grades - get all grades in network \
POST /grades - add grade to network \
GET /student - get grades for student name and surname \
POST /addUser - add user for organization \
GET /logIn - log as user \
GET /addWalet - add walet for organization with admin \