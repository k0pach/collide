import axios from 'axios'

const TOKEN_KEY = 'collide.jwt'
const USER_KEY = 'collide.user'

export const getStoredToken = () => localStorage.getItem(TOKEN_KEY)
export const getStoredUser = () => {
  try {
    const raw = localStorage.getItem(USER_KEY)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

export const saveAuthSession = ({ token, user }) => {
  if (token) localStorage.setItem(TOKEN_KEY, token)
  if (user) localStorage.setItem(USER_KEY, JSON.stringify(user))
}

export const clearAuthSession = () => {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}

export const api = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

api.interceptors.request.use((config) => {
  const token = getStoredToken()
  const storedUser = getStoredUser()

  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }

  if (storedUser?.id && !config.headers['X-User-Id']) {
    config.headers['X-User-Id'] = storedUser.id
  }

  return config
})

const unwrap = (response) => response.data

export const authApi = {
  login: (payload) => api.post('/auth/login', payload).then(unwrap),
  register: (payload) => api.post('/auth/register', payload).then(unwrap),
  me: () => api.get('/auth/me').then(unwrap),
}

export const categoriesApi = {
  list: () => api.get('/categories').then(unwrap),
}

export const usersApi = {
  me: () => api.get('/users/me').then(unwrap),
  updateMe: (payload) => api.put('/users/me', payload).then(unwrap),
  get: (id) => api.get(`/users/${id}`).then(unwrap),
  stats: (id) => api.get(`/users/${id}/stats`).then(unwrap),
  follow: (id) => api.post(`/users/${id}/follow`).then(unwrap),
  unfollow: (id) => api.delete(`/users/${id}/follow`).then(unwrap),
}

export const collectionsApi = {
  list: (params = {}) => api.get('/collections', { params }).then(unwrap),
  detail: (id, params = {}) => api.get(`/collections/${id}`, { params }).then(unwrap),
  create: (payload) => api.post('/collections', payload).then(unwrap),
  update: (id, payload) => api.put(`/collections/${id}`, payload).then(unwrap),
  delete: (id) => api.delete(`/collections/${id}`).then(unwrap),
  removeItem: (collectionId, itemId) => api.delete(`/collections/${collectionId}/items/${itemId}`).then(unwrap),
  rate: (id, rating) => api.post(`/collections/${id}/rating`, { rating }).then(unwrap),
  favorite: (id) => api.post(`/collections/${id}/favorite`).then(unwrap),
  comments: (id) => api.get(`/collections/${id}/comments`).then(unwrap),
  addComment: (id, body) => api.post(`/collections/${id}/comments`, { body }).then(unwrap),
}

export const itemsApi = {
  list: (params = {}) => api.get('/items', { params }).then(unwrap),
  detail: (id) => api.get(`/items/${id}`).then(unwrap),
  create: (payload) => api.post('/items', payload).then(unwrap),
  update: (id, payload) => api.put(`/items/${id}`, payload).then(unwrap),
  delete: (id) => api.delete(`/items/${id}`).then(unwrap),
  like: (id) => api.post(`/items/${id}/like`).then(unwrap),
  favorite: (id) => api.post(`/items/${id}/favorite`).then(unwrap),
  comments: (id) => api.get(`/items/${id}/comments`).then(unwrap),
  addComment: (id, body) => api.post(`/items/${id}/comments`, { body }).then(unwrap),
}

export const favoritesApi = {
  list: (params = {}) => api.get('/favorites', { params }).then(unwrap),
}

export const chatsApi = {
  list: (params = {}) => api.get('/chats', { params }).then(unwrap),
  create: (companionId) => api.post('/chats', { companionId }).then(unwrap),
  messages: (chatId) => api.get(`/chats/${chatId}/messages`).then(unwrap),
  send: (chatId, body) => api.post(`/chats/${chatId}/messages`, { body }).then(unwrap),
  markRead: (chatId) => api.post(`/chats/${chatId}/read`).then(unwrap),
}

export const uploadImageFile = async (file) => {
  if (!file) return null
  const formData = new FormData()
  formData.append('file', file)
  return api.post('/uploads/images', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  }).then(unwrap)
}

export const imagePlaceholder = (title = 'Collide') => title
