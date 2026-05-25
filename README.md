# Chwigo (취구)

자취생을 위한 공동구매 서비스 — 같은 동네 자취생들이 식재료·생필품을 함께 구매하고 정산합니다.

## 실행 방법

### 1. Docker Compose
도커가 켜져있어야합니다.

```bash
docker compose up -d --build
```

## 테스트 계정

테스트용 계정들입니다.

| 이메일 | 비밀번호 | 권한 |
|--------|----------|------|
| admin@chwigo.com | admin1234 | ROLE_ADMIN |
| user1@chwigo.com | user1234! | ROLE_USER |
| user2@chwigo.com | user1234! | ROLE_USER |
| user3@chwigo.com | user1234! | ROLE_USER |

## API 명세



### 인증 (Auth)

| 메서드 | 경로 | 설명 | 인증 |
|--------|------|------|------|
| POST | /api/auth/register | 회원가입 | 불필요 |
| POST | /api/auth/login | 로그인 | 불필요 |
| POST | /api/auth/refresh | 토큰 갱신 | 불필요 |
| POST | /api/auth/logout | 로그아웃 | 필요 |

### 게시글 (Posts)

| 메서드 | 경로 | 설명 | 인증 |
|--------|------|------|------|
| GET | /api/posts | 게시글 목록 (검색/필터) | 불필요 |
| GET | /api/posts/{id} | 게시글 상세 | 불필요 |
| POST | /api/posts | 게시글 작성 | 필요 |
| PUT | /api/posts/{id} | 게시글 수정 | 필요 |
| DELETE | /api/posts/{id} | 게시글 삭제 | 필요 |
| PUT | /api/posts/{id}/close | 게시글 마감 | 필요 |

**목록 조회 쿼리 파라미터**
- `keyword`: 제목/내용 검색어
- `category`: FOOD, DAILY, ELECTRONICS, CLOTHING, OTHER
- `status`: OPEN, FULL, CLOSED
- `page`, `size`, `sort` (Spring Pageable)

### 참여 (Participations)

| 메서드 | 경로 | 설명 | 인증 |
|--------|------|------|------|
| POST | /api/posts/{postId}/participations | 참여 신청 | 필요 |
| GET | /api/posts/{postId}/participations | 참여자 목록 (작성자만) | 필요 |
| PUT | /api/posts/{postId}/participations/{id}/approve | 참여 승인 | 필요 |
| PUT | /api/posts/{postId}/participations/{id}/reject | 참여 거절 | 필요 |
| DELETE | /api/posts/{postId}/participations/{id} | 참여 취소 | 필요 |
| GET | /api/my/participations | 내 참여 목록 | 필요 |

### 정산 (Settlements)

| 메서드 | 경로 | 설명 | 인증 |
|--------|------|------|------|
| GET | /api/posts/{postId}/settlements | 게시글 정산 목록 (작성자만) | 필요 |
| PUT | /api/settlements/{id}/pay | 정산 완료 처리 | 필요 |
| GET | /api/my/settlements | 내 정산 목록 | 필요 |

## 인증 방식

Bearer Token (JWT)

```
Authorization: Bearer <access_token>
```

AccessToken 만료 후 RefreshToken으로 재발급:

```http
POST /api/auth/refresh
Content-Type: application/json

{ "refreshToken": "<refresh_token>" }
```

## 예시 요청

### 로그인

```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user1@chwigo.com",
  "password": "user1234!"
}
```

### 게시글 작성

```http
POST /api/posts
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "title": "계란 30구 같이 사실 분",
  "description": "홈플러스 계란 4,900원 3명이서 나눕니다",
  "category": "FOOD",
  "targetCount": 3,
  "totalPrice": 4900,
  "meetLocation": "마포구 홈플러스 앞",
  "deadline": "2026-06-01T18:00:00"
}
```

### 게시글 목록 검색

```http
GET /api/posts?keyword=계란&category=FOOD&status=OPEN&page=0&size=10
```
