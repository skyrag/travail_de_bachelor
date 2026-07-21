= Présentation de notre jeu
#v(2em)
Maintenant que nous avons une meilleure vue d'ensemble du projet, nous allons rappeler ce qu'est un auto-battler avant de présenter les règles de notre jeu.
#v(2em)

== Présentation d'un auto-battler
#v(2em)

Un auto-battler est un genre de jeu dans lequel chaque joueur constitue progressivement une équipe de personnages afin d'affronter les autres joueurs. Les combats se déroulent automatiquement, sans intervention du joueur, d'où le terme _auto-battler_. À l'issue de chaque combat, le perdant perd une partie de ses points de vie. La partie se termine lorsqu'il ne reste plus qu'un seul joueur en vie.
#v(1em)

Voyons maintenant plus en détail les mécaniques propres à notre jeu.
#v(2em)

== Mécanique principale de notre jeu
#v(2em)

Chaque joueur dispose d'une réserve d'or ainsi que d'un magasin lui permettant d'acheter des unités parmi les cinq unités actuellement proposées. Les unités achetées sont immédiatement placées sur son banc, comparable au banc des remplaçants au football. Le joueur peut ensuite décider de les intégrer à son équipe.
#v(1em)

Les joueurs possèdent également un niveau qui détermine le nombre maximal d'unités pouvant être déployées sur le terrain. Ils peuvent dépenser de l'or pour gagner de l'expérience et ainsi augmenter leur niveau.
#v(1em)

Enfin, chaque joueur possède un inventaire contenant des objets pouvant être équipés sur les unités afin d'améliorer leurs statistiques.
#v(2em)

Les unités sont classées selon 5 niveaux de rareté : Commune, Peu commune, Rare, Épique et Légendaire. Plus une unité est rare, plus son coût est élevé et plus le joueur doit être d'un niveau élevé pour avoir des chances de la voir apparaître dans le magasin.
#v(1em)

Les unités possèdent également un niveau, où chaque passage au niveau supérieur les rend plus puissantes. Lorsqu'un joueur possède trois exemplaires d'une même unité, celles-ci fusionnent automatiquement pour former une unité de niveau supérieur.
#v(1em)

Chaque unité a ses propres statistiques, qui déterminent son efficacité au combat. Les statistiques sont :
- *Points de vie* : quantité de dégâts que l'unité peut subir avant de mourir.
- *Mana initial* : quantité de mana avec laquelle l'unité commence le combat
- *Mana maximal* : la quantité de mana nécessaire pour lancer sa compétence
- *Attaque de base* : les dégâts que l'unité inflige avant application de multiplicateur ou de mitigation des dégâts
- *Dégâts d'attaque* : un multiplicateur appliqué a l'attaque de base pour déterminer les dégâts totaux
- *Puissance*: utilisé principalement pour déterminer l'efficacité de la compétence
- *Vitesse d'attaque* : fréquence des attaques
- *Armure* : mitige les dégâts d'attaque
- *Résistance magique* : mitige les dégâts de compétences
- *Portée* : distance maximale d'attaque
Enfin chaque unité dispose également d'une capacité unique qu'elle lance automatiquement lorsque son mana est au maximum. Les unités gagnent du mana lors de chaque attaque.
#v(2em)

Les objets permettent d'améliorer les statistiques des unités qui les équipent. Une même unité peut porter jusqu'à trois objets simultanément.

L'objectif du joueur est donc de gérer son économie, améliorer ses unités, composer une équipe efficace et survivre jusqu'à être le dernier joueur encore en vie.
#v(2em)

== Déroulement de la partie
#v(2em)

Une partie oppose 8 joueurs. Au début de la partie, chaque joueur :
- Commence au niveau 1
- Possède une unité commune aléatoire de niveau 1
- Dispose de 30 pièces d'or

La partie est ensuite découpée en une succession de manches(rounds). Chaque manche comporte deux phases :
#v(2em)

*Phase de préparation*:

Pendant 90 secondes, chaque joueur peut:
	- Déplacer une unité qui est dans son équipe ou sur son banc.
	- Acheter une unité présente dans le magasin. Son coût varie entre 1 et 5 pièces d'or selon sa rareté.
	- Vendre une unité qui est dans son équipe ou sur son banc, afin de récupérer son coût d'achat ainsi que les objets dont elle était équipée.
	- Équiper un objet sur une unité
	- Rafraîchir son magasin pour 2 pièces d'or
	- Acheter 4 point d'expérience pour 4 pièces d'or.
#v(2em)

*Phase de combat* :
Chaque joueur affronte automatiquement l'équipe d'un autre joueur choisi aléatoirement.

Durant cette phase, les joueurs ne peuvent plus modifier leur équipe. En revanche, ils peuvent toujours effectuer des actions n'ayant aucun impact sur le combat en cours, comme gérer leur banc ou acheter des unités dans le magasin.
À la fin du combat, le joueur vaincu perd un montant de point de vie calculé selon la formule suivante :
Points de vies perdus = nombre d'unités en vie chez l'adversaire + le niveau de l'adversaire.
#v(2em)

À la fin de chaque combat, tous les joueurs gagnent 5 pièces d'or et 2 points d'expérience. Le vainqueur reçoit en plus un bonus de 2 pièces d'or.

La partie prend fin lorsqu'il ne reste plus qu'un seul joueur ayant encore des points de vie.
#v(2em)


