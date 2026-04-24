# SnackOverflow 구현 계획

> 작성일: 2026-04-24  
> Getting_Start!.md 요구사항 대비 현재 구현 상태 및 남은 작업 정리

---

## 현재 구현 상태

### ✅ 완료

#### 백엔드
| 기능 | 파일 |
|------|------|
| 회원가입 / 로그인 / 로그아웃 | `AuthController`, `AuthService` |
| JWT access + refresh 토큰 인가 | `JwtTokenProvider`, `JwtAuthenticationFilter` |
| BCrypt 비밀번호 해시 (salt 포함) | `AuthService`, `SecurityConfig` |
| 비밀번호 특수문자 안전 검증 | `SafePassword`, `SafePasswordValidator` |
| 이메일 유효성 검증 | `SignupRequest` @Email |
| 유저 프로필 조회 / 닉네임·비밀번호 변경 / 탈퇴 | `UserController`, `UserService` |
| 구매한 과자 CRUD (관리자 전용 등록/수정/삭제) | `SnackPurchaseController`, `SnackPurchaseService` |
| 구매한 과자 상태 변경 (모든 사용자 가능) | `SnackPurchaseController` PATCH status |
| 관리자 유저 관리 (목록·상태 변경·비밀번호 초기화) | `UserController` /admin/* |
| 전역 예외 처리 | `GlobalExceptionHandler` |
| Spring Security RBAC | `SecurityConfig`, `@PreAuthorize` |

#### 프론트엔드
| 기능 | 파일 |
|------|------|
| 로그인 / 회원가입 페이지 | `app/login`, `app/signup` |
| 구매한 과자 목록 (홈) | `app/page.tsx` |
| 구매한 과자 상세 페이지 | `app/snacks/[id]/page.tsx` |
| 관리자 패널 (과자 등록·관리·유저 관리) | `app/admin/page.tsx`, `UserManagement.tsx` |
| 마이페이지 (닉네임·비밀번호 변경, 탈퇴) | `app/profile/page.tsx` |
| 인증 상태 헤더 | `Header.tsx` |

---

## ❌ 미구현 (요구사항 Gap)

### 핵심 기능

| # | 기능 | 비고 |
|---|------|------|
| 1 | **과자 추천 게시판** CRUD (목록·상세·작성·수정·삭제) | 백엔드 + 프론트 모두 없음 |
| 2 | **댓글 시스템** (게시글에 댓글, 대댓글, CRUD) | 백엔드 + 프론트 모두 없음 |
| 3 | **좋아요/싫어요** (토글, 1인1표, 호버 시 닉네임 목록) | 백엔드 + 프론트 모두 없음 |
| 4 | **마이페이지 내 게시글·댓글 목록** | 백엔드 endpoint + 프론트 UI 없음 |

### 인프라 / 품질

| # | 기능 | 비고 |
|---|------|------|
| 5 | **로깅 시스템** (INFO/ERROR/DEBUG, 날짜별 파일, 7일 자동 삭제) | 백엔드 Logback + 프론트 없음 |
| 6 | **백엔드 단위 테스트** (JUnit5, 유지보수성 있는 행위 기반) | 일부 통합테스트만 존재 |
| 7 | **프론트엔드 단위 테스트** (Vitest) | 전혀 없음 |

### 디자인

| # | 요구사항 |
|---|----------|
| 8 | 제빵소 메뉴판 느낌, 종이질감 아이보리 UI, round 최소화 |

---

## 구현 계획

### Phase 3: 과자 추천 게시판

**백엔드**
- `SnackRecommendation` 엔티티 + Flyway 마이그레이션
  - 필드: `id(UUID)`, `name`, `reason`, `likes`, `dislikes`, `author(User FK)`, `createdAt`
- `SnackRecommendationController`
  - `GET /api/snack-recommendations` — 목록 (페이지 5개)
  - `GET /api/snack-recommendations/{id}` — 상세
  - `POST /api/snack-recommendations` — 작성 (인증 필요)
  - `PUT /api/snack-recommendations/{id}` — 수정 (본인 또는 관리자)
  - `DELETE /api/snack-recommendations/{id}` — 삭제 (본인 또는 관리자)
- `SnackRecommendationService`, `SnackRecommendationRepository`, DTO 4종

**프론트엔드**
- `app/recommendations/page.tsx` — 목록
- `app/recommendations/[id]/page.tsx` — 상세 + 수정/삭제 버튼 (본인·관리자)
- `components/recommendation/RecommendationCard.tsx`
- `components/recommendation/CreateRecommendationForm.tsx`
- `actions/recommendations.ts` — Server Actions 5종
- `Header.tsx`에 "과자 추천" 메뉴 추가

---

### Phase 4: 댓글 시스템

**백엔드**
- `Comment` 엔티티
  - 필드: `id(UUID)`, `content`, `author(User FK)`, `parentComment(nullable self FK)`, `targetType(SNACK_PURCHASE|RECOMMENDATION)`, `targetId(UUID)`, `likes`, `dislikes`, `createdAt`
- `CommentController`
  - `GET /api/comments?targetType=&targetId=` — 댓글 목록 (트리 구조)
  - `POST /api/comments` — 댓글 작성
  - `PUT /api/comments/{id}` — 수정 (본인)
  - `DELETE /api/comments/{id}` — 삭제 (본인 또는 관리자)
- `CommentService`, `CommentRepository`

**프론트엔드**
- `components/comment/CommentSection.tsx` — 목록 + 입력창
- `components/comment/CommentItem.tsx` — 단일 댓글 (대댓글 재귀 렌더링)
- `actions/comments.ts`
- `app/snacks/[id]/page.tsx`에 CommentSection 추가
- `app/recommendations/[id]/page.tsx`에 CommentSection 추가

---

### Phase 5: 좋아요/싫어요

**백엔드**
- `Reaction` 엔티티
  - 필드: `id(UUID)`, `user(FK)`, `targetType(SNACK_PURCHASE|RECOMMENDATION|COMMENT)`, `targetId(UUID)`, `type(LIKE|DISLIKE)`
  - UNIQUE 제약: `(user_id, target_type, target_id)` — 1인 1표
- `ReactionController`
  - `POST /api/reactions` — 토글 (같은 타입이면 취소, 다른 타입이면 전환)
  - `GET /api/reactions/voters?targetType=&targetId=&type=` — 닉네임 목록
- `ReactionService`, `ReactionRepository`
- 각 엔티티 likes/dislikes 카운트 동기화

**프론트엔드**
- `components/reaction/ReactionButtons.tsx`
  - 클릭: 토글 Server Action
  - 1초 호버: 닉네임 팝오버 (GET voters)
- `actions/reactions.ts`
- `SnackCard`, `RecommendationCard`, `CommentItem`에 적용

---

### Phase 6: 마이페이지 게시글·댓글 목록

**백엔드**
- `UserController`에 추가
  - `GET /api/users/me/posts` — 내 과자 추천 게시글 목록
  - `GET /api/users/me/comments` — 내 댓글 목록

**프론트엔드**
- `app/profile/page.tsx` 탭 추가 (프로필 설정 | 내 게시글 | 내 댓글)
- `components/profile/MyPostList.tsx`
- `components/profile/MyCommentList.tsx`

---

### Phase 7: 로깅 시스템

**백엔드** (Logback)
- `logback-spring.xml`
  - INFO / WARN / ERROR 파일 분리 또는 단일 rolling
  - `RollingFileAppender` — `logs/app.YYYY-MM-DD.log`
  - `maxHistory=7` (7일 초과 자동 삭제)
  - `totalSizeCap` 설정

**프론트엔드** (pino)
- `lib/logger.ts` — INFO/ERROR/DEBUG 레벨
- Next.js Server Action / Route Handler 에러 로깅
- 날짜별 파일 출력, 7일 초과 삭제

---

### Phase 8: 테스트

**백엔드** (JUnit5 + Testcontainers)
- 기존 통합 테스트 정비
- 서비스 단위 테스트 추가
  - `SnackRecommendationServiceTest`
  - `CommentServiceTest`
  - `ReactionServiceTest`
- 구현 코드 변경에도 통과하는 행위(behavior) 기반 테스트

**프론트엔드** (Vitest + Testing Library)
- `vitest.config.ts` 설정
- Server Action 단위 테스트
- 컴포넌트 렌더링 테스트
- 커버리지 80% 목표

---

### Phase 9: 디자인 리뉴얼

- 배경: 아이보리 `#FDF8F0`, 텍스트: 짙은 갈색 `#3D2B1F`
- `border-radius: 0` 기준 (종이 느낌)
- 종이 질감 배경 패턴 (CSS noise 또는 SVG)
- 타이포그래피: Noto Serif KR + 고정폭 보조 폰트
- 메뉴판 스타일 섹션 구분선, 카드 레이아웃

---

## 진행 순서

```
Phase 3 → Phase 4 → Phase 5 → Phase 6 → Phase 7 → Phase 8 → Phase 9
과자추천    댓글      좋아요     마이페이지   로깅       테스트     디자인
```

> Phase 3~6: 핵심 기능, 순서대로 진행 필수  
> Phase 7: Phase 6 완료 후 또는 병행 가능  
> Phase 8: 각 Phase 완료 직후 작성 권장 (일괄 작성도 가능)  
> Phase 9: 기능 완성 후 마지막에 진행
