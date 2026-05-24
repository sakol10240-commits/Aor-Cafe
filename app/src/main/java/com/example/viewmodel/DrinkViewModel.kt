package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiManager
import com.example.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface ReportUiState {
    object Initial : ReportUiState
    object Loading : ReportUiState
    data class Success(val content: String) : ReportUiState
    data class Error(val message: String) : ReportUiState
    object ApiKeyMissing : ReportUiState
}

class DrinkViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = DrinkRepository(db.drinkDao())

    val drinksState: StateFlow<List<DrinkItem>> = repository.allDrinks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val visitsState: StateFlow<List<ClientVisit>> = repository.allVisits
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _reportUiState = MutableStateFlow<ReportUiState>(ReportUiState.Initial)
    val reportUiState: StateFlow<ReportUiState> = _reportUiState.asStateFlow()

    fun addNewDrink(name: String, price: Double, stock: Int, minThreshold: Int, category: String, colorHex: String) {
        viewModelScope.launch {
            repository.insertDrink(
                DrinkItem(
                    name = name,
                    price = price,
                    stock = stock,
                    minThreshold = minThreshold,
                    category = category,
                    colorHex = colorHex
                )
            )
        }
    }

    fun updateDrinkStock(drink: DrinkItem, newStock: Int) {
        viewModelScope.launch {
            repository.updateDrink(drink.copy(stock = newStock))
        }
    }

    fun updateDrink(drink: DrinkItem) {
        viewModelScope.launch {
            repository.updateDrink(drink)
        }
    }
    
    fun deleteDrink(drink: DrinkItem) {
        viewModelScope.launch {
            repository.deleteDrink(drink)
        }
    }

    fun logClientVisit(clientName: String, drink: DrinkItem, qty: Int, notes: String, discountPercent: Int = 0) {
        viewModelScope.launch {
            val basePrice = drink.price * qty
            val totalPrice = basePrice * (1.0 - discountPercent / 100.0)
            val finalNotes = if (discountPercent > 0) {
                if (notes.isNotBlank()) "[Discount: $discountPercent%] $notes" else "[Discount: $discountPercent%]"
            } else {
                notes
            }
            val visit = ClientVisit(
                clientName = clientName,
                drinkName = drink.name,
                drinkPrice = drink.price,
                qty = qty,
                totalPrice = totalPrice,
                notes = finalNotes
            )
            repository.logVisit(visit)
        }
    }

    fun deleteClientVisit(visit: ClientVisit) {
        viewModelScope.launch {
            repository.deleteVisit(visit)
        }
    }

    fun generateReport(interval: com.example.api.ReportInterval = com.example.api.ReportInterval.DAILY) {
        viewModelScope.launch {
            _reportUiState.value = ReportUiState.Loading
            val currentVisits = visitsState.value
            val currentDrinks = drinksState.value
            
            val result = GeminiManager.generateDrinkSalesReport(currentVisits, currentDrinks, interval)
            _reportUiState.value = when (result) {
                "API_KEY_MISSING" -> ReportUiState.ApiKeyMissing
                else -> {
                    if (result.startsWith("ERROR:")) {
                        ReportUiState.Error(result.substringAfter("ERROR:"))
                    } else {
                        ReportUiState.Success(result)
                    }
                }
            }
        }
    }

    fun resetReport() {
        _reportUiState.value = ReportUiState.Initial
    }
}
