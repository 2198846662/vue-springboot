<script setup>
import { ref, watch, onMounted, onUnmounted } from 'vue'
import { MapPin } from 'lucide-vue-next'

const props = defineProps({
  sceneryList: {
    type: Array,
    default: () => []
  },
  selectedId: {
    type: [Number, String],
    default: null
  }
})

const emit = defineEmits(['markerClick'])

const mapContainer = ref(null)
const map = ref(null)
const markers = ref([])
const mapError = ref('')

// Default center (China)
const defaultCenter = [35.0, 105.0]
const defaultZoom = 4

const isValidCoordinate = (lat, lng) => {
  const latitude = Number(lat)
  const longitude = Number(lng)
  return Number.isFinite(latitude)
    && Number.isFinite(longitude)
    && latitude >= -90
    && latitude <= 90
    && longitude >= -180
    && longitude <= 180
}

const initMap = () => {
  if (!mapContainer.value || map.value) return

  // Dynamically import Leaflet
  import('leaflet').then((L) => {
    try {
      mapError.value = ''

      // 兼容热更新场景，避免 “Map container is already initialized”
      if (mapContainer.value && mapContainer.value._leaflet_id) {
        mapContainer.value._leaflet_id = null
      }

      // Create map
      map.value = L.map(mapContainer.value, {
        center: defaultCenter,
        zoom: defaultZoom,
        scrollWheelZoom: true,
        zoomControl: false
      })

      // Add tile layer (OpenStreetMap for better compatibility)
      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
        maxZoom: 19
      }).addTo(map.value)

      // Add zoom control to bottom right
      L.control.zoom({ position: 'bottomright' }).addTo(map.value)

      // Add markers
      updateMarkers()
    } catch (error) {
      console.error('Map init error:', error)
      mapError.value = '地图初始化失败，请刷新页面重试'
    }
  }).catch((error) => {
    console.error('Leaflet import error:', error)
    mapError.value = '地图组件加载失败'
  })
}

const createCustomIcon = (L, isSelected = false) => {
  return L.divIcon({
    className: 'custom-map-marker',
    html: `
      <div style="
        width: ${isSelected ? '36px' : '28px'};
        height: ${isSelected ? '36px' : '28px'};
        background: ${isSelected ? 'var(--accent)' : 'var(--primary)'};
        border: 3px solid white;
        border-radius: 50%;
        box-shadow: 0 3px 10px rgba(0,0,0,0.3);
        display: flex;
        align-items: center;
        justify-content: center;
        transition: all 0.2s ease;
        cursor: pointer;
      ">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
          <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
          <circle cx="12" cy="10" r="3"></circle>
        </svg>
      </div>
    `,
    iconSize: [isSelected ? 36 : 28, isSelected ? 36 : 28],
    iconAnchor: [isSelected ? 18 : 14, isSelected ? 36 : 28],
    popupAnchor: [0, -(isSelected ? 36 : 28)]
  })
}

const updateMarkers = ({ fitBounds = true } = {}) => {
  if (!map.value || !Array.isArray(props.sceneryList)) return

  import('leaflet').then((L) => {
    // Clear existing markers
    markers.value.forEach(m => m.remove())
    markers.value = []

    // Add new markers
    props.sceneryList.forEach(scenery => {
      if (!isValidCoordinate(scenery.latitude, scenery.longitude)) return

      const isSelected = scenery.id === props.selectedId
      const marker = L.marker([Number(scenery.latitude), Number(scenery.longitude)], {
        icon: createCustomIcon(L, isSelected)
      })

      // Tooltip
      marker.bindTooltip(`
        <div style="
          font-family: var(--font-body);
          font-size: 13px;
          font-weight: 600;
          color: var(--text-primary);
          padding: 4px 0;
        ">
          ${scenery.title}
        </div>
      `, {
        direction: 'top',
        offset: [0, -10],
        className: 'custom-tooltip'
      })

      // Click handler
      marker.on('click', () => {
        emit('markerClick', scenery)
      })

      marker.addTo(map.value)
      markers.value.push(marker)
    })

    // Fit bounds if we have markers
    if (fitBounds && props.sceneryList.length > 0 && markers.value.length > 0) {
      const group = L.featureGroup(markers.value)
      map.value.fitBounds(group.getBounds().pad(0.1))
    }
  }).catch((error) => {
    console.error('Update marker error:', error)
    mapError.value = '地图点位渲染失败，请检查景点坐标数据'
  })
}

// Watch for scenery list changes
watch(() => props.sceneryList, () => {
  updateMarkers({ fitBounds: true })
}, { deep: true })

// Watch for selected id changes
watch(() => props.selectedId, () => {
  updateMarkers({ fitBounds: false })
})

onMounted(() => {
  initMap()
})

onUnmounted(() => {
  if (map.value) {
    map.value.remove()
    map.value = null
  }
})
</script>

<template>
  <div class="map-view-container relative w-full h-full">
    <div ref="mapContainer" class="w-full h-full rounded-xl"></div>

    <div
      v-if="mapError"
      class="absolute left-3 right-3 top-3 rounded-lg px-3 py-2 text-xs"
      style="background: #fef2f2; border: 1px solid #fecaca; color: #b91c1c;"
    >
      {{ mapError }}
    </div>

    <!-- Loading State -->
    <div v-if="!map && !mapError" class="absolute inset-0 flex items-center justify-center rounded-xl"
      style="background: var(--bg-secondary);">
      <div class="text-center">
        <div class="w-12 h-12 mx-auto mb-3 rounded-full flex items-center justify-center animate-pulse"
          style="background: var(--primary);">
          <MapPin class="w-6 h-6 text-white" />
        </div>
        <p class="text-sm" style="color: var(--text-muted);">加载地图中...</p>
      </div>
    </div>
  </div>
</template>

<style>
/* Global styles for Leaflet */
.custom-map-marker {
  background: transparent !important;
  border: none !important;
}

.custom-tooltip {
  background: var(--bg-card) !important;
  border: 1px solid var(--border) !important;
  border-radius: var(--radius) !important;
  box-shadow: var(--shadow-lg) !important;
  padding: 8px 12px !important;
}

.custom-tooltip::before {
  border-top-color: var(--bg-card) !important;
}

.leaflet-popup-content-wrapper {
  border-radius: var(--radius) !important;
  box-shadow: var(--shadow-lg) !important;
}

.leaflet-control-zoom a {
  background: var(--bg-card) !important;
  color: var(--text-primary) !important;
  border: 1px solid var(--border) !important;
}

.leaflet-control-zoom a:hover {
  background: var(--bg-secondary) !important;
}
</style>
