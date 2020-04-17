CREATE TABLE IF NOT EXISTS users
(
    id     INT AUTO_INCREMENT PRIMARY KEY,
    name   VARCHAR(255) NOT NULL,
    login  VARCHAR(255) NOT NULL,
    email  VARCHAR(255) NOT NULL,
    avatar VARCHAR(255)
);

/*
create table if not exists event(
id bigint AUTO_INCREMENT primary key,
qid int not null,
ids array[1000],
etime date default now()
);

truncate table event;

insert into event(qid, ids) values
(1, ('id101','id102','id103','id104','id105')),
(2, ('id201','id202','id203','id204','id205')),
(3, ('id301','id302','id303','id304','id305')),
(4, ('id401','id402','id403','id404','id405')),
(5, ('id501','id502','id503','id504','id505'));

-- array functions of h2 => http://www.h2database.com/html/functions.html#array_get
select qid, array_length(ids) as ids_size, array_contains(ids,'id303') as chk_tf, array_slice(ids,1,3) as parts from event;

*/
