

create table user_notification_preference (
    user_id int not null references notification(user_id),
    email_enabled boolean default true,
    sms_enabled boolean default true,
    push_enabled boolean default true,
    in_app_enabled boolean default true,
    dnd_start_time time default null,
    dnd_end_time time default null,
    created_at timestamp default current_timestamp
);