<script setup>
import { onMounted, ref } from 'vue'
import { adminService } from '../services/api.js'
import { adminOrderService } from '../services/orderService.js'

const emit = defineEmits(['updated'])

const isLoading = ref(false)
const actionLoading = ref(false)
const error = ref('')
const success = ref('')

const orderItems = ref([])
const sceneryItems = ref([])

const page = ref(0)
const size = 20
const totalPages = ref(0)
const total = ref(0)

const status = ref('')
const sceneryId = ref('')
const keyword = ref('')

const statusTextMap = {
  CART: '购物车',
  PAID: '待审核',
  APPROVED: '已通过',
  REJECTED: '已驳回'
}

const canReview = (item) => item.status === 'PAID'

const statusText = (value) => statusTextMap[value] || value

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

const loadSceneryOptions = async () => {
  const response = await adminService.listScenery({ page: 0, size: 200 })
  sceneryItems.value = response.items || []
}

const loadOrders = async ({ resetPage = false } = {}) => {
  if (resetPage) page.value = 0

  isLoading.value = true
  error.value = ''
  success.value = ''
  try {
    const response = await adminOrderService.listOrders({
      page: page.value,
      size,
      status: status.value || undefined,
      sceneryId: sceneryId.value || undefined,
      keyword: keyword.value.trim() || undefined
    })
    orderItems.value = response.items
    total.value = response.total
    totalPages.value = response.totalPages
  } catch (e) {
    error.value = e.message || '订单加载失败'
  } finally {
    isLoading.value = false
  }
}

const resetFilters = async () => {
  status.value = ''
  sceneryId.value = ''
  keyword.value = ''
  await loadOrders({ resetPage: true })
}

const goPage = async (delta) => {
  const next = page.value + delta
  if (next < 0 || next >= totalPages.value) return
  page.value = next
  await loadOrders()
}

const doReview = async (item, action) => {
  if (!canReview(item)) return

  let reason = ''
  if (action === 'REJECT') {
    reason = window.prompt('请输入驳回原因（可选）', '') || ''
    if (!window.confirm(`确认驳回订单 ${item.orderNo} 吗？`)) return
  } else if (!window.confirm(`确认通过订单 ${item.orderNo} 吗？`)) {
    return
  }

  actionLoading.value = true
  error.value = ''
  success.value = ''
  try {
    await adminOrderService.reviewOrder(item.id, { action, reason })
    success.value = action === 'APPROVE' ? '订单已通过' : '订单已驳回'
    await loadOrders()
    emit('updated')
  } catch (e) {
    error.value = e.message || '审核失败'
  } finally {
    actionLoading.value = false
  }
}

onMounted(async () => {
  isLoading.value = true
  try {
    await Promise.all([loadSceneryOptions(), loadOrders()])
  } finally {
    isLoading.value = false
  }
})
</script>

<template>
  <div class="space-y-4">
    <div v-if="error" class="p-3 rounded-lg text-sm bg-red-50 border border-red-200 text-red-600">{{ error }}</div>
    <div v-if="success" class="p-3 rounded-lg text-sm bg-green-50 border border-green-200 text-green-700">{{ success }}</div>

    <div class="p-4 rounded-xl" style="background: var(--bg-secondary); border: 1px solid var(--border);">
      <div class="grid grid-cols-1 md:grid-cols-5 gap-3">
        <select
          v-model="status"
          class="px-3 py-2 rounded-lg text-sm"
          style="background: var(--bg-card); border: 1px solid var(--border); color: var(--text-primary);"
        >
          <option value="">全部状态</option>
          <option value="PAID">待审核</option>
          <option value="APPROVED">已通过</option>
          <option value="REJECTED">已驳回</option>
        </select>
        <select
          v-model="sceneryId"
          class="px-3 py-2 rounded-lg text-sm"
          style="background: var(--bg-card); border: 1px solid var(--border); color: var(--text-primary);"
        >
          <option value="">全部景点</option>
          <option v-for="item in sceneryItems" :key="item.id" :value="String(item.id)">
            {{ item.id }} - {{ item.title }}
          </option>
        </select>
        <input
          v-model="keyword"
          type="text"
          class="md:col-span-2 px-3 py-2 rounded-lg text-sm"
          style="background: var(--bg-card); border: 1px solid var(--border); color: var(--text-primary);"
          placeholder="按订单号/用户/景点关键字搜索"
        />
        <div class="flex gap-2">
          <button
            class="px-4 py-2 rounded-lg text-sm font-medium text-white"
            style="background: var(--primary);"
            :disabled="actionLoading"
            @click="loadOrders({ resetPage: true })"
          >
            查询
          </button>
          <button
            class="px-4 py-2 rounded-lg text-sm font-medium"
            style="background: var(--bg-card); border: 1px solid var(--border); color: var(--text-secondary);"
            :disabled="actionLoading"
            @click="resetFilters"
          >
            重置
          </button>
        </div>
      </div>
    </div>

    <div class="p-4 rounded-xl" style="background: var(--bg-secondary); border: 1px solid var(--border);">
      <div class="flex items-center justify-between mb-3">
        <h3 class="font-semibold" style="color: var(--text-primary);">订单列表</h3>
        <p class="text-xs" style="color: var(--text-muted);">共 {{ total }} 条</p>
      </div>

      <div v-if="isLoading" class="py-10 text-center text-sm" style="color: var(--text-muted);">加载中...</div>
      <div v-else-if="orderItems.length === 0" class="py-10 text-center text-sm" style="color: var(--text-muted);">
        暂无订单数据
      </div>

      <div v-else class="space-y-2 max-h-[620px] overflow-auto pr-1">
        <article
          v-for="item in orderItems"
          :key="item.id"
          class="p-3 rounded-lg"
          style="background: var(--bg-card); border: 1px solid var(--border-subtle);"
        >
          <div class="flex flex-wrap items-center justify-between gap-2">
            <p class="text-sm font-semibold" style="color: var(--text-primary);">
              {{ item.orderNo }} · {{ item.sceneryName }}
            </p>
            <span
              class="px-2 py-1 rounded text-xs font-medium"
              :style="item.status === 'APPROVED'
                ? 'background:#ecfdf3;color:#166534;'
                : item.status === 'REJECTED'
                  ? 'background:#fef2f2;color:#b91c1c;'
                  : item.status === 'PAID'
                    ? 'background:#eff6ff;color:#1d4ed8;'
                    : 'background:#f8fafc;color:#475569;'"
            >
              {{ statusText(item.status) }}
            </span>
          </div>
          <p class="text-xs mt-1" style="color: var(--text-muted);">
            用户：{{ item.userName || '-' }} · {{ item.bookingDays }} 天 · {{ item.travelerCount }} 人 · 金额 {{ formatAmount(item.totalAmount) }}
          </p>
          <p class="text-xs mt-1" style="color: var(--text-muted);">
            出行日期：{{ item.travelDate || '-' }} · 支付时间：{{ formatDateTime(item.paidAt) }} · 创建时间：{{ formatDateTime(item.createdAt) }}
          </p>
          <p v-if="item.note" class="text-xs mt-1" style="color: var(--text-secondary);">备注：{{ item.note }}</p>
          <p v-if="item.status === 'REJECTED'" class="text-xs mt-1 text-red-600">驳回原因：{{ item.rejectReason || '无' }}</p>

          <div v-if="canReview(item)" class="mt-2 flex gap-2">
            <button
              class="px-3 py-1.5 text-xs rounded-md"
              style="background: #ecfdf3; color: #166534;"
              :disabled="actionLoading"
              @click="doReview(item, 'APPROVE')"
            >
              通过
            </button>
            <button
              class="px-3 py-1.5 text-xs rounded-md"
              style="background: #fef2f2; color: #dc2626;"
              :disabled="actionLoading"
              @click="doReview(item, 'REJECT')"
            >
              驳回
            </button>
          </div>
        </article>
      </div>

      <div class="mt-4 flex items-center justify-end gap-2">
        <button
          class="px-3 py-1.5 rounded-lg text-xs"
          style="background: var(--bg-card); border: 1px solid var(--border); color: var(--text-secondary);"
          :disabled="page === 0 || actionLoading"
          @click="goPage(-1)"
        >
          上一页
        </button>
        <span class="text-xs" style="color: var(--text-muted);">
          第 {{ page + 1 }} / {{ Math.max(totalPages, 1) }} 页
        </span>
        <button
          class="px-3 py-1.5 rounded-lg text-xs"
          style="background: var(--bg-card); border: 1px solid var(--border); color: var(--text-secondary);"
          :disabled="page >= totalPages - 1 || actionLoading || totalPages === 0"
          @click="goPage(1)"
        >
          下一页
        </button>
      </div>
    </div>
  </div>
</template>
