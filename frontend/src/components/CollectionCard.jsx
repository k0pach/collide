import { Link } from 'react-router-dom'
import CollectionVisual from './CollectionVisual.jsx'

function CollectionCard({ collection }) {
  return (
    <article className="collection-card">
      <Link to={`/collections/${collection.id}`} className="collection-card__cover-link" aria-label={`Открыть коллекцию ${collection.title}`}>
        <CollectionVisual collection={collection} />
      </Link>
      <div className="collection-card__body">
        <Link to={`/collections/${collection.id}`} className="collection-card__title">
          <h3>{collection.title}</h3>
        </Link>
        <p>{collection.description}</p>
        <div className="collection-card__bottom">
          <span>{collection.itemsCount} предметов</span>
          <span>{collection.ownerName}</span>
        </div>
      </div>
    </article>
  )
}

export default CollectionCard
