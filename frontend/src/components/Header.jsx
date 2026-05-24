import {Link, NavLink} from 'react-router-dom'
import {useDispatch} from 'react-redux'
import Avatar from './Avatar.jsx'
import Button from './Button.jsx'
import {openAddItemModal} from '../features/collide/collideSlice.js'

function Header() {
    const dispatch = useDispatch()

    return (
        <header className="navbar">
            <Link className="brand" to="/">Collide</Link>

            <nav className="top-nav" aria-label="Основная навигация">
                <NavLink to="/" end>Главная</NavLink>
                <NavLink to="/profile/collections">Профиль</NavLink>
                <NavLink to="/chats">Чаты</NavLink>
            </nav>

            <div className="nav-actions">
                <Button variant="add" onClick={() => dispatch(openAddItemModal())}>
                    <span>Add</span>
                    <span aria-hidden="true">＋</span>
                </Button>
                <Link to="/profile/collections" aria-label="Открыть профиль">
                    <Avatar tone="cream" size="sm" label="JesseYo"/>
                </Link>
            </div>
        </header>
    )
}

export default Header
