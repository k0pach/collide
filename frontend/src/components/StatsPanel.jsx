function StatsPanel({ collections, items, metrics = {} }) {
  const saleCount = items.filter((item) => item.status === 'sale').length
  const exchangeCount = items.filter((item) => item.status === 'exchange').length
  const archivedCount = items.filter((item) => item.status === 'archive').length

  return (
    <div className="stats-grid">
      <article className="stat-card">
        <strong>{collections.length}</strong>
        <span>Коллекций</span>
      </article>
      <article className="stat-card">
        <strong>{items.length}</strong>
        <span>Предметов</span>
      </article>
      <article className="stat-card">
        <strong>{metrics.collectionAverageRating || '—'}</strong>
        <span>Средний балл коллекций</span>
      </article>
      <article className="stat-card">
        <strong>{metrics.totalItemLikes || 0}</strong>
        <span>Лайков на предметах</span>
      </article>
      <article className="stat-card">
        <strong>{metrics.followersCount || 0}</strong>
        <span>Подписчиков</span>
      </article>
      <article className="stat-card">
        <strong>{metrics.totalCollectionsValueLabel || '0 ₽'}</strong>
        <span>Стоимость коллекций</span>
      </article>
      <article className="stat-card">
        <strong>{saleCount}</strong>
        <span>В продаже</span>
      </article>
      <article className="stat-card">
        <strong>{exchangeCount}</strong>
        <span>Для обмена</span>
      </article>
      <article className="stat-card">
        <strong>{archivedCount}</strong>
        <span>В архиве</span>
      </article>
    </div>
  )
}

export default StatsPanel
