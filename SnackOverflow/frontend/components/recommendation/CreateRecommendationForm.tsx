'use client'
import { useState, useTransition } from 'react'
import { useRouter } from 'next/navigation'
import { createRecommendationAction } from '@/actions/recommendations'

export default function CreateRecommendationForm() {
  const [name, setName] = useState('')
  const [reason, setReason] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [isPending, startTransition] = useTransition()
  const router = useRouter()

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setError(null)
    startTransition(async () => {
      const result = await createRecommendationAction(name, reason)
      if (result.error) {
        setError(result.error)
        return
      }
      setName('')
      setReason('')
      router.refresh()
    })
  }

  return (
    <form onSubmit={handleSubmit} className="flex flex-col gap-3 rounded-lg border border-zinc-200 bg-white p-4">
      <h2 className="font-semibold text-zinc-900">과자 추천하기</h2>
      {error && <p className="text-sm text-red-600">{error}</p>}
      <input
        type="text"
        placeholder="과자 이름"
        value={name}
        onChange={(e) => setName(e.target.value)}
        required
        maxLength={100}
        className="rounded-md border border-zinc-300 px-3 py-2 text-sm"
      />
      <textarea
        placeholder="추천 이유를 적어주세요"
        value={reason}
        onChange={(e) => setReason(e.target.value)}
        required
        maxLength={1000}
        rows={3}
        className="rounded-md border border-zinc-300 px-3 py-2 text-sm resize-none"
      />
      <button
        type="submit"
        disabled={isPending}
        className="rounded-md bg-zinc-900 px-4 py-2 text-sm font-medium text-white hover:bg-zinc-700 disabled:opacity-50"
      >
        {isPending ? '등록 중...' : '추천 등록'}
      </button>
    </form>
  )
}
