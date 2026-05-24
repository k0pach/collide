import { useEffect, useRef, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { useDispatch, useSelector } from 'react-redux'
import Avatar from '../components/Avatar.jsx'
import Button from '../components/Button.jsx'
import ItemVisual from '../components/ItemVisual.jsx'
import {
  addItemComment,
  selectCategories,
  selectCollections,
  selectCurrentUser,
  selectItemById,
  selectItemFavoritedByCurrentUser,
  selectItemLikedByCurrentUser,
  toggleFavoriteItem,
  toggleItemLike,
  updateItem,
} from '../features/collide/collideSlice.js'

const SHORT_DESCRIPTION_LIMIT = 60

function ItemPage() {
  const { itemId } = useParams()
  const dispatch = useDispatch()
  const commentsRef = useRef(null)
  const item = useSelector(selectItemById(itemId))
  const currentUser = useSelector(selectCurrentUser)
  const isLiked = useSelector(selectItemLikedByCurrentUser(itemId))
  const isFavorite = useSelector(selectItemFavoritedByCurrentUser(itemId))
  const categories = useSelector(selectCategories).filter((category) => category.id !== 'all')
  const collections = useSelector(selectCollections).filter((collection) => collection.ownerId === currentUser.id)
  const [comment, setComment] = useState('')
  const [shareStatus, setShareStatus] = useState('')
  const [editForm, setEditForm] = useState(null)
  const [isEditing, setIsEditing] = useState(false)
  const canEdit = item?.ownerId === currentUser.id

  useEffect(() => {
    if (!item) return
    setEditForm({
      title: item.title,
      price: item.price,
      description: item.description,
      fullDescription: item.fullDescription,
      category: item.category,
      collectionId: item.collectionId || '',
      status: item.status,
      imageUrl: item.imageUrl || '',
      placeholderColor: item.placeholderColor || '#FB8500',
    })
    setIsEditing(false)
  }, [item?.id])

  if (!item) {
    return (
      <main className="item-page">
        <section className="item-detail item-detail--missing">
          <h1>Предмет не найден</h1>
          <p>Возможно, ссылка устарела или предмет был удалён.</p>
          <Link to="/">Вернуться на главную</Link>
        </section>
      </main>
    )
  }

  const updateEditField = (field, value) => setEditForm((prev) => ({ ...prev, [field]: value }))

  const handleEditImageChange = (event) => {
    const file = event.target.files?.[0]
    if (!file) return
    const objectUrl = URL.createObjectURL(file)
    updateEditField('imageUrl', objectUrl)
  }

  const submitEditForm = (event) => {
    event.preventDefault()
    dispatch(updateItem({ id: item.id, ...editForm }))
    setIsEditing(false)
  }

  const submitComment = (event) => {
    event.preventDefault()
    dispatch(addItemComment({ itemId: item.id, text: comment }))
    setComment('')
  }

  const scrollToComments = () => {
    commentsRef.current?.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }

  const shareItem = async () => {
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
    <main className="item-page">
      <section className="item-detail">
        <div className="item-detail__media">
          <ItemVisual item={item} className="item-visual--large" />
        </div>

        <div className="item-detail__content">
          <div className="item-detail__topline">
            <span className={`status-badge status-badge--${item.status}`}>{item.statusLabel}</span>
            {item.collectionId ? (
              <Link to={`/collections/${item.collectionId}`} className="item-detail__collection">{item.collectionTitle}</Link>
            ) : (
              <span className="item-detail__collection">Без коллекции</span>
            )}
          </div>
          <h1>{item.title}</h1>
          <p className="item-detail__short">{item.description}</p>
          <p className="item-detail__price">{item.price}</p>

          <div className="item-detail__owner">
            <Avatar tone="orange" size="sm" label={item.ownerName} />
            <div>
              <span>Владелец</span>
              <Link to={item.ownerId === currentUser.id ? '/profile/collections' : `/users/${item.ownerId}/collections`}>
                {item.ownerName} {item.ownerHandle}
              </Link>
            </div>
          </div>

          <div className="item-detail__actions">
            <Button variant={isLiked ? 'secondary' : 'primary'} onClick={() => dispatch(toggleItemLike(item.id))}>
              {isLiked ? 'Убрать лайк' : 'Лайк'} · {item.likes}
            </Button>
            <Button variant={isFavorite ? 'secondary' : 'ghost'} onClick={() => dispatch(toggleFavoriteItem(item.id))}>
              {isFavorite ? 'В избранном' : 'В избранное'}
            </Button>
            <Button variant="ghost" onClick={scrollToComments}>Комментарии · {item.comments?.length || 0}</Button>
            <Button variant="ghost" onClick={shareItem}>Поделиться</Button>
            {canEdit && <Button variant="secondary" onClick={() => setIsEditing((value) => !value)}>{isEditing ? 'Отменить редактирование' : 'Редактировать'}</Button>}
          </div>
          {shareStatus && <p className="share-status">{shareStatus}</p>}
        </div>
      </section>

      {canEdit && isEditing && editForm && (
        <section className="edit-card">
          <h2>Редактирование предмета</h2>
          <form className="form-grid" onSubmit={submitEditForm}>
            <label>
              <span>Название</span>
              <input value={editForm.title} onChange={(event) => updateEditField('title', event.target.value)} required />
            </label>

            <label>
              <span>Цена</span>
              <input value={editForm.price} onChange={(event) => updateEditField('price', event.target.value)} />
            </label>

            <label>
              <span>Категория</span>
              <select value={editForm.category} onChange={(event) => updateEditField('category', event.target.value)}>
                {categories.map((category) => <option key={category.id} value={category.id}>{category.title}</option>)}
              </select>
            </label>

            <label>
              <span>Коллекция</span>
              <select value={editForm.collectionId} onChange={(event) => updateEditField('collectionId', event.target.value)}>
                <option value="">Без коллекции</option>
                {collections.map((collection) => <option key={collection.id} value={collection.id}>{collection.title}</option>)}
              </select>
            </label>

            <label>
              <span>Статус</span>
              <select value={editForm.status} onChange={(event) => updateEditField('status', event.target.value)}>
                <option value="collection">В коллекции</option>
                <option value="sale">В продаже</option>
                <option value="exchange">Для обмена</option>
                <option value="archive">В архиве</option>
              </select>
            </label>

            <label>
              <span>Новое фото</span>
              <input type="file" accept="image/*" onChange={handleEditImageChange} />
            </label>

            <div className="form-grid__wide upload-preview">
              <div
                className="upload-preview__image"
                style={editForm.imageUrl ? {} : { background: `linear-gradient(135deg, ${editForm.placeholderColor}, #F3D5B5)` }}
              >
                {editForm.imageUrl ? <img src={editForm.imageUrl} alt="Новое фото предмета" /> : <span>Фото не выбрано</span>}
              </div>
              <p>Можно заменить фото предмета или оставить текущую заглушку.</p>
            </div>

            <label className="form-grid__wide">
              <span>Краткое описание</span>
              <textarea
                value={editForm.description}
                maxLength={SHORT_DESCRIPTION_LIMIT}
                onChange={(event) => updateEditField('description', event.target.value)}
                rows="3"
              />
              <small className="field-counter">Осталось символов: {SHORT_DESCRIPTION_LIMIT - editForm.description.length}</small>
            </label>

            <label className="form-grid__wide">
              <span>Полное описание</span>
              <textarea
                value={editForm.fullDescription}
                onChange={(event) => updateEditField('fullDescription', event.target.value)}
                rows="6"
              />
            </label>

            <div className="form-grid__actions">
              <Button variant="primary" type="submit">Сохранить изменения</Button>
            </div>
          </form>
        </section>
      )}

      <section className="item-description-card">
        <h2>Описание</h2>
        <p>{item.fullDescription}</p>
      </section>

      <section className="comments-card" ref={commentsRef} id="comments">
        <h2>Комментарии · {item.comments?.length || 0}</h2>
        <form className="comment-form" onSubmit={submitComment}>
          <textarea
            value={comment}
            onChange={(event) => setComment(event.target.value)}
            rows="4"
            placeholder="Оставьте комментарий к предмету"
          />
          <Button variant="primary" type="submit">Отправить комментарий</Button>
        </form>

        <div className="comments-list">
          {item.comments.length === 0 && <p className="empty-note">Комментариев пока нет. Станьте первым.</p>}
          {item.comments.map((entry) => (
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
    </main>
  )
}

export default ItemPage
