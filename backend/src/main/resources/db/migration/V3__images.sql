-- Uploaded product / packaging photos. Kept in the database so they survive deploys
-- and are backed up with everything else (see IMAGES.md for the move to object storage).
create table images (
    id           uuid         primary key,
    content_type varchar(20)  not null,
    data         bytea        not null,
    created_at   timestamptz  not null default now()
);

-- URL of an uploaded image (/api/images/{id}); null means "show the emoji instead".
alter table products          add column image_url varchar(80);
alter table packaging_options add column image_url varchar(80);
