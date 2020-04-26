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
    query VARCHAR(2000) NOT NULL,                   -- gremlin query
    cr_date DATE DEFAULT CURRENT_DATE(),            -- create date
    up_date DATE DEFAULT CURRENT_DATE()             -- update date
);
--select * from event_qry;

-- DROP TABLE IF EXISTS event_row;
CREATE TABLE IF NOT EXISTS event_row(
    id BIGINT AUTO_INCREMENT(101) PRIMARY KEY,
    qid INT NOT NULL,
--    type ENUM('nodes','edges') NOT NULL DEFAULT 'nodes',
    type VARCHAR(10) NOT NULL DEFAULT 'nodes',
--    labels ARRAY NOT NULL DEFAULT (),   -- labels
--    ids ARRAY NOT NULL DEFAULT (),      -- ids
    labels VARCHAR(2000) NOT NULL DEFAULT '[]',   -- json(List)
    ids VARCHAR(5000) NOT NULL DEFAULT '[]',            -- json(List)
    edate DATE DEFAULT CURRENT_DATE(),
    etime TIME DEFAULT CURRENT_TIME()
);

--select type, array_agg(labels) as labels, array_agg(ids) as ids from event_row group by type;
--select * from event_row where array_contains((1,2,103,104),id);
--select * from event_row where edate = DATE '2020-04-20';

-- DROP TABLE IF EXISTS event_agg;
CREATE TABLE IF NOT EXISTS event_agg(
    id INT AUTO_INCREMENT(101) PRIMARY KEY,
    edate DATE DEFAULT CURRENT_DATE(),
    qid INT NOT NULL DEFAULT 0,         -- 0 means total qids
    type VARCHAR(10) NOT NULL,          -- nodes, edges
--    labels ARRAY NOT NULL DEFAULT (),   -- labels : array of array
    labels VARCHAR(2000) NOT NULL DEFAULT '[]',   -- json(List)
    row_cnt INT NOT NULL DEFAULT 0,     -- # of records
    ids_cnt INT NOT NULL DEFAULT 0      -- sum of ids of all records
);

--merge into event_agg(id, edate, qid, type, labels, row_cnt, ids_cnt)
--select TRANSACTION_ID(), edate, qid, type, array_agg(labels), count(id), sum(array_length(ids))
--from event_row
--group by edate, qid, type
--order by edate, qid, type;


-- array functions of h2 => http://www.h2database.com/html/functions.html#array_get
--select qid, array_length(ids) as ids_size, array_contains(ids,'id303') as chk_tf, array_slice(ids,1,3) as parts from event;
