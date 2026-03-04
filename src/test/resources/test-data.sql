delete from film_likes;
delete from user_friends;
delete from users;
delete from films;

-- reset sequences
alter table users alter column id restart with 1;
alter table films alter column id restart with 1;

-- Insert test users
insert into users (email, login, name, birthday) values
    ('user1@test.com', 'user1', 'User One', '1990-01-01'),
    ('user2@test.com', 'user2', 'User Two', '1991-02-02'),
    ('user3@test.com', 'user3', 'User Three', '1992-03-03');

-- Insert test films
insert into films (name, description, release_date, duration) values
    ('Film 1', 'Description 1', '2020-01-01', 120),
    ('Film 2', 'Description 2', '2021-02-02', 90),
    ('Film 3', 'Description 3', '2022-03-03', 150);

-- Add friends relationships
insert into user_friends (user_id, friend_id) values
    (1, 2),
    (1, 3),
    (2, 1);

-- Add film likes
insert into film_likes (film_id, user_id) values
    (1, 1),
    (1, 2),
    (2, 1);