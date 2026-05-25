<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { MapPin, Compass, Heart, User, Menu, X, Moon, Sun, ShoppingCart } from 'lucide-vue-next'

const props = defineProps({
  user: {
    type: Object,
    default: null
  },
  isAdminView: {
    type: Boolean,
    default: false
  },
  isFavoritesView: {
    type: Boolean,
    default: false
  },
  isCartView: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits([
  'login',
  'logout',
  'search',
  'open-admin',
  'close-admin',
  'open-favorites',
  'close-favorites',
  'open-cart',
  'close-cart'
])

const isDark = ref(false)
const showUserMenu = ref(false)
const showMobileMenu = ref(false)
const searchQuery = ref('')

const isLoggedIn = computed(() => !!props.user)
const isAdmin = computed(() => (props.user?.role || '').toUpperCase() === 'ADMIN')

const toggleTheme = () => {
  isDark.value = !isDark.value
  const newTheme = isDark.value ? 'dark' : 'light'
  document.documentElement.setAttribute('data-theme', newTheme)
  localStorage.setItem('theme', newTheme)
}

const handleSearch = () => {
  emit('search', searchQuery.value)
}

const handleLogout = () => {
  showUserMenu.value = false
  emit('logout')
}

const toggleUserMenu = () => {
  showUserMenu.value = !showUserMenu.value
}

const handleDocumentClick = (e) => {
  const userMenu = document.querySelector('.user-menu-container')
  if (userMenu && !userMenu.contains(e.target)) {
    showUserMenu.value = false
  }
}

const toggleFavorites = () => {
  if (!isLoggedIn.value) {
    emit('login')
    return
  }
  props.isFavoritesView ? emit('close-favorites') : emit('open-favorites')
}

const toggleCart = () => {
  if (!isLoggedIn.value) {
    emit('login')
    return
  }
  props.isCartView ? emit('close-cart') : emit('open-cart')
}

onMounted(() => {
  const savedTheme = localStorage.getItem('theme')
  if (savedTheme) {
    isDark.value = savedTheme === 'dark'
    document.documentElement.setAttribute('data-theme', savedTheme)
  }

  document.addEventListener('click', handleDocumentClick)
})

onUnmounted(() => {
  document.removeEventListener('click', handleDocumentClick)
})
</script>

<template>
  <nav class="navbar sticky top-0 z-50 transition-all duration-300"
    style="background: var(--bg-card); border-bottom: 1px solid var(--border);">
    <div class="max-w-[1600px] mx-auto px-4 md:px-6">
      <div class="flex justify-between items-center h-16 md:h-20">
        <a href="#" class="flex items-center gap-2.5 no-underline group">
          <div class="w-10 h-10 rounded-lg flex items-center justify-center transition-transform duration-300 group-hover:scale-105"
            style="background: linear-gradient(135deg, var(--primary), var(--primary-light));">
            <Compass class="w-5 h-5 text-white" />
          </div>
          <span class="text-xl md:text-2xl font-bold" style="font-family: var(--font-display);">
            Travel<span style="color: var(--accent);">Vista</span>
          </span>
        </a>

        <div class="hidden md:flex items-center gap-8">
          <a href="#" class="nav-link flex items-center gap-2 text-sm font-medium no-underline transition-colors duration-200"
            style="color: var(--text-secondary);">
            <Compass class="w-4 h-4" />
            发现
          </a>
          <a href="#" class="nav-link flex items-center gap-2 text-sm font-medium no-underline transition-colors duration-200"
            style="color: var(--text-secondary);">
            <MapPin class="w-4 h-4" />
            目的地
          </a>
          <a href="#" class="nav-link flex items-center gap-2 text-sm font-medium no-underline transition-colors duration-200"
            style="color: var(--text-secondary);"
            @click.prevent="toggleFavorites">
            <Heart class="w-4 h-4" />
            {{ props.isFavoritesView ? '返回发现' : '我的收藏' }}
          </a>
          <a href="#" class="nav-link flex items-center gap-2 text-sm font-medium no-underline transition-colors duration-200"
            style="color: var(--text-secondary);"
            @click.prevent="toggleCart">
            <ShoppingCart class="w-4 h-4" />
            {{ props.isCartView ? '返回发现' : '购物车' }}
          </a>
        </div>

        <div class="flex items-center gap-3">
          <div class="hidden md:flex items-center relative">
            <input
              v-model="searchQuery"
              type="text"
              class="search-input pl-10 pr-4 py-2 text-sm rounded-full transition-all duration-200"
              placeholder="搜索景点..."
              style="background: var(--bg-secondary); border: 1px solid var(--border); color: var(--text-primary);"
              @keyup.enter="handleSearch"
            />
            <Compass class="w-4 h-4 absolute left-3" style="color: var(--text-muted);" />
          </div>

          <button
            class="w-10 h-10 rounded-full flex items-center justify-center transition-all duration-200 hover:scale-105"
            style="background: var(--bg-secondary); border: 1px solid var(--border);"
            aria-label="切换主题"
            @click="toggleTheme"
          >
            <Moon v-if="!isDark" class="w-5 h-5" style="color: var(--text-secondary);" />
            <Sun v-else class="w-5 h-5" style="color: var(--text-secondary);" />
          </button>

          <div v-if="!isLoggedIn">
            <button
              class="px-5 py-2.5 rounded-full text-sm font-semibold transition-all duration-200 hover:scale-105"
              style="background: var(--primary); color: white;"
              @click="emit('login')"
            >
              登录
            </button>
          </div>

          <div v-else class="user-menu-container relative">
            <button
              class="flex items-center gap-2 px-3 py-2 rounded-full transition-all duration-200 hover:scale-[1.02]"
              style="background: var(--bg-secondary); border: 1px solid var(--border);"
              @click.stop="toggleUserMenu"
            >
              <div class="w-7 h-7 rounded-full flex items-center justify-center text-white text-xs font-bold"
                style="background: linear-gradient(135deg, var(--accent), var(--gold));">
                {{ user?.nickname?.charAt(0)?.toUpperCase() || 'U' }}
              </div>
              <span class="hidden md:block text-sm font-medium max-w-[80px] truncate"
                style="color: var(--text-primary);">
                {{ user?.nickname || user?.username }}
              </span>
            </button>

            <Transition
              enter-active-class="transition ease-out duration-150"
              enter-from-class="opacity-0 translate-y-1"
              enter-to-class="opacity-100 translate-y-0"
              leave-active-class="transition ease-in duration-100"
              leave-from-class="opacity-100 translate-y-0"
              leave-to-class="opacity-0 translate-y-1"
            >
              <div
                v-if="showUserMenu"
                class="absolute right-0 top-full mt-2 w-48 overflow-hidden rounded-lg"
                style="background: var(--bg-card); border: 1px solid var(--border); box-shadow: var(--shadow-lg);"
              >
                <div class="px-4 py-3" style="border-bottom: 1px solid var(--border-subtle);">
                  <p class="text-xs" style="color: var(--text-muted);">已登录</p>
                  <p class="text-sm font-medium truncate mt-0.5" style="color: var(--text-primary);">
                    {{ user?.nickname || user?.username }}
                  </p>
                </div>
                <div class="py-1">
                  <button
                    class="dropdown-item w-full text-left px-4 py-2.5 text-sm flex items-center gap-3"
                    style="color: var(--text-secondary);"
                    @click="toggleFavorites"
                  >
                    <Heart class="w-4 h-4" />
                    {{ props.isFavoritesView ? '返回发现' : '我的收藏' }}
                  </button>
                  <button
                    class="dropdown-item w-full text-left px-4 py-2.5 text-sm flex items-center gap-3"
                    style="color: var(--text-secondary);"
                    @click="toggleCart"
                  >
                    <ShoppingCart class="w-4 h-4" />
                    {{ props.isCartView ? '返回发现' : '购物车' }}
                  </button>
                  <button
                    v-if="isAdmin"
                    class="dropdown-item w-full text-left px-4 py-2.5 text-sm flex items-center gap-3"
                    style="color: var(--text-secondary);"
                    @click="props.isAdminView ? emit('close-admin') : emit('open-admin')"
                  >
                    <User class="w-4 h-4" />
                    {{ props.isAdminView ? '返回首页' : '管理后台' }}
                  </button>
                  <button
                    class="dropdown-item w-full text-left px-4 py-2.5 text-sm flex items-center gap-3"
                    style="color: var(--text-secondary);"
                    @click="handleLogout"
                  >
                    <User class="w-4 h-4" />
                    退出登录
                  </button>
                </div>
              </div>
            </Transition>
          </div>

          <button
            class="md:hidden w-10 h-10 rounded-full flex items-center justify-center"
            style="background: var(--bg-secondary); border: 1px solid var(--border);"
            @click="showMobileMenu = !showMobileMenu"
          >
            <Menu v-if="!showMobileMenu" class="w-5 h-5" style="color: var(--text-secondary);" />
            <X v-else class="w-5 h-5" style="color: var(--text-secondary);" />
          </button>
        </div>
      </div>

      <Transition
        enter-active-class="transition ease-out duration-200"
        enter-from-class="opacity-0 -translate-y-2"
        enter-to-class="opacity-100 translate-y-0"
        leave-active-class="transition ease-in duration-150"
        leave-from-class="opacity-100 translate-y-0"
        leave-to-class="opacity-0 -translate-y-2"
      >
        <div v-if="showMobileMenu" class="md:hidden py-4" style="border-top: 1px solid var(--border-subtle);">
          <div class="relative mb-4">
            <input
              v-model="searchQuery"
              type="text"
              class="w-full pl-10 pr-4 py-3 text-sm rounded-xl"
              placeholder="搜索景点..."
              style="background: var(--bg-secondary); border: 1px solid var(--border); color: var(--text-primary);"
              @keyup.enter="handleSearch"
            />
            <Compass class="w-4 h-4 absolute left-4 top-1/2 -translate-y-1/2" style="color: var(--text-muted);" />
          </div>

          <div class="space-y-1">
            <a href="#" class="mobile-nav-link flex items-center gap-3 px-4 py-3 rounded-lg text-sm font-medium no-underline"
              style="color: var(--text-secondary);">
              <Compass class="w-5 h-5" />
              发现
            </a>
            <a href="#" class="mobile-nav-link flex items-center gap-3 px-4 py-3 rounded-lg text-sm font-medium no-underline"
              style="color: var(--text-secondary);"
              @click.prevent="toggleFavorites">
              <Heart class="w-5 h-5" />
              {{ props.isFavoritesView ? '返回发现' : '我的收藏' }}
            </a>
            <a href="#" class="mobile-nav-link flex items-center gap-3 px-4 py-3 rounded-lg text-sm font-medium no-underline"
              style="color: var(--text-secondary);"
              @click.prevent="toggleCart">
              <ShoppingCart class="w-5 h-5" />
              {{ props.isCartView ? '返回发现' : '购物车' }}
            </a>
          </div>
        </div>
      </Transition>
    </div>
  </nav>
</template>

<style scoped>
.nav-link:hover {
  color: var(--primary) !important;
}

.dropdown-item {
  transition: background-color 0.15s ease;
}

.dropdown-item:hover {
  background: var(--bg-secondary);
  color: var(--primary) !important;
}

.search-input::placeholder {
  color: var(--text-muted);
}

.search-input:focus {
  outline: none;
  border-color: var(--primary);
  box-shadow: 0 0 0 3px rgba(27, 67, 50, 0.1);
}

.mobile-nav-link:hover {
  background: var(--bg-secondary);
  color: var(--primary) !important;
}
</style>
