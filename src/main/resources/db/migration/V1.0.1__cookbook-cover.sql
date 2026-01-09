alter table if exists cookbook
    add column if not exists cover_data oid,
    add column if not exists cover_content_type varchar(255);
