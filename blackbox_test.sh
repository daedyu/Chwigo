#!/bin/bash
BASE="http://localhost:8080"
PASS=0; FAIL=0; WARN=0

GREEN='\033[0;32m'; RED='\033[0;31m'; YELLOW='\033[1;33m'; NC='\033[0m'

check() {
    local label="$1" expect="$2" actual="$3"
    if echo "$actual" | grep -q "$expect"; then
        echo -e "  ${GREEN}[PASS]${NC} $label"
        PASS=$((PASS+1))
    else
        echo -e "  ${RED}[FAIL]${NC} $label (expected: $expect, got: $actual)"
        FAIL=$((FAIL+1))
    fi
}

warn() {
    echo -e "  ${YELLOW}[WARN]${NC} $1"
    WARN=$((WARN+1))
}

section() { echo -e "\n[${1}] ${2}"; }

# ===== 가상 유저 정보 =====
# Alice: 게시글 작성자 (공동구매 주최자)
# Bob:   참여자 (공동구매 참여자)

ALICE_EMAIL="alice_bb_$(date +%s)@test.com"
BOB_EMAIL="bob_bb_$(date +%s)@test.com"
PASSWORD="Test1234!"

# =========================================================
section 1 "회원가입 & 로그인"
# =========================================================

# Alice 회원가입
RES=$(curl -s -X POST "$BASE/auth/register" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$ALICE_EMAIL\",\"password\":\"$PASSWORD\",\"nickname\":\"앨리스\",\"address\":\"서울시 마포구\"}")
check "Alice 회원가입" '"success":true' "$RES"
check "Alice 닉네임 반환" '"nickname":"앨리스"' "$RES"

# Bob 회원가입
RES=$(curl -s -X POST "$BASE/auth/register" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$BOB_EMAIL\",\"password\":\"$PASSWORD\",\"nickname\":\"밥\",\"address\":\"서울시 마포구\"}")
check "Bob 회원가입" '"success":true' "$RES"

# 이미 가입된 이메일 재가입 시도
RES=$(curl -s -X POST "$BASE/auth/register" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$ALICE_EMAIL\",\"password\":\"$PASSWORD\",\"nickname\":\"앨리스2\",\"address\":\"\"}")
check "중복 이메일 가입 → 409" '"status":409' "$RES"

# 빈 필드로 가입 시도
RES=$(curl -s -X POST "$BASE/auth/register" \
  -H "Content-Type: application/json" \
  -d '{"email":"","password":"","nickname":"","address":""}')
check "빈 필드 가입 → 400" '"status":400' "$RES"

# Alice 로그인
RES=$(curl -s -X POST "$BASE/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$ALICE_EMAIL\",\"password\":\"$PASSWORD\"}")
check "Alice 로그인" '"accessToken"' "$RES"
ALICE_TOKEN=$(echo "$RES" | python3 -c "import json,sys; d=json.load(sys.stdin); print(d['data']['accessToken'])" 2>/dev/null)
ALICE_REFRESH=$(echo "$RES" | python3 -c "import json,sys; d=json.load(sys.stdin); print(d['data']['refreshToken'])" 2>/dev/null)

# Bob 로그인
RES=$(curl -s -X POST "$BASE/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$BOB_EMAIL\",\"password\":\"$PASSWORD\"}")
check "Bob 로그인" '"accessToken"' "$RES"
BOB_TOKEN=$(echo "$RES" | python3 -c "import json,sys; d=json.load(sys.stdin); print(d['data']['accessToken'])" 2>/dev/null)

# 틀린 비밀번호
RES=$(curl -s -X POST "$BASE/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$ALICE_EMAIL\",\"password\":\"wrongpassword\"}")
check "틀린 비밀번호 → 401" '"status":401' "$RES"

# =========================================================
section 2 "내 프로필 조회 & 수정"
# =========================================================

RES=$(curl -s "$BASE/auth/me" -H "Authorization: Bearer $ALICE_TOKEN")
check "Alice 내 프로필 조회" '"email"' "$RES"
check "Alice 프로필 이메일 일치" "$ALICE_EMAIL" "$RES"

# 인증 없이 프로필 조회
RES=$(curl -s -o /dev/null -w "%{http_code}" "$BASE/auth/me")
check "비인증 프로필 조회 → 401" "401" "$RES"

# 프로필 수정
RES=$(curl -s -X PUT "$BASE/auth/me" \
  -H "Authorization: Bearer $ALICE_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"nickname":"앨리스(수정됨)","address":"서울시 강남구"}')
check "Alice 프로필 수정" '"nickname":"앨리스(수정됨)"' "$RES"
check "주소 수정 반영" '"address":"서울시 강남구"' "$RES"

# =========================================================
section 3 "토큰 재발급"
# =========================================================

RES=$(curl -s -X POST "$BASE/auth/refresh" \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\":\"$ALICE_REFRESH\"}")
check "토큰 재발급" '"accessToken"' "$RES"
ALICE_TOKEN=$(echo "$RES" | python3 -c "import json,sys; d=json.load(sys.stdin); print(d['data']['accessToken'])" 2>/dev/null)

# 잘못된 refreshToken
RES=$(curl -s -X POST "$BASE/auth/refresh" \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"invalid.token.here"}')
check "잘못된 refreshToken → 401" '"status":401' "$RES"

# =========================================================
section 4 "카테고리 조회"
# =========================================================

RES=$(curl -s "$BASE/categories")
check "카테고리 목록 조회" '"code"' "$RES"
check "FOOD 카테고리 포함" '"code":"FOOD"' "$RES"
check "DAILY 카테고리 포함" '"code":"DAILY"' "$RES"
CNT=$(echo "$RES" | python3 -c "import json,sys; d=json.load(sys.stdin); print(len(d['data']))" 2>/dev/null)
check "카테고리 5개" '5' "$CNT"

# =========================================================
section 5 "게시글 작성 (Alice)"
# =========================================================

# 정상 게시글 작성 (품목 2개)
RES=$(curl -s -X POST "$BASE/posts" \
  -H "Authorization: Bearer $ALICE_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title":"계란 30구 공동구매",
    "description":"마트 계란 30구 같이 사실 분 구해요. 반반 나눠요.",
    "category":"FOOD",
    "meetLocation":"마포구 망원동 편의점 앞",
    "deadline":"2026-12-31T23:59:59",
    "items":[
      {"name":"계란 30구","totalPrice":9000,"maxParticipants":2},
      {"name":"배달비","totalPrice":3000,"maxParticipants":2}
    ]
  }')
check "게시글 작성" '"success":true' "$RES"
check "품목 2개 포함" '"items"' "$RES"
check "상태 OPEN" '"status":"OPEN"' "$RES"
POST_ID=$(echo "$RES" | python3 -c "import json,sys; d=json.load(sys.stdin); print(d['data']['id'])" 2>/dev/null)
ITEM1_ID=$(echo "$RES" | python3 -c "import json,sys; d=json.load(sys.stdin); print(d['data']['items'][0]['id'])" 2>/dev/null)
ITEM2_ID=$(echo "$RES" | python3 -c "import json,sys; d=json.load(sys.stdin); print(d['data']['items'][1]['id'])" 2>/dev/null)
check "게시글 ID 추출" '[0-9]' "$POST_ID"

# 인증 없이 게시글 작성 시도
RES=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE/posts" \
  -H "Content-Type: application/json" \
  -d '{"title":"test","category":"FOOD","items":[]}')
check "비인증 게시글 작성 → 401" "401" "$RES"

# 품목 없이 작성 시도
RES=$(curl -s -X POST "$BASE/posts" \
  -H "Authorization: Bearer $ALICE_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"품목없음","category":"FOOD","deadline":"2026-12-31T23:59:59","items":[]}')
check "품목 없는 게시글 → 400" '"status":400' "$RES"

# =========================================================
section 6 "게시글 목록 & 상세 조회"
# =========================================================

# 비인증으로 목록 조회 가능해야 함
RES=$(curl -s "$BASE/posts")
check "게시글 목록 (비인증)" '"content"' "$RES"

# 키워드 검색 (한글 URL 인코딩 필요)
RES=$(curl -s -G "$BASE/posts" --data-urlencode "keyword=계란")
check "키워드 검색 '계란'" '"title":"계란 30구 공동구매"' "$RES"

# 카테고리 필터
RES=$(curl -s "$BASE/posts?category=FOOD")
check "카테고리 FOOD 필터" '"category":"FOOD"' "$RES"

# 상태 필터
RES=$(curl -s "$BASE/posts?status=OPEN")
check "상태 OPEN 필터" '"status":"OPEN"' "$RES"

# 잘못된 카테고리
RES=$(curl -s "$BASE/posts?category=INVALID")
check "잘못된 카테고리 → 400" '"status":400' "$RES"

# 게시글 상세 조회 (비인증)
RES=$(curl -s "$BASE/posts/$POST_ID")
check "게시글 상세 조회 (비인증)" '"title":"계란 30구 공동구매"' "$RES"
check "품목 상세 포함 (unitPrice)" '"unitPrice"' "$RES"
check "남은 자리 표시 (remainingSlots)" '"remainingSlots"' "$RES"

# 존재하지 않는 게시글
RES=$(curl -s "$BASE/posts/99999999")
check "없는 게시글 → 404" '"status":404' "$RES"

# =========================================================
section 7 "내 게시글 조회"
# =========================================================

RES=$(curl -s "$BASE/posts/my" -H "Authorization: Bearer $ALICE_TOKEN")
check "내 게시글 목록 (Alice)" '"title":"계란 30구 공동구매"' "$RES"

RES=$(curl -s "$BASE/posts/my" -H "Authorization: Bearer $BOB_TOKEN")
CNT=$(echo "$RES" | python3 -c "import json,sys; d=json.load(sys.stdin); print(len(d['data']))" 2>/dev/null)
check "Bob 내 게시글 (없음=0개)" '0' "$CNT"

# =========================================================
section 8 "참여 신청 (Bob → Alice 게시글)"
# =========================================================

# Bob이 Alice 게시글에 참여 신청
RES=$(curl -s -X POST "$BASE/posts/$POST_ID/participations" \
  -H "Authorization: Bearer $BOB_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"items\":[
      {\"postItemId\":$ITEM1_ID,\"quantity\":1},
      {\"postItemId\":$ITEM2_ID,\"quantity\":1}
    ]
  }")
check "Bob 참여 신청" '"status":"PENDING"' "$RES"
check "신청 품목 포함" '"items"' "$RES"
PART_ID=$(echo "$RES" | python3 -c "import json,sys; d=json.load(sys.stdin); print(d['data']['id'])" 2>/dev/null)

# 중복 신청
RES=$(curl -s -X POST "$BASE/posts/$POST_ID/participations" \
  -H "Authorization: Bearer $BOB_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"items\":[{\"postItemId\":$ITEM1_ID,\"quantity\":1}]}")
check "중복 참여 신청 → 409" '"status":409' "$RES"

# Alice(작성자) 본인 신청 시도
RES=$(curl -s -X POST "$BASE/posts/$POST_ID/participations" \
  -H "Authorization: Bearer $ALICE_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"items\":[{\"postItemId\":$ITEM1_ID,\"quantity\":1}]}")
check "작성자 본인 신청 → 400" '"status":400' "$RES"

# 초과 수량 신청 (별도 게시글에서 maxParticipants=1인데 2 신청)
RES_OVER=$(curl -s -X POST "$BASE/posts" \
  -H "Authorization: Bearer $ALICE_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"초과테스트","description":"test","category":"OTHER","meetLocation":"test","deadline":"2026-12-31T23:59:59","items":[{"name":"아이템","totalPrice":1000,"maxParticipants":2}]}')
OVER_POST_ID=$(echo "$RES_OVER" | python3 -c "import json,sys; d=json.load(sys.stdin); print(d['data']['id'])" 2>/dev/null)
OVER_ITEM_ID=$(echo "$RES_OVER" | python3 -c "import json,sys; d=json.load(sys.stdin); print(d['data']['items'][0]['id'])" 2>/dev/null)
RES=$(curl -s -X POST "$BASE/posts/$OVER_POST_ID/participations" \
  -H "Authorization: Bearer $BOB_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"items\":[{\"postItemId\":$OVER_ITEM_ID,\"quantity\":3}]}")
check "초과 수량 신청 → 400" '"status":400' "$RES"
curl -s -X DELETE "$BASE/posts/$OVER_POST_ID" -H "Authorization: Bearer $ALICE_TOKEN" > /dev/null

# =========================================================
section 9 "참여자 목록 조회"
# =========================================================

# Alice(작성자)는 목록 볼 수 있음
RES=$(curl -s "$BASE/posts/$POST_ID/participations" -H "Authorization: Bearer $ALICE_TOKEN")
check "작성자 참여자 목록 조회" '"status":"PENDING"' "$RES"

# Bob(비작성자)은 볼 수 없음
RES=$(curl -s "$BASE/posts/$POST_ID/participations" -H "Authorization: Bearer $BOB_TOKEN")
check "비작성자 참여자 목록 → 403" '"status":403' "$RES"

# =========================================================
section 10 "참여 승인 (Alice)"
# =========================================================

# Bob이 아닌 사람이 승인 시도
RES=$(curl -s -X PUT "$BASE/posts/$POST_ID/participations/$PART_ID/approve" \
  -H "Authorization: Bearer $BOB_TOKEN")
check "비작성자 승인 시도 → 403" '"status":403' "$RES"

# Alice가 승인
RES=$(curl -s -X PUT "$BASE/posts/$POST_ID/participations/$PART_ID/approve" \
  -H "Authorization: Bearer $ALICE_TOKEN")
check "Alice 참여 승인" '"status":"APPROVED"' "$RES"

# 이미 승인된 건 재승인
RES=$(curl -s -X PUT "$BASE/posts/$POST_ID/participations/$PART_ID/approve" \
  -H "Authorization: Bearer $ALICE_TOKEN")
check "이미 승인된 건 재승인 → 400" '"status":400' "$RES"

# =========================================================
section 11 "정산 조회"
# =========================================================

# Alice가 게시글 정산 목록 조회
RES=$(curl -s "$BASE/posts/$POST_ID/settlements" -H "Authorization: Bearer $ALICE_TOKEN")
check "작성자 정산 목록 조회" '"status":"PENDING"' "$RES"
check "정산 금액 포함" '"amount"' "$RES"
SETTLE_ID=$(echo "$RES" | python3 -c "import json,sys; d=json.load(sys.stdin); print(d['data'][0]['id'])" 2>/dev/null)

# Bob이 자신의 정산 조회
RES=$(curl -s "$BASE/my/settlements" -H "Authorization: Bearer $BOB_TOKEN")
check "Bob 내 정산 조회" '"status":"PENDING"' "$RES"

# 비작성자가 게시글 정산 조회 (보안 체크)
RES=$(curl -s "$BASE/posts/$POST_ID/settlements" -H "Authorization: Bearer $BOB_TOKEN")
check "비작성자 게시글 정산 조회 → 403" '"status":403' "$RES"

# =========================================================
section 12 "정산 완료 처리"
# =========================================================

# Bob(참여자)이 정산 완료 시도 → Alice(작성자)만 가능해야 함
RES=$(curl -s -X PUT "$BASE/settlements/$SETTLE_ID/pay" -H "Authorization: Bearer $BOB_TOKEN")
check "참여자가 정산 완료 → 403" '"status":403' "$RES"

# Alice(작성자)가 정산 완료 처리
RES=$(curl -s -X PUT "$BASE/settlements/$SETTLE_ID/pay" -H "Authorization: Bearer $ALICE_TOKEN")
check "작성자 정산 완료 처리" '"status":"PAID"' "$RES"
check "paidAt 기록" '"paidAt"' "$RES"

# 이미 완료된 정산 재처리
RES=$(curl -s -X PUT "$BASE/settlements/$SETTLE_ID/pay" -H "Authorization: Bearer $ALICE_TOKEN")
check "이미 완료된 정산 재처리 → 400" '"status":400' "$RES"

# =========================================================
section 13 "게시글 수정"
# =========================================================

# 수정 테스트용 게시글 생성 (참여자 없음)
RES_EDIT=$(curl -s -X POST "$BASE/posts" \
  -H "Authorization: Bearer $ALICE_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"수정테스트 게시글","description":"초기 설명","category":"DAILY","meetLocation":"테스트 장소","deadline":"2026-12-31T23:59:59","items":[{"name":"품목A","totalPrice":5000,"maxParticipants":3}]}')
EDIT_POST_ID=$(echo "$RES_EDIT" | python3 -c "import json,sys; d=json.load(sys.stdin); print(d['data']['id'])" 2>/dev/null)

# Alice가 본인 게시글 수정 (참여자 없으므로 가능)
RES=$(curl -s -X PUT "$BASE/posts/$EDIT_POST_ID" \
  -H "Authorization: Bearer $ALICE_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"수정테스트 게시글 (수정됨)","description":"수정된 설명","category":"DAILY","meetLocation":"수정된 장소","deadline":"2026-12-31T23:59:59","items":[{"name":"품목A수정","totalPrice":6000,"maxParticipants":3}]}')
check "본인 게시글 수정" '"title":"수정테스트 게시글 (수정됨)"' "$RES"

# 참여자 있는 게시글 수정 시도 → 400 (POST_ID에는 이미 Bob이 승인됨)
RES=$(curl -s -X PUT "$BASE/posts/$POST_ID" \
  -H "Authorization: Bearer $ALICE_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"수정시도","description":"설명","category":"FOOD","meetLocation":"장소","deadline":"2026-12-31T23:59:59","items":[{"name":"계란","totalPrice":9000,"maxParticipants":2}]}')
check "참여자 있는 게시글 수정 → 400" '"status":400' "$RES"

# Bob이 Alice 게시글 수정 시도 → 403
RES=$(curl -s -X PUT "$BASE/posts/$EDIT_POST_ID" \
  -H "Authorization: Bearer $BOB_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"해킹","description":"해킹설명","category":"FOOD","meetLocation":"장소","deadline":"2026-12-31T23:59:59","items":[{"name":"a","totalPrice":1000,"maxParticipants":2}]}')
check "타인 게시글 수정 → 403" '"status":403' "$RES"

# 수정 테스트 게시글 삭제
curl -s -X DELETE "$BASE/posts/$EDIT_POST_ID" -H "Authorization: Bearer $ALICE_TOKEN" > /dev/null

# =========================================================
section 14 "게시글 마감"
# =========================================================

# Alice가 게시글 마감
RES=$(curl -s -X PUT "$BASE/posts/$POST_ID/close" -H "Authorization: Bearer $ALICE_TOKEN")
check "게시글 마감" '"status":"CLOSED"' "$RES"

# 이미 마감된 게시글 재마감
RES=$(curl -s -X PUT "$BASE/posts/$POST_ID/close" -H "Authorization: Bearer $ALICE_TOKEN")
check "이미 마감된 게시글 재마감 → 400" '"status":400' "$RES"

# 마감된 게시글에 참여 신청
RES=$(curl -s -X POST "$BASE/posts/$POST_ID/participations" \
  -H "Authorization: Bearer $BOB_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"items\":[{\"postItemId\":$ITEM1_ID,\"quantity\":1}]}")
check "마감된 게시글 참여 신청 → 400" '"status":400' "$RES"

# =========================================================
section 15 "참여 거절 & 취소 플로우"
# =========================================================

# 새 게시글 생성 (거절/취소 테스트용)
RES=$(curl -s -X POST "$BASE/posts" \
  -H "Authorization: Bearer $ALICE_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title":"양파 한망 공동구매",
    "description":"양파 3kg 나눠요",
    "category":"FOOD",
    "meetLocation":"은평구 홈플러스 앞",
    "deadline":"2026-12-31T23:59:59",
    "items":[{"name":"양파 3kg","totalPrice":6000,"maxParticipants":3}]
  }')
POST2_ID=$(echo "$RES" | python3 -c "import json,sys; d=json.load(sys.stdin); print(d['data']['id'])" 2>/dev/null)
ITEM3_ID=$(echo "$RES" | python3 -c "import json,sys; d=json.load(sys.stdin); print(d['data']['items'][0]['id'])" 2>/dev/null)
check "두 번째 게시글 생성" '"success":true' "$RES"

# Bob 참여 신청
RES=$(curl -s -X POST "$BASE/posts/$POST2_ID/participations" \
  -H "Authorization: Bearer $BOB_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"items\":[{\"postItemId\":$ITEM3_ID,\"quantity\":2}]}")
PART2_ID=$(echo "$RES" | python3 -c "import json,sys; d=json.load(sys.stdin); print(d['data']['id'])" 2>/dev/null)
check "Bob 두 번째 게시글 참여 신청" '"status":"PENDING"' "$RES"

# Alice가 거절
RES=$(curl -s -X PUT "$BASE/posts/$POST2_ID/participations/$PART2_ID/reject" \
  -H "Authorization: Bearer $ALICE_TOKEN")
check "참여 거절" '"status":"REJECTED"' "$RES"

# Bob 내 참여 목록에서 REJECTED 확인
RES=$(curl -s "$BASE/my/participations" -H "Authorization: Bearer $BOB_TOKEN")
check "내 참여 목록 조회" '"status"' "$RES"

# 취소 테스트 - Bob 재신청
RES=$(curl -s -X POST "$BASE/posts/$POST2_ID/participations" \
  -H "Authorization: Bearer $BOB_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"items\":[{\"postItemId\":$ITEM3_ID,\"quantity\":1}]}")
check "거절 후 재신청" '"status":"PENDING"' "$RES"
PART3_ID=$(echo "$RES" | python3 -c "import json,sys; d=json.load(sys.stdin); print(d['data']['id'])" 2>/dev/null)

# Bob이 본인 신청 취소
RES=$(curl -s -X DELETE "$BASE/posts/$POST2_ID/participations/$PART3_ID" \
  -H "Authorization: Bearer $BOB_TOKEN")
check "본인 참여 취소" '"success":true' "$RES"

# Alice가 타인 신청 취소 시도
RES=$(curl -s -X POST "$BASE/posts/$POST2_ID/participations" \
  -H "Authorization: Bearer $BOB_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"items\":[{\"postItemId\":$ITEM3_ID,\"quantity\":1}]}")
PART4_ID=$(echo "$RES" | python3 -c "import json,sys; d=json.load(sys.stdin); print(d['data']['id'])" 2>/dev/null)
RES=$(curl -s -X DELETE "$BASE/posts/$POST2_ID/participations/$PART4_ID" \
  -H "Authorization: Bearer $ALICE_TOKEN")
check "작성자가 타인 신청 취소 → 403" '"status":403' "$RES"

# =========================================================
section 16 "게시글 삭제"
# =========================================================

# Bob이 Alice 게시글 삭제 시도
RES=$(curl -s -o /dev/null -w "%{http_code}" -X DELETE "$BASE/posts/$POST2_ID" \
  -H "Authorization: Bearer $BOB_TOKEN")
check "타인 게시글 삭제 → 403" "403" "$RES"

# Alice가 삭제
RES=$(curl -s -X DELETE "$BASE/posts/$POST2_ID" -H "Authorization: Bearer $ALICE_TOKEN")
check "본인 게시글 삭제" '"success":true' "$RES"

RES=$(curl -s "$BASE/posts/$POST2_ID")
check "삭제된 게시글 조회 → 404" '"status":404' "$RES"

# =========================================================
section 17 "로그아웃"
# =========================================================

RES=$(curl -s -X POST "$BASE/auth/logout" -H "Authorization: Bearer $ALICE_TOKEN")
check "Alice 로그아웃" '"success":true' "$RES"

# 로그아웃 후 기존 refreshToken 재사용 시도
RES=$(curl -s -X POST "$BASE/auth/refresh" \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\":\"$ALICE_REFRESH\"}")
check "로그아웃 후 refreshToken 재사용 → 401" '"status":401' "$RES"

# =========================================================
echo ""
echo "======================================="
printf "  결과: ${GREEN}%d 통과${NC}, ${RED}%d 실패${NC}, ${YELLOW}%d 경고${NC}\n" $PASS $FAIL $WARN
echo "======================================="
