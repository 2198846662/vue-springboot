<script setup>
import { computed, ref } from 'vue'
import { Calendar, Users, Ticket } from 'lucide-vue-next'
import { cartService } from '../services/orderService.js'

const props = defineProps({
  user: {
    type: Object,
    default: null
  },
  scenery: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['close', 'login', 'open-cart'])

const isSubmitting = ref(false)
const error = ref('')
const success = ref('')

const form = ref({
  bookingDays: 1,
  travelerCount: 1,
  travelDate: '',
  note: ''
})

const canSubmit = computed(() => {
  const days = Number(form.value.bookingDays || 0)
  const travelers = Number(form.value.travelerCount || 0)
  return days >= 1 && travelers >= 1 && !!props.scenery?.id
})

const submitBooking = async () => {
  if (!props.user) {
    emit('login')
    return
  }
  if (!canSubmit.value) {
    error.value = '请先完善预订信息'
    success.value = ''
    return
  }

  isSubmitting.value = true
  error.value = ''
  success.value = ''
  try {
    await cartService.addToCart({
      sceneryId: props.scenery.id,
      bookingDays: Number(form.value.bookingDays),
      travelerCount: Number(form.value.travelerCount),
      travelDate: form.value.travelDate || null,
      note: form.value.note?.trim() || null
    })
    success.value = '已加入购物车，可前往购物车进行模拟支付。'
  } catch (e) {
    error.value = e.message || '加入购物车失败'
  } finally {
    isSubmitting.value = false
  }
}
</script>

<template>
  <section class="max-w-[1200px] mx-auto px-4 md:px-6 py-8">
    <div class="rounded-2xl p-5 md:p-6" style="background: var(--bg-card); border: 1px solid var(--border);">
      <div class="flex flex-wrap items-center justify-between gap-3 mb-6">
        <div>
          <h2 class="text-2xl font-bold" style="color: var(--text-primary);">景点预订</h2>
          <p class="text-sm mt-1" style="color: var(--text-muted);">填写预订信息后加入购物车，完成模拟支付。</p>
        </div>
        <button
          class="px-4 py-2 rounded-lg text-sm font-medium"
          style="background: var(--bg-secondary); border: 1px solid var(--border); color: var(--text-secondary);"
          @click="emit('close')"
        >
          返回
        </button>
      </div>

      <div v-if="!scenery" class="py-10 text-center text-sm" style="color: var(--text-muted);">
        未选择景点，请返回景点详情页重新发起预订。
      </div>

      <template v-else>
        <div class="grid grid-cols-1 lg:grid-cols-5 gap-5">
          <div class="lg:col-span-2 p-4 rounded-xl" style="background: var(--bg-secondary); border: 1px solid var(--border);">
            <h3 class="font-semibold mb-3" style="color: var(--text-primary);">{{ scenery.title }}</h3>
            <img
              :src="scenery.images?.[0]"
              :alt="scenery.title"
              class="w-full aspect-[16/10] object-cover rounded-lg mb-3"
            />
            <p class="text-sm" style="color: var(--text-secondary);">{{ scenery.location }}</p>
            <p class="text-sm mt-2 inline-flex items-center gap-1" style="color: var(--text-secondary);">
              <Ticket class="w-4 h-4" />
              票价：{{ scenery.ticket || '免费' }}
            </p>
          </div>

          <div class="lg:col-span-3 p-4 rounded-xl" style="background: var(--bg-secondary); border: 1px solid var(--border);">
            <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
              <label class="text-sm" style="color: var(--text-secondary);">
                预订天数
                <input
                  v-model.number="form.bookingDays"
                  type="number"
                  min="1"
                  class="mt-1 w-full px-3 py-2 rounded-lg text-sm"
                  style="background: var(--bg-card); border: 1px solid var(--border); color: var(--text-primary);"
                />
              </label>
              <label class="text-sm" style="color: var(--text-secondary);">
                出行人数
                <input
                  v-model.number="form.travelerCount"
                  type="number"
                  min="1"
                  class="mt-1 w-full px-3 py-2 rounded-lg text-sm"
                  style="background: var(--bg-card); border: 1px solid var(--border); color: var(--text-primary);"
                />
              </label>
              <label class="text-sm sm:col-span-2" style="color: var(--text-secondary);">
                出行日期
                <input
                  v-model="form.travelDate"
                  type="date"
                  class="mt-1 w-full px-3 py-2 rounded-lg text-sm"
                  style="background: var(--bg-card); border: 1px solid var(--border); color: var(--text-primary);"
                />
              </label>
              <label class="text-sm sm:col-span-2" style="color: var(--text-secondary);">
                备注
                <textarea
                  v-model="form.note"
                  rows="3"
                  class="mt-1 w-full px-3 py-2 rounded-lg text-sm resize-none"
                  style="background: var(--bg-card); border: 1px solid var(--border); color: var(--text-primary);"
                  placeholder="可填写同行需求、联系方式等（选填）"
                />
              </label>
            </div>

            <div v-if="error" class="mt-3 p-3 rounded-lg text-sm bg-red-50 border border-red-200 text-red-600">{{ error }}</div>
            <div v-if="success" class="mt-3 p-3 rounded-lg text-sm bg-green-50 border border-green-200 text-green-700">{{ success }}</div>

            <div class="mt-4 flex flex-wrap gap-2">
              <button
                class="px-4 py-2 rounded-lg text-sm font-medium text-white inline-flex items-center gap-1"
                style="background: var(--primary);"
                :disabled="isSubmitting || !canSubmit"
                @click="submitBooking"
              >
                <Calendar class="w-4 h-4" />
                {{ isSubmitting ? '提交中...' : '加入购物车' }}
              </button>
              <button
                class="px-4 py-2 rounded-lg text-sm font-medium inline-flex items-center gap-1"
                style="background: var(--bg-card); border: 1px solid var(--border); color: var(--text-secondary);"
                @click="emit('open-cart')"
              >
                <Users class="w-4 h-4" />
                前往购物车
              </button>
            </div>
          </div>
        </div>
      </template>
    </div>
  </section>
</template>
