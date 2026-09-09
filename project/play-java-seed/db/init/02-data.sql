INSERT INTO shop_level (
    lvl,
    patch_version,
    common_chances,
    uncommon_chances,
    rare_chance,
    epic_chance,
    legendary_chances,
    nextlvl
) VALUES
      (1, '0.1', 100, 0, 0, 0, 0, 2),
      (2, '0.1', 75, 25, 0, 0, 0, 4),
      (3, '0.1', 70, 30, 0, 0, 0, 6),
      (4, '0.1', 55, 40, 5, 0, 0, 10),
      (5, '0.1', 45, 40, 15, 0, 0, 16),
      (6, '0.1', 25, 45, 25, 5, 0, 26),
      (7, '0.1', 10, 20, 50, 20, 0, 42),
      (8, '0.1', 10, 20, 40, 28, 2, 68),
      (9, '0.1', 10, 15, 35, 40, 5, 110),
      (10, '0.1', 5, 15, 30, 40, 10, 178);


-- ============================
-- Insertion de "geralt" pour chaque rareté
-- ============================

-- COMMON
INSERT INTO unit (
    patch_version, sprite_key, name, cost, ability_name, ability_description,
    max_health, starting_mana, max_mana, base_attack, attack_speed,
    armor, magic_resist, range, rarity
)
VALUES (
           '0.1', 1, 'geralt', 1, 'exécution',
           'Inflige des dégâts magiques à la cible et la stun.',
           350, 0, 100, 35, 70, 15, 15, 1, 'COMMON'
       )
    RETURNING id;

-- UNCOMMON
INSERT INTO unit (
    patch_version, sprite_key, name, cost, ability_name, ability_description,
    max_health, starting_mana, max_mana, base_attack, attack_speed,
    armor, magic_resist, range, rarity
)
VALUES (
           '0.1', 1, 'geralt', 2, 'exécution',
           'Inflige des dégâts magiques à la cible et la stun.',
           500, 0, 100, 50, 80, 20, 20, 1, 'UNCOMMON'
       )
    RETURNING id;

-- RARE
INSERT INTO unit (
    patch_version, sprite_key, name, cost, ability_name, ability_description,
    max_health, starting_mana, max_mana, base_attack, attack_speed,
    armor, magic_resist, range, rarity
)
VALUES (
           '0.1', 1, 'geralt', 3, 'exécution',
           'Inflige des dégâts magiques à la cible et la stun.',
           650, 0, 100, 70, 90, 25, 25, 1, 'RARE'
       )
    RETURNING id;

-- EPIC
INSERT INTO unit (
    patch_version, sprite_key, name, cost, ability_name, ability_description,
    max_health, starting_mana, max_mana, base_attack, attack_speed,
    armor, magic_resist, range, rarity
)
VALUES (
           '0.1', 1, 'geralt', 4, 'exécution',
           'Inflige des dégâts magiques à la cible et la stun.',
           850, 0, 100, 95, 100, 30, 30, 1, 'EPIC'
       )
    RETURNING id;

-- LEGENDARY
INSERT INTO unit (
    patch_version, sprite_key, name, cost, ability_name, ability_description,
    max_health, starting_mana, max_mana, base_attack, attack_speed,
    armor, magic_resist, range, rarity
)
VALUES (
           '0.1', 1, 'geralt', 5, 'exécution',
           'Inflige des dégâts magiques à la cible et la stun.',
           1100, 0, 100, 130, 110, 35, 35, 1, 'LEGENDARY'
       )
    RETURNING id;

-- ============================
-- Insertion d'un objet
-- ============================
-- id et created_at sont générés automatiquement, pas besoin de les fournir

WITH new_object AS (
INSERT INTO object (patch_version, name, description)
VALUES ('1.0.0', 'Épée +10 ATK', 'Augmente les dégâts d''attaque de 10')
    RETURNING id
    ),
    new_effect AS (
INSERT INTO effect DEFAULT VALUES
    RETURNING id
    ),
    new_stat_effect AS (
INSERT INTO stat_changing_effect (id, type_change, value)
SELECT id, 'ATTACKDAMAGE', 10
FROM new_effect
    RETURNING id
    )
INSERT INTO object_effect (object_id, effect_id)
SELECT new_object.id, new_effect.id
FROM new_object, new_effect;