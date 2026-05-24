import booksIcon from '../assets/icons/books_category_icon.svg'
import coinsIcon from '../assets/icons/coins_category_icon.svg'
import figuresIcon from '../assets/icons/figures_category_icon.svg'
import mineralsIcon from '../assets/icons/minerals_category_icon.svg'
import recordsIcon from '../assets/icons/records_category_icon.svg'
import retroTechIcon from '../assets/icons/retro_tech_category_icon.svg'
import tableGamesIcon from '../assets/icons/table_games_category_icon.svg'
import watchesIcon from '../assets/icons/watches_category_icon.svg'

const categoryIcons = {
    books: booksIcon,
    coins: coinsIcon,
    figures: figuresIcon,
    minerals: mineralsIcon,
    vinyl: recordsIcon,
    records: recordsIcon,
    retro: retroTechIcon,
    retroTech: retroTechIcon,
    boardgames: tableGamesIcon,
    tableGames: tableGamesIcon,
    watches: watchesIcon,
}

function CategoryIcon({categoryId, title = '', className = ''}) {
    const icon = categoryIcons[categoryId]

    if (!icon) {
        return <span className={`category-icon category-icon--fallback ${className}`.trim()}>⌘</span>
    }

    return <img className={`category-icon ${className}`.trim()} src={icon} alt={title}/>
}

export default CategoryIcon
