truncate table users;

INSERT INTO users(`id`, `name`, `login`, `email`, `avatar`)
values (1, 'User no 1', 'user1', 'user1@users.com', 'user1.png'),
       (2, 'User no 2', 'user2', 'user2@users.com', 'user2.png'),
       (3, 'User no 3', 'user3', 'user3@users.com', 'user3.png'),
       (4, 'User no 4', 'user4', 'user4@users.com', 'user4.png');

----------------------------------------------------------------

truncate table event_qry;

insert into event_qry(datasource, name, script) values
('modern', 'new person', 'g.V().hasLabel("person")'),
('modern', 'EDGE: created', 'g.E().hasLabel("created")'),
('northwind', 'new customer', 'g.V().hasLabel("customer")'),
('northwind', 'new product', 'g.V().hasLabel("product")'),
('northwind', 'new order of USA', 'g.V().hasLabel("order").has("ship_country","USA")'),
('northwind', 'new purchased', 'g.E().hasLabel("purchased")'),
('northwind', 'EDGE: sold', 'g.E().hasLabel("sold")'),
('northwind', 'EDGE: ships', 'g.E().hasLabel("ships")');
--('airroutes', 'new airport', 'g.V().hasLabel("airport")'),
--('airroutes', 'EDGE: route', 'g.E().hasLabel("route")');

----------------------------------------------------------------

-- truncate table event_row;

----------------------------------------------------------------

-- truncate table event_agg;

--merge into event_agg(id, edate, qid, type, labels, row_cnt, ids_cnt)
--select TRANSACTION_ID(), edate, qid, type, array_agg(labels), count(id), sum(array_length(ids))
--from event_row
--group by edate, qid, type
--order by edate, qid, type;

----------------------------------------------------------------
