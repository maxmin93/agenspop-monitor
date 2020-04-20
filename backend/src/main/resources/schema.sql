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
    cr_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP(),  -- create time
    up_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP()   -- update time
);
/*
truncate table event_qry;
insert into event_qry(datasource, query) values
('modern','g.V().hasLabel("person")'),
('modern','g.V().hasLabel("software")');
select * from event_qry;
*/

-- DROP TABLE IF EXISTS event_row;
CREATE TABLE IF NOT EXISTS event_row(
    id BIGINT AUTO_INCREMENT(101) PRIMARY KEY,
    qid INT NOT NULL,
    type ENUM('nodes','edges') NOT NULL,          -- nodes, edges
    labels ARRAY NOT NULL DEFAULT (),   -- labels
    ids ARRAY DEFAULT (),               -- ids
    edate DATE DEFAULT CURRENT_DATE(),
    etime TIMESTAMP DEFAULT CURRENT_TIMESTAMP()
);
/*
truncate table event_row;
insert into event_row(qid, type, labels, ids) values
(101, 'nodes', ('person'), ('modern_1','modern_2','modern_4','modern_6')),
(102, 'nodes', ('software'), ('modern_3','modern_5'));
select * from event_row;

select type, array_agg(labels) as labels, array_agg(ids) as ids
from event_row group by type;

select * from event_row where array_contains((1,2,103,104),id);
select * from event_row where edate = DATE '2020-04-20';
*/

-- DROP TABLE IF EXISTS event_stat;
CREATE TABLE IF NOT EXISTS event_stat(
    id INT AUTO_INCREMENT(101) PRIMARY KEY,
    edate DATE DEFAULT CURRENT_DATE(),
    qid INT NOT NULL DEFAULT 0,         -- 0 means total qids
    type ENUM('nodes','edges') NOT NULL,          -- nodes, edges
    labels ARRAY NOT NULL DEFAULT (),   -- labels : array of array
    row_cnt INT NOT NULL DEFAULT 0,     -- # of records
    ids_cnt INT NOT NULL DEFAULT 0      -- sum of ids of all records
);

/*
truncate table event_stat;
insert into event_stat(edate, qid, type, labels, row_cnt, ids_cnt) values
(DATE '2019-09-01', 101, 'nodes', (('person')), 10, 901),
(DATE '2019-09-01', 102, 'nodes', (('software')), 15, 651),
(DATE '2019-10-01', 101, 'nodes', (('person')), 10, 1001),
(DATE '2019-10-01', 102, 'nodes', (('software')), 15, 1051),
(DATE '2019-11-01', 101, 'nodes', (('person')), 10, 801),
(DATE '2019-11-01', 102, 'nodes', (('software')), 15, 751),
(DATE '2019-12-01', 101, 'nodes', (('person')), 10, 301),
(DATE '2019-12-01', 102, 'nodes', (('software')), 15, 251),
(DATE '2020-01-01', 101, 'nodes', (('person')), 10, 101),
(DATE '2020-01-01', 102, 'nodes', (('software')), 15, 151),
(DATE '2020-02-01', 101, 'nodes', (('person')), 10, 101),
(DATE '2020-02-01', 102, 'nodes', (('software')), 15, 151),
(DATE '2020-03-01', 101, 'nodes', (('person')), 10, 101),
(DATE '2020-03-01', 102, 'nodes', (('software')), 15, 151),
(DATE '2020-04-01', 101, 'nodes', (('person')), 10, 121),
(DATE '2020-04-01', 102, 'nodes', (('software')), 15, 31);

merge into event_stat(id, edate, qid, type, labels, row_cnt, ids_cnt)
select TRANSACTION_ID(), edate, qid, type, array_agg(labels), count(id), sum(array_length(ids))
from event_row
group by edate, qid, type
order by edate, qid, type;


-- array functions of h2 => http://www.h2database.com/html/functions.html#array_get
select qid, array_length(ids) as ids_size, array_contains(ids,'id303') as chk_tf, array_slice(ids,1,3) as parts from event;

*/
