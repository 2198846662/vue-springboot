<script setup>
import { onMounted, ref } from 'vue'
import { Heart } from 'lucide-vue-next'
import { sceneryService } from '../services/api.js'
import SceneryCard from './SceneryCard.vue'

const props = defineProps({
  user: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['close', 'scenery-click', 'login'])

const isLoading = ref(false)
const error = ref('')
const favorites = ref([])

const handleFavoriteToggled = (item, status) => {
  if (status === 'unfavorited') {
    favorites.value = favorites.value.filter(f => f.id !== item.id)
  }
}

const loadFavorites = async () => {
  if (!props.user) {
    emit('login')
    return
  }

  isLoading.value = true
  error.value = ''
  try {
    const response = await sceneryService.getMyFavorites({ page: 0, size: 200 })
    favorites.value = response.items
  } catch (e) {
    error.value = e.message || '收藏加载失败，请稍后重试'
  } finally {
    isLoading.value = false
  }
}

onMounted(() => {
  loadFavorites()
})
</script>

<template>
  <section class="max-w-[1600px] mx-auto px-4 md:px-6 py-8">
    <div class="rounded-2xl p-5 md:p-6" style="background: var(--bg-card); border: 1px solid var(--border);">
      <div class="flex items-center justify-between mb-6">
        <div class="flex items-center gap-2">
          <Heart class="w-5 h-5" style="color: var(--accent);" />
          <h2 class="text-2xl font-bold" style="color: var(--text-primary);">我的收藏</h2>
        </div>
        <button
          class="px-4 py-2 rounded-lg text-sm font-medium"
          style="background: var(--bg-secondary); border: 1px solid var(--border); color: var(--text-secondary);"
          @click="emit('close')"
        >
          返回首页
        </button>
      </div>

      <div v-if="isLoading" class="py-12 text-center text-sm" style="color: var(--text-muted);">收藏加载中...</div>
      <div v-else-if="error" class="py-8 text-center">
        <p class="text-sm mb-3" style="color: #dc2626;">{{ error }}</p>
        <button
          class="px-4 py-2 rounded-lg text-sm font-medium text-white"
          style="background: var(--primary);"
          @click="loadFavorites"
        >
          重试
        </button>
      </div>
      <div v-else-if="favorites.length === 0" class="py-12 text-center">
        <p class="text-base font-medium" style="color: var(--text-secondary);">你还没有收藏景点</p>
        <p class="text-sm mt-1" style="color: var(--text-muted);">在景点详情页点击“收藏”即可加入这里</p>
      </div>
      <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <SceneryCard
          v-for="item in favorites"
          :key="item.id"
          :scenery="item"
          :user="props.user"
          @click="emit('scenery-click', item)"
          @login-required="emit('login')"
          @favorite-toggled="handleFavoriteToggled(item, $event)"
        />
      </div>
    </div>
  </section>
</template>
