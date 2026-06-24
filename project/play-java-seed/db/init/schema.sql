/*
CREATE TYPE rarity AS ENUM ('COMMON', 'UNCOMMON', 'RARE', 'EPIC', 'LEGENDARY');

CREATE TYPE tuple AS (x INTEGER, y INTEGER);

CREATE TABLE shop_level (
                            lvl int NOT NULL,
                            patch_version VARCHAR(20) NOT NULL,
                            common_chances int NOT NULL,
                            uncommon_chances int NOT NULL,
                            rare_chance int NOT NULL,
                            epic_chance int NOT NULL,
                            legendary_chances int NOT NULL,
                            created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                            PRIMARY KEY (lvl, patch)
);
*/

-- solution du regex pour valider l'email trouver  : https://dba.stackexchange.com/questions/68266/what-is-the-best-way-to-store-an-email-address-in-postgresql/165923#165923
CREATE EXTENSION citext;
CREATE DOMAIN email AS citext
    CHECK ( value ~ '^[a-zA-Z0-9.!#$%&''*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$' );

-- Users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    surname VARCHAR(30),
    name VARCHAR(30),
    username VARCHAR(50) NOT NULL UNIQUE,
    email email NOT NULL UNIQUE,
    password_hash VARCHAR(255),
    oauth_provider TEXT,
    oauth_sub TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT auth_method_check
        CHECK (
            password_hash IS NOT NULL
            OR oauth_provider IS NOT NULL
        ),
    CONSTRAINT oauth_pair_check
        CHECK (
            (oauth_provider IS NULL AND oauth_sub IS NULL)
            OR
            (oauth_provider IS NOT NULL AND oauth_sub IS NOT NULL)
        ),
    CONSTRAINT oauth_unique
        UNIQUE (oauth_provider, oauth_sub)
);
/*
CREATE TABLE game (
    id BIGINT PRIMARY KEY,
    patch_version VARCHAR(20) NOT NULL,
    seed BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
);

CREATE TABLE pool (
    id BIGINT PRIMARY KEY,
    game_id BIGINT NOT NULL,
    pools_rarity rarity NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_pool_game FOREIGN KEY (game_id) REFERENCES game(id) ON DELETE CASCADE,

);

CREATE TABLE unit (
    id BIGINT PRIMARY KEY,
    patch_version VARCHAR(20) NOT NULL,
    name VARCHAR(30) NOT NULL,
    -- art ? TODO
    -- icon ? TODO
    cost int NOT NULL,
    ability_name VARCHAR(30) NOT NULL,
    ability_description TEXT NOT NULL,
    max_health int NOT NULL,
    starting_mana int NOT NULL,
    max_mana int NOT NULL,
    base_attack int NOT NULL,
    attack_speed float NOT NULL,
    armor int NOT NULL,
    magic_resist int NOT NULL,
    range int NOT NULL,
    -- TODO rajouter les traits
    -- TODO rajouter les effect de compétence et les effets permanents.
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
);

-- TODO implement unit
CREATE TABLE pool_entry (
    pool_id BIGINT NOT NULL,
    number int NOT NULL,
    unit_id BIGINT NOT NULL,
    PRIMARY KEY (pool_id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_pool_entry_pool FOREIGN KEY (pool_id) REFERENCES pool(id) ON DELETE CASCADE,
    CONSTRAINT fk_pool_entry_unit FOREIGN KEY (unit_id) REFERENCES unit(id) ON DELETE CASCADE,
);

CREATE TABLE team (
    user_id BIGINT NOT NULL,
    game_id BIGINT NOT NULL,
    rank int NOT NULL,
    winstreak int NOT NULL,
    health int NOT NULL,
    lvl int NOT NULL,
    gold int NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_team_user FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    CONSTRAINT fk_team_game FOREIGN KEY (game_id) REFERENCES game(id) ON DELETE CASCADE,
);

CREATE TABLE teams_shop (
    unit_id BIGINT NOT NULL,
    team_id BIGINT NOT NULL,
    PRIMARY KEY (unit_id, team_id),
    CONSTRAINT fk_teams_shop_unit FOREIGN KEY (unit_id) REFERENCES unit(id) ON DELETE CASCADE,
    CONSTRAINT fk_teams_shop_team FOREIGN KEY (team_id) REFERENCES team(id) ON DELETE CASCADE,
);

CREATE TABLE instance_unit (
    id BIGINT PRIMARY KEY,
    lvl int NOT NULL,
    pos tuple NOT NULL,
    unit_id BIGINT NOT NULL,
    team_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_instance_unit_unit FOREIGN KEY (unit_id) REFERENCES unit(id) ON DELETE CASCADE,
    CONSTRAINT fk_instance_unit_team FOREIGN KEY (team_id) REFERENCES team(id) ON DELETE CASCADE,
);

CREATE TABLE object (
    id BIGINT PRIMARY KEY,
    -- TODO icon
    patch_version VARCHAR(20) NOT NULL,
    name VARCHAR(30) NOT NULL,
    description TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
);

 */