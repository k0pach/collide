import { useEffect, useState } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { Link } from 'react-router-dom'
import Avatar from '../components/Avatar.jsx'
import Button from '../components/Button.jsx'
import ChatSidebar from '../components/ChatSidebar.jsx'
import { fetchDialogMessages, selectActiveDialog, selectAuth, selectCurrentUserId, sendMessage } from '../features/collide/collideSlice.js'

function ChatsPage() {
  const dispatch = useDispatch()
  const dialog = useSelector(selectActiveDialog)
  const currentUserId = useSelector(selectCurrentUserId)
  const auth = useSelector(selectAuth)
  const [message, setMessage] = useState('')

  useEffect(() => {
    if (dialog?.id) dispatch(fetchDialogMessages(dialog.id))
  }, [dispatch, dialog?.id])

  const submitMessage = async (event) => {
    event.preventDefault()
    if (!dialog || !message.trim()) return
    await dispatch(sendMessage({ dialogId: dialog.id, text: message })).unwrap().catch(() => null)
    setMessage('')
  }

  if (!auth.token) {
    return (
      <main className="auth-page">
        <section className="auth-card">
          <h1>Чаты</h1>
          <p>Для просмотра личных сообщений нужно войти или зарегистрироваться.</p>
          <Link className="nav-auth-link nav-auth-link--panel" to="/auth">Перейти ко входу</Link>
        </section>
      </main>
    )
  }

  return (
    <main className="chat-page">
      <div className="chat-layout">
        <ChatSidebar />
        <section className="chat-window">
          {!dialog && (
            <div className="chat-empty-state">
              <h1>Выберите чат для общения</h1>
              <p>Список переписок находится слева.</p>
            </div>
          )}

          {dialog && (
            <>
              <header className="chat-window__header">
                <Avatar tone={dialog.avatarTone} size="sm" label={dialog.name} />
                <div>
                  <h1>{dialog.name}</h1>
                  <p>онлайн недавно</p>
                </div>
              </header>

              <div className="messages-list">
                {dialog.messages.length === 0 && <p className="empty-note">История сообщений пока пустая.</p>}
                {dialog.messages.map((item) => (
                  <div key={item.id} className={`message ${item.from === currentUserId ? 'message--mine' : ''}`}>
                    <p>{item.text}</p>
                    <span>{item.time}</span>
                  </div>
                ))}
              </div>

              <form className="message-form" onSubmit={submitMessage}>
                <input value={message} onChange={(event) => setMessage(event.target.value)} placeholder="Введите сообщение" />
                <Button variant="primary" type="submit">Отправить</Button>
              </form>
            </>
          )}
        </section>
      </div>
    </main>
  )
}

export default ChatsPage
