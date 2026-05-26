import { useMemo, useState } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { addItem, selectCategories, selectCollections, selectCurrentUserId } from '../features/collide/collideSlice.js'
import { uploadImageFile } from '../services/api.js'
import Button from './Button.jsx'

const SHORT_DESCRIPTION_LIMIT = 60
const warmColors = ['#FD3E3E', '#FB8500', '#FFB703', '#F3D5B5', '#BC8A5F', '#A47148']

function getRandomWarmColor() {
  return warmColors[Math.floor(Math.random() * warmColors.length)]
}

function AddItemForm() {
  const dispatch = useDispatch()
  const currentUserId = useSelector(selectCurrentUserId)
  const collections = useSelector(selectCollections).filter((collection) => String(collection.ownerId) === String(currentUserId))
  const categories = useSelector(selectCategories).filter((category) => category.id !== 'all')
  const initialPlaceholderColor = useMemo(() => getRandomWarmColor(), [])
  const [imageFile, setImageFile] = useState(null)
  const [imagePreview, setImagePreview] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState('')
  const [form, setForm] = useState({
    title: '',
    category: categories[0]?.id || 'figures',
    collectionId: '',
    status: 'collection',
    price: '',
    description: '',
    fullDescription: '',
    imageUrl: '',
    placeholderColor: initialPlaceholderColor,
  })

  const updateField = (field, value) => setForm((prev) => ({ ...prev, [field]: value }))

  const handleImageChange = (event) => {
    const file = event.target.files?.[0]
    setImageFile(file || null)
    if (!file) {
      setImagePreview('')
      updateField('imageUrl', '')
      return
    }

    const objectUrl = URL.createObjectURL(file)
    setImagePreview(objectUrl)
  }

  const submitForm = async (event) => {
    event.preventDefault()
    setIsSubmitting(true)
    setError('')

    try {
      let imageUrl = form.imageUrl
      if (imageFile) {
        try {
          const upload = await uploadImageFile(imageFile)
          imageUrl = upload?.url || imageUrl
        } catch {
          imageUrl = imagePreview || imageUrl
        }
      }
      await dispatch(addItem({ ...form, imageUrl })).unwrap()
    } catch (requestError) {
      setError(String(requestError || 'Не удалось сохранить предмет'))
    } finally {
      setIsSubmitting(false)
    }
  }

  const descriptionLeft = SHORT_DESCRIPTION_LIMIT - form.description.length

  return (
    <form className="form-grid" onSubmit={submitForm}>
      <label>
        <span>Название предмета</span>
        <input
          value={form.title}
          onChange={(event) => updateField('title', event.target.value)}
          placeholder="Например: Комикс выпуск #12"
          required
        />
      </label>

      <label>
        <span>Коллекция</span>
        <select value={form.collectionId} onChange={(event) => updateField('collectionId', event.target.value)}>
          <option value="">Без коллекции</option>
          {collections.map((collection) => <option key={collection.id} value={collection.id}>{collection.title}</option>)}
        </select>
      </label>

      <label>
        <span>Категория</span>
        <select value={form.category} onChange={(event) => updateField('category', event.target.value)}>
          {categories.map((category) => <option key={category.id} value={category.id}>{category.title}</option>)}
        </select>
      </label>

      <label>
        <span>Статус</span>
        <select value={form.status} onChange={(event) => updateField('status', event.target.value)}>
          <option value="collection">В коллекции</option>
          <option value="sale">В продаже</option>
          <option value="exchange">Для обмена</option>
          <option value="archive">В архиве</option>
        </select>
      </label>

      <label>
        <span>Стоимость</span>
        <input value={form.price} onChange={(event) => updateField('price', event.target.value)} placeholder="2 900 ₽" />
      </label>

      <label>
        <span>Фото предмета</span>
        <input type="file" accept="image/*" onChange={handleImageChange} />
      </label>

      <div className="form-grid__wide upload-preview">
        <div
          className="upload-preview__image"
          style={imagePreview ? {} : { background: `linear-gradient(135deg, ${form.placeholderColor}, #F3D5B5)` }}
        >
          {imagePreview ? <img src={imagePreview} alt="Предпросмотр" /> : <span>Фото не выбрано</span>}
        </div>
        <p>Если фото не выбрать, карточка получит случайную тёплую заглушку в палитре сайта.</p>
      </div>

      <label className="form-grid__wide">
        <span>Краткое описание</span>
        <textarea
          value={form.description}
          maxLength={SHORT_DESCRIPTION_LIMIT}
          onChange={(event) => updateField('description', event.target.value)}
          rows="3"
          placeholder="Краткое описание для карточки"
        />
        <small className="field-counter">Осталось символов: {descriptionLeft}</small>
      </label>

      <label className="form-grid__wide">
        <span>Полное описание</span>
        <textarea
          value={form.fullDescription}
          onChange={(event) => updateField('fullDescription', event.target.value)}
          rows="6"
          placeholder="Подробно опишите состояние, историю, комплектацию и условия обмена или продажи"
        />
      </label>

      {error && <p className="form-error form-grid__wide">{error}</p>}

      <div className="form-grid__actions">
        <Button variant="primary" type="submit" disabled={isSubmitting}>{isSubmitting ? 'Сохранение...' : 'Сохранить предмет'}</Button>
      </div>
    </form>
  )
}

export default AddItemForm
