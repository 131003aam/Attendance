import { createContext, useContext, useState, useEffect, ReactNode } from 'react'
import type { UserInfo, UserRole } from '../types'

interface AuthContextType {
  user: UserInfo | null
  isAuthenticated: boolean
  isAdmin: boolean
  login: (userInfo: UserInfo) => void
  logout: () => void
}

const AuthContext = createContext<AuthContextType | undefined>(undefined)

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [user, setUser] = useState<UserInfo | null>(() => {
    try {
      const saved = localStorage.getItem('user')
      return saved ? JSON.parse(saved) : null
    } catch (error) {
      console.error('Failed to parse user from localStorage:', error)
      return null
    }
  })

  useEffect(() => {
    if (user) {
      localStorage.setItem('user', JSON.stringify(user))
    } else {
      localStorage.removeItem('user')
    }
  }, [user])

  const login = (userInfo: UserInfo) => {
    setUser(userInfo)
  }

  const logout = () => {
    setUser(null)
    localStorage.removeItem('user')
  }

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated: !!user,
        isAdmin: user?.role === 'ADMIN',
        login,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => {
  const context = useContext(AuthContext)
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}

