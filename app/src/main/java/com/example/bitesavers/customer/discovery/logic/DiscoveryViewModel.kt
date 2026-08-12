package com.example.bitesavers.customer.discovery.logic

import androidx.lifecycle.ViewModel
import com.example.bitesavers.customer.discovery.data.DiscoveryDummyData
import com.example.bitesavers.customer.discovery.data.DiscoveryUiState
import com.example.bitesavers.data.model.DiscoveryCategory
import com.example.bitesavers.data.model.OfferUiModel
import com.example.bitesavers.data.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DiscoveryViewModel : ViewModel() {

    //If we put standard variables inside Compose UI, and part of the UI could accidentally
    //overwrite the data, causing weird bugs where the wrong food shows up

    /**
     * BACKING PROPERTY PATTERN (Unidirectional Data Flow)
     * This setup ensures the UI can only read data, while only this ViewModel can change it.
     *
     * 1. MutableStateFlow (The Kitchen):
     *    - 'Mutable' means the data inside can be edited or updated.
     *    - 'private' acts as a security guard. It ensures the Compose UI cannot
     *      accidentally overwrite this data directly.
     *    - '_' (underscore) is the standard Kotlin naming convention indicating
     *      that this is an internal, private variable.
     */
    private val _uiState = MutableStateFlow(DiscoveryDummyData.initialState())

    /**
     * 2. StateFlow (The Food Served):
     *    - This is the public variable the UI will observe to draw the screen.
     *    - 'asStateFlow()' takes the secret, editable data above and converts it
     *      into a strictly Read-Only format.
     *    - The UI can look at it to trigger recomposition (redrawing the screen),
     *      but cannot change it, keeping our business logic safe and predictable.
     */
    val uiState: StateFlow<DiscoveryUiState> = _uiState.asStateFlow()

    // To Handle map pin clicked
    fun onMapMarkerClicked(offerId: String?) {
        _uiState.update { current ->
            current.copy(selectedMapOfferId = offerId)
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { current -> //looks at the current state of the screen this second
            val updated = current.copy(searchQuery = query) //use copy because cannot directly change data
            updated.copy(filteredOffers = applyFilters(updated))
            //immediately recalculates which food cards should be visible, for each word type
        }
    }

    fun onCategorySelected(category: DiscoveryCategory) {
        _uiState.update { current ->
            val updated = current.copy(selectedCategory = category)
            updated.copy(filteredOffers = applyFilters(updated))
        }
    }

    /**
     * Future hook:
     * NGO users can see RM0 items (e.g., close to expiry/closing).
     */
    fun onUserRoleChanged(role: UserRole) {
        _uiState.update { current ->
            val updatedCategories = if (role == UserRole.NGO) {
                (current.availableCategories + DiscoveryCategory.FREE).distinct()
            } else {
                current.availableCategories.filterNot { it == DiscoveryCategory.FREE }
            }

            val updated = current.copy(
                userRole = role,
                availableCategories = updatedCategories,
                selectedCategory = if (
                    role == UserRole.CONSUMER && current.selectedCategory == DiscoveryCategory.FREE
                ) DiscoveryCategory.ALL else current.selectedCategory
            )
            updated.copy(filteredOffers = applyFilters(updated))
        }
    }

    private fun applyFilters(state: DiscoveryUiState): List<OfferUiModel> {
        val query = state.searchQuery.trim().lowercase()
        //takes the query (in all form)

        return state.offers.asSequence() //process it as stream
            .filter { offer ->
                // UPDATED: Now matches your actual app categories!
                when (state.selectedCategory) {
                    DiscoveryCategory.ALL -> true
                    DiscoveryCategory.BAKERY -> offer.category == DiscoveryCategory.BAKERY
                    DiscoveryCategory.HOT_MEALS -> offer.category == DiscoveryCategory.HOT_MEALS
                    DiscoveryCategory.DESSERTS -> offer.category == DiscoveryCategory.DESSERTS
                    DiscoveryCategory.BEVERAGES -> offer.category == DiscoveryCategory.BEVERAGES
                    DiscoveryCategory.FREE -> {
                        state.userRole == UserRole.NGO &&
                                offer.hoursToClose <= 1 &&
                                offer.isEligibleForNgoFree
                    }
                }
            }
            .filter { offer ->
                query.isBlank() ||
                        offer.title.lowercase().contains(query) ||
                        offer.storeName.lowercase().contains(query)
            }
            .toList() //bundles the surviving food cards back into a standard list
    }
}