import { Link } from 'react-router-dom'

function NotFoundPage() {
  return (
    <main className="not-found-page">
      <h1>Страница не найдена</h1>
      <p>Такого раздела пока нет в MVP.</p>
      <Link to="/">Вернуться на главную</Link>
    </main>
  )
}

export default NotFoundPage
