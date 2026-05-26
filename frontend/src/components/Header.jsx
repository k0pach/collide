import { Link, NavLink, useNavigate } from 'react-router-dom'
import { useDispatch, useSelector } from 'react-redux'
import Avatar from './Avatar.jsx'
import Button from './Button.jsx'
import { logoutUser, openAddItemModal, selectCurrentUser, selectAuth } from '../features/collide/collideSlice.js'

function Header() {
  const dispatch = useDispatch()
  const navigate = useNavigate()
  const user = useSelector(selectCurrentUser)
  const auth = useSelector(selectAuth)
  const isAuthenticated = Boolean(auth.token && user)

  const logout = () => {
    dispatch(logoutUser())
    navigate('/auth')
  }

  return (
    <header className="navbar">
      <Link className="brand" to="/">Collide</Link>

      <nav className="top-nav" aria-label="Основная навигация">
        <NavLink to="/" end>Главная</NavLink>
        <NavLink to="/profile/collections">Профиль</NavLink>
        <NavLink to="/chats">Чаты</NavLink>
      </nav>

      <div className="nav-actions">
        {isAuthenticated && (
          <Button variant="add" onClick={() => dispatch(openAddItemModal())}>
            <span>Add</span>
            <span aria-hidden="true">＋</span>
          </Button>
        )}

        {isAuthenticated ? (
          <>
            <Button variant="ghost" onClick={logout}>Выйти</Button>
            <Link to="/profile/collections" aria-label="Открыть профиль">
              <Avatar tone={user.avatarTone} size="sm" label={user.name} />
            </Link>
          </>
        ) : (
          <Link className="nav-auth-link" to="/auth">Войти</Link>
        )}
      </div>
    </header>
  )
}

export default Header
