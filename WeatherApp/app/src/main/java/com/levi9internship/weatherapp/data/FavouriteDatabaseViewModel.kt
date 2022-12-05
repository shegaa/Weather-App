package com.levi9internship.weatherapp.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavouriteDatabaseViewModel @Inject constructor(
    private val repository: FavouriteRepository
) : ViewModel() {  //(application: Application) : AndroidViewModel(application) {
    val readAllData: LiveData<List<Favourite>>

    init {
        readAllData = repository.readAllData()
    }

    fun addFavourite(favourite: Favourite) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addFavourite(favourite)
        }
    }

    fun updateFavourite(favourite: Favourite) {//just in case
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateFavourite(favourite)
        }
    }

    fun deleteFavourite(favourite: Favourite) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteFavourite(favourite)
        }
    }

    fun deleteAllFavourites() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllFavourites()
        }
    }
}