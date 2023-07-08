#!/bin/bash

BASE_DIR="$(dirname "$(realpath "$0")")/files"
echo "Base Dir = ${BASE_DIR}"
OUT_DIR="${BASE_DIR}/out"
CC_NAME="mycc"
CC_VER="1.0"
CH_NAME="ch1"
PKG_ID="${CC_NAME}:${CC_VER}"
export FABRIC_CFG_PATH="${OUT_DIR}"

####

function printInfo() {
  "${BASE_DIR}"/bin/configtxgen --version
}

function runInPeer() {
  local SCRIPT=$1

  docker exec -it hlf-peer sh -c "${SCRIPT}"
}

function stop() {
  docker compose down

  rm -rf "${OUT_DIR}"
}

function start() {
  ########
  ## INIT
  mkdir -p "${OUT_DIR}"
  cp -r "${BASE_DIR}"/config/{configtx.yaml,msp} "${OUT_DIR}"

  "${BASE_DIR}"/bin/configtxgen \
    -profile SampleDevModeSolo \
    -channelID syschannel \
    -outputBlock genesisblock \
    -configPath "${FABRIC_CFG_PATH}" \
    -outputBlock "${FABRIC_CFG_PATH}/genesisblock"

  docker compose up -d
  sleep 5

  ##################
  ## CREATE CHANNEL

  "${BASE_DIR}"/bin/configtxgen \
    -channelID ${CH_NAME} \
    -outputCreateChannelTx "${OUT_DIR}/${CH_NAME}.tx" \
    -profile SampleSingleMSPChannel \
    -configPath "${FABRIC_CFG_PATH}"

  runInPeer "
  echo '---> channel create'
  peer channel create \
    -o hlf-orderer:7050 \
    -c ${CH_NAME} \
    -f /var/hlf/${CH_NAME}.tx \
    --outputBlock ${CH_NAME}.block

  echo '---> channel join'
  peer channel join \
    -b ${CH_NAME}.block
  "

  #############
  ## CHAINCODE
  runInPeer "
  echo '---> lifecycle chaincode approveformyorg'
  peer lifecycle chaincode approveformyorg \
    -o hlf-orderer:7050 \
    --channelID ${CH_NAME} \
    --name ${CC_NAME} \
    --version ${CC_VER} \
    --sequence 1 \
    --signature-policy \"OR ('SampleOrg.member')\" \
    --package-id ${PKG_ID}

  echo '---> lifecycle chaincode checkcommitreadiness'
  peer lifecycle chaincode checkcommitreadiness \
    -o hlf-orderer:7050 \
    --channelID ${CH_NAME} \
    --name ${CC_NAME} \
    --version ${CC_VER} \
    --sequence 1 \
    --signature-policy \"OR ('SampleOrg.member')\"

  echo '---> lifecycle chaincode commit'
  peer lifecycle chaincode commit \
    -o hlf-orderer:7050 \
    --channelID ${CH_NAME} \
    --name ${CC_NAME} \
    --version ${CC_VER} \
    --sequence 1 \
    --signature-policy \"OR ('SampleOrg.member')\" \
    --peerAddresses 127.0.0.1:7051
  "
}

function callCC() {
  TYPE=${1:-q}
  FUNC=${2:-getTime}
  ARGS=${3:-""}

  if [ "${TYPE}" == "i" ]; then
    echo "--- INVOKE: f=${FUNC} ---"
    runInPeer "
    peer chaincode invoke \
      -o hlf-orderer:7050 \
      --channelID ${CH_NAME} \
      --name ${CC_NAME} \
      --peerAddresses 127.0.0.1:7051 \
      -c \"{'function':'${FUNC}','Args':[${ARGS}]}\"
    "
  else
    echo "--- QUERY: f=${FUNC} ---"
    runInPeer "
    peer chaincode query \
      -o hlf-orderer:7050 \
      --channelID ${CH_NAME} \
      --name ${CC_NAME} \
      --peerAddresses 127.0.0.1:7051 \
      -c \"{\\\"function\\\":\\\"${FUNC}\\\",\\\"Args\\\":[${ARGS}]}\"
    "
  fi
}

####

case $1 in
  'start')
    start
  ;;
  'stop')
    stop
  ;;
  'call')
    callCC "$2" "$3" "$4"
  ;;
  'info')
    printInfo
  ;;
  *)
    echo "ERROR: $0 start|stop|call|info"
  ;;
esac