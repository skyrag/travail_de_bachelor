= Introduction <introduction>

== Contexte motivation
#v(2em)
Ce projet de bachelor s'inscrit dans la volonté de réaliser un travail à la fois exigeant et en accord avec l'orientation professionnelle envisagée à l'issue des études, à savoir le domaine du jeu vidéo. Il constitue une opportunité de concevoir un jeu de manière structurée et professionnelle, et de disposer ainsi d'une réalisation concrète illustrant les compétences acquises durant le cursus.

#v(2em)
#v(2em)

== Description du sujet
#v(2em)
Ce travail de bachelor consiste en la conception et le développement d'un jeu multijoueur accessible via le web, s'inscrivant dans le genre *auto-battler*. Ce type de jeu oppose plusieurs joueurs (généralement huit) au sein d'une même partie, chacun devant constituer et faire évoluer une équipe d'unités sur un plateau de jeu qui lui est propre. Contrairement à un jeu de stratégie traditionnel, le joueur ne contrôle pas directement ses unités pendant les combats : ceux-ci se déroulent de manière automatique, une fois les phases de préparation (achat, positionnement, équipement) terminées. Une partie se déroule par rounds successifs, alternant une phase de préparation et une phase de combat, durant laquelle chaque joueur affronte tour à tour différents adversaires, jusqu'à ce qu'il ne reste qu'un seul joueur en vie.
#v(2em)
Le projet repose sur le développement d'une application complète intégrant une interface utilisateur, une logique métier, et un système de persistance des données. Il s'inscrit dans une démarche de conception logicielle moderne, à travers une architecture distribuée permettant de séparer clairement les différentes responsabilités du système.
#v(2em)
Au-delà de l'aspect ludique, ce projet vise à mobiliser un ensemble de compétences acquises durant le cursus, telles que le développement web, la conception d'une architecture multi-tiers, la communication entre client et serveur, ainsi que la mise en place de bonnes pratiques de développement. Une attention particulière est portée à la qualité du code, à la maintenabilité de l'application, ainsi qu'à l'automatisation des tests.
#v(2em)
#v(2em)
== Architecture du projet
#v(2em)
Avant de développer un jeu pensé spécifiquement pour le web, l'accent est mis sur le développement d'une application robuste, pouvant être exploitée dans un contexte web, ce qui implique un certain nombre de choix architecturaux détaillés ci-dessous.
#v(2em)
L'application repose sur une architecture multi-tiers, structurée en trois composants principaux : un frontend, un backend, et une base de données.
#v(2em)
Le frontend ne contient aucune logique métier. Son unique rôle est d'afficher les différents états de l'application, à savoir l'écran de connexion, le lobby une fois l'utilisateur authentifié, et le déroulement de la partie en cours.
#v(2em)
Le backend contient l'intégralité de la logique métier du jeu. Il a pour rôle de simuler chaque combat et d'en transmettre le résultat au frontend afin que celui-ci puisse l'afficher au joueur. Il est également responsable de la validation des entrées utilisateur, ainsi que de la gestion des connexions avec le client.
#v(2em)
La base de données contient l'ensemble des informations relatives aux utilisateurs et au déroulement du jeu, notamment l'historique des parties.
#v(2em)
Dans une optique de bonnes pratiques de développement, un pipeline d'intégration continue (CI) est également mis en place, afin d'automatiser les phases de test.
#v(2em)
#v(2em)

== Planification <planification>

Pour la planification initiale, l'objectif était de travailler en cascade tout du long. Cependant, ce plan a évolué lors de la réalisation ce project. En effet, travailler en cascade a été planifier jusqu'a la réalisation d'une version minimal, suite a quoi une implémentation par itération sera utiliser pour les fonctionnalité supplémentaire.

#figure(
  image("../images/planningTb.png", width: 100%),
  caption: [
    Plannification
  ]
) <plan>