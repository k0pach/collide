import { useDispatch, useSelector } from 'react-redux'
import {
  selectActiveDialogId,
  selectChatSearchQuery,
  selectFilteredDialogs,
  setActiveDialog,
  setChatSearchQuery,
} from '../features/collide/collideSlice.js'
import Avatar from './Avatar.jsx'
import SearchInput from './SearchInput.jsx'

function ChatSidebar() {
  const dispatch = useDispatch()
  const dialogs = useSelector(selectFilteredDialogs)
  const activeDialogId = useSelector(selectActiveDialogId)
  const query = useSelector(selectChatSearchQuery)

  return (
    <aside className="chat-sidebar" aria-label="Диалоги">
      <div className="chat-sidebar__header">
        <h2>Сообщения</h2>
        <SearchInput
          value={query}
          onChange={(value) => dispatch(setChatSearchQuery(value))}
          placeholder="Найти человека"
          className="search-pill--chat"
        />
      </div>

      <div className="dialog-list">
        {dialogs.length === 0 && <p className="dialog-list__empty">Переписки не найдены</p>}
        {dialogs.map((dialog) => (
          <button
            type="button"
            key={dialog.id}
            className={`dialog-card ${dialog.id === activeDialogId ? 'dialog-card--active' : ''}`}
            onClick={() => dispatch(setActiveDialog(dialog.id))}
          >
            <Avatar tone={dialog.avatarTone} size="xs" label={dialog.name} />
            <span className="dialog-card__content">
              <span className="dialog-card__name">{dialog.name}</span>
              <span className="dialog-card__preview">{dialog.preview}</span>
            </span>
            {dialog.unread > 0 && <span className="dialog-card__badge">{dialog.unread}</span>}
          </button>
        ))}
      </div>
    </aside>
  )
}

export default ChatSidebar
