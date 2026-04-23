import type { SnackStatus } from '@/lib/types'

const STATUS_MAP: Record<SnackStatus, { label: string; className: string }> = {
  DELIVERING: { label: '배송 중', className: 'bg-blue-100 text-blue-700' },
  IN_STOCK: { label: '재고 있음', className: 'bg-green-100 text-green-700' },
  OUT_OF_STOCK: { label: '재고 없음', className: 'bg-red-100 text-red-600' },
}

export default function StatusBadge({ status }: { status: SnackStatus }) {
  const { label, className } = STATUS_MAP[status]
  return (
    <span
      className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium ${className}`}
    >
      {label}
    </span>
  )
}
