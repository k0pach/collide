import { useEffect, useMemo, useRef, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { useDispatch, useSelector } from 'react-redux'
import Avatar from '../components/Avatar.jsx'
import Button from '../components/Button.jsx'
import CollectionVisual from '../components/CollectionVisual.jsx'
import ItemCard from '../components/ItemCard.jsx'
import RatingModal from '../components/RatingModal.jsx'
import SectionHeader from '../components/SectionHeader.jsx'
import SortControls from '../components/SortControls.jsx'
import {
  addCollectionComment,
  rateCollection,
  removeItemFromCollection,
  selectCategories,
  selectCollectionById,
  selectCollectionFavoritedByCurrentUser,
  selectCurrentUser,
  selectItemsByCollection,
  toggleFavoriteCollection,
  updateCollection,
} from '../features/collide/collideSlice.js'

const itemSortOptions = [
  { value: 'alphabet', label: 'По алфавиту' },
  { value: 'likes', label: 'По количеству лайков' },
  { value: 'price', label: 'По цене' },
]

function parsePrice(value) {
  const number = String(value || '').replace(/[^0-9]/g, '')
  return Number(number || 0)
}

function formatPrice(value) {
  return new Intl.NumberFormat('ru-RU').format(value) + ' ₽'
}

function sortItems(items, sortType) {
  const prepared = [...items]
  if (sortType === 'alphabet') return prepared.sort((a, b) => a.title.localeCompare(b.title, 'ru'))
  if (sortType === 'price') return prepared.sort((a, b) => parsePrice(b.price) - parsePrice(a.price))
  return prepared.sort((a, b) => (b.likes || 0) - (a.likes || 0))
}

function CollectionPage() {
  const { collectionId } = useParams()
  const dispatch = useDispatch()
  const commentsRef = useRef(null)
  const collection = useSelector(selectCollectionById(collectionId))
  const items = useSelector(selectItemsByCollection(collectionId))
  const isFavorite = useSelector(selectCollectionFavoritedByCurrentUser(collectionId))
  const currentUser = useSelector(selectCurrentUser)
  const categories = useSelector(selectCategories).filter((category) => category.id !== 'all')
  const [editForm, setEditForm] = useState(null)
  const [isEditing, setIsEditing] = useState(false)
  const [isRatingOpen, setIsRatingOpen] = useState(false)
  const [comment, setComment] = useState('')
  const [shareStatus, setShareStatus] = useState('')
  const [sortType, setSortType] = useState('likes')
  const canEdit = collection?.ownerId === currentUser.id

  useEffect(() => {
    if (!collection) return
    setEditForm({
      title: collection.title,
      description: collection.description,
      category: collection.category,
      coverImageUrl: collection.coverImageUrl || '',
      placeholderColor: collection.placeholderColor || '#FB8500',
    })
    setIsEditing(false)
  }, [collection?.id])

  const sortedItems = useMemo(() => sortItems(items, sortType), [items, sortType])
  const totalPrice = useMemo(() => items.reduce((sum, item) => sum + parsePrice(item.price), 0), [items])

  if (!collection) {
    return (
      <main className="collection-page">
        <section className="item-detail item-detail--missing">
          <h1>Коллекция не найдена</h1>
          <p>Возможно, ссылка устарела или коллекция была удалена.</p>
          <Link to="/">Вернуться на главную</Link>
        </section>
      </main>
    )
  }

  const updateEditField = (field, value) => setEditForm((prev) => ({ ...prev, [field]: value }))

  const handleCoverChange = (event) => {
    const file = event.target.files?.[0]
    if (!file) return
    const objectUrl = URL.createObjectURL(file)
    updateEditField('coverImageUrl', objectUrl)
  }

  const submitEditForm = (event) => {
    event.preventDefault()
    dispatch(updateCollection({ id: collection.id, ...editForm }))
    setIsEditing(false)
  }

  const submitComment = (event) => {
    event.preventDefault()
    dispatch(addCollectionComment({ collectionId: collection.id, text: comment }))
    setComment('')
  }

  const submitRating = (value) => {
    dispatch(rateCollection({ collectionId: collection.id, value }))
    setIsRatingOpen(false)
  }

  const scrollToComments = () => {
    commentsRef.current?.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }

  const shareCollection = async () => {
    const link = window.location.href
    try {
      if (navigator.clipboard?.writeText) {
        await navigator.clipboard.writeText(link)
        setShareStatus('Ссылка скопирована')
      } else {
        setShareStatus(link)
      }
    } catch {
      setShareStatus(link)
    }
  }

  return (
    <main className="collection-page">
      <section className="item-detail item-detail--collection">
        <div className="item-detail__media">
          <CollectionVisual collection={collection} className="collection-cover--large" />
        </div>

        <div className="item-detail__content">
          <div className="item-detail__topline">
            <span className="status-badge status-badge--collection">{collection.itemsCount} предметов</span>
            <span className="item-detail__collection">★ {collection.averageRating || '—'} · {collection.ratingsCount} оценок</span>
          </div>
          <h1>{collection.title}</h1>
          <p className="item-detail__short">{collection.description}</p>
          <p className="item-detail__price">Общая стоимость: {formatPrice(totalPrice)}</p>

          <div className="item-detail__owner">
            <Avatar tone="orange" size="sm" label={collection.ownerName} />
            <div>
              <span>Владелец</span>
              <Link to={collection.ownerId === currentUser.id ? '/profile/collections' : `/users/${collection.ownerId}/collections`}>
                {collection.ownerName} {collection.ownerHandle}
              </Link>
            </div>
          </div>

          <div className="item-detail__actions">
            <Button variant="primary" onClick={() => setIsRatingOpen(true)}>★ Оценить</Button>
            <Button variant={isFavorite ? 'secondary' : 'ghost'} onClick={() => dispatch(toggleFavoriteCollection(collection.id))}>
              {isFavorite ? 'В избранном' : 'В избранное'}
            </Button>
            <Button variant="ghost" onClick={scrollToComments}>Комментарии · {collection.comments?.length || 0}</Button>
            <Button variant="ghost" onClick={shareCollection}>Поделиться</Button>
            {canEdit && <Button variant="secondary" onClick={() => setIsEditing((value) => !value)}>{isEditing ? 'Отменить редактирование' : 'Редактировать'}</Button>}
          </div>
          {shareStatus && <p className="share-status">{shareStatus}</p>}
        </div>
      </section>

      {canEdit && isEditing && editForm && (
        <section className="edit-card">
          <h2>Редактирование коллекции</h2>
          <form className="form-grid" onSubmit={submitEditForm}>
            <label>
              <span>Название коллекции</span>
              <input value={editForm.title} onChange={(event) => updateEditField('title', event.target.value)} required />
            </label>

            <label>
              <span>Категория</span>
              <select value={editForm.category} onChange={(event) => updateEditField('category', event.target.value)}>
                {categories.map((category) => <option key={category.id} value={category.id}>{category.title}</option>)}
              </select>
            </label>

            <label className="form-grid__wide">
              <span>Описание коллекции</span>
              <textarea value={editForm.description} onChange={(event) => updateEditField('description', event.target.value)} rows="4" />
            </label>

            <label>
              <span>Новое фото коллекции</span>
              <input type="file" accept="image/*" onChange={handleCoverChange} />
            </label>

            <div className="form-grid__wide upload-preview">
              <div
                className="upload-preview__image"
                style={editForm.coverImageUrl ? {} : { background: `linear-gradient(135deg, ${editForm.placeholderColor}, #F3D5B5)` }}
              >
                {editForm.coverImageUrl ? <img src={editForm.coverImageUrl} alt="Фото коллекции" /> : <span>Фото не выбрано</span>}
              </div>
              <p>Можно заменить обложку коллекции или оставить цветовую заглушку.</p>
            </div>

            <div className="form-grid__actions">
              <Button variant="primary" type="submit">Сохранить коллекцию</Button>
            </div>
          </form>
        </section>
      )}

      <section className="collection-items-panel">
        <SectionHeader title="Предметы коллекции">
          <SortControls value={sortType} onChange={setSortType} options={itemSortOptions} />
        </SectionHeader>
        <div className="items-grid">
          {sortedItems.map((item) => (
            <ItemCard
              key={item.id}
              item={item}
              readonly={!canEdit}
              onRemoveFromCollection={canEdit ? (itemId) => dispatch(removeItemFromCollection(itemId)) : undefined}
            />
          ))}
        </div>
        {sortedItems.length === 0 && <p className="empty-note">В этой коллекции пока нет предметов.</p>}
      </section>

      <section className="comments-card" ref={commentsRef} id="collection-comments">
        <h2>Комментарии · {collection.comments?.length || 0}</h2>
        <form className="comment-form" onSubmit={submitComment}>
          <textarea
            value={comment}
            onChange={(event) => setComment(event.target.value)}
            rows="4"
            placeholder="Оставьте комментарий к коллекции"
          />
          <Button variant="primary" type="submit">Отправить комментарий</Button>
        </form>

        <div className="comments-list">
          {collection.comments.length === 0 && <p className="empty-note">Комментариев пока нет. Станьте первым.</p>}
          {collection.comments.map((entry) => (
            <article className="comment" key={entry.id}>
              <Avatar tone="cream" size="xs" label={entry.authorName} />
              <div>
                <header>
                  <strong>{entry.authorName}</strong>
                  <span>{entry.time}</span>
                </header>
                <p>{entry.text}</p>
              </div>
            </article>
          ))}
        </div>
      </section>

      {isRatingOpen && <RatingModal onClose={() => setIsRatingOpen(false)} onSubmit={submitRating} />}
    </main>
  )
}

export default CollectionPage
