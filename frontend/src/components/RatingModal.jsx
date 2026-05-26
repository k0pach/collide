import { useState } from 'react'
import Button from './Button.jsx'

function RatingModal({ title = 'Оценить коллекцию', onClose, onSubmit }) {
  const [value, setValue] = useState(5)

  const submitRating = (event) => {
    event.preventDefault()
    onSubmit(value)
  }

  return (
    <div className="modal-backdrop" role="presentation" onMouseDown={onClose}>
      <section className="modal-card rating-modal" role="dialog" aria-modal="true" aria-label={title} onMouseDown={(event) => event.stopPropagation()}>
        <header className="modal-card__header">
          <h2>{title}</h2>
          <Button variant="ghost" onClick={onClose} aria-label="Закрыть окно">×</Button>
        </header>

        <form className="rating-form" onSubmit={submitRating}>
          <p>Поставьте оценку от 1 до 5 звёзд.</p>
          <div className="rating-stars" role="radiogroup" aria-label="Оценка коллекции">
            {[1, 2, 3, 4, 5].map((star) => (
              <button
                key={star}
                type="button"
                className={star <= value ? 'rating-star rating-star--active' : 'rating-star'}
                onClick={() => setValue(star)}
                aria-label={`${star} из 5`}
              >
                ★
              </button>
            ))}
          </div>
          <div className="form-grid__actions">
            <Button variant="primary" type="submit">Сохранить оценку</Button>
          </div>
        </form>
      </section>
    </div>
  )
}

export default RatingModal
