'use client'
import { useState, useTransition } from 'react'
import type { Comment, TargetType } from '@/lib/types'
import { createCommentAction } from '@/actions/comments'
import CommentItem from './CommentItem'

interface Props {
  initialComments: Comment[]
  targetType: TargetType
  targetId: string
  currentUserId: string | null
  isAdmin: boolean
}

export default function CommentSection({
  initialComments,
  targetType,
  targetId,
  currentUserId,
  isAdmin,
}: Props) {
  const [comments, setComments] = useState(initialComments)
  const [newContent, setNewContent] = useState('')
  const [isPending, startTransition] = useTransition()

  function handleAdd(c: Comment) {
    setComments((prev) => [...prev, c])
  }

  function handleUpdate(c: Comment) {
    setComments((prev) => prev.map((x) => (x.id === c.id ? c : x)))
  }

  function handleDelete(id: string) {
    setComments((prev) => prev.filter((x) => x.id !== id))
  }

  function handleSubmit() {
    if (!newContent.trim()) return
    startTransition(async () => {
      const result = await createCommentAction(newContent, targetType, targetId, null)
      if (result.data) {
        setComments((prev) => [...prev, result.data!])
        setNewContent('')
      }
    })
  }

  const topLevel = comments.filter((c) => c.parentId === null)

  return (
    <section className="flex flex-col gap-4">
      <h2 className="text-base font-semibold text-zinc-800">댓글 {comments.length}개</h2>

      {topLevel.length === 0 && (
        <p className="text-sm text-zinc-400">
          아직 댓글이 없습니다. 첫 댓글을 남겨보세요!
        </p>
      )}

      <div className="flex flex-col gap-3">
        {topLevel.map((comment) => (
          <CommentItem
            key={comment.id}
            comment={comment}
            replies={comments.filter((c) => c.parentId === comment.id)}
            allComments={comments}
            currentUserId={currentUserId}
            isAdmin={isAdmin}
            targetType={targetType}
            targetId={targetId}
            onAdd={handleAdd}
            onUpdate={handleUpdate}
            onDelete={handleDelete}
          />
        ))}
      </div>

      {currentUserId ? (
        <div className="flex flex-col gap-2 border-t border-zinc-100 pt-4">
          <textarea
            value={newContent}
            onChange={(e) => setNewContent(e.target.value)}
            placeholder="댓글을 입력하세요..."
            rows={3}
            className="w-full resize-none rounded-lg border border-zinc-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-zinc-400"
          />
          <div className="flex justify-end">
            <button
              disabled={isPending || !newContent.trim()}
              onClick={handleSubmit}
              className="rounded-lg bg-zinc-900 px-4 py-2 text-sm text-white hover:bg-zinc-700 disabled:opacity-50"
            >
              댓글 등록
            </button>
          </div>
        </div>
      ) : (
        <p className="border-t border-zinc-100 pt-4 text-sm text-zinc-400">
          댓글을 작성하려면 로그인이 필요합니다.
        </p>
      )}
    </section>
  )
}