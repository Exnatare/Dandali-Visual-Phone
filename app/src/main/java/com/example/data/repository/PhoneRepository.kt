package com.example.data.repository

import com.example.data.dao.ContactDao
import com.example.data.dao.LocationDao
import com.example.data.dao.NoteDao
import com.example.data.dao.SettingDao
import com.example.data.model.ContactEntity
import com.example.data.model.LocationEntity
import com.example.data.model.NoteEntity
import com.example.data.model.SettingEntity
import kotlinx.coroutines.flow.Flow

class PhoneRepository(
    private val contactDao: ContactDao,
    private val noteDao: NoteDao,
    private val locationDao: LocationDao,
    private val settingDao: SettingDao
) {
    val allContacts: Flow<List<ContactEntity>> = contactDao.getAllContacts()
    val allNotes: Flow<List<NoteEntity>> = noteDao.getAllNotes()
    val allLocations: Flow<List<LocationEntity>> = locationDao.getAllLocations()
    val allSettings: Flow<List<SettingEntity>> = settingDao.getAllSettings()

    suspend fun insertContact(contact: ContactEntity) = contactDao.insertContact(contact)
    suspend fun updateContact(contact: ContactEntity) = contactDao.updateContact(contact)
    suspend fun deleteContact(contact: ContactEntity) = contactDao.deleteContact(contact)
    fun searchContacts(query: String): Flow<List<ContactEntity>> = contactDao.searchContacts(query)

    suspend fun insertNote(note: NoteEntity) = noteDao.insertNote(note)
    suspend fun updateNote(note: NoteEntity) = noteDao.updateNote(note)
    suspend fun deleteNote(note: NoteEntity) = noteDao.deleteNote(note)

    suspend fun insertLocation(location: LocationEntity) = locationDao.insertLocation(location)
    suspend fun deleteLocation(location: LocationEntity) = locationDao.deleteLocation(location)

    suspend fun getSetting(key: String): String? {
        return settingDao.getSetting(key)?.value
    }

    suspend fun saveSetting(key: String, value: String) {
        settingDao.insertSetting(SettingEntity(key, value))
    }

    suspend fun deleteSetting(key: String) {
        settingDao.deleteSetting(key)
    }
}
