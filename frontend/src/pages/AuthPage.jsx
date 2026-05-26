import { useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useDispatch, useSelector } from 'react-redux'
import Button from '../components/Button.jsx'
import { loginUser, registerUser, selectAuth } from '../features/collide/collideSlice.js'

const emptyLoginForm = {
  login: '',
  password: '',
}

const emptyRegisterForm = {
  email: '',
  username: '',
  displayName: '',
  password: '',
}

function AuthPage() {
  const dispatch = useDispatch()
  const navigate = useNavigate()
  const auth = useSelector(selectAuth)
  const [mode, setMode] = useState('login')
  const [loginForm, setLoginForm] = useState(emptyLoginForm)
  const [registerForm, setRegisterForm] = useState(emptyRegisterForm)
  const [error, setError] = useState('')

  const title = useMemo(() => (mode === 'login' ? 'Вход' : 'Регистрация'), [mode])
  const activeForm = mode === 'login' ? loginForm : registerForm

  const updateLoginField = (field, value) => setLoginForm((prev) => ({ ...prev, [field]: value }))
  const updateRegisterField = (field, value) => setRegisterForm((prev) => ({ ...prev, [field]: value }))

  const switchMode = () => {
    setError('')
    setMode((currentMode) => (currentMode === 'login' ? 'register' : 'login'))
  }

  const submit = async (event) => {
    event.preventDefault()
    setError('')

    try {
      if (mode === 'login') {
        await dispatch(loginUser({ login: loginForm.login, password: loginForm.password })).unwrap()
      } else {
        await dispatch(registerUser({
          email: registerForm.email,
          username: registerForm.username,
          password: registerForm.password,
          displayName: registerForm.displayName,
        })).unwrap()
      }
      navigate('/profile/collections')
    } catch (requestError) {
      setError(String(requestError || 'Не удалось выполнить запрос'))
    }
  }

  return (
      <main className="auth-page">
        <section className="auth-card">
          <h1>{title}</h1>

          <form className="auth-form" onSubmit={submit} autoComplete={mode === 'login' ? 'on' : 'off'}>
            {mode === 'login' ? (
                <label>
                  <span>Email или username</span>
                  <input
                      value={activeForm.login}
                      onChange={(event) => updateLoginField('login', event.target.value)}
                      autoComplete="username"
                      required
                  />
                </label>
            ) : (
                <>
                  <label>
                    <span>Email</span>
                    <input
                        type="email"
                        value={activeForm.email}
                        onChange={(event) => updateRegisterField('email', event.target.value)}
                        autoComplete="email"
                        required
                    />
                  </label>
                  <label>
                    <span>Username</span>
                    <input
                        value={activeForm.username}
                        onChange={(event) => updateRegisterField('username', event.target.value)}
                        autoComplete="username"
                        required
                        minLength={3}
                    />
                  </label>
                  <label>
                    <span>Отображаемое имя</span>
                    <input
                        value={activeForm.displayName}
                        onChange={(event) => updateRegisterField('displayName', event.target.value)}
                        autoComplete="name"
                        required
                    />
                  </label>
                </>
            )}

            <label>
              <span>Пароль</span>
              <input
                  type="password"
                  value={activeForm.password}
                  onChange={(event) => {
                    if (mode === 'login') updateLoginField('password', event.target.value)
                    else updateRegisterField('password', event.target.value)
                  }}
                  autoComplete={mode === 'login' ? 'current-password' : 'new-password'}
                  required
                  minLength={6}
              />
            </label>

            {(error || auth.error) && <p className="form-error">{error || auth.error}</p>}

            <Button variant="primary" type="submit" disabled={auth.status === 'loading'}>
              {auth.status === 'loading' ? 'Подождите...' : mode === 'login' ? 'Войти' : 'Зарегистрироваться'}
            </Button>
          </form>

          <button className="text-button" type="button" onClick={switchMode}>
            {mode === 'login' ? 'Создать аккаунт' : 'Уже есть аккаунт'}
          </button>
        </section>
      </main>
  )
}

export default AuthPage