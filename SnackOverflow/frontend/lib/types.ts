export type SnackStatus = 'DELIVERING' | 'IN_STOCK' | 'OUT_OF_STOCK'
export type UserRole = 'USER' | 'ADMIN'
export type UserStatus = 'ACTIVE' | 'INACTIVE' | 'DELETED'

export interface SnackPurchase {
  id: string
  name: string
  status: SnackStatus
  likes: number
  dislikes: number
  createdByNickname: string
  createdAt: string
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

export interface UserProfile {
  id: string
  username: string
  nickname: string
  email: string
  role: UserRole
  status: UserStatus
  postCount: number
  commentCount: number
  mustChangePassword: boolean
}

export interface AdminUser {
  id: string
  username: string
  nickname: string
  postCount: number
  commentCount: number
  status: UserStatus
  role: UserRole
}

export interface ApiResponse<T> {
  success: boolean
  data: T | null
  error: string | null
}
