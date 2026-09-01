package com.example.bitesavers.customer.discovery.logic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bitesavers.customer.discovery.data.DiscoveryUiState
import com.example.bitesavers.customer.discovery.data.NearbyDealMarkerUiModel
import com.example.bitesavers.customer.discovery.ui.DiscoveryUiEvent
import com.example.bitesavers.data.model.DiscoveryCategory
import com.example.bitesavers.data.model.OfferUiModel
import com.example.bitesavers.data.model.UserRole
import com.example.bitesavers.data.remote.UserSession
import com.example.bitesavers.data.repository.OfferRepository
import com.example.bitesavers.data.repository.SavedRepository
import com.example.bitesavers.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DiscoveryViewModel : ViewModel() {

    private val repository: OfferRepository = OfferRepository()
    private val savedRepository: SavedRepository = SavedRepository()
    private val userRepository: UserRepository = UserRepository()

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
        observeUserSessionChanges()
    }

    /**
     * Listens to UserSession changes dynamically.
     * Whenever MainActivity changes user (e.g., u1 -> u2), it automatically fetches the new name and bookmarks.
     */
    private fun observeUserSessionChanges() {
        viewModelScope.launch {
            UserSession.currentUserId.collectLatest { userId ->
                if (userId.isNotBlank()) {
                    // 1. Fetch real username from Supabase for this user ID
                    val profile = userRepository.fetchUserProfile(userId)
                    if (profile != null) {
                        _uiState.update { current ->
                            current.copy(user = profile)
                        }
                    }

                    // 2. Load bookmarks for this active user
                    savedRepository.loadUserSavedOffers(userId)
                }
            }
        }
    }

    /**
     * Called when GPS location is acquired from the UI/Device.
     * Updates coordinates, sorts offers by closest distance, and groups top items by store.
     */
    fun updateUserLocation(lat: Double, lng: Double) {
        userLatitude = lat
        userLongitude = lng
        _uiState.update { current ->
            val visibleOffers = applyFilters(current, allOffers)
            val generatedMarkers = groupOffersByStore(visibleOffers, lat, lng)

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
                    val generatedMarkers = groupOffersByStore(visibleOffers, baseLat, baseLng)

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

    /**
     * Groups offers by store coordinates and returns only the 3 closest store markers for the map.
     */
    private fun groupOffersByStore(
        offers: List<OfferUiModel>,
        fallbackLat: Double,
        fallbackLng: Double
    ): List<NearbyDealMarkerUiModel> {
        val groupedByStore = offers.groupBy { it.storeName }

        return groupedByStore.entries
            // Sorts store groups by the closest offer distance
            .sortedBy { entry -> entry.value.minOfOrNull { it.distanceKm } ?: Double.MAX_VALUE }
            // Limits map pins strictly to the top 3 closest stores
            .take(3)
            .mapIndexed { index, entry ->
                val storeOffers = entry.value
                val firstOffer = storeOffers.first()

                val pinLat = firstOffer.latitude ?: (fallbackLat + (index * 0.003))
                val pinLng = firstOffer.longitude ?: (fallbackLng + (index * 0.003))

                val label = if (storeOffers.size > 1) {
                    "${storeOffers.size} DEALS"
                } else {
                    "RM %.2f".format(firstOffer.currentPrice)
                }

                NearbyDealMarkerUiModel(
                    storeId = firstOffer.id, // Primary key identifier for pin selection
                    storeName = entry.key,
                    labelText = label,
                    latitude = pinLat,
                    longitude = pinLng,
                    offers = storeOffers
                )
            }
    }

    // To Handle map pin clicked
    fun onMapMarkerClicked(markerStoreId: String?) {
        _uiState.update { current ->
            current.copy(selectedMapOfferId = markerStoreId)
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { current ->
            val updated = current.copy(searchQuery = query)
            val filtered = applyFilters(updated, allOffers)
            val baseLat = userLatitude ?: 3.1390
            val baseLng = userLongitude ?: 101.6869
            updated.copy(
                offers = filtered,
                nearbyMarkers = groupOffersByStore(filtered, baseLat, baseLng)
            )
        }
    }

    fun onCategorySelected(category: DiscoveryCategory) {
        _uiState.update { current ->
            val updated = current.copy(selectedCategory = category)
            val filtered = applyFilters(updated, allOffers)
            val baseLat = userLatitude ?: 3.1390
            val baseLng = userLongitude ?: 101.6869
            updated.copy(
                offers = filtered,
                nearbyMarkers = groupOffersByStore(filtered, baseLat, baseLng)
            )
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
            val filtered = applyFilters(updated, allOffers)
            val baseLat = userLatitude ?: 3.1390
            val baseLng = userLongitude ?: 101.6869
            updated.copy(
                offers = filtered,
                nearbyMarkers = groupOffersByStore(filtered, baseLat, baseLng)
            )
        }
    }

    private fun applyFilters(state: DiscoveryUiState, sourceList: List<OfferUiModel>): List<OfferUiModel> {
        val query = state.searchQuery.trim().lowercase()
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
            }
            .filter { offer ->
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

        // Returns all matching items sorted by closest distance without truncating the list
        return if (currentLat != null && currentLng != null) {
            filteredSequence
                .map { offer ->
                    val offerLat = offer.latitude ?: 3.1390
                    val offerLng = offer.longitude ?: 101.6869
                    val dist = LocationUtils.calculateDistanceKm(currentLat, currentLng, offerLat, offerLng)
                    offer.copy(distanceKm = dist)
                }
                .sortedBy { it.distanceKm }
                .toList()
        } else {
            filteredSequence.toList()
        }
    }

    fun onEvent(event: DiscoveryUiEvent) {
        when (event) {
            is DiscoveryUiEvent.OnSearchQueryChanged -> onSearchQueryChanged(event.query)
            is DiscoveryUiEvent.OnCategorySelected -> onCategorySelected(event.category)
            is DiscoveryUiEvent.OnMapMarkerClicked -> onMapMarkerClicked(event.offerId)
            is DiscoveryUiEvent.OnResetFilters -> onResetFilters()
            is DiscoveryUiEvent.OnToggleBookmark -> onToggleBookmark(event.offerId)
            else -> {}
        }
    }

    private fun onToggleBookmark(offerId: String) {
        val currentUserId = UserSession.currentUserId.value
        if (currentUserId.isNotBlank()) {
            viewModelScope.launch {
                savedRepository.toggleSaveOffer(
                    userId = currentUserId,
                    offerId = offerId
                )
            }
        }
    }

    fun onResetFilters() {
        _uiState.update { current ->
            val updated = current.copy(
                searchQuery = "",
                selectedCategory = DiscoveryCategory.ALL
            )
            val filtered = applyFilters(updated, allOffers)
            val baseLat = userLatitude ?: 3.1390
            val baseLng = userLongitude ?: 101.6869
            updated.copy(
                offers = filtered,
                nearbyMarkers = groupOffersByStore(filtered, baseLat, baseLng)
            )
        }
    }
}