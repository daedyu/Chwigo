-- 초기 테스트 데이터
-- Claude AI를 활용하여 실제 자취생 공동구매 시나리오를 반영한 테스트 데이터 설계
-- 비밀번호: admin@chwigo.com → admin1234 / user1~3@chwigo.com → user1234!

INSERT INTO users (email, password, nickname, address, role, created_at, updated_at) VALUES
('admin@chwigo.com',
 '$2a$10$4qn4qpYtvehLodrX5jEw7OffPTMVmnKxMe8HuS5.K6DFHsZ6NM5Du',
 '관리자', '서울시 강남구', 'ROLE_ADMIN', NOW(), NOW()),
('user1@chwigo.com',
 '$2a$10$uFQwW4HLRnGnikwPwbenL.I0jLUfoQ/QiumHX644X480neaM9Be.O',
 '김자취', '서울시 마포구', 'ROLE_USER', NOW(), NOW()),
('user2@chwigo.com',
 '$2a$10$wY/YtOnqTs5A8n3w2tapuON15bmXrr24UTzU70d53fF.3W5wpn2.C',
 '홍공구', '서울시 마포구', 'ROLE_USER', NOW(), NOW()),
('user3@chwigo.com',
 '$2a$10$SFBAAXi7r7I.Ni6C2SCJG.tMee.4.y3GhlRPut9nNCvKR419jGO/i',
 '박분배', '서울시 용산구', 'ROLE_USER', NOW(), NOW());

-- user1(id=2) 작성: 계란+우유
INSERT INTO posts (title, description, category, status, meet_location, deadline, author_id, created_at, updated_at) VALUES
('계란 + 우유 같이 사실 분 (마포 홈플러스)',
 '홈플러스 계란 30구(4,900원)랑 연세우유 1L 6팩(5,400원) 함께 사실 분 구합니다.',
 'FOOD', 'OPEN', '마포구 홈플러스 정문 앞',
 DATE_ADD(NOW(), INTERVAL 3 DAY), 2, NOW(), NOW()),

-- user2(id=3) 작성: 양파
('양파 한 망 나눠 살 분 구해요',
 '이마트 양파 3kg 한망(3,500원) 2명이서 나눌 분 구합니다.',
 'FOOD', 'OPEN', '용산구 이마트 앞',
 DATE_ADD(NOW(), INTERVAL 2 DAY), 3, NOW(), NOW()),

-- admin(id=1) 작성: 코스트코 묶음
('코스트코 화장지 + 키친타월 공동구매',
 '코스트코 화장지 30롤(12,900원) + 키친타월 12롤(8,900원). 각 품목 3명이서 나눕니다.',
 'DAILY', 'OPEN', '양천구 코스트코 정문 앞',
 DATE_ADD(NOW(), INTERVAL 5 DAY), 1, NOW(), NOW());

-- post1 품목: 계란(최대3명) / 연세우유(최대2명)
INSERT INTO post_items (post_id, name, total_price, max_participants, current_participants, created_at, updated_at) VALUES
(1, '계란 30구',      4900.00, 3, 1, NOW(), NOW()),
(1, '연세우유 1L 6팩', 5400.00, 2, 0, NOW(), NOW()),
-- post2 품목: 양파
(2, '양파 3kg',       3500.00, 2, 0, NOW(), NOW()),
-- post3 품목: 화장지 / 키친타월
(3, '화장지 30롤',    12900.00, 3, 0, NOW(), NOW()),
(3, '키친타월 12롤',   8900.00, 3, 0, NOW(), NOW());

-- user2(id=3)가 post1(계란) 참여 → 승인
INSERT INTO participations (post_id, user_id, status, created_at, updated_at) VALUES
(1, 3, 'APPROVED', NOW(), NOW());

INSERT INTO participation_items (participation_id, post_item_id, quantity, created_at, updated_at) VALUES
(1, 1, 1, NOW(), NOW());

-- 정산 생성 (계란 단가: 4900/3 = 1633.33)
INSERT INTO settlements (participation_id, amount, status, created_at, updated_at) VALUES
(1, 1633.33, 'PENDING', NOW(), NOW());

-- user3(id=4)가 post1(우유) 참여 신청 → 대기 중
INSERT INTO participations (post_id, user_id, status, created_at, updated_at) VALUES
(1, 4, 'PENDING', NOW(), NOW());

INSERT INTO participation_items (participation_id, post_item_id, quantity, created_at, updated_at) VALUES
(2, 2, 1, NOW(), NOW());
