'use client'
import { useState, useTransition } from 'react'
import { useRouter } from 'next/navigation'
import { updateRecommendationAction, deleteRecommendationAction } from '@/actions/recommendations'

interface Props {
  id: string
  initialName: string
  initialReason: string
}

export default function RecommendationActions({ id, initialName, initialReason }: Props) {
  const [editing, setEditing] = useState(false)
  const [name, setName] = useState(initialName)
  const [reason, setReason] = useState(initialReason)
  const [error, setError] = useState<string | null>(null)
  const [isPending, startTransition] = useTransition()
  const router = useRouter()

  function handleUpdate(e: React.FormEvent) {
    e.preventDefault()
    startTransition(async () => {
      const result = await updateRecommendationAction(id, name, reason)
      if (result.error) { setError(result.error); return }
      setEditing(false)
      router.refresh()
    })
  }

  function handleDelete() {
    if (!confirm('삭제하시겠습니까?')) return
    startTransition(async () => {
      const result = await deleteRecommendationAction(id)
      if (result.error) { setError(result.error); return }
      router.push('/recommendations')
    })
  }

  if (editing) {
    return (
      <form onSubmit={handleUpdate} className="flex flex-col gap-3 border border-zinc-200 rounded-lg p-4">
        {error && <p className="text-sm text-red-600">{error}</p>}
        <input value={name} onChange={(e) => setName(e.target.value)} required maxLength={100}
          className="rounded-md border border-zinc-300 px-3 py-2 text-sm" />
        <textarea value={reason} onChange={(e) => setReason(e.target.value)} required rows={4}
          className="rounded-md border border-zinc-300 px-3 py-2 text-sm resize-none" />
        <div className="flex gap-2">
          <button type="submit" disabled={isPending}
            className="rounded-md bg-zinc-900 px-3 py-1.5 text-sm text-white hover:bg-zinc-700 disabled:opacity-50">
            저장
          </button>
          <button type="button" onClick={() => setEditing(false)}
            className="text-sm text-zinc-400 hover:text-zinc-600">취소</button>
        </div>
      </form>
    )
  }

  return (
    <div className="flex gap-2">
      <button onClick={() => setEditing(true)}
        className="rounded-md border border-zinc-300 px-3 py-1.5 text-sm hover:bg-zinc-50">수정</button>
      <button onClick={handleDelete} disabled={isPending}
        className="rounded-md border border-red-200 px-3 py-1.5 text-sm text-red-600 hover:bg-red-50 disabled:opacity-50">삭제</button>
    </div>
  )
}