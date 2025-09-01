package com.app.plutope.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.app.plutope.model.ContactModel

@Dao
interface ContactDao {

    @Query("SELECT * from contacts")
    fun getAllContacts(): List<ContactModel>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: ContactModel)

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllContacts(list: List<ContactModel>)

    @Query("SELECT * from contacts where address=:contactAddress")
    fun getSpecificContacts(contactAddress: String): List<ContactModel>

    @Update
    suspend fun updateContact(contact: ContactModel): Int

    @Query("Delete from Contacts where id=:addressId")
    suspend fun deleteContact(addressId: Int)

}