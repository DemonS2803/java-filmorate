delete from film_likes;
delete from film_genres_mapper;
delete from film_genre;
delete from film_ratings;
delete from user_friends;
delete from users;
delete from films;

insert into film_genre(name) values
      ('Комедия'),
      ('Драма'),
      ('Мультфильм'),
      ('Триллер'),
      ('Документальный'),
      ('Боевик');

insert into film_ratings(name) values
    ('G'),
    ('PG'),
    ('PG-13'),
    ('R'),
    ('NC-17');
