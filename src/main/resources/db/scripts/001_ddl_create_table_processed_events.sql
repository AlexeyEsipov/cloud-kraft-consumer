create table if not exists processed_events
(
    id        serial primary key,
    message_id varchar,
    product_id varchar
);