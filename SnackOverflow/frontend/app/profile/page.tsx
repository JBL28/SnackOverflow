import { redirect } from 'next/navigation'
import { getAccessToken } from '@/lib/auth-cookie'
import { serverApi } from '@/lib/api/server'
import type { ApiResponse, UserProfile } from '@/lib/types'
import NicknameForm from '@/components/profile/NicknameForm'
import PasswordForm from '@/components/profile/PasswordForm'
import WithdrawButton from '@/components/profile/WithdrawButton'

export default async function ProfilePage() {
  const token = await getAccessToken()
  if (!token) redirect('/login')

  const res = await serverApi.get<ApiResponse<UserProfile>>('/api/users/me', {
    headers: { Authorization: `Bearer ${token}` },
  })
  const profile = res.data.data
  if (!profile) redirect('/login')

  const INFO_ROWS = [
    { term: '아이디', desc: profile.username },
    { term: '이메일', desc: profile.email },
    { term: '역할', desc: profile.role === 'ADMIN' ? '관리자' : '일반 사용자' },
  ]

  return (
    <div className="mx-auto max-w-md flex flex-col gap-8">
      <section>
        <h1 className="mb-3 text-2xl font-bold text-zinc-900">내 프로필</h1>
        <div className="rounded-xl border border-zinc-200 bg-white p-5">
          <dl className="flex flex-col gap-3 text-sm">
            {INFO_ROWS.map(({ term, desc }) => (
              <div key={term} className="flex gap-4">
                <dt className="w-20 shrink-0 text-zinc-500">{term}</dt>
                <dd className="font-medium text-zinc-900">{desc}</dd>
              </div>
            ))}
          </dl>
        </div>
      </section>

      <section>
        <h2 className="mb-3 text-base font-semibold text-zinc-800">닉네임 변경</h2>
        <NicknameForm currentNickname={profile.nickname} />
      </section>

      <section>
        <h2 className="mb-3 text-base font-semibold text-zinc-800">비밀번호 변경</h2>
        <PasswordForm />
      </section>

      <section>
        <h2 className="mb-3 text-base font-semibold text-red-600">회원 탈퇴</h2>
        <WithdrawButton />
      </section>
    </div>
  )
}
