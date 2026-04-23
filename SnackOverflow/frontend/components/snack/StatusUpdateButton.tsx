'use client'
import { useTransition } from 'react'
import { updateSnackStatusAction } from '@/actions/snacks'
import type { SnackStatus } from '@/lib/types'

const NEXT_STATUS: Record<SnackStatus, { next: SnackStatus; label: string }> = {
  DELIVERING: { next: 'IN_STOCK', label: '재고 있음으로 변경' },
  IN_STOCK: { next: 'OUT_OF_STOCK', label: '재고 없음으로 변경' },
  OUT_OF_STOCK: { next: 'DELIVERING', label: '배송 중으로 변경' },
}

interface Props {
  id: string
  status: SnackStatus
}

export default function StatusUpdateButton({ id, status }: Props) {
  const [isPending, startTransition] = useTransition()
  const { next, label } = NEXT_STATUS[status]

  return (
    <button
      onClick={() =>
        startTransition(async () => {
          await updateSnackStatusAction(id, next)
        })
      }
      disabled={isPending}
      className="rounded-md border border-zinc-300 px-3 py-1 text-xs font-medium text-zinc-700 hover:bg-zinc-50 disabled:opacity-50"
    >
      {isPending ? '변경 중...' : label}
    </button>
  )
}
