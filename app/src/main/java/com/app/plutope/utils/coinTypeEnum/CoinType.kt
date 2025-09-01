package com.app.plutope.utils.coinTypeEnum

enum class CoinType(private val value: Int) {
    AETERNITY(457), AION(425), BINANCE(714), BITCOIN(0), BITCOINCASH(145), BITCOINGOLD(156), CALLISTO(
        820
    ),
    CARDANO(1815), COSMOS(118), DASH(5), DECRED(42), DIGIBYTE(20), DOGECOIN(3), EOS(194), WAX(14001), ETHEREUM(
        60
    ),
    ETHEREUMCLASSIC(61), FIO(235), GOCHAIN(6060), GROESTLCOIN(17), ICON(74), IOTEX(304), KAVA(459), KIN(
        2017
    ),
    LITECOIN(2), MONACOIN(22), NEBULAS(2718), NULS(8964), NANO(165), NEAR(397), NIMIQ(242), ONTOLOGY(
        1024
    ),
    POANETWORK(178), QTUM(2301), XRP(144), SOLANA(501), STELLAR(148), TEZOS(1729), THETA(500), THUNDERCORE(
        1001
    ),
    NEO(888), TOMOCHAIN(889), TRON(195), VECHAIN(818), VIACOIN(14), WANCHAIN(5718350), ZCASH(133), FIRO(
        136
    ),
    ZILLIQA(313), ZELCASH(19167), RAVENCOIN(175), WAVES(5741564), TERRA(330), TERRAV2(10000330), HARMONY(
        1023
    ),
    ALGORAND(283), KUSAMA(434), POLKADOT(354), FILECOIN(461), MULTIVERSX(508), BANDCHAIN(494), SMARTCHAINLEGACY(
        10000714
    ),
    SMARTCHAIN(20000714), OASIS(474), POLYGON(966), THORCHAIN(931), BLUZELLE(483), OPTIMISM(10000070), ZKSYNC(
        10000324
    ),
    ARBITRUM(10042221), ECOCHAIN(10000553), AVALANCHECCHAIN(10009000), XDAI(10000100), FANTOM(
        10000250
    ),
    CRYPTOORG(394), CELO(52752), RONIN(10002020), OSMOSIS(10000118), ECASH(899), CRONOSCHAIN(
        10000025
    ),
    SMARTBITCOINCASH(10000145), KUCOINCOMMUNITYCHAIN(10000321), BOBA(10000288), METIS(1001088), AURORA(
        1323161554
    ),
    EVMOS(10009001), NATIVEEVMOS(20009001), MOONRIVER(10001285), MOONBEAM(10001284), KAVAEVM(
        10002222
    ),
    KLAYTN(10008217), METER(18000), OKXCHAIN(996), NERVOS(309), EVERSCALE(396), APTOS(637), HEDERA(
        3030
    ),
    SECRET(529), NATIVEINJECTIVE(10000060), AGORIC(564), TON(607), SUI(784), STARGAZE(20000118), POLYGONZKEVM(
        10001101
    ),
    JUNO(30000118), STRIDE(40000118), AXELAR(50000118), CRESCENT(60000118), KUJIRA(70000118), IOTEXEVM(
        10004689
    ),
    NATIVECANTO(10007700), COMDEX(80000118), NEUTRON(90000118), SOMMELIER(11000118), FETCHAI(
        12000118
    ),
    MARS(13000118), UMEE(14000118), COREUM(10000990), QUASAR(15000118), PERSISTENCE(16000118), AKASH(
        17000118
    ),
    NOBLE(18000118), BASEMAINNET(999999);

    fun value(): Int {
        return value
    }


    companion object {
        fun createFromValue(value: Int): CoinType? {
            return when (value) {
                457 -> AETERNITY
                425 -> AION
                714 -> BINANCE
                0 -> BITCOIN
                145 -> BITCOINCASH
                156 -> BITCOINGOLD
                820 -> CALLISTO
                1815 -> CARDANO
                118 -> COSMOS
                5 -> DASH
                42 -> DECRED
                20 -> DIGIBYTE
                3 -> DOGECOIN
                194 -> EOS
                14001 -> WAX
                60 -> ETHEREUM
                61 -> ETHEREUMCLASSIC
                235 -> FIO
                6060 -> GOCHAIN
                17 -> GROESTLCOIN
                74 -> ICON
                304 -> IOTEX
                459 -> KAVA
                2017 -> KIN
                2 -> LITECOIN
                22 -> MONACOIN
                2718 -> NEBULAS
                8964 -> NULS
                165 -> NANO
                397 -> NEAR
                242 -> NIMIQ
                1024 -> ONTOLOGY
                178 -> POANETWORK
                2301 -> QTUM
                144 -> XRP
                501 -> SOLANA
                148 -> STELLAR
                1729 -> TEZOS
                500 -> THETA
                1001 -> THUNDERCORE
                888 -> NEO
                889 -> TOMOCHAIN
                195 -> TRON
                818 -> VECHAIN
                14 -> VIACOIN
                5718350 -> WANCHAIN
                133 -> ZCASH
                136 -> FIRO
                313 -> ZILLIQA
                19167 -> ZELCASH
                175 -> RAVENCOIN
                5741564 -> WAVES
                330 -> TERRA
                10000330 -> TERRAV2
                1023 -> HARMONY
                283 -> ALGORAND
                434 -> KUSAMA
                354 -> POLKADOT
                461 -> FILECOIN
                508 -> MULTIVERSX
                494 -> BANDCHAIN
                10000714 -> SMARTCHAINLEGACY
                20000714 -> SMARTCHAIN
                474 -> OASIS
                966 -> POLYGON
                931 -> THORCHAIN
                483 -> BLUZELLE
                10000070 -> OPTIMISM
                10000324 -> ZKSYNC
                10042221 -> ARBITRUM
                10000553 -> ECOCHAIN
                10009000 -> AVALANCHECCHAIN
                10000100 -> XDAI
                10000250 -> FANTOM
                394 -> CRYPTOORG
                52752 -> CELO
                10002020 -> RONIN
                10000118 -> OSMOSIS
                899 -> ECASH
                10000025 -> CRONOSCHAIN
                10000145 -> SMARTBITCOINCASH
                10000321 -> KUCOINCOMMUNITYCHAIN
                10000288 -> BOBA
                1001088 -> METIS
                1323161554 -> AURORA
                10009001 -> EVMOS
                20009001 -> NATIVEEVMOS
                10001285 -> MOONRIVER
                10001284 -> MOONBEAM
                10002222 -> KAVAEVM
                10008217 -> KLAYTN
                18000 -> METER
                996 -> OKXCHAIN
                309 -> NERVOS
                396 -> EVERSCALE
                637 -> APTOS
                3030 -> HEDERA
                529 -> SECRET
                10000060 -> NATIVEINJECTIVE
                564 -> AGORIC
                607 -> TON
                784 -> SUI
                20000118 -> STARGAZE
                10001101 -> POLYGONZKEVM
                30000118 -> JUNO
                40000118 -> STRIDE
                50000118 -> AXELAR
                60000118 -> CRESCENT
                70000118 -> KUJIRA
                10004689 -> IOTEXEVM
                10007700 -> NATIVECANTO
                80000118 -> COMDEX
                90000118 -> NEUTRON
                11000118 -> SOMMELIER
                12000118 -> FETCHAI
                13000118 -> MARS
                14000118 -> UMEE
                10000990 -> COREUM
                15000118 -> QUASAR
                16000118 -> PERSISTENCE
                17000118 -> AKASH
                18000118 -> NOBLE
                999999 -> BASEMAINNET
                else -> null
            }
        }
    }
}
