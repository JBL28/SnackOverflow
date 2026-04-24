import Link from 'next/link'
import type { SnackRecommendation } from '@/lib/types'

interface Props {
  rec: SnackRecommendation
}

export default function RecommendationCard({ rec }: Props) {
  return (
    <Link
      href={`/recommendations/${rec.id}`}
      className="block rounded-lg border border-zinc-200 bg-white p-4 hover:border-zinc-400 transition-colors"
    >
      <div className="flex items-start justify-between gap-3">
        <div className="flex flex-col gap-1 min-w-0">
          <h3 className="font-semibold text-zinc-900 truncate">{rec.name}</h3>
          <p className="text-sm text-zinc-500 line-clamp-2">{rec.reason}</p>
        </div>
        <div className="flex shrink-0 gap-3 text-sm text-zinc-400">
          <span>👍 {rec.likes}</span>
          <span>👎 {rec.dislikes}</span>
        </div>
      </div>
      <div className="mt-2 text-xs text-zinc-400">
        {rec.createdByNickname} · {new Date(rec.createdAt).toLocaleDateString('ko-KR')}
      </div>
    </Link>
  )
}
