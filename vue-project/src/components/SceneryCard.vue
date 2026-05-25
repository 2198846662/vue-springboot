<script setup>
import { Star, MapPin, Clock } from 'lucide-vue-next'

const props = defineProps({
  scenery: {
    type: Object,
    required: true
  },
  user: {
    type: Object,
    default: null
  }
})

defineEmits(['click', 'loginRequired', 'favoriteToggled'])

const formatRating = (rating) => {
  const num = Number(rating ?? 0)
  return Number.isFinite(num) ? num.toFixed(1) : '0.0'
}
</script>

<template>
  <article
    class="scenery-card group cursor-pointer"
    @click="$emit('click', props.scenery)"
  >
    <div class="relative aspect-[16/10] overflow-hidden rounded-xl mb-4">
      <img
        :src="props.scenery.images[0]"
        :alt="props.scenery.title"
        class="w-full h-full object-cover transition-transform duration-700 group-hover:scale-105"
        loading="lazy"
      />

      <span
        class="absolute top-3 left-3 px-3 py-1 text-xs font-semibold rounded-full"
        style="background: rgba(255,255,255,0.95); color: var(--primary);"
      >
        {{ props.scenery.categoryName }}
      </span>
    </div>

    <div class="space-y-2">
      <h3
        class="text-lg font-semibold leading-tight line-clamp-1 group-hover:text-[var(--accent)] transition-colors duration-200"
        style="font-family: var(--font-display); color: var(--text-primary);"
      >
        {{ props.scenery.title }}
      </h3>

      <div class="flex items-center gap-1.5 text-sm" style="color: var(--text-muted);">
        <MapPin class="w-4 h-4 flex-shrink-0" />
        <span class="truncate">{{ props.scenery.location }}</span>
      </div>

      <p class="text-sm leading-relaxed line-clamp-2" style="color: var(--text-secondary);">
        {{ props.scenery.description }}
      </p>

      <div class="flex items-center justify-between pt-2">
        <div class="flex items-center gap-1.5">
          <Star class="w-4 h-4 fill-current" style="color: var(--gold);" />
          <span class="text-sm font-semibold" style="color: var(--text-primary);">
            {{ formatRating(props.scenery.rating) }}
          </span>
        </div>

        <div class="flex items-center gap-1.5 text-xs" style="color: var(--text-muted);">
          <Clock class="w-3.5 h-3.5" />
          <span class="hidden sm:inline truncate">{{ props.scenery.openingHours }}</span>
        </div>
      </div>
    </div>
  </article>
</template>

<style scoped>
.scenery-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: 1rem;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid transparent;
}

.scenery-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
  border-color: var(--border-accent);
}

.line-clamp-1 {
  display: -webkit-box;
  line-clamp: 1;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.line-clamp-2 {
  display: -webkit-box;
  line-clamp: 2;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
