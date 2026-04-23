import Link from 'next/link'
import type { SnackPurchase } from '@/lib/types'
import StatusBadge from './StatusBadge'
import StatusUpdateButton from './StatusUpdateButton'
import AdminSnackActions from '@/components/admin/AdminSnackActions'

interface Props {
  snack: SnackPurchase
  isAdmin?: boolean
  showStatusAction?: boolean
}

export default function SnackCard({ snack, isAdmin, showStatusAction }: Props) {
  const hasActions = showStatusAction || isAdmin

  return (
    <div className="flex flex-col gap-3 rounded-xl border border-zinc-200 bg-white p-5 shadow-sm transition-shadow hover:shadow-md">
      <div className="flex items-start justify-between gap-2">
        <Link
          href={`/snacks/${snack.id}`}
          className="font-semibold text-zinc-900 hover:underline"
        >
          {snack.name}
        </Link>
        <StatusBadge status={snack.status} />
      </div>
      <div className="flex items-center gap-3 text-xs text-zinc-500">
        <span>👍 {snack.likes}</span>
        <span>👎 {snack.dislikes}</span>
        <span>· {snack.createdByNickname}</span>
      </div>
      {hasActions && (
        <div className="flex flex-wrap gap-2 border-t border-zinc-100 pt-3">
          {showStatusAction && <StatusUpdateButton id={snack.id} status={snack.status} />}
          {isAdmin && <AdminSnackActions id={snack.id} currentName={snack.name} />}
        </div>
      )}
    </div>
  )
}
