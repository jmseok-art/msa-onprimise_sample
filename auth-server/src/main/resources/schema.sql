create table if not exists oauth2_registered_client (
    id varchar(100) primary key,
    client_id varchar(100) not null,
    client_id_issued_at timestamp not null,
    client_secret varchar(200) default null,
    client_secret_expires_at timestamp default null,
    client_name varchar(200) not null,
    client_authentication_methods varchar(1000) not null,
    authorization_grant_types varchar(1000) not null,
    redirect_uris varchar(1000) default null,
    scopes varchar(1000) not null,
    client_settings varchar(2000) not null,
    token_settings varchar(2000) not null
);

create unique index if not exists idx_oauth2_client_id
    on oauth2_registered_client (client_id);
