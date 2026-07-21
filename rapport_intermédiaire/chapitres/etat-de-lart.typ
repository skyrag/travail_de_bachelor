= State of the art <stateOfArt>
#v(2em)

== Introduction
#v(2em)

Dans le cadre de ce projet, dont l'objectif est de développer un jeu selon des standards professionnels, ce travail s'intéresse à la manière dont différents acteurs de l'industrie répondent aux enjeux liés à ce type de développement.
#v(1em)

Le projet repose notamment sur l'utilisation du framework Play @playFrameworkDocs, qui propose déjà un ensemble de solutions intégrées. L'objectif est donc d'analyser les choix technologiques de solutions existantes ainsi que les fonctionnalités offertes par ce framework, afin de concevoir une infrastructure cohérente et réfléchie.
#v(1em)

Cette étude portera plus particulièrement sur les aspects suivants : le frontend, l'authentification des utilisateurs, les communications client-serveur, l'intégration entre la base de données et le backend, ainsi que la mise en place d'un pipeline CI/CD.
#v(2em)

== Frontend
#v(2em)

Ce projet nécessite le développement d'un frontend ne contenant que très peu de logique métier. Son rôle principal est d'afficher un grand nombre de sprites en deux dimensions, de permettre l'interaction avec l'utilisateur, et de transmettre des messages au backend. En effet, l'ensemble des combats est simulé et calculé côté serveur, qui constitue l'unique source de vérité ; le backend transmet ensuite les résultats de la simulation au frontend, chargé de leur affichage.
#v(1em)

Le framework Play propose des solutions telles que les vues Scala et les templates Twirl @playFrameworkScalaTemplates pour le rendu de pages HTML. Toutefois, en raison du niveau d'interactivité requis, cette approche ne semble pas adaptée ; une solution basée sur JavaScript côté client apparaît donc plus pertinente.
#v(1em)

Dans l'écosystème JavaScript, plusieurs approches sont envisageables. Il est possible d'utiliser du JavaScript pur, ou des technologies bas niveau telles que WebGL @webgl pour gérer le rendu graphique. Des solutions plus haut niveau existent également, comme les bibliothèques de rendu (par exemple PixiJS @pixijs) ou des moteurs de jeu complets tels que Phaser @phaser.
#v(1em)

L'utilisation de JavaScript pur ou de WebGL offre une grande flexibilité, mais implique un coût de développement important @htmlGameDiscussion. Compte tenu des contraintes de temps du projet, ces solutions ne sont pas retenues, bien que WebGL puisse constituer une option intéressante dans une perspective d'évolution vers la 3D.
#v(1em)

L'un des critères de choix concerne d'ailleurs la compatibilité avec un éventuel rendu 3D. Certaines technologies, notamment les bibliothèques de rendu 2D, ne permettent pas une telle évolution, ce qui pourrait nécessiter une refonte importante en cas de changement futur.
#v(1em)

Le choix se restreint ainsi entre une bibliothèque de rendu et un moteur de jeu. La principale différence réside dans les fonctionnalités fournies : un moteur de jeu intègre des éléments complets tels que la boucle de jeu, la gestion du son ou des interactions, tandis qu'une bibliothèque de rendu se concentre principalement sur l'affichage graphique.
#v(1em)

Dans le cadre de ce projet, une grande partie des fonctionnalités offertes par un moteur de jeu ne sont pas utiles, notamment la gestion des collisions, la logique métier étant entièrement gérée côté backend.
#v(1em)

Selon des benchmarks existants @renderingBenchmark @engineComparison, PixiJS apparaît comme l'une des solutions les plus performantes en moyenne, toutes librairies et navigateurs confondus.
#v(1em)

Le premier benchmark @engineComparison a été réalisé sur un MacBook Pro, sur plusieurs navigateurs, en affichant huit mille sprites simultanément. Sur Chrome, PixiJS atteint 60 images par seconde et se classe premier ; sur Firefox, il atteint 48 images par seconde et se classe deuxième ; sur les autres navigateurs testés, ses performances se situent dans la moyenne, autour de 24 images par seconde.
#v(1em)

Le second benchmark @renderingBenchmark permet de tester soi-même différentes librairies graphiques, en choisissant librement le nombre de sprites à afficher. Lors de nos propres essais, seules PixiJS, Three.js, Phaser et Excalibur sont parvenues à afficher 10'000 sprites à 120 images par seconde. Les statistiques globales fournies par l'auteur du site indiquent par ailleurs des moyennes de 47 images par seconde pour PixiJS et de 43 images par seconde pour Phaser, ce qui confirme l'avantage de PixiJS pour ce cas d'usage.
#v(1em)

Le choix de PixiJS permet ainsi d'optimiser les performances d'affichage. Cette approche nécessite toutefois l'ajout de fonctionnalités complémentaires, telles que la gestion du son ou de certaines interactions ; ces besoins peuvent être couverts par des bibliothèques externes, comme Howler.js pour la gestion audio. Ce choix implique également qu'un passage ultérieur à un affichage 3D nécessiterait une refonte importante, PixiJS ne prenant en charge que la 2D.
#v(2em)

== Authentification
#v(2em)

Afin de proposer une expérience personnalisée, le jeu nécessite la mise en place d'un système d'authentification permettant d'associer des données aux utilisateurs.
#v(1em)

Le framework Play propose des mécanismes d'authentification intégrés. Leur utilisation implique toutefois de gérer plusieurs aspects sensibles, tels que le stockage sécurisé des mots de passe (hachage), la gestion des connexions, ainsi que le traitement des erreurs associées.
#v(1em)

Une analyse des pratiques adoptées par des applications web populaires, telles qu'Agar.io, GeoGuessr ou encore GeoGamer, montre que l'authentification déléguée constitue une approche largement répandue. Celle-ci repose généralement sur des protocoles standards comme OAuth2 @Oauth2, souvent complétés par OpenID Connect, permettant de déléguer l'authentification à des fournisseurs tiers tels que Google ou GitHub.
#v(1em)

Cette approche présente plusieurs avantages : elle permet de réduire la complexité de développement, d'améliorer la sécurité en s'appuyant sur des acteurs spécialisés, et d'offrir une meilleure expérience utilisateur grâce à des mécanismes de connexion simplifiés.
#v(1em)

Ainsi, le recours à une solution d'authentification déléguée permettrait de concentrer les efforts de développement sur les fonctionnalités principales du jeu, tout en limitant les risques liés à la gestion directe de données sensibles.
#v(2em)

== Connexion client-serveur
#v(2em)

Le serveur backend doit établir des connexions avec les utilisateurs afin de pouvoir échanger des messages avec ces derniers. C'est grâce à ces connexions que la communication entre le client et le serveur est assurée. Il doit également assurer le maintien de ces connexions, et être capable de les rétablir en cas d'interruption.
#v(2em)

== Connexions bidirectionnelles
#v(2em)

Compte tenu du volume et de la fréquence des échanges, un canal de communication bidirectionnel persistant, tel que les WebSockets, constitue une solution plus adaptée que des approches traditionnelles comme le polling HTTP @ablyLongPolling, utilisé avant l'apparition des WebSockets et impliquant des requêtes répétées du client vers le serveur, ce qui engendre une surcharge inutile.
#v(1em)

Une autre alternative est l'utilisation des Server-Sent Events (SSE) @serverSentEvents, qui permettent au serveur d'envoyer des mises à jour au client. Cette solution reste toutefois unidirectionnelle et ne permet pas au client de communiquer directement avec le serveur, ce qui la rend inadaptée dans le cadre d'un jeu interactif.
#v(1em)

Le framework Play propose une intégration native des WebSockets @playFrameworkWebSockets pour gérer les communications entre le backend et les utilisateurs, avec deux approches principales disponibles : l'une basée sur le modèle d'acteurs @actorModelWikipedia, l'autre utilisant directement Akka Streams pour manipuler les flux de données.
#v(1em)

Le choix s'oriente vers l'approche basée sur le modèle d'acteurs, particulièrement adapté à la gestion d'états concurrents. Il permet notamment d'associer un acteur à chaque client afin de gérer son état individuel, ainsi qu'un ou plusieurs acteurs supplémentaires pour représenter l'état global de la partie.
#v(2em)

== Gestion des connexions
#v(2em)

Pour assurer le maintien des connexions, il sera nécessaire de mettre en place un système de heartbeat, ainsi qu'un mécanisme de reconnexion en cas de déconnexion.
#v(1em)

Concernant le système de reconnexion, l'objectif principal est de pouvoir transmettre l'état actuel de la partie au moment où l'utilisateur se reconnecte. Cela nécessite de conserver un historique de l'état de la partie, cohérent entre tous les utilisateurs — il s'agit d'un problème classique de concurrence en environnement distribué.
#v(1em)

Plusieurs questions restent néanmoins à trancher, notamment la taille de l'historique à conserver, ainsi que la manière dont la reconnexion sera présentée à l'utilisateur : les combats manqués doivent-ils être rejoués séquentiellement, ou seul le résultat final doit-il être affiché, ou encore est-ce que l'utilisateur ne reçoit-il aucune indication des événements survenus durant son absence ?
#v(2em)

== Lien entre la base de données et le backend
#v(2em)

L'application devra stocker des données relatives aux utilisateurs, qu'il faudra créer, modifier, etc. Des requêtes adaptées seront donc nécessaires pour accéder à ces données et faire le lien entre le backend Play et la base de données PostgreSQL. Plusieurs solutions sont envisageables pour cela : l'écriture de SQL pur, l'utilisation de JOOQ @jooqPlayFramework, ou encore le recours à un ORM(Object-Relational Mapping).
#v(1em)

Le premier problème rencontré est celui de l'impedance mismatch, c'est-à-dire la dissonance entre le modèle orienté objet utilisé dans le backend et le modèle relationnel de la base de données : le premier repose sur des notions telles que l'héritage ou les références directes entre objets, quand le second ne connaît que des tables plates reliées par des clés étrangères. Pour résoudre ce problème, la solution la plus simple et la plus économique consiste à utiliser un ORM, dont le rôle principal est justement de gérer cette différence. Play propose d'ailleurs des tutoriels d'intégration pour des ORM tels qu'Hibernate @playFrameworkDatabaseDocs, largement utilisé dans l'industrie.
#v(1em)

L'abstraction proposée par les ORM s'avère cependant souvent imparfaite (on parle d'abstraction leaky, c'est-à-dire laissant transparaître les détails du fonctionnement sous-jacent) : ces outils ne constituent donc pas une solution à l'ensemble des problèmes rencontrés. Leur utilisation nécessite une attention particulière afin de préciser au mieux les intentions du développeur ; sans cela, l'ORM peut générer des structures inefficaces, telles que des tables inutiles, ou entraîner des problèmes de performance dans les requêtes générées.
#v(1em)

Les ORM restent néanmoins une bonne solution pour la plupart des requêtes liées à un utilisateur particulier. En revanche, dans le cas d'un système de classement (leaderboard) ou de statistiques portant sur l'ensemble des personnages, des requêtes plus complexes seront nécessaires. Dans ce type de situation, l'écriture de SQL pur ou l'utilisation de JOOQ pourrait s'avérer plus appropriée.
#v(2em)

== CI/CD
#v(2em)

Ce projet ayant pour objectif la mise en place d'un pipeline CI/CD, cette section s'intéresse aux différentes solutions disponibles.
#v(1em)

Afin de garantir un environnement standardisé pour les différents builds tout au long du projet, Docker et son système d'images seront utilisés.
#v(1em)

L'étude des outils employés par différents acteurs du domaine montre que Jenkins constitue une solution populaire, en raison de sa flexibilité et de son caractère open source. Il permet de ne pas tout reconstruire soi-même, grâce à l'utilisation de plugins créés et maintenus par une large communauté. Son utilisation demande cependant un investissement en temps conséquent, et nécessiterait la mise en place d'un serveur dédié pour l'exécuter. Les retours de développeurs l'ayant utilisé indiquent en outre que la maintenance des pipelines peut devenir chronophage, notamment en raison de problèmes de compatibilité entre certains plugins.
#v(1em)

Une alternative plus simple à mettre en œuvre est GitHub Actions. Cette solution est particulièrement pratique, puisque le versionnement du projet a pour objectif d'être réalisé sur GitHub ; elle permet une intégration rapide et ne nécessite pas de serveur supplémentaire pour exécuter les pipelines, sauf en cas d'utilisation de runners auto-hébergés.
#v(1em)

Bien qu'il existe des tutoriels réalisés par Riot Games (développeurs de Teamfight Tactics) présentant une intégration basée sur Jenkins et Docker @riotContainerArticle, nous avons choisi d'utiliser GitHub Actions afin de gagner du temps et de simplifier la mise en place du pipeline. Seule une partie de Delivery Continue sera mise en place dans un premier temps, la création et la publication de l'image Docker étant automatisées.