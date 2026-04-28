
CREATE TABLE users (
    id           SERIAL PRIMARY KEY,
    username     VARCHAR(50)  UNIQUE NOT NULL,
    email        VARCHAR(100) UNIQUE NOT NULL,
    password     VARCHAR(255) NOT NULL,
    total_score  INT DEFAULT 0,
    created_at   TIMESTAMP DEFAULT NOW()
);

CREATE TABLE submissions (
    id            SERIAL PRIMARY KEY,
    user_id       INT REFERENCES users(id) ON DELETE CASCADE,
    challenge_id  VARCHAR(50),
    code          TEXT NOT NULL,
    result        VARCHAR(20),
    submitted_at  TIMESTAMP DEFAULT NOW()
);

CREATE TABLE scores (
    id            SERIAL PRIMARY KEY,
    user_id       INT REFERENCES users(id) ON DELETE CASCADE,
    challenge_id  VARCHAR(50) NOT NULL,
    passed        BOOLEAN NOT NULL,
    points_earned INT NOT NULL DEFAULT 0,
    updated_at  TIMESTAMP DEFAULT NOW(),
	CONSTRAINT unique_user_challenge UNIQUE (user_id, challenge_id)
);