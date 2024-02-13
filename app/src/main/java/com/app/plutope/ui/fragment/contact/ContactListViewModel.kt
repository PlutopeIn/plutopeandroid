package com.app.plutope.ui.fragment.contact

import androidx.lifecycle.viewModelScope
import com.app.plutope.data.repository.ContactRepo
import com.app.plutope.model.ContactModel
import com.app.plutope.model.Tokens
import com.app.plutope.ui.base.BaseViewModel
import com.app.plutope.utils.common.CommonNavigator
import com.app.plutope.utils.network.NetworkState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactListViewModel @Inject constructor(private val contactRepo: ContactRepo) : BaseViewModel<CommonNavigator>() {

    //Insert Contacts
    private val _tagInsertContacts =
        MutableStateFlow<NetworkState<ContactModel?>>(NetworkState.Empty())

    val insertContactResponse: StateFlow<NetworkState<ContactModel?>>
        get() = _tagInsertContacts

    fun executeInsertTokens(contact: ContactModel) {
        viewModelScope.launch {
            _tagInsertContacts.emit(NetworkState.Loading())
            _tagInsertContacts.collectStateFlow(contactRepo.insertContact(contact))
        }
    }

    //get Contacts
    private val _getContactsResponse =
        MutableStateFlow<NetworkState<List<ContactModel?>>>(NetworkState.Empty())
    val contactsResponse: MutableStateFlow<NetworkState<List<ContactModel?>>>
        get() = _getContactsResponse

    fun getContactList() {

        viewModelScope.launch {
            _getContactsResponse.emit(NetworkState.Loading())
            contactRepo.getAllContacts().collect { networkState ->
                _getContactsResponse.value = networkState
            }
        }
    }

    //get Contacts
    private val _getSpecificContactsResponse =
        MutableStateFlow<NetworkState<List<ContactModel?>>>(NetworkState.Empty())
    val contactsSpecificResponse: MutableStateFlow<NetworkState<List<ContactModel?>>>
        get() = _getSpecificContactsResponse

    fun getSpecificContactList(address:String) {

        viewModelScope.launch {
            _getSpecificContactsResponse.emit(NetworkState.Loading())
            contactRepo.getSpecificContacts(address).collect { networkState ->
                _getSpecificContactsResponse.value = networkState
            }
        }
    }

}