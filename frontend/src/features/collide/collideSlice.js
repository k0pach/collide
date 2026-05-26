import { createAsyncThunk, createSelector, createSlice } from '@reduxjs/toolkit'
import {
  authApi,
  categoriesApi,
  chatsApi,
  clearAuthSession,
  collectionsApi,
  favoritesApi,
  getStoredToken,
  getStoredUser,
  itemsApi,
  saveAuthSession,
  usersApi,
} from '../../services/api.js'

const placeholderPalette = ['#FD3E3E', '#FB8500', '#FFB703', '#F3D5B5', '#BC8A5F', '#A47148', '#6F4518']

const defaultCategories = [
  { id: 'all', slug: 'all', title: 'Все категории', sortOrder: 0 },
  { id: 'figures', slug: 'figures', title: 'Фигурки', sortOrder: 10 },
  { id: 'coins', slug: 'coins', title: 'Монеты', sortOrder: 20 },
  { id: 'records', slug: 'records', title: 'Пластинки', sortOrder: 30 },
  { id: 'books', slug: 'books', title: 'Книги', sortOrder: 40 },
  { id: 'watches', slug: 'watches', title: 'Часы', sortOrder: 50 },
  { id: 'retro_tech', slug: 'retro_tech', title: 'Ретро-техника', sortOrder: 60 },
  { id: 'table_games', slug: 'table_games', title: 'Настольные игры', sortOrder: 70 },
  { id: 'minerals', slug: 'minerals', title: 'Минералы', sortOrder: 80 },
]

const storedToken = getStoredToken()
const storedUser = getStoredUser()
const normalizedStoredUser = storedUser ? normalizeUser(storedUser) : null

const initialState = {
  categories: defaultCategories,
  users: normalizedStoredUser ? [normalizedStoredUser] : [],
  collections: [],
  items: [],
  dialogs: [],
  currentUserId: normalizedStoredUser?.id || null,
  profileStatsByUserId: {},
  favorites: { collections: [], items: [] },
  selectedCategory: 'all',
  searchQuery: '',
  profileSearchQuery: '',
  chatSearchQuery: '',
  activeDialogId: null,
  isAddItemModalOpen: false,
  isAddCollectionModalOpen: false,
  auth: {
    token: storedToken,
    user: normalizedStoredUser,
    status: 'idle',
    error: null,
  },
  apiStatus: 'idle',
  apiError: null,
  usingFallback: false,
}

function normalizeUser(user = {}) {
  return {
    id: String(user.id || user.uuid || 'u1'),
    username: user.username || String(user.handle || '@user').replace('@', ''),
    name: user.name || user.displayName || 'Пользователь',
    displayName: user.displayName || user.name || 'Пользователь',
    handle: user.handle || `@${user.username || 'user'}`,
    status: user.status || user.statusMessage || 'Заходил недавно',
    avatarUrl: user.avatarUrl || '',
    avatarTone: user.avatarTone || 'orange',
    about: user.about || user.bio || '',
    following: Boolean(user.following),
    followersBy: user.followersBy || [],
  }
}

function normalizeCategory(category = {}) {
  const slug = category.slug || category.id || category.title || ''
  return {
    uuid: category.uuid || category.id || slug,
    id: slug,
    slug,
    title: category.title || slug,
    sortOrder: category.sortOrder || 0,
  }
}

function normalizeComment(comment = {}) {
  const date = comment.createdAt ? new Date(comment.createdAt) : null
  return {
    id: String(comment.id || makeId('comment')),
    authorId: String(comment.authorId || ''),
    authorName: comment.authorName || 'Пользователь',
    authorHandle: comment.authorHandle || '',
    text: comment.text || comment.body || '',
    body: comment.body || comment.text || '',
    time: date && !Number.isNaN(date.getTime())
        ? date.toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit' })
        : comment.time || '',
    createdAt: comment.createdAt || null,
  }
}

function normalizeCollection(collection = {}, currentUserId = '') {
  const category = collection.category ? normalizeCategory(collection.category) : null
  const id = String(collection.id || makeId('c'))
  const comments = Array.isArray(collection.comments) ? collection.comments.map(normalizeComment) : []
  const favorite = Boolean(collection.favorite || collection.isFavorite)

  return {
    id,
    ownerId: String(collection.ownerId || ''),
    ownerName: collection.ownerName || 'Пользователь',
    ownerHandle: collection.ownerHandle || '',
    title: collection.title || 'Без названия',
    category: collection.categorySlug || category?.slug || collection.category || 'figures',
    description: collection.description || '',
    coverTone: collection.coverTone || 'orange',
    coverImageUrl: collection.coverImageUrl || '',
    placeholderColor: collection.placeholderColor || getWarmPlaceholderColor(id),
    popularity: Number(collection.popularity || collection.itemsCount || collection.ratingCount || 0),
    createdAt: timestampValue(collection.createdAt),
    createdAtRaw: collection.createdAt || null,
    itemsCount: Number(collection.itemsCount || 0),
    totalValue: Number(collection.totalValue || collection.totalPriceAmount || 0),
    totalValueLabel: collection.totalValueLabel || formatMoney(collection.totalValue || 0),
    averageRating: Number(collection.averageRating || 0),
    ratingsCount: Number(collection.ratingsCount || collection.ratingCount || 0),
    commentsCount: Number(collection.commentCount || collection.commentsCount || comments.length),
    comments,
    favoritedBy: favorite && currentUserId ? [currentUserId] : collection.favoritedBy || [],
    isFavorite: favorite,
    items: Array.isArray(collection.items) ? collection.items.map((item) => normalizeItem(item, currentUserId)) : [],
  }
}

function normalizeItem(item = {}, currentUserId = '') {
  const category = item.category ? normalizeCategory(item.category) : null
  const id = String(item.id || makeId('i'))
  const comments = Array.isArray(item.comments) ? item.comments.map(normalizeComment) : []
  const liked = Boolean(item.liked || item.isLiked)
  const favorite = Boolean(item.favorite || item.isFavorite)
  const shortDescription = item.shortDescription || item.description || ''

  return {
    id,
    ownerId: String(item.ownerId || ''),
    ownerName: item.ownerName || item.owner?.name || 'Пользователь',
    ownerHandle: item.ownerHandle || item.owner?.handle || '',
    collectionId: item.collectionId ? String(item.collectionId) : '',
    collectionTitle: item.collectionTitle || 'Без коллекции',
    title: item.title || 'Без названия',
    category: item.categorySlug || category?.slug || item.category || 'figures',
    status: item.status || 'collection',
    statusLabel: item.statusLabel || statusLabels[item.status] || 'В коллекции',
    price: item.price || formatMoney(item.priceAmount),
    priceAmount: Number(item.priceAmount || parsePriceAmount(item.price || 0) || 0),
    description: String(shortDescription).slice(0, 60),
    shortDescription: String(shortDescription).slice(0, 60),
    fullDescription: item.fullDescription || item.description || '',
    imageTone: item.imageTone || 'orange',
    imageUrl: item.imageUrl || '',
    placeholderColor: item.placeholderColor || getWarmPlaceholderColor(id),
    popularity: Number(item.popularity || item.likesCount || item.likes || 0),
    createdAt: timestampValue(item.createdAt),
    createdAtRaw: item.createdAt || null,
    likes: Number(item.likesCount ?? item.likes ?? 0),
    commentsCount: Number(item.commentsCount || comments.length),
    likedBy: liked && currentUserId ? [currentUserId] : item.likedBy || [],
    favoritedBy: favorite && currentUserId ? [currentUserId] : item.favoritedBy || [],
    comments,
    owner: item.owner ? normalizeUser(item.owner) : null,
  }
}

function normalizeDialog(dialog = {}) {
  return {
    id: String(dialog.id || makeId('d')),
    userId: String(dialog.companionId || dialog.userId || ''),
    companionId: String(dialog.companionId || dialog.userId || ''),
    name: dialog.name || 'Диалог',
    handle: dialog.handle || '',
    preview: dialog.preview || 'Нет сообщений',
    unread: Number(dialog.unread || 0),
    avatarTone: dialog.avatarTone || 'cream',
    updatedAt: timestampValue(dialog.updatedAt),
    updatedAtRaw: dialog.updatedAt || null,
    messages: Array.isArray(dialog.messages) ? dialog.messages.map(normalizeMessage) : [],
  }
}

function normalizeMessage(message = {}) {
  const date = message.createdAt ? new Date(message.createdAt) : null
  return {
    id: String(message.id || makeId('m')),
    chatId: String(message.chatId || ''),
    from: String(message.senderId || message.from || ''),
    senderId: String(message.senderId || message.from || ''),
    senderName: message.senderName || '',
    text: message.text || message.body || '',
    body: message.body || message.text || '',
    mine: Boolean(message.mine),
    time: date && !Number.isNaN(date.getTime())
        ? date.toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit' })
        : message.time || '',
    createdAt: timestampValue(message.createdAt),
  }
}

function normalizeStats(stats = {}) {
  return {
    collectionAverageRating: Number(stats.collectionAverageRating || 0) || '—',
    totalItemLikes: Number(stats.totalItemLikes || 0),
    followersCount: Number(stats.followersCount || 0),
    followingCount: Number(stats.followingCount || 0),
    totalCollectionsValue: Number(stats.totalCollectionsValue || 0),
    totalCollectionsValueLabel: stats.totalCollectionsValueLabel || formatMoney(stats.totalCollectionsValue || 0),
    collectionsCount: Number(stats.collectionsCount || 0),
    itemsCount: Number(stats.itemsCount || 0),
  }
}

const statusLabels = {
  collection: 'В коллекции',
  sale: 'В продаже',
  exchange: 'Для обмена',
  archive: 'В архиве',
}

const makeId = (prefix) => `${prefix}-${Date.now()}-${Math.random().toString(16).slice(2)}`
const normalize = (value) => String(value || '').trim().toLowerCase()
const timestampValue = (value) => {
  if (!value) return 0
  if (typeof value === 'number') return value
  const time = new Date(value).getTime()
  return Number.isNaN(time) ? 0 : time
}
const parsePriceAmount = (value) => Number(String(value || '').replace(',', '.').replace(/[^0-9.]/g, '') || 0)
const formatMoney = (value) => `${new Intl.NumberFormat('ru-RU').format(Number(value || 0))} ₽`
const getWarmPlaceholderColor = (seed = '') => placeholderPalette[Math.abs(String(seed).split('').reduce((sum, char) => sum + char.charCodeAt(0), 0)) % placeholderPalette.length]

function upsertById(list, entity) {
  const id = String(entity.id)
  const index = list.findIndex((entry) => String(entry.id) === id)
  if (index >= 0) {
    list[index] = { ...list[index], ...entity }
  } else {
    list.unshift(entity)
  }
}

function removeById(list, id) {
  return list.filter((entry) => String(entry.id) !== String(id))
}

function toCollectionRequest(payload = {}) {
  return {
    title: payload.title || 'Новая коллекция',
    description: payload.description || '',
    category: payload.category || payload.categorySlug || null,
    categorySlug: payload.categorySlug || payload.category || null,
    coverImageUrl: payload.coverImageUrl || '',
  }
}

function toItemRequest(payload = {}) {
  const priceAmount = payload.priceAmount ?? parsePriceAmount(payload.price)
  return {
    title: payload.title || 'Новый предмет',
    collectionId: payload.collectionId || null,
    category: payload.category || payload.categorySlug || null,
    categorySlug: payload.categorySlug || payload.category || null,
    status: payload.status || 'collection',
    priceAmount: priceAmount ? Number(priceAmount) : null,
    price: payload.price || '',
    description: String(payload.description || payload.shortDescription || '').slice(0, 60),
    shortDescription: String(payload.shortDescription || payload.description || '').slice(0, 60),
    fullDescription: payload.fullDescription || payload.description || '',
    imageUrl: payload.imageUrl || '',
  }
}

async function loadCommonData() {
  const token = getStoredToken()
  const hasSession = Boolean(token)

  const [categories, collections, items] = await Promise.all([
    categoriesApi.list(),
    collectionsApi.list(),
    itemsApi.list(),
  ])

  const currentUser = hasSession
      ? normalizeUser(await authApi.me())
      : null
  const currentUserId = currentUser?.id || null

  const [chats, favorites, stats] = hasSession && currentUserId
      ? await Promise.all([
        chatsApi.list().catch(() => []),
        favoritesApi.list().catch(() => ({ collections: [], items: [] })),
        usersApi.stats(currentUserId).catch(() => null),
      ])
      : [[], { collections: [], items: [] }, null]

  const normalizedCollections = collections.map((collection) => normalizeCollection(collection, currentUserId))
  const normalizedItems = items.map((item) => normalizeItem(item, currentUserId))
  const normalizedFavorites = {
    collections: (favorites.collections || []).map((collection) => ({ ...normalizeCollection(collection, currentUserId), isFavorite: true, favoritedBy: currentUserId ? [currentUserId] : [] })),
    items: (favorites.items || []).map((item) => ({ ...normalizeItem(item, currentUserId), isFavorite: true, favoritedBy: currentUserId ? [currentUserId] : [] })),
  }
  const favoriteCollectionIds = new Set(normalizedFavorites.collections.map((collection) => String(collection.id)))
  const favoriteItemIds = new Set(normalizedFavorites.items.map((item) => String(item.id)))

  normalizedCollections.forEach((collection) => {
    if (favoriteCollectionIds.has(String(collection.id)) && currentUserId) collection.favoritedBy = [currentUserId]
  })
  normalizedItems.forEach((item) => {
    if (favoriteItemIds.has(String(item.id)) && currentUserId) item.favoritedBy = [currentUserId]
  })

  const normalizedDialogs = hasSession
      ? await Promise.all(chats.map(async (dialog) => {
        const normalizedDialog = normalizeDialog(dialog)
        try {
          const messages = await chatsApi.messages(normalizedDialog.id)
          normalizedDialog.messages = messages.map(normalizeMessage)
        } catch {
          normalizedDialog.messages = []
        }
        return normalizedDialog
      }))
      : []

  return {
    categories: [{ id: 'all', slug: 'all', title: 'Все категории', sortOrder: 0 }, ...categories.map(normalizeCategory)],
    currentUser,
    collections: normalizedCollections,
    items: normalizedItems,
    dialogs: normalizedDialogs,
    favorites: normalizedFavorites,
    stats: stats ? normalizeStats(stats) : null,
  }
}

export const loadInitialData = createAsyncThunk('collide/loadInitialData', async (_, { rejectWithValue }) => {
  try {
    return await loadCommonData()
  } catch (error) {
    return rejectWithValue(error.response?.data?.message || error.message || 'Backend недоступен')
  }
})

export const loginUser = createAsyncThunk('collide/loginUser', async (payload, { rejectWithValue }) => {
  try {
    const response = await authApi.login(payload)
    saveAuthSession(response)
    const common = await loadCommonData()
    return { ...response, ...common }
  } catch (error) {
    return rejectWithValue(error.response?.data?.message || 'Не удалось войти')
  }
})

export const registerUser = createAsyncThunk('collide/registerUser', async (payload, { rejectWithValue }) => {
  try {
    const response = await authApi.register(payload)
    saveAuthSession(response)
    const common = await loadCommonData()
    return { ...response, ...common }
  } catch (error) {
    return rejectWithValue(error.response?.data?.message || 'Не удалось зарегистрироваться')
  }
})

export const updateUserProfile = createAsyncThunk('collide/updateUserProfile', async (payload, { getState, rejectWithValue }) => {
  try {
    let response = payload
    if (usersApi.update) {
      response = await usersApi.update(payload.id, payload).catch(() => payload)
    } else if (authApi.updateProfile) {
      response = await authApi.updateProfile(payload).catch(() => payload)
    }

    const existingUser = selectCurrentUser(getState())
    return normalizeUser({
      ...existingUser,
      ...response,
      name: payload.name || payload.displayName,
      displayName: payload.name || payload.displayName,
      about: payload.about,
    })
  } catch (error) {
    return rejectWithValue('Не удалось обновить профиль')
  }
})

export const fetchUserProfile = createAsyncThunk('collide/fetchUserProfile', async (userId, { getState, rejectWithValue }) => {
  try {
    const currentUserId = getState().collide.currentUserId
    const [user, stats, collections, items] = await Promise.all([
      usersApi.get(userId),
      usersApi.stats(userId).catch(() => null),
      collectionsApi.list({ ownerId: userId }),
      itemsApi.list({ ownerId: userId }),
    ])
    return {
      user: normalizeUser(user),
      stats: stats ? normalizeStats(stats) : null,
      collections: collections.map((collection) => normalizeCollection(collection, currentUserId)),
      items: items.map((item) => normalizeItem(item, currentUserId)),
    }
  } catch (error) {
    return rejectWithValue(error.response?.data?.message || 'Не удалось загрузить профиль')
  }
})

export const fetchCollectionDetail = createAsyncThunk('collide/fetchCollectionDetail', async ({ id, itemSort } = {}, { getState, rejectWithValue }) => {
  try {
    const currentUserId = getState().collide.currentUserId
    const [detail, comments] = await Promise.all([
      collectionsApi.detail(id, itemSort ? { itemSort } : {}),
      collectionsApi.comments(id).catch(() => []),
    ])
    const collection = normalizeCollection({ ...detail, comments }, currentUserId)
    return { collection, items: (detail.items || []).map((item) => normalizeItem(item, currentUserId)) }
  } catch (error) {
    return rejectWithValue(error.response?.data?.message || 'Не удалось загрузить коллекцию')
  }
})

export const fetchItemDetail = createAsyncThunk('collide/fetchItemDetail', async (id, { getState, rejectWithValue }) => {
  try {
    const currentUserId = getState().collide.currentUserId
    const detail = await itemsApi.detail(id)
    const item = normalizeItem({ ...detail.item, owner: detail.owner, comments: detail.comments || [] }, currentUserId)
    const owner = detail.owner ? normalizeUser(detail.owner) : null
    return { item, owner }
  } catch (error) {
    return rejectWithValue(error.response?.data?.message || 'Не удалось загрузить предмет')
  }
})

export const addCollection = createAsyncThunk('collide/addCollection', async (payload, { getState, rejectWithValue }) => {
  try {
    const currentUserId = getState().collide.currentUserId
    const detail = await collectionsApi.create(toCollectionRequest(payload))
    const comments = await collectionsApi.comments(detail.id).catch(() => [])
    return { collection: normalizeCollection({ ...detail, comments }, currentUserId), items: (detail.items || []).map((item) => normalizeItem(item, currentUserId)) }
  } catch (error) {
    return rejectWithValue(error.response?.data?.message || 'Не удалось создать коллекцию')
  }
})

export const updateCollection = createAsyncThunk('collide/updateCollection', async (payload, { getState, rejectWithValue }) => {
  try {
    const currentUserId = getState().collide.currentUserId
    const detail = await collectionsApi.update(payload.id, toCollectionRequest(payload))
    const comments = await collectionsApi.comments(detail.id).catch(() => [])
    return { collection: normalizeCollection({ ...detail, comments }, currentUserId), items: (detail.items || []).map((item) => normalizeItem(item, currentUserId)) }
  } catch (error) {
    return rejectWithValue(error.response?.data?.message || 'Не удалось обновить коллекцию')
  }
})

export const addItem = createAsyncThunk('collide/addItem', async (payload, { getState, rejectWithValue }) => {
  try {
    const currentUserId = getState().collide.currentUserId
    const detail = await itemsApi.create(toItemRequest(payload))
    return { item: normalizeItem({ ...detail.item, owner: detail.owner, comments: detail.comments || [] }, currentUserId) }
  } catch (error) {
    return rejectWithValue(error.response?.data?.message || 'Не удалось создать предмет')
  }
})

export const updateItem = createAsyncThunk('collide/updateItem', async (payload, { getState, rejectWithValue }) => {
  try {
    const currentUserId = getState().collide.currentUserId
    const detail = await itemsApi.update(payload.id, toItemRequest(payload))
    return { item: normalizeItem({ ...detail.item, owner: detail.owner, comments: detail.comments || [] }, currentUserId) }
  } catch (error) {
    return rejectWithValue(error.response?.data?.message || 'Не удалось обновить предмет')
  }
})

export const deleteItem = createAsyncThunk('collide/deleteItem', async (id, { rejectWithValue }) => {
  try {
    await itemsApi.delete(id)
    return id
  } catch (error) {
    return rejectWithValue(error.response?.data?.message || 'Не удалось удалить предмет')
  }
})

export const setItemStatus = createAsyncThunk('collide/setItemStatus', async ({ id, status }, { getState, rejectWithValue }) => {
  try {
    const existing = selectItemById(id)(getState())
    const detail = await itemsApi.update(id, toItemRequest({ ...existing, status }))
    return { item: normalizeItem({ ...detail.item, owner: detail.owner, comments: detail.comments || [] }, getState().collide.currentUserId) }
  } catch (error) {
    return rejectWithValue(error.response?.data?.message || 'Не удалось изменить статус')
  }
})

export const removeItemFromCollection = createAsyncThunk('collide/removeItemFromCollection', async ({ collectionId, itemId }, { getState, rejectWithValue }) => {
  try {
    const currentUserId = getState().collide.currentUserId
    const detail = await collectionsApi.removeItem(collectionId, itemId)
    return {
      collection: normalizeCollection(detail, currentUserId),
      items: (detail.items || []).map((item) => normalizeItem(item, currentUserId)),
      itemId,
    }
  } catch (error) {
    return rejectWithValue(error.response?.data?.message || 'Не удалось убрать предмет из коллекции')
  }
})

export const toggleItemLike = createAsyncThunk('collide/toggleItemLike', async (id, { rejectWithValue }) => {
  try {
    const result = await itemsApi.like(id)
    return { id, liked: Boolean(result.liked) }
  } catch (error) {
    return rejectWithValue(error.response?.data?.message || 'Не удалось поставить лайк')
  }
})

export const toggleFavoriteItem = createAsyncThunk('collide/toggleFavoriteItem', async (id, { rejectWithValue }) => {
  try {
    const result = await itemsApi.favorite(id)
    return { id, favorite: Boolean(result.favorite) }
  } catch (error) {
    return rejectWithValue(error.response?.data?.message || 'Не удалось изменить избранное')
  }
})

export const toggleFavoriteCollection = createAsyncThunk('collide/toggleFavoriteCollection', async (id, { rejectWithValue }) => {
  try {
    const result = await collectionsApi.favorite(id)
    return { id, favorite: Boolean(result.favorite) }
  } catch (error) {
    return rejectWithValue(error.response?.data?.message || 'Не удалось изменить избранное')
  }
})

export const toggleFollowUser = createAsyncThunk('collide/toggleFollowUser', async (userId, { getState, rejectWithValue }) => {
  try {
    const isFollowing = selectIsFollowingUser(userId)(getState())
    const user = isFollowing ? await usersApi.unfollow(userId) : await usersApi.follow(userId)
    const stats = await usersApi.stats(userId).catch(() => null)
    return { user: normalizeUser(user), stats: stats ? normalizeStats(stats) : null }
  } catch (error) {
    return rejectWithValue(error.response?.data?.message || 'Не удалось изменить подписку')
  }
})

export const rateCollection = createAsyncThunk('collide/rateCollection', async ({ collectionId, value }, { getState, rejectWithValue }) => {
  try {
    const currentUserId = getState().collide.currentUserId
    const detail = await collectionsApi.rate(collectionId, Number(value))
    const comments = await collectionsApi.comments(collectionId).catch(() => [])
    return { collection: normalizeCollection({ ...detail, comments }, currentUserId), items: (detail.items || []).map((item) => normalizeItem(item, currentUserId)) }
  } catch (error) {
    return rejectWithValue(error.response?.data?.message || 'Не удалось оценить коллекцию')
  }
})

export const addItemComment = createAsyncThunk('collide/addItemComment', async ({ itemId, text }, { rejectWithValue }) => {
  try {
    const comment = await itemsApi.addComment(itemId, text)
    return { itemId, comment: normalizeComment(comment) }
  } catch (error) {
    return rejectWithValue(error.response?.data?.message || 'Не удалось добавить комментарий')
  }
})

export const addCollectionComment = createAsyncThunk('collide/addCollectionComment', async ({ collectionId, text }, { rejectWithValue }) => {
  try {
    const comment = await collectionsApi.addComment(collectionId, text)
    return { collectionId, comment: normalizeComment(comment) }
  } catch (error) {
    return rejectWithValue(error.response?.data?.message || 'Не удалось добавить комментарий')
  }
})

export const createOrGetChat = createAsyncThunk('collide/createOrGetChat', async (companionId, { rejectWithValue }) => {
  try {
    const dialog = await chatsApi.create(companionId)
    const normalizedDialog = normalizeDialog(dialog)
    try {
      const messages = await chatsApi.messages(normalizedDialog.id)
      normalizedDialog.messages = messages.map(normalizeMessage)
    } catch {
      normalizedDialog.messages = []
    }
    return normalizedDialog
  } catch (error) {
    return rejectWithValue(error.response?.data?.message || 'Не удалось начать чат')
  }
})

export const fetchDialogMessages = createAsyncThunk('collide/fetchDialogMessages', async (dialogId, { rejectWithValue }) => {
  try {
    const messages = await chatsApi.messages(dialogId)
    await chatsApi.markRead(dialogId).catch(() => null)
    return { dialogId, messages: messages.map(normalizeMessage) }
  } catch (error) {
    return rejectWithValue(error.response?.data?.message || 'Не удалось загрузить сообщения')
  }
})

export const sendMessage = createAsyncThunk('collide/sendMessage', async ({ dialogId, text }, { rejectWithValue }) => {
  try {
    const message = await chatsApi.send(dialogId, text)
    return { dialogId, message: normalizeMessage(message) }
  } catch (error) {
    return rejectWithValue(error.response?.data?.message || 'Не удалось отправить сообщение')
  }
})

const collideSlice = createSlice({
  name: 'collide',
  initialState,
  reducers: {
    setSelectedCategory(state, action) { state.selectedCategory = action.payload },
    setSearchQuery(state, action) { state.searchQuery = action.payload },
    setProfileSearchQuery(state, action) { state.profileSearchQuery = action.payload },
    setChatSearchQuery(state, action) { state.chatSearchQuery = action.payload },
    setActiveDialog(state, action) {
      state.activeDialogId = action.payload
      const dialog = state.dialogs.find((item) => item.id === action.payload)
      if (dialog) dialog.unread = 0
    },
    openAddItemModal(state) { state.isAddItemModalOpen = true },
    closeAddItemModal(state) { state.isAddItemModalOpen = false },
    openAddCollectionModal(state) { state.isAddCollectionModalOpen = true },
    closeAddCollectionModal(state) { state.isAddCollectionModalOpen = false },
    logoutUser(state) {
      clearAuthSession()
      state.auth = { token: null, user: null, status: 'idle', error: null }
      state.currentUserId = null
      state.users = []
      state.dialogs = []
      state.favorites = { collections: [], items: [] }
      state.activeDialogId = null
    },
  },
  extraReducers: (builder) => {
    const setLoading = (state) => { state.apiStatus = 'loading'; state.apiError = null }
    const setError = (state, action) => { state.apiStatus = 'failed'; state.apiError = action.payload || action.error?.message; state.usingFallback = false }

    builder
        .addCase(loadInitialData.pending, setLoading)
        .addCase(loadInitialData.fulfilled, (state, action) => applyCommonData(state, action.payload))
        .addCase(loadInitialData.rejected, setError)
        .addCase(loginUser.pending, (state) => { state.auth.status = 'loading'; state.auth.error = null })
        .addCase(loginUser.fulfilled, (state, action) => {
          state.auth.status = 'succeeded'
          state.auth.token = action.payload.token
          state.auth.user = normalizeUser(action.payload.user || action.payload.currentUser)
          applyCommonData(state, action.payload)
        })
        .addCase(loginUser.rejected, (state, action) => { state.auth.status = 'failed'; state.auth.error = action.payload || action.error?.message })
        .addCase(registerUser.pending, (state) => { state.auth.status = 'loading'; state.auth.error = null })
        .addCase(registerUser.fulfilled, (state, action) => {
          state.auth.status = 'succeeded'
          state.auth.token = action.payload.token
          state.auth.user = normalizeUser(action.payload.user || action.payload.currentUser)
          applyCommonData(state, action.payload)
        })
        .addCase(registerUser.rejected, (state, action) => { state.auth.status = 'failed'; state.auth.error = action.payload || action.error?.message })
        .addCase(updateUserProfile.fulfilled, (state, action) => {
          upsertById(state.users, action.payload)
          if (state.auth.user?.id === action.payload.id) {
            state.auth.user = action.payload
          }
        })
        .addCase(fetchUserProfile.fulfilled, (state, action) => {
          upsertById(state.users, action.payload.user)
          if (action.payload.stats) state.profileStatsByUserId[action.payload.user.id] = action.payload.stats
          action.payload.collections.forEach((collection) => upsertById(state.collections, collection))
          action.payload.items.forEach((item) => upsertById(state.items, item))
        })
        .addCase(fetchCollectionDetail.fulfilled, (state, action) => {
          upsertById(state.collections, action.payload.collection)
          action.payload.items.forEach((item) => upsertById(state.items, item))
        })
        .addCase(fetchItemDetail.fulfilled, (state, action) => {
          upsertById(state.items, action.payload.item)
          if (action.payload.owner) upsertById(state.users, action.payload.owner)
        })
        .addCase(addCollection.fulfilled, (state, action) => {
          upsertById(state.collections, action.payload.collection)
          action.payload.items.forEach((item) => upsertById(state.items, item))
          state.isAddCollectionModalOpen = false
        })
        .addCase(updateCollection.fulfilled, (state, action) => {
          upsertById(state.collections, action.payload.collection)
          action.payload.items.forEach((item) => upsertById(state.items, item))
        })
        .addCase(addItem.fulfilled, (state, action) => {
          upsertById(state.items, action.payload.item)
          state.isAddItemModalOpen = false
        })
        .addCase(updateItem.fulfilled, (state, action) => upsertById(state.items, action.payload.item))
        .addCase(deleteItem.fulfilled, (state, action) => { state.items = removeById(state.items, action.payload) })
        .addCase(setItemStatus.fulfilled, (state, action) => upsertById(state.items, action.payload.item))
        .addCase(removeItemFromCollection.fulfilled, (state, action) => {
          upsertById(state.collections, action.payload.collection)
          action.payload.items.forEach((item) => upsertById(state.items, item))
          const item = state.items.find((entry) => entry.id === action.payload.itemId)
          if (item) { item.collectionId = ''; item.collectionTitle = 'Без коллекции' }
        })
        .addCase(toggleItemLike.fulfilled, (state, action) => {
          const item = state.items.find((entry) => entry.id === action.payload.id)
          if (!item) return
          const liked = action.payload.liked
          const hasLike = item.likedBy?.includes(state.currentUserId)
          if (liked && !hasLike) { item.likedBy = [...(item.likedBy || []), state.currentUserId]; item.likes += 1 }
          if (!liked && hasLike) { item.likedBy = item.likedBy.filter((id) => id !== state.currentUserId); item.likes = Math.max(0, item.likes - 1) }
        })
        .addCase(toggleFavoriteItem.fulfilled, (state, action) => {
          const item = state.items.find((entry) => entry.id === action.payload.id)
          if (!item) return
          item.favoritedBy = action.payload.favorite ? [state.currentUserId] : []
          item.isFavorite = action.payload.favorite
        })
        .addCase(toggleFavoriteCollection.fulfilled, (state, action) => {
          const collection = state.collections.find((entry) => entry.id === action.payload.id)
          if (!collection) return
          collection.favoritedBy = action.payload.favorite ? [state.currentUserId] : []
          collection.isFavorite = action.payload.favorite
        })
        .addCase(toggleFollowUser.fulfilled, (state, action) => {
          upsertById(state.users, action.payload.user)
          if (action.payload.stats) state.profileStatsByUserId[action.payload.user.id] = action.payload.stats
        })
        .addCase(rateCollection.fulfilled, (state, action) => {
          upsertById(state.collections, action.payload.collection)
          action.payload.items.forEach((item) => upsertById(state.items, item))
        })
        .addCase(addItemComment.fulfilled, (state, action) => {
          const item = state.items.find((entry) => entry.id === action.payload.itemId)
          if (item) { item.comments = [...(item.comments || []), action.payload.comment]; item.commentsCount = item.comments.length }
        })
        .addCase(addCollectionComment.fulfilled, (state, action) => {
          const collection = state.collections.find((entry) => entry.id === action.payload.collectionId)
          if (collection) { collection.comments = [...(collection.comments || []), action.payload.comment]; collection.commentsCount = collection.comments.length }
        })
        .addCase(createOrGetChat.fulfilled, (state, action) => {
          const existingIndex = state.dialogs.findIndex((d) => d.id === action.payload.id)
          if (existingIndex >= 0) {
            state.dialogs[existingIndex] = { ...state.dialogs[existingIndex], ...action.payload }
          } else {
            state.dialogs.unshift(action.payload)
          }
          state.activeDialogId = action.payload.id
        })
        .addCase(fetchDialogMessages.fulfilled, (state, action) => {
          const dialog = state.dialogs.find((entry) => entry.id === action.payload.dialogId)
          if (dialog) { dialog.messages = action.payload.messages; dialog.unread = 0 }
        })
        .addCase(sendMessage.fulfilled, (state, action) => {
          const dialog = state.dialogs.find((entry) => entry.id === action.payload.dialogId)
          if (dialog) {
            dialog.messages = [...(dialog.messages || []), action.payload.message]
            dialog.preview = action.payload.message.text
          }
        })
  },
})

function applyCommonData(state, payload) {
  const currentUser = payload.currentUser || (payload.user ? normalizeUser(payload.user) : null)
  state.apiStatus = 'succeeded'
  state.apiError = null
  state.usingFallback = false
  state.currentUserId = currentUser?.id || null
  state.users = currentUser ? [currentUser] : []
  if (currentUser) state.auth.user = currentUser
  state.categories = payload.categories?.length ? payload.categories : state.categories
  state.collections = payload.collections || []
  state.items = payload.items || []
  state.dialogs = payload.dialogs || []
  state.favorites = payload.favorites || { collections: [], items: [] }
  if (payload.stats && currentUser) state.profileStatsByUserId[currentUser.id] = payload.stats
  if (!state.activeDialogId && state.dialogs[0]) state.activeDialogId = state.dialogs[0].id
  if (state.activeDialogId && !state.dialogs.some((dialog) => dialog.id === state.activeDialogId)) state.activeDialogId = state.dialogs[0]?.id || null
}

export const {
  setSelectedCategory,
  setSearchQuery,
  setProfileSearchQuery,
  setChatSearchQuery,
  setActiveDialog,
  openAddItemModal,
  closeAddItemModal,
  openAddCollectionModal,
  closeAddCollectionModal,
  logoutUser,
} = collideSlice.actions

export const selectCategories = (state) => state.collide.categories
export const selectRawCollections = (state) => state.collide.collections
export const selectRawItems = (state) => state.collide.items
export const selectDialogs = (state) => state.collide.dialogs
export const selectCurrentUserId = (state) => state.collide.currentUserId
export const selectCurrentUser = (state) => state.collide.users.find((user) => String(user.id) === String(state.collide.currentUserId)) || state.collide.auth.user || null
export const selectUserById = (id) => (state) => state.collide.users.find((user) => String(user.id) === String(id)) || null
export const selectSelectedCategory = (state) => state.collide.selectedCategory
export const selectSearchQuery = (state) => state.collide.searchQuery
export const selectProfileSearchQuery = (state) => state.collide.profileSearchQuery
export const selectChatSearchQuery = (state) => state.collide.chatSearchQuery
export const selectActiveDialogId = (state) => state.collide.activeDialogId
export const selectAddItemModalOpen = (state) => state.collide.isAddItemModalOpen
export const selectAddCollectionModalOpen = (state) => state.collide.isAddCollectionModalOpen
export const selectAuth = (state) => state.collide.auth
export const selectApiStatus = (state) => state.collide.apiStatus
export const selectApiError = (state) => state.collide.apiError
export const selectUsingFallback = (state) => state.collide.usingFallback

const getUser = (userId, state) => state.collide.users.find((user) => String(user.id) === String(userId))
const getCollectionItems = (collectionId, state) => state.collide.items.filter((item) => String(item.collectionId) === String(collectionId))
const getCollectionItemsCount = (collectionId, state) => getCollectionItems(collectionId, state).length
const getCollectionAverageRating = (collection) => collection?.averageRating || 0
const getUserFollowersCount = (userId, state) => {
  const stats = state.collide.profileStatsByUserId[String(userId)]
  if (stats) return stats.followersCount || 0
  const user = getUser(userId, state)
  return user?.followersBy?.length || 0
}
const getUserItemLikesCount = (userId, state) => {
  const stats = state.collide.profileStatsByUserId[String(userId)]
  if (stats) return stats.totalItemLikes || 0
  return state.collide.items.filter((item) => String(item.ownerId) === String(userId)).reduce((sum, item) => sum + Number(item.likes || 0), 0)
}
const getUserCollectionsTotalValue = (userId, state) => {
  const stats = state.collide.profileStatsByUserId[String(userId)]
  if (stats) return stats.totalCollectionsValue || 0
  const collectionIds = new Set(state.collide.collections.filter((collection) => String(collection.ownerId) === String(userId)).map((collection) => collection.id))
  return state.collide.items.filter((item) => collectionIds.has(item.collectionId)).reduce((sum, item) => sum + Number(item.priceAmount || parsePriceAmount(item.price)), 0)
}
const getUserCollectionsAverageRating = (userId, state) => {
  const stats = state.collide.profileStatsByUserId[String(userId)]
  if (stats) return stats.collectionAverageRating || '—'
  const ratings = state.collide.collections.filter((collection) => String(collection.ownerId) === String(userId)).map(getCollectionAverageRating).filter(Boolean)
  if (!ratings.length) return '—'
  return Number((ratings.reduce((sum, value) => sum + value, 0) / ratings.length).toFixed(1))
}
const getUserProfileMetrics = (userId, state) => {
  const stats = state.collide.profileStatsByUserId[String(userId)]
  if (stats) return stats
  const value = getUserCollectionsTotalValue(userId, state)
  return {
    collectionAverageRating: getUserCollectionsAverageRating(userId, state),
    totalItemLikes: getUserItemLikesCount(userId, state),
    followersCount: getUserFollowersCount(userId, state),
    totalCollectionsValue: value,
    totalCollectionsValueLabel: formatMoney(value),
  }
}

const enrichCollection = (collection, state) => {
  const owner = getUser(collection.ownerId, state)
  const itemsCount = collection.itemsCount ?? getCollectionItemsCount(collection.id, state)
  return {
    ...collection,
    itemsCount,
    ownerName: collection.ownerName || owner?.name || 'Пользователь',
    ownerHandle: collection.ownerHandle || owner?.handle || '',
    averageRating: getCollectionAverageRating(collection),
    ratingsCount: collection.ratingsCount || 0,
    commentsCount: collection.commentsCount || collection.comments?.length || 0,
    isFavorite: Boolean(collection.favoritedBy?.includes(state.collide.currentUserId) || collection.isFavorite),
  }
}

const enrichItem = (item, state) => {
  const collection = state.collide.collections.find((entry) => String(entry.id) === String(item.collectionId))
  const owner = getUser(item.ownerId, state)
  return {
    ...item,
    collectionTitle: item.collectionTitle || collection?.title || 'Без коллекции',
    collectionId: item.collectionId || '',
    ownerName: item.ownerName || owner?.name || 'Пользователь',
    ownerHandle: item.ownerHandle || owner?.handle || '',
    commentsCount: item.commentsCount || item.comments?.length || 0,
    isFavorite: Boolean(item.favoritedBy?.includes(state.collide.currentUserId) || item.isFavorite),
  }
}

export const selectCollections = createSelector([(state) => state], (state) => state.collide.collections.map((collection) => enrichCollection(collection, state)))
export const selectItems = createSelector([(state) => state], (state) => state.collide.items.map((item) => enrichItem(item, state)))
export const selectCollectionById = (id) => createSelector(
    [selectCollections],
    (collectionsList) => collectionsList.find((entry) => String(entry.id) === String(id)) || null,
)
export const selectCollectionsByUser = (userId) => createSelector(
    [selectCollections, selectProfileSearchQuery],
    (collectionsList, profileSearchQuery) => {
      if (!userId) return []
      const query = normalize(profileSearchQuery)
      return collectionsList
          .filter((collection) => String(collection.ownerId) === String(userId))
          .filter((collection) => !query || normalize(`${collection.title} ${collection.description}`).includes(query))
    },
)
export const selectItemsByUser = (userId) => createSelector(
    [selectItems, selectProfileSearchQuery],
    (itemsList, profileSearchQuery) => {
      if (!userId) return []
      const query = normalize(profileSearchQuery)
      return itemsList
          .filter((item) => String(item.ownerId) === String(userId))
          .filter((item) => !query || normalize(`${item.title} ${item.description} ${item.fullDescription} ${item.statusLabel}`).includes(query))
    },
)
export const selectItemsByCollection = (collectionId) => createSelector(
    [selectItems],
    (itemsList) => itemsList.filter((item) => String(item.collectionId) === String(collectionId)),
)
export const selectCollectionItemsRaw = (collectionId) => createSelector(
    [selectRawItems],
    (itemsList) => itemsList.filter((item) => String(item.collectionId) === String(collectionId)),
)
export const selectItemById = (id) => createSelector(
    [selectItems],
    (itemsList) => itemsList.find((entry) => String(entry.id) === String(id)) || null,
)
export const selectItemLikedByCurrentUser = (id) => (state) => {
  const item = state.collide.items.find((entry) => String(entry.id) === String(id))
  return Boolean(item?.likedBy?.includes(state.collide.currentUserId))
}
export const selectItemFavoritedByCurrentUser = (id) => (state) => {
  const item = state.collide.items.find((entry) => String(entry.id) === String(id))
  return Boolean(item?.favoritedBy?.includes(state.collide.currentUserId) || item?.isFavorite)
}
export const selectCollectionFavoritedByCurrentUser = (id) => (state) => {
  const collection = state.collide.collections.find((entry) => String(entry.id) === String(id))
  return Boolean(collection?.favoritedBy?.includes(state.collide.currentUserId) || collection?.isFavorite)
}
export const selectUserCollectionAverageRating = (userId) => (state) => getUserCollectionsAverageRating(userId, state)
export const selectUserProfileMetrics = (userId) => createSelector([(state) => state], (state) => (userId ? getUserProfileMetrics(userId, state) : { collectionAverageRating: '—', totalItemLikes: 0, followersCount: 0, totalCollectionsValue: 0, totalCollectionsValueLabel: '0 ₽' }))
export const selectIsFollowingUser = (userId) => (state) => {
  const user = state.collide.users.find((entry) => String(entry.id) === String(userId))
  return Boolean(user?.following || user?.followersBy?.includes(state.collide.currentUserId))
}
export const selectUserFollowersCount = (userId) => (state) => getUserFollowersCount(userId, state)
export const selectFavoritesByCurrentUser = createSelector(
    [selectCollections, selectItems, (state) => state.collide.favorites, selectCurrentUserId],
    (collectionsList, itemsList, favorites, userId) => {
      const favoriteCollectionIds = new Set((favorites.collections || []).map((collection) => String(collection.id)))
      const favoriteItemIds = new Set((favorites.items || []).map((item) => String(item.id)))
      return {
        collections: collectionsList.filter((collection) => collection.favoritedBy?.includes(userId) || collection.isFavorite || favoriteCollectionIds.has(String(collection.id))),
        items: itemsList.filter((item) => item.favoritedBy?.includes(userId) || item.isFavorite || favoriteItemIds.has(String(item.id))),
      }
    },
)
export const selectFilteredItems = createSelector([selectItems, selectSelectedCategory, selectSearchQuery], (itemsList, selectedCategory, searchQuery) => {
  const query = normalize(searchQuery)
  return itemsList
      .filter((item) => selectedCategory === 'all' || item.category === selectedCategory)
      .filter((item) => !query || normalize(`${item.title} ${item.description} ${item.statusLabel} ${item.collectionTitle}`).includes(query))
})
export const selectFilteredDialogs = createSelector([selectDialogs, selectChatSearchQuery], (dialogsList, query) => {
  const normalized = normalize(query)
  return dialogsList.filter((dialog) => !normalized || normalize(`${dialog.name} ${dialog.handle}`).includes(normalized))
})
export const selectActiveDialog = createSelector([selectDialogs, selectActiveDialogId], (dialogsList, activeDialogId) => dialogsList.find((dialog) => dialog.id === activeDialogId) || null)

export default collideSlice.reducer

export const selectIsAuthenticated = (state) => Boolean(state.collide.auth.token && state.collide.auth.user)