-- for postman tests passing only
-- delete from film_likes;
-- delete from film_genres_mapper;
-- delete from user_friends;
-- delete from users;
-- delete from films;

drop table film_likes if exists;
drop table film_genres_mapper if exists;
drop table film_genre if exists;
drop table user_friends if exists;
drop table users if exists;
drop table films if exists;

create table if not exists users (
    id bigserial primary key,
    email varchar(255) not null,
    login varchar(255) not null,
    name varchar(255),
    birthday date
);

create table if not exists films (
    id bigserial primary key,
    name varchar(255) not null,
    description varchar(200),
    release_date date,
    duration int check (duration > 0),
    rating int
);

create table if not exists user_friends (
    user_id bigint not null,
    friend_id bigint not null,
    primary key (user_id, friend_id),
    foreign key (user_id) references users(id),
    foreign key (friend_id) references users(id)
);

create table if not exists film_likes (
    film_id bigint not null,
    user_id bigint not null,
    primary key (film_id, user_id),
    foreign key (film_id) references films(id),
    foreign key (user_id) references users(id)
);

create table if not exists film_genre (
    id bigserial primary key,
    name varchar(100) not null
);

create table if not exists film_genres_mapper (
    film_id bigint not null,
    genre_id bigint not null,
    primary key (film_id, genre_id),
    foreign key (film_id) references films(id),
    foreign key (genre_id) references film_genre(id)
)