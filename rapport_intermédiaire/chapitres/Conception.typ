= Conception <conception>
Pour la partie conception, on va d’abord se concentrer sur la partie base de données pour mettre au clair toutes les données que j’ai besoin de stocker ainsi que les données que je veux stocker. Pour l’instant, on va mettre de côté l’objectif de faire un système de replay des parties, que l’on conceptualisera une fois le nécessaire conceptualisé.
#v(2em)

Disclaimer, ici beaucoup de choses sont dépendantes de mon jeu (comment il marche, ce qu’il est intéressant de stocker ou non), je vous invite donc à regarder le Game Design Document (GDD) présent dans l’annexe pour avoir une meilleure compréhension du jeu.

#v(2em)
#v(2em)
== Conception de la base de données
=== Les données statiques

Tout d'abord, pour qu'une partie se déroule sans soucis, il nous faut des données (unités, objets, etc..), et afin que le jeu puisse perdurer dans le temps et que je puisse ajouter ou retirer des unités, objets ou autres, je vais stocker ces données "static" sur ma base de données afin que je n'aie pas besoin d'accéder au code pour ça.

#v(2em)

On va donc s’intéresser aux données essentielles dont mon jeu a besoin pour faire fonctionner une partie. Pour cela, il m'est nécessaire de stocker :
- Les unités qui peuvent être jouées : leur nom, stats, sprite, etc..
- Les objets que les unités peuvent avoir : leur nom, sprite, etc..
- Les traits que les unités sont censées avoir ainsi que les bonus venant de ces traits
- Les effets qui décrivent les différents bonus et malus des traits, des objets et des unités.

#v(2em)

Enfin, comme les unités ont un certain pourcentage de chance d'apparaître en fonction du niveau des joueurs, il faut que je stocke ça. De plus, comme le jeu peut être voué à changer et à être équilibré, chaque catégorie individuelle aura le patch auquel elle appartient. Ça nous donne ceci :
#figure(
image("../images/DB-minimumPartie.png", width: 100%),
caption: [
Schéma EA Unité
]
) <EA-minimum>
#v(2em)

L'association permanent effect fera plus de sens plus tard, mais comme j'ai pour objectif de pouvoir ajouter des modificateurs permanents et que donc la source de cet effet ne fait peut-être plus partie de ton équipe, alors je dois le stocker.
#v(2em)
#v(2em)

=== La résistance aux pannes
Maintenant qu’on a les données “static”, qui ne bougeront qu’en cas de futurs patchs pour équilibrer le jeu. J'ai maintenant un souci dans le cas où un utilisateur perd sa connexion à mon serveur , je dois pouvoir lui renvoyer son état tel qu'il était avant sa déconnexion. \

Je pourrais donc stocker cette information sur le serveur, mais si mon serveur tombe en panne ou s'éteint entre-temps, je ne pourrais plus lui redonner cette information. Il faut donc que je la stocke dans ma base de données. Et au passage, comme je dois implémenter un système de replay, je vais regarder les deux.
#v(2em)

Car je vais considèrer les parties comme un enchaînement d'événements, j'aurai alors juste besoin de rejouer les événements pour faire mon replay. Et pour la tolérance aux pannes, c'est pareil. Donc l'objectif est de stocker les actions des joueurs, ici les événements.
#v(2em)
#v(2em)

==== Event Sourcing vs Relationnel
Pour ça, j'ai deux solutions : la première vers laquelle je suis parti, est d'utiliser le pattern d'event sourcing, et la deuxième solution est une base de données totalement relationnelle.

#v(2em)

Le pattern d'event sourcing correspond totalement à notre façon de voir les parties @eventSourcing, et si je l'utilise, cela me permettrait d'avoir un stockage et une architecture de ma DB, et le replay assez simplement. Le but ici serait de stocker tous mes événements et leurs données dans un JSON et de faire des screenshots de la fin des rounds, aussi stockés dans un JSON, pour permettre d'avoir l'état actuel d'une équipe plus rapidement.

#v(2em)

Un des plus gros problèmes que l'on a avec cette direction est que l'on perd la cohérence et l'intégrité des données fournies par les BD relationnelles. Il faudrait donc gérer cela dans le backend et c'est donc une chose en plus, sachant qu'il faudra aussi parser tous les JSON stockés si l'on veut les utiliser une fois récupérés depuis la base de données.

#v(2em)

Alors que le relationnel, lui, va nous rendre l'architecture de notre BD plus complexe, mais une fois la conception faite, on n'aura plus rien à faire de plus côté backend et notre système de replay et de tolérance aux pannes seront opérationnels.

#v(2em)

Je vais donc me tourner vers une BD entièrement relationnelle pour ce projet.
#v(2em)
#v(2em)

=== La tolérance aux pannes sans le replay
Maintenant que je sais que je vais faire ça en relationnel, il faut que je regarde comment conceptualiser la tolérance aux pannes et le système de replay.
#v(2em)
#v(2em)

==== Stockage des équipes
Pour ça, il me faudra :
- Stocker des informations des joueurs : pour qu'ils puissent se connecter et que je puisse les identifier
- Les équipes utilisées par les joueurs :
- leurs unités et leurs objets
- l'inventaire des objets que le joueur a
- les données actuelles de l'équipe (gold, lvl, pv, winstreak, placement, le shop actuel)

#v(2em)

J'ai donc toutes les données de l'équipe d'un joueur et je vais aussi stocker les anciens combats afin qu'un joueur puisse voir les derniers combats ainsi que le résultat et qui il a affronté.
#figure(
image("../images/DB - ScreenshotTeam.png", width: 100%),
caption: [
Schéma EA Team
]
) <EA-team>
#v(2em)
#v(2em)

==== Stockage des parties
Pour pouvoir récupérer d'une panne, il faut aussi que je sache à quelle partie l'équipe participait et il me faut les données de la partie, soit la partie, sa seed et le pool de champions actuel de la partie. Pour rendre les choses plus simples plus tard, les pools de champions sont séparés en fonction de la rareté des champions à l'intérieur de ces derniers.
#figure(
image("../images/DB - ScreenshotGame.png", width: 100%),
caption: [
Schéma EA Game
]
) <EA-game>
#v(2em)
#v(2em)

=== Tolérance aux pannes avec le replay
J'ai conceptualisé une BD qui est tolérante aux pannes en stockant l'état actuel du début de round, mais je perds toute action du round actuel lors de la panne. Voici le schéma complet jusqu'à présent.
#figure(
image("../images/DB - sansReplay.png", width: 100%),
caption: [
Schéma EA SansReplay
]
) <EA-sansreplay>
#v(2em)
#v(2em)

==== Description des actions utilisateurs
Maintenant que l’on a tout ça, il faut regarder le principe de replay des parties. Un autre gros avantage de ce replay, c'est que ça permet aux joueurs de se rendre compte de leurs erreurs et potentiellement de les corriger dans le futur.
#v(2em)

Cependant, pour cela, il faut comprendre un peu mieux comment se déroule une partie. Dans mon jeu, une partie va consister en plusieurs rounds qui se suivent. Chaque joueur va commencer la partie avec le même niveau d’exp et de gold ainsi qu’une unité de la rareté la plus basse aléatoire. Ensuite, il commence le premier round. Un round est composé de deux phases, chacune durant laquelle on peut faire des actions. Il y a la buying phase et lorsque le temps est écoulé, le joueur bascule dans la fighting phase où il doit affronter un adversaire au hasard.
#v(2em)

Voici un diagramme de séquence pour mieux comprendre les actions que l’on a le droit d’effectuer ainsi que ce que cela engendre :
#figure(
image("../images/DéroulementDeLaPartie.png", width: 100%),
caption: [
Diagramme de séquence des actions pendant un round
]
) <schéma-séquence>
#v(2em)
#v(2em)


==== Conception dans le schéma

Maintenant que nous comprenons mieux les actions des utilisateurs, il paraît évident qu’un système de stockage basé sur les événements réalisés par les utilisateurs est la voie à suivre. Pour cela, nous ne conserverons que les données essentielles à l’événement et qu’il n’est pas possible de retrouver autrement.
#v(2em)

Pour donner un exemple, lorsqu’on achète une unité, il faut payer le coût de cette unité. Cependant, il faudra déjà stocker dans la base de données quelle unité a été achetée ; il sera donc possible de retrouver son coût sans aucun problème lors de l’exécution de l’événement. Je n’ai donc pas besoin de stocker également le prix de l’unité dans l’événement.
#figure(
  image("../images/DB - replayParEvent.png", width: 100%),
  caption: [
    Schéma EA Event pour replay
  ]
) <EA-eventReplay>
#v(2em)
#v(2em)

=== Schéma final de la base de donnée
Maintenant que nous disposons d’une base de données offrant une tolérance aux pannes ainsi qu’un système de replay, tout en stockant les données "statiques" nécessaires au fonctionnement du jeu, nous obtenons le schéma suivant.
#figure(
  image("../images/DB - avecReplay.png", width: 100%),
  caption: [
    Schéma EA
  ]
) <EA-avecreplay>
#v(2em)
#v(2em)