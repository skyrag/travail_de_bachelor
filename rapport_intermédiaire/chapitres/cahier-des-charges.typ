= Cahier des charges <cahier-des-charges>

== Description du projet
#v(2em)

Le projet a pour but de créer un jeu ayant un style similaire à des gros jeux déjà existants mais en y ajoutant un twist pour le rendre un peu unique. L'objectif n'est en aucun cas de créer quelque chose de révolutionnaire, mais de créer un jeu fonctionnel avec lequel les joueurs s'amuseront avec des personnages de la pop-culture. On cherche donc à réaliser un jeu jouable en ligne multijoueur auto-battler, où 8 personnes pourront s'affronter dans la même partie.
#v(2em)
#v(2em)

== Technologie utilisés
#v(2em)

Pour ce projet je vais utiliser le framework Play pour le développer, il aura donc pour objectif d'être jouable sur navigateur internet. Pour le stockage des données je vais utiliser le système de gestion de base de données PostgreSQL.
#v(2em)
#v(2em)

== Valeur d’ingénierie du projet
Tout l’intérêt du projet réside dans la réalisation d’un jeu multijoueur entièrement en ligne, accessible directement depuis un navigateur web et ne nécessitant aucune installation côté utilisateur.
#v(2em)

L’aspect multijoueur introduit des défis techniques importants, notamment la synchronisation d’état entre plusieurs clients, la gestion de la latence réseau, la cohérence des actions concurrentes et la fiabilité des communications. Le serveur doit également orchestrer plusieurs parties simultanées tout en garantissant l’intégrité des données et la continuité de l’expérience de jeu. Il faudra donc que le design de la Base de donnée permet une résistance a la panne.
#v(2em)

La conception de la base de donnée devra aussi permettre un système de replay de partie afin que l’on puisse rejouer entièrement des partie qui auront été enregistrer sur la base de donnée.
#v(2em)
#v(2em)

== Fonctionnalités attendues
Dans cette section, je décris les fonctionnalités qui seront attendues à la fin du projet. On listera aussi de potentielles fonctionnalités futures.
=== Must have
-	Un système de login, où chaque utilisateur pourra se connecter et regarder ses statistiques, soit le nombre de victoires dans un premier temps
-	Un système de connexion multijoueur, où l'on pourra connecter 8 personnes à la même partie afin qu'ils s'affrontent
-	Un système de stockage des données utilisateurs
-	Un système de replay de partie
-	Un système de partie opérationnel : donc où un joueur pourrait lancer une partie pour jouer contre d'autres joueurs ou des bots. Donc cela inclut :
    -	Un système de boutique pour acheter les unités
    -	Un système de combat qui fera s'affronter les unités de deux joueurs à intervalles réguliers
    -	Un système de gestion de son équipe afin que l'on puisse changer soit la formation de son équipe ou bien les unités constituant mon équipe.
#v(2em)

=== Should have
-	Un système de classe et de famille pour les unités : au-delà d'avoir une unité, en avoir plusieurs différentes de la même classe ou famille devrait accorder un bonus (cf. GDD).
-	Un roster d'unités suffisamment grand : même si cela est assez subjectif, on va partir sur une base de 40 unités différentes qui seraient jouables
-	Une interaction spécifique lors de l'utilisation de chaque famille : s'il y a suffisamment d'unités différentes d'une même famille dans votre équipe, cela devrait apporter une mécanique nouvelle lors des combats. Et le bonus ou la mécanique de classe devrait apporter un clin d’œil sympa aux mécaniques originel du jeu.
#v(2em)

=== Could have
-	Un système d'augment pendant la partie : durant la partie plusieurs choix aléatoires sont proposés aux joueurs afin d'améliorer leur équipe ou de changer leur partie actuelle
-	Un pouvoir spécifique aux joueurs, et où chaque joueur pourrait choisir entre certains pouvoirs afin de changer la dynamique de sa partie
-	Une IA complexe et challengeante, qui pourrait poser un vrai danger à des joueurs connaissant le jeu ou les jeux de ce genre
#v(2em)

=== Won't have
-	Une vraie direction artistique
-	Une bande son

#v(2em)

== Durée du projet
Je dois mettre un travail équivalent à 13 h de travail par semaine le long du semestre, et 45 h lorsque mon semestre sera fini, donc en juillet. Cela fera un total de 450 h de travail disponibles.
#v(2em)

== Livrables <livrables>
-	remise du cahier des charges pour le 27.02.2026
-	remise du rapport intermédiaire le 20.05.2026 avant 15 h
-	remise du travail ainsi que le rapport des sources, une affiche et un résumé publiable pour GAPS le 23.07.2026 avant 11 h.
#v(2em)

== Jalon
Milestone 1 : Planification et setup du projet, finir le cahier des charges, le planning\
Milestone 2 : analyse des besoins ainsi que la spécification de ces derniers\
Milestone 3 : conception, réalisation de l’architecture design(stack, et UML de classe), design de database ainsi que le design de l’UI\
Milestone 4 : implémentation des must have\
Milestone 5 : implémentation des should have\
Milestone 6 : réalisation des documents à rendre (affiche et résumé publiable)\
