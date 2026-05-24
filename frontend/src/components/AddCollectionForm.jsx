import {useMemo, useState} from 'react'
import {useDispatch, useSelector} from 'react-redux'
import {addCollection, selectCategories} from '../features/collide/collideSlice.js'
import Button from './Button.jsx'

const warmColors = ['#FD3E3E', '#FB8500', '#FFB703', '#F3D5B5', '#BC8A5F', '#A47148']

function getRandomWarmColor() {
    return warmColors[Math.floor(Math.random() * warmColors.length)]
}

function AddCollectionForm() {
    const dispatch = useDispatch()
    const categories = useSelector(selectCategories).filter((category) => category.id !== 'all')
    const initialPlaceholderColor = useMemo(() => getRandomWarmColor(), [])
    const [imagePreview, setImagePreview] = useState('')
    const [form, setForm] = useState({
        title: '',
        category: categories[0]?.id || 'figures',
        description: '',
        coverImageUrl: '',
        placeholderColor: initialPlaceholderColor,
    })

    const updateField = (field, value) => setForm((prev) => ({...prev, [field]: value}))

    const handleImageChange = (event) => {
        const file = event.target.files?.[0]
        if (!file) {
            setImagePreview('')
            updateField('coverImageUrl', '')
            return
        }

        const objectUrl = URL.createObjectURL(file)
        setImagePreview(objectUrl)
        updateField('coverImageUrl', objectUrl)
    }

    const submitForm = (event) => {
        event.preventDefault()
        dispatch(addCollection(form))
    }

    return (
        <form className="form-grid" onSubmit={submitForm}>
            <label>
                <span>Название коллекции</span>
                <input
                    value={form.title}
                    onChange={(event) => updateField('title', event.target.value)}
                    placeholder="Например: Винил 80-х"
                    required
                />
            </label>

            <label>
                <span>Категория</span>
                <select value={form.category} onChange={(event) => updateField('category', event.target.value)}>
                    {categories.map((category) => <option key={category.id} value={category.id}>{category.title}</option>)}
                </select>
            </label>

            <label className="form-grid__wide">
                <span>Описание коллекции</span>
                <textarea
                    value={form.description}
                    onChange={(event) => updateField('description', event.target.value)}
                    rows="4"
                    placeholder="Опишите тематику коллекции, редкость предметов и историю подбора"
                />
            </label>

            <label>
                <span>Фото коллекции</span>
                <input type="file" accept="image/*" onChange={handleImageChange}/>
            </label>

            <div className="form-grid__wide upload-preview">
                <div
                    className="upload-preview__image"
                    style={imagePreview ? {} : {background: `linear-gradient(135deg, ${form.placeholderColor}, #F3D5B5)`}}
                >
                    {imagePreview ? <img src={imagePreview} alt="Предпросмотр коллекции"/> :
                        <span>Фото не выбрано</span>}
                </div>
                <p>Если фото не выбрать, обложка получит случайную заглушку в тёплой палитре сайта.</p>
            </div>

            <div className="form-grid__actions">
                <Button variant="primary" type="submit">Сохранить коллекцию</Button>
            </div>
        </form>
    )
}

export default AddCollectionForm
