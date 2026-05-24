package com.example.data

import kotlinx.coroutines.flow.Flow

class DrinkRepository(private val drinkDao: DrinkDao) {
    val allDrinks: Flow<List<DrinkItem>> = drinkDao.getAllDrinks()
    val allVisits: Flow<List<ClientVisit>> = drinkDao.getAllVisits()

    suspend fun insertDrink(drink: DrinkItem) {
        drinkDao.insertDrink(drink)
    }

    suspend fun updateDrink(drink: DrinkItem) {
        drinkDao.updateDrink(drink)
    }

    suspend fun deleteDrink(drink: DrinkItem) {
        drinkDao.deleteDrink(drink)
    }

    suspend fun logVisit(visit: ClientVisit) {
        drinkDao.insertVisit(visit)
        // Stock count is cancelled, so we no longer deduct stock
        // drinkDao.deductStock(visit.drinkName, visit.qty)
    }

    suspend fun deleteVisit(visit: ClientVisit) {
        drinkDao.deleteVisit(visit.id)
        // Stock count is cancelled, so we no longer restock/refund
        // drinkDao.addStock(visit.drinkName, visit.qty) // Refund/restock the deleted order
    }
}
