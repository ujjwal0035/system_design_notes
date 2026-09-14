

create table notification_delivery (
    id serial primary key,
    notification_id int not null references notification(id),
    channel enum('email', 'sms', 'push', 'in_app') not null,
    status enum('pending', 'sent', 'failed') not null,
    retry_count int default 0,
    delivered_at timestamp default null,
    failed_reason text default null,
);