import { describe, it, expect } from 'vitest'

function formatKoreanDate(isoString: string): string {
  return new Date(isoString).toLocaleDateString('ko-KR')
}

describe('date formatting', () => {
  it('formats ISO date to Korean locale', () => {
    const result = formatKoreanDate('2026-04-24T10:00:00Z')
    expect(result).toMatch(/\d{4}\. \d{1,2}\. \d{1,2}\./)
  })

  it('handles different dates correctly', () => {
    const d1 = formatKoreanDate('2026-01-01T00:00:00Z')
    const d2 = formatKoreanDate('2026-12-31T00:00:00Z')
    expect(d1).not.toBe(d2)
  })
})