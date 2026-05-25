<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { ArrowUpDown, Grid, List, Loader2 } from 'lucide-vue-next'
import SceneryCard from './SceneryCard.vue'
import CategoryFilter from './CategoryFilter.vue'
import { sceneryService } from '../services/api.js'

const props = defineProps({
  searchQuery: {
    type: String,
    default: ''
  },
  activeCategory: {
    type: String,
    default: 'all'
  },
  sortBy: {
    type: String,
    default: 'newest'
  }
})

const emit = defineEmits(['categoryChange', 'sortChange', 'sceneryClick', 'sceneryUpdate', 'loginRequired'])

// Data state
const sceneryList = ref([])
const isLoading = ref(true)
const error = ref(null)

// Pagination
const currentPage = ref(1)
const itemsPerPage = 12
const hasMore = computed(() => sceneryList.value.length < totalCount.value)
const totalCount = ref(0)

// View mode
const viewMode = ref('grid') // 'grid' or 'list'

// Load data
const loadScenery = async () => {
  isLoading.value = true
  error.value = null

  try {
    const response = await sceneryService.getSceneryList({
      page: currentPage.value,
      limit: itemsPerPage,
      category: props.activeCategory,
      search: props.searchQuery,
      sort: props.sortBy === 'newest' ? undefined : props.sortBy
    })

    if (currentPage.value === 1) {
      sceneryList.value = response.items
    } else {
      sceneryList.value = [...sceneryList.value, ...response.items]
    }

    totalCount.value = response.total
    emit('sceneryUpdate', sceneryList.value)
  } catch (err) {
    error.value = '加载失败，请重试'
    console.error('Failed to load scenery:', err)
  } finally {
    isLoading.value = false
  }
}

// Watch for filter changes
watch([() => props.searchQuery, () => props.activeCategory, () => props.sortBy], () => {
  currentPage.value = 1
  loadScenery()
})

// Initial load
onMounted(() => {
  loadScenery()
})

// Load more
const loadMore = () => {
  if (!hasMore.value || isLoading.value) return
  currentPage.value++
  loadScenery()
}

// Handle category change
const handleCategoryChange = (categoryId) => {
  emit('categoryChange', categoryId)
}

// Handle sort change
const handleSortChange = (sort) => {
  emit('sortChange', sort)
}

// Handle scenery click
const handleSceneryClick = (scenery) => {
  emit('sceneryClick', scenery)
}
</script>

<template>
  <div class="scenery-list">
    <!-- Header: Filter + Sort + View Mode -->
    <div class="flex flex-wrap items-center justify-between gap-4 mb-6">
      <!-- Category Filter -->
      <CategoryFilter
        :active-category="activeCategory"
        @change="handleCategoryChange"
      />

      <!-- Right side controls -->
      <div class="flex items-center gap-3">
        <!-- Sort Dropdown -->
        <div class="relative">
          <select
            :value="sortBy"
            class="sort-select pl-3 pr-8 py-2 text-sm rounded-lg appearance-none cursor-pointer"
            style="background: var(--bg-card); border: 1px solid var(--border); color: var(--text-secondary);"
            @change="handleSortChange($event.target.value)"
          >
            <option value="newest">最新</option>
            <option value="rating">评分最高</option>
            <option value="reviews">评论最多</option>
          </select>
          <ArrowUpDown class="w-4 h-4 absolute right-3 top-1/2 -translate-y-1/2 pointer-events-none" style="color: var(--text-muted);" />
        </div>

        <!-- View Mode Toggle (Desktop) -->
        <div class="hidden md:flex items-center gap-1 p-1 rounded-lg" style="background: var(--bg-card); border: 1px solid var(--border);">
          <button
            class="p-2 rounded-md transition-colors duration-150"
            :class="viewMode === 'grid' ? 'bg-[var(--primary)] text-white' : 'text-[var(--text-muted)] hover:text-[var(--text-primary)]'"
            @click="viewMode = 'grid'"
          >
            <Grid class="w-4 h-4" />
          </button>
          <button
            class="p-2 rounded-md transition-colors duration-150"
            :class="viewMode === 'list' ? 'bg-[var(--primary)] text-white' : 'text-[var(--text-muted)] hover:text-[var(--text-primary)]'"
            @click="viewMode = 'list'"
          >
            <List class="w-4 h-4" />
          </button>
        </div>
      </div>
    </div>

    <!-- Results count -->
    <div class="mb-6 text-sm" style="color: var(--text-muted);">
      <span v-if="!isLoading">找到 </span>
      <span v-if="!isLoading" class="font-semibold" style="color: var(--text-primary);">{{ totalCount }}</span>
      <span v-if="!isLoading"> 个目的地</span>
    </div>

    <!-- Loading State -->
    <div v-if="isLoading && sceneryList.length === 0" class="grid grid-cols-1 md:grid-cols-2 gap-6">
      <div v-for="i in 4" :key="i" class="skeleton-card">
        <div class="aspect-[16/10] rounded-xl skeleton-img"></div>
        <div class="mt-4 space-y-2">
          <div class="skeleton-line w-3/4 h-5"></div>
          <div class="skeleton-line w-1/2 h-4"></div>
          <div class="skeleton-line w-full h-4"></div>
        </div>
      </div>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="text-center py-16">
      <p class="text-lg mb-4" style="color: var(--text-secondary);">{{ error }}</p>
      <button
        class="px-6 py-3 rounded-xl font-medium text-white transition-all hover:scale-105"
        style="background: var(--primary);"
        @click="loadScenery"
      >
        重试
      </button>
    </div>

    <!-- Empty State -->
    <div v-else-if="sceneryList.length === 0" class="text-center py-16">
      <div class="w-24 h-24 mx-auto mb-6 rounded-full flex items-center justify-center" style="background: var(--bg-card);">
        <svg class="w-12 h-12" style="color: var(--text-muted);" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9.172 16.172a4 4 0 015.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
      </div>
      <h3 class="text-xl font-semibold mb-2" style="color: var(--text-primary);">暂无相关景点</h3>
      <p class="text-sm" style="color: var(--text-muted);">尝试调整筛选条件或搜索关键词</p>
    </div>

    <!-- Grid View -->
    <div v-else-if="viewMode === 'grid'" class="grid grid-cols-1 md:grid-cols-2 gap-6">
      <SceneryCard
        v-for="scenery in sceneryList"
        :key="scenery.id"
        :scenery="scenery"
        @click="handleSceneryClick"
        @login-required="$emit('loginRequired')"
      />
    </div>

    <!-- List View -->
    <div v-else class="space-y-4">
      <div
        v-for="scenery in sceneryList"
        :key="scenery.id"
        class="list-item flex gap-4 p-4 rounded-xl cursor-pointer transition-all duration-200"
        style="background: var(--bg-card); border: 1px solid var(--border);"
        @click="handleSceneryClick(scenery)"
      >
        <img
          :src="scenery.images[0]"
          :alt="scenery.title"
          class="w-32 h-24 object-cover rounded-lg flex-shrink-0"
        />
        <div class="flex-1 min-w-0">
          <h3 class="text-base font-semibold mb-1 truncate" style="color: var(--text-primary);">
            {{ scenery.title }}
          </h3>
          <p class="text-xs mb-2 truncate" style="color: var(--text-muted);">
            {{ scenery.location }}
          </p>
          <p class="text-sm line-clamp-2" style="color: var(--text-secondary);">
            {{ scenery.description }}
          </p>
          <div class="flex items-center gap-3 mt-2">
            <span class="flex items-center gap-1 text-xs" style="color: var(--gold);">
              <svg class="w-3 h-3 fill-current" viewBox="0 0 24 24"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/></svg>
              {{ scenery.rating }}
            </span>
            <span class="text-xs" style="color: var(--text-muted);">{{ scenery.reviewCount }} 评论</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Load More -->
    <div v-if="hasMore && !isLoading && sceneryList.length > 0" class="text-center mt-10">
      <button
        class="load-more-btn px-8 py-3 rounded-xl text-sm font-medium transition-all duration-200 hover:scale-105"
        @click="loadMore"
      >
        <Loader2 v-if="isLoading" class="w-5 h-5 animate-spin inline mr-2" />
        {{ isLoading ? '加载中...' : '加载更多' }}
      </button>
    </div>

    <!-- All loaded -->
    <p v-if="!hasMore && sceneryList.length > 0" class="text-center text-sm mt-10" style="color: var(--text-muted);">
      — 已展示全部 {{ totalCount }} 个景点 —
    </p>
  </div>
</template>

<style scoped>
.sort-select:focus {
  outline: none;
  border-color: var(--primary);
}

.skeleton-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: 1rem;
}

.skeleton-img {
  background: linear-gradient(90deg, var(--bg-secondary) 25%, var(--border) 50%, var(--bg-secondary) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.skeleton-line {
  background: linear-gradient(90deg, var(--bg-secondary) 25%, var(--border) 50%, var(--bg-secondary) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 4px;
}

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

.list-item:hover {
  border-color: var(--border-accent) !important;
  box-shadow: var(--shadow);
}

.load-more-btn {
  background: var(--bg-card);
  border: 1px solid var(--border);
  color: var(--text-secondary);
}

.load-more-btn:hover {
  background: var(--primary);
  color: white;
  border-color: var(--primary);
}

.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
