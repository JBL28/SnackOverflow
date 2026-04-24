import { redirect } from 'next/navigation'
import { getAccessToken } from '@/lib/auth-cookie'
import { serverApi } from '@/lib/api/server'
import { getMyPostsAction, getMyCommentsAction } from '@/actions/users'
import type { ApiResponse, UserProfile } from '@/lib/types'
import NicknameForm from '@/components/profile/NicknameForm'
import PasswordForm from '@/components/profile/PasswordForm'
import WithdrawButton from '@/components/profile/WithdrawButton'
import Link from 'next/link'

interface PageProps {
  searchParams: Promise<{ tab?: string }>
}

export default async function ProfilePage({ searchParams }: PageProps) {
  const token = await getAccessToken()
  if (!token) redirect('/login')

  const res = await serverApi.get<ApiResponse<UserProfile>>('/api/users/me', {
    headers: { Authorization: `Bearer ${token}` },
  })
  const profile = res.data.data
  if (!profile) redirect('/login')

  const { tab = 'profile' } = await searchParams

  const INFO_ROWS = [
    { term: '아이디', desc: profile.username },
    { term: '이메일', desc: profile.email },
    { term: '역할', desc: profile.role === 'ADMIN' ? '관리자' : '일반 사용자' },
  ]

  const tabs = [
    { key: 'profile', label: '프로필 설정' },
    { key: 'posts', label: `내 게시글 (${profile.postCount})` },
    { key: 'comments', label: `내 댓글 (${profile.commentCount})` },
  ]

  return (
    <div className="mx-auto max-w-md flex flex-col gap-6">
      <div>
        <h1 className="mb-4 text-2xl font-bold text-zinc-900">마이페이지</h1>
        <nav className="flex gap-0 border-b border-zinc-200">
          {tabs.map((t) => (
            <Link
              key={t.key}
              href={`/profile?tab=${t.key}`}
              className={`px-4 py-2 text-sm font-medium transition-colors ${
                tab === t.key
                  ? 'border-b-2 border-zinc-900 text-zinc-900'
                  : 'text-zinc-400 hover:text-zinc-700'
              }`}
            >
              {t.label}
            </Link>
          ))}
        </nav>
      </div>

      {tab === 'profile' && (
        <>
          <section>
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
        </>
      )}

      {tab === 'posts' && <MyPostsTab />}
      {tab === 'comments' && <MyCommentsTab />}
    </div>
  )
}

async function MyPostsTab() {
  const posts = await getMyPostsAction()
  if (posts.length === 0) {
    return <p className="text-sm text-zinc-400">작성한 게시글이 없습니다.</p>
  }
  return (
    <ul className="flex flex-col gap-3">
      {posts.map((post) => (
        <li key={post.id} className="rounded-lg border border-zinc-200 bg-white p-4">
          <Link href={`/recommendations/${post.id}`} className="hover:underline">
            <p className="font-medium text-zinc-900">{post.name}</p>
          </Link>
          <p className="mt-1 line-clamp-2 text-sm text-zinc-500">{post.reason}</p>
          <p className="mt-1 text-xs text-zinc-400">
            👍 {post.likes} · 👎 {post.dislikes} ·{' '}
            {new Date(post.createdAt).toLocaleDateString('ko-KR')}
          </p>
        </li>
      ))}
    </ul>
  )
}

async function MyCommentsTab() {
  const comments = await getMyCommentsAction()
  if (comments.length === 0) {
    return <p className="text-sm text-zinc-400">작성한 댓글이 없습니다.</p>
  }
  return (
    <ul className="flex flex-col gap-3">
      {comments.map((comment) => (
        <li key={comment.id} className="rounded-lg border border-zinc-200 bg-white p-4">
          <p className="text-sm text-zinc-700 whitespace-pre-wrap">{comment.content}</p>
          <p className="mt-1 text-xs text-zinc-400">
            {comment.targetType === 'SNACK_PURCHASE' ? '구매한 과자' : '과자 추천'} ·{' '}
            {new Date(comment.createdAt).toLocaleDateString('ko-KR')}
          </p>
        </li>
      ))}
    </ul>
  )
}