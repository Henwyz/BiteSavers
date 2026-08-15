package com.example.bitesavers.customer.profile.logic

/**
 * MVP "math engine" for BiteSaver's sustainability stats.
 *
 * Assumptions (per team brief, to keep the business upload form simple):
 * - every rescued portion/meal averages AVG_PORTION_WEIGHT_KG
 * - CO2E_SAVED_PER_KG is a rounded, defensible estimate. Real food-rescue
 *   platforms (e.g. FoodMesh) report ~3.36 kg CO2e saved per kg of food
 *   diverted to charity vs. landfill; we use a conservative 2.5 for the
 *   demo. Swap this constant if your tutor wants a cited exact figure.
 */
object SustainabilityCalculator {

    const val AVG_PORTION_WEIGHT_KG = 0.5
    const val CO2E_SAVED_PER_KG = 2.5

    fun calculateImpact(mealsRescued: Int): ImpactStats {
        val kgSaved = mealsRescued * AVG_PORTION_WEIGHT_KG
        val co2SavedKg = kgSaved * CO2E_SAVED_PER_KG
        return ImpactStats(
            mealsRescued = mealsRescued,
            kgFoodSaved = kgSaved,
            co2SavedKg = co2SavedKg
        )
    }

    /**
     * NGO checkout should be RM0 regardless of listing price.
     * Call this from checkout logic (Member 1's flow) rather than
     * duplicating the isNGO check there.
     */
    fun finalPriceFor(originalOfferPrice: Double, isNgo: Boolean): Double =
        if (isNgo) 0.0 else originalOfferPrice
}

data class ImpactStats(
    val mealsRescued: Int,
    val kgFoodSaved: Double,
    val co2SavedKg: Double
)
