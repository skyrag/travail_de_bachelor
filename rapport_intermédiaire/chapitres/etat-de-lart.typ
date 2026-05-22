= State of the art <stateOfArt>

== Introduction

Dans le cadre de ce projet, dont l’objectif est de développer un jeu selon des standards professionnels, ce travail s’intéresse à la manière dont différents acteurs de l’industrie répondent aux enjeux liés à ce type de développement.

#v(2em)

Le projet repose notamment sur l’utilisation du Framework Play @playFrameworkDocs, qui propose déjà un ensemble de solutions intégrées. L’objectif est donc d’analyser les choix technologiques de solutions existante et les fonctionnalités offertes par ce Framework, afin de concevoir une infrastructure cohérente et réfléchie.

#v(2em)

Cette étude portera plus particulièrement sur les aspects suivants : le frontend, l’authentification des utilisateurs, les communications client-serveur, l’intégration entre la base de données et le backend, ainsi que la mise en place d’un pipeline CI/CD.

#v(2em)
#v(2em)

== Frontend

Ce projet nécessite le développement d’un frontend ne contenant aucune logique métier. Son rôle principal est d’afficher un grand nombre de sprites en deux dimensions, de permettre l’interaction avec l’utilisateur, et de transmettre des messages au backend. 
En effet, l’ensemble des combats est simulé et calculé côté serveur, qui constitue l’unique source de vérité. Le backend transmet ensuite les résultats de la simulation au frontend, chargé de leur affichage.

#v(2em)

Le Framework Play propose des solutions telles que les vues Scala et les templates Twirl @playFrameworkScalaTemplates pour le rendu de page html. Toutefois, en raison du niveau d’interactivité requis, cette approche ne semble pas adaptée. Une solution basée sur JavaScript côté client apparaît donc plus pertinente.

#v(2em)

Dans l’écosystème JavaScript, plusieurs approches sont envisageables. Il est possible d’utiliser du JavaScript pur, ou bien des technologies bas niveau telles que WebGL @webgl pour gérer le rendu graphique. Des solutions plus haut niveau existent également, comme les bibliothèques de rendu (par exemple PixiJS @pixijs) ou des moteurs de jeu complets tels que Phaser @phaser.

#v(2em)

L’utilisation de JavaScript pur ou de WebGL offre une grande flexibilité, mais implique un coût de développement important @htmlGameDiscussion. Compte tenu des contraintes de temps du projet, ces solutions ne sont pas privilégiées, bien que WebGL puisse constituer une option intéressante dans une perspective d’évolution vers la 3D.

#v(2em)

L’un des critères de choix concerne justement la compatibilité avec des rendus 3D. Certaines technologies, notamment les bibliothèques de rendu 2D, ne supportent pas cette évolution, ce qui pourrait nécessiter une refonte importante en cas de changement futur.

#v(2em)

Le choix se restreint ainsi entre une bibliothèque de rendu et un moteur de jeu. La principale différence réside dans les fonctionnalités fournies : un moteur de jeu intègre des éléments complets tels que la boucle de jeu, la gestion du son ou des interactions, tandis qu’une bibliothèque de rendu se concentre principalement sur l’affichage graphique.

#v(2em)

Dans le cadre de ce projet, une grande partie des fonctionnalités offertes par un moteur de jeu ne sont pas utiles, notamment la gestion des collisions, puisque la logique métier est entièrement géré côté backend.

#v(2em)

Selon des benchmarks existants @renderingBenchmark @engineComparison , PixiJS apparaît comme l’une des solutions les plus performantes pour l’affichage d’un grand nombre de sprites avec un taux de rafraîchissement élevé.

#v(2em)

Le choix de PixiJS permet ainsi d’optimiser les performances d’affichage. Toutefois, cette approche nécessite l’ajout de fonctionnalités complémentaires, telles que la gestion du son ou de certaines interactions. Ces besoins peuvent être couverts par des bibliothèques externes, comme Howler.js pour la gestion audio. Ce choix implique aussi que dans le cas d’une envie de passer l’affichage en 3D une refonte importante sera nécessaire car PixiJS ne supporte que la 2D.

#v(2em)
#v(2em)

== Authentification

Afin de proposer une expérience personnalisée, le jeu nécessite la mise en place d’un système d’authentification permettant d’associer des données aux utilisateurs.
#v(2em)

Le Framework Play propose des mécanismes d’authentification intégrés. Toutefois, leur utilisation implique de gérer plusieurs aspects sensibles, tels que le stockage sécurisé des mots de passe (hash), la gestion des connexions ainsi que le traitement des erreurs associées.
#v(2em)

Une analyse des pratiques adoptées par des applications web populaires, telles que Agar.io, montre que l’authentification déléguée constitue une approche largement répandue. Celle-ci repose généralement sur des protocoles standards comme OAuth2 @Oauth2, souvent complétés par OpenID Connect, permettant de déléguer l’authentification à des fournisseurs tiers tels que Google ou GitHub.
#v(2em)

Cette approche présente plusieurs avantages. Elle permet de réduire la complexité de développement, d’améliorer la sécurité en s’appuyant sur des acteurs spécialisés, et d’offrir une meilleure expérience utilisateur grâce à des mécanismes de connexion simplifiés.
#v(2em)

Ainsi, le recours à une solution d’authentification déléguée permet de concentrer les efforts de développement sur les fonctionnalités principales du jeu, tout en limitant les risques liés à la gestion directe des données sensibles.

#v(2em)
#v(2em)

== Connection Client-Serveur

Le serveur backend doit établir des connexions avec les utilisateurs afin de pouvoir échanger des messages avec ces derniers. C’est grâce à ces connexions que la communication entre le client et le serveur est assurée. Il doit également assurer le maintient de ces connexions et être capable de les rétablir en cas d’interruption.
#v(2em)
#v(2em)


=== Connexions bidirectionnelles

Compte tenu du volume et de la fréquence des échanges, un canal de communication bidirectionnel persistant, tel que les WebSockets, constitue une solution plus adaptée que des approches traditionnelles comme le polling HTTP @ablyLongPolling, utilisé avant l’apparition des WebSockets. Cette technique implique des requêtes répétées du client vers le serveur, ce qui engendre une surcharge inutile.

#v(2em)

Une autre alternative est l’utilisation des Server-Sent Events (SSE) @serverSentEvents, qui permettent au serveur d’envoyer des mises à jour au client. Toutefois, cette solution reste unidirectionnelle et ne permet pas au client de communiquer directement avec le serveur, ce qui la rend inadaptée dans le cadre d’un jeu interactif.

#v(2em)

Le framework Play propose une intégration native des WebSockets @playFrameworkWebSockets pour gérer les communications entre le backend et les utilisateurs. Deux approches principales sont disponibles : une première basée sur Akka Streams et le modèle d’acteurs @actorModelWikipedia, et une seconde utilisant directement Akka Streams pour manipuler les flux de données.

#v(2em)

Le choix s’oriente vers l’approche basée sur le modèle d’acteurs. En effet, ce modèle est particulièrement adapté à la gestion d’états concurrents. Il permet notamment d’associer un acteur à chaque client afin de gérer son état individuel, ainsi qu’un ou plusieurs acteurs supplémentaires pour représenter l’état global de la partie. 

#v(2em)
#v(2em)

=== Gestions des connexions

Pour assurer le maintien des connexions, il sera nécessaire de mettre en place un système de heartbeat ainsi qu’un mécanisme de reconnexion en cas de déconnexion. 

#v(2em)

Concernant le système de reconnexion, l’objectif principal est de pouvoir envoyer l’état actuel de la partie au moment où l’utilisateur se reconnecte. Pour cela, il sera nécessaire de conserver un historique de l’état de la partie, cohérent entre tous les utilisateurs. Il s’agit d’un problème classique de concurrence dans un environnement distribué. Dans ce contexte, on pourra s’inspirer de l’algorithme Raft @raft afin d’élaborer notre propre solution.

#v(2em)

Cependant, plusieurs problèmes restent à résoudre, notamment la taille de l’historique à conserver, ainsi que la manière dont la reconnexion sera présentée à l’utilisateur. Par exemple, les combats peuvent-ils être rejoués séquentiellement, ou alors est-ce que l’on affiche seulement les résultats, ou bien est-ce que l’utilisateur ne va avoir aucune indication des événements passés ? 

#v(2em)
#v(2em)


== Lien entre DB et Backend

L’application va avoir besoin de stocker des données sur les utilisateurs, que l’on devra créer, éditer, etc. Nous aurons donc besoin de créer des requêtes pertinentes pour accéder à ces données et faire le lien entre notre backend sur Play et notre base de données PostgreSQL. Nous avons à disposition plusieurs solutions pour cela : utiliser du SQL pur, JOOQ @jooqPlayFramework ou encore un ORM.

#v(2em)

Le premier problème auquel nous serons confrontés est l’impedance mismatch, c’est-à-dire la dissonance entre le modèle orienté objet que nous utilisons et le modèle relationnel de la base de données. Pour résoudre ce problème, la solution la plus simple et la plus économique serait d’utiliser un ORM, dont le but principal est justement de gérer cette différence. De plus, Play propose des tutoriels pour l’intégration d’ORM tels que Hibernate @playFrameworkDatabaseDocs, qui est très utilisé dans l’industrie.

#v(2em)

Cependant, l’abstraction proposée par les ORM s’avère souvent « leaky », et ces derniers ne constituent pas une solution à tous nos problèmes. En effet, leur utilisation nécessite une attention particulière afin de préciser au maximum nos intentions. Sans cela, l’ORM pourrait générer des structures inefficaces, comme des tables inutiles. De plus, il faut être vigilant quant aux problèmes de performance pouvant survenir dans les requêtes générées. 

#v(2em)

Néanmoins, les ORM restent une bonne solution pour la plupart des requêtes liées à un utilisateur particulier. Cependant, dans le cas où l’on souhaite implémenter un leaderboard ou effectuer des statistiques sur les personnages, cela nécessitera des requêtes plus complexes. Dans ces situations, écrire des requêtes en SQL pur ou utiliser JOOQ pourrait s’avérer plus approprié. 

#v(2em)
#v(2em)


== CI/CD

Sachant que ce projet a pour objectif de mettre en place un pipeline CI/CD, nous allons nous intéresser aux différentes solutions disponibles. 

#v(2em)

Tout d’abord, afin de garantir un environnement standardisé pour les différents builds tout au long du projet, nous utiliserons Docker et son système d’images. 

#v(2em)

Ensuite, après avoir étudié les outils utilisés par différents acteurs du domaine, il apparaît que Jenkins est une solution populaire en raison de sa flexibilité et de son caractère open source. Il permet de ne pas tout construire soi-même grâce à l’utilisation de plugins créés et maintenus par une large communauté. Cependant, son utilisation demande un certain investissement en temps, et nécessiterait la mise en place d’un serveur dédié pour exécuter Jenkins. De plus, les retours de développeurs ayant travaillé avec cet outil indiquent que la maintenance des pipelines peut devenir chronophage, notamment en raison de problèmes de compatibilité entre certains plugins. 

#v(2em)

Une alternative plus simple à mettre en œuvre serait d’utiliser GitHub Actions. Cette solution est particulièrement pratique , puisque le versionnement du projet sera déjà réalisé sur GitHub. Elle permet une intégration rapide et ne nécessite pas de serveur supplémentaire pour exécuter les pipelines, sauf en cas d’utilisation de runners auto-hébergés. 

#v(2em)

Finalement, bien qu’il existe des tutoriels réalisés par Riot Games (les développeurs de TFT) montrant une intégration basée sur Jenkins et Docker @riotContainerArticle, nous choisirons d’utiliser GitHub Actions afin de gagner du temps et de simplifier la mise en place du pipeline. De plus, il n’y aura qu’une partie Continuous Delivery dans en premier temps où la création et la publication de l’image docker sera automatiser.

#v(2em)
#v(2em)
