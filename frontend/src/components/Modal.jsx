import Button from './Button.jsx'

function Modal({title, children, onClose}) {
    return (
        <div className="modal-backdrop" role="presentation" onMouseDown={onClose}>
            <section className="modal-card" role="dialog" aria-modal="true" aria-label={title} onMouseDown={(event) => event.stopPropagation()}>
                <header className="modal-card__header">
                    <h2>{title}</h2>
                    <Button variant="ghost" onClick={onClose} aria-label="Закрыть окно">×</Button>
                </header>
                {children}
            </section>
        </div>
    )
}

export default Modal
