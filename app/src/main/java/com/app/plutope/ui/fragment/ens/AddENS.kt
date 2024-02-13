package com.app.plutope.ui.fragment.ens

import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.app.plutope.BR
import com.app.plutope.R
import com.app.plutope.databinding.FragmentAddENSBinding
import com.app.plutope.dialogs.DialogConfirmationTransaction
import com.app.plutope.dialogs.SwapProgressDialog
import com.app.plutope.model.Tokens
import com.app.plutope.model.Wallet
import com.app.plutope.ui.base.BaseFragment
import com.app.plutope.utils.coinTypeEnum.CoinType
import com.app.plutope.utils.customSnackbar.CustomSnackbar
import com.app.plutope.utils.hexToMatic
import com.app.plutope.utils.hideLoader
import com.app.plutope.utils.loge
import com.app.plutope.utils.network.NetworkState
import com.app.plutope.utils.showLoader
import com.app.plutope.utils.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.kethereum.eip137.model.ENSName
import org.kethereum.ens.ENS
import org.kethereum.model.Address
import org.kethereum.model.ByteCode
import org.kethereum.model.ChainId
import org.kethereum.model.SignatureData
import org.kethereum.model.SignedTransaction
import org.kethereum.rpc.EthereumRPC
import org.kethereum.rpc.model.BlockInformation
import org.kethereum.rpc.model.FeeHistory
import org.kethereum.rpc.model.StringResultResponse
import org.komputing.khex.model.HexString
import org.web3j.ens.EnsResolver
import org.web3j.ens.EnsResolver.isValidEnsName
import org.web3j.protocol.Web3j
import org.web3j.protocol.Web3jService
import org.web3j.protocol.http.HttpService
import java.math.BigInteger
import java.math.RoundingMode
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class AddENS : BaseFragment<FragmentAddENSBinding, AddENSViewModel>() {

    private var eNSListAdapter: ENSListAdapter? = null
    private val addENSViewModel: AddENSViewModel by viewModels()
    val token = Tokens()
    private var web3jService: Web3jService? = null
    override fun getViewModel(): AddENSViewModel {
        return addENSViewModel
    }

    override fun getBindingVariable(): Int {
        return BR.addENSViewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_add_e_n_s
    }

    override fun setupToolbarText(): String {
        return "ENS"
    }

    override fun setupUI() {

        token.chain?.coinType = CoinType.POLYGON
        token.t_address = ""
        token.t_name = "Polygon"
        token.t_symbol = "MATIC"
        token.t_type = "POLYGON"
        token.tokenId = "matic-network"


        /*
                CoroutineScope(Dispatchers.Default).launch {
                   // getEns()

                    getEthENS()
                }
        */




        val ensList = arrayListOf<ENSListModel>()
        eNSListAdapter = ENSListAdapter {
            DialogConfirmationTransaction.getInstance()?.show(requireContext(), it, token) {
                sendTransaction(it)
            }


        }
        eNSListAdapter?.submitList(ensList)
        viewDataBinding!!.rvEnsList.adapter = eNSListAdapter

        viewDataBinding!!.btnSearch.setOnClickListener {
            if (viewDataBinding!!.edtSearch.text.toString().isNotEmpty()) {
                searchDomain(viewDataBinding!!.edtSearch.text.toString())
            }
        }


    }

    private suspend fun getEns() {
        val ENS_CACHE_TIME_VALIDITY = (10 * (1000 * 60)).toLong()
        val httpClientBuilder = OkHttpClient.Builder()
        httpClientBuilder.connectTimeout(
            5,
            TimeUnit.MINUTES
        )
        httpClientBuilder.readTimeout(5, TimeUnit.MINUTES)
        httpClientBuilder.writeTimeout(5, TimeUnit.MINUTES)

        val httpClient = httpClientBuilder.build()
        // val web3 = Web3j.build(HttpService(chain?.rpcURL))
        val web3 = Web3j.build(
            HttpService(
                "https://mainnet.infura.io/v3/da3717f25f824cc1baa32d812386d93f",
                httpClient
            )
        )
        val ensResolver = EnsResolver(web3, ENS_CACHE_TIME_VALIDITY)
        CoroutineScope(Dispatchers.IO).launch {

/*
             web3.ethSyncing().sendAsync().apply {
                 loge("ENS","${this.get().isSyncing}")

                 if (this.get().isSyncing){

                 }else{

                 }
                 val name= ensResolver.resolve("web3j.eth")
                 loge("ENS_name", name)


             }
*/

            ensResolver.syncThreshold = ENS_CACHE_TIME_VALIDITY
            val name = ensResolver.reverseResolve("0xD02D090F8f99B61D65d8e8876Ea86c2720aB27BC")
            loge("isValidEnsName", "${isValidEnsName("web3j.eth")} :: $name")


        }

    }

    private fun getEthENS() {
        val ethereumRPC = object : EthereumRPC {
            override fun accounts(): List<Address>? {
                val model = Address(Wallet.getPublicWalletAddress(CoinType.ETHEREUM)!!)
                val address = arrayListOf<Address>()
                address.add(model)
                return address

            }

            override fun blockNumber(): BigInteger? {
                TODO("Not yet implemented")
            }

            override fun call(
                transaction: org.kethereum.model.Transaction,
                block: String
            ): HexString? {
                TODO("Not yet implemented")
            }

            override fun chainId(): ChainId? {
                return ChainId(1)
            }

            override fun clientVersion(): String? {
                return "1"
            }

            override fun estimateGas(transaction: org.kethereum.model.Transaction): BigInteger? {
                return 0.toBigInteger()
            }

            override fun gasPrice(): BigInteger? {
                return 0.toBigInteger()
            }

            override fun getBalance(address: Address, block: String): BigInteger? {
                return 0.toBigInteger()
            }

            override fun getBlockByNumber(number: BigInteger): BlockInformation? {
                TODO("Not yet implemented")
            }

            override fun getCode(address: Address, block: String): ByteCode? {
                TODO("Not yet implemented")
            }

            override fun getFeeHistory(
                blocks: Int,
                lastBlock: String,
                percentiles: String
            ): FeeHistory? {
                TODO("Not yet implemented")
            }

            override fun getStorageAt(
                address: Address,
                position: String,
                block: String
            ): HexString? {
                TODO("Not yet implemented")
            }

            override fun getTransactionByHash(hash: String): SignedTransaction? {
                TODO("Not yet implemented")
            }

            override fun getTransactionCount(address: Address, block: String): BigInteger? {
                TODO("Not yet implemented")
            }

            override fun sendRawTransaction(data: String): String? {
                TODO("Not yet implemented")
            }

            override fun sign(address: Address, message: ByteArray): SignatureData? {
                TODO("Not yet implemented")
            }

            override fun stringCall(function: String, params: String): StringResultResponse? {
                TODO("Not yet implemented")
            }
        }

        val ens = ENS(ethereumRPC)
        val address = ens.getAddress(ENSName("web3j.eth"))

        loge("ENS_ADD", "$address")

    }


    private fun sendTransaction(it: ENSListModel) {

        val matic = hexToMatic(it.data?.tx?.arguments?.price!!)
        matic.toBigDecimal().setScale(2, RoundingMode.DOWN)
        openSwapProgressDialog(
            "Please wait till transaction completed",
            "Domain buy with price of ${
                matic.toBigDecimal().setScale(2, RoundingMode.DOWN)
            }(Matic) in processing.."
        )

        val txValue =
            if (it.data?.tx?.params?.value == "null" || it.data?.tx?.params?.value == null) "0" else it.data?.tx?.params?.value
        CoroutineScope(Dispatchers.Main).launch {
            token.callFunction.signAndSendTranscation(
                toAddress = it.data?.tx?.params?.to,
                gasLimit = "0",
                gasPrice = "0",
                data = it.data?.tx?.params?.data!!,
                value = txValue!!

            ) { success, errorMessage, _ ->
                if (success) {
                    requireActivity().runOnUiThread {

                        CoroutineScope(Dispatchers.Main).launch {
                            token.callFunction.signAndSendTranscation(
                                toAddress = it.data?.tx?.params?.to,
                                gasLimit = "0",
                                gasPrice = "0",
                                data = it.data?.tx?.params?.data!!,
                                value = txValue!!

                            ) { success, errorMessage, _ ->
                                if (success) {
                                    requireActivity().runOnUiThread {

                                        SwapProgressDialog.getInstance()
                                            .dismiss()
                                        requireContext().showToast("Success")
                                        findNavController().popBackStack()
                                    }

                                } else {
                                    requireActivity().runOnUiThread {
                                        SwapProgressDialog.getInstance()
                                            .dismiss()
                                        hideLoader()
                                        requireContext().showToast("$errorMessage")
                                    }
                                }

                            }
                        }
                    }

                } else {
                    requireActivity().runOnUiThread {
                        SwapProgressDialog.getInstance().dismiss()
                        hideLoader()
                        requireContext().showToast("$errorMessage")
                    }
                }


            }
        }

    }

    private fun searchDomain(domainName: String) {
        eNSListAdapter?.submitList(arrayListOf())
        val model = DomainSearchModel(
            currency = "MATIC",
            domainName = domainName,
            owner = DomainSearchModel.Owner(address = Wallet.getPublicWalletAddress(CoinType.ETHEREUM)),
            records = DomainSearchModel.Records(
                cryptoETHAddress = Wallet.getPublicWalletAddress(
                    CoinType.ETHEREUM
                )
            )

        )

        addENSViewModel.getEnsListCall(model)
    }

    override fun setupObserver() {

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                addENSViewModel.ensListResponse.collect {
                    when (it) {
                        is NetworkState.Success -> {
                            hideLoader()
                            viewDataBinding!!.txtErrorMessage.apply {
                                visibility = View.GONE
                                text = getString(R.string.please_search_domain_name_from_searchbar)
                            }
                            loge(tag = "ENSList", "${it.data}")
                            if (it.data != null) {
                                val list = mutableListOf<ENSListModel>()
                                list.add(it.data)
                                eNSListAdapter?.submitList(list)
                            }

                        }

                        is NetworkState.Loading -> {
                            requireContext().showLoader()
                        }

                        is NetworkState.Error -> {
                            hideLoader()

                            viewDataBinding!!.txtErrorMessage.apply {
                                visibility = View.VISIBLE
                                text = it.message.toString()
                            }
                        }

                        is NetworkState.SessionOut -> {
                            hideLoader()
                            CustomSnackbar.make(
                                requireActivity().window.decorView.rootView as ViewGroup,
                                it.message.toString()
                            ).show()
                        }

                        else -> {
                            hideLoader()
                        }
                    }
                }
            }
        }

    }


    private fun openSwapProgressDialog(title: String, subtitle: String) {
        SwapProgressDialog.getInstance().show(
            requireContext(),
            title,
            subtitle,
            listener = object : SwapProgressDialog.DialogOnClickBtnListner {
                override fun onOkClick() {

                    //  findNavController().safeNavigate(PreviewSwapFragmentDirections.actionPreviewSwapFragmentToDashboard())

                }
            })
    }


}