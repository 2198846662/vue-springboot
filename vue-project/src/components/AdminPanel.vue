<script setup>
import { computed, onMounted, ref } from 'vue'
import { adminService, sceneryService } from '../services/api.js'
import AdminOrderManager from './AdminOrderManager.vue'

const emit = defineEmits(['close', 'updated'])

const activeTab = ref('scenery')
const isLoading = ref(false)
const actionLoading = ref(false)
const error = ref('')
const success = ref('')

const categories = ref([])
const sceneryItems = ref([])

const comments = ref([])
const commentPage = ref(0)
const commentSize = 20
const commentTotal = ref(0)
const commentKeyword = ref('')
const commentSceneryId = ref('')

const editingCategoryId = ref(null)
const categoryForm = ref({ name: '' })

const editingSceneryId = ref(null)
const sceneryForm = ref({
  name: '',
  description: '',
  coverImage: '',
  location: '',
  ticketPrice: '',
  openTime: '',
  categoryId: '',
  rating: '',
  latitude: '',
  longitude: ''
})

const canSubmitScenery = computed(() => !!sceneryForm.value.name.trim())
const commentTotalPages = computed(() => Math.max(1, Math.ceil(commentTotal.value / commentSize)))

const resetMessages = () => {
  error.value = ''
  success.value = ''
}

const resetCategoryForm = () => {
  editingCategoryId.value = null
  categoryForm.value = { name: '' }
}

const resetSceneryForm = () => {
  editingSceneryId.value = null
  sceneryForm.value = {
    name: '',
    description: '',
    coverImage: '',
    location: '',
    ticketPrice: '',
    openTime: '',
    categoryId: '',
    rating: '',
    latitude: '',
    longitude: ''
  }
}

const toNullableNumber = (value) => {
  if (value === '' || value == null) return null
  const num = Number(value)
  return Number.isNaN(num) ? null : num
}

const normalizeCoverImage = (value) => {
  const raw = (value || '').trim()
  if (!raw) return ''
  if (raw.startsWith('data:image/')) return ''

  if (raw.includes('/uploads/')) {
    const idx = raw.indexOf('/uploads/')
    return raw.slice(idx)
  }
  return raw
}

const parseTicketNumber = (ticket) => {
  const match = String(ticket || '').match(/[0-9]+(\.[0-9]+)?/)
  return match ? match[0] : ''
}

const loadBaseData = async () => {
  isLoading.value = true
  resetMessages()
  try {
    const [categoryList, sceneryResult] = await Promise.all([
      adminService.listCategories(),
      adminService.listScenery({ size: 200 })
    ])
    categories.value = categoryList
    sceneryItems.value = sceneryResult.items
  } catch (e) {
    error.value = e.message || '加载管理数据失败'
  } finally {
    isLoading.value = false
  }
}

const loadCommentList = async ({ resetPage = false } = {}) => {
  if (resetPage) commentPage.value = 0

  isLoading.value = true
  resetMessages()
  try {
    const result = await adminService.listComments({
      page: commentPage.value,
      size: commentSize,
      sceneryId: commentSceneryId.value || undefined,
      keyword: commentKeyword.value.trim() || undefined
    })
    comments.value = result.items
    commentTotal.value = result.total
  } catch (e) {
    error.value = e.message || '加载评论列表失败'
  } finally {
    isLoading.value = false
  }
}

const switchTab = async (tab) => {
  activeTab.value = tab
  resetMessages()
  if (tab === 'comment') {
    await loadCommentList({ resetPage: true })
  }
}

const submitCategory = async () => {
  resetMessages()
  if (!categoryForm.value.name.trim()) {
    error.value = '分类名称不能为空'
    return
  }

  actionLoading.value = true
  try {
    const payload = { name: categoryForm.value.name.trim() }
    if (editingCategoryId.value) {
      await adminService.updateCategory(editingCategoryId.value, payload)
      success.value = '分类更新成功'
    } else {
      await adminService.createCategory(payload)
      success.value = '分类创建成功'
    }
    resetCategoryForm()
    await loadBaseData()
    emit('updated')
  } catch (e) {
    error.value = e.message || '分类保存失败'
  } finally {
    actionLoading.value = false
  }
}

const editCategory = (item) => {
  resetMessages()
  editingCategoryId.value = item.id
  categoryForm.value = { name: item.name || '' }
  activeTab.value = 'category'
}

const removeCategory = async (item) => {
  if (!window.confirm(`确认删除分类“${item.name}”吗？`)) return

  resetMessages()
  actionLoading.value = true
  try {
    await adminService.deleteCategory(item.id)
    success.value = '分类已删除'
    await loadBaseData()
    emit('updated')
  } catch (e) {
    error.value = e.message || '删除分类失败'
  } finally {
    actionLoading.value = false
  }
}

const submitScenery = async () => {
  resetMessages()
  if (!canSubmitScenery.value) {
    error.value = '景点名称不能为空'
    return
  }

  actionLoading.value = true
  try {
    const payload = {
      name: sceneryForm.value.name.trim(),
      description: sceneryForm.value.description || '',
      coverImage: normalizeCoverImage(sceneryForm.value.coverImage),
      location: sceneryForm.value.location || '',
      ticketPrice: toNullableNumber(sceneryForm.value.ticketPrice),
      openTime: sceneryForm.value.openTime || '',
      categoryId: toNullableNumber(sceneryForm.value.categoryId),
      rating: toNullableNumber(sceneryForm.value.rating),
      latitude: toNullableNumber(sceneryForm.value.latitude),
      longitude: toNullableNumber(sceneryForm.value.longitude)
    }

    if (editingSceneryId.value) {
      await adminService.updateScenery(editingSceneryId.value, payload)
      success.value = '景点更新成功'
    } else {
      await adminService.createScenery(payload)
      success.value = '景点创建成功'
    }

    resetSceneryForm()
    await loadBaseData()
    emit('updated')
  } catch (e) {
    error.value = e.message || '景点保存失败'
  } finally {
    actionLoading.value = false
  }
}

const editScenery = (item) => {
  resetMessages()
  editingSceneryId.value = item.id
  sceneryForm.value = {
    name: item.title || '',
    description: item.description || '',
    coverImage: item.coverImageRaw || '',
    location: item.location || '',
    ticketPrice: parseTicketNumber(item.ticket),
    openTime: item.openingHours || '',
    categoryId: '',
    rating: item.rating ?? '',
    latitude: item.latitude ?? '',
    longitude: item.longitude ?? ''
  }

  const matchedCategory = categories.value.find(c => c.name === item.categoryName)
  sceneryForm.value.categoryId = matchedCategory?.id ? String(matchedCategory.id) : ''

  activeTab.value = 'scenery'
}

const removeScenery = async (item) => {
  if (!window.confirm(`确认删除景点“${item.title}”吗？`)) return

  resetMessages()
  actionLoading.value = true
  try {
    await adminService.deleteScenery(item.id)
    success.value = '景点已删除'
    await loadBaseData()
    emit('updated')
  } catch (e) {
    error.value = e.message || '删除景点失败'
  } finally {
    actionLoading.value = false
  }
}

const uploadCover = async (event) => {
  const file = event.target.files?.[0]
  if (!file) return

  resetMessages()
  actionLoading.value = true
  try {
    const url = await sceneryService.uploadImage(file)
    sceneryForm.value.coverImage = url || ''
    success.value = '封面上传成功'
  } catch (e) {
    error.value = e.message || '封面上传失败'
  } finally {
    actionLoading.value = false
    event.target.value = ''
  }
}

const canDeleteComment = (item) => (item?.userRole || 'USER').toUpperCase() !== 'ADMIN'

const removeComment = async (item) => {
  if (!item?.sceneryId) {
    error.value = '评论缺少景点信息，无法删除'
    return
  }
  if (!canDeleteComment(item)) {
    error.value = '管理员账号评论请保留'
    return
  }

  if (!window.confirm(`确认删除该评论吗？\n用户：${item.userName}\n景点：${item.sceneryName}`)) return

  resetMessages()
  actionLoading.value = true
  try {
    await adminService.deleteComment(item.sceneryId, item.id)
    success.value = '评论已删除'

    const shouldGoPrevPage = comments.value.length === 1 && commentPage.value > 0
    if (shouldGoPrevPage) {
      commentPage.value -= 1
    }
    await loadCommentList()
    emit('updated')
  } catch (e) {
    error.value = e.message || '删除评论失败'
  } finally {
    actionLoading.value = false
  }
}

const applyCommentFilters = async () => {
  await loadCommentList({ resetPage: true })
}

const clearCommentFilters = async () => {
  commentKeyword.value = ''
  commentSceneryId.value = ''
  await loadCommentList({ resetPage: true })
}

const goCommentPage = async (delta) => {
  const next = commentPage.value + delta
  if (next < 0 || next >= commentTotalPages.value) return
  commentPage.value = next
  await loadCommentList()
}

const formatDateTime = (value) => {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return date.toLocaleString('zh-CN', { hour12: false })
}

onMounted(() => {
  loadBaseData()
})
</script>

<template>
  <section class="max-w-[1600px] mx-auto px-4 md:px-6 py-8">
    <div class="rounded-2xl p-5 md:p-6" style="background: var(--bg-card); border: 1px solid var(--border);">
      <div class="flex flex-wrap items-center justify-between gap-3 mb-5">
        <div>
          <h2 class="text-2xl font-bold" style="color: var(--text-primary);">管理员后台</h2>
          <p class="text-sm mt-1" style="color: var(--text-muted);">管理分类、景点、评论和订单</p>
        </div>
        <button
          class="px-4 py-2 rounded-lg text-sm font-medium"
          style="background: var(--bg-secondary); border: 1px solid var(--border); color: var(--text-secondary);"
          @click="emit('close')"
        >
          返回首页
        </button>
      </div>

      <div class="flex flex-wrap gap-2 mb-5">
        <button
          class="px-4 py-2 rounded-lg text-sm font-medium"
          :style="activeTab === 'scenery' ? 'background: var(--primary); color: white;' : 'background: var(--bg-secondary); color: var(--text-secondary); border: 1px solid var(--border);'"
          @click="switchTab('scenery')"
        >
          景点管理
        </button>
        <button
          class="px-4 py-2 rounded-lg text-sm font-medium"
          :style="activeTab === 'category' ? 'background: var(--primary); color: white;' : 'background: var(--bg-secondary); color: var(--text-secondary); border: 1px solid var(--border);'"
          @click="switchTab('category')"
        >
          分类管理
        </button>
        <button
          class="px-4 py-2 rounded-lg text-sm font-medium"
          :style="activeTab === 'comment' ? 'background: var(--primary); color: white;' : 'background: var(--bg-secondary); color: var(--text-secondary); border: 1px solid var(--border);'"
          @click="switchTab('comment')"
        >
          评论管理
        </button>
        <button
          class="px-4 py-2 rounded-lg text-sm font-medium"
          :style="activeTab === 'order' ? 'background: var(--primary); color: white;' : 'background: var(--bg-secondary); color: var(--text-secondary); border: 1px solid var(--border);'"
          @click="switchTab('order')"
        >
          订单管理
        </button>
      </div>

      <div v-if="error" class="mb-4 p-3 rounded-lg text-sm bg-red-50 border border-red-200 text-red-600">{{ error }}</div>
      <div v-if="success" class="mb-4 p-3 rounded-lg text-sm bg-green-50 border border-green-200 text-green-700">{{ success }}</div>

      <div v-if="isLoading" class="py-12 text-center text-sm" style="color: var(--text-muted);">加载中...</div>

      <template v-else>
        <div v-if="activeTab === 'category'" class="grid grid-cols-1 lg:grid-cols-5 gap-5">
          <div class="lg:col-span-2 p-4 rounded-xl" style="background: var(--bg-secondary); border: 1px solid var(--border);">
            <h3 class="font-semibold mb-3" style="color: var(--text-primary);">{{ editingCategoryId ? '编辑分类' : '新增分类' }}</h3>
            <div class="space-y-3">
              <label class="block text-xs font-semibold" style="color: var(--text-muted);">分类名称</label>
              <input
                v-model="categoryForm.name"
                type="text"
                class="w-full px-3 py-2 rounded-lg text-sm"
                style="background: var(--bg-card); border: 1px solid var(--border); color: var(--text-primary);"
                placeholder="例如：自然风光"
              />
              <div class="flex gap-2">
                <button
                  class="px-4 py-2 rounded-lg text-sm font-medium text-white"
                  style="background: var(--primary);"
                  :disabled="actionLoading"
                  @click="submitCategory"
                >{{ editingCategoryId ? '更新' : '创建' }}</button>
                <button
                  class="px-4 py-2 rounded-lg text-sm font-medium"
                  style="background: var(--bg-card); border: 1px solid var(--border); color: var(--text-secondary);"
                  @click="resetCategoryForm"
                >重置</button>
              </div>
            </div>
          </div>

          <div class="lg:col-span-3 p-4 rounded-xl" style="background: var(--bg-secondary); border: 1px solid var(--border);">
            <h3 class="font-semibold mb-3" style="color: var(--text-primary);">分类列表</h3>
            <div class="space-y-2 max-h-[420px] overflow-auto pr-1">
              <div
                v-for="item in categories"
                :key="item.id"
                class="p-3 rounded-lg flex items-center justify-between"
                style="background: var(--bg-card); border: 1px solid var(--border-subtle);"
              >
                <div>
                  <div class="text-sm font-medium" style="color: var(--text-primary);">{{ item.name }}</div>
                  <div class="text-xs" style="color: var(--text-muted);">ID: {{ item.id }}</div>
                </div>
                <div class="flex gap-2">
                  <button class="px-3 py-1.5 text-xs rounded-md" style="background: #eff6ff; color: #1d4ed8;" @click="editCategory(item)">编辑</button>
                  <button class="px-3 py-1.5 text-xs rounded-md" style="background: #fef2f2; color: #dc2626;" @click="removeCategory(item)">删除</button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-else-if="activeTab === 'comment'" class="space-y-4">
          <div class="p-4 rounded-xl" style="background: var(--bg-secondary); border: 1px solid var(--border);">
            <div class="grid grid-cols-1 md:grid-cols-4 gap-3">
              <select
                v-model="commentSceneryId"
                class="px-3 py-2 rounded-lg text-sm"
                style="background: var(--bg-card); border: 1px solid var(--border); color: var(--text-primary);"
              >
                <option value="">全部景点</option>
                <option v-for="item in sceneryItems" :key="item.id" :value="String(item.id)">
                  {{ item.id }} - {{ item.title }}
                </option>
              </select>
              <input
                v-model="commentKeyword"
                type="text"
                class="md:col-span-2 px-3 py-2 rounded-lg text-sm"
                style="background: var(--bg-card); border: 1px solid var(--border); color: var(--text-primary);"
                placeholder="按评论内容关键字搜索"
              />
              <div class="flex gap-2">
                <button
                  class="px-4 py-2 rounded-lg text-sm font-medium text-white"
                  style="background: var(--primary);"
                  :disabled="actionLoading"
                  @click="applyCommentFilters"
                >
                  查询
                </button>
                <button
                  class="px-4 py-2 rounded-lg text-sm font-medium"
                  style="background: var(--bg-card); border: 1px solid var(--border); color: var(--text-secondary);"
                  :disabled="actionLoading"
                  @click="clearCommentFilters"
                >
                  重置
                </button>
              </div>
            </div>
          </div>

          <div class="p-4 rounded-xl" style="background: var(--bg-secondary); border: 1px solid var(--border);">
            <div class="flex items-center justify-between mb-3">
              <h3 class="font-semibold" style="color: var(--text-primary);">评论列表</h3>
              <p class="text-xs" style="color: var(--text-muted);">共 {{ commentTotal }} 条</p>
            </div>

            <div v-if="comments.length === 0" class="py-10 text-center text-sm" style="color: var(--text-muted);">
              暂无评论数据
            </div>

            <div v-else class="space-y-2 max-h-[620px] overflow-auto pr-1">
              <div
                v-for="item in comments"
                :key="item.id"
                class="p-3 rounded-lg flex gap-3 justify-between"
                style="background: var(--bg-card); border: 1px solid var(--border-subtle);"
              >
                <div class="min-w-0">
                  <p class="text-sm font-semibold truncate" style="color: var(--text-primary);">
                    景点：{{ item.sceneryName }}
                  </p>
                  <p class="text-xs mt-0.5" style="color: var(--text-muted);">
                    评论ID：{{ item.id }} · 用户：{{ item.userName }}
                    <span
                      class="ml-1 px-1.5 py-0.5 rounded"
                      :style="item.userRole === 'ADMIN' ? 'background:#ede9fe;color:#5b21b6;' : 'background:#ecfeff;color:#155e75;'"
                    >
                      {{ item.userRole }}
                    </span>
                    · 时间：{{ formatDateTime(item.createdAt) }}
                  </p>
                  <p class="text-sm mt-1" style="color: var(--text-secondary); white-space: pre-wrap; word-break: break-word;">
                    {{ item.content }}
                  </p>
                  <p class="text-xs mt-1" style="color: var(--text-muted);">
                    评分：{{ item.rating ?? '-' }} · 点赞：{{ item.likeCount ?? 0 }}
                  </p>
                </div>
                <div class="flex items-center gap-2 shrink-0">
                  <button
                    v-if="canDeleteComment(item)"
                    class="px-3 py-1.5 text-xs rounded-md"
                    style="background: #fef2f2; color: #dc2626;"
                    :disabled="actionLoading"
                    @click="removeComment(item)"
                  >
                    删除
                  </button>
                  <span
                    v-else
                    class="text-xs px-2 py-1 rounded-md"
                    style="background:#f8fafc;color:#64748b;border:1px solid #e2e8f0;"
                  >
                    管理员评论
                  </span>
                </div>
              </div>
            </div>

            <div class="mt-4 flex items-center justify-end gap-2">
              <button
                class="px-3 py-1.5 rounded-lg text-xs"
                style="background: var(--bg-card); border: 1px solid var(--border); color: var(--text-secondary);"
                :disabled="commentPage === 0 || actionLoading"
                @click="goCommentPage(-1)"
              >
                上一页
              </button>
              <span class="text-xs" style="color: var(--text-muted);">
                第 {{ commentPage + 1 }} / {{ commentTotalPages }} 页
              </span>
              <button
                class="px-3 py-1.5 rounded-lg text-xs"
                style="background: var(--bg-card); border: 1px solid var(--border); color: var(--text-secondary);"
                :disabled="commentPage >= commentTotalPages - 1 || actionLoading"
                @click="goCommentPage(1)"
              >
                下一页
              </button>
            </div>
          </div>
        </div>

        <AdminOrderManager
          v-else-if="activeTab === 'order'"
          @updated="emit('updated')"
        />

        <div v-else class="grid grid-cols-1 xl:grid-cols-5 gap-5">
          <div class="xl:col-span-2 p-4 rounded-xl" style="background: var(--bg-secondary); border: 1px solid var(--border);">
            <h3 class="font-semibold mb-3" style="color: var(--text-primary);">{{ editingSceneryId ? '编辑景点' : '新增景点' }}</h3>
            <div class="space-y-2.5">
              <div>
                <label class="block text-xs font-semibold mb-1" style="color: var(--text-muted);">景点名称</label>
                <input v-model="sceneryForm.name" type="text" class="w-full px-3 py-2 rounded-lg text-sm" style="background: var(--bg-card); border: 1px solid var(--border); color: var(--text-primary);" placeholder="例如：黄山风景区" />
              </div>

              <div>
                <label class="block text-xs font-semibold mb-1" style="color: var(--text-muted);">景点描述</label>
                <textarea v-model="sceneryForm.description" rows="3" class="w-full px-3 py-2 rounded-lg text-sm" style="background: var(--bg-card); border: 1px solid var(--border); color: var(--text-primary);" placeholder="景点简介" />
              </div>

              <div>
                <label class="block text-xs font-semibold mb-1" style="color: var(--text-muted);">地址</label>
                <input v-model="sceneryForm.location" type="text" class="w-full px-3 py-2 rounded-lg text-sm" style="background: var(--bg-card); border: 1px solid var(--border); color: var(--text-primary);" placeholder="例如：安徽省黄山市" />
              </div>

              <div>
                <label class="block text-xs font-semibold mb-1" style="color: var(--text-muted);">开放时间</label>
                <input v-model="sceneryForm.openTime" type="text" class="w-full px-3 py-2 rounded-lg text-sm" style="background: var(--bg-card); border: 1px solid var(--border); color: var(--text-primary);" placeholder="例如：06:30-17:30" />
              </div>

              <div class="grid grid-cols-2 gap-2">
                <div>
                  <label class="block text-xs font-semibold mb-1" style="color: var(--text-muted);">票价</label>
                  <input v-model="sceneryForm.ticketPrice" type="number" step="0.01" class="w-full px-3 py-2 rounded-lg text-sm" style="background: var(--bg-card); border: 1px solid var(--border); color: var(--text-primary);" placeholder="如：230" />
                </div>
                <div>
                  <label class="block text-xs font-semibold mb-1" style="color: var(--text-muted);">评分</label>
                  <input v-model="sceneryForm.rating" type="number" step="0.1" min="0" max="5" class="w-full px-3 py-2 rounded-lg text-sm" style="background: var(--bg-card); border: 1px solid var(--border); color: var(--text-primary);" placeholder="0-5" />
                </div>
              </div>

              <div class="grid grid-cols-2 gap-2">
                <div>
                  <label class="block text-xs font-semibold mb-1" style="color: var(--text-muted);">纬度</label>
                  <input v-model="sceneryForm.latitude" type="number" step="0.000001" class="w-full px-3 py-2 rounded-lg text-sm" style="background: var(--bg-card); border: 1px solid var(--border); color: var(--text-primary);" placeholder="例如：30.131129" />
                </div>
                <div>
                  <label class="block text-xs font-semibold mb-1" style="color: var(--text-muted);">经度</label>
                  <input v-model="sceneryForm.longitude" type="number" step="0.000001" class="w-full px-3 py-2 rounded-lg text-sm" style="background: var(--bg-card); border: 1px solid var(--border); color: var(--text-primary);" placeholder="例如：118.182763" />
                </div>
              </div>

              <div>
                <label class="block text-xs font-semibold mb-1" style="color: var(--text-muted);">分类ID（可选）</label>
                <select v-model="sceneryForm.categoryId" class="w-full px-3 py-2 rounded-lg text-sm" style="background: var(--bg-card); border: 1px solid var(--border); color: var(--text-primary);">
                  <option value="">选择分类（可选）</option>
                  <option v-for="item in categories" :key="item.id" :value="String(item.id)">{{ item.id }} - {{ item.name }}</option>
                </select>
              </div>

              <div>
                <label class="block text-xs font-semibold mb-1" style="color: var(--text-muted);">封面图</label>
                <input v-model="sceneryForm.coverImage" type="text" class="w-full px-3 py-2 rounded-lg text-sm" style="background: var(--bg-card); border: 1px solid var(--border); color: var(--text-primary);" placeholder="支持 /uploads/xxx.jpg 或 http(s)://..." />
                <input type="file" accept="image/*" class="w-full text-xs mt-1" @change="uploadCover" />
              </div>

              <div class="flex gap-2 pt-1">
                <button
                  class="px-4 py-2 rounded-lg text-sm font-medium text-white"
                  style="background: var(--primary);"
                  :disabled="actionLoading || !canSubmitScenery"
                  @click="submitScenery"
                >{{ editingSceneryId ? '更新' : '创建' }}</button>
                <button
                  class="px-4 py-2 rounded-lg text-sm font-medium"
                  style="background: var(--bg-card); border: 1px solid var(--border); color: var(--text-secondary);"
                  @click="resetSceneryForm"
                >重置</button>
              </div>
            </div>
          </div>

          <div class="xl:col-span-3 p-4 rounded-xl" style="background: var(--bg-secondary); border: 1px solid var(--border);">
            <h3 class="font-semibold mb-3" style="color: var(--text-primary);">景点列表</h3>
            <div class="space-y-2 max-h-[640px] overflow-auto pr-1">
              <div
                v-for="item in sceneryItems"
                :key="item.id"
                class="p-3 rounded-lg flex gap-3 justify-between"
                style="background: var(--bg-card); border: 1px solid var(--border-subtle);"
              >
                <div class="min-w-0">
                  <p class="text-sm font-semibold truncate" style="color: var(--text-primary);">{{ item.title }}</p>
                  <p class="text-xs mt-0.5" style="color: var(--text-muted);">ID: {{ item.id }} · 分类: {{ item.categoryName || '未设置' }}</p>
                  <p class="text-xs mt-0.5" style="color: var(--text-muted);">地址: {{ item.location || '未设置' }} · 评分: {{ item.rating ?? '-' }}</p>
                  <p class="text-xs mt-0.5 line-clamp-1" style="color: var(--text-secondary);">描述: {{ item.description || '暂无描述' }}</p>
                </div>
                <div class="flex items-center gap-2 shrink-0">
                  <button class="px-3 py-1.5 text-xs rounded-md" style="background: #eff6ff; color: #1d4ed8;" @click="editScenery(item)">编辑</button>
                  <button class="px-3 py-1.5 text-xs rounded-md" style="background: #fef2f2; color: #dc2626;" @click="removeScenery(item)">删除</button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </template>
    </div>
  </section>
</template>
