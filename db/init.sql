-- Выполняется один раз при первой инициализации volume db_data (пользователь postgres, база polls).

CREATE ROLE poll LOGIN PASSWORD 'poll_pass';
CREATE ROLE vote LOGIN PASSWORD 'vote_pass';

-- Каждая схема принадлежит своему пользователю: прав на чужую схему нет.
CREATE SCHEMA poll AUTHORIZATION poll;
CREATE SCHEMA vote AUTHORIZATION vote;

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA poll FROM PUBLIC;
REVOKE ALL ON SCHEMA vote FROM PUBLIC;

ALTER ROLE poll SET search_path = poll;
ALTER ROLE vote SET search_path = vote;
