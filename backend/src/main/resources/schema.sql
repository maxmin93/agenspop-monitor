CREATE TABLE IF NOT EXISTS users
(
    id     INT AUTO_INCREMENT PRIMARY KEY,
    name   VARCHAR(255) NOT NULL,
    login  VARCHAR(255) NOT NULL,
    email  VARCHAR(255) NOT NULL,
    avatar VARCHAR(255)
);

-- DROP TABLE IF EXISTS event_qry;
CREATE TABLE IF NOT EXISTS event_qry(
    id INT AUTO_INCREMENT(101) PRIMARY KEY,
    delete_yn BOOLEAN NOT NULL DEFAULT FALSE,
    active_yn BOOLEAN NOT NULL DEFAULT TRUE,
    datasource VARCHAR(100) NOT NULL,               -- datasource
    name VARCHAR(1000) NOT NULL,                    -- name
    script VARCHAR(2000) NOT NULL,                  -- gremlin query
    cr_date DATE DEFAULT CURRENT_DATE(),            -- create date
    up_date DATE DEFAULT CURRENT_DATE()             -- update date
);

-- DROP TABLE IF EXISTS event_row;
CREATE TABLE IF NOT EXISTS event_row(
    id BIGINT AUTO_INCREMENT(101) PRIMARY KEY,
    qid INT NOT NULL,
    type VARCHAR(10) NULL,                          -- DEFAULT 'unknown',
    labels VARCHAR(2000) NOT NULL DEFAULT '',       -- joinToString
    ids VARCHAR NOT NULL DEFAULT '',          -- joinToString
    ids_cnt INT NOT NULL DEFAULT 0,                 -- count of ids
    edate DATE DEFAULT CURRENT_DATE(),
    etime TIME DEFAULT CURRENT_TIME()
);

-- DROP TABLE IF EXISTS event_agg;
CREATE TABLE IF NOT EXISTS event_agg(
    id INT AUTO_INCREMENT(101) PRIMARY KEY,
    edate DATE DEFAULT CURRENT_DATE(),
    qid INT NOT NULL DEFAULT 0,                     -- 0 means total qids
    type VARCHAR(10) NULL,                          -- nodes, edges
    labels VARCHAR(2000) NOT NULL DEFAULT '',       -- joinToString
    row_cnt INT NOT NULL DEFAULT 0,                 -- # of records
    ids_cnt INT NOT NULL DEFAULT 0                  -- sum of count of ids
);
