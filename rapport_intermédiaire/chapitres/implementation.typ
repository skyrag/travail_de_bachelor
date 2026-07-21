= Implémentation <implementation>
#v(2em)

== Configuration du projet
#v(2em)

Avant de commencer a coder nous nous somme d'abord occupez de configurer le project afin que tout soit bien.
#v(2em)
=== Répo
#v(2em)

En premier lieux, nous avions le souhait de poser un cadre afin d'instaurer une certaine rigueur dans le travail et les commits. Pour cela nous avons fait un répository git en s'inspirant de la pratique GitFlow avec ces différentes branche mais les plus importante, une pour les realease et une pour le développement afin de séparé les version propre des verions qui pourrait encore présenté des défauts.
#v(1em)

On a aussi souhaiter créer un CI a l'aide des githubs actions afin d'être sur qu'avant chaque merge de pull Request les test ainsi que le coverage soient fait automatique pour éviter des oublies. En ce qui concerne le workflow on s'est inspiré de ce que proposé github pour les application Play et on a rajouter jacoco pour le coverage sur les tests.

#v(2em)
=== docker
#v(2em)
 Nous avons aussi créer notre docker compose, comparer a lancer séparément chaque servie docker compose nous permet d'avoir une centralisation des logs ainsi que d'avoir un environnement commun reproductible ou les service peuvent simplement discuter en localhost. Ca nous permet aussi de simplement tout lancer d'un coup.

#v(2em)
=== application.config
#v(2em)

 En ce qui concerne la configuration de Play certain de nos service demande une certaine configuration notamment notre Db et Argon2. Tout est présent dans notre fichier de configuration de l'application et il y a aussi un .env pour des donner potentiellement plus sensible comme les secrets.
#v(1em)

 Dans notre application.config on a aussi déclarer deux pool de thread. En effet, on a deux moment ou l'on souhaiterais éxécuter du travail asynchrone sans forcément utiliser les threads principaux. Un premier pool est pour les connexions a la DB qui sont bloquante par nature et comme play est de nature très asynchrone bloquer le thread actuel pour attendre la réponse de la DB paraît insensé. Notre deuxième pool bien que plus discutable est pour la simulation des combats. Même si le système est pour l'instant pas très gourmand en ressource pour le calcul, il va très certainement dans le future prendre plus de temps et de ressource et nous n'avons pas envie que la simulation des combats impacte les performance du reste de l'application. C'est donc pour ca que l'on a ces deux thread pool plutot que de faires ces actions asynchrone sur le main pool.
#v(1em)

 Par rapport aux tailles des pools nous avons consulte ikariCp qui nous disais d'utiliser deux fois le nombre de coeurs plus un lecteur, mais cela etait certainement pour des machines qui ne faisant pas tourner d'autre services et qui faisait beaucoup de requêtes. Notre application a pour vocation de faire tourner la un docker compose avec toute les images dont on a besoin, notamment la DB et le backend pour l'instant. Nous avons donc décider d'être économe quant a l'attribution de nos ressources et l'on a arbitrairement donné 8 thread au pool de DB (contre un 33 conseiller par hikariCP sur notre machine actuel) et 8 pour notre pool de simulation. Nous sommes contiens du choix d'être plus économe est nous observerons si le besoin d'attribuer plus de ressource au pool de connexion se fait ressentir.

#v(2em)
== Authentification
#v(2em)

Nous avions en premier lieux pour but d'utiliser openIdConnect a la place de faire un système d'authentification afin de déléguer cet aspect de la sécurité qui n'est pas notre point fort. Cependant notre approche assez naive car même si le système marche bien, nous ne nous sommes pas rendu compte lros de nos recherche que cet approche demandais de faire approuvé par le verificateur tiers une page pour demander les informations ainsi que le scope des informations que l'on demanderais. Comme ce genre de démarche peut prendre un certain temps et que l'on souhaitait implémenter un système d'authentification. Nous nous sommes finalement tourner vers un système d'authentification a l'aide des form de Play, dans lequel on recolte les informations utilisateur que l'on stocke ensuite dans Notre DB.

#v(2em)
=== Hashing
#v(2em)

Comme nous avons décidez de nous occupez nous même de l'authentification il a fallut que l'on trouve un moyen de hasher les mot de passe de nos utilisateur afin de proposé un minimum de sécurité.
Plutôt que de s'essayer a une pratique qui nous est que très peu familière, nous avons donc décider d'opter pour un choix de sécurité et de simplicité, qui est de passer par une librairie de hashing entretenu et sécurisé. Pour cela, on s'est tourné vers un des standard de l'industrie qui est argon2 qui en plus de hasher et de vérifier les mot de passe a travers l'api c'est aussi un algorithme qui prend délibérement du temps et de la mémoire afin de rendre les attaques plus lente et gourmande.

#v(2em)
== Communication réseau
#v(2em)
=== Format des messages
#v(2em)
Les messages échangés entre le client et le serveur sont au format JSON et suivent une structure commune, composée d'un identifiant, d'un type et d'un payload :
```Json
json{
  "id": "",
  "type": "",
  "payload": {
    "entity1": "",
    "entity2": ""
  }
}
```
Le champ id permet d'identifier chaque action de manière unique, notamment pour pouvoir la bufferiser côté serveur et la retransmettre en cas de déconnexion, ou pour l'invalider en cas de triche détectée. Le champ type définit l'action concernée (BuyUnit, SellUnit, MoveUnit, GiveToUnit, FuseObject, RerollShop, BuyExp), et le contenu du payload varie selon ce type — par exemple, MoveUnit transporte l'identifiant de l'unité déplacée ainsi que sa nouvelle position, tandis que RerollShop et BuyExp n'ont pas besoin de payload.
#v(1em)

Chaque action envoyée par le client fait l'objet d'une validation côté serveur, indépendamment des vérifications déjà effectuées côté frontend, afin de se prémunir contre toute tentative de triche. Par exemple, pour un achat d'unité (BuyUnit), le serveur vérifie que l'unité est bien présente dans le shop actuel du joueur, que celui-ci dispose de l'argent nécessaire, et qu'il a la place pour l'accueillir dans son équipe. Le serveur répond ensuite par un message de type OK ou Error, référencant l'id du message d'origine, ce qui permet au client de savoir précisément quelle action a été acceptée ou rejetée.

#v(2em)
=== Le pont entre WebSocket et acteurs typés : BridgeActor
#v(2em)

Play expose les connexions WebSocket sous la forme d'acteurs classiques (AbstractActor), alors que le reste de notre logique métier repose sur des acteurs typés Pekko (AbstractBehavior), plus sûrs à l'utilisation puisqu'ils forcent un typage strict des messages échangés. Pour faire cohabiter les deux, nous avons introduit un BridgeActor, dont le seul rôle est de faire la traduction entre les deux mondes : chaque message JSON reçu sur la WebSocket est transmis tel quel au ConnexionActor typé correspondant sous la forme d'un IncomingMessage, et à l'inverse, la fermeture de la connexion (postStop) est traduite en un message ConnectionClosed envoyé au ConnexionActor. Cette séparation permet de garder toute la logique de gestion des connexions et des parties dans des acteurs typés, sans que le typage de Pekko ne soit contraint par l'API WebSocket de Play.

#v(2em)
=== Le ConnexionActor : représentant serveur d'un joueur connecté
#v(2em)

Chaque joueur connecté est représenté côté serveur par un ConnexionActor, qui a pour rôle de router les messages entrants vers la partie en cours (GameActor), de transmettre les messages sortants au client, et de gérer le cycle de vie de la connexion (heartbeat, déconnexion, reconnexion).
#v(1em)

Routage des messages entrants. À la réception d'un IncomingMessage, le ConnexionActor lit le champ type du JSON et traduit chaque action utilisateur en un message typé correspondant, transmis au GameActor de la partie en cours (par exemple, BuyUnit devient un GameActor.BuyingUnitMessage). C'est le GameActor, et non le ConnexionActor, qui porte la responsabilité de valider ces actions au regard de l'état de la partie.
#v(1em)

Heartbeat et détection de déconnexion. Un mécanisme de heartbeat envoie un message PING au client toutes les 30 secondes. Si le client ne répond pas par un PONG après trois envois consécutifs, la connexion est considérée comme interrompue et le processus de déconnexion est déclenché — ce mécanisme permet de détecter une perte de connexion silencieuse (par exemple une coupure réseau côté client qui ne fermerait pas proprement la WebSocket).
#v(1em)

Buffer de reconnexion. Lorsqu'une connexion est fermée, le ConnexionActor n'est pas immédiatement détruit : il conserve chaque message sortant dans un buffer (reconnectionBuffer), et démarre un minuteur de 5 minutes. Si le client se reconnecte dans ce délai (ReconnectMessage), le timer est annulé et l'acteur reprend son fonctionnement normal ; les messages accumulés dans le buffer peuvent alors être retransmis. Si les 5 minutes s'écoulent sans reconnexion, l'acteur s'arrête (GracefulStop) et se désenregistre du moniteur.
#v(1em)

Confirmation de réception (ACK). Chaque message envoyé au client est conservé dans le reconnectionBuffer jusqu'à ce qu'un message ACK portant le même id soit reçu, ce qui permet de le retirer du buffer. Ce mécanisme garantit qu'un message non confirmé sera bien retransmis en cas de reconnexion, plutôt que d'être perdu silencieusement.
#v(1em)

Buffer de fin de round. Un second buffer (endOfRoundBuffer) conserve les messages reçus après l'heure de fin du round en cours (endOfRoundTime). À l'arrivée du round suivant, ces messages en attente sont réinjectés avec un horodatage mis à jour, ce qui évite qu'une action envoyée juste avant la bascule entre deux phases ne soit perdue ou traitée dans le mauvais contexte temporel.

#v(2em)
=== Le GameActor : logique métier et validation
#v(2em)
Le GameActor représente une partie en cours et centralise toute la logique de validation des actions, la diffusion des changements aux autres joueurs, ainsi que le déroulement des rounds et des combats.
Validation des actions. Pour chaque action reçue (achat, vente, déplacement d'unité, etc.), le GameActor vérifie l'état actuel de l'équipe concernée avant de l'appliquer — par exemple, onBuyingUnitMessage vérifie que l'équipe existe, que l'unité demandée est valide, et que l'achat est possible, avant de répondre au joueur et de propager l'information aux autres participants de la partie via tellOtherUsers.
#v(1em)

Diffusion aux autres joueurs. Lorsqu'une action est validée, elle n'est pas seulement confirmée à son auteur : elle est également transmise à tous les autres joueurs de la partie sous forme de ChangesFromOtherUser, ce qui permet à chaque client de maintenir une vue à jour de l'état des équipes adverses.
#v(1em)

Déroulement des rounds. Le passage d'un round à l'autre est piloté par un minuteur Pekko (timers.startSingleTimer), déclenché au début de chaque round pour une durée fixe (ROUNDTIMEMS), à laquelle s'ajoute une marge (INPUTBUFFER) pour absorber les derniers messages en transit. À l'expiration du timer, le GameActor détermine les combats à simuler pour le round en cours en appariant aléatoirement les équipes encore en vie, à l'aide d'une seed dérivée de la seed globale de la partie (garantissant la reproductibilité de chaque combat).
#v(1em)

Simulation asynchrone des combats. Chaque combat est délégué au SimulationService de façon asynchrone (CompletionStage), ce qui permet au GameActor de continuer à traiter d'autres messages pendant que la simulation s'exécute sur son propre pool de threads dédié. Une fois le résultat disponible, il est transmis directement aux ConnexionActor des deux joueurs concernés, puis le GameActor en est informé via un EndOfFightMessage pour mettre à jour l'état des équipes (points de vie perdus, victoire/défaite) et vérifier les conditions de fin de partie.

#v(2em)
=== Le moniteur : accès centralisé aux acteurs
#v(2em)

Comme évoqué dans la partie conception, l'ActeurMonitor centralise l'accès aux ConnexionActor et aux GameActor actifs, sous la forme de deux listes parallèles synchronisées. Toute méthode qui crée, retrouve ou supprime un acteur (getOrCreateActorFromId, getActorFromId, removeByActor, createGame) est déclarée synchronized, ce qui garantit qu'un seul thread à la fois peut modifier ces listes et évite ainsi toute race condition en cas d'accès concurrent — par exemple si deux requêtes HTTP tentaient simultanément de récupérer ou créer l'acteur d'un même utilisateur.


#v(2em)
== Base de données
#v(2em)
=== Les triggers
#v(2em)
Comme évoqué dans la partie conception, plusieurs de nos entités (Effect, Event, Strategie, etc.) sont représentées en programmation orientée objet sous forme de hiérarchies de classes — par exemple, un Effect est soit un ScalingEffect, soit un StatChangingEffect. Une base de données relationnelle ne connaît cependant pas nativement la notion d'héritage : nous avons donc traduit chaque hiérarchie en plusieurs tables liées par clé étrangère, une table par niveau de la hiérarchie (par exemple effect, puis scaling_effect et stat_changing_effect, chacune référençant effect par son id).
#v(1em)

Le problème de cette traduction est qu'elle ne garantit, à elle seule, aucune cohérence : rien n'empêche, par exemple, d'insérer une ligne dans effect sans jamais créer la ligne fille correspondante dans scaling_effect ou stat_changing_effect, ce qui laisserait un effet "orphelin", sans comportement concret associé. De la même manière, nos règles métier (une unité ne peut porter plus de 3 objets, une équipe ne peut proposer plus de 5 unités dans son shop, une partie doit comporter exactement 5 pools) ne sont pas non plus exprimables par de simples contraintes SQL classiques, puisqu'elles portent sur un comptage de lignes liées plutôt que sur la valeur d'une seule colonne.
#v(1em)

Pour garantir ces règles au niveau de la base elle-même — plutôt que de faire reposer cette responsabilité uniquement sur le backend, ce qui laisserait la porte ouverte à des incohérences en cas d'accès concurrent ou d'oubli — nous avons utilisé des triggers (déclencheurs), sous la forme de CONSTRAINT TRIGGER PostgreSQL. Chaque trigger exécute une fonction plpgsql qui vérifie une règle précise après une insertion, une mise à jour ou une suppression, et lève une exception si la règle est violée, ce qui annule automatiquement la transaction en cours.
#v(1em)

Un point important de notre implémentation est que ces triggers sont déclarés DEFERRABLE INITIALLY DEFERRED : la vérification n'est pas effectuée immédiatement après chaque instruction, mais reportée à la fin de la transaction. Ce choix est nécessaire car nos vérifications d'héritage ou de comptage supposent souvent plusieurs insertions liées (par exemple, insérer la ligne event, puis sa ligne fille buy_unit_event) : si la contrainte était vérifiée immédiatement après la première insertion, elle échouerait systématiquement puisque la ligne fille n'existerait pas encore. En différant la vérification à la fin de la transaction, on s'assure que toutes les insertions liées ont eu le temps de se produire avant que la cohérence ne soit contrôlée.

#v(2em)
=== Deux catégories de règles vérifiées
#v(2em)
Vérification de l'héritage. Pour chaque hiérarchie (event/unit_event, effect/scaling_effect, etc.), une fonction dédiée compte, pour la ligne concernée, le nombre de lignes correspondantes existant dans chacune des tables filles possibles, et vérifie que ce total vaut exactement 1 — ni 0 (ligne orpheline, sans sous-type), ni plus de 1 (ligne appartenant à plusieurs sous-types à la fois, ce qui n'aurait pas de sens métier). Par exemple, check_event_inheritance s'assure qu'un event correspond à exactement une ligne parmi leveling_event, losing_health_event, changing_gold_event, changing_shop_event ou unit_event.
#v(1em)

Vérification de règles de comptage métier. D'autres triggers portent sur des règles propres au jeu plutôt qu'à la structure d'héritage : check_unit_objects empêche une unité de porter plus de 3 objets, check_team_shop_units limite le shop d'une équipe à 5 unités maximum, et check_game_pool/check_game_team garantissent qu'une partie est toujours associée au bon nombre de pools et d'équipes. Ces triggers s'exécutent après insertion ou suppression sur les tables de liaison concernées (instance_units_object, teams_shop, pool, team), ce qui permet de rejeter toute transaction qui laisserait la base dans un état métier invalide, indépendamment de la couche applicative.

#v(2em)
=== Création de la partie : un repository dédié pour une transaction unique
#v(2em)
Le lancement d'une partie nécessite de rassembler plusieurs types de données provenant de la base : l'ensemble des unités du jeu (pour constituer les pools de champions disponibles), ainsi que les comptes des joueurs participants (pour créer leurs équipes respectives). Comme évoqué dans la partie conception, ces données doivent être récupérées et assemblées en une seule transaction cohérente, afin d'éviter qu'une partie ne soit créée dans un état partiel ou incohérent — par exemple si une erreur survenait après la création des équipes mais avant celle des pools.
#v(1em)

Pour cela, nous avons isolé toute la logique de création dans un repository dédié, le GameCreationRepository, dont l'unique responsabilité est de construire une Game complète et prête à être jouée. Cette création se déroule en plusieurs étapes, exécutées au sein d'une même transaction :

    - Récupération des unités. Toutes les unités définies dans le jeu sont chargées depuis la base, afin de pouvoir constituer les pools de champions disponibles pour la partie.
    - Création des équipes. Pour chaque identifiant de joueur fourni, l'utilisateur correspondant est recherché en base. Une Team est alors créée pour chaque utilisateur, avec l'or de départ défini par la configuration du jeu (STARTINGGOLD).
    - Constitution des pools. Un Pool est créé pour chaque rareté d'unité existante (Rarity), puis chaque unité chargée à l'étape 1 est répartie dans le pool correspondant à sa rareté, sous la forme d'une PoolEntry initialisée avec le nombre d'exemplaires de départ (UNITSTARTINGPOOL).
#v(1em)

Une fois ces trois étapes réalisées, la Game — désormais associée à ses équipes et à ses pools — est persistée en une seule fois (em.persist(game)), la cascade de persistance de JPA se chargeant de sauvegarder également les entités liées (équipes, pools, entrées de pool).
#v(1em)

Isoler cette logique dans un repository spécifique plutôt que de la répartir entre plusieurs services présente deux avantages. D'une part, cela garantit que la création d'une partie reste atomique : toute la construction se fait dans le contexte d'une seule transaction JPA, si bien qu'une erreur à n'importe quelle étape (par exemple un utilisateur introuvable) annule l'ensemble de la création plutôt que de laisser une partie à moitié initialisée en base.
#v(1em)

 D'autre part, cela centralise en un seul endroit toute la connaissance de "ce qu'il faut pour démarrer une partie", ce qui facilite l'évolution de cette logique si de nouveaux éléments devaient être initialisés au lancement d'une partie (par exemple des objets ou des règles spécifiques à un mode de jeu).
#v(1em)

La création d'une partie n'a pas été mis dans le gameRepository car nous avons estimé que c'est un comportement qui n'a pas lieux d'être disponible a une parti et ne doit être utiliser qu'a une seul endroit d'ou le fait d'isoler ce comportement.

#v(2em)
== Algorithmes
#v(2em)

=== Explication d'$A^*$ pour le pathfinding
#v(2em)
Lors de la simulation des combats, chaque unité doit soit attaquer, soit se déplacer jusqu'à se trouver à portée d'une cible. Il nous fallait donc un algorithme de pathfinding capable de calculer le chemin le plus court entre une unité et sa cible. Afin de ne pas frustrer le joueur en lui montrant une unité emprunter un chemin visiblement non optimal, nous avons choisi l'algorithme $A^*$, un standard largement utilisé pour ce type de problème, notamment dans le jeu vidéo.

#v(2em)
==== Rappel du fonctionnement d'$A^*$
#v(2em)
$A^*$ est un algorithme de recherche de chemin qui explore un graphe (ici, notre grille de cases) en cherchant à atteindre une case d'arrivée depuis une case de départ, tout en minimisant le coût total du trajet. Sa particularité, par rapport à un algorithme de parcours plus naïf comme un simple parcours en largeur (BFS), est qu'il priorise intelligemment les cases à explorer plutôt que de les explorer toutes indifféremment.
Pour cela, $A^*$ attribue à chaque case explorée un score f, calculé comme la somme de deux valeurs :
#v(1em)

g : le coût réel du chemin déjà parcouru depuis le départ pour atteindre cette case (dans notre cas, simplement le nombre de cases traversées, chaque déplacement coûtant 1).
h : une estimation du coût restant pour atteindre l'arrivée depuis cette case, appelée heuristique. Cette estimation doit être admissible, c'est-à-dire qu'elle ne doit jamais surestimer le coût réel restant, sous peine de produire un chemin qui ne soit plus garanti optimal.
#v(1em)

À chaque étape, l'algorithme retire de la liste des cases à explorer (la open list) celle ayant le score f le plus bas, et explore ses voisines. Une fois qu'une case a été explorée, elle est marquée comme définitivement traitée (la closed list) et ne sera plus reconsidérée, sauf si un chemin strictement plus court vers elle est découvert par la suite. L'algorithme s'arrête dès que la case d'arrivée est retirée de la open list, garantissant qu'aucun chemin plus court n'existe. Le chemin final est ensuite reconstruit en remontant, de case en case, le "parent" de chaque case jusqu'au point de départ.
#v(1em)

C'est précisément cette combinaison entre coût réel déjà parcouru (g) et estimation du coût restant (h) qui rend $A^*$ à la fois complet (il trouve toujours un chemin s'il en existe un) et efficace (il évite d'explorer inutilement des cases qui s'éloignent manifestement de la destination), contrairement à un algorithme comme Dijkstra qui, en l'absence d'heuristique, explore le graphe de façon plus large et donc moins ciblée.
#v(2em)
==== Représentation de la grille hexagonale
#v(2em)
Notre terrain de combat est hexagonal, ce qui complique légèrement l'application d'$A^*$, pensé à l'origine pour des grilles carrées. Nous avons choisi de représenter chaque case selon un système de coordonnées cubiques (x, y, z, avec la contrainte x + y + z = 0), une représentation classique pour les grilles hexagonales qui simplifie grandement le calcul des voisins d'une case (six directions fixes) ainsi que la distance entre deux cases, calculée simplement comme le maximum des différences absolues sur chacun des trois axes .

#figure(
image("../images/hexagone.png", width: 100%),
caption: [
schéma explicatif des rprésentation cubique et classique
]
) <hexagone>

Cette représentation cubique coexiste avec une représentation en coordonnées "offset" (col, row), plus intuitive pour l'affichage 2D côté frontend et pour l'indexation de notre grille en mémoire (un simple tableau à deux dimensions Tile[][]). Un jeu de fonctions de conversion (offsetToCube et cubeToOffset) permet de passer de l'une à l'autre selon le besoin.
(schéma à insérer ici : illustration de la grille hexagonale avec les deux systèmes de coordonnées, offset et cubique, côte à côte)
#v(2em)
==== Notre implémentation
#v(2em)
Chaque case de la grille (Tile) porte directement l'état nécessaire à la recherche A (g, h, parent, état de recherche), plutôt que de passer par une structure de données externe (comme une Map). Ce choix simplifie l'implémentation, mais impose une précaution : puisque l'état est stocké sur les cases elles-mêmes, une recherche pourrait "polluer" l'état laissé par la précédente. Pour l'éviter, chaque appel à findPath conserve la liste des cases qu'il a effectivement modifiées et les réinitialise systématiquement à la fin de son exécution, plutôt que de devoir parcourir l'intégralité de la grille à chaque fois — un compromis qui garde de bonnes performances tout en assurant que chaque recherche reparte d'un état propre.

L'heuristique h utilisée est la distance hexagonale définie plus haut, qui est admissible puisqu'elle correspond exactement au nombre minimal de déplacements nécessaires en l'absence d'obstacle — elle ne peut donc jamais surestimer le coût réel restant.
(schéma à insérer ici : exemple pas-à-pas d'un calcul de chemin sur quelques cases, avec les valeurs g/h/f affichées, pour rendre le fonctionnement concret visuellement)

#v(2em)
=== Service de Matchmaking
#v(2em)

Le matchmaking doit maintenir une liste des joueurs en attente ainsi que leur connexion associée, afin de pouvoir démarrer une partie une fois qu'un nombre suffisant de joueurs est réuni.
#v(1em)

Les contrôleurs de Play sont par nature concurrents, et c'est déjàa un problème que nous avons rencontré lorsque l'on a vu notre *ActeurMonitor*.
#v(1em)

Nous avons donc opté pour un MatchmakingService singleton, dans lequel les méthodes d'ajout d'un joueur (addPlayer) et de tentative de création de partie (tryCreateGame) sont déclarées synchronized.
#v(1em)

Ce choix garantit qu'un seul thread à la fois peut exécuter l'ensemble de la séquence "ajouter un joueur à la file d'attente, puis vérifier si le seuil de 8 joueurs est atteint et créer la partie le cas échéant" — la vérification du seuil et la création de la partie se font donc de manière atomique du point de vue des autres contrôleurs, ce qui élimine tout risque de créer deux parties simultanément ou d'assigner un même joueur à deux parties différentes.
#v(1em)

Ce même principe de synchronisation centralisée est également celui déjà utilisé dans l'ActeurMonitor pour la gestion des connexions actives — le MatchmakingService s'appuie d'ailleurs directement sur ce dernier (monitor.getActorFromId, monitor.createGame) pour retrouver la connexion d'un joueur et déléguer la création effective de la partie une fois le quota atteint.


#v(2em)
=== Service de SeedMaker
#v(2em)
Comme expliqué précédemment, notre jeu repose sur de nombreux mécanismes aléatoires, mais nous souhaitons pouvoir proposer un système de replay des parties. Pour que ce dernier soit fiable, il est nécessaire de stocker les graines (seeds) utilisées, afin que chaque décision aléatoire, une fois rejouée, mène exactement aux mêmes conséquences.

#v(2em)
==== Le problème de l'ordre des événements
#v(2em)
La principale difficulté rencontrée dans notre conceptualisation initiale provient de la façon dont nous ordonnons les événements. Chaque événement possède un step qui définit son ordre de passage, mais ce step est propre à chaque équipe : rien ne permet donc de connaître l'ordre relatif entre deux événements d'un même round appartenant à des équipes différentes.

Ce manque d'ordre global pose un problème concret pour tout événement dépendant de l'aléatoire et calculé côté serveur — le reroll du shop, par exemple, est calculé par le GameActor, qui détermine alors quels champions seront proposés à partir de la seed. Si l'ordre entre deux rerolls d'équipes différentes n'est pas connu lors du replay, la seed risque d'être consommée dans un ordre différent de celui de la partie originale, ce qui produirait des résultats différents et romprait le déterminisme recherché.
#v(2em)
==== Solutions envisagées
#v(2em)
Une première option aurait été d'introduire un step global, commun à toutes les équipes plutôt que propre à chacune. Cette solution aurait cependant nécessité que chaque round ait connaissance du step global courant, ce qui implique soit une synchronisation de ce step entre toutes les équipes, soit sa prise en charge centralisée par la Game elle-même — dans les deux cas, une complexité supplémentaire non négligeable dans la gestion de l'état de la partie.
#v(1em)

Nous avons donc préféré résoudre le problème autrement : plutôt que d'imposer un ordre global entre les équipes, nous avons choisi de dériver une seed distincte par équipe à partir de la seed globale de la partie, de façon déterministe. Ainsi, tout événement aléatoire propre à une équipe dépend uniquement de la seed de cette équipe, elle-même reproductible à l'identique à chaque replay. L'ordre entre les événements de deux équipes différentes n'a alors plus d'importance, puisque leurs séquences aléatoires respectives sont totalement indépendantes l'une de l'autre.
#v(1em)

Ce même problème se posait pour la simulation des combats, ceux-ci reposant également sur de l'aléatoire (notamment pour les coups critiques). Nous avons appliqué la même solution : une seed dédiée est dérivée de la seed de la partie pour chaque combat, ce qui garantit sa reproductibilité indépendamment de l'ordre dans lequel les combats sont traités.
#v(2em)
==== Génération des seeds dérivées
#v(2em)
Pour que les seeds dérivées soient suffisamment différentes les unes des autres et ne présentent pas de motif prévisible, chaque seed est générée à partir d'un contexte — une chaîne de caractères décrivant brièvement son usage (par exemple l'identifiant de l'équipe ou du combat concerné) — qui est ensuite hashé de façon déterministe pour produire la seed finale.
#v(1em)

Nous n'avons pas pu réutiliser notre service de hashing basé sur Argon2 pour cet usage : cet algorithme est volontairement conçu pour être lent et gourmand en ressources (afin de ralentir les attaques par force brute sur les mots de passe), ce qui le rend inadapté à une génération de seed devant rester rapide, et il nécessite en outre un sel non réutilisable tel quel dans un contexte déterministe. Nous nous sommes donc tournés vers SHA-256, une fonction de hash cryptographique rapide et surtout parfaitement déterministe : pour une même entrée, elle produit toujours exactement la même sortie, ce qui est précisément la propriété recherchée pour garantir la reproductibilité de nos seeds lors d'un replay.