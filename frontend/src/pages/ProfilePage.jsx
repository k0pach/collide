import { useEffect, useMemo, useState } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { Link, useParams } from 'react-router-dom'
import Button from '../components/Button.jsx'
import CollectionCard from '../components/CollectionCard.jsx'
import ItemCard from '../components/ItemCard.jsx'
import ProfileHeader from '../components/ProfileHeader.jsx'
import ProfileTabs from '../components/ProfileTabs.jsx'
import SectionHeader from '../components/SectionHeader.jsx'
import SortControls from '../components/SortControls.jsx'
import StatsPanel from '../components/StatsPanel.jsx'
import {
  openAddCollectionModal,
  openAddItemModal,
  selectCollectionsByUser,
  selectCurrentUser,
  selectFavoritesByCurrentUser,
  selectItemsByUser,
  selectProfileSearchQuery,
  selectUserProfileMetrics,
  selectAuth,
  updateUserProfile,
} from '../features/collide/collideSlice.js'
import { exportItemsToCsv } from '../utils/exportCsv.js'

const normalize = (value) => String(value || '').trim().toLowerCase()

const commonSortOptions = [
  { value: 'alphabet', label: 'По алфавиту' },
  { value: 'popular', label: 'По популярности' },
  { value: 'new', label: 'Сначала новые' },
]

const favoriteSortOptions = [
  { value: 'alphabet', label: 'По алфавиту' },
  { value: 'popular', label: 'По популярности' },
  { value: 'new', label: 'Сначала новые' },
]

function sortEntries(entries, sortType) {
  const prepared = [...entries]

  if (sortType === 'alphabet') {
    return prepared.sort((a, b) => a.title.localeCompare(b.title, 'ru'))
  }

  if (sortType === 'new') {
    return prepared.sort((a, b) => (b.createdAt || 0) - (a.createdAt || 0))
  }

  return prepared.sort((a, b) => (b.popularity || b.likes || b.itemsCount || b.averageRating || 0) - (a.popularity || a.likes || a.itemsCount || a.averageRating || 0))
}

function ProfilePage() {
  const { tab = 'collections' } = useParams()
  const dispatch = useDispatch()
  const auth = useSelector(selectAuth)
  const user = useSelector(selectCurrentUser)

  const collectionsSelector = useMemo(() => selectCollectionsByUser(user?.id), [user?.id])
  const itemsSelector = useMemo(() => selectItemsByUser(user?.id), [user?.id])
  const metricsSelector = useMemo(() => selectUserProfileMetrics(user?.id), [user?.id])

  const collections = useSelector(collectionsSelector)
  const items = useSelector(itemsSelector)
  const favorites = useSelector(selectFavoritesByCurrentUser)
  const profileSearchQuery = useSelector(selectProfileSearchQuery)
  const metrics = useSelector(metricsSelector)

  const [collectionsSort, setCollectionsSort] = useState('popular')
  const [itemsSort, setItemsSort] = useState('popular')
  const [favoriteType, setFavoriteType] = useState('collections')
  const [favoriteSort, setFavoriteSort] = useState('popular')

  // Состояние для редактирования профиля
  const [isEditing, setIsEditing] = useState(false)
  const [editForm, setEditForm] = useState(null)
  const [isSaving, setIsSaving] = useState(false)

  useEffect(() => {
    if (user) {
      setEditForm({ name: user.name, about: user.about || '' })
    }
  }, [user])

  const normalizedTab = ['collections', 'items', 'favorites', 'stats'].includes(tab) ? tab : 'collections'
  const sortedCollections = useMemo(() => sortEntries(collections, collectionsSort), [collections, collectionsSort])
  const sortedItems = useMemo(() => sortEntries(items, itemsSort), [items, itemsSort])

  const favoriteCollections = useMemo(() => {
    const query = normalize(profileSearchQuery)
    return sortEntries(
        favorites.collections.filter((collection) => !query || normalize(`${collection.title} ${collection.description} ${collection.ownerName}`).includes(query)),
        favoriteSort,
    )
  }, [favorites.collections, favoriteSort, profileSearchQuery])

  const favoriteItems = useMemo(() => {
    const query = normalize(profileSearchQuery)
    return sortEntries(
        favorites.items.filter((item) => !query || normalize(`${item.title} ${item.description} ${item.fullDescription} ${item.collectionTitle}`).includes(query)),
        favoriteSort,
    )
  }, [favorites.items, favoriteSort, profileSearchQuery])

  // ВАЖНО: Early return (преждевременный выход) должен быть ПОСЛЕ всех хуков (useMemo, useState, useEffect)
  if (!auth.token || !user) {
    return (
        <main className="auth-page">
          <section className="auth-card">
            <h1>Профиль</h1>
            <p>Для просмотра профиля нужно войти или зарегистрироваться.</p>
            <Link className="nav-auth-link nav-auth-link--panel" to="/auth">Перейти ко входу</Link>
          </section>
        </main>
    )
  }

  const submitEditProfile = async (event) => {
    event.preventDefault()
    setIsSaving(true)
    try {
      await dispatch(updateUserProfile({ id: user.id, ...editForm })).unwrap()
      setIsEditing(false)
    } catch (err) {
      console.error(err)
    } finally {
      setIsSaving(false)
    }
  }

  return (
      <main className="profile-page">
        <div className="profile-content profile-content--standalone">
          <ProfileHeader
              user={user}
              metrics={metrics}
              onEdit={() => setIsEditing(!isEditing)}
          />

          {isEditing && editForm && (
              <section className="edit-card profile-edit-card">
                <h2>Редактирование профиля</h2>
                <form className="form-grid" onSubmit={submitEditProfile}>
                  <label>
                    <span>Отображаемое имя</span>
                    <input
                        value={editForm.name}
                        onChange={(event) => setEditForm((prev) => ({ ...prev, name: event.target.value }))}
                        required
                    />
                  </label>

                  <label className="form-grid__wide">
                    <span>О себе</span>
                    <textarea
                        value={editForm.about}
                        onChange={(event) => setEditForm((prev) => ({ ...prev, about: event.target.value }))}
                        rows="4"
                        placeholder="Расскажите немного о себе..."
                    />
                  </label>

                  <div className="form-grid__actions">
                    <Button variant="ghost" type="button" onClick={() => setIsEditing(false)}>Отмена</Button>
                    <Button variant="primary" type="submit" disabled={isSaving}>
                      {isSaving ? 'Сохранение...' : 'Сохранить изменения'}
                    </Button>
                  </div>
                </form>
              </section>
          )}

          <div className="profile-panel">
            <ProfileTabs />

            {normalizedTab === 'collections' && (
                <section>
                  <SectionHeader title="Коллекции">
                    <SortControls value={collectionsSort} onChange={setCollectionsSort} options={commonSortOptions} />
                    <Button variant="primary" onClick={() => dispatch(openAddCollectionModal())}>
                      Новая коллекция
                    </Button>
                  </SectionHeader>
                  <div className="collection-grid">
                    {sortedCollections.map((collection) => <CollectionCard key={collection.id} collection={collection} />)}
                  </div>
                  {sortedCollections.length === 0 && <p className="empty-note">По этому запросу коллекций не найдено.</p>}
                </section>
            )}

            {normalizedTab === 'items' && (
                <section>
                  <SectionHeader title="Предметы">
                    <SortControls value={itemsSort} onChange={setItemsSort} options={commonSortOptions} />
                    <Button variant="primary" onClick={() => dispatch(openAddItemModal())}>Новый предмет</Button>
                    <Button variant="secondary" onClick={() => exportItemsToCsv(items)}>Выгрузить в CSV</Button>
                  </SectionHeader>
                  <div className="items-grid">
                    {sortedItems.map((item) => <ItemCard key={item.id} item={item} />)}
                  </div>
                  {sortedItems.length === 0 && <p className="empty-note">По этому запросу предметов не найдено.</p>}
                </section>
            )}

            {normalizedTab === 'favorites' && (
                <section>
                  <SectionHeader title="Избранное">
                    <label className="feed-control">
                      <span>Фильтр</span>
                      <select value={favoriteType} onChange={(event) => setFavoriteType(event.target.value)}>
                        <option value="collections">Коллекции</option>
                        <option value="items">Предметы</option>
                      </select>
                    </label>
                    <SortControls value={favoriteSort} onChange={setFavoriteSort} options={favoriteSortOptions} />
                  </SectionHeader>

                  {favoriteType === 'collections' ? (
                      <div className="collection-grid">
                        {favoriteCollections.map((collection) => <CollectionCard key={collection.id} collection={collection} />)}
                      </div>
                  ) : (
                      <div className="items-grid">
                        {favoriteItems.map((item) => <ItemCard key={item.id} item={item} compact readonly />)}
                      </div>
                  )}

                  {favoriteType === 'collections' && favoriteCollections.length === 0 && <p className="empty-note">В избранном коллекций пока нет.</p>}
                  {favoriteType === 'items' && favoriteItems.length === 0 && <p className="empty-note">В избранном предметов пока нет.</p>}
                </section>
            )}

            {normalizedTab === 'stats' && (
                <section>
                  <SectionHeader title="Статистика" />
                  <StatsPanel collections={collections} items={items} metrics={metrics} />
                </section>
            )}
          </div>
        </div>
      </main>
  )
}

export default ProfilePage