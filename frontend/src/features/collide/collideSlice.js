import {createSelector, createSlice} from '@reduxjs/toolkit'

const placeholderPalette = [
    '#FD3E3E',
    '#FB8500',
    '#FFB703',
    '#F3D5B5',
    '#BC8A5F',
    '#A47148',
    '#6F4518',
]

const categories = [
    {id: 'all', title: 'Все категории'},
    {id: 'figures', title: 'Фигурки'},
    {id: 'coins', title: 'Монеты'},
    {id: 'vinyl', title: 'Пластинки'},
    {id: 'books', title: 'Книги'},
    {id: 'watches', title: 'Часы'},
    {id: 'retro', title: 'Ретро-техника'},
    {id: 'boardgames', title: 'Настольные игры'},
    {id: 'minerals', title: 'Минералы'},
]

const users = [
    {
        id: 'u1',
        name: 'JesseYo',
        handle: '@heisenberg',
        status: 'Заходил недавно',
        avatarTone: 'orange',
        isCurrent: true,
        about: 'Коллекционирую винил, фигурки и редкие издания комиксов.',
        rating: 4.8,
        reviews: 18,
        followersBy: ['u2', 'u3', 'u4', 'u5', 'u6'],
    },
    {
        id: 'u2',
        name: 'Mr. White',
        handle: '@whitecollector',
        status: 'Заходил недавно',
        avatarTone: 'cream',
        about: 'Собираю монеты, минералы и редкие предметы ретро-техники.',
        rating: 4.9,
        reviews: 31,
        followersBy: ['u1', 'u3', 'u4', 'u6', 'u7', 'u8', 'u9'],
    },
]

const collections = [
    {
        id: 'c1',
        ownerId: 'u1',
        title: 'Ретро-фигурки',
        category: 'figures',
        description: 'Серия фигурок из игр, сериалов и старых промо-наборов.',
        coverTone: 'red',
        coverImageUrl: '',
        placeholderColor: '#FD3E3E',
        popularity: 94,
        createdAt: 4,
        ratings: [{userId: 'u2', value: 5}],
        comments: [
            {
                id: 'cc1',
                authorId: 'u2',
                authorName: 'Mr. White',
                text: 'Хорошая подборка фигурок, особенно космонавт.',
                time: '11:30'
            },
        ],
        favoritedBy: [],
    },
    {
        id: 'c2',
        ownerId: 'u1',
        title: 'Винил 80-х',
        category: 'vinyl',
        description: 'Пластинки с тёплым звучанием, яркими обложками и историей.',
        coverTone: 'orange',
        coverImageUrl: '',
        placeholderColor: '#FB8500',
        popularity: 88,
        createdAt: 3,
        ratings: [{userId: 'u2', value: 4}],
        comments: [],
        favoritedBy: ['u2'],
    },
    {
        id: 'c3',
        ownerId: 'u1',
        title: 'Комиксы',
        category: 'books',
        description: 'Выпуски в мягкой и твёрдой обложке, часть открыта для обмена.',
        coverTone: 'cream',
        coverImageUrl: '',
        placeholderColor: '#F3D5B5',
        popularity: 76,
        createdAt: 2,
        ratings: [{userId: 'u2', value: 5}],
        comments: [],
        favoritedBy: [],
    },
    {
        id: 'c4',
        ownerId: 'u2',
        title: 'Монеты США',
        category: 'coins',
        description: 'Нумизматическая коллекция с описанием состояния и редкости.',
        coverTone: 'brown',
        coverImageUrl: '',
        placeholderColor: '#A47148',
        popularity: 98,
        createdAt: 1,
        ratings: [{userId: 'u1', value: 5}],
        comments: [
            {
                id: 'cc2',
                authorId: 'u1',
                authorName: 'JesseYo',
                text: 'Добавил в избранное, монеты выглядят отлично.',
                time: '15:44'
            },
        ],
        favoritedBy: ['u1'],
    },
]

const items = [
    {
        id: 'i1',
        ownerId: 'u1',
        collectionId: 'c1',
        title: 'Фигурка космонавта',
        category: 'figures',
        status: 'exchange',
        statusLabel: 'Для обмена',
        price: '4 500 ₽',
        description: 'Фигурка в хорошем состоянии.',
        fullDescription:
            'Фигурка космонавта из лимитированной серии. Состояние хорошее: есть небольшие следы хранения на упаковке, сама фигурка без повреждений. Подходит для обмена на винил или ретро-технику.',
        imageTone: 'red',
        imageUrl: '',
        placeholderColor: '#FD3E3E',
        popularity: 71,
        createdAt: 6,
        likes: 12,
        likedBy: [],
        favoritedBy: [],
        comments: [
            {
                id: 'cm1',
                authorId: 'u2',
                authorName: 'Mr. White',
                text: 'Очень крутая фигурка, выглядит как редкий выпуск.',
                time: '12:20'
            },
        ],
    },
    {
        id: 'i2',
        ownerId: 'u1',
        collectionId: 'c2',
        title: 'Пластинка Synth Night',
        category: 'vinyl',
        status: 'collection',
        statusLabel: 'В коллекции',
        price: '2 900 ₽',
        description: 'Редкое переиздание.',
        fullDescription:
            'Переиздание с плотной обложкой и приятным тёплым звучанием. Пластинка хранится во внутреннем антистатическом конверте, проигрывалась несколько раз.',
        imageTone: 'orange',
        imageUrl: '',
        placeholderColor: '#FB8500',
        popularity: 62,
        createdAt: 5,
        likes: 8,
        likedBy: ['u1'],
        favoritedBy: ['u1'],
        comments: [],
    },
    {
        id: 'i3',
        ownerId: 'u1',
        collectionId: 'c3',
        title: 'Комикс выпуск #12',
        category: 'books',
        status: 'sale',
        statusLabel: 'В продаже',
        price: '1 200 ₽',
        description: 'Сохранность VF, без повреждений.',
        fullDescription:
            'Комикс из личной коллекции. Обложка без серьёзных заломов, страницы чистые, корешок не повреждён. Возможна продажа или обмен на близкий по редкости выпуск.',
        imageTone: 'cream',
        imageUrl: '',
        placeholderColor: '#F3D5B5',
        popularity: 43,
        createdAt: 4,
        likes: 5,
        likedBy: [],
        favoritedBy: [],
        comments: [],
    },
    {
        id: 'i4',
        ownerId: 'u1',
        collectionId: 'c2',
        title: 'Винил Neon City',
        category: 'vinyl',
        status: 'archive',
        statusLabel: 'В архиве',
        price: '3 600 ₽',
        description: 'С постером и оригинальной вставкой.',
        fullDescription:
            'Архивный предмет коллекции. В комплекте оригинальная вставка и постер. На обмен пока не выставляется, добавлена в профиль для демонстрации коллекции.',
        imageTone: 'brown',
        imageUrl: '',
        placeholderColor: '#A47148',
        popularity: 83,
        createdAt: 3,
        likes: 14,
        likedBy: [],
        favoritedBy: [],
        comments: [],
    },
    {
        id: 'i5',
        ownerId: 'u2',
        collectionId: 'c4',
        title: 'Серебряная монета 1964',
        category: 'coins',
        status: 'sale',
        statusLabel: 'В продаже',
        price: '6 800 ₽',
        description: 'Монета с историей владения.',
        fullDescription:
            'Серебряная монета 1964 года. В карточке зафиксированы состояние, примерная стоимость и история владения. Предмет открыт для просмотра другими коллекционерами.',
        imageTone: 'blue',
        imageUrl: '',
        placeholderColor: '#FFB703',
        popularity: 93,
        createdAt: 2,
        likes: 21,
        likedBy: ['u1'],
        favoritedBy: ['u1'],
        comments: [
            {
                id: 'cm2',
                authorId: 'u1',
                authorName: 'JesseYo',
                text: 'Интересный экземпляр, добавил в избранное.',
                time: '15:44'
            },
        ],
    },
    {
        id: 'i6',
        ownerId: 'u2',
        collectionId: '',
        title: 'Минерал аметист',
        category: 'minerals',
        status: 'exchange',
        statusLabel: 'Для обмена',
        price: '900 ₽',
        description: 'Небольшой образец для обмена.',
        fullDescription:
            'Небольшой образец аметиста. Подойдёт для начинающей коллекции минералов. Рассматривается обмен на монеты или ретро-аксессуары.',
        imageTone: 'cream',
        imageUrl: '',
        placeholderColor: '#BC8A5F',
        popularity: 29,
        createdAt: 1,
        likes: 3,
        likedBy: [],
        favoritedBy: [],
        comments: [],
    },
]

const dialogs = [
    {
        id: 'd1',
        userId: 'u2',
        name: 'Mr. White',
        preview: '*Sent you a photo*',
        unread: 1,
        avatarTone: 'cream',
        messages: [
            {id: 'm1', from: 'u2', text: 'Привет! Посмотришь мою коллекцию монет?', time: '12:20'},
            {id: 'm2', from: 'u1', text: 'Да, отправь фото редких экземпляров.', time: '12:22'},
            {id: 'm3', from: 'u2', text: 'Отправил фото серебряной монеты.', time: '12:24'},
        ],
    },
    {
        id: 'd2',
        userId: 'u3',
        name: 'Mike “Waltuh”',
        preview: 'Есть интересный комикс для обмена.',
        unread: 1,
        avatarTone: 'orange',
        messages: [
            {id: 'm4', from: 'u3', text: 'Есть интересный комикс для обмена.', time: '13:10'},
        ],
    },
    {
        id: 'd3',
        userId: 'u4',
        name: 'Skinny P',
        preview: 'Скинь фото пластинки, пожалуйста.',
        unread: 1,
        avatarTone: 'red',
        messages: [
            {id: 'm5', from: 'u4', text: 'Скинь фото пластинки, пожалуйста.', time: '14:05'},
        ],
    },
    {
        id: 'd4',
        userId: 'u5',
        name: 'Badger',
        preview: 'Можно забронировать фигурку?',
        unread: 1,
        avatarTone: 'brown',
        messages: [
            {id: 'm6', from: 'u5', text: 'Можно забронировать фигурку?', time: '16:18'},
        ],
    },
    {
        id: 'd5',
        userId: 'u6',
        name: 'Jane',
        preview: 'Спасибо за обмен!',
        unread: 0,
        avatarTone: 'cream',
        messages: [
            {id: 'm7', from: 'u6', text: 'Спасибо за обмен!', time: '17:00'},
        ],
    },
]

const statusLabels = {
    collection: 'В коллекции',
    sale: 'В продаже',
    exchange: 'Для обмена',
    archive: 'В архиве',
}

const initialState = {
    categories,
    users,
    collections,
    items,
    dialogs,
    currentUserId: 'u1',
    selectedCategory: 'all',
    searchQuery: '',
    profileSearchQuery: '',
    chatSearchQuery: '',
    activeDialogId: 'd1',
    isAddItemModalOpen: false,
    isAddCollectionModalOpen: false,
}

const makeId = (prefix) => `${prefix}-${Date.now()}-${Math.random().toString(16).slice(2)}`
const getWarmPlaceholderColor = () => placeholderPalette[Math.floor(Math.random() * placeholderPalette.length)]
const normalize = (value) => String(value || '').trim().toLowerCase()
const trimShortDescription = (value) => String(value || '').trim().slice(0, 60)

const getCollectionItemsCount = (collectionId, state) => state.collide.items.filter((item) => item.collectionId === collectionId).length
const getCollectionItems = (collectionId, state) => state.collide.items.filter((item) => item.collectionId === collectionId)
const getUser = (userId, state) => state.collide.users.find((user) => user.id === userId)
const getCollectionAverageRating = (collection) => {
    if (!collection?.ratings?.length) return 0
    const total = collection.ratings.reduce((sum, rating) => sum + Number(rating.value || 0), 0)
    return Number((total / collection.ratings.length).toFixed(1))
}

const getUserCollectionsAverageRating = (userId, state) => {
    const userCollections = state.collide.collections.filter((collection) => collection.ownerId === userId)
    const ratings = userCollections.flatMap((collection) => collection.ratings || [])
    if (!ratings.length) return 0
    const total = ratings.reduce((sum, rating) => sum + Number(rating.value || 0), 0)
    return Number((total / ratings.length).toFixed(1))
}

const parsePriceValue = (value) => {
    const numeric = String(value || '').replace(/[^0-9]/g, '')
    return numeric ? Number(numeric) : 0
}

const formatPriceValue = (value) => `${Number(value || 0).toLocaleString('ru-RU')} ₽`

const getUserItemLikesCount = (userId, state) => state.collide.items
    .filter((item) => item.ownerId === userId)
    .reduce((sum, item) => sum + Number(item.likes || 0), 0)

const getUserFollowersCount = (userId, state) => {
    const user = getUser(userId, state)
    return user?.followersBy?.length || 0
}

const getUserCollectionsTotalValue = (userId, state) => {
    const collectionIds = state.collide.collections
        .filter((collection) => collection.ownerId === userId)
        .map((collection) => collection.id)

    return state.collide.items
        .filter((item) => collectionIds.includes(item.collectionId))
        .reduce((sum, item) => sum + parsePriceValue(item.price), 0)
}

const getUserProfileMetrics = (userId, state) => {
    const totalCollectionsValue = getUserCollectionsTotalValue(userId, state)

    return {
        collectionAverageRating: getUserCollectionsAverageRating(userId, state),
        totalItemLikes: getUserItemLikesCount(userId, state),
        followersCount: getUserFollowersCount(userId, state),
        totalCollectionsValue,
        totalCollectionsValueLabel: formatPriceValue(totalCollectionsValue),
    }
}

const enrichCollection = (collection, state) => {
    const owner = getUser(collection.ownerId, state)
    const averageRating = getCollectionAverageRating(collection)
    return {
        ...collection,
        itemsCount: getCollectionItemsCount(collection.id, state),
        ownerName: owner?.name || 'Пользователь',
        ownerHandle: owner?.handle || '',
        averageRating,
        ratingsCount: collection.ratings?.length || 0,
        commentsCount: collection.comments?.length || 0,
        isFavorite: Boolean(collection.favoritedBy?.includes(state.collide.currentUserId)),
    }
}

const enrichItem = (item, state) => {
    const collection = state.collide.collections.find((entry) => entry.id === item.collectionId)
    const owner = getUser(item.ownerId, state)
    return {
        ...item,
        collectionTitle: collection?.title || 'Без коллекции',
        collectionId: collection?.id || '',
        ownerName: owner?.name || 'Пользователь',
        ownerHandle: owner?.handle || '',
        commentsCount: item.comments?.length || 0,
        isFavorite: Boolean(item.favoritedBy?.includes(state.collide.currentUserId)),
    }
}

const collideSlice = createSlice({
    name: 'collide',
    initialState,
    reducers: {
        setSelectedCategory(state, action) {
            state.selectedCategory = action.payload
        },
        setSearchQuery(state, action) {
            state.searchQuery = action.payload
        },
        setProfileSearchQuery(state, action) {
            state.profileSearchQuery = action.payload
        },
        setChatSearchQuery(state, action) {
            state.chatSearchQuery = action.payload
        },
        setActiveDialog(state, action) {
            state.activeDialogId = action.payload
            const dialog = state.dialogs.find((item) => item.id === action.payload)
            if (dialog) dialog.unread = 0
        },
        openAddItemModal(state) {
            state.isAddItemModalOpen = true
        },
        closeAddItemModal(state) {
            state.isAddItemModalOpen = false
        },
        openAddCollectionModal(state) {
            state.isAddCollectionModalOpen = true
        },
        closeAddCollectionModal(state) {
            state.isAddCollectionModalOpen = false
        },
        addCollection(state, action) {
            const payload = action.payload || {}
            const title = payload.title?.trim() || 'Новая коллекция'
            state.collections.unshift({
                id: makeId('c'),
                ownerId: state.currentUserId,
                title,
                category: payload.category || 'figures',
                description: payload.description?.trim() || 'Описание коллекции можно будет изменить позднее.',
                coverTone: 'custom',
                coverImageUrl: payload.coverImageUrl || '',
                placeholderColor: payload.placeholderColor || getWarmPlaceholderColor(),
                popularity: 0,
                createdAt: Date.now(),
                ratings: [],
                comments: [],
                favoritedBy: [],
            })
            state.isAddCollectionModalOpen = false
        },
        updateCollection(state, action) {
            const payload = action.payload || {}
            const collection = state.collections.find((entry) => entry.id === payload.id)
            if (!collection) return
            if (payload.title !== undefined) collection.title = payload.title.trim() || collection.title
            if (payload.description !== undefined) collection.description = payload.description.trim()
            if (payload.category !== undefined) collection.category = payload.category || collection.category
            if (payload.coverImageUrl !== undefined) collection.coverImageUrl = payload.coverImageUrl
            if (payload.placeholderColor !== undefined) collection.placeholderColor = payload.placeholderColor || collection.placeholderColor
        },
        addItem(state, action) {
            const payload = action.payload || {}
            const collection = payload.collectionId ? state.collections.find((item) => item.id === payload.collectionId) : null
            const status = payload.status || 'collection'
            const shortDescription = trimShortDescription(payload.description) || 'Краткое описание будет добавлено позднее.'
            state.items.unshift({
                id: makeId('i'),
                ownerId: state.currentUserId,
                collectionId: collection?.id || '',
                title: payload.title?.trim() || 'Новый предмет',
                category: payload.category || collection?.category || 'figures',
                status,
                statusLabel: statusLabels[status],
                price: payload.price?.trim() || '—',
                description: shortDescription,
                fullDescription: payload.fullDescription?.trim() || shortDescription,
                imageTone: payload.imageTone || 'custom',
                imageUrl: payload.imageUrl || '',
                placeholderColor: payload.placeholderColor || getWarmPlaceholderColor(),
                popularity: 0,
                createdAt: Date.now(),
                likes: 0,
                likedBy: [],
                favoritedBy: [],
                comments: [],
            })
            state.isAddItemModalOpen = false
        },
        updateItem(state, action) {
            const payload = action.payload || {}
            const item = state.items.find((entry) => entry.id === payload.id)
            if (!item) return
            if (payload.title !== undefined) item.title = payload.title.trim() || item.title
            if (payload.price !== undefined) item.price = payload.price.trim() || '—'
            if (payload.description !== undefined) item.description = trimShortDescription(payload.description) || item.description
            if (payload.fullDescription !== undefined) item.fullDescription = payload.fullDescription.trim() || item.description
            if (payload.category !== undefined) item.category = payload.category || item.category
            if (payload.collectionId !== undefined) item.collectionId = payload.collectionId || ''
            if (payload.status !== undefined) {
                item.status = payload.status
                item.statusLabel = statusLabels[payload.status]
            }
            if (payload.imageUrl !== undefined) item.imageUrl = payload.imageUrl
            if (payload.placeholderColor !== undefined) item.placeholderColor = payload.placeholderColor || item.placeholderColor
        },
        removeItemFromCollection(state, action) {
            const item = state.items.find((entry) => entry.id === action.payload)
            if (item) item.collectionId = ''
        },
        deleteItem(state, action) {
            state.items = state.items.filter((entry) => entry.id !== action.payload)
        },
        setItemStatus(state, action) {
            const item = state.items.find((entry) => entry.id === action.payload.id)
            if (item) {
                item.status = action.payload.status
                item.statusLabel = statusLabels[action.payload.status]
            }
        },
        toggleItemLike(state, action) {
            const item = state.items.find((entry) => entry.id === action.payload)
            if (!item) return
            if (!Array.isArray(item.likedBy)) item.likedBy = []
            const alreadyLiked = item.likedBy.includes(state.currentUserId)
            if (alreadyLiked) {
                item.likedBy = item.likedBy.filter((id) => id !== state.currentUserId)
                item.likes = Math.max(0, item.likes - 1)
            } else {
                item.likedBy.push(state.currentUserId)
                item.likes += 1
            }
        },
        toggleFavoriteItem(state, action) {
            const item = state.items.find((entry) => entry.id === action.payload)
            if (!item) return
            if (!Array.isArray(item.favoritedBy)) item.favoritedBy = []
            if (item.favoritedBy.includes(state.currentUserId)) {
                item.favoritedBy = item.favoritedBy.filter((id) => id !== state.currentUserId)
            } else {
                item.favoritedBy.push(state.currentUserId)
            }
        },
        toggleFavoriteCollection(state, action) {
            const collection = state.collections.find((entry) => entry.id === action.payload)
            if (!collection) return
            if (!Array.isArray(collection.favoritedBy)) collection.favoritedBy = []
            if (collection.favoritedBy.includes(state.currentUserId)) {
                collection.favoritedBy = collection.favoritedBy.filter((id) => id !== state.currentUserId)
            } else {
                collection.favoritedBy.push(state.currentUserId)
            }
        },
        toggleFollowUser(state, action) {
            const targetUserId = action.payload
            if (!targetUserId || targetUserId === state.currentUserId) return
            const user = state.users.find((entry) => entry.id === targetUserId)
            if (!user) return
            if (!Array.isArray(user.followersBy)) user.followersBy = []
            if (user.followersBy.includes(state.currentUserId)) {
                user.followersBy = user.followersBy.filter((id) => id !== state.currentUserId)
            } else {
                user.followersBy.push(state.currentUserId)
            }
        },
        rateCollection(state, action) {
            const collection = state.collections.find((entry) => entry.id === action.payload.collectionId)
            if (!collection) return
            const value = Math.min(5, Math.max(1, Number(action.payload.value || 1)))
            if (!Array.isArray(collection.ratings)) collection.ratings = []
            const existing = collection.ratings.find((rating) => rating.userId === state.currentUserId)
            if (existing) {
                existing.value = value
            } else {
                collection.ratings.push({userId: state.currentUserId, value})
            }
        },
        addItemComment(state, action) {
            const text = action.payload?.text?.trim()
            if (!text) return
            const item = state.items.find((entry) => entry.id === action.payload.itemId)
            if (!item) return
            const currentUser = state.users.find((user) => user.id === state.currentUserId)
            item.comments.push({
                id: makeId('cm'),
                authorId: state.currentUserId,
                authorName: currentUser?.name || 'Пользователь',
                text,
                time: new Date().toLocaleTimeString('ru-RU', {hour: '2-digit', minute: '2-digit'}),
            })
        },
        addCollectionComment(state, action) {
            const text = action.payload?.text?.trim()
            if (!text) return
            const collection = state.collections.find((entry) => entry.id === action.payload.collectionId)
            if (!collection) return
            const currentUser = state.users.find((user) => user.id === state.currentUserId)
            collection.comments.push({
                id: makeId('cc'),
                authorId: state.currentUserId,
                authorName: currentUser?.name || 'Пользователь',
                text,
                time: new Date().toLocaleTimeString('ru-RU', {hour: '2-digit', minute: '2-digit'}),
            })
        },
        sendMessage(state, action) {
            const text = action.payload?.text?.trim()
            if (!text) return
            const dialog = state.dialogs.find((item) => item.id === action.payload.dialogId)
            if (!dialog) return
            dialog.messages.push({
                id: makeId('m'),
                from: state.currentUserId,
                text,
                time: new Date().toLocaleTimeString('ru-RU', {hour: '2-digit', minute: '2-digit'}),
            })
            dialog.preview = text
        },
    },
})

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
    addCollection,
    updateCollection,
    addItem,
    updateItem,
    removeItemFromCollection,
    deleteItem,
    setItemStatus,
    toggleItemLike,
    toggleFavoriteItem,
    toggleFavoriteCollection,
    toggleFollowUser,
    rateCollection,
    addItemComment,
    addCollectionComment,
    sendMessage,
} = collideSlice.actions

export const selectCategories = (state) => state.collide.categories
export const selectRawCollections = (state) => state.collide.collections
export const selectRawItems = (state) => state.collide.items
export const selectDialogs = (state) => state.collide.dialogs
export const selectCurrentUserId = (state) => state.collide.currentUserId
export const selectCurrentUser = (state) => state.collide.users.find((user) => user.id === state.collide.currentUserId)
export const selectUserById = (id) => (state) => state.collide.users.find((user) => user.id === id)
export const selectSelectedCategory = (state) => state.collide.selectedCategory
export const selectSearchQuery = (state) => state.collide.searchQuery
export const selectProfileSearchQuery = (state) => state.collide.profileSearchQuery
export const selectChatSearchQuery = (state) => state.collide.chatSearchQuery
export const selectActiveDialogId = (state) => state.collide.activeDialogId
export const selectAddItemModalOpen = (state) => state.collide.isAddItemModalOpen
export const selectAddCollectionModalOpen = (state) => state.collide.isAddCollectionModalOpen

export const selectCollections = createSelector(
    [(state) => state],
    (state) => state.collide.collections.map((collection) => enrichCollection(collection, state)),
)

export const selectItems = createSelector(
    [(state) => state],
    (state) => state.collide.items.map((item) => enrichItem(item, state)),
)

export const selectCollectionById = (id) => (state) => {
    const collection = state.collide.collections.find((entry) => entry.id === id)
    return collection ? enrichCollection(collection, state) : null
}

export const selectCollectionsByUser = (userId) => (state) => {
    const query = normalize(state.collide.profileSearchQuery)
    return state.collide.collections
        .filter((collection) => collection.ownerId === userId)
        .filter((collection) => !query || normalize(`${collection.title} ${collection.description}`).includes(query))
        .map((collection) => enrichCollection(collection, state))
}

export const selectItemsByUser = (userId) => (state) => {
    const query = normalize(state.collide.profileSearchQuery)
    return state.collide.items
        .filter((item) => item.ownerId === userId)
        .filter((item) => !query || normalize(`${item.title} ${item.description} ${item.fullDescription} ${item.statusLabel}`).includes(query))
        .map((item) => enrichItem(item, state))
}

export const selectItemsByCollection = (collectionId) => (state) => state.collide.items
    .filter((item) => item.collectionId === collectionId)
    .map((item) => enrichItem(item, state))

export const selectCollectionItemsRaw = (collectionId) => (state) => getCollectionItems(collectionId, state)

export const selectItemById = (id) => (state) => {
    const item = state.collide.items.find((entry) => entry.id === id)
    return item ? enrichItem(item, state) : null
}

export const selectItemLikedByCurrentUser = (id) => (state) => {
    const item = state.collide.items.find((entry) => entry.id === id)
    return Boolean(item?.likedBy?.includes(state.collide.currentUserId))
}

export const selectItemFavoritedByCurrentUser = (id) => (state) => {
    const item = state.collide.items.find((entry) => entry.id === id)
    return Boolean(item?.favoritedBy?.includes(state.collide.currentUserId))
}

export const selectCollectionFavoritedByCurrentUser = (id) => (state) => {
    const collection = state.collide.collections.find((entry) => entry.id === id)
    return Boolean(collection?.favoritedBy?.includes(state.collide.currentUserId))
}

export const selectUserCollectionAverageRating = (userId) => (state) => getUserCollectionsAverageRating(userId, state)

export const selectUserProfileMetrics = (userId) => (state) => getUserProfileMetrics(userId, state)
export const selectIsFollowingUser = (userId) => (state) => {
    const user = state.collide.users.find((entry) => entry.id === userId)
    return Boolean(user?.followersBy?.includes(state.collide.currentUserId))
}
export const selectUserFollowersCount = (userId) => (state) => getUserFollowersCount(userId, state)

export const selectFavoritesByCurrentUser = (state) => {
    const userId = state.collide.currentUserId
    return {
        collections: state.collide.collections
            .filter((collection) => collection.favoritedBy?.includes(userId))
            .map((collection) => enrichCollection(collection, state)),
        items: state.collide.items
            .filter((item) => item.favoritedBy?.includes(userId))
            .map((item) => enrichItem(item, state)),
    }
}

export const selectFilteredItems = createSelector(
    [selectItems, selectSelectedCategory, selectSearchQuery],
    (itemsList, selectedCategory, searchQuery) => {
        const query = normalize(searchQuery)
        return itemsList
            .filter((item) => selectedCategory === 'all' || item.category === selectedCategory)
            .filter((item) => !query || normalize(`${item.title} ${item.description} ${item.statusLabel} ${item.collectionTitle}`).includes(query))
    },
)

export const selectFilteredDialogs = createSelector(
    [selectDialogs, selectChatSearchQuery],
    (dialogsList, query) => {
        const normalized = normalize(query)
        return dialogsList.filter((dialog) => !normalized || normalize(dialog.name).includes(normalized))
    },
)

export const selectActiveDialog = createSelector(
    [selectDialogs, selectActiveDialogId],
    (dialogsList, activeDialogId) => dialogsList.find((dialog) => dialog.id === activeDialogId) || null,
)

export default collideSlice.reducer
