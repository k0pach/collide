import { Navigate, Route, Routes } from 'react-router-dom'
import { useDispatch, useSelector } from 'react-redux'
import AddCollectionForm from './components/AddCollectionForm.jsx'
import AddItemForm from './components/AddItemForm.jsx'
import Header from './components/Header.jsx'
import Modal from './components/Modal.jsx'
import {
  closeAddCollectionModal,
  closeAddItemModal,
  selectAddCollectionModalOpen,
  selectAddItemModalOpen,
} from './features/collide/collideSlice.js'
import AuthPage from './pages/AuthPage.jsx'
import ChatsPage from './pages/ChatsPage.jsx'
import CollectionPage from './pages/CollectionPage.jsx'
import ForeignProfilePage from './pages/ForeignProfilePage.jsx'
import HomePage from './pages/HomePage.jsx'
import ItemPage from './pages/ItemPage.jsx'
import NotFoundPage from './pages/NotFoundPage.jsx'
import ProfilePage from './pages/ProfilePage.jsx'

function App() {
  const dispatch = useDispatch()
  const isAddItemModalOpen = useSelector(selectAddItemModalOpen)
  const isAddCollectionModalOpen = useSelector(selectAddCollectionModalOpen)

  return (
    <div className="app-shell">
      <Header />
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/profile" element={<Navigate to="/profile/collections" replace />} />
        <Route path="/profile/:tab" element={<ProfilePage />} />
        <Route path="/users/:id" element={<Navigate to="/users/:id/collections" replace />} />
        <Route path="/users/:id/:tab" element={<ForeignProfilePage />} />
        <Route path="/items/:itemId" element={<ItemPage />} />
        <Route path="/collections/:collectionId" element={<CollectionPage />} />
        <Route path="/chats" element={<ChatsPage />} />
        <Route path="/auth" element={<AuthPage />} />
        <Route path="*" element={<NotFoundPage />} />
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
