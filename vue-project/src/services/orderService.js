const BASE_URL = '/api'
const IMAGE_PLACEHOLDER = 'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="800" height="600" viewBox="0 0 800 600"><defs><linearGradient id="g" x1="0" y1="0" x2="1" y2="1"><stop offset="0%" stop-color="%23dbeafe"/><stop offset="100%" stop-color="%23bfdbfe"/></linearGradient></defs><rect width="800" height="600" fill="url(%23g)"/><text x="50%" y="50%" dominant-baseline="middle" text-anchor="middle" fill="%231e3a8a" font-size="34" font-family="Arial, sans-serif">WorldThings</text></svg>'

function getToken() {
  try {
    const user = JSON.parse(localStorage.getItem('currentUser') || '{}')
    return user.token || null
  } catch (error) {
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
  return raw.startsWith('/') ? raw : `/uploads/${raw}`
}

async function fetchJSON(url, options = {}) {
  const token = getToken()

  const headers = {
    'Content-Type': 'application/json',
    ...options.headers
  }
  if (token) {
    headers.Authorization = `Bearer ${token}`
  }

  const response = await fetch(url, {
    ...options,
    headers
  })

  if (response.status === 401) {
    localStorage.removeItem('currentUser')
    localStorage.removeItem('rememberMe')
    window.location.reload()
    throw new Error('登录已过期，请重新登录')
  }

  const text = await response.text()
  let payload = null
  try {
    payload = text ? JSON.parse(text) : null
  } catch (e) {
    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`)
    }
    return null
  }

  if (!response.ok) {
    throw new Error(payload?.message || `HTTP ${response.status}`)
  }
  return payload
}

function normalizeAmount(value) {
  const num = Number(value ?? 0)
  return Number.isFinite(num) ? num : 0
}

function transformOrder(raw) {
  return {
    id: raw?.id,
    orderNo: raw?.orderNo || '',
    sceneryId: raw?.scenery?.id ?? null,
    sceneryName: raw?.scenery?.name || '未知景点',
    sceneryImage: resolveImageUrl(raw?.scenery?.coverImage),
    sceneryLocation: raw?.scenery?.location || '',
    userName: raw?.user?.nickname || raw?.user?.username || '',
    bookingDays: Number(raw?.bookingDays ?? 1),
    travelerCount: Number(raw?.travelerCount ?? 1),
    travelDate: raw?.travelDate || null,
    note: raw?.note || '',
    status: raw?.status || 'CART',
    unitPrice: normalizeAmount(raw?.unitPrice),
    totalAmount: normalizeAmount(raw?.totalAmount),
    paidAt: raw?.paidAt || null,
    reviewedAt: raw?.reviewedAt || null,
    rejectReason: raw?.rejectReason || '',
    createdAt: raw?.createdAt || null
  }
}

export const cartService = {
  async listCart() {
    const response = await fetchJSON(`${BASE_URL}/cart`)
    return (response?.data || []).map(transformOrder)
  },

  async addToCart(payload) {
    const response = await fetchJSON(`${BASE_URL}/cart`, {
      method: 'POST',
      body: JSON.stringify(payload)
    })
    return transformOrder(response?.data || {})
  },

  async removeCartItem(orderId) {
    await fetchJSON(`${BASE_URL}/cart/${orderId}`, {
      method: 'DELETE'
    })
  },

  async payCart() {
    const response = await fetchJSON(`${BASE_URL}/cart/pay`, {
      method: 'POST'
    })
    return {
      paidCount: Number(response?.data?.paidCount ?? 0),
      totalAmount: normalizeAmount(response?.data?.totalAmount),
      paidAt: response?.data?.paidAt || null
    }
  }
}

export const orderService = {
  async listMyOrders(params = {}) {
    const query = new URLSearchParams()
    query.set('page', params.page ?? 0)
    query.set('size', params.size ?? 20)
    if (params.status) {
      query.set('status', params.status)
    }

    const response = await fetchJSON(`${BASE_URL}/orders/my?${query}`)
    const page = response?.data || {}
    return {
      items: (page.content || []).map(transformOrder),
      total: Number(page.totalElements || 0),
      page: Number(page.number || 0),
      totalPages: Number(page.totalPages || 0)
    }
  }
}

export const adminOrderService = {
  async listOrders(params = {}) {
    const query = new URLSearchParams()
    query.set('page', params.page ?? 0)
    query.set('size', params.size ?? 20)
    if (params.status) query.set('status', params.status)
    if (params.sceneryId) query.set('sceneryId', params.sceneryId)
    if (params.keyword) query.set('keyword', params.keyword)

    const response = await fetchJSON(`${BASE_URL}/admin/orders?${query}`)
    const page = response?.data || {}
    return {
      items: (page.content || []).map(transformOrder),
      total: Number(page.totalElements || 0),
      page: Number(page.number || 0),
      totalPages: Number(page.totalPages || 0)
    }
  },

  async reviewOrder(orderId, payload) {
    const response = await fetchJSON(`${BASE_URL}/admin/orders/${orderId}/review`, {
      method: 'POST',
      body: JSON.stringify(payload)
    })
    return transformOrder(response?.data || {})
  }
}
