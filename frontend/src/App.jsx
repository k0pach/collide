import { useEffect } from 'react'
import { Navigate, Route, Routes, useLocation } from 'react-router-dom'
import { useDispatch, useSelector } from 'react-redux'
import AddCollectionForm from './components/AddCollectionForm.jsx'
import AddItemForm from './components/AddItemForm.jsx'
import Header from './components/Header.jsx'
import Modal from './components/Modal.jsx'
import {
  closeAddCollectionModal,
  closeAddItemModal,
  loadInitialData,
  selectAddCollectionModalOpen,
  selectAddItemModalOpen,
  selectAuth,
  selectCurrentUser
} from './features/collide/collideSlice.js'
import AuthPage from './pages/AuthPage.jsx'
import ChatsPage from './pages/ChatsPage.jsx'
import CollectionPage from './pages/CollectionPage.jsx'
import ForeignProfilePage from './pages/ForeignProfilePage.jsx'
import HomePage from './pages/HomePage.jsx'
import ItemPage from './pages/ItemPage.jsx'
import NotFoundPage from './pages/NotFoundPage.jsx'
import ProfilePage from './pages/ProfilePage.jsx'

function ProtectedRoute({ isAuthenticated, children }) {
  if (!isAuthenticated) {
    return <Navigate to="/auth" replace />
  }
  return children
}

function App() {
  const dispatch = useDispatch()
  const location = useLocation()

  const isAddItemModalOpen = useSelector(selectAddItemModalOpen)
  const isAddCollectionModalOpen = useSelector(selectAddCollectionModalOpen)

  // Надежная проверка авторизации (как в компоненте Header)
  const auth = useSelector(selectAuth)
  const user = useSelector(selectCurrentUser)
  const isAuthenticated = Boolean(auth.token && user)

  useEffect(() => {
    dispatch(loadInitialData())
  }, [dispatch])

  // Скрываем Header на странице авторизации
  const showHeader = location.pathname !== '/auth'

  return (
      <div className="app-shell">
        {showHeader && <Header />}

        <Routes>
          {/* Роут авторизации. Если авторизован - перекидываем на главную */}
          <Route path="/auth" element={!isAuthenticated ? <AuthPage /> : <Navigate to="/" replace />} />

          {/* Защищенные роуты */}
          <Route path="/" element={<ProtectedRoute isAuthenticated={isAuthenticated}><HomePage /></ProtectedRoute>} />
          <Route path="/profile" element={<ProtectedRoute isAuthenticated={isAuthenticated}><Navigate to="/profile/collections" replace /></ProtectedRoute>} />
          <Route path="/profile/:tab" element={<ProtectedRoute isAuthenticated={isAuthenticated}><ProfilePage /></ProtectedRoute>} />
          <Route path="/users/:id" element={<ProtectedRoute isAuthenticated={isAuthenticated}><Navigate to="/users/:id/collections" replace /></ProtectedRoute>} />
          <Route path="/users/:id/:tab" element={<ProtectedRoute isAuthenticated={isAuthenticated}><ForeignProfilePage /></ProtectedRoute>} />
          <Route path="/items/:itemId" element={<ProtectedRoute isAuthenticated={isAuthenticated}><ItemPage /></ProtectedRoute>} />
          <Route path="/collections/:collectionId" element={<ProtectedRoute isAuthenticated={isAuthenticated}><CollectionPage /></ProtectedRoute>} />
          <Route path="/chats" element={<ProtectedRoute isAuthenticated={isAuthenticated}><ChatsPage /></ProtectedRoute>} />
          <Route path="*" element={<ProtectedRoute isAuthenticated={isAuthenticated}><NotFoundPage /></ProtectedRoute>} />
        </Routes>

        {isAddItemModalOpen && (
            <Modal title="Новый предмет" onClose={() => dispatch(closeAddItemModal())}>
              <AddItemForm />
            </Modal>
        )}

        {isAddCollectionModalOpen && (
            <Modal title="Новая коллекция" onClose={() => dispatch(closeAddCollectionModal())}>
              <AddCollectionForm />
            </Modal>
        )}
      </div>
  )
}

export default App