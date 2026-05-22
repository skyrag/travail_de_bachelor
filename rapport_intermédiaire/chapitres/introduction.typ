= Introduction <introduction>

== Contexte motivation

Ce projet s’inscrit dans une optique de réaliser un travail passionnant et en accord avec la direction vers laquelle j’aimerais que ma carrière se tourne.  

#v(2em)

En effet, je désire m’orienter vers le domaine du jeu vidéo à l’issue de mes études. Ce projet de bachelor est une opportunité de concevoir un jeu de manière structurée et professionnelle, afin de pouvoir utiliser ce projet comme une vitrine de mes compétences.

#v(2em)
#v(2em)

== Description du sujet

Ce projet de bachelor consiste en la conception et le développement d’un jeu multijoueur accessible via le web. L’objectif est de créer une aspplication interactive permettant à plusieurs joueurs de s’affronter au cours de parties, en mettant en œuvre des mécaniques de stratégie et de gestion d’équipe.

#v(2em)

Le projet repose sur le développement d’une application complète intégrant à la fois une interface utilisateur, une logique métier et un système de persistance des données. Il s’inscrit dans une démarche de conception logicielle moderne, en utilisant une architecture distribuée permettant de séparer les différentes responsabilités du système. 
#v(2em)

Au-delà de l’aspect ludique, ce projet vise à mobiliser un ensemble de compétences acquises lors du cursus, telles que le développement web, la gestion d’une architecture multi-tiers, la communication entre client et serveur, ainsi que la mise en place de bonnes pratiques de développement. Une attention particulière est également portée à la qualité du code, à la maintenabilité de l’application et à l’automatisation des tests et du déploiement. 
#v(2em)
#v(2em)

== Description du jeu

Le jeu consistera à jouer des parties. Chaque partie fera affronter 8 joueurs les uns contre les autres, au fur et à mesure des rounds.  
#v(2em)

Chaque joueur aura une équipe qu’il pourra composer des unités de son choix, ainsi que des points de vie. A chaque round chaque joueur recevra de l’argent et pourra alors acheter des unités et réagencer son équipe afin d’être le plus fort possible.  
#v(2em)

Un round dure un temps imparti et à la fin de ce dernier, chaque joueur devra affronter l’équipe d’un autre joueur et celui qui gagne inflige des dégâts au perdant.  
#v(2em)

Enfin, le dernier joueur avec encore des points de vie sera celui qui gagne la partie. Ce système s’inspire du genre des auto-battlers, où la stratégie repose principalement sur la composition et l’optimisation d’équipe plutôt que sur des actions en temps réel. 
#v(2em)
#v(2em)

 

== Architecture du projet

L’objectif est de mettre à profit toutes les connaissances que j’ai pu acquérir lors de mon bachelor dans la réalisation d’un jeu c’est pourquoi avant de développer un jeu web, je développe une application qui pourra être utiliser sur le web, et tout cela implique certaines choses que je vais mettre en place. 
#v(2em)

Mon application est donc multi-tiers et a un frontend, un backend et une base de données.  
#v(2em)

Le frontend ne contiendra pas de logique métier est aura pour seul est unique but d’afficher les différents états de l’application, soit l’écran pour se connecter en tant qu’utilisateur, le lobby une fois connecter et la partie en cours. 
#v(2em)

Le backend lui contiendra toute la logique métier du jeu, il aura pour but de simuler chaque combat et d’envoyer par la suite la simulation réaliser afin que le frontend puisse afficher au client le combat. Le backend aura aussi la responsabilité de checker les entrées utilisateur et de gérer les connections avec ce dernier.  
#v(2em)

La base de données va contenir toutes les informations des clients relative au jeu (historique des partie, etc…) 
#v(2em)

Dans une optique de bonnes pratiques de développement, un pipeline d’intégration et de déploiement continus (CI/CD) sera également mise en place afin d’automatiser les phases de test et de déploiement de l’application. 
#v(2em)
#v(2em)
