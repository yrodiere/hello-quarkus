--liquibase formatted sql

--changeset crm:1
create table customers (
    id  integer primary key,
    name varchar(255) not null,
    phone varchar(255) not null,
    email varchar(50) not null,
    uuid varchar(36) not null

);
create sequence customer_id_seq increment 1 start 1;

create table orders(
    id integer primary key,
    status varchar(255) not null,
    date_created timestamp not null,
    uuid varchar(36) not null,
    id_customer integer references customers(id)
);
create sequence order_id_seq increment 1 start 1;


create table products(
    id integer primary key,
    name varchar(255) not null,
    price decimal not null,
    category varchar(255) not null,
    description varchar(255),
    amount integer not null,
    date_created timestamp not null,
    image bytea,
    uuid varchar(36) not null
);
create sequence product_id_seq increment 1 start 1;
create unique index idx_name on products(name);


create table orders_products(
    product_id integer not null references products(id),
    order_id integer not null references orders(id),
    primary key(product_id, order_id)
);


