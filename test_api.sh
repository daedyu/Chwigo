#!/bin/bash
BASE="http://localhost:8080"
PASS=0
FAIL=0

ok() { echo "  [PASS] $1"; ((PASS++)); }
fail() { echo "  [FAIL] $1 — $2"; ((FAIL++)); }

check() {
  local label="$1"; local expected="$2"; local actual="$3"
  if [[ "$actual" == *"$expected"* ]]; then ok "$label"; else fail "$label" "expected '$expected' in '$actual'"; fi
}

check_status() {
  local label="$1"; local expected="$2"; local actual="$3"
  if [[ "$actual" == "$expected" ]]; then ok "$label"; else fail "$label" "HTTP $actual (expected $expected)"; fi
}

check_nonempty() {
  local label="$1"; local actual="$2"
  if [[ -n "$actual" && "$actual" != "None" && "$actual" != "null" ]]; then ok "$label"; else fail "$label" "got empty/null: '$actual'"; fi
}

echo "======================================="
echo "  Chwigo API Test Suite"
echo "======================================="

# ========== AUTH ==========
echo ""
echo "[1] Auth"

TS=$(date +%s)
EMAIL="test$TS@chwigo.com"

# Register
REG=$(curl -s -w "\n%{http_code}" -X POST "$BASE/auth/register" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$EMAIL\",\"password\":\"pass1234!\",\"nickname\":\"테스터\",\"address\":\"서울시 종로구\"}")
check_status "Register new user" "201" "$(echo "$REG" | tail -1)"

# Duplicate email
DUP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/auth/register" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$EMAIL\",\"password\":\"pass1234!\",\"nickname\":\"중복\",\"address\":\"서울\"}")
check_status "Duplicate email → 409" "409" "$(echo "$DUP" | tail -1)"

# Login as new user
LOGIN=$(curl -s -w "\n%{http_code}" -X POST "$BASE/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$EMAIL\",\"password\":\"pass1234!\"}")
check_status "Login success" "200" "$(echo "$LOGIN" | tail -1)"
ACCESS=$(echo "$LOGIN" | head -1 | python3 -c "import sys,json; d=json.load(sys.stdin); print(d['data']['accessToken'])" 2>/dev/null)
REFRESH_TOK=$(echo "$LOGIN" | head -1 | python3 -c "import sys,json; d=json.load(sys.stdin); print(d['data']['refreshToken'])" 2>/dev/null)
check "Access token issued" "eyJ" "$ACCESS"

# Login as user1 (post author for participation tests)
U1_LOGIN=$(curl -s -X POST "$BASE/auth/login" -H "Content-Type: application/json" -d '{"email":"user1@chwigo.com","password":"user1234!"}')
U1_TOKEN=$(echo "$U1_LOGIN" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d['data']['accessToken'])" 2>/dev/null)

# Login as user2
U2_LOGIN=$(curl -s -X POST "$BASE/auth/login" -H "Content-Type: application/json" -d '{"email":"user2@chwigo.com","password":"user1234!"}')
U2_TOKEN=$(echo "$U2_LOGIN" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d['data']['accessToken'])" 2>/dev/null)

# Wrong password → 401
WRONG=$(curl -s -w "\n%{http_code}" -X POST "$BASE/auth/login" \
  -H "Content-Type: application/json" -d '{"email":"user1@chwigo.com","password":"wrongpass"}')
check_status "Wrong password → 401" "401" "$(echo "$WRONG" | tail -1)"

# Get my profile
ME=$(curl -s -w "\n%{http_code}" "$BASE/auth/me" -H "Authorization: Bearer $ACCESS")
check_status "Get my profile" "200" "$(echo "$ME" | tail -1)"
check "Profile has email" "$EMAIL" "$(echo "$ME" | head -1)"

# Unauthenticated /me → 401
UNAUTH=$(curl -s -w "\n%{http_code}" "$BASE/auth/me")
check_status "Unauthenticated /me → 401" "401" "$(echo "$UNAUTH" | tail -1)"

# Update profile
UPD=$(curl -s -w "\n%{http_code}" -X PUT "$BASE/auth/me" \
  -H "Authorization: Bearer $ACCESS" -H "Content-Type: application/json" \
  -d '{"nickname":"새닉네임","address":"부산시 해운대구"}')
check_status "Update profile" "200" "$(echo "$UPD" | tail -1)"

# Token refresh
REFRESH_R=$(curl -s -w "\n%{http_code}" -X POST "$BASE/auth/refresh" \
  -H "Content-Type: application/json" -d "{\"refreshToken\":\"$REFRESH_TOK\"}")
check_status "Token refresh" "200" "$(echo "$REFRESH_R" | tail -1)"
ACCESS=$(echo "$REFRESH_R" | head -1 | python3 -c "import sys,json; d=json.load(sys.stdin); print(d['data']['accessToken'])" 2>/dev/null)

# Logout
LOGOUT=$(curl -s -w "\n%{http_code}" -X POST "$BASE/auth/logout" -H "Authorization: Bearer $ACCESS")
check_status "Logout" "200" "$(echo "$LOGOUT" | tail -1)"

# Re-login after logout (get fresh token)
RELOGIN=$(curl -s -X POST "$BASE/auth/login" -H "Content-Type: application/json" \
  -d "{\"email\":\"$EMAIL\",\"password\":\"pass1234!\"}")
ACCESS=$(echo "$RELOGIN" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d['data']['accessToken'])" 2>/dev/null)

# ========== POSTS ==========
echo ""
echo "[2] Posts"

# List posts (public)
LIST=$(curl -s -w "\n%{http_code}" "$BASE/posts")
check_status "List posts (public)" "200" "$(echo "$LIST" | tail -1)"
check "Posts content exists" "content" "$(echo "$LIST" | head -1)"

# Search by keyword (URL-encoded 계란)
SEARCH=$(curl -s -w "\n%{http_code}" "$BASE/posts?keyword=%EA%B3%84%EB%9E%80")
check_status "Search posts by keyword" "200" "$(echo "$SEARCH" | tail -1)"

# Filter by category
CAT=$(curl -s -w "\n%{http_code}" "$BASE/posts?category=FOOD")
check_status "Filter posts by category" "200" "$(echo "$CAT" | tail -1)"

# Filter by status
STAT_F=$(curl -s -w "\n%{http_code}" "$BASE/posts?status=OPEN")
check_status "Filter posts by status" "200" "$(echo "$STAT_F" | tail -1)"

# Get post detail
FIRST_POST_ID=$(echo "$LIST" | head -1 | python3 -c "import sys,json; d=json.load(sys.stdin); print(d['data']['content'][0]['id'])" 2>/dev/null)
DETAIL=$(curl -s -w "\n%{http_code}" "$BASE/posts/$FIRST_POST_ID")
check_status "Get post detail" "200" "$(echo "$DETAIL" | tail -1)"
check "Post has items" "items" "$(echo "$DETAIL" | head -1)"

# Nonexistent post → 404
NF=$(curl -s -w "\n%{http_code}" "$BASE/posts/99999")
check_status "Nonexistent post → 404" "404" "$(echo "$NF" | tail -1)"

# Create post (as new test user)
DEADLINE=$(python3 -c "from datetime import datetime, timedelta; print((datetime.now()+timedelta(days=5)).strftime('%Y-%m-%dT%H:%M:%S'))")
CREATE=$(curl -s -w "\n%{http_code}" -X POST "$BASE/posts" \
  -H "Authorization: Bearer $ACCESS" -H "Content-Type: application/json" \
  -d "{
    \"title\":\"테스트 공구 게시글\",
    \"description\":\"테스트용 공동구매입니다\",
    \"category\":\"FOOD\",
    \"meetLocation\":\"강남역 2번 출구\",
    \"deadline\":\"$DEADLINE\",
    \"items\":[
      {\"name\":\"테스트 품목A\",\"totalPrice\":6000,\"maxParticipants\":3},
      {\"name\":\"테스트 품목B\",\"totalPrice\":9000,\"maxParticipants\":3}
    ]
  }")
check_status "Create post" "201" "$(echo "$CREATE" | tail -1)"
NEW_POST_ID=$(echo "$CREATE" | head -1 | python3 -c "import sys,json; d=json.load(sys.stdin); print(d['data']['id'])" 2>/dev/null)
check "New post has items" "items" "$(echo "$CREATE" | head -1)"
check_nonempty "New post ID" "$NEW_POST_ID"

# Create post without auth → 401
NO_AUTH=$(curl -s -w "\n%{http_code}" -X POST "$BASE/posts" -H "Content-Type: application/json" \
  -d '{"title":"x","description":"x","category":"FOOD","meetLocation":"x","deadline":"2099-01-01T00:00:00","items":[{"name":"x","totalPrice":1000,"maxParticipants":2}]}')
check_status "Create post without auth → 401" "401" "$(echo "$NO_AUTH" | tail -1)"

# Update post (by author)
UPD_POST=$(curl -s -w "\n%{http_code}" -X PUT "$BASE/posts/$NEW_POST_ID" \
  -H "Authorization: Bearer $ACCESS" -H "Content-Type: application/json" \
  -d "{
    \"title\":\"수정된 공구 게시글\",
    \"description\":\"수정된 내용\",
    \"category\":\"FOOD\",
    \"meetLocation\":\"강남역 3번 출구\",
    \"deadline\":\"$DEADLINE\",
    \"items\":[
      {\"name\":\"수정 품목A\",\"totalPrice\":6000,\"maxParticipants\":3},
      {\"name\":\"수정 품목B\",\"totalPrice\":9000,\"maxParticipants\":3}
    ]
  }")
check_status "Update own post" "200" "$(echo "$UPD_POST" | tail -1)"

# Update post by non-author → 403
UPD_OTHER=$(curl -s -w "\n%{http_code}" -X PUT "$BASE/posts/$NEW_POST_ID" \
  -H "Authorization: Bearer $U1_TOKEN" -H "Content-Type: application/json" \
  -d "{\"title\":\"x\",\"description\":\"x\",\"category\":\"FOOD\",\"meetLocation\":\"x\",\"deadline\":\"$DEADLINE\",\"items\":[{\"name\":\"x\",\"totalPrice\":1000,\"maxParticipants\":2}]}")
check_status "Update others post → 403" "403" "$(echo "$UPD_OTHER" | tail -1)"

# Get my posts
MY_POSTS=$(curl -s -w "\n%{http_code}" "$BASE/posts/my" -H "Authorization: Bearer $ACCESS")
check_status "Get my posts" "200" "$(echo "$MY_POSTS" | tail -1)"
check "My posts list" "수정된 공구" "$(echo "$MY_POSTS" | head -1)"

# ========== PARTICIPATIONS ==========
echo ""
echo "[3] Participations"

# Get updated item IDs from post detail
POST_DETAIL=$(curl -s "$BASE/posts/$NEW_POST_ID")
ITEM_A_ID=$(echo "$POST_DETAIL" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d['data']['items'][0]['id'])" 2>/dev/null)
ITEM_B_ID=$(echo "$POST_DETAIL" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d['data']['items'][1]['id'])" 2>/dev/null)
check_nonempty "Item A ID" "$ITEM_A_ID"

# user1 applies to NEW_POST
APPLY=$(curl -s -w "\n%{http_code}" -X POST "$BASE/posts/$NEW_POST_ID/participations" \
  -H "Authorization: Bearer $U1_TOKEN" -H "Content-Type: application/json" \
  -d "{\"items\":[{\"postItemId\":$ITEM_A_ID,\"quantity\":1},{\"postItemId\":$ITEM_B_ID,\"quantity\":1}]}")
check_status "Apply for participation" "201" "$(echo "$APPLY" | tail -1)"
P_ID=$(echo "$APPLY" | head -1 | python3 -c "import sys,json; d=json.load(sys.stdin); print(d['data']['id'])" 2>/dev/null)
check "Participation has items" "items" "$(echo "$APPLY" | head -1)"
check_nonempty "Participation ID" "$P_ID"

# Duplicate participation → 409
DUP_APPLY=$(curl -s -w "\n%{http_code}" -X POST "$BASE/posts/$NEW_POST_ID/participations" \
  -H "Authorization: Bearer $U1_TOKEN" -H "Content-Type: application/json" \
  -d "{\"items\":[{\"postItemId\":$ITEM_A_ID,\"quantity\":1}]}")
check_status "Duplicate participation → 409" "409" "$(echo "$DUP_APPLY" | tail -1)"

# Author cannot apply to own post → 400
OWN_APPLY=$(curl -s -w "\n%{http_code}" -X POST "$BASE/posts/$NEW_POST_ID/participations" \
  -H "Authorization: Bearer $ACCESS" -H "Content-Type: application/json" \
  -d "{\"items\":[{\"postItemId\":$ITEM_A_ID,\"quantity\":1}]}")
check_status "Author applies own post → 400" "400" "$(echo "$OWN_APPLY" | tail -1)"

# List participations for post (by author)
PART_LIST=$(curl -s -w "\n%{http_code}" "$BASE/posts/$NEW_POST_ID/participations" \
  -H "Authorization: Bearer $ACCESS")
check_status "List post participations (author)" "200" "$(echo "$PART_LIST" | tail -1)"

# List participations for post (by non-author) → 403
PART_LIST_UNAUTH=$(curl -s -w "\n%{http_code}" "$BASE/posts/$NEW_POST_ID/participations" \
  -H "Authorization: Bearer $U2_TOKEN")
check_status "List participations non-author → 403" "403" "$(echo "$PART_LIST_UNAUTH" | tail -1)"

# Get my participations
MY_PART=$(curl -s -w "\n%{http_code}" "$BASE/my/participations" -H "Authorization: Bearer $U1_TOKEN")
check_status "Get my participations" "200" "$(echo "$MY_PART" | tail -1)"

# Non-author cannot approve → 403
NON_APPROVE=$(curl -s -w "\n%{http_code}" -X PUT "$BASE/posts/$NEW_POST_ID/participations/$P_ID/approve" \
  -H "Authorization: Bearer $U2_TOKEN")
check_status "Non-author approve → 403" "403" "$(echo "$NON_APPROVE" | tail -1)"

# Approve participation (by post author)
APPROVE=$(curl -s -w "\n%{http_code}" -X PUT "$BASE/posts/$NEW_POST_ID/participations/$P_ID/approve" \
  -H "Authorization: Bearer $ACCESS")
check_status "Approve participation" "200" "$(echo "$APPROVE" | tail -1)"
check "Status is APPROVED" "APPROVED" "$(echo "$APPROVE" | head -1)"

# Approve again → 400
DUP_APPROVE=$(curl -s -w "\n%{http_code}" -X PUT "$BASE/posts/$NEW_POST_ID/participations/$P_ID/approve" \
  -H "Authorization: Bearer $ACCESS")
check_status "Double approve → 400" "400" "$(echo "$DUP_APPROVE" | tail -1)"

# user2 applies (for reject test)
U2_APPLY=$(curl -s -w "\n%{http_code}" -X POST "$BASE/posts/$NEW_POST_ID/participations" \
  -H "Authorization: Bearer $U2_TOKEN" -H "Content-Type: application/json" \
  -d "{\"items\":[{\"postItemId\":$ITEM_A_ID,\"quantity\":1}]}")
check_status "User2 apply for participation" "201" "$(echo "$U2_APPLY" | tail -1)"
P2_ID=$(echo "$U2_APPLY" | head -1 | python3 -c "import sys,json; d=json.load(sys.stdin); print(d['data']['id'])" 2>/dev/null)

# Reject participation
REJECT=$(curl -s -w "\n%{http_code}" -X PUT "$BASE/posts/$NEW_POST_ID/participations/$P2_ID/reject" \
  -H "Authorization: Bearer $ACCESS")
check_status "Reject participation" "200" "$(echo "$REJECT" | tail -1)"
check "Status is REJECTED" "REJECTED" "$(echo "$REJECT" | head -1)"

# Cancel approved participation (user1 cancels P_ID)
CANCEL=$(curl -s -w "\n%{http_code}" -X DELETE "$BASE/posts/$NEW_POST_ID/participations/$P_ID" \
  -H "Authorization: Bearer $U1_TOKEN")
check_status "Cancel participation" "200" "$(echo "$CANCEL" | tail -1)"

# ========== SETTLEMENTS ==========
echo ""
echo "[4] Settlements"

# user1 re-applies after cancel
REAPPLY=$(curl -s -w "\n%{http_code}" -X POST "$BASE/posts/$NEW_POST_ID/participations" \
  -H "Authorization: Bearer $U1_TOKEN" -H "Content-Type: application/json" \
  -d "{\"items\":[{\"postItemId\":$ITEM_A_ID,\"quantity\":1}]}")
check_status "Re-apply after cancel" "201" "$(echo "$REAPPLY" | tail -1)"
P3_ID=$(echo "$REAPPLY" | head -1 | python3 -c "import sys,json; d=json.load(sys.stdin); print(d['data']['id'])" 2>/dev/null)

# Approve re-participation
RE_APPROVE=$(curl -s -w "\n%{http_code}" -X PUT "$BASE/posts/$NEW_POST_ID/participations/$P3_ID/approve" \
  -H "Authorization: Bearer $ACCESS")
check_status "Approve re-participation" "200" "$(echo "$RE_APPROVE" | tail -1)"

# Get settlements for post (by author)
POST_SETTLE=$(curl -s -w "\n%{http_code}" "$BASE/posts/$NEW_POST_ID/settlements" \
  -H "Authorization: Bearer $ACCESS")
check_status "Get settlements for post" "200" "$(echo "$POST_SETTLE" | tail -1)"
S_ID=$(echo "$POST_SETTLE" | head -1 | python3 -c "import sys,json; d=json.load(sys.stdin); items=d['data']; print(items[0]['id'] if items else '')" 2>/dev/null)
check_nonempty "Settlement ID from post" "$S_ID"

# Get my settlements (as user1)
MY_SETTLE=$(curl -s -w "\n%{http_code}" "$BASE/my/settlements" -H "Authorization: Bearer $U1_TOKEN")
check_status "Get my settlements" "200" "$(echo "$MY_SETTLE" | tail -1)"
check "My settlements has data" "participationId" "$(echo "$MY_SETTLE" | head -1)"

# Mark as paid
if [[ -n "$S_ID" && "$S_ID" != "None" && "$S_ID" != "null" && "$S_ID" != "" ]]; then
  # Post author marks settlement as paid (not the participant)
  PAID=$(curl -s -w "\n%{http_code}" -X PUT "$BASE/settlements/$S_ID/pay" \
    -H "Authorization: Bearer $ACCESS")
  check_status "Mark settlement as paid" "200" "$(echo "$PAID" | tail -1)"
  check "Settlement status PAID" "PAID" "$(echo "$PAID" | head -1)"

  # Pay again → 400
  PAID2=$(curl -s -w "\n%{http_code}" -X PUT "$BASE/settlements/$S_ID/pay" \
    -H "Authorization: Bearer $ACCESS")
  check_status "Double pay → 400" "400" "$(echo "$PAID2" | tail -1)"
else
  fail "Mark settlement as paid" "No settlement ID"
  fail "Double pay → 400" "No settlement ID"
fi

# ========== POST LIFECYCLE ==========
echo ""
echo "[5] Post lifecycle"

# Close post (by author) — PUT /api/posts/{id}/close
CLOSE=$(curl -s -w "\n%{http_code}" -X PUT "$BASE/posts/$NEW_POST_ID/close" \
  -H "Authorization: Bearer $ACCESS")
check_status "Close post" "200" "$(echo "$CLOSE" | tail -1)"
check "Post status CLOSED" "CLOSED" "$(echo "$CLOSE" | head -1)"

# Apply to closed post → 400
CLOSED_APPLY=$(curl -s -w "\n%{http_code}" -X POST "$BASE/posts/$NEW_POST_ID/participations" \
  -H "Authorization: Bearer $U2_TOKEN" -H "Content-Type: application/json" \
  -d "{\"items\":[{\"postItemId\":$ITEM_A_ID,\"quantity\":1}]}")
check_status "Apply to closed post → 400" "400" "$(echo "$CLOSED_APPLY" | tail -1)"

# Create another post for delete test
DEL_POST=$(curl -s -w "\n%{http_code}" -X POST "$BASE/posts" \
  -H "Authorization: Bearer $ACCESS" -H "Content-Type: application/json" \
  -d "{
    \"title\":\"삭제할 게시글\",
    \"description\":\"곧 삭제됩니다\",
    \"category\":\"DAILY\",
    \"meetLocation\":\"어딘가\",
    \"deadline\":\"$DEADLINE\",
    \"items\":[{\"name\":\"삭제품목\",\"totalPrice\":3000,\"maxParticipants\":2}]
  }")
check_status "Create post for delete" "201" "$(echo "$DEL_POST" | tail -1)"
DEL_POST_ID=$(echo "$DEL_POST" | head -1 | python3 -c "import sys,json; d=json.load(sys.stdin); print(d['data']['id'])" 2>/dev/null)

# Delete by non-author → 403
DEL_OTHER=$(curl -s -w "\n%{http_code}" -X DELETE "$BASE/posts/$DEL_POST_ID" -H "Authorization: Bearer $U1_TOKEN")
check_status "Delete by non-author → 403" "403" "$(echo "$DEL_OTHER" | tail -1)"

# Delete by author
DEL=$(curl -s -w "\n%{http_code}" -X DELETE "$BASE/posts/$DEL_POST_ID" -H "Authorization: Bearer $ACCESS")
check_status "Delete own post" "200" "$(echo "$DEL" | tail -1)"

# Verify deleted
AFTER_DEL=$(curl -s -w "\n%{http_code}" "$BASE/posts/$DEL_POST_ID")
check_status "Deleted post returns 404" "404" "$(echo "$AFTER_DEL" | tail -1)"

# ========== VALIDATION ==========
echo ""
echo "[6] Validation"

# Missing required fields → 400
BAD_REG=$(curl -s -w "\n%{http_code}" -X POST "$BASE/auth/register" \
  -H "Content-Type: application/json" -d '{"email":"bad@test.com"}')
check_status "Missing fields → 400" "400" "$(echo "$BAD_REG" | tail -1)"

# Invalid enum category → 400
BAD_CAT=$(curl -s -w "\n%{http_code}" "$BASE/posts?category=INVALID")
check_status "Invalid category → 400" "400" "$(echo "$BAD_CAT" | tail -1)"

# ========== SUMMARY ==========
echo ""
echo "======================================="
echo "  Results: $PASS passed, $FAIL failed"
echo "======================================="
