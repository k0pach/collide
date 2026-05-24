import { useMemo, useState } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { useParams } from 'react-router-dom'
import CollectionCard from '../components/CollectionCard.jsx'
import ItemCard from '../components/ItemCard.jsx'
import ProfileHeader from '../components/ProfileHeader.jsx'
import ProfileTabs from '../components/ProfileTabs.jsx'
import SectionHeader from '../components/SectionHeader.jsx'
import SortControls from '../components/SortControls.jsx'
import StatsPanel from '../components/StatsPanel.jsx'
import {
  selectCollectionsByUser,
  selectIsFollowingUser,
  selectItemsByUser,
  selectUserById,
  selectUserProfileMetrics,
  toggleFollowUser,
} from '../features/collide/collideSlice.js'

const commonSortOptions = [
  { value: 'alphabet', label: 'По алфавиту' },
  { value: 'popular', label: 'По популярности' },
  { value: 'new', label: 'Сначала новые' },
]

function sortEntries(entries, sortType) {
  const prepared = [...entries]

  if (sortType === 'alphabet') return prepared.sort((a, b) => a.title.localeCompare(b.title, 'ru'))
  if (sortType === 'new') return prepared.sort((a, b) => (b.createdAt || 0) - (a.createdAt || 0))

  return prepared.sort((a, b) => (b.popularity || b.likes || b.itemsCount || b.averageRating || 0) - (a.popularity || a.likes || a.itemsCount || a.averageRating || 0))
}

function ForeignProfilePage() {
  const { id = 'u2', tab = 'collections' } = useParams()
  const dispatch = useDispatch()
  const requestedUser = useSelector(selectUserById(id))
  const fallbackUser = useSelector(selectUserById('u2'))
  const user = requestedUser || fallbackUser
  const collections = useSelector(selectCollectionsByUser(user.id))
  const items = useSelector(selectItemsByUser(user.id))
  const metrics = useSelector(selectUserProfileMetrics(user.id))
  const isFollowing = useSelector(selectIsFollowingUser(user.id))
  const [collectionsSort, setCollectionsSort] = useState('popular')
  const [itemsSort, setItemsSort] = useState('popular')
  const normalizedTab = ['collections', 'items', 'stats'].includes(tab) ? tab : 'collections'

  const sortedCollections = useMemo(() => sortEntries(collections, collectionsSort), [collections, collectionsSort])
  const sortedItems = useMemo(() => sortEntries(items, itemsSort), [items, itemsSort])

  return (
    <main className="profile-page">
      <div className="profile-content profile-content--standalone">
        <ProfileHeader
          user={user}
          metrics={metrics}
          isOwnProfile={false}
          isFollowing={isFollowing}
          onToggleFollow={() => dispatch(toggleFollowUser(user.id))}
        />
        <div className="profile-panel">
          <ProfileTabs basePath={`/users/${user.id}`} showFavorites={false} />
          {normalizedTab === 'collections' && (
            <section>
              <SectionHeader title="Популярное">
                <SortControls value={collectionsSort} onChange={setCollectionsSort} options={commonSortOptions} />
              </SectionHeader>
              <div className="collection-grid">
                {sortedCollections.map((collection) => <CollectionCard key={collection.id} collection={collection} readonly />)}
              </div>
              {sortedCollections.length === 0 && <p className="empty-note">По этому запросу коллекций не найдено.</p>}
            </section>
          )}
          {normalizedTab === 'items' && (
            <section>
              <SectionHeader title="Предметы">
                <SortControls value={itemsSort} onChange={setItemsSort} options={commonSortOptions} />
              </SectionHeader>
              <div className="items-grid">
                {sortedItems.map((item) => <ItemCard key={item.id} item={item} readonly />)}
              </div>
              {sortedItems.length === 0 && <p className="empty-note">По этому запросу предметов не найдено.</p>}
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

export default ForeignProfilePage
