import { configureStore } from '@reduxjs/toolkit'
import collideReducer from '../features/collide/collideSlice.js'

export const store = configureStore({
  reducer: {
    collide: collideReducer,
  },
})
