= Architecture logicielle
#v(2em)


Après avoir analysé les différentes solutions à notre disposition, nous allons maintenant présenter l'architecture logicielle retenue, ainsi qu'une brève présentation des technologies associées.

== Schéma de l'architecture
#figure(
  image("../images/Architecture.png", width: 100%),
  caption: [
    Schéma architecture logiciel
  ]
) <stack>

#v(2em)
== Frontend
#v(2em)

Le frontend, c'est-à-dire l'interface client, est développé en JavaScript, notamment à l'aide de la librairie PixiJS.

#v(2em)
=== PixiJS
#v(2em)

PixiJS est un moteur de rendu 2D open source permettant de créer des visuels de haute qualité sur le web à l'aide de JavaScript. Ce moteur est construit sur WebGL et, de manière optionnelle, sur WebGPU, ce qui lui permet d'offrir des performances élevées et d'exécuter sans difficulté des scènes contenant de nombreux sprites.

Dans le cadre de ce projet, PixiJS gère l'affichage de nombreux sprites, tels que les personnages, l'interface utilisateur, ainsi que l'ensemble des animations de combat. Il permet ainsi de supporter un grand nombre d'éléments graphiques simultanés sans dégradation significative des performances.
#v(2em)

== Backend
#v(2em)
Le backend est développé en Java. Il exécute l'ensemble de la logique métier de l'application, notamment l'authentification, la gestion des connexions avec les utilisateurs, la gestion des parties, ainsi que la simulation des combats.
#v(2em)

=== Play
#v(2em)
Le framework Play est un framework web open source conçu pour le développement d'applications web en Java et Scala. Il repose sur une architecture moderne basée sur le modèle MVC (Model-View-Controller), ce qui permet de structurer clairement le code et d'en faciliter la maintenance.
Play se distingue par son approche réactive et non bloquante, qui lui permet de gérer efficacement un grand nombre de requêtes simultanées. Il est également fortement intégré à l'écosystème Java, ce qui le rend compatible avec de nombreuses bibliothèques et outils utilisés en entreprise.
Dans le cadre de ce projet, le framework Play est utilisé pour implémenter la partie backend de l'application, notamment la gestion des connexions à l'aide du pattern acteur et de Pekko Streams, permettant de gérer la concurrence de manière fiable.
#v(2em)

=== OAuth2 / OpenID Connect
#v(2em)
OAuth 2.0 est un protocole d'autorisation largement utilisé, qui permet à une application d'accéder à des ressources protégées au nom d'un utilisateur, sans avoir besoin de connaître ses identifiants. Il repose sur l'émission de tokens d'accès, définissant des permissions limitées et temporaires.
OpenID Connect est une couche d'authentification construite au-dessus d'OAuth 2.0. Elle permet de vérifier l'identité d'un utilisateur et de récupérer des informations de profil de manière standardisée, à l'aide d'un mécanisme basé sur des tokens d'identité (ID Token).
Dans le cadre de ce projet, si Oauth2 n'est pas implémenter pour quelconque soucis il est prévu de gérer l'authentification nous même.

#v(2em)
=== Hibernate
#v(2em)
Hibernate est un framework de type ORM (Object-Relational Mapping) utilisé en Java pour faciliter la persistance des objets en base de données relationnelle. Il permet de faire le lien entre le modèle objet de l'application et le modèle relationnel de la base de données, en automatisant la génération des requêtes SQL.
Grâce à Hibernate, il est possible de manipuler directement des objets Java sans avoir à écrire explicitement la majorité des requêtes SQL. Le framework se charge de la traduction entre les objets et les tables, ainsi que de la gestion des relations, des transactions et du cycle de vie des entités.
Dans le cadre de ce projet, Hibernate est utilisé pour simplifier l'accès à la base de données et réduire la complexité liée à l'écriture et à la maintenance des requêtes SQL.
#v(2em)
== Base de données
#v(2em)
PostgreSQL est un système de gestion de base de données relationnelle (SGBDR) open source, reconnu pour sa robustesse, sa conformité aux standards SQL et ses performances. Il permet de stocker, organiser et interroger des données structurées à l'aide du langage SQL.
PostgreSQL offre de nombreuses fonctionnalités avancées, telles que la gestion des transactions ACID, le support d'index performants, ainsi que la possibilité de définir des types de données personnalisés et d'exécuter des requêtes complexes. Il est également apprécié pour sa fiabilité et sa capacité à gérer de grandes quantités de données.
Dans le cadre de ce projet, PostgreSQL est utilisé comme base de données principale afin de stocker les informations liées aux utilisateurs et aux données applicatives, tout en garantissant la cohérence et l'intégrité des données.
#v(2em)

== Pipeline CI/CD
#v(2em)

L'objectif du pipeline CI/CD mis en place est d'automatiser les étapes de construction (build) et de test de chaque nouvelle sortie (release), tout en garantissant l'absence de mauvaises surprises grâce à l'exécution des tests dans un environnement identique à celui utilisé en production. Seule une démarche d'Integration continue est mise en place à ce stade, le déploiement, lui, reste manuel.
#v(2em)

== GitHub Actions
#v(2em)

GitHub Actions est une plateforme d'intégration et de déploiement continus (CI/CD) intégrée directement à GitHub. Elle permet d'automatiser des workflows tels que les tests, la compilation, le linting, ou encore le déploiement d'une application à chaque modification du code source.
Les workflows GitHub Actions sont définis sous forme de fichiers YAML et s'exécutent automatiquement en réponse à des événements du dépôt, tels qu'un push, une pull request, ou la création d'une release. Cette approche permet de standardiser et de fiabiliser les processus de développement.
Dans le cadre de ce projet, GitHub Actions est utilisé pour automatiser les tests et les étapes de déploiement, afin de garantir la qualité du code tout en simplifiant la mise en place du pipeline CI/CD.
#v(2em)

== Docker
#v(2em)

Docker est une plateforme open source permettant de créer, déployer et exécuter des applications dans des conteneurs. Un conteneur est un environnement isolé regroupant une application ainsi que l'ensemble de ses dépendances, garantissant un comportement identique quel que soit le système hôte.
Grâce à cette approche, Docker simplifie la gestion des environnements de développement, de test et de production, en réduisant les problèmes liés aux différences de configuration entre machines. Les conteneurs sont légers, rapides à démarrer, et plus efficaces en ressources que les machines virtuelles traditionnelles.
Dans le cadre de ce projet, Docker est utilisé afin de standardiser les environnements d'exécution et de faciliter le déploiement des différentes parties de l'application, notamment le backend et les services associés.
#v(2em)