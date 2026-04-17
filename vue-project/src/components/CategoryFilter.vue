<script setup>
import { ref, onMounted } from 'vue'
import { Mountain, Umbrella, Landmark, Waves, Trees, Sun, Droplets, Globe } from 'lucide-vue-next'
import { sceneryService } from '../services/api.js'

defineProps({
  activeCategory: {
    type: String,
    default: 'all'
  }
})

const emit = defineEmits(['change'])

const fallbackCategories = [
  { id: 'all', name: '全部', icon: 'Globe' },
  { id: '自然风光', name: '自然风光', icon: 'Mountain' },
  { id: '人文景观', name: '人文景观', icon: 'Landmark' },
  { id: '主题公园', name: '主题公园', icon: 'Umbrella' },
  { id: '博物馆', name: '博物馆', icon: 'Landmark' },
  { id: '古镇村落', name: '古镇村落', icon: 'Landmark' }
]

const categories = ref([...fallbackCategories])

const iconComponents = {
  Globe,
  Mountain,
  Umbrella,
  Landmark,
  Waves,
  Trees,
  Sun,
  Droplets
}

const normalizeIcon = (icon) => {
  const key = (icon || '').toString().trim().toLowerCase()

  if (['globe', 'all', 'global'].includes(key)) return 'Globe'
  if (['mountain', 'nature'].includes(key)) return 'Mountain'
  if (['umbrella', 'amusement', 'themepark'].includes(key)) return 'Umbrella'
  if (['waves', 'water'].includes(key)) return 'Waves'
  if (['trees', 'forest'].includes(key)) return 'Trees'
  if (['sun'].includes(key)) return 'Sun'
  if (['droplets', 'lake', 'river'].includes(key)) return 'Droplets'
  return 'Landmark'
}

const loadCategories = async () => {
  try {
    const list = await sceneryService.getCategories()

    if (!Array.isArray(list) || list.length === 0) {
      categories.value = [...fallbackCategories]
      return
    }

    const normalized = list
      .filter(item => item && item.id && item.name)
      .map(item => ({
        id: item.id,
        name: item.name,
        icon: normalizeIcon(item.icon)
      }))

    const hasAll = normalized.some(item => item.id === 'all')
    categories.value = hasAll
      ? normalized
      : [{ id: 'all', name: '全部', icon: 'Globe' }, ...normalized]
  } catch (error) {
    console.error('Failed to load categories, fallback to defaults:', error)
    categories.value = [...fallbackCategories]
  }
}

const handleSelect = (categoryId) => {
  emit('change', categoryId)
}

onMounted(() => {
  loadCategories()
})
</script>

<template>
  <div class="category-filter">
    <div class="flex flex-wrap gap-2">
      <button
        v-for="category in categories"
        :key="category.id"
        class="category-btn flex items-center gap-2 px-4 py-2 rounded-full text-sm font-medium transition-all duration-200 whitespace-nowrap"
        :class="{ 'active': activeCategory === category.id }"
        @click="handleSelect(category.id)"
      >
        <component :is="iconComponents[category.icon]" class="w-4 h-4" />
        {{ category.name }}
      </button>
    </div>
  </div>
</template>

<style scoped>
.category-btn {
  background: var(--bg-card);
  color: var(--text-secondary);
  border: 1px solid var(--border);
}

.category-btn:hover:not(.active) {
  border-color: var(--primary);
  color: var(--primary);
  background: rgba(27, 67, 50, 0.05);
}

.category-btn.active {
  background: var(--primary);
  color: white;
  border-color: var(--primary);
  box-shadow: 0 2px 8px rgba(27, 67, 50, 0.25);
}
</style>
