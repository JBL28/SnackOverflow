'use client'
import { useState, useTransition } from 'react'
import type { Comment, TargetType } from '@/lib/types'
import {
  createCommentAction,
  updateCommentAction,
  deleteCommentAction,
} from '@/actions/comments'

interface Props {
  comment: Comment
  replies: Comment[]
  allComments: Comment[]
  currentUserId: string | null
  isAdmin: boolean
  targetType: TargetType
  targetId: string
  onAdd: (c: Comment) => void
  onUpdate: (c: Comment) => void
  onDelete: (id: string) => void
}

export default function CommentItem({
  comment,
  replies,
  allComments,
  currentUserId,
  isAdmin,
  targetType,
  targetId,
  onAdd,
  onUpdate,
  onDelete,
}: Props) {
  const [isEditing, setIsEditing] = useState(false)
  const [editContent, setEditContent] = useState(comment.content)
  const [isReplying, setIsReplying] = useState(false)
  const [replyContent, setReplyContent] = useState('')
  const [isPending, startTransition] = useTransition()

  const canModify = currentUserId && (currentUserId === comment.authorId || isAdmin)

  function handleEdit() {
    if (!editContent.trim()) return
    startTransition(async () => {
      const result = await updateCommentAction(comment.id, editContent)
      if (result.data) {
        onUpdate(result.data)
        setIsEditing(false)
      }
    })
  }

  function handleDelete() {
    startTransition(async () => {
      const result = await deleteCommentAction(comment.id)
      if (!result.error) onDelete(comment.id)
    })
  }

  function handleReply() {
    if (!replyContent.trim()) return
    startTransition(async () => {
      const result = await createCommentAction(replyContent, targetType, targetId, comment.id)
      if (result.data) {
        onAdd(result.data)
        setReplyContent('')
        setIsReplying(false)
      }
    })
  }

  return (
    <div className="flex flex-col gap-2">
      <div className="rounded-lg border border-zinc-100 bg-zinc-50 p-3">
        <div className="flex items-center justify-between gap-2">
          <span className="text-sm font-medium text-zinc-800">{comment.authorNickname}</span>
          <span className="text-xs text-zinc-400">
            {new Date(comment.createdAt).toLocaleDateString('ko-KR')}
          </span>
        </div>

        {isEditing ? (
          <div className="mt-2 flex flex-col gap-2">
            <textarea
              value={editContent}
              onChange={(e) => setEditContent(e.target.value)}
              rows={2}
              className="w-full resize-none rounded border border-zinc-300 px-2 py-1 text-sm"
            />
            <div className="flex gap-2">
              <button
                disabled={isPending}
                onClick={handleEdit}
                className="rounded bg-zinc-800 px-3 py-1 text-xs text-white disabled:opacity-50"
              >
                저장
              </button>
              <button
                onClick={() => {
                  setIsEditing(false)
                  setEditContent(comment.content)
                }}
                className="text-xs text-zinc-400 hover:text-zinc-600"
              >
                취소
              </button>
            </div>
          </div>
        ) : (
          <p className="mt-1 whitespace-pre-wrap text-sm text-zinc-700">{comment.content}</p>
        )}

        <div className="mt-2 flex gap-3 text-xs text-zinc-400">
          {currentUserId && !isEditing && (
            <button onClick={() => setIsReplying(!isReplying)} className="hover:text-zinc-600">
              답글
            </button>
          )}
          {canModify && !isEditing && (
            <>
              <button onClick={() => setIsEditing(true)} className="hover:text-zinc-600">
                수정
              </button>
              <button
                disabled={isPending}
                onClick={handleDelete}
                className="hover:text-red-500 disabled:opacity-50"
              >
                삭제
              </button>
            </>
          )}
        </div>
      </div>

      {isReplying && (
        <div className="ml-6 flex flex-col gap-2">
          <textarea
            value={replyContent}
            onChange={(e) => setReplyContent(e.target.value)}
            placeholder="답글을 입력하세요..."
            rows={2}
            className="w-full resize-none rounded border border-zinc-300 px-2 py-1 text-sm"
          />
          <div className="flex gap-2">
            <button
              disabled={isPending}
              onClick={handleReply}
              className="rounded bg-zinc-800 px-3 py-1 text-xs text-white disabled:opacity-50"
            >
              등록
            </button>
            <button
              onClick={() => {
                setIsReplying(false)
                setReplyContent('')
              }}
              className="text-xs text-zinc-400 hover:text-zinc-600"
            >
              취소
            </button>
          </div>
        </div>
      )}

      {replies.length > 0 && (
        <div className="ml-6 flex flex-col gap-2">
          {replies.map((reply) => (
            <CommentItem
              key={reply.id}
              comment={reply}
              replies={allComments.filter((c) => c.parentId === reply.id)}
              allComments={allComments}
              currentUserId={currentUserId}
              isAdmin={isAdmin}
              targetType={targetType}
              targetId={targetId}
              onAdd={onAdd}
              onUpdate={onUpdate}
              onDelete={onDelete}
            />
          ))}
        </div>
      )}
    </div>
  )
}