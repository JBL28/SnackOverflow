import Link from 'next/link'
import { cookies } from 'next/headers'
import { logoutAction } from '@/actions/auth'
import type { UserRole } from '@/lib/types'

interface UserCookie {
  nickname: string
  role: UserRole
}

export default async function Header() {
  const store = await cookies()
  const raw = store.get('snack_user')?.value
  const user: UserCookie | null = raw ? (JSON.parse(raw) as UserCookie) : null
  const isAuthenticated = !!store.get('snack_access')?.value

  return (
    <header className="sticky top-0 z-50 border-b border-zinc-200 bg-white/90 backdrop-blur-sm">
      <nav className="mx-auto flex max-w-4xl items-center justify-between px-4 py-3">
        <Link href="/" className="text-lg font-bold tracking-tight text-zinc-900">
          SnackOverflow
        </Link>
        <nav className="hidden sm:flex items-center gap-4 text-sm">
          <Link href="/" className="text-zinc-600 hover:text-zinc-900">구매한 과자</Link>
          <Link href="/recommendations" className="text-zinc-600 hover:text-zinc-900">과자 추천</Link>
        </nav>
        <div className="flex items-center gap-4 text-sm">
          {isAuthenticated && user ? (
            <>
              <Link href="/profile" className="font-medium text-zinc-700 hover:text-zinc-900">
                {user.nickname}
              </Link>
              {user.role === 'ADMIN' && (
                <Link href="/admin" className="font-medium text-orange-600 hover:text-orange-700">
                  관리자
                </Link>
              )}
              <form action={logoutAction}>
                <button type="submit" className="text-zinc-500 hover:text-zinc-700">
                  로그아웃
                </button>
              </form>
            </>
          ) : (
            <>
              <Link href="/login" className="text-zinc-700 hover:text-zinc-900">
                로그인
              </Link>
              <Link
                href="/signup"
                className="rounded-md bg-zinc-900 px-3 py-1.5 text-white hover:bg-zinc-700"
              >
                회원가입
              </Link>
            </>
          )}
        </div>
      </nav>
    </header>
  )
}
