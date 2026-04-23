'use client'
import { useEffect } from 'react'
import { useAuthStore } from '@/store/auth'
import type { UserRole } from '@/lib/types'

interface InitialUser {
  username: string
  nickname: string
  role: UserRole
}

interface ProvidersProps {
  initialUser: InitialUser | null
  children: React.ReactNode
}

export default function Providers({ initialUser, children }: ProvidersProps) {
  const setUser = useAuthStore((s) => s.setUser)
  const clearUser = useAuthStore((s) => s.clearUser)

  useEffect(() => {
    if (initialUser) setUser(initialUser)
    else clearUser()
  }, [initialUser, setUser, clearUser])

  return <>{children}</>
}
