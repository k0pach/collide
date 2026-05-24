import booksIcon from '../assets/icons/books_category_icon.svg'
import coinsIcon from '../assets/icons/coins_category_icon.svg'
import figuresIcon from '../assets/icons/figures_category_icon.svg'
import mineralsIcon from '../assets/icons/minerals_category_icon.svg'
import recordsIcon from '../assets/icons/records_category_icon.svg'
import retroTechIcon from '../assets/icons/retro_tech_category_icon.svg'
import tableGamesIcon from '../assets/icons/table_games_category_icon.svg'
import watchesIcon from '../assets/icons/watches_category_icon.svg'

export const categoryIcons = {
  figures: figuresIcon,
  coins: coinsIcon,
  vinyl: recordsIcon,
  books: booksIcon,
  watches: watchesIcon,
  retro: retroTechIcon,
  boardgames: tableGamesIcon,
  minerals: mineralsIcon,
}

export function getCategoryIcon(categoryId) {
  return categoryIcons[categoryId] || figuresIcon
}
