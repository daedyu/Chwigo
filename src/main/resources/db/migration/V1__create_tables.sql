-- chwigo 스키마 초기 생성

CREATE TABLE users (
    id          BIGINT(20)   NOT NULL AUTO_INCREMENT,
    created_at  DATETIME(6)  DEFAULT NULL,
    updated_at  DATETIME(6)  DEFAULT NULL,
    address     VARCHAR(255) DEFAULT NULL,
    email       VARCHAR(255) NOT NULL,
    nickname    VARCHAR(255) NOT NULL,
    password    VARCHAR(255) NOT NULL,
    refresh_token VARCHAR(512) DEFAULT NULL,
    role        ENUM('ROLE_ADMIN','ROLE_USER') NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE posts (
    id            BIGINT(20)   NOT NULL AUTO_INCREMENT,
    created_at    DATETIME(6)  DEFAULT NULL,
    updated_at    DATETIME(6)  DEFAULT NULL,
    category      ENUM('CLOTHING','DAILY','ELECTRONICS','FOOD','OTHER') NOT NULL,
    deadline      DATETIME(6)  DEFAULT NULL,
    description   TEXT         DEFAULT NULL,
    meet_location VARCHAR(255) DEFAULT NULL,
    status        ENUM('CLOSED','FULL','OPEN') NOT NULL,
    title         VARCHAR(255) NOT NULL,
    author_id     BIGINT(20)   NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_posts_author FOREIGN KEY (author_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE post_items (
    id                   BIGINT(20)    NOT NULL AUTO_INCREMENT,
    created_at           DATETIME(6)   DEFAULT NULL,
    updated_at           DATETIME(6)   DEFAULT NULL,
    current_participants INT(11)       NOT NULL DEFAULT 0,
    max_participants     INT(11)       NOT NULL,
    name                 VARCHAR(255)  NOT NULL,
    total_price          DECIMAL(15,2) NOT NULL,
    post_id              BIGINT(20)    NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_post_items_post FOREIGN KEY (post_id) REFERENCES posts (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE participations (
    id         BIGINT(20)  NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) DEFAULT NULL,
    updated_at DATETIME(6) DEFAULT NULL,
    status     ENUM('APPROVED','PENDING','REJECTED') NOT NULL,
    post_id    BIGINT(20)  NOT NULL,
    user_id    BIGINT(20)  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_participations (post_id, user_id),
    CONSTRAINT fk_participations_post FOREIGN KEY (post_id) REFERENCES posts (id),
    CONSTRAINT fk_participations_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE participation_items (
    id               BIGINT(20)  NOT NULL AUTO_INCREMENT,
    created_at       DATETIME(6) DEFAULT NULL,
    updated_at       DATETIME(6) DEFAULT NULL,
    quantity         INT(11)     NOT NULL DEFAULT 1,
    participation_id BIGINT(20)  NOT NULL,
    post_item_id     BIGINT(20)  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_participation_items (participation_id, post_item_id),
    CONSTRAINT fk_participation_items_participation FOREIGN KEY (participation_id) REFERENCES participations (id),
    CONSTRAINT fk_participation_items_post_item     FOREIGN KEY (post_item_id)     REFERENCES post_items (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE settlements (
    id               BIGINT(20)    NOT NULL AUTO_INCREMENT,
    created_at       DATETIME(6)   DEFAULT NULL,
    updated_at       DATETIME(6)   DEFAULT NULL,
    amount           DECIMAL(15,2) NOT NULL,
    paid_at          DATETIME(6)   DEFAULT NULL,
    status           ENUM('PAID','PENDING') NOT NULL,
    participation_id BIGINT(20)    NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_settlements_participation (participation_id),
    CONSTRAINT fk_settlements_participation FOREIGN KEY (participation_id) REFERENCES participations (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
