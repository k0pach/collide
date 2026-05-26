import { useDispatch, useSelector } from 'react-redux'
import {
  selectCategories,
  selectSearchQuery,
  selectSelectedCategory,
  setSearchQuery,
  setSelectedCategory,
} from '../features/collide/collideSlice.js'
import CategoryIcon from './CategoryIcon.jsx'
import SearchInput from './SearchInput.jsx'

function CategorySidebar() {
  const dispatch = useDispatch()
  const categories = useSelector(selectCategories)
  const selectedCategory = useSelector(selectSelectedCategory)
  const searchQuery = useSelector(selectSearchQuery)

  const visibleCategories = categories.filter((category) => {
    if (category.id === 'all') return true
    return category.title.toLowerCase().includes(searchQuery.trim().toLowerCase())
  })

  return (
    <aside className="category-sidebar" aria-label="Категории">
      <div className="category-sidebar__inner">
        <h2>Категории</h2>
        <SearchInput
          value={searchQuery}
          onChange={(value) => dispatch(setSearchQuery(value))}
          placeholder="Найти категорию"
        />
        <div className="category-list">
          {visibleCategories.map((category) => (
            <button
              key={category.id}
              type="button"
              className={`category-row ${selectedCategory === category.id ? 'category-row--active' : ''}`}
              onClick={() => dispatch(setSelectedCategory(category.id))}
            >
              <CategoryIcon categoryId={category.id} title={category.title} />
              <span>{category.title}</span>
            </button>
          ))}
        </div>
      </div>
    </aside>
  )
}

export default CategorySidebar
