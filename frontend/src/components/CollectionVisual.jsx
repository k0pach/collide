function CollectionVisual({ collection, className = '' }) {
  const styles = collection.coverImageUrl
    ? {}
    : {
        background: `linear-gradient(135deg, ${collection.placeholderColor || '#FB8500'}, #F3D5B5)`,
      }

  return (
    <div className={`collection-cover collection-cover--${collection.coverTone || 'orange'} ${className}`.trim()} style={styles}>
      {collection.coverImageUrl ? (
        <img src={collection.coverImageUrl} alt={collection.title} />
      ) : (
        <span>{collection.title}</span>
      )}
    </div>
  )
}

export default CollectionVisual
