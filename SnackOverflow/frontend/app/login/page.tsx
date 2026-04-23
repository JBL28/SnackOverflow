import Link from 'next/link'
import LoginForm from '@/components/auth/LoginForm'

export default function LoginPage() {
  return (
    <div className="mx-auto max-w-sm">
      <h1 className="mb-6 text-2xl font-bold text-zinc-900">로그인</h1>
      <LoginForm />
      <p className="mt-4 text-center text-sm text-zinc-500">
        계정이 없으신가요?{' '}
        <Link href="/signup" className="font-medium text-zinc-900 hover:underline">
          회원가입
        </Link>
      </p>
    </div>
  )
}
