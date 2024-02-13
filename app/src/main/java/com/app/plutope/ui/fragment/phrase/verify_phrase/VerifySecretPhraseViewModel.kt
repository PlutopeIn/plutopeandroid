package com.app.plutope.ui.fragment.phrase.verify_phrase

import androidx.lifecycle.viewModelScope
import com.app.plutope.data.repository.WalletRepo
import com.app.plutope.model.Wallet
import com.app.plutope.model.Wallets
import com.app.plutope.ui.base.BaseViewModel
import com.app.plutope.utils.common.CommonNavigator
import com.app.plutope.utils.network.NetworkState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VerifySecretPhraseViewModel @Inject constructor(private val walletRepo: WalletRepo) :
    BaseViewModel<CommonNavigator>() {
    //Insert Wallet
    private val _tagInsertWallet =
        MutableStateFlow<NetworkState<Wallets?>>(NetworkState.Empty())

    val insertWalletResponse: StateFlow<NetworkState<Wallets?>>
        get() = _tagInsertWallet

    fun executeInsertWallet(mnemonics: String, walletName: String = "",isFromDrive:Boolean=false,isManualBackup:Boolean=false,folderId:String="",fileId:String="") {
        viewModelScope.launch {
            _tagInsertWallet.emit(NetworkState.Loading())
            _tagInsertWallet.collectStateFlow(walletRepo.insertWallet(mnemonics, walletName,isFromDrive,isManualBackup))
        }
    }



    //get Primary Wallet
    private val _getPrimaryWalletsResponse =
        MutableStateFlow<NetworkState<Wallets?>>(NetworkState.Empty())
    val walletsPrimaryResponse: MutableStateFlow<NetworkState<Wallets?>>
        get() = _getPrimaryWalletsResponse

    fun getPrimaryWallet() {
        viewModelScope.launch {
            _getPrimaryWalletsResponse.emit(NetworkState.Loading())
            walletRepo.getPrimaryWallet().collect { networkState ->
                _getPrimaryWalletsResponse.value = networkState
            }
        }
    }

    //update primary wallet and other primary wallet should be false
    private val _getPrimaryWalletsUpdateResponse =
        MutableStateFlow<NetworkState<Wallets?>>(NetworkState.Empty())
    val walletsPrimaryWalletUpdateResponse: MutableStateFlow<NetworkState<Wallets?>>
        get() = _getPrimaryWalletsUpdateResponse

    fun updatePrimaryWallet(id: Int) {
        viewModelScope.launch {
            _getPrimaryWalletsUpdateResponse.emit(NetworkState.Loading())
            walletRepo.updatePrimaryWallet(id).collect { networkState ->

                _getPrimaryWalletsUpdateResponse.value = networkState
            }
        }
    }

    //update primary wallet and other primary wallet should be false
    private val _getUpdateWalletBackupResponse =
        MutableStateFlow<NetworkState<Wallets?>>(NetworkState.Empty())
    val updateWalletBackupResponse: MutableStateFlow<NetworkState<Wallets?>>
        get() = _getUpdateWalletBackupResponse

    fun updateWalletBackup(isCloudBackup:Boolean,isManualBackup:Boolean,walletId:Int,walletName: String,folderId:String,fileId:String) {
        viewModelScope.launch {
            _getPrimaryWalletsUpdateResponse.emit(NetworkState.Loading())
            walletRepo.updateWalletBackupSet(isCloudBackup,isManualBackup,walletId,walletName,folderId,fileId).collect { networkState ->
                _getUpdateWalletBackupResponse.value = networkState
            }
        }
    }

    //get Wallet
    private val _getWalletsResponse =
        MutableStateFlow<NetworkState<List<Wallets?>>>(NetworkState.Empty())
    val walletsListResponse: MutableStateFlow<NetworkState<List<Wallets?>>>
        get() = _getWalletsResponse

    fun getWalletsList() {
        viewModelScope.launch {
            _getWalletsResponse.emit(NetworkState.Loading())
            walletRepo.getAllWalletList().collect { networkState ->
                _getWalletsResponse.value = networkState
            }
        }
    }

    //delete wallet
    private val _deleteWalletsResponse =
        MutableStateFlow<NetworkState<Int?>>(NetworkState.Empty())
    val walletsDeleteResponse: MutableStateFlow<NetworkState<Int?>>
        get() = _deleteWalletsResponse

    fun deleteWalletById(walletId: Int, walletList: MutableList<Wallets?>, isPrimary: Int?) {
        viewModelScope.launch {
            _deleteWalletsResponse.emit(NetworkState.Loading())
            if(isPrimary==1){
                walletList.remove(walletList.filter { it?.w_id==walletId }.first())

                walletRepo.deleteWalletByWalletId(walletId).collect { networkState ->
                    walletRepo.updatePrimaryWallet(walletList.first()?.w_id!!).collect{networkState1->
                        Wallet.setWalletObjectFromInstance(walletList.first()!!)
                        Wallet.refreshWallet()
                        _deleteWalletsResponse.value = networkState
                    }

                }
            } else {
                walletRepo.deleteWalletByWalletId(walletId).collect { networkState ->
                    _deleteWalletsResponse.value = networkState
                }
            }
        }
    }


    /***
     * Update wallet
     * **/
    private val _updateWalletsResponse =
        MutableStateFlow<NetworkState<Int?>>(NetworkState.Empty())
    val updateWalletsResponse: MutableStateFlow<NetworkState<Int?>>
        get() = _updateWalletsResponse

    fun updateWallets(walletList: MutableList<Wallets?>) {
        viewModelScope.launch {
            _updateWalletsResponse.emit(NetworkState.Loading())
            walletRepo.updateAllWalletList(walletList).collect { networkState ->
                _updateWalletsResponse.value = networkState
            }
        }
    }


}