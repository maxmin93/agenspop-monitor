/*
-------------------------------------------------
CREATE TABLE users (
                id SERIAL PRIMARY KEY,
                name VARCHAR(255) NOT NULL,
                login VARCHAR(255) NOT NULL,
                email VARCHAR(255) NOT NULL,
                avatar VARCHAR(255)
);

 */
truncate table users;

INSERT INTO users(`id`, `name`, `login`, `email`, `avatar`)
values (1, 'User no 1', 'user1', 'user1@users.com', 'user1.png'),
       (2, 'User no 2', 'user2', 'user2@users.com', 'user2.png'),
       (3, 'User no 3', 'user3', 'user3@users.com', 'user3.png'),
       (4, 'User no 4', 'user4', 'user4@users.com', 'user4.png');


/*
-------------------------------------------------
CREATE TABLE IF NOT EXISTS event_qry(
    id INT AUTO_INCREMENT(101) PRIMARY KEY,
    delete_yn BOOLEAN NOT NULL DEFAULT FALSE,
    active_yn BOOLEAN NOT NULL DEFAULT TRUE,
    datasource VARCHAR(100) NOT NULL,               -- datasource
    query VARCHAR(2000) NOT NULL,                   -- gremlin query
    cr_date DATE DEFAULT CURRENT_DATE(),            -- create date
    up_date DATE DEFAULT CURRENT_DATE()             -- update date
);
*/

truncate table event_qry;

insert into event_qry(datasource, query) values
('modern','g.V().hasLabel("person")'),
('modern','g.E().hasLabel("created")'),
('northwind','g.V().hasLabel("customer")'),
('northwind','g.V().hasLabel("product")'),
('northwind','g.V().hasLabel("order")'),
('northwind','g.E().hasLabel("purchased")'),
('northwind','g.E().hasLabel("sold")'),
('airroutes','g.V().hasLabel("airport")'),
('airroutes','g.E().hasLabel("route")');

-- select * from event_qry;

/*
-------------------------------------------------
CREATE TABLE IF NOT EXISTS event_row(
    id BIGINT AUTO_INCREMENT(101) PRIMARY KEY,
    qid INT NOT NULL,
    type ENUM('nodes','edges') NOT NULL,          -- nodes, edges
    labels ARRAY NOT NULL DEFAULT (),   -- labels
    ids ARRAY DEFAULT (),               -- ids
    edate DATE DEFAULT CURRENT_DATE(),
    etime TIME DEFAULT CURRENT_TIME()
);
*/

truncate table event_row;

insert into event_row(qid, type, labels, ids) values
(102, 'nodes', '["person"]', '["modern_1","modern_2","modern_4","modern_6"]'),
(103, 'nodes', '["software"]', '["modern_3","modern_5"]');

-- select id, qid, type, labels from event_row;

/*
-------------------------------------------------
CREATE TABLE IF NOT EXISTS event_agg(
    id INT AUTO_INCREMENT(101) PRIMARY KEY,
    edate DATE DEFAULT CURRENT_DATE(),
    qid INT NOT NULL DEFAULT 0,         -- 0 means total qids
    type ENUM('nodes','edges') NOT NULL,          -- nodes, edges
    labels ARRAY NOT NULL DEFAULT (),   -- labels : array of array
    row_cnt INT NOT NULL DEFAULT 0,     -- # of records
    ids_cnt INT NOT NULL DEFAULT 0      -- sum of ids of all records
);
*/

truncate table event_agg;

/*
insert into event_agg(edate, qid, type, labels, row_cnt, ids_cnt) values
(DATE '2019-09-01', 101, 'nodes', '["person"]', 10, 901),
(DATE '2019-09-01', 102, 'nodes', '["software"]', 15, 651),
(DATE '2019-10-01', 101, 'nodes', '["person"]', 10, 1001),
(DATE '2019-10-01', 102, 'nodes', '["software"]', 15, 1051),
(DATE '2019-11-01', 101, 'nodes', '["person"]', 10, 801),
(DATE '2019-11-01', 102, 'nodes', '["software"]', 15, 751),
(DATE '2019-12-01', 101, 'nodes', '["person"]', 10, 301),
(DATE '2019-12-01', 102, 'nodes', '["software"]', 15, 251),
(DATE '2020-01-01', 101, 'nodes', '["person"]', 10, 101),
(DATE '2020-01-01', 102, 'nodes', '["software"]', 15, 151),
(DATE '2020-02-01', 101, 'nodes', '["person"]', 10, 101),
(DATE '2020-02-01', 102, 'nodes', '["software"]', 15, 151),
(DATE '2020-03-01', 101, 'nodes', '["person"]', 10, 101),
(DATE '2020-03-01', 102, 'nodes', '["software"]', 15, 151),
(DATE '2020-04-01', 101, 'nodes', '["person"]', 10, 121),
(DATE '2020-04-01', 102, 'nodes', '["software"]', 15, 31);

merge into event_agg(id, edate, qid, type, labels, row_cnt, ids_cnt)
select TRANSACTION_ID(), edate, qid, type, array_agg(labels), count(id), sum(array_length(ids))
from event_row
group by edate, qid, type
order by edate, qid, type;
*/