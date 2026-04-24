import { describe, it, expect } from 'vitest'
import type { Comment, SnackRecommendation, TargetType } from '@/lib/types'

describe('Comment type', () => {
  it('accepts SNACK_PURCHASE as target type', () => {
    const targetType: TargetType = 'SNACK_PURCHASE'
    expect(targetType).toBe('SNACK_PURCHASE')
  })

  it('accepts RECOMMENDATION as target type', () => {
    const targetType: TargetType = 'RECOMMENDATION'
    expect(targetType).toBe('RECOMMENDATION')
  })

  it('constructs a valid Comment object', () => {
    const comment: Comment = {
      id: '1',
      content: '맛있어요',
      authorNickname: '홍길동',
      authorId: 'user-1',
      parentId: null,
      targetType: 'SNACK_PURCHASE',
      targetId: 'snack-1',
      likes: 0,
      dislikes: 0,
      createdAt: new Date().toISOString(),
    }
    expect(comment.content).toBe('맛있어요')
    expect(comment.parentId).toBeNull()
  })
})

describe('SnackRecommendation type', () => {
  it('constructs a valid recommendation object', () => {
    const rec: SnackRecommendation = {
      id: '1',
      name: '허니버터칩',
      reason: '달달하고 맛있음',
      likes: 5,
      dislikes: 1,
      createdByNickname: '유저',
      createdById: 'user-1',
      createdAt: new Date().toISOString(),
    }
    expect(rec.name).toBe('허니버터칩')
    expect(rec.likes).toBe(5)
  })
})