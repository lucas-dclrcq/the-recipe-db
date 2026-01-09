create table cookbook
(
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    id         uuid                        not null,
    author     varchar(255)                not null,
    title      varchar(255)                not null,
    primary key (id)
);

create table cookbook_index_page
(
    page_order   integer                     not null,
    created_at   timestamp(6) with time zone not null,
    updated_at   timestamp(6) with time zone not null,
    cookbook_id  uuid                        not null,
    id           uuid                        not null,
    content_type varchar(255)                not null,
    image_data   oid                         not null,
    primary key (id)
);

create table ingredient
(
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    id         uuid                        not null,
    name       varchar(255)                not null unique,
    primary key (id)
);

create table recipe
(
    page_number integer                     not null,
    created_at  timestamp(6) with time zone not null,
    updated_at  timestamp(6) with time zone not null,
    cookbook_id uuid                        not null,
    id          uuid                        not null,
    name        varchar(255)                not null,
    primary key (id)
);

create table recipe_ingredient
(
    ingredient_id uuid not null,
    recipe_id     uuid not null,
    primary key (ingredient_id, recipe_id)
);

alter table if exists recipe_ingredient
    add constraint FK9b3oxoskt0chwqxge0cnlkc29
        foreign key (ingredient_id)
            references ingredient;

alter table if exists recipe_ingredient
    add constraint FKgu1oxq7mbcgkx5dah6o8geirh
        foreign key (recipe_id)
            references recipe;
