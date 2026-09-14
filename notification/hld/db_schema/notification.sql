
Create table notification (
    id serial primary key,
    user_id int not null,
    type varchar(50) not null,
    title varchar(255) not null,
    message text not null,
    read_at timestamp default null,
    created_at timestamp default current_timestamp
);