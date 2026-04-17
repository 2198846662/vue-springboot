<script setup>
import { computed, onMounted, ref } from 'vue'
import { ShoppingCart, CreditCard, Calendar, Users, MapPin, Trash2 } from 'lucide-vue-next'
import { cartService, orderService } from '../services/orderService.js'

const props = defineProps({
  user: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['close', 'login', 'scenery-click'])

const isLoading = ref(false)
const actionLoading = ref(false)
const error = ref('')
const success = ref('')

const cartItems = ref([])
const orders = ref([])
const orderStatus = ref('')

const statusLabelMap = {
  PAID: '待审核',
  APPROVED: '已通过',
  REJECTED: '已驳回'
}

const cartTotalAmount = computed(() =>
  cartItems.value.reduce((sum, item) => sum + Number(item.totalAmount || 0), 0)
)

const formatAmount = (value) => {
  const num = Number(value || 0)
  return `¥${num.toFixed(2)}`
}

const formatDateTime = (value) => {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return date.toLocaleString('zh-CN', { hour12: false })
}

const statusText = (status) => statusLabelMap[status] || status

const loadCart = async () => {
  cartItems.value = await cartService.listCart()
}

const loadOrders = async () => {
  const response = await orderService.listMyOrders({
    page: 0,
    size: 50,
    status: orderStatus.value || undefined
  })
  orders.value = response.items
}

const loadAll = async () => {
  if (!props.user) {
    emit('login')
    return
  }

  isLoading.value = true
  error.value = ''
  try {
    await Promise.all([loadCart(), loadOrders()])
  } catch (e) {
    error.value = e.message || '购物车加载失败'
  } finally {
    isLoading.value = false
  }
}

const removeItem = async (item) => {
  if (!window.confirm(`确认删除该购物车项吗？\n${item.sceneryName}`)) return

  actionLoading.value = true
  error.value = ''
  success.value = ''
  try {
    await cartService.removeCartItem(item.id)
    success.value = '已删除购物车项'
    await loadCart()
  } catch (e) {
    error.value = e.message || '删除失败'
  } finally {
    actionLoading.value = false
  }
}

const payAll = async () => {
  if (cartItems.value.length === 0) {
    error.value = '购物车为空，无需支付'
    return
  }
  if (!window.confirm(`确认模拟支付 ${cartItems.value.length} 项预订吗？`)) return

  actionLoading.value = true
  error.value = ''
  success.value = ''
  try {
    const result = await cartService.payCart()
    success.value = `支付成功：${result.paidCount} 项，合计 ${formatAmount(result.totalAmount)}`
    await Promise.all([loadCart(), loadOrders()])
  } catch (e) {
    error.value = e.message || '支付失败'
  } finally {
    actionLoading.value = false
  }
}

onMounted(() => {
  loadAll()
})
</script>

<template>
  <section class="max-w-[1600px] mx-auto px-4 md:px-6 py-8">
    <div class="rounded-2xl p-5 md:p-6" style="background: var(--bg-card); border: 1px solid var(--border);">
      <div class="flex flex-wrap items-center justify-between gap-3 mb-6">
        <div class="flex items-center gap-2">
          <ShoppingCart class="w-5 h-5" style="color: var(--accent);" />
          <h2 class="text-2xl font-bold" style="color: var(--text-primary);">购物车与订单</h2>
        </div>
        <button
          class="px-4 py-2 rounded-lg text-sm font-medium"
          style="background: var(--bg-secondary); border: 1px solid var(--border); color: var(--text-secondary);"
          @click="emit('close')"
        >
          返回首页
        </button>
      </div>

      <div v-if="error" class="mb-4 p-3 rounded-lg text-sm bg-red-50 border border-red-200 text-red-600">{{ error }}</div>
      <div v-if="success" class="mb-4 p-3 rounded-lg text-sm bg-green-50 border border-green-200 text-green-700">{{ success }}</div>

      <div v-if="isLoading" class="py-12 text-center text-sm" style="color: var(--text-muted);">加载中...</div>

      <template v-else>
        <div class="grid grid-cols-1 xl:grid-cols-5 gap-5">
          <div class="xl:col-span-3 p-4 rounded-xl" style="background: var(--bg-secondary); border: 1px solid var(--border);">
            <div class="flex items-center justify-between mb-3">
              <h3 class="font-semibold" style="color: var(--text-primary);">购物车</h3>
              <p class="text-xs" style="color: var(--text-muted);">共 {{ cartItems.length }} 项</p>
            </div>

            <div v-if="cartItems.length === 0" class="py-10 text-center text-sm" style="color: var(--text-muted);">
              购物车还是空的，去景点详情页添加预订吧
            </div>

            <div v-else class="space-y-3 max-h-[620px] overflow-auto pr-1">
              <article
                v-for="item in cartItems"
                :key="item.id"
                class="p-3 rounded-lg flex gap-3 justify-between"
                style="background: var(--bg-card); border: 1px solid var(--border-subtle);"
              >
                <div class="min-w-0">
                  <button
                    class="text-sm font-semibold truncate text-left hover:underline"
                    style="color: var(--text-primary);"
                    @click="emit('scenery-click', { id: item.sceneryId, title: item.sceneryName })"
                  >
                    {{ item.sceneryName }}
                  </button>
                  <p class="text-xs mt-1 flex flex-wrap items-center gap-3" style="color: var(--text-muted);">
                    <span class="inline-flex items-center gap-1"><Calendar class="w-3.5 h-3.5" /> {{ item.bookingDays }} 天</span>
                    <span class="inline-flex items-center gap-1"><Users class="w-3.5 h-3.5" /> {{ item.travelerCount }} 人</span>
                    <span class="inline-flex items-center gap-1"><MapPin class="w-3.5 h-3.5" /> {{ item.sceneryLocation || '未填写地点' }}</span>
                  </p>
                  <p v-if="item.travelDate" class="text-xs mt-1" style="color: var(--text-muted);">出行日期：{{ item.travelDate }}</p>
                  <p v-if="item.note" class="text-xs mt-1" style="color: var(--text-secondary); white-space: pre-wrap;">备注：{{ item.note }}</p>
                  <p class="text-xs mt-2" style="color: var(--text-muted);">
                    单价 {{ formatAmount(item.unitPrice) }} · 小计 <span style="color: var(--text-primary); font-weight: 600;">{{ formatAmount(item.totalAmount) }}</span>
                  </p>
                </div>
                <button
                  class="px-3 h-8 text-xs rounded-md inline-flex items-center gap-1 shrink-0"
                  style="background: #fef2f2; color: #dc2626;"
                  :disabled="actionLoading"
                  @click="removeItem(item)"
                >
                  <Trash2 class="w-3.5 h-3.5" /> 删除
                </button>
              </article>
            </div>
          </div>

          <div class="xl:col-span-2 p-4 rounded-xl flex flex-col gap-4" style="background: var(--bg-secondary); border: 1px solid var(--border);">
            <h3 class="font-semibold" style="color: var(--text-primary);">支付汇总</h3>
            <p class="text-sm" style="color: var(--text-secondary);">当前购物车金额</p>
            <p class="text-3xl font-bold" style="color: var(--text-primary);">{{ formatAmount(cartTotalAmount) }}</p>
            <button
              class="px-4 py-2.5 rounded-lg text-sm font-medium text-white inline-flex items-center justify-center gap-2"
              style="background: var(--primary);"
              :disabled="actionLoading || cartItems.length === 0"
              @click="payAll"
            >
              <CreditCard class="w-4 h-4" /> 模拟支付并提交审核
            </button>
            <p class="text-xs" style="color: var(--text-muted);">支付后订单会进入“待审核”，等待管理员通过或驳回。</p>
          </div>
        </div>

        <div class="mt-6 p-4 rounded-xl" style="background: var(--bg-secondary); border: 1px solid var(--border);">
          <div class="flex flex-wrap items-center justify-between gap-3 mb-4">
            <h3 class="font-semibold" style="color: var(--text-primary);">我的订单</h3>
            <select
              v-model="orderStatus"
              class="px-3 py-2 rounded-lg text-sm"
              style="background: var(--bg-card); border: 1px solid var(--border); color: var(--text-primary);"
              @change="loadOrders"
            >
              <option value="">全部状态</option>
              <option value="PAID">待审核</option>
              <option value="APPROVED">已通过</option>
              <option value="REJECTED">已驳回</option>
            </select>
          </div>

          <div v-if="orders.length === 0" class="py-10 text-center text-sm" style="color: var(--text-muted);">
            暂无订单记录
          </div>

          <div v-else class="space-y-2 max-h-[520px] overflow-auto pr-1">
            <article
              v-for="item in orders"
              :key="item.id"
              class="p-3 rounded-lg"
              style="background: var(--bg-card); border: 1px solid var(--border-subtle);"
            >
              <div class="flex flex-wrap items-center justify-between gap-2">
                <p class="text-sm font-semibold" style="color: var(--text-primary);">
                  {{ item.sceneryName }} · {{ item.orderNo }}
                </p>
                <span
                  class="px-2 py-1 rounded text-xs font-medium"
                  :style="item.status === 'APPROVED'
                    ? 'background:#ecfdf3;color:#166534;'
                    : item.status === 'REJECTED'
                      ? 'background:#fef2f2;color:#b91c1c;'
                      : 'background:#eff6ff;color:#1d4ed8;'"
                >
                  {{ statusText(item.status) }}
                </span>
              </div>
              <p class="text-xs mt-1" style="color: var(--text-muted);">
                {{ item.bookingDays }} 天 · {{ item.travelerCount }} 人 · 金额 {{ formatAmount(item.totalAmount) }}
              </p>
              <p class="text-xs mt-1" style="color: var(--text-muted);">支付时间：{{ formatDateTime(item.paidAt) }}</p>
              <p v-if="item.status === 'REJECTED'" class="text-xs mt-1 text-red-600">驳回原因：{{ item.rejectReason || '无' }}</p>
            </article>
          </div>
        </div>
      </template>
    </div>
  </section>
</template>
