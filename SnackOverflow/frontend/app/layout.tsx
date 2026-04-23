import type { Metadata } from 'next'
import { Geist, Geist_Mono } from 'next/font/google'
import { cookies } from 'next/headers'
import './globals.css'
import Providers from '@/components/providers'
import Header from '@/components/layout/Header'
import type { UserRole } from '@/lib/types'

const geistSans = Geist({ subsets: ['latin'], variable: '--font-geist-sans' })
const geistMono = Geist_Mono({ subsets: ['latin'], variable: '--font-geist-mono' })

export const metadata: Metadata = {
  title: 'SnackOverflow',
  description: 'SSAFY 12반 간식 선호도 조사 플랫폼',
}

interface UserCookie {
  username: string
  nickname: string
  role: UserRole
}

export default async function RootLayout({ children }: { children: React.ReactNode }) {
  const store = await cookies()
  const raw = store.get('snack_user')?.value
  const initialUser: UserCookie | null = raw ? (JSON.parse(raw) as UserCookie) : null

  return (
    <html lang="ko" className={`${geistSans.variable} ${geistMono.variable}`}>
      <body className="min-h-screen bg-zinc-50 antialiased">
        <Providers initialUser={initialUser}>
          <Header />
          <div className="mx-auto max-w-4xl px-4 py-8">{children}</div>
        </Providers>
      </body>
    </html>
  )
}
