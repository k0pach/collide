import {useDispatch, useSelector} from 'react-redux'
import {selectSearchQuery, setSearchQuery} from '../features/collide/collideSlice.js'

function SearchBar({placeholder = 'Найти предмет, коллекцию или статус'}) {
    const dispatch = useDispatch()
    const searchQuery = useSelector(selectSearchQuery)

    return (
        <label className="search-bar">
            <span>Поиск</span>
            <input
                value={searchQuery}
                onChange={(event) => dispatch(setSearchQuery(event.target.value))}
                placeholder={placeholder}
            />
        </label>
    )
}

export default SearchBar
