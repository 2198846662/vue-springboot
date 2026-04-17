<script setup>
import { ref, onMounted } from 'vue'
import { Search, Sparkles } from 'lucide-vue-next'
import { featuredScenery } from '../data/scenery.js'

const emit = defineEmits(['search'])

const searchQuery = ref('')
const currentSlide = ref(0)

const handleSearch = () => {
  emit('search', searchQuery.value)
}

onMounted(() => {
  // Auto-rotate featured images
  setInterval(() => {
    currentSlide.value = (currentSlide.value + 1) % featuredScenery.length
  }, 6000)
})
</script>

<template>
  <section class="hero-section relative overflow-hidden">
    <!-- Background Slides -->
    <div class="absolute inset-0">
      <TransitionGroup name="slide-fade" tag="div" class="relative h-full">
        <div
          v-for="(scenery, index) in featuredScenery"
          :key="scenery.id"
          v-show="index === currentSlide"
          class="absolute inset-0"
        >
          <img
            :src="scenery.images[0]"
            :alt="scenery.title"
            class="w-full h-full object-cover"
          />
          <!-- Gradient Overlay -->
          <div class="absolute inset-0 bg-gradient-to-t from-black/70 via-black/30 to-transparent"></div>
        </div>
      </TransitionGroup>
    </div>

    <!-- Content -->
    <div class="relative z-10 max-w-[1600px] mx-auto px-4 md:px-6 py-16 md:py-24">
      <div class="max-w-2xl">
        <!-- Eyebrow -->
        <div class="flex items-center gap-2 mb-4">
          <Sparkles class="w-5 h-5" style="color: var(--gold);" />
          <span class="text-sm font-medium tracking-wider uppercase" style="color: rgba(255,255,255,0.8);">
            发现世界之美
          </span>
        </div>

        <!-- Title -->
        <h1 class="text-4xl md:text-5xl lg:text-6xl font-bold text-white mb-4 leading-tight"
          style="font-family: var(--font-display);">
          {{ featuredScenery[currentSlide]?.title }}
        </h1>

        <!-- Subtitle -->
        <p class="text-lg md:text-xl text-white/80 mb-8 leading-relaxed max-w-xl">
          {{ featuredScenery[currentSlide]?.description?.slice(0, 80) }}...
        </p>

        <!-- Search Box -->
        <div class="relative max-w-xl">
          <input
            v-model="searchQuery"
            type="text"
            class="hero-search w-full pl-14 pr-14 py-4 md:py-5 rounded-2xl text-base md:text-lg"
            placeholder="搜索目的地、景点..."
            style="background: rgba(255,255,255,0.95); color: var(--text-primary);"
            @keyup.enter="handleSearch"
          />
          <Search class="w-6 h-6 absolute left-5 top-1/2 -translate-y-1/2" style="color: var(--text-muted);" />
          <button
            class="absolute right-2 top-1/2 -translate-y-1/2 px-5 py-2.5 rounded-xl text-white text-sm font-semibold transition-all duration-200 hover:scale-105"
            style="background: var(--primary);"
            @click="handleSearch"
          >
            搜索
          </button>
        </div>

        <!-- Dots Indicator -->
        <div class="flex items-center gap-2 mt-8">
          <button
            v-for="(_, index) in featuredScenery"
            :key="index"
            class="dot h-2 rounded-full transition-all duration-300"
            :class="index === currentSlide ? 'w-8' : 'w-2'"
            :style="index === currentSlide ? 'background: var(--accent);' : 'background: rgba(255,255,255,0.4);'"
            @click="currentSlide = index"
          />
        </div>
      </div>
    </div>

    <!-- Bottom Fade -->
    <div class="absolute bottom-0 left-0 right-0 h-24 bg-gradient-to-t from-[var(--bg-secondary)] to-transparent"></div>
  </section>
</template>

<style scoped>
.hero-section {
  min-height: 500px;
  max-height: 650px;
}

@media (min-width: 768px) {
  .hero-section {
    min-height: 550px;
    max-height: 700px;
  }
}

.hero-search {
  box-shadow: 0 8px 32px rgba(0,0,0,0.2);
}

.hero-search:focus {
  outline: none;
  box-shadow: 0 8px 40px rgba(0,0,0,0.3);
}

.slide-fade-enter-active {
  transition: all 0.8s ease-out;
}

.slide-fade-leave-active {
  transition: all 0.5s ease-in;
}

.slide-fade-enter-from {
  opacity: 0;
  transform: scale(1.05);
}

.slide-fade-leave-to {
  opacity: 0;
  transform: scale(0.95);
}
</style>
