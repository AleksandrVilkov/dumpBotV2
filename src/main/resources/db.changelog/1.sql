
DROP TABLE user_photo;
create table if not exists user_photo(
    telegram_user_id varchar not null,
    telegram_photo_id varchar not null,
    request_id uuid not null
);

DROP TABLE user_state;
create table if not exists user_state(
    telegram_id varchar not null,
    state varchar not null
);

DROP TABLE user_request;

create table if not exists user_request(
    id uuid primary key,
    telegram_login varchar not null,
    telegram_user_id varchar not null,
    text text,
    type varchar not null,
    user_approve boolean default false,
    admin_approve boolean default false,
    posted boolean default false,
    has_photo boolean default false,
    created_at bigint,
    tg_account_created_at bigint,
    deleted boolean default false,
    is_at_work boolean
)