# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

> **상세 요구사항은 [`Getting_Start!.md`](./Getting_Start!.md)를 반드시 먼저 참고할 것.**  
> 기능 명세, 권한 정책, 마이페이지, 관리자 페이지, 좋아요/댓글 세부 동작이 모두 거기에 정의되어 있음.

## Project Overview

**SnackOverflow** — SSAFY 12반 간식 선호 조사 및 추천 플랫폼.  
관리자가 구매한 과자를 게시하고, 사용자가 과자를 추천하며 좋아요/댓글로 피드백하는 커뮤니티 서비스.

## Architecture

```
SnackOverflow/
├── frontend/   # Next.js (View + Controller)
└── backend/    # Spring Boot (Model)
```

**Database**: MySQL

### Frontend (Next.js)
- 백엔드 API 호출은 **반드시 Server Component**에서 수행 — 백엔드 주소 클라이언트 노출 금지
- **Zustand**: 클라이언트 전역 상태관리
- **Axios**: API 호출 모듈화 — 별도 파일로 백엔드 주소와 API 목록 중앙 관리
- Axios 버전: 최근 공급망 공격(0.x 취약 버전) 회피, `1.x` 이상 사용

### Backend (Spring Boot)
- 패키지 구조: `controller`, `config`, `service`, `dto`, `entity`, `repository`
- **Spring Security + JWT**: ID/PW 로그인, JWT access token + refresh token 인가
- **JPA**: 엔티티 관리 (Java 코드로 스키마 정의)
- `application.yml`에서 프론트엔드 주소, DB 주소/계정 정보 중앙 관리 (환경변수로 주입)

## Domain Model

### User
- 필드: UUID(고유ID), username(중복 불가), nickname, email, password(bcrypt+salt), createdAt, status(ACTIVE|INACTIVE|DELETED), postCount, commentCount
- Role: `USER`, `ADMIN`

### SnackPurchase (구매한 과자, 관리자 전용 CRUD)
- 필드: UUID, name, status(DELIVERING|IN_STOCK|OUT_OF_STOCK), likes, dislikes
- status는 모든 사용자가 변경 가능

### SnackRecommendation (과자 추천, 사용자 CRUD)
- 필드: UUID, name, reason, likes, dislikes
- 작성자 본인 수정/삭제, 관리자는 타인 것도 삭제 가능

### Comment
- 게시글(SnackPurchase, SnackRecommendation) 양쪽에 붙음
- 대댓글 지원 (parentCommentId)
- 좋아요/싫어요 지원

### Like/Dislike
- 게시글과 댓글 모두 적용
- 1인 1피드백 (토글 방식)
- 1초 이상 호버 시 피드백 남긴 사용자 닉네임 목록 표시

## Auth Flow
- 로그인: ID/PW → JWT access token + refresh token 발급
- PW 규칙: 6자 이상, 영문+숫자, 허용된 특수문자만 (SQL/코드 인젝션 방지)
- 이메일 유효성 검증 필요 (회원가입/로그인)

## Common Rules
- 페이지네이션: 게시글 5개/페이지
- 모든 게시글/댓글 엔티티는 Unique UUID 별도 관리 (이름 중복 허용)

## Development Commands

### Frontend
```bash
cd frontend
npm install
npm run dev        # 개발 서버
npm run build      # 프로덕션 빌드
npm run lint       # ESLint
```

### Backend
```bash
cd backend
./mvnw spring-boot:run          # 개발 실행
./mvnw test                     # 전체 테스트
./mvnw test -Dtest=ClassName    # 단일 클래스 테스트
./mvnw package -DskipTests      # 빌드 (테스트 생략)
```

### Database
```bash
# application.yml에서 DB 설정 확인 후 MySQL 실행
mysql -u root -p snackoverflow
```
