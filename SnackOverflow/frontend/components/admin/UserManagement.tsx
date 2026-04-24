'use client'
import { useState, useTransition } from 'react'
import type { AdminUser, UserStatus } from '@/lib/types'
import {
  adminChangeUserStatusAction,
  adminResetPasswordAction,
} from '@/actions/users'

const STATUS_LABELS: Record<UserStatus, string> = {
  ACTIVE: '활성',
  INACTIVE: '비활성',
  DELETED: '삭제됨',
}

const STATUS_COLORS: Record<UserStatus, string> = {
  ACTIVE: 'bg-green-100 text-green-700',
  INACTIVE: 'bg-yellow-100 text-yellow-700',
  DELETED: 'bg-red-100 text-red-700',
}

interface Props {
  initialUsers: AdminUser[]
  totalPages: number
}

export default function UserManagement({ initialUsers, totalPages }: Props) {
  const [users, setUsers] = useState(initialUsers)
  const [message, setMessage] = useState<{ text: string; ok: boolean } | null>(null)
  const [resetTarget, setResetTarget] = useState<string | null>(null)
  const [newPassword, setNewPassword] = useState('')
  const [isPending, startTransition] = useTransition()

  function notify(text: string, ok: boolean) {
    setMessage({ text, ok })
    setTimeout(() => setMessage(null), 3000)
  }

  function handleStatusChange(userId: string, status: UserStatus) {
    startTransition(async () => {
      const result = await adminChangeUserStatusAction(userId, status)
      if (result.error) {
        notify(result.error, false)
        return
      }
      setUsers((prev) =>
        prev.map((u) => (u.id === userId ? { ...u, status } : u)),
      )
      notify('상태가 변경되었습니다.', true)
    })
  }

  function handleResetPassword(userId: string) {
    if (!newPassword || newPassword.length < 6) {
      notify('비밀번호는 6자 이상이어야 합니다.', false)
      return
    }
    startTransition(async () => {
      const result = await adminResetPasswordAction(userId, newPassword)
      if (result.error) {
        notify(result.error, false)
        return
      }
      setResetTarget(null)
      setNewPassword('')
      notify('비밀번호가 초기화되었습니다.', true)
    })
  }

  return (
    <div className="flex flex-col gap-3">
      {message && (
        <div
          className={`rounded-md px-4 py-2 text-sm ${
            message.ok ? 'bg-green-50 text-green-700' : 'bg-red-50 text-red-700'
          }`}
        >
          {message.text}
        </div>
      )}

      {users.length === 0 && (
        <p className="text-sm text-zinc-500">등록된 유저가 없습니다.</p>
      )}

      {users.map((user) => (
        <div
          key={user.id}
          className="rounded-lg border border-zinc-200 bg-white p-4"
        >
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div className="flex flex-col gap-0.5">
              <div className="flex items-center gap-2">
                <span className="font-medium text-zinc-900">{user.nickname}</span>
                <span className="text-xs text-zinc-400">@{user.username}</span>
                {user.role === 'ADMIN' && (
                  <span className="rounded bg-orange-100 px-1.5 py-0.5 text-xs font-medium text-orange-600">
                    관리자
                  </span>
                )}
                <span
                  className={`rounded px-1.5 py-0.5 text-xs font-medium ${STATUS_COLORS[user.status]}`}
                >
                  {STATUS_LABELS[user.status]}
                </span>
              </div>
              <span className="text-xs text-zinc-400">
                게시글 {user.postCount} · 댓글 {user.commentCount}
              </span>
            </div>

            <div className="flex items-center gap-2">
              <select
                disabled={isPending || user.role === 'ADMIN'}
                value={user.status}
                onChange={(e) =>
                  handleStatusChange(user.id, e.target.value as UserStatus)
                }
                className="rounded-md border border-zinc-300 px-2 py-1 text-sm disabled:opacity-50"
              >
                <option value="ACTIVE">활성</option>
                <option value="INACTIVE">비활성</option>
                <option value="DELETED">삭제됨</option>
              </select>

              <button
                disabled={isPending}
                onClick={() =>
                  setResetTarget(resetTarget === user.id ? null : user.id)
                }
                className="rounded-md border border-zinc-300 px-2 py-1 text-sm hover:bg-zinc-50 disabled:opacity-50"
              >
                PW 초기화
              </button>
            </div>
          </div>

          {resetTarget === user.id && (
            <div className="mt-3 flex items-center gap-2">
              <input
                type="password"
                placeholder="새 비밀번호 (6자 이상)"
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
                className="flex-1 rounded-md border border-zinc-300 px-3 py-1.5 text-sm"
              />
              <button
                disabled={isPending}
                onClick={() => handleResetPassword(user.id)}
                className="rounded-md bg-zinc-900 px-3 py-1.5 text-sm text-white hover:bg-zinc-700 disabled:opacity-50"
              >
                확인
              </button>
              <button
                onClick={() => {
                  setResetTarget(null)
                  setNewPassword('')
                }}
                className="text-sm text-zinc-400 hover:text-zinc-600"
              >
                취소
              </button>
            </div>
          )}
        </div>
      ))}

      <p className="text-xs text-zinc-400">총 {totalPages}페이지 (현재 1페이지)</p>
    </div>
  )
}
