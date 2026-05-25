<script setup>
import { ref, onMounted, provide } from 'vue'
import Navbar from './components/Navbar.vue'
import Hero from './components/Hero.vue'
import MapView from './components/MapView.vue'
import SceneryList from './components/SceneryList.vue'
import Footer from './components/Footer.vue'
import SceneryDetail from './views/SceneryDetail.vue'
import LoginRegister from './components/LoginRegister.vue'
import AdminPanel from './components/AdminPanel.vue'
import FavoritesPanel from './components/FavoritesPanel.vue'
import CartPanel from './components/CartPanel.vue'
import BookingPanel from './components/BookingPanel.vue'

// Auth state
const currentUser = ref(null)
const showLoginModal = ref(false)
const showAdminPanel = ref(false)
const showFavoritesPanel = ref(false)
const showCartPanel = ref(false)
const showBookingPanel = ref(false)

// Selected scenery for detail view
const selectedScenery = ref(null)
const showDetail = ref(false)
const bookingScenery = ref(null)

// Search and filter state
const searchQuery = ref('')
const activeCategory = ref('all')
const sortBy = ref('newest')

// Scenery data
const sceneryList = ref([])
const sceneryListKey = ref(0)

const isAdmin = () => (currentUser.value?.role || '').toUpperCase() === 'ADMIN'

// Check if user is logged in on mount
onMounted(() => {
  const savedUser = localStorage.getItem('currentUser')
  const rememberMe = localStorage.getItem('rememberMe')

  if (savedUser && rememberMe) {
    try {
      currentUser.value = JSON.parse(savedUser)
    } catch (error) {
      console.warn('Invalid currentUser in localStorage, clearing stale data.', error)
      localStorage.removeItem('currentUser')
      localStorage.removeItem('rememberMe')
    }
  }
})

// Provide auth state to child components
provide('currentUser', currentUser)

// Handlers
const openLoginModal = () => {
  showLoginModal.value = true
}

const handleLoginSuccess = (user) => {
  currentUser.value = user
  showLoginModal.value = false
}

const handleLogout = () => {
  currentUser.value = null
  showAdminPanel.value = false
  showFavoritesPanel.value = false
  showCartPanel.value = false
  showBookingPanel.value = false
  localStorage.removeItem('currentUser')
  localStorage.removeItem('rememberMe')
}

const openAdminPanel = () => {
  if (!isAdmin()) return
  showFavoritesPanel.value = false
  showCartPanel.value = false
  showBookingPanel.value = false
  showAdminPanel.value = true
}

const closeAdminPanel = () => {
  showAdminPanel.value = false
}

const openFavoritesPanel = () => {
  if (!currentUser.value) {
    openLoginModal()
    return
  }
  showAdminPanel.value = false
  showCartPanel.value = false
  showBookingPanel.value = false
  showFavoritesPanel.value = true
}

const closeFavoritesPanel = () => {
  showFavoritesPanel.value = false
}

const openCartPanel = () => {
  if (!currentUser.value) {
    openLoginModal()
    return
  }
  showAdminPanel.value = false
  showFavoritesPanel.value = false
  showBookingPanel.value = false
  showCartPanel.value = true
}

const closeCartPanel = () => {
  showCartPanel.value = false
}

const openBookingPanel = (scenery) => {
  if (!currentUser.value) {
    openLoginModal()
    return
  }
  bookingScenery.value = scenery || selectedScenery.value
  showDetail.value = false
  document.body.style.overflow = ''
  showAdminPanel.value = false
  showFavoritesPanel.value = false
  showCartPanel.value = false
  showBookingPanel.value = true
}

const closeBookingPanel = () => {
  showBookingPanel.value = false
}

const handleAdminUpdated = () => {
  sceneryListKey.value += 1
}

const handleSearch = (query) => {
  searchQuery.value = query
}

const handleCategorySelect = (categoryId) => {
  activeCategory.value = categoryId
}

const handleSortChange = (sort) => {
  sortBy.value = sort
}

const handleScenerySelect = (scenery) => {
  selectedScenery.value = scenery
  showDetail.value = true
  document.body.style.overflow = 'hidden'
}

const handleCloseDetail = () => {
  showDetail.value = false
  selectedScenery.value = null
  document.body.style.overflow = ''
}

const handleMapMarkerClick = (scenery) => {
  handleScenerySelect(scenery)
}

const handleSceneryRated = ({ sceneryId, rating }) => {
  const normalizedId = Number(sceneryId)
  const normalizedRating = Number(rating ?? 0)
  if (!Number.isFinite(normalizedId) || !Number.isFinite(normalizedRating)) return

  if (selectedScenery.value && Number(selectedScenery.value.id) === normalizedId) {
    selectedScenery.value = {
      ...selectedScenery.value,
      rating: normalizedRating
    }
  }

  if (Array.isArray(sceneryList.value)) {
    sceneryList.value = sceneryList.value.map(item => {
      if (Number(item.id) !== normalizedId) return item
      return {
        ...item,
        rating: normalizedRating
      }
    })
  }
}
</script>

<template>
  <div class="app-container min-h-screen flex flex-col">
    <!-- Background Atmosphere -->
    <div class="bg-atmosphere"></div>
    <div class="bg-texture"></div>

    <!-- Navbar -->
    <Navbar
      :user="currentUser"
      :is-admin-view="showAdminPanel"
      :is-favorites-view="showFavoritesPanel"
      :is-cart-view="showCartPanel"
      @login="openLoginModal"
      @logout="handleLogout"
      @search="handleSearch"
      @open-admin="openAdminPanel"
      @close-admin="closeAdminPanel"
      @open-favorites="openFavoritesPanel"
      @close-favorites="closeFavoritesPanel"
      @open-cart="openCartPanel"
      @close-cart="closeCartPanel"
    />

    <!-- Main Content -->
    <main class="flex-1">
      <template v-if="showAdminPanel && isAdmin()">
        <AdminPanel @close="closeAdminPanel" @updated="handleAdminUpdated" />
      </template>
      <template v-else-if="showFavoritesPanel">
        <FavoritesPanel
          :user="currentUser"
          @close="closeFavoritesPanel"
          @scenery-click="handleScenerySelect"
          @login="openLoginModal"
        />
      </template>
      <template v-else-if="showCartPanel">
        <CartPanel
          :user="currentUser"
          @close="closeCartPanel"
          @login="openLoginModal"
        />
      </template>
      <template v-else-if="showBookingPanel">
        <BookingPanel
          :user="currentUser"
          :scenery="bookingScenery"
          @close="closeBookingPanel"
          @login="openLoginModal"
          @open-cart="() => { closeBookingPanel(); openCartPanel() }"
        />
      </template>
      <template v-else>
        <!-- Hero Section -->
        <Hero @search="handleSearch" />

        <!-- Map + List Section -->
        <section class="map-list-section">
          <div class="max-w-[1600px] mx-auto px-4 md:px-6 py-8">
            <div class="map-list-container">
              <!-- Map View (Left) -->
              <div class="map-wrapper">
                <MapView
                  :scenery-list="sceneryList"
                  :selected-id="selectedScenery?.id"
                  @marker-click="handleMapMarkerClick"
                />
              </div>

              <!-- Scenery List (Right) -->
              <div class="list-wrapper">
                <SceneryList
                  :key="sceneryListKey"
                  :search-query="searchQuery"
                  :active-category="activeCategory"
                  :sort-by="sortBy"
                  @category-change="handleCategorySelect"
                  @sort-change="handleSortChange"
                  @scenery-click="handleScenerySelect"
                  @scenery-update="(list) => sceneryList = list"
                />
              </div>
            </div>
          </div>
        </section>
      </template>
    </main>

    <!-- Footer -->
    <Footer />

    <!-- Scenery Detail Modal -->
    <SceneryDetail
      v-if="showDetail"
      :scenery="selectedScenery"
      :user="currentUser"
      @close="handleCloseDetail"
      @login="openLoginModal"
      @rated="handleSceneryRated"
      @book="openBookingPanel(selectedScenery)"
    />

    <!-- Login Modal -->
    <LoginRegister
      v-model="showLoginModal"
      @login-success="handleLoginSuccess"
    />
  </div>
</template>

<style scoped>
.app-container {
  position: relative;
}

.map-list-section {
  background: var(--bg-secondary);
}

.map-list-container {
  display: grid;
  grid-template-columns: 1fr;
  gap: 1.5rem;
}

@media (min-width: 1024px) {
  .map-list-container {
    grid-template-columns: 45% 55%;
  }
}

.map-wrapper {
  position: relative;
  height: 500px;
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow);
}

@media (min-width: 1024px) {
  .map-wrapper {
    position: sticky;
    top: 100px;
    height: calc(100vh - 140px);
    max-height: 700px;
  }
}

.list-wrapper {
  min-height: 500px;
}
</style>
