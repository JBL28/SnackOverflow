import Link from 'next/link'
import SignupForm from '@/components/auth/SignupForm'

export default function SignupPage() {
  return (
    <div className="mx-auto max-w-sm">
      <h1 className="mb-6 text-2xl font-bold text-zinc-900">회원가입</h1>
      <SignupForm />
      <p className="mt-4 text-center text-sm text-zinc-500">
        이미 계정이 있으신가요?{' '}
        <Link href="/login" className="font-medium text-zinc-900 hover:underline">
          로그인
        </Link>
      </p>
    </div>
  )
}
