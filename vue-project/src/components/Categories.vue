<script setup>
import { categories } from '../data/news.js'

const props = defineProps({
  activeCategory: {
    type: String,
    default: 'all'
  }
})

const emit = defineEmits(['select'])

const selectCategory = (categoryId) => {
  emit('select', categoryId)
}
</script>

<template>
  <div class="flex flex-wrap gap-3 mb-10">
    <button
      v-for="category in categories"
      :key="category.id"
      class="category-btn px-6 py-2.5 rounded-full text-sm font-medium transition-all duration-300"
      :class="{ 'active': activeCategory === category.id }"
      @click="selectCategory(category.id)"
    >
      {{ category.name }}
    </button>
  </div>
</template>

<style scoped>
.category-btn {
  background: var(--bg-card);
  color: var(--text-secondary);
  border: 1px solid var(--border);
  box-shadow: var(--shadow-sm);
}

.category-btn:hover:not(.active) {
  border-color: var(--primary);
  color: var(--primary);
  transform: translateY(-2px);
}

.category-btn.active {
  background: linear-gradient(135deg, var(--primary), var(--secondary));
  color: white;
  border-color: transparent;
  box-shadow: 0 4px 20px rgba(14, 165, 233, 0.35);
}
</style>