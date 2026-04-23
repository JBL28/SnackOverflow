import 'server-only'
import { cookies } from 'next/headers'

const ACCESS_COOKIE = 'snack_access'
const USER_COOKIE = 'snack_user'
const TTL = 1800

export async function getAccessToken(): Promise<string | undefined> {
  const store = await cookies()
  return store.get(ACCESS_COOKIE)?.value
}

export async function setTokens(
  accessToken: string,
  user: { username: string; nickname: string; role: string },
): Promise<void> {
  const store = await cookies()
  store.set(ACCESS_COOKIE, accessToken, {
    httpOnly: true,
    path: '/',
    maxAge: TTL,
    sameSite: 'lax',
  })
  store.set(USER_COOKIE, JSON.stringify(user), {
    httpOnly: false,
    path: '/',
    maxAge: TTL,
    sameSite: 'lax',
  })
}

export async function clearTokens(): Promise<void> {
  const store = await cookies()
  store.delete(ACCESS_COOKIE)
  store.delete(USER_COOKIE)
}

export async function getRefreshCookieHeader(): Promise<string | undefined> {
  const store = await cookies()
  const refresh = store.get('snack_refresh')?.value
  return refresh ? `snack_refresh=${refresh}` : undefined
}
