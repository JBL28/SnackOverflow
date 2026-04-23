'use client'
import { useTransition } from 'react'
import { withdrawAction } from '@/actions/users'

export default function WithdrawButton() {
  const [isPending, startTransition] = useTransition()

  const handleWithdraw = () => {
    if (!confirm('정말 탈퇴하시겠습니까? 이 작업은 되돌릴 수 없습니다.')) return
    startTransition(async () => {
      await withdrawAction()
    })
  }

  return (
    <button
      onClick={handleWithdraw}
      disabled={isPending}
      className="rounded-md border border-red-300 px-4 py-2 text-sm font-medium text-red-600 hover:bg-red-50 disabled:opacity-50"
    >
      {isPending ? '탈퇴 처리 중...' : '회원 탈퇴'}
    </button>
  )
}
