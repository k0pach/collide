import {NavLink} from 'react-router-dom'
import {useDispatch, useSelector} from 'react-redux'
import {
    selectProfileSearchQuery,
    setProfileSearchQuery,
} from '../features/collide/collideSlice.js'
import SearchInput from './SearchInput.jsx'

function ProfileTabs({basePath = '/profile', showFavorites = true}) {
    const dispatch = useDispatch()
    const searchQuery = useSelector(selectProfileSearchQuery)

    return (
        <div className="profile-tabs-bar">
            <nav className="profile-tabs" aria-label="Разделы профиля">
                <NavLink to={`${basePath}/collections`}>Коллекции</NavLink>
                <NavLink to={`${basePath}/items`}>Предметы</NavLink>
                {showFavorites && <NavLink to={`${basePath}/favorites`}>Избранное</NavLink>}
                <NavLink to={`${basePath}/stats`}>Статистика</NavLink>
            </nav>
            <SearchInput
                value={searchQuery}
                onChange={(value) => dispatch(setProfileSearchQuery(value))}
                placeholder="Поиск по коллекциям и предметам"
                className="search-pill--profile"
            />
        </div>
    )
}

export default ProfileTabs
