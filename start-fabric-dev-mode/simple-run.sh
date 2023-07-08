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
  echo ""
  "${BASE_DIR}"/bin/orderer version
  "${BASE_DIR}"/bin/peer version
}

function stop() {
  rm -rf "${BASE_DIR}"/logs
  rm -rf "${OUT_DIR}"

  for f in "${BASE_DIR}"/*.pid
  do
    if [ -f "${f}" ]; then
      echo "PID FILE: ${f} - $(cat "${f}")"
      kill -9 "$(cat "${f}")"
      rm -f "${f}"
    fi
  done
}

function start() {
  ########
  ## INIT
  mkdir -p "${OUT_DIR}" "${BASE_DIR}"/logs
  cp -r "${BASE_DIR}"/config/{configtx.yaml,msp} "${OUT_DIR}"

  OUT_DIR_PARAM="$(echo "${OUT_DIR}" | sed 's/\//\\\//g')"
  sed "s/_BASE_DIR_/${OUT_DIR_PARAM}/g" "${BASE_DIR}"/config/core.yaml > "${OUT_DIR}"/core.yaml
  sed "s/_BASE_DIR_/${OUT_DIR_PARAM}/g" "${BASE_DIR}"/config/orderer.yaml > "${OUT_DIR}"/orderer.yaml

  "${BASE_DIR}"/bin/configtxgen \
    -profile SampleDevModeSolo \
    -channelID syschannel \
    -outputBlock genesisblock \
    -configPath "${FABRIC_CFG_PATH}" \
    -outputBlock "${FABRIC_CFG_PATH}/genesisblock"

  #################
  ## START ORDERER
  export ORDERER_GENERAL_GENESISPROFILE=SampleDevModeSolo

  "${BASE_DIR}"/bin/orderer > "${BASE_DIR}"/logs/orderer.log 2>&1 &
  if [ "$?" != "0" ]; then
    echo "Orderer Error"
    exit 1
  fi
  echo $! > "${BASE_DIR}"/orderer.pid

  echo "----- Orderer Started"

  sleep 5

  ##############
  ## START PEER
  export FABRIC_LOGGING_SPEC=chaincode=debug
  export CORE_PEER_CHAINCODELISTENADDRESS=0.0.0.0:7052

  "${BASE_DIR}"/bin/peer node start --peer-chaincodedev=true > "${BASE_DIR}"/logs/peer.log 2>&1 &
  if [ "$?" != "0" ]; then
    echo "Peer Error"
    exit 1
  fi
  echo $! > "${BASE_DIR}"/peer.pid

  echo "----- Peer Started"

  sleep 5

  ##################
  ## CREATE CHANNEL
  cd "${OUT_DIR}" || exit

  "${BASE_DIR}"/bin/configtxgen \
    -channelID ${CH_NAME} \
    -outputCreateChannelTx ${CH_NAME}.tx \
    -profile SampleSingleMSPChannel \
    -configPath "${FABRIC_CFG_PATH}"

  "${BASE_DIR}"/bin/peer channel create \
    -o 127.0.0.1:7050 \
    -c ${CH_NAME} \
    -f ${CH_NAME}.tx \
    --outputBlock ${CH_NAME}.block

  "${BASE_DIR}"/bin/peer channel join \
    -b ${CH_NAME}.block

  #############
  ## CHAINCODE
  "${BASE_DIR}"/bin/peer lifecycle chaincode approveformyorg \
    -o "127.0.0.1:7050" \
    --channelID ${CH_NAME} \
    --name ${CC_NAME} \
    --version ${CC_VER} \
    --sequence 1 \
    --signature-policy "OR ('SampleOrg.member')" \
    --package-id "${PKG_ID}"

  "${BASE_DIR}"/bin/peer lifecycle chaincode checkcommitreadiness \
    -o "127.0.0.1:7050" \
    --channelID ${CH_NAME} \
    --name ${CC_NAME} \
    --version ${CC_VER} \
    --sequence 1 \
    --signature-policy "OR ('SampleOrg.member')"

  "${BASE_DIR}"/bin/peer lifecycle chaincode commit \
    -o "127.0.0.1:7050" \
    --channelID ${CH_NAME} \
    --name ${CC_NAME} \
    --version ${CC_VER} \
    --sequence 1 \
    --signature-policy "OR ('SampleOrg.member')" \
    --peerAddresses "127.0.0.1:7051"
}

function callCC() {
  TYPE=${1:-q}
  FUNC=${2:-getTime}
  ARGS=${3:-""}

  if [ "${TYPE}" == "i" ]; then
    echo "--- INVOKE: f=${FUNC} ---"
    "${BASE_DIR}"/bin/peer chaincode invoke \
      -o "127.0.0.1:7050" \
      --channelID ${CH_NAME} \
      --name ${CC_NAME} \
      --peerAddresses "127.0.0.1:7051" \
      -c "{\"function\":\"${FUNC}\",\"Args\":[${ARGS}]}"
  else
    echo "--- QUERY: f=${FUNC} ---"
    "${BASE_DIR}"/bin/peer chaincode query \
      -o "127.0.0.1:7050" \
      --channelID ${CH_NAME} \
      --name ${CC_NAME} \
      --peerAddresses "127.0.0.1:7051" \
      -c "{\"function\":\"${FUNC}\",\"Args\":[${ARGS}]}"
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