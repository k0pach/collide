import { useMemo, useState } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import CategoryIcon from '../components/CategoryIcon.jsx'
import CategorySidebar from '../components/CategorySidebar.jsx'
import CollectionCard from '../components/CollectionCard.jsx'
import Footer from '../components/Footer.jsx'
import ItemCard from '../components/ItemCard.jsx'
import SectionHeader from '../components/SectionHeader.jsx'
import SortControls from '../components/SortControls.jsx'
import {
  selectCategories,
  selectCollections,
  selectFilteredItems,
  selectSearchQuery,
  selectSelectedCategory,
  setSelectedCategory,
} from '../features/collide/collideSlice.js'

const normalize = (value) => String(value || '').trim().toLowerCase()

const feedSortOptions = [
  { value: 'popular', label: 'По популярности' },
  { value: 'alphabet', label: 'По алфавиту' },
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

  return prepared.sort((a, b) => (b.popularity || b.likes || b.itemsCount || 0) - (a.popularity || a.likes || a.itemsCount || 0))
}

function HomePage() {
  const dispatch = useDispatch()
  const [feedType, setFeedType] = useState('collections')
  const [sortType, setSortType] = useState('popular')
  const categories = useSelector(selectCategories).filter((category) => category.id !== 'all')
  const collections = useSelector(selectCollections)
  const items = useSelector(selectFilteredItems)
  const selectedCategory = useSelector(selectSelectedCategory)
  const searchQuery = useSelector(selectSearchQuery)

  const visibleCollections = useMemo(() => {
    const query = normalize(searchQuery)
    return sortEntries(
      collections
        .filter((collection) => selectedCategory === 'all' || collection.category === selectedCategory)
        .filter((collection) => !query || normalize(`${collection.title} ${collection.description} ${collection.ownerName}`).includes(query)),
      sortType,
    )
  }, [collections, searchQuery, selectedCategory, sortType])

  const visibleItems = useMemo(() => sortEntries(items, sortType), [items, sortType])
  const topCollections = useMemo(() => sortEntries(collections, 'popular').slice(0, 4), [collections])
  const feedTitle = feedType === 'collections' ? 'Коллекции' : 'Предметы'

  return (
    <main className="general-page">
      <div className="main-layout main-layout--home">
        <CategorySidebar />
        <div className="home-content">
          <section className="top-categories">
            <SectionHeader title="Топ категорий" />
            <div className="category-tile-grid">
              {categories.slice(0, 8).map((category) => (
                <button
                  type="button"
                  className={`category-tile ${selectedCategory === category.id ? 'category-tile--active' : ''}`}
                  key={category.id}
                  onClick={() => dispatch(setSelectedCategory(category.id))}
                >
                  <CategoryIcon categoryId={category.id} title={category.title} className="category-icon--large" />
                  <strong>{category.title}</strong>
                </button>
              ))}
            </div>
          </section>

          <section>
            <SectionHeader title="Топ коллекций недели" />
            <div className="collection-grid collection-grid--wide">
              {topCollections.map((collection) => <CollectionCard key={collection.id} collection={collection} />)}
            </div>
          </section>

          <section>
            <SectionHeader title={feedTitle}>
              <label className="feed-control">
                <span>Фильтр</span>
                <select value={feedType} onChange={(event) => setFeedType(event.target.value)}>
                  <option value="collections">Коллекции</option>
                  <option value="items">Предметы</option>
                </select>
              </label>
              <SortControls value={sortType} onChange={setSortType} options={feedSortOptions} />
            </SectionHeader>

            {feedType === 'collections' ? (
              <div className="collection-grid">
                {visibleCollections.map((collection) => <CollectionCard key={collection.id} collection={collection} />)}
              </div>
            ) : (
              <div className="items-grid">
                {visibleItems.map((item) => <ItemCard key={item.id} item={item} compact readonly />)}
              </div>
            )}

            {feedType === 'collections' && visibleCollections.length === 0 && <p className="empty-note">Коллекции не найдены.</p>}
            {feedType === 'items' && visibleItems.length === 0 && <p className="empty-note">Предметы не найдены.</p>}
          </section>
        </div>
      </div>
      <Footer />
    </main>
  )
}

export default HomePage
