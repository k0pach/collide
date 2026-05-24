import searchIcon from '../assets/icons/search_icon.svg'

function SearchInput({value, onChange, placeholder = 'Поиск', className = ''}) {
    return (
        <label className={`search-pill ${className}`.trim()}>
            <img className="search-pill__icon" src={searchIcon} alt="" aria-hidden="true"/>
            <input
                type="search"
                value={value}
                placeholder={placeholder}
                onChange={(event) => onChange(event.target.value)}
            />
        </label>
    )
}

export default SearchInput
