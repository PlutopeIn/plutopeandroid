package com.app.plutope.data.repository

import com.app.plutope.data.database.ContactDao
import com.app.plutope.model.ContactModel
import com.app.plutope.network.NoConnectivityException
import com.app.plutope.utils.constant.NO_INTERNET_CONNECTION
import com.app.plutope.utils.network.NetworkState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.net.UnknownHostException
import javax.inject.Inject

class ContactRepo @Inject constructor(private val contactDao: ContactDao) {

    suspend fun insertContact(
        contact:ContactModel
    ): NetworkState<ContactModel?> {
        return try {
            val data= contactDao.getSpecificContacts(contact.address)
            if(data.isNotEmpty()){
                val contactObj = data[0]
                contactObj.name=contact.name
                contactDao.updateContact(contactObj)
            }else{
                contactDao.insert(contact)
            }

            NetworkState.Success("", contact)
        } catch (e: Exception) {
            e.printStackTrace()
            if(e is NoConnectivityException || e is UnknownHostException) {
                NetworkState.SessionOut("", NO_INTERNET_CONNECTION)
            }else {
                NetworkState.Error(e.message.toString())
            }
        }

    }

    suspend fun getAllContacts(): Flow<NetworkState<List<ContactModel?>>> {
        return flow {
            val localData: MutableList<ContactModel> = mutableListOf()
            localData.addAll(contactDao.getAllContacts())

            if (localData.isNotEmpty()) {
                // If the local data is not empty, return it immediately
                emit(NetworkState.Success("", localData))
            } else {
                emit(NetworkState.Error("Failed to load"))
            }
        }
    }

    suspend fun getSpecificContacts(address:String): Flow<NetworkState<List<ContactModel?>>> {
        return flow {
            val localData: MutableList<ContactModel> = mutableListOf()
            localData.addAll(contactDao.getSpecificContacts(address))

            if (localData.isNotEmpty()) {
                // If the local data is not empty, return it immediately
                emit(NetworkState.Success("", localData))
            } else {
                emit(NetworkState.Error("Failed to load"))
            }
        }
    }
}