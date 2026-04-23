'use client'
import { useState, useTransition } from 'react'
import { updateSnackNameAction, deleteSnackAction } from '@/actions/snacks'

interface Props {
  id: string
  currentName: string
}

export default function AdminSnackActions({ id, currentName }: Props) {
  const [isEditing, setIsEditing] = useState(false)
  const [name, setName] = useState(currentName)
  const [error, setError] = useState<string>()
  const [isPending, startTransition] = useTransition()

  const handleSave = () => {
    setError(undefined)
    startTransition(async () => {
      const result = await updateSnackNameAction(id, name)
      if (result.error) setError(result.error)
      else setIsEditing(false)
    })
  }

  const handleDelete = () => {
    if (!confirm('정말 삭제하시겠습니까?')) return
    startTransition(async () => {
      await deleteSnackAction(id)
    })
  }

  if (isEditing) {
    return (
      <div className="flex flex-col gap-2">
        <input
          value={name}
          onChange={(e) => setName(e.target.value)}
          className="rounded-md border border-zinc-300 px-2 py-1 text-sm focus:border-zinc-500 focus:outline-none"
        />
        {error && <p className="text-xs text-red-500">{error}</p>}
        <div className="flex gap-2">
          <button
            onClick={handleSave}
            disabled={isPending}
            className="rounded-md bg-zinc-900 px-3 py-1 text-xs text-white hover:bg-zinc-700 disabled:opacity-50"
          >
            저장
          </button>
          <button
            onClick={() => { setIsEditing(false); setName(currentName) }}
            className="rounded-md border border-zinc-300 px-3 py-1 text-xs hover:bg-zinc-50"
          >
            취소
          </button>
        </div>
      </div>
    )
  }

  return (
    <div className="flex gap-2">
      <button
        onClick={() => setIsEditing(true)}
        className="rounded-md border border-zinc-300 px-3 py-1 text-xs hover:bg-zinc-50"
      >
        이름 수정
      </button>
      <button
        onClick={handleDelete}
        disabled={isPending}
        className="rounded-md border border-red-300 px-3 py-1 text-xs text-red-600 hover:bg-red-50 disabled:opacity-50"
      >
        삭제
      </button>
    </div>
  )
}
