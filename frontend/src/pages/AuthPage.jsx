import { Link } from 'react-router-dom'
import Button from '../components/Button.jsx'

function AuthPage() {
  return (
    <main className="auth-page">
      <section className="auth-card">
        <h1>Вход в Collide</h1>
        <p>Страница авторизации пока работает как frontend-заглушка под будущий Java backend.</p>
        <label>
          <span>Email</span>
          <input type="email" placeholder="user@example.com" />
        </label>
        <label>
          <span>Пароль</span>
          <input type="password" placeholder="••••••••" />
        </label>
        <Button variant="primary">Войти</Button>
        <Link to="/profile/collections">Продолжить как демо-пользователь</Link>
      </section>
    </main>
  )
}

export default AuthPage
