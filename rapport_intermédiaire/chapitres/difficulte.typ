= Difficultés rencontrées
#v(2em)
Nous allons ici parler des difficultés rencontrées lors de ce travail de bachelor. C'est une partie importante, car il s'agit de défis que nous n'avions pas anticipés, et dont il est essentiel de tirer un maximum d'enseignements afin de ne plus y être confrontés à l'avenir, mais surtout afin de progresser.
#v(2em)
== Première base de données
#v(2em)
Abordons tout d'abord la conception et l'implémentation de notre base de données. Lors de la conception, nous avions décidé de nous orienter vers une base de données relationnelle. Cependant, parmis les cours que j'ai suivi durant mon cursus ceux parlant de base de donnée avais quelque lacune quant a l'aspect pratique de la conceptualisation et l'implémentation d'un base de donnée. Je me suis donc retrouvé à devoir rattraper le cours de Base de Données Relationnelles de la HEIG afin de combler mes lacunes sur le sujet.
#v(1em)

Ce rattrapage a entraîné un net ralentissement, aussi bien au niveau de la conception que de l'implémentation. C'est notamment à cette occasion que j'ai découvert le fonctionnement de l'héritage dans une base de données relationnelle, ainsi que la manière de garantir la cohérence de cet héritage à l'aide de triggers.

#v(2em)
== Découverte de Play et notamment Pekko avec le pattern acteur
#v(2em)
Le framework utilisé était forcément un peu différent de ce que j'avais pu rencontrer jusque-là, sans présenter toutefois de différence fondamentale, si ce n'est que l'ensemble du système de communication repose sur Pekko, qui utilise le pattern acteur. Il s'agit d'un pattern que je n'avais jamais rencontré auparavant, et qui m'a demandé un certain temps avant d'en comprendre le fonctionnement, ainsi que la manière d'en tirer pleinement parti dans l'implémentation de mon projet.
#v(2em)
== difficulté de focus sur les technologie utile
#v(2em)
Lors de la phase initiale et de l'état de l'art, n'ayant encore jamais eu à faire de choix d'architecture ou de technologies dans les différents projets auxquels j'avais participé jusqu'ici (principalement des laboratoires), j'ai perdu beaucoup de temps à m'éparpiller sur des aspects peu utiles — soit trop poussés pour le périmètre de mon projet, soit inutiles au vu de sa nature ou du framework choisi.
#v(1em)

Je n'ai pas non plus pris le temps d'analyser suffisamment en profondeur les points réellement critiques de mon projet : par exemple, le problème rencontré avec OpenIDConnect, qui nécessitait des démarches administratives avec des tiers, ne s'est révélé à moi qu'au moment de commencer son implémentation, plutôt qu'en amont lors de l'état de l'art.

#v(2em)
== Difficulté de conception et de poser les choses à plat
#v(2em)
Dans les paragraphes précédents, nous avons discuté des difficultés rencontrées lors de ce travail. Mais je pense que la plus grande difficulté que j'aie rencontrée provient de mon inexpérience et de ma naïveté dans mon approche de ce travail.
#v(1em)

En effet, le fait d'avoir moi-même proposé le sujet impliquait que la plupart des décisions de conception et d'architecture découlaient finalement de la façon dont je percevais le projet. Pour rester cohérent avec cette vision, il aurait fallu que je formalise une conceptualisation claire des différentes parties bien en amont. Or, alors même qu'on me l'avait conseillé, mon approche a été de me dire que je verrais les choses au fur et à mesure, persuadé de savoir précisément ce que je voulais obtenir. Cette mentalité n'a cependant mené, tout au long de l'implémentation, qu'à de nombreux allers-retours, car j'avais oublié certains éléments — sans compter les fonctionnalités que j'ai dû sacrifier à cause de cette négligence initiale.

#v(2em)
== Confusion entre vision du jeu et besoins logiciels
#v(2em)
C'est le plus gros projet sur lequel j'ai eu l'occasion de travailler, et le fait d'en être moi-même le client m'a en réalité desservi : en plus du travail de bachelor à proprement parler, j'avais une véritable réflexion à mener sur ce que je voulais construire, réflexion qui n'a pas été menée suffisamment en amont pour être efficace au moment de l'implémentation.
#v(1em)

Je pense que j'abordais le projet en sachant exactement ce que je voulais obtenir en tant que jeu, mais que j'étais en revanche complètement perdu quant aux fonctionnalités nécessaires du point de vue logiciel. Cela m'a empêché de me rendre compte, dès le départ, de l'ampleur du travail supplémentaire que cela allait représenter, et qu'il aurait fallu commencer par me concentrer sur un ensemble de fonctionnalités minimal — quitte à ce que le jeu ne soit pas encore "amusant" — plutôt que de chercher à tout construire en même temps. C'est pourtant la direction qui aurait dû être prise dès le départ.
#v(1em)

Bien que cela paraisse évident aujourd'hui, et qu'il faille admettre qu'à l'époque j'en connaissais déjà la théorie, je n'ai, dans la pratique, pas su séparer suffisamment les choses pour obtenir un jeu minimal viable, tout en implémentant les fonctionnalités logicielles nécessaires à son fonctionnement.