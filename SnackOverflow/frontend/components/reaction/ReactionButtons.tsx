'use client'
import { useState, useTransition, useRef } from 'react'
import type { ReactionTargetType, ReactionType } from '@/lib/types'
import { toggleReactionAction, getVotersAction } from '@/actions/reactions'

interface Props {
  targetType: ReactionTargetType
  targetId: string
  initialLikes: number
  initialDislikes: number
  initialMyReaction: ReactionType | null
  isAuthenticated: boolean
}

export default function ReactionButtons({
  targetType,
  targetId,
  initialLikes,
  initialDislikes,
  initialMyReaction,
  isAuthenticated,
}: Props) {
  const [likes, setLikes] = useState(initialLikes)
  const [dislikes, setDislikes] = useState(initialDislikes)
  const [myReaction, setMyReaction] = useState<ReactionType | null>(initialMyReaction)
  const [isPending, startTransition] = useTransition()

  const [hoverVoters, setHoverVoters] = useState<string[] | null>(null)
  const [hoverType, setHoverType] = useState<ReactionType | null>(null)
  const hoverTimer = useRef<ReturnType<typeof setTimeout> | null>(null)

  function handleReact(type: ReactionType) {
    if (!isAuthenticated) return
    startTransition(async () => {
      const result = await toggleReactionAction(targetType, targetId, type)
      if (result.data) {
        setLikes(result.data.likes)
        setDislikes(result.data.dislikes)
        setMyReaction(result.data.myReaction)
      }
    })
  }

  function handleMouseEnter(type: ReactionType) {
    hoverTimer.current = setTimeout(async () => {
      const voters = await getVotersAction(targetType, targetId, type)
      setHoverVoters(voters)
      setHoverType(type)
    }, 1000)
  }

  function handleMouseLeave() {
    if (hoverTimer.current) clearTimeout(hoverTimer.current)
    setHoverVoters(null)
    setHoverType(null)
  }

  return (
    <div className="flex items-center gap-3">
      <div className="relative">
        <button
          disabled={isPending || !isAuthenticated}
          onClick={() => handleReact('LIKE')}
          onMouseEnter={() => handleMouseEnter('LIKE')}
          onMouseLeave={handleMouseLeave}
          className={`flex items-center gap-1 rounded px-2 py-1 text-sm transition-colors disabled:cursor-not-allowed disabled:opacity-50 ${
            myReaction === 'LIKE'
              ? 'bg-blue-100 text-blue-600 font-medium'
              : 'text-zinc-500 hover:bg-zinc-100'
          }`}
        >
          👍 {likes}
        </button>
        {hoverType === 'LIKE' && hoverVoters !== null && (
          <div className="absolute bottom-full left-0 mb-1 z-10 min-w-max rounded border border-zinc-200 bg-white px-2 py-1 text-xs shadow-md">
            {hoverVoters.length === 0 ? (
              <span className="text-zinc-400">아직 없음</span>
            ) : (
              hoverVoters.map((n) => <div key={n} className="text-zinc-700">{n}</div>)
            )}
          </div>
        )}
      </div>

      <div className="relative">
        <button
          disabled={isPending || !isAuthenticated}
          onClick={() => handleReact('DISLIKE')}
          onMouseEnter={() => handleMouseEnter('DISLIKE')}
          onMouseLeave={handleMouseLeave}
          className={`flex items-center gap-1 rounded px-2 py-1 text-sm transition-colors disabled:cursor-not-allowed disabled:opacity-50 ${
            myReaction === 'DISLIKE'
              ? 'bg-red-100 text-red-600 font-medium'
              : 'text-zinc-500 hover:bg-zinc-100'
          }`}
        >
          👎 {dislikes}
        </button>
        {hoverType === 'DISLIKE' && hoverVoters !== null && (
          <div className="absolute bottom-full left-0 mb-1 z-10 min-w-max rounded border border-zinc-200 bg-white px-2 py-1 text-xs shadow-md">
            {hoverVoters.length === 0 ? (
              <span className="text-zinc-400">아직 없음</span>
            ) : (
              hoverVoters.map((n) => <div key={n} className="text-zinc-700">{n}</div>)
            )}
          </div>
        )}
      </div>

      {!isAuthenticated && (
        <span className="text-xs text-zinc-400">로그인 후 반응 가능</span>
      )}
    </div>
  )
}