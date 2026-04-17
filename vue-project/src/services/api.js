/**
 * API Service Layer for Scenery Tourism Platform
 * Connected to Spring Boot Backend at http://localhost:8080
 * Uses JWT Authentication
 */

const BASE_URL = '/api'
const IMAGE_PLACEHOLDER = 'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="800" height="600" viewBox="0 0 800 600"><defs><linearGradient id="g" x1="0" y1="0" x2="1" y2="1"><stop offset="0%" stop-color="%23dbeafe"/><stop offset="100%" stop-color="%23bfdbfe"/></linearGradient></defs><rect width="800" height="600" fill="url(%23g)"/><text x="50%" y="50%" dominant-baseline="middle" text-anchor="middle" fill="%231e3a8a" font-size="34" font-family="Arial, sans-serif">WorldThings</text></svg>'

// Simulate network delay (for development)
const delay = (ms) => new Promise(resolve => setTimeout(resolve, ms))

// ================== USE_REAL_API ==================
// Set to true when backend is running
const USE_REAL_API = true
// ==================================================

/**
 * Get auth token from localStorage
 */
function getToken() {
  try {
    const user = JSON.parse(localStorage.getItem('currentUser') || '{}')
    return user.token || null
  } catch (error) {
    console.warn('Invalid currentUser in localStorage, clearing stale auth data.', error)
    localStorage.removeItem('currentUser')
    localStorage.removeItem('rememberMe')
    return null
  }
}

function resolveImageUrl(path) {
  if (!path) return IMAGE_PLACEHOLDER

  const raw = String(path).trim()
  if (!raw) return IMAGE_PLACEHOLDER

  if (raw.startsWith('http://') || raw.startsWith('https://') || raw.startsWith('data:') || raw.startsWith('blob:')) {
    return raw
  }

  if (raw.startsWith('/')) {
    return raw
  }

  return `/uploads/${raw}`
}

/**
 * Generic fetch wrapper with error handling
 */
async function fetchJSON(url, options = {}) {
  const token = getToken()

  const headers = {
    'Content-Type': 'application/json',
    ...options.headers
  }

  // Add Authorization header if token exists
  if (token) {
    headers['Authorization'] = `Bearer ${token}`
  }

  const response = await fetch(url, {
    ...options,
    headers
  })

  // Handle 401 Unauthorized
  if (response.status === 401) {
    localStorage.removeItem('currentUser')
    localStorage.removeItem('rememberMe')
    window.location.reload()
    throw new Error('登录已过期，请重新登录')
  }

  if (!response.ok) {
    // Try to parse error message from response body
    const text = await response.text()
    let errorMsg = `HTTP error! status: ${response.status}`
    try {
      const json = JSON.parse(text)
      if (json.message) {
        errorMsg = json.message
      }
    } catch (e) {
      // Use default error message
    }
    throw new Error(errorMsg)
  }

  return response.json()
}

/**
 * Transform backend scenery to frontend format
 */
function transformScenery(backendScenery) {
  if (!backendScenery) return null

  return {
    id: backendScenery.id,
    title: backendScenery.name,
    description: backendScenery.description,
    location: backendScenery.location,
    category: backendScenery.category?.name?.toLowerCase() || 'other',
    categoryName: backendScenery.category?.name || '其他',
  coverImageRaw: backendScenery.coverImage || '',
    images: [resolveImageUrl(backendScenery.coverImage)],
    rating: parseFloat(backendScenery.rating) || 0,
    reviewCount: backendScenery.viewCount || 0,
    openingHours: backendScenery.openTime || '全天开放',
    ticket: backendScenery.ticketPrice ? `¥${backendScenery.ticketPrice}` : '免费',
    latitude: backendScenery.latitude == null ? null : Number(backendScenery.latitude),
    longitude: backendScenery.longitude == null ? null : Number(backendScenery.longitude),
    featured: backendScenery.featured ?? false
  }
}

/**
 * Transform backend category to frontend format
 */
function transformCategory(backendCategory) {
  if (!backendCategory) return null

  return {
    id: backendCategory.name || 'all',
    name: backendCategory.name || '其他',
    icon: backendCategory.icon || 'landmark'
  }
}

/**
 * Transform backend comment to frontend format
 */
function transformComment(backendComment) {
  if (!backendComment) return null

  const rawContent = backendComment.content || ''
  const replyMatch = rawContent.match(/^\[RT:(\d+):([^\]]+)\](.*)$/s)
  const legacyReplyMatch = !replyMatch ? rawContent.match(/^回复@([^：:]+)[：:](.*)$/s) : null

  const replyToCommentId = replyMatch ? Number(replyMatch[1]) : null
  const replyToUserName = replyMatch
    ? replyMatch[2]
    : (legacyReplyMatch ? legacyReplyMatch[1] : null)
  const content = replyMatch
    ? replyMatch[3]
    : (legacyReplyMatch ? legacyReplyMatch[2] : rawContent)

  return {
    id: backendComment.id,
    userId: backendComment.user?.id?.toString() || 'unknown',
    userName: backendComment.user?.nickname || backendComment.user?.username || '匿名用户',
    userAvatar: (backendComment.user?.nickname || backendComment.user?.username || '匿')?.charAt(0).toUpperCase(),
    content,
    replyToCommentId,
    replyToUserName,
    rating: backendComment.rating,
    likeCount: Number(backendComment.likeCount ?? 0),
    isLikedByCurrentUser: false,
    createdAt: backendComment.createdAt
  }
}

function transformAdminComment(backendComment) {
  if (!backendComment) return null

  const rawContent = backendComment.content || ''
  const replyMatch = rawContent.match(/^\[RT:(\d+):([^\]]+)\](.*)$/s)
  const legacyReplyMatch = !replyMatch ? rawContent.match(/^鍥炲@([^锛?]+)[锛?](.*)$/s) : null

  const content = replyMatch
    ? replyMatch[3]
    : (legacyReplyMatch ? legacyReplyMatch[2] : rawContent)

  return {
    id: backendComment.id,
    sceneryId: backendComment.scenery?.id ?? null,
    sceneryName: backendComment.scenery?.name || '未命名景点',
    userId: backendComment.user?.id?.toString() || 'unknown',
    userName: backendComment.user?.nickname || backendComment.user?.username || '匿名用户',
    userRole: (backendComment.user?.role || 'USER').toUpperCase(),
    content,
    rating: backendComment.rating,
    likeCount: Number(backendComment.likeCount ?? 0),
    createdAt: backendComment.createdAt
  }
}

/**
 * Scenery API Service
 */
export const sceneryService = {
  /**
   * Upload local image file
   * POST /api/upload/image
   * @param {File} file
   * @returns {Promise<string>} uploaded url path, e.g. /uploads/xxx.jpg
   */
  async uploadImage(file) {
    if (!file) {
      throw new Error('请选择图片文件')
    }

    const token = getToken()
    const formData = new FormData()
    formData.append('file', file)

    const headers = {}
    if (token) {
      headers['Authorization'] = `Bearer ${token}`
    }

    let response
    try {
      response = await fetch(`${BASE_URL}/upload/image`, {
        method: 'POST',
        headers,
        body: formData
      })
    } catch (error) {
      throw new Error('无法连接上传接口，请确认后端服务已启动')
    }

    if (response.status === 401) {
      localStorage.removeItem('currentUser')
      localStorage.removeItem('rememberMe')
      throw new Error('登录已过期，请重新登录后再上传')
    }

    const text = await response.text()
    let result = null
    try {
      result = text ? JSON.parse(text) : null
    } catch (e) {
      if (!response.ok) {
        throw new Error(`图片上传失败（HTTP ${response.status}）`)
      }
      throw new Error('上传返回格式异常，请稍后重试')
    }

    if (!response.ok || result?.code !== 200) {
      throw new Error(result?.message || `图片上传失败（HTTP ${response.status}）`)
    }

    return result.data?.url
  },

  /**
   * Get scenery list with pagination and filters
   * GET /api/scenery
   * @param {Object} params - { page, limit, category, search, sort }
   */
  async getSceneryList(params = {}) {
    if (!USE_REAL_API) {
      await delay(300)
      const { mockSceneryList } = await import('../data/scenery.js')
      let result = [...mockSceneryList]

      if (params.category && params.category !== 'all') {
        result = result.filter(s => s.category === params.category)
      }

      if (params.search) {
        const searchLower = params.search.toLowerCase()
        result = result.filter(s =>
          s.title.toLowerCase().includes(searchLower) ||
          s.description.toLowerCase().includes(searchLower)
        )
      }

      const page = params.page || 1
      const limit = params.limit || 12
      const start = (page - 1) * limit
      const items = result.slice(start, start + limit)

      return {
        items,
        total: result.length,
        page,
        limit,
        totalPages: Math.ceil(result.length / limit)
      }
    }

    const queryParams = new URLSearchParams()
    queryParams.set('page', (params.page || 1) - 1)  // Spring Data Page is 0-based
    queryParams.set('size', params.limit || 10)  // Default 10 per documentation

    if (params.search) {
      queryParams.set('name', params.search)
    }

    if (params.category && params.category !== 'all') {
      // category name is used to filter by category name on backend
      queryParams.set('category', params.category)
    }

    if (params.sort) {
      const sortMapping = {
        newest: { sortBy: 'createdAt', sortDir: 'desc' },
        rating: { sortBy: 'rating', sortDir: 'desc' },
        reviews: { sortBy: 'viewCount', sortDir: 'desc' }
      }
      const mapped = sortMapping[params.sort] || sortMapping.newest
      queryParams.set('sortBy', mapped.sortBy)
      queryParams.set('sortDir', mapped.sortDir)
    }

    const response = await fetchJSON(`${BASE_URL}/scenery?${queryParams}`)
    const page = response.data

    return {
      items: page.content.map(transformScenery),
      total: page.totalElements,
      page: page.number,
      limit: page.size,
      totalPages: page.totalPages
    }
  },

  /**
   * Get scenery detail by ID
   * GET /api/scenery/:id
   * @param {string|number} id
   */
  async getSceneryDetail(id) {
    if (!USE_REAL_API) {
      await delay(200)
      const { mockSceneryList } = await import('../data/scenery.js')
      const scenery = mockSceneryList.find(s => s.id === id)
      if (!scenery) throw new Error('Scenery not found')
      return scenery
    }

    const response = await fetchJSON(`${BASE_URL}/scenery/${id}`)
    const data = response.data
    return transformScenery(data.scenery)
  },

  /**
   * Get scenery rating summary
   * GET /api/scenery/:id/rating/summary
   */
  async getSceneryRatingSummary(id) {
    const response = await fetchJSON(`${BASE_URL}/scenery/${id}/rating/summary`)
    return {
      averageScore: Number(response.data?.averageScore ?? 0),
      ratingCount: Number(response.data?.ratingCount ?? 0)
    }
  },

  /**
   * Get current user's rating for scenery
   * GET /api/scenery/:id/rating/my
   */
  async getMySceneryRating(id) {
    const response = await fetchJSON(`${BASE_URL}/scenery/${id}/rating/my`)
    const score = response.data?.score
    return {
      score: score == null ? null : Number(score),
      canRate: !!response.data?.canRate,
      alreadyRated: !!response.data?.alreadyRated,
      hasPurchased: !!response.data?.hasPurchased,
      reason: response.data?.reason || ''
    }
  },

  /**
   * Rate scenery (0.5 - 5.0, step 0.5)
   * POST /api/scenery/:id/rating
   */
  async rateScenery(id, score) {
    const response = await fetchJSON(`${BASE_URL}/scenery/${id}/rating`, {
      method: 'POST',
      body: JSON.stringify({ score })
    })
    return {
      score: Number(response.data?.score ?? score),
      averageScore: Number(response.data?.averageScore ?? 0),
      ratingCount: Number(response.data?.ratingCount ?? 0)
    }
  },

  /**
   * Get all categories
   * GET /api/categories
   */
  async getCategories() {
    if (!USE_REAL_API) {
      await delay(100)
      const { mockCategories } = await import('../data/scenery.js')
      return mockCategories
    }

    const response = await fetchJSON(`${BASE_URL}/categories`)
    const categories = response.data

    return [
      { id: 'all', name: '全部', icon: 'globe' },
      ...categories.map(transformCategory)
    ]
  },

  /**
   * Toggle favorite status
   * POST /api/scenery/:id/favorite
   * Requires JWT token in Authorization header (userId extracted from token)
   * @param {string|number} id - scenery id
   * @returns {string} - "favorited" or "unfavorited"
   */
  async toggleFavorite(id) {
    if (!USE_REAL_API) {
      await delay(150)
      const favorites = JSON.parse(localStorage.getItem('favorites') || '[]')
      const index = favorites.indexOf(id)
      if (index > -1) {
        favorites.splice(index, 1)
        localStorage.setItem('favorites', JSON.stringify(favorites))
        return 'unfavorited'
      } else {
        favorites.push(id)
        localStorage.setItem('favorites', JSON.stringify(favorites))
        return 'favorited'
      }
    }

    const response = await fetchJSON(`${BASE_URL}/scenery/${id}/favorite`, {
      method: 'POST'
    })
    return response.data
  },

  /**
   * Check if scenery is favorited by current user
   * @param {string|number} id - scenery id
   */
  async isFavorited(id) {
    if (!USE_REAL_API) {
      const favorites = JSON.parse(localStorage.getItem('favorites') || '[]')
      return favorites.includes(id)
    }

    const response = await fetchJSON(`${BASE_URL}/scenery/${id}/favorite/check`)
    return response.data
  },

  /**
   * Get comments for scenery
   * GET /api/scenery/:id/comments
   * @param {string|number} id
   */
  async getComments(id) {
    if (!USE_REAL_API) {
      await delay(200)
      const { mockComments } = await import('../data/scenery.js')
      return mockComments[id] || []
    }

    const response = await fetchJSON(`${BASE_URL}/scenery/${id}/comments?page=0&size=100`)
    const page = response.data
    return page.content.map(transformComment)
  },

  /**
   * Add comment to scenery
   * POST /api/scenery/:id/comments
   * Requires JWT token in Authorization header (userId extracted from token)
   * @param {string|number} id - scenery id
   * @param {string} content - comment content
   * @param {Object} options - { rating }
   */
  async addComment(id, content, options = {}) {
    const { rating = 5, replyToCommentId = null } = options

    if (!USE_REAL_API) {
      await delay(300)
      const { mockComments } = await import('../data/scenery.js')
      const newComment = {
        id: Date.now(),
        userId: 'current_user',
        userName: '我',
        userAvatar: '我',
        content,
        createdAt: new Date().toISOString()
      }
      if (!mockComments[id]) mockComments[id] = []
      mockComments[id].unshift(newComment)
      return newComment
    }

    const response = await fetchJSON(`${BASE_URL}/scenery/${id}/comments`, {
      method: 'POST',
      body: JSON.stringify({
        content,
        rating,
        replyToCommentId
      })
    })

    return transformComment(response.data)
  },

  /**
   * Delete a comment
   * DELETE /api/scenery/:id/comments/:commentId
   * Requires JWT token in Authorization header
   * @param {string|number} sceneryId
   * @param {string|number} commentId
   */
  async deleteComment(sceneryId, commentId) {
    await fetchJSON(`${BASE_URL}/scenery/${sceneryId}/comments/${commentId}`, {
      method: 'DELETE'
    })
  },

  /**
   * Toggle like status for a comment
   * POST /api/scenery/:sceneryId/comments/:commentId/like
   */
  async toggleCommentLike(sceneryId, commentId) {
    const response = await fetchJSON(`${BASE_URL}/scenery/${sceneryId}/comments/${commentId}/like`, {
      method: 'POST'
    })
    return response.data
  },

  /**
   * Check if current user liked a comment
   * GET /api/scenery/:sceneryId/comments/:commentId/like/check
   */
  async isCommentLiked(sceneryId, commentId) {
    const response = await fetchJSON(`${BASE_URL}/scenery/${sceneryId}/comments/${commentId}/like/check`)
    return !!response.data
  },

  /**
   * Get current user's favorite scenery list
   * GET /api/favorites
   */
  async getMyFavorites(params = {}) {
    const queryParams = new URLSearchParams()
    queryParams.set('page', params.page ?? 0)
    queryParams.set('size', params.size ?? 100)

    const response = await fetchJSON(`${BASE_URL}/favorites?${queryParams}`)
    const page = response.data

    return {
      items: (page.content || []).map(transformScenery),
      total: page.totalElements || 0
    }
  }
}

export const adminService = {
  async listCategories() {
    const response = await fetchJSON(`${BASE_URL}/categories`)
    return response.data || []
  },

  async createCategory(payload) {
    const response = await fetchJSON(`${BASE_URL}/categories`, {
      method: 'POST',
      body: JSON.stringify(payload)
    })
    return response.data
  },

  async updateCategory(id, payload) {
    const response = await fetchJSON(`${BASE_URL}/categories/${id}`, {
      method: 'PUT',
      body: JSON.stringify(payload)
    })
    return response.data
  },

  async deleteCategory(id) {
    await fetchJSON(`${BASE_URL}/categories/${id}`, {
      method: 'DELETE'
    })
  },

  async listScenery(params = {}) {
    const queryParams = new URLSearchParams()
    queryParams.set('page', params.page ?? 0)
    queryParams.set('size', params.size ?? 50)
    queryParams.set('sortBy', params.sortBy || 'createdAt')
    queryParams.set('sortDir', params.sortDir || 'desc')

    const response = await fetchJSON(`${BASE_URL}/scenery?${queryParams}`)
    const page = response.data

    return {
      items: (page.content || []).map(transformScenery),
      total: page.totalElements || 0
    }
  },

  async createScenery(payload) {
    const response = await fetchJSON(`${BASE_URL}/scenery`, {
      method: 'POST',
      body: JSON.stringify(payload)
    })
    return transformScenery(response.data)
  },

  async updateScenery(id, payload) {
    const response = await fetchJSON(`${BASE_URL}/scenery/${id}`, {
      method: 'PUT',
      body: JSON.stringify(payload)
    })
    return transformScenery(response.data)
  },

  async deleteScenery(id) {
    await fetchJSON(`${BASE_URL}/scenery/${id}`, {
      method: 'DELETE'
    })
  },

  async listComments(params = {}) {
    const queryParams = new URLSearchParams()
    queryParams.set('page', params.page ?? 0)
    queryParams.set('size', params.size ?? 20)

    if (params.sceneryId) {
      queryParams.set('sceneryId', params.sceneryId)
    }

    if (params.keyword) {
      queryParams.set('keyword', params.keyword)
    }

    const response = await fetchJSON(`${BASE_URL}/comments?${queryParams}`)
    const page = response.data

    return {
      items: (page.content || []).map(transformAdminComment),
      total: page.totalElements || 0,
      page: page.number || 0,
      totalPages: page.totalPages || 0
    }
  },

  async deleteComment(sceneryId, commentId) {
    await fetchJSON(`${BASE_URL}/scenery/${sceneryId}/comments/${commentId}`, {
      method: 'DELETE'
    })
  }
}
