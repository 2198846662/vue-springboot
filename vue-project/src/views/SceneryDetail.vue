<script setup>
import { computed, onMounted, ref } from 'vue'
import {
  X,
  Heart,
  Star,
  MapPin,
  Clock,
  Ticket,
  ChevronLeft,
  ChevronRight,
  MessageCircle,
  Send,
  Trash2
} from 'lucide-vue-next'
import { sceneryService } from '../services/api.js'

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

const emit = defineEmits(['close', 'login', 'rated', 'book'])

// comments
const comments = ref([])
const newComment = ref('')
const isLoadingComments = ref(true)
const isSubmitting = ref(false)
const commentError = ref('')
const replyTo = ref(null)
const expandedReplyGroups = ref({})
const likeLoadingMap = ref({})

// image carousel
const currentImageIndex = ref(0)
const totalImages = computed(() => props.scenery.images?.length || 0)

// favorite
const isFavorited = ref(false)

// rating
const ratingSummary = ref({
  averageScore: Number(props.scenery?.rating ?? 0),
  ratingCount: 0
})
const myRating = ref(null)
const ratingHover = ref(null)
const isRatingSubmitting = ref(false)
const ratingError = ref('')
const ratingSuccess = ref('')
const canRateScenery = ref(false)
const ratingBlockReason = ref('')

const isNormalUser = computed(() => (props.user?.role || 'USER').toUpperCase() === 'USER')
const displayAverageScore = computed(() => {
  const avg = Number(ratingSummary.value.averageScore ?? 0)
  return Number.isFinite(avg) ? avg.toFixed(1) : '0.0'
})
const ratingDisplayValue = computed(() => ratingHover.value ?? myRating.value ?? 0)

const commentThreads = computed(() => {
  const list = comments.value || []
  const commentMap = new Map()
  list.forEach(item => commentMap.set(Number(item.id), item))

  const findRootId = (comment) => {
    let current = comment
    let safety = 0
    while (current?.replyToCommentId && safety < 20) {
      const parent = commentMap.get(Number(current.replyToCommentId))
      if (!parent) break
      current = parent
      safety += 1
    }
    return Number(current?.id || comment.id)
  }

  const rootMap = new Map()
  list.forEach(item => {
    const id = Number(item.id)
    if (!rootMap.has(id)) {
      rootMap.set(id, { ...item, replies: [] })
    }
  })

  list.forEach(item => {
    if (!item.replyToCommentId) return
    const rootId = findRootId(item)
    if (!rootMap.has(rootId)) {
      rootMap.set(rootId, { ...item, replies: [] })
    }
    if (Number(item.id) !== rootId) {
      rootMap.get(rootId).replies.push(item)
    }
  })

  return Array.from(rootMap.values())
    .filter(item => !item.replyToCommentId || !commentMap.has(Number(item.replyToCommentId)) || item.replies.length > 0)
    .map(item => ({
      ...item,
      replies: item.replies.sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt))
    }))
})

const getVisibleReplies = (thread) => {
  if (!thread?.replies?.length) return []
  return expandedReplyGroups.value[thread.id] ? thread.replies : []
}

const toggleReplies = (threadId) => {
  expandedReplyGroups.value = {
    ...expandedReplyGroups.value,
    [threadId]: !expandedReplyGroups.value[threadId]
  }
}

const nextImage = () => {
  currentImageIndex.value = (currentImageIndex.value + 1) % totalImages.value
}

const prevImage = () => {
  currentImageIndex.value = (currentImageIndex.value - 1 + totalImages.value) % totalImages.value
}

const goToImage = (index) => {
  currentImageIndex.value = index
}

const getStarFill = (starIndex, score) => {
  const diff = Number(score || 0) - (starIndex - 1)
  if (diff >= 1) return 100
  if (diff >= 0.5) return 50
  return 0
}

const setRatingHover = (value) => {
  if (!props.user || !isNormalUser.value || !canRateScenery.value) return
  ratingHover.value = value
}

const clearRatingHover = () => {
  ratingHover.value = null
}

const submitRating = async (score) => {
  if (!props.user) {
    emit('login')
    return
  }
  if (!isNormalUser.value) {
    ratingError.value = '仅普通用户可以评分'
    return
  }
  if (!canRateScenery.value) {
    ratingError.value = ratingBlockReason.value || '当前无法评分'
    return
  }

  ratingError.value = ''
  ratingSuccess.value = ''
  isRatingSubmitting.value = true
  try {
    const result = await sceneryService.rateScenery(props.scenery.id, score)
    myRating.value = Number(result.score)
    ratingSummary.value = {
      averageScore: Number(result.averageScore ?? 0),
      ratingCount: Number(result.ratingCount ?? 0)
    }
    ratingSuccess.value = '评分成功'
    emit('rated', {
      sceneryId: props.scenery.id,
      rating: Number(result.averageScore ?? 0)
    })
  } catch (err) {
    ratingError.value = err.message || '评分失败'
  } finally {
    isRatingSubmitting.value = false
  }
}

const loadRatingState = async () => {
  try {
    const summary = await sceneryService.getSceneryRatingSummary(props.scenery.id)
    ratingSummary.value = {
      averageScore: Number(summary.averageScore ?? props.scenery?.rating ?? 0),
      ratingCount: Number(summary.ratingCount ?? 0)
    }
    emit('rated', {
      sceneryId: props.scenery.id,
      rating: Number(summary.averageScore ?? props.scenery?.rating ?? 0)
    })
  } catch (err) {
    console.error('Failed to load rating summary:', err)
  }

  if (props.user && isNormalUser.value) {
    try {
      const ratingState = await sceneryService.getMySceneryRating(props.scenery.id)
      myRating.value = ratingState.score
      canRateScenery.value = !!ratingState.canRate
      ratingBlockReason.value = ratingState.reason || ''
    } catch (err) {
      console.error('Failed to load user rating:', err)
    }
  }
}

const toggleFavorite = async () => {
  if (!props.user) {
    emit('login')
    return
  }
  try {
    const status = await sceneryService.toggleFavorite(props.scenery.id)
    isFavorited.value = status === 'favorited'
  } catch (err) {
    console.error('Failed to toggle favorite:', err)
  }
}

const openBookingPanel = () => {
  if (!props.user) {
    emit('login')
    return
  }
  if (!isNormalUser.value) return
  emit('book')
}

const loadComments = async () => {
  isLoadingComments.value = true
  try {
    comments.value = await sceneryService.getComments(props.scenery.id)

    if (props.user?.id && comments.value.length > 0) {
      const likeStatusList = await Promise.all(
        comments.value.map(async (comment) => {
          try {
            const liked = await sceneryService.isCommentLiked(props.scenery.id, comment.id)
            return { id: comment.id, liked }
          } catch (err) {
            return { id: comment.id, liked: false }
          }
        })
      )

      const likeMap = new Map(likeStatusList.map(item => [item.id, item.liked]))
      comments.value = comments.value.map(item => ({
        ...item,
        isLikedByCurrentUser: !!likeMap.get(item.id)
      }))
    }
  } catch (err) {
    console.error('Failed to load comments:', err)
  } finally {
    isLoadingComments.value = false
  }
}

const toggleCommentLike = async (comment) => {
  if (!props.user) {
    emit('login')
    return
  }
  if (likeLoadingMap.value[comment.id]) return

  likeLoadingMap.value = {
    ...likeLoadingMap.value,
    [comment.id]: true
  }
  try {
    const result = await sceneryService.toggleCommentLike(props.scenery.id, comment.id)
    comments.value = comments.value.map(item => {
      if (item.id !== comment.id) return item
      return {
        ...item,
        likeCount: Number(result.likeCount ?? item.likeCount ?? 0),
        isLikedByCurrentUser: !!result.liked
      }
    })
  } catch (err) {
    commentError.value = err.message || 'Failed to like comment.'
  } finally {
    likeLoadingMap.value = {
      ...likeLoadingMap.value,
      [comment.id]: false
    }
  }
}

const submitComment = async () => {
  if (!newComment.value.trim()) return
  if (!props.user) {
    emit('login')
    return
  }

  commentError.value = ''
  isSubmitting.value = true
  try {
    const comment = await sceneryService.addComment(props.scenery.id, newComment.value, {
      rating: null,
      replyToCommentId: replyTo.value?.id || null
    })
    comments.value.unshift(comment)
    newComment.value = ''
    replyTo.value = null
  } catch (err) {
    commentError.value = err.message || 'Failed to add comment.'
  } finally {
    isSubmitting.value = false
  }
}

const startReply = (comment) => {
  if (!props.user) {
    emit('login')
    return
  }
  replyTo.value = {
    id: comment.id,
    userName: comment.userName
  }
}

const cancelReply = () => {
  replyTo.value = null
}

const deleteComment = async (commentId) => {
  try {
    await sceneryService.deleteComment(props.scenery.id, commentId)
    comments.value = comments.value.filter(item => item.id !== commentId)
  } catch (err) {
    commentError.value = err.message || 'Failed to delete comment.'
  }
}

const isOwnComment = (comment) => {
  return props.user && String(props.user.id) === String(comment.userId)
}

const formatDate = (dateString) => {
  const date = new Date(dateString)
  const now = new Date()
  const diffMs = now - date
  const diffMins = Math.floor(diffMs / 60000)
  const diffHours = Math.floor(diffMs / 3600000)
  const diffDays = Math.floor(diffMs / 86400000)

  if (diffMins < 1) return 'just now'
  if (diffMins < 60) return `${diffMins}m ago`
  if (diffHours < 24) return `${diffHours}h ago`
  if (diffDays < 7) return `${diffDays}d ago`
  return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
}

onMounted(async () => {
  loadComments()
  loadRatingState()

  if (props.user?.id) {
    try {
      isFavorited.value = await sceneryService.isFavorited(props.scenery.id)
    } catch (err) {
      console.error('Failed to load favorite state:', err)
    }
  }
})
</script>

<template>
  <Teleport to="body">
    <div class="detail-modal fixed inset-0 z-[100]">
      <div class="absolute inset-0 bg-black/60 backdrop-blur-sm" @click="$emit('close')" />

      <div class="absolute inset-4 md:inset-8 bg-[var(--bg-card)] rounded-2xl overflow-hidden flex flex-col animate-slide-up">
        <button
          class="absolute top-4 right-4 z-20 w-10 h-10 rounded-full bg-black/30 backdrop-blur-sm flex items-center justify-center text-white hover:bg-black/50 transition-colors"
          @click="$emit('close')"
        >
          <X class="w-5 h-5" />
        </button>

        <div class="flex-1 overflow-y-auto">
          <div class="grid grid-cols-1 lg:grid-cols-2 h-full">
            <div class="relative bg-black aspect-[4/3] lg:aspect-auto lg:min-h-full">
              <img
                v-for="(image, index) in scenery.images"
                :key="index"
                :src="image"
                :alt="`${scenery.title} image ${index + 1}`"
                class="absolute inset-0 w-full h-full object-cover transition-opacity duration-500"
                :class="index === currentImageIndex ? 'opacity-100' : 'opacity-0'"
              />

              <button
                v-if="totalImages > 1"
                class="nav-arrow absolute left-4 top-1/2 -translate-y-1/2 w-10 h-10 rounded-full bg-white/20 backdrop-blur-sm flex items-center justify-center text-white hover:bg-white/40 transition-colors"
                @click="prevImage"
              >
                <ChevronLeft class="w-6 h-6" />
              </button>
              <button
                v-if="totalImages > 1"
                class="nav-arrow absolute right-4 top-1/2 -translate-y-1/2 w-10 h-10 rounded-full bg-white/20 backdrop-blur-sm flex items-center justify-center text-white hover:bg-white/40 transition-colors"
                @click="nextImage"
              >
                <ChevronRight class="w-6 h-6" />
              </button>

              <div v-if="totalImages > 1" class="absolute bottom-4 left-1/2 -translate-x-1/2 flex gap-2">
                <button
                  v-for="(_, index) in scenery.images"
                  :key="index"
                  class="h-2 rounded-full transition-all duration-300"
                  :class="index === currentImageIndex ? 'w-6 bg-white' : 'w-2 bg-white/50'"
                  @click="goToImage(index)"
                />
              </div>

              <span
                class="absolute top-4 left-4 px-3 py-1 text-xs font-semibold rounded-full"
                style="background: rgba(255,255,255,0.95); color: var(--primary);"
              >
                {{ scenery.categoryName }}
              </span>
            </div>

            <div class="flex flex-col h-full overflow-y-auto">
              <div class="flex-1 p-6 md:p-8">
                <div class="mb-6">
                  <h1 class="text-2xl md:text-3xl font-bold mb-3" style="font-family: var(--font-display); color: var(--text-primary);">
                    {{ scenery.title }}
                  </h1>

                  <div class="mb-4">
                    <div class="flex items-center gap-2">
                      <Star class="w-5 h-5 fill-current" style="color: var(--gold);" />
                      <span class="text-lg font-semibold" style="color: var(--text-primary);">{{ displayAverageScore }}</span>
                      <span class="text-sm" style="color: var(--text-muted);">
                        {{ ratingSummary.ratingCount > 0 ? `${ratingSummary.ratingCount} ratings` : 'No ratings yet' }}
                      </span>
                    </div>

                    <div v-if="user && isNormalUser" class="mt-3">
                      <p class="text-xs mb-2" style="color: var(--text-muted);">
                        我的评分：{{ myRating == null ? '未评分' : myRating.toFixed(1) }}
                      </p>
                      <div class="rating-stars flex items-center gap-1" @mouseleave="clearRatingHover">
                        <div
                          v-for="starIndex in 5"
                          :key="`rate-star-${starIndex}`"
                          class="star-control relative w-7 h-7"
                        >
                          <span class="star-base" :style="{ '--fill': `${getStarFill(starIndex, ratingDisplayValue)}%` }">★</span>
                          <button
                            type="button"
                            class="star-hit left"
                            :disabled="isRatingSubmitting || !canRateScenery"
                            @mouseenter="setRatingHover(starIndex - 0.5)"
                            @click="submitRating(starIndex - 0.5)"
                          />
                          <button
                            type="button"
                            class="star-hit right"
                            :disabled="isRatingSubmitting || !canRateScenery"
                            @mouseenter="setRatingHover(starIndex)"
                            @click="submitRating(starIndex)"
                          />
                        </div>
                      </div>
                      <p v-if="ratingError" class="text-xs mt-2 text-red-600">{{ ratingError }}</p>
                      <p v-else-if="ratingSuccess" class="text-xs mt-2 text-green-700">{{ ratingSuccess }}</p>
                      <p v-else-if="!canRateScenery" class="text-xs mt-2 text-amber-700">{{ ratingBlockReason || '购买并审核通过后可评分，且只能评分一次。' }}</p>
                    </div>

                    <div v-else-if="user && !isNormalUser" class="mt-2 text-xs" style="color: var(--text-muted);">
                      管理员账号不参与景点评分。
                    </div>
                  </div>

                  <div class="flex items-center gap-2 text-sm mb-4" style="color: var(--text-secondary);">
                    <MapPin class="w-4 h-4 flex-shrink-0" style="color: var(--accent);" />
                    <span>{{ scenery.location }}</span>
                  </div>
                </div>

                <div class="grid grid-cols-2 gap-4 mb-6">
                  <div class="p-4 rounded-xl" style="background: var(--bg-secondary);">
                    <div class="flex items-center gap-2 mb-1">
                      <Clock class="w-4 h-4" style="color: var(--text-muted);" />
                      <span class="text-xs font-medium" style="color: var(--text-muted);">Open Hours</span>
                    </div>
                    <p class="text-sm font-medium" style="color: var(--text-primary);">{{ scenery.openingHours }}</p>
                  </div>
                  <div class="p-4 rounded-xl" style="background: var(--bg-secondary);">
                    <div class="flex items-center gap-2 mb-1">
                      <Ticket class="w-4 h-4" style="color: var(--text-muted);" />
                      <span class="text-xs font-medium" style="color: var(--text-muted);">Ticket</span>
                    </div>
                    <p class="text-sm font-medium" style="color: var(--text-primary);">{{ scenery.ticket }}</p>
                  </div>
                </div>

                <div class="mb-8">
                  <h3 class="text-lg font-semibold mb-3" style="font-family: var(--font-display); color: var(--text-primary);">
                    About This Place
                  </h3>
                  <p class="text-base leading-relaxed" style="color: var(--text-secondary);">
                    {{ scenery.description }}
                  </p>
                </div>

                <div class="mb-8 p-4 rounded-xl" style="background: var(--bg-secondary); border: 1px solid var(--border-subtle);">
                  <h3 class="text-lg font-semibold mb-3" style="font-family: var(--font-display); color: var(--text-primary);">
                    预订
                  </h3>
                  <p class="text-sm mb-3" style="color: var(--text-secondary);">
                    点击下方“立即预订”进入预订界面，填写预订天数、出行日期等信息后加入购物车。
                  </p>
                  <button
                    class="px-4 py-2 rounded-lg text-sm font-medium text-white"
                    style="background: var(--primary);"
                    @click="openBookingPanel"
                  >
                    立即预订
                  </button>
                </div>

                <div class="border-t pt-6" style="border-color: var(--border-subtle);">
                  <div class="flex items-center gap-2 mb-6">
                    <MessageCircle class="w-5 h-5" style="color: var(--accent);" />
                    <h3 class="text-lg font-semibold" style="font-family: var(--font-display); color: var(--text-primary);">
                      Comments ({{ comments.length }})
                    </h3>
                  </div>

                  <div v-if="user" class="mb-6">
                    <div class="flex gap-3">
                      <div
                        class="w-10 h-10 rounded-full flex items-center justify-center text-white font-semibold flex-shrink-0"
                        style="background: linear-gradient(135deg, var(--accent), var(--gold));"
                      >
                        {{ user?.nickname?.charAt(0)?.toUpperCase() || 'U' }}
                      </div>
                      <div class="flex-1">
                        <div
                          v-if="replyTo"
                          class="mb-2 px-3 py-2 rounded-lg text-xs flex items-center justify-between"
                          style="background: #eff6ff; border: 1px solid #bfdbfe; color: #1e40af;"
                        >
                          <span>Replying to: {{ replyTo.userName }}</span>
                          <button class="underline" @click="cancelReply">Cancel</button>
                        </div>
                        <textarea
                          v-model="newComment"
                          class="comment-input w-full px-4 py-3 text-sm rounded-xl resize-none transition-all duration-200"
                          :placeholder="replyTo ? `Reply to ${replyTo.userName}...` : 'Write your comment...'"
                          rows="3"
                        />
                        <div class="flex justify-end mt-2">
                          <button
                            class="px-4 py-2 rounded-lg text-sm font-medium text-white transition-all hover:scale-105 disabled:opacity-50"
                            style="background: var(--primary);"
                            :disabled="!newComment.trim() || isSubmitting"
                            @click="submitComment"
                          >
                            <Send v-if="!isSubmitting" class="w-4 h-4 inline mr-1" />
                            {{ isSubmitting ? 'Posting...' : 'Post' }}
                          </button>
                        </div>
                      </div>
                    </div>
                    <div v-if="commentError" class="mt-3 p-3 bg-red-50 border border-red-200 rounded-xl text-red-600 text-sm">
                      {{ commentError }}
                    </div>
                  </div>

                  <div v-else class="mb-6 p-4 rounded-xl text-center" style="background: var(--bg-secondary);">
                    <p class="text-sm mb-2" style="color: var(--text-secondary);">Login to join comments</p>
                    <button
                      class="px-5 py-2 rounded-lg text-sm font-medium text-white transition-all hover:scale-105"
                      style="background: var(--primary);"
                      @click="$emit('login')"
                    >
                      Login
                    </button>
                  </div>

                  <div v-if="isLoadingComments" class="space-y-4">
                    <div v-for="i in 3" :key="i" class="comment-skeleton flex gap-3">
                      <div class="w-10 h-10 rounded-full skeleton-avatar" />
                      <div class="flex-1">
                        <div class="skeleton-line w-24 h-4 mb-2" />
                        <div class="skeleton-line w-full h-4 mb-1" />
                        <div class="skeleton-line w-3/4 h-4" />
                      </div>
                    </div>
                  </div>

                  <div v-else-if="comments.length === 0" class="text-center py-8">
                    <p class="text-sm" style="color: var(--text-muted);">No comments yet.</p>
                  </div>

                  <div v-else class="space-y-5">
                    <div v-for="thread in commentThreads" :key="thread.id" class="comment-item">
                      <div class="flex gap-3">
                        <div
                          class="w-10 h-10 rounded-full flex items-center justify-center text-white font-semibold flex-shrink-0"
                          style="background: linear-gradient(135deg, var(--primary), var(--primary-light));"
                        >
                          {{ thread.userAvatar || thread.userName?.charAt(0) }}
                        </div>
                        <div class="flex-1">
                          <div class="flex items-center justify-between gap-2 mb-1">
                            <div class="flex items-center gap-2">
                              <span class="text-sm font-medium" style="color: var(--text-primary);">{{ thread.userName }}</span>
                              <span class="text-xs" style="color: var(--text-muted);">{{ formatDate(thread.createdAt) }}</span>
                            </div>
                            <button
                              v-if="isOwnComment(thread)"
                              class="p-1 rounded hover:bg-red-50 text-red-400 transition-colors"
                              title="Delete comment"
                              @click="deleteComment(thread.id)"
                            >
                              <Trash2 class="w-4 h-4" />
                            </button>
                          </div>

                          <p class="text-sm leading-relaxed" style="color: var(--text-secondary);">{{ thread.content }}</p>

                          <div class="mt-2 flex items-center gap-4 text-xs" style="color: var(--text-muted);">
                            <button class="inline-flex items-center gap-1 hover:text-[#2563eb]" @click="startReply(thread)">
                              <MessageCircle class="w-3.5 h-3.5" /> Reply
                            </button>
                            <button
                              class="inline-flex items-center gap-1"
                              :class="thread.isLikedByCurrentUser ? 'text-[#e11d48]' : 'hover:text-[#e11d48]'"
                              :disabled="likeLoadingMap[thread.id]"
                              @click="toggleCommentLike(thread)"
                            >
                              <Heart class="w-3.5 h-3.5" /> {{ thread.likeCount || 0 }}
                            </button>
                          </div>

                          <div v-if="thread.replies.length" class="mt-3 pl-3 border-l-2" style="border-color: var(--border-subtle);">
                            <div v-for="reply in getVisibleReplies(thread)" :key="reply.id" class="py-2">
                              <div class="flex items-center justify-between gap-2">
                                <p class="text-xs" style="color: var(--text-muted);">
                                  <span class="font-medium" style="color: var(--text-primary);">{{ reply.userName }}</span>
                                  replied
                                  <span class="font-medium" style="color: #2563eb;">{{ reply.replyToUserName || thread.userName }}</span>
                                  · {{ formatDate(reply.createdAt) }}
                                </p>
                                <div class="flex items-center gap-2">
                                  <button
                                    v-if="user"
                                    class="text-xs px-2 py-0.5 rounded hover:bg-blue-50"
                                    style="color: #2563eb;"
                                    @click="startReply(reply)"
                                  >
                                    Reply
                                  </button>
                                  <button
                                    v-if="isOwnComment(reply)"
                                    class="p-1 rounded hover:bg-red-50 text-red-400 transition-colors"
                                    title="Delete comment"
                                    @click="deleteComment(reply.id)"
                                  >
                                    <Trash2 class="w-3.5 h-3.5" />
                                  </button>
                                </div>
                              </div>
                              <p class="text-sm mt-1" style="color: var(--text-secondary);">
                                <span style="color:#2563eb;">@{{ reply.replyToUserName || thread.userName }}</span>
                                {{ reply.content }}
                              </p>
                            </div>

                            <button
                              class="mt-2 text-xs inline-flex items-center gap-1"
                              style="color: #2563eb;"
                              @click="toggleReplies(thread.id)"
                            >
                              <span class="inline-block w-8 h-[1px]" style="background: var(--border);" />
                              {{ expandedReplyGroups[thread.id] ? 'Collapse replies' : `Expand ${thread.replies.length} replies` }}
                            </button>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <div class="p-4 border-t" style="border-color: var(--border-subtle); background: var(--bg-card);">
                <div class="flex items-center justify-between">
                  <button
                    class="favorite-action flex items-center gap-2 px-5 py-2.5 rounded-xl text-sm font-medium transition-all duration-200"
                    :class="isFavorited ? 'bg-red-50 text-red-500' : 'bg-[var(--bg-secondary)] text-[var(--text-secondary)]'"
                    @click="toggleFavorite"
                  >
                    <Heart class="w-5 h-5" :class="isFavorited ? 'fill-current' : ''" />
                    {{ isFavorited ? '已收藏' : '收藏' }}
                  </button>

                  <p class="text-sm" style="color: var(--text-muted);">
                    {{ totalImages }} images
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.animate-slide-up {
  animation: slideUp 0.3s ease-out;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: scale(0.95) translateY(20px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

.comment-input {
  background: var(--bg-secondary);
  border: 1px solid var(--border);
  color: var(--text-primary);
}

.comment-input::placeholder {
  color: var(--text-muted);
}

.comment-input:focus {
  outline: none;
  border-color: var(--primary);
  box-shadow: 0 0 0 3px rgba(27, 67, 50, 0.1);
}

.rating-stars {
  user-select: none;
}

.star-control {
  position: relative;
}

.star-base {
  position: relative;
  display: inline-block;
  width: 100%;
  height: 100%;
  font-size: 26px;
  line-height: 1;
  color: #d1d5db;
}

.star-base::before {
  content: '★';
  position: absolute;
  top: 0;
  left: 0;
  width: var(--fill);
  overflow: hidden;
  color: var(--gold);
}

.star-hit {
  position: absolute;
  top: 0;
  height: 100%;
  width: 50%;
  background: transparent;
  border: none;
  cursor: pointer;
}

.star-hit.left {
  left: 0;
}

.star-hit.right {
  right: 0;
}

.star-hit:disabled {
  cursor: not-allowed;
}

.skeleton-avatar {
  background: linear-gradient(90deg, var(--bg-secondary) 25%, var(--border) 50%, var(--bg-secondary) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.skeleton-line {
  background: linear-gradient(90deg, var(--bg-secondary) 25%, var(--border) 50%, var(--bg-secondary) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 4px;
}

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}
</style>
