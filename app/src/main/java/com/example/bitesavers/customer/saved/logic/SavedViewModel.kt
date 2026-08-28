package com.example.bitesavers.customer.saved.logic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bitesavers.customer.saved.data.SavedUiState
import com.example.bitesavers.customer.saved.ui.SavedUiEvent
import com.example.bitesavers.data.remote.UserSession
import com.example.bitesavers.data.repository.OfferRepository
import com.example.bitesavers.data.repository.SavedRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Manages state and business logic for the Saved Items screen.
 * Handles fetching user-specific bookmarked offers and real-time bookmark toggling.
 */
class SavedViewModel(
    private val offerRepository: OfferRepository = OfferRepository(),
    private val savedRepository: SavedRepository = SavedRepository()
) : ViewModel() {

    // Internal mutable state flow (Unidirectional Data Flow)
    private val _uiState = MutableStateFlow(SavedUiState(isLoading = true))

    // Read-only state flow observed by the Compose UI
    val uiState: StateFlow<SavedUiState> = _uiState.asStateFlow()

    init {
        // Initial setup: pull persisted bookmarks from Supabase, then listen for updates
        loadSavedItems()
        observeSavedOffers()
    }

    /**
     * Fetches the initial list of saved offer IDs for the currently logged-in user from Supabase.
     */
    private fun loadSavedItems() {
        val currentUserId = UserSession.currentUserId.value
        viewModelScope.launch {
            if (currentUserId.isNotBlank()) {
                savedRepository.loadUserSavedOffers(currentUserId)
            }
        }
    }

    /**
     * Reactively observes the saved IDs StateFlow.
     * Fetches offer details for saved IDs (including 0-quantity items to display as Sold Out).
     */
    private fun observeSavedOffers() {
        viewModelScope.launch {
            SavedRepository.savedOfferIds.collectLatest { savedIds ->
                if (savedIds.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            savedOffers = emptyList()
                        )
                    }
                    return@collectLatest
                }

                _uiState.update { it.copy(isLoading = true) }

                // Fetch offers by IDs without filtering out 0-quantity stock
                val savedOffersList = offerRepository.fetchSavedOffersByIds(savedIds)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        savedOffers = savedOffersList
                    )
                }
            }
        }
    }

    /**
     * Processes incoming user UI interactions.
     */
    fun onEvent(event: SavedUiEvent) {
        when (event) {
            is SavedUiEvent.OnToggleBookmark -> {
                val currentUserId = UserSession.currentUserId.value
                if (currentUserId.isNotBlank()) {
                    viewModelScope.launch {
                        savedRepository.toggleSaveOffer(
                            userId = currentUserId,
                            offerId = event.offerId
                        )
                    }
                }
            }
            is SavedUiEvent.OnOfferClicked -> {
                // Handled at the Navigation/Route layer
            }
        }
    }
}