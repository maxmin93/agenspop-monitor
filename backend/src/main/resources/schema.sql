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
CREATE INDEX IF NOT EXISTS idx_qry_deleted ON event_qry(delete_yn);
CREATE INDEX IF NOT EXISTS idx_qry_datasource ON event_qry(datasource);

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
CREATE INDEX IF NOT EXISTS idx_row_qid ON event_row(qid);
CREATE INDEX IF NOT EXISTS idx_row_edate ON event_row(edate);

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
CREATE INDEX IF NOT EXISTS idx_agg_qid ON event_agg(qid);
CREATE INDEX IF NOT EXISTS idx_agg_edate ON event_agg(edate);
