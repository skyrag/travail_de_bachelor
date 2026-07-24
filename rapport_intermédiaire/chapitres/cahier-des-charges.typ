= Cahier des charges <cahier-des-charges>

== Description du projet
#v(2em)

Le projet a pour but de créer un jeu ayant un style similaire à des jeux comme TeamfightTactics ou Hearstone battleground mais en le personnalisant. L'objectif n'est pas de réinventer le mode de jeu, mais de créer un jeu fonctionnel avec lequel les joueurs s'amuseront avec des personnages de la pop-culture. On cherche donc à réaliser un jeu multijoueur en ligne de type auto-battler,  permettant à huit joueurs de s’affronter simultanément au cours d’une même partie.
#v(2em)
#v(2em)

== Technologies utilisées
#v(2em)

Pour ce projet, j'ai utilisé le framework Play pour le développer. Il a pour objectif d'être jouable sur navigateur internet. Pour le stockage des données, le projet utilise le système de gestion de base de données PostgreSQL.
#v(2em)
#v(2em)

== Valeur d’ingénierie du projet
L’intérêt du projet réside dans la réalisation d’un jeu multijoueur entièrement en ligne, accessible directement depuis un navigateur web et ne nécessitant aucune installation de la part des utilisateurs. Cette approche permet de réduire les contraintes techniques à l’entrée et d’offrir une expérience de jeu immédiatement accessible, favorisant ainsi l’engagement et la participation des joueurs.
#v(2em)

L’aspect multijoueur introduit des défis techniques importants, notamment la synchronisation d’état entre plusieurs clients, la gestion de la latence réseau, la cohérence des actions concurrentes et la fiabilité des communications. Le serveur doit également être capable d’orchestrer plusieurs parties simultanées tout en garantissant l’intégrité des données et la continuité de l’expérience de jeu. Pour répondre à ces exigences, il faudra que le design de la base de données permette une résistance à la panne, afin de limiter les risques de perte de données et de garantir la disponibilité du service.
#v(2em)

La conception de la base de données doit aussi permettre un système de 'replay' de partie afin que l’on puisse rejouer entièrement des parties qui ont été enregistrées sur la base de données. Cette fonctionnalité offrira non seulement aux joueurs la possibilité de revoir leurs parties, mais constituera également un outil précieux pour l’analyse de parties, le débogage et l’amélioration continue du jeu.
#v(2em)
#v(2em)

== Fonctionnalités attendues
Cette section décrit les fonctionnalités attendues à la fin du projet. Elle contient également des fonctionnalités futures souhaitées.

=== Fonctionnalités obligatoires
-	Un système d'authentification
-	Un système de stockage des données utilisateurs
-   Une page ou chaque utilisateur peut regarder ses statistiques, soit le nombre de victoires dans un premier temps
-	Un système de connexion multijoueur, où l'on pourra connecter 8 personnes à la même partie afin qu'ils s'affrontent en temps réel
-	Un système de partie opérationnel : un joueur pourrait lancer une partie pour jouer contre d'autres joueurs ou des bots. Donc cela inclut :
    -	Une boutique pour acheter les unités
    -	Un système de combat qui fera s'affronter les unités de deux joueurs à intervalles réguliers
    -	Un système de gestion de son équipe afin que l'on puisse changer soit la formation de son équipe ou bien les unités constituant mon équipe.
-	Un système de replay de partie
#v(2em)

=== Fonctionnalités optionnelles
-	Un système de classe et de famille pour les unités, leur attribuant des effets spécifiques
-	Une liste de 40 unités différentes jouables
#v(2em)


#v(2em)

== Durée du projet
Une charge de 13 hebdomadaire devrait être investie le long du semestre ainsi que 45h supplémentaires à temps plein. Cela fait un total de 450 h de travail disponibles.
#v(2em)

== Livrables <livrables>
-	Remise du cahier des charges pour le 27.02.2026
-	Remise du rapport intermédiaire le 20.05.2026 avant 15 h
-	Remise du travail de Bachelor, du rapport, d'une affiche et un résumé publiable pour GAPS le 24.07.2026 avant 11 h.
#v(2em)

== Jalon
Étape 1 : Planification et définition du projet, rédaction du cahier des charges et du planning\
Étape 2 : Analyse des besoins ainsi que leur spécification
Étape 3 : Conception et réalisation de l’architecture (stack, et UML de classe), conception de la base de donnée et conception de l’interface utilisateur\
Étape 4 : Implémentation des Fonctionnalités attendues
Étape 5 : Implémentation des Fonctionnalités optionnelles
Étape 6 : Réalisation des documents à rendre (affiche et résumé publiable)\
