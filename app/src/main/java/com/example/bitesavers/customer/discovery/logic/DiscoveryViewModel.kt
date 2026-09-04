package com.example.bitesavers.customer.discovery.logic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bitesavers.customer.discovery.data.DiscoveryStoreUiModel
import com.example.bitesavers.customer.discovery.data.DiscoveryUiState
import com.example.bitesavers.customer.discovery.data.DiscoveryViewMode
import com.example.bitesavers.customer.discovery.data.NearbyDealMarkerUiModel
import com.example.bitesavers.customer.discovery.data.NotificationUiModel
import com.example.bitesavers.customer.discovery.ui.DiscoveryUiEvent
import com.example.bitesavers.data.model.DiscoveryCategory
import com.example.bitesavers.data.model.OfferUiModel
import com.example.bitesavers.data.model.UserRole
import com.example.bitesavers.data.remote.UserSession
import com.example.bitesavers.data.repository.NotificationRepository
import com.example.bitesavers.data.repository.OfferRepository
import com.example.bitesavers.data.repository.SavedRepository
import com.example.bitesavers.data.repository.UserRepository
import com.example.bitesavers.util.LocationUtils
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
    private val notificationRepository: NotificationRepository = NotificationRepository()

    // Default regional coordinates centered on Penang, Malaysia
    private val defaultLatitude = 5.4674
    private val defaultLongitude = 100.2790

    // Master list of all offers fetched from Supabase (kept private in memory)
    private var allOffers: List<OfferUiModel> = emptyList()

    // User's live GPS coordinates (null until location permission is granted and resolved)
    private var userLatitude: Double? = null
    private var userLongitude: Double? = null

    // Backing property pattern for unidirectional data flow
    private val _uiState = MutableStateFlow(DiscoveryUiState(isLoading = true))
    val uiState: StateFlow<DiscoveryUiState> = _uiState.asStateFlow()

    init {
        // Automatically fetch live Supabase offers when ViewModel is created
        loadOffers()
        observeUserSessionChanges()
        observeRealtimeNotificationEvents()
    }

    // Listens to UserSession changes dynamically and validates approved NGO status directly against Supabase
    private fun observeUserSessionChanges() {
        viewModelScope.launch {
            UserSession.currentUserId.collectLatest { userId ->
                if (userId.isNotBlank()) {
                    val profile = userRepository.fetchUserProfile(userId)

                    // Verifies ngo_status directly from the users table in Supabase
                    val ngoStatus = userRepository.fetchUserNgoStatus(userId)
                    val isApprovedNgo = ngoStatus.equals("APPROVED", ignoreCase = true)

                    val updatedCategories = if (isApprovedNgo) {
                        (_uiState.value.availableCategories + DiscoveryCategory.FREE).distinct()
                    } else {
                        _uiState.value.availableCategories.filterNot { it == DiscoveryCategory.FREE }
                    }

                    _uiState.update { current ->
                        current.copy(
                            user = profile ?: current.user,
                            userRole = UserRole.CONSUMER, // Kept as CONSUMER
                            isNgoApproved = isApprovedNgo,
                            availableCategories = updatedCategories,
                            selectedCategory = if (!isApprovedNgo && current.selectedCategory == DiscoveryCategory.FREE) {
                                DiscoveryCategory.ALL
                            } else current.selectedCategory
                        )
                    }

                    // Load bookmarks for this active user
                    savedRepository.loadUserSavedOffers(userId)

                    // Refresh notifications for active user
                    loadUserNotifications(userId)
                }
            }
        }
    }

    // Listens for cross-screen order updates emitted by MainActivity to immediately refresh badge
    private fun observeRealtimeNotificationEvents() {
        viewModelScope.launch {
            UserSession.notificationRefreshEvent.collectLatest {
                val userId = UserSession.getUserId()
                if (userId.isNotBlank()) {
                    loadUserNotifications(userId)
                }
            }
        }
    }

    // Fetches notifications directly from the remote database table
    private fun loadUserNotifications(userId: String) {
        viewModelScope.launch {
            try {
                val dtoList = notificationRepository.fetchUserNotifications(userId)
                val mapped = dtoList.map { dto ->
                    NotificationUiModel(
                        id = dto.id,
                        orderId = dto.orderId.orEmpty(),
                        title = dto.title,
                        message = dto.message,
                        timestamp = com.example.bitesavers.util.TimeUtils.formatNotificationTimestamp(dto.createdAt),
                        isRead = dto.isRead
                    )
                }
                _uiState.update { it.copy(notifications = mapped) }
            } catch (e: Exception) {
                android.util.Log.e("DiscoveryVM", "Failed to load notifications: ${e.message}")
            }
        }
    }

    // Called when GPS location is acquired from the UI/Device
    fun updateUserLocation(lat: Double, lng: Double) {
        userLatitude = lat
        userLongitude = lng
        _uiState.update { current ->
            val visibleOffers = applyFilters(current, allOffers)
            val visibleStores = deriveStoresFromOffers(visibleOffers, lat, lng)
            val generatedMarkers = groupOffersByStore(visibleOffers, lat, lng)

            current.copy(
                userLatitude = lat,
                userLongitude = lng,
                offers = visibleOffers,
                stores = visibleStores,
                nearbyMarkers = generatedMarkers
            )
        }
    }

    // Fetch offers from Supabase via Repository
    fun loadOffers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // Fetch all items from Supabase safely
                allOffers = repository.fetchOffers()

                // Filter them and generate map markers & stores
                _uiState.update { current ->
                    val visibleOffers = applyFilters(current, allOffers)
                    val baseLat = userLatitude ?: defaultLatitude
                    val baseLng = userLongitude ?: defaultLongitude
                    val visibleStores = deriveStoresFromOffers(visibleOffers, baseLat, baseLng)
                    val generatedMarkers = groupOffersByStore(visibleOffers, baseLat, baseLng)

                    current.copy(
                        isLoading = false,
                        userLatitude = userLatitude,
                        userLongitude = userLongitude,
                        offers = visibleOffers,
                        stores = visibleStores,
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

    // Transforms unique stores from the offer inventory with distances and active bag counts
    private fun deriveStoresFromOffers(
        offers: List<OfferUiModel>,
        userLat: Double?,
        userLng: Double?
    ): List<DiscoveryStoreUiModel> {
        return offers.groupBy { it.storeName }
            .map { (storeName, storeOffers) ->
                val firstOffer = storeOffers.first()
                val storeLat = firstOffer.latitude
                val storeLng = firstOffer.longitude
                val dist = if (userLat != null && userLng != null && storeLat != null && storeLng != null) {
                    LocationUtils.calculateDistanceKm(userLat, userLng, storeLat, storeLng)
                } else {
                    firstOffer.distanceKm
                }

                // Resolves genuine store ID, eliminating incorrect fallback to offer ID
                val trueStoreId = firstOffer.storeId.ifBlank { "store_01" }

                DiscoveryStoreUiModel(
                    id = trueStoreId,
                    name = storeName,
                    address = firstOffer.description.takeIf { !it.isNullOrBlank() } ?: "Penang, Malaysia",
                    rating = firstOffer.storeRating ?: 0.0,
                    imageUrl = firstOffer.imageUrl,
                    operatingHours = firstOffer.pickupWindow,
                    activeOffersCount = storeOffers.size,
                    distanceKm = dist,
                    latitude = storeLat,
                    longitude = storeLng
                )
            }
            .sortedBy { it.distanceKm }
    }

    // Groups offers by store coordinates and returns top 3 store markers for the map
    private fun groupOffersByStore(
        offers: List<OfferUiModel>,
        fallbackLat: Double,
        fallbackLng: Double
    ): List<NearbyDealMarkerUiModel> {
        val groupedByStore = offers.groupBy { it.storeName }

        return groupedByStore.entries
            .sortedBy { entry -> entry.value.minOfOrNull { it.distanceKm } ?: Double.MAX_VALUE }
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

                // Pin target navigates using the authentic store ID
                val markerStoreId = firstOffer.storeId.ifBlank { "store_01" }

                NearbyDealMarkerUiModel(
                    storeId = markerStoreId,
                    storeName = entry.key,
                    labelText = label,
                    latitude = pinLat,
                    longitude = pinLng,
                    offers = storeOffers
                )
            }
    }

    // Handles map pin selection
    fun onMapMarkerClicked(markerStoreId: String?) {
        _uiState.update { current ->
            current.copy(selectedMapOfferId = markerStoreId)
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { current ->
            val updated = current.copy(searchQuery = query)
            val filtered = applyFilters(updated, allOffers)
            val baseLat = userLatitude ?: defaultLatitude
            val baseLng = userLongitude ?: defaultLongitude
            updated.copy(
                offers = filtered,
                stores = deriveStoresFromOffers(filtered, baseLat, baseLng),
                nearbyMarkers = groupOffersByStore(filtered, baseLat, baseLng)
            )
        }
    }

    fun onCategorySelected(category: DiscoveryCategory) {
        _uiState.update { current ->
            val updated = current.copy(selectedCategory = category)
            val filtered = applyFilters(updated, allOffers)
            val baseLat = userLatitude ?: defaultLatitude
            val baseLng = userLongitude ?: defaultLongitude
            updated.copy(
                offers = filtered,
                stores = deriveStoresFromOffers(filtered, baseLat, baseLng),
                nearbyMarkers = groupOffersByStore(filtered, baseLat, baseLng)
            )
        }
    }

    fun onViewModeSelected(mode: DiscoveryViewMode) {
        _uiState.update { current ->
            current.copy(viewMode = mode)
        }
    }

    // Queries the users table in Supabase to verify if ngo_status is APPROVED before unlocking free claims
    fun onUserRoleChanged(role: UserRole? = null) {
        viewModelScope.launch {
            val userId = UserSession.getUserId()
            var isApprovedNgo = false

            if (userId.isNotBlank()) {
                val ngoStatus = userRepository.fetchUserNgoStatus(userId)
                isApprovedNgo = ngoStatus.equals("APPROVED", ignoreCase = true)
            }

            _uiState.update { current ->
                val updatedCategories = if (isApprovedNgo) {
                    (current.availableCategories + DiscoveryCategory.FREE).distinct()
                } else {
                    current.availableCategories.filterNot { it == DiscoveryCategory.FREE }
                }

                val updated = current.copy(
                    userRole = UserRole.CONSUMER,
                    isNgoApproved = isApprovedNgo,
                    availableCategories = updatedCategories,
                    selectedCategory = if (
                        !isApprovedNgo && current.selectedCategory == DiscoveryCategory.FREE
                    ) DiscoveryCategory.ALL else current.selectedCategory
                )

                val filtered = applyFilters(updated, allOffers)
                val baseLat = userLatitude ?: defaultLatitude
                val baseLng = userLongitude ?: defaultLongitude

                updated.copy(
                    offers = filtered,
                    stores = deriveStoresFromOffers(filtered, baseLat, baseLng),
                    nearbyMarkers = groupOffersByStore(filtered, baseLat, baseLng)
                )
            }
        }
    }

    // Applies search queries, role-based visibility, and rescue claim window criteria
    private fun applyFilters(state: DiscoveryUiState, sourceList: List<OfferUiModel>): List<OfferUiModel> {
        val query = state.searchQuery.trim().lowercase()
        val currentLat = userLatitude
        val currentLng = userLongitude

        val filteredSequence = sourceList.asSequence()
            // 1. Availability Filter:
            // Keep all active, closed, and expired offers visible so the UI can display badges
            // and prevent purchase on the detail page as designed.
            // When filtering by FREE, restrict strictly to approved NGO eligible items.
            .filter { offer ->
                if (state.selectedCategory == DiscoveryCategory.FREE) {
                    state.isNgoApproved && offer.isEligibleForNgoFree
                } else {
                    true
                }
            }
            // 2. Category Tab Filter:
            .filter { offer ->
                when (state.selectedCategory) {
                    DiscoveryCategory.ALL -> true
                    DiscoveryCategory.BAKERY -> offer.category == DiscoveryCategory.BAKERY
                    DiscoveryCategory.HOT_MEALS -> offer.category == DiscoveryCategory.HOT_MEALS
                    DiscoveryCategory.DESSERTS -> offer.category == DiscoveryCategory.DESSERTS
                    DiscoveryCategory.BEVERAGES -> offer.category == DiscoveryCategory.BEVERAGES
                    DiscoveryCategory.FREE -> {
                        // Unpurchased rescue items become free exclusively to approved NGOs
                        state.isNgoApproved && offer.isEligibleForNgoFree
                    }
                }
            }
            // 3. Search Query Filter:
            .filter { offer ->
                query.isBlank() ||
                        offer.title.lowercase().contains(query) ||
                        offer.storeName.lowercase().contains(query)
            }

        return if (currentLat != null && currentLng != null) {
            filteredSequence
                .map { offer ->
                    val offerLat = offer.latitude ?: defaultLatitude
                    val offerLng = offer.longitude ?: defaultLongitude
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
            is DiscoveryUiEvent.OnViewModeSelected -> onViewModeSelected(event.mode)
            is DiscoveryUiEvent.OnMapMarkerClicked -> onMapMarkerClicked(event.offerId)
            is DiscoveryUiEvent.OnResetFilters -> onResetFilters()
            is DiscoveryUiEvent.OnToggleBookmark -> onToggleBookmark(event.offerId)
            is DiscoveryUiEvent.OnOpenNotifications -> markAllNotificationsAsRead()
            is DiscoveryUiEvent.OnClearAllNotifications -> clearAllNotifications()
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
            val baseLat = userLatitude ?: defaultLatitude
            val baseLng = userLongitude ?: defaultLongitude
            updated.copy(
                offers = filtered,
                stores = deriveStoresFromOffers(filtered, baseLat, baseLng),
                nearbyMarkers = groupOffersByStore(filtered, baseLat, baseLng)
            )
        }
    }

    fun refreshNotifications() {
        val currentUserId = UserSession.currentUserId.value
        if (currentUserId.isNotBlank()) {
            loadUserNotifications(currentUserId)
        }
    }

    // Updates unread notifications as read on the backend database and locally in UI
    fun markAllNotificationsAsRead() {
        val unreadIds = _uiState.value.notifications.filter { !it.isRead }.map { it.id }
        if (unreadIds.isEmpty()) return

        viewModelScope.launch {
            notificationRepository.markAsRead(unreadIds)
            _uiState.update { current ->
                val readList = current.notifications.map { it.copy(isRead = true) }
                current.copy(notifications = readList)
            }
        }
    }

    // Deletes all notifications for the active user directly from Supabase
    fun clearAllNotifications() {
        val userId = UserSession.getUserId()
        if (userId.isBlank()) return

        viewModelScope.launch {
            val isSuccess = notificationRepository.clearAllNotifications(userId)
            if (isSuccess) {
                _uiState.update { current ->
                    current.copy(notifications = emptyList())
                }
            }
        }
    }
}