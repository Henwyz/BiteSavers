package com.example.bitesavers.customer.discovery.logic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bitesavers.customer.discovery.data.DiscoveryUiState
import com.example.bitesavers.customer.discovery.data.NearbyDealMarkerUiModel
import com.example.bitesavers.customer.discovery.ui.DiscoveryUiEvent
import com.example.bitesavers.data.model.DiscoveryCategory
import com.example.bitesavers.data.model.OfferUiModel
import com.example.bitesavers.data.model.UserRole
import com.example.bitesavers.data.repository.OfferRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DiscoveryViewModel : ViewModel() {

    private val repository: OfferRepository = OfferRepository()

    // Master list of all offers fetched from Supabase (kept private in memory)
    private var allOffers: List<OfferUiModel> = emptyList()

    // User's live GPS coordinates (null until location permission is granted and resolved)
    private var userLatitude: Double? = null
    private var userLongitude: Double? = null

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
    private val _uiState = MutableStateFlow(DiscoveryUiState(isLoading = true))

    /**
     * 2. StateFlow (The Food Served):
     *    - This is the public variable the UI will observe to draw the screen.
     *    - 'asStateFlow()' takes the secret, editable data above and converts it
     *      into a strictly Read-Only format.
     *    - The UI can look at it to trigger recomposition (redrawing the screen),
     *      but cannot change it, keeping our business logic safe and predictable.
     */
    val uiState: StateFlow<DiscoveryUiState> = _uiState.asStateFlow()

    init {
        // Automatically fetch live Supabase offers when ViewModel is created
        loadOffers()
    }

    /**
     * Called when GPS location is acquired from the UI/Device.
     * Updates coordinates, sorts offers by closest distance, and picks the top 3 items.
     */
    fun updateUserLocation(lat: Double, lng: Double) {
        userLatitude = lat
        userLongitude = lng
        _uiState.update { current ->
            val visibleOffers = applyFilters(current, allOffers)

            // Generate map pins centered around user coordinates
            val generatedMarkers = visibleOffers.mapIndexed { index, offer ->
                NearbyDealMarkerUiModel(
                    id = offer.id,
                    labelPrice = "RM %.2f".format(offer.currentPrice),
                    latitude = offer.latitude ?: (lat + (index * 0.002)),
                    longitude = offer.longitude ?: (lng + (index * 0.002))
                )
            }

            current.copy(
                userLatitude = lat,
                userLongitude = lng,
                offers = visibleOffers,
                nearbyMarkers = generatedMarkers
            )
        }
    }

    /**
     * Fetch offers from Supabase via Repository
     */
    fun loadOffers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // 1. Fetch all items from Supabase SAFELY
                allOffers = repository.fetchOffers()

                // 2. Filter them and generate map markers
                _uiState.update { current ->
                    val visibleOffers = applyFilters(current, allOffers)

                    val baseLat = userLatitude ?: 3.1390
                    val baseLng = userLongitude ?: 101.6869

                    val generatedMarkers = visibleOffers.mapIndexed { index, offer ->
                        NearbyDealMarkerUiModel(
                            id = offer.id,
                            labelPrice = "RM %.2f".format(offer.currentPrice),
                            latitude = offer.latitude ?: (baseLat + (index * 0.003)),
                            longitude = offer.longitude ?: (baseLng + (index * 0.003))
                            //dummy data for now if offer coordinates are not set
                        )
                    }

                    current.copy(
                        isLoading = false,
                        userLatitude = userLatitude,
                        userLongitude = userLongitude,
                        offers = visibleOffers,
                        nearbyMarkers = generatedMarkers
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("DiscoveryVM", "Failed to fetch offers", e)
                _uiState.update { current ->
                    current.copy(isLoading = false)
                }
            }
        }
    }

    // To Handle map pin clicked
    fun onMapMarkerClicked(offerId: String?) {
        _uiState.update { current ->
            current.copy(selectedMapOfferId = offerId)
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { current -> //looks at the current state of the screen this second
            val updated = current.copy(searchQuery = query) //use copy because cannot directly change data
            updated.copy(offers = applyFilters(updated, allOffers))
            //immediately recalculates which food cards should be visible, for each word type
        }
    }

    fun onCategorySelected(category: DiscoveryCategory) {
        _uiState.update { current ->
            val updated = current.copy(selectedCategory = category)
            updated.copy(offers = applyFilters(updated, allOffers))
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
            updated.copy(offers = applyFilters(updated, allOffers))
        }
    }

    private fun applyFilters(state: DiscoveryUiState, sourceList: List<OfferUiModel>): List<OfferUiModel> {
        val query = state.searchQuery.trim().lowercase()
        //takes the query (in all form)
        val currentLat = userLatitude
        val currentLng = userLongitude

        val filteredSequence = sourceList.asSequence()
            .filter { offer ->
                if (state.userRole == UserRole.CONSUMER) {
                    // Customers can ONLY see items with MORE than 1 hour remaining
                    offer.hoursToClose > 1
                } else {
                    // NGOs can see all active items
                    true
                }
            } //process it as stream
            .filter { offer ->
                // Matches app categories
                when (state.selectedCategory) {
                    null, DiscoveryCategory.ALL -> true
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

        // If GPS is available, calculate distance with Haversine formula,
        // sort closest to farthest, and take the top 3 closest items!
        return if (currentLat != null && currentLng != null) {
            filteredSequence
                .map { offer ->
                    val offerLat = offer.latitude ?: 3.1390
                    val offerLng = offer.longitude ?: 101.6869
                    val dist = LocationUtils.calculateDistanceKm(currentLat, currentLng, offerLat, offerLng)
                    offer.copy(distanceKm = dist)
                }
                .sortedBy { it.distanceKm }
                .take(3)
                .toList() //bundles the surviving food cards back into a standard list
        } else {
            filteredSequence
                .take(3)
                .toList() //bundles the surviving food cards back into a standard list
        }
    }

    fun onEvent(event: DiscoveryUiEvent) {
        when (event) {
            is DiscoveryUiEvent.OnSearchQueryChanged -> onSearchQueryChanged(event.query)
            is DiscoveryUiEvent.OnCategorySelected -> onCategorySelected(event.category)
            is DiscoveryUiEvent.OnMapMarkerClicked -> onMapMarkerClicked(event.offerId)
            is DiscoveryUiEvent.OnResetFilters -> onResetFilters()
            else -> {}
        }
    }

    fun onResetFilters() {
        _uiState.update { current ->
            val updated = current.copy(
                searchQuery = "",
                selectedCategory = DiscoveryCategory.ALL
            )
            updated.copy(offers = applyFilters(updated, allOffers))
        }
    }
}