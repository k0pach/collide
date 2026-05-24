import {Link} from 'react-router-dom'
import {useDispatch, useSelector} from 'react-redux'
import {deleteItem, selectCurrentUserId, setItemStatus} from '../features/collide/collideSlice.js'
import Button from './Button.jsx'
import ItemVisual from './ItemVisual.jsx'

function ItemCard({item, compact = false, readonly = false, onRemoveFromCollection}) {
    const dispatch = useDispatch()
    const currentUserId = useSelector(selectCurrentUserId)
    const canEdit = !readonly && item.ownerId === currentUserId

    return (
        <article className={`item-card ${compact ? 'item-card--compact' : ''}`}>
            <Link to={`/items/${item.id}`} className="item-card__visual-link"
                  aria-label={`Открыть предмет ${item.title}`}>
                <ItemVisual item={item}/>
            </Link>
            <div className="item-card__body">
                <div className="item-card__header">
                    <Link to={`/items/${item.id}`} className="item-card__title">
                        <h3>{item.title}</h3>
                    </Link>
                    <span className={`status-badge status-badge--${item.status}`}>{item.statusLabel}</span>
                </div>
                <p className="item-card__description">{item.description}</p>
                <div className="item-card__meta">
                    <span>{item.collectionTitle}</span>
                    <strong>{item.price}</strong>
                </div>
                {canEdit && (
                    <div className="item-card__actions">
                        <select
                            value={item.status}
                            onChange={(event) => dispatch(setItemStatus({id: item.id, status: event.target.value}))}
                            aria-label="Изменить статус предмета"
                        >
                            <option value="collection">В коллекции</option>
                            <option value="sale">В продаже</option>
                            <option value="exchange">Для обмена</option>
                            <option value="archive">В архиве</option>
                        </select>
                        {onRemoveFromCollection && (
                            <Button variant="ghost" onClick={() => onRemoveFromCollection(item.id)}>Убрать из
                                коллекции</Button>
                        )}
                        <Button variant="ghost" onClick={() => dispatch(deleteItem(item.id))}>Удалить</Button>
                    </div>
                )}
            </div>
        </article>
    )
}

export default ItemCard
