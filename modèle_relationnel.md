Context :
On cherche a stocker toute les informations intéressante des utlisateurs en rapport avec les parties:
- Chaque utilisateur possède un ID (numéro unique), il possède un username (String de longeur maximale), le nombre de victoire (nombre), le nombre de partie jouer (nombre)

- Chaque parti possède un ID(numéro unique), il possède les ID de chaque utilisateur (nombre) qui ont participé a la parti ainsi que sa place. On va aussi stocker son équipe ainsi que les objets qu'il avait mis sur eux. la date de la partie

- Chaque partie est enregistrer dans un but de resistance a la panne, il faut donc stocker chaque état de la partie au fur et a mesure que cette dernière se déroule.
Pour ca il va falloir stocker :
  - l'équipe de chaque joueur qui contient :
    - ses unités avec leur objects et les position ( qu'ils soient sur le terrain ou sur le banc)
    - l'argent actuellement a disposition
    - le niveau actuel de l'équipe
    - la winstreak actuel
    - le shop actuel
    - les objects non équipés a disposition
    - la quantité de pv restante
  - le shop : le pool d'unités(regarder les fonctions et les vues)
  - résultat des combats ? (avec un historique de qui a affronter qui) 

changements :
``` JSON
{
  "_description": "Le joueur achète une unité depuis le shop",
  "id": ,
  "game_id": ,
  "participation_id": ,
  "event_type": "UNIT_BOUGHT",
  "event_version": ,
  "seq_number": ,
  "created_at": ,
  "payload": {
    "unit_id": "unit_yasuo",
    "patch_version": ,
    "shop_slot": 2,
    "cost": 4,
    "gold_before": 7,
    "placed_on_bench": true,
    "bench_slot": 0
}
```

snapshots :
``` JSON
{
"ROUND_END": {
      "id": "",
      "game_id": "",
      "participation_id": "",
      "last_changement_seq": ,
      "created_at": "",
      "state": {
        "meta": {
          "round": ,
          "stage": ,
          "patch_version": 
        },
        "player": {
          "participation_id": ,
          "user_id": ,
          "hp": 76,
          "gold": 9,
          "level": 4,
          "xp": 18,
          "xp_to_next_level": 22,
          "streak": {
            "type": "loss",
            "count": 2
          }
        },
        "board": [
          {
            "instance_id": ,
            "unit_id": "unit_yasuo",
            "patch_version": ,
            "tier": 2,
            "position_x": 3,
            "position_y": 1,
            "items": [
              {
                "item_id": "item_infinity_edge",
                "slot": 1,
                "patch_version": 
              }
            ],
          },
        ],
        "bench": [],
        "shop": [],
        "item_bench": [],
        "active_traits": [
          {
            "trait_id": "guerrier",
            "type": "classe",
            "unit_count": 1,
            "active_bonus": {
              "seuil": 1,
              "effet_id": "effet_hp"
            },
            "next_bonus": {
              "seuil": 2,
              "effet_id": "effet_hp_2"
            }
          }
        ]
      }
}
```

```
User(<u>id</u>, oauth_provider, oauth_sub, username)
    User.oauth_provider NOT NULL
    User.oauth_sub NOT NULL
    User.username NOT NULL
Unit(<u>id</u>, player_id, game_id, type)
    Unit(player_id, game_id) references Participation(player_id, game_id)
    Unit.player_id NOT NULL
    Unit.game_id NOT NULL
    Unit.type NOT NULL
Items(<u>unit_id</u>, <u>items</u>)
    Items.unit_id references Unit.id
Game(<u>id</u>, date),
    Game.date NOT NULL
Participation(<u>player_id</u>, <u>game_id</u>, rank)
    Participation.player_id references User.id
    Participation.game_id references Game.id
    Participation.rank NOT NULL
```

