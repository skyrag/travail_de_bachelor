On va traduire ici a quoi va ressembler un paylod généré par notre utilisateur lors des ses inputs ainsi que les différents check qu'il est nécessaire de réaliser pour s'assurer qu'aucune triche n'a lieu. Un Id sera présent dans le paylod pour que l'on puisse buffer les actions afin de revert en cas de triche.

Paylod classique :

```json
{
  "id" : "",
  "type": "",
  "paylod": {
    "entity1" : "",
    "entity2" : ""
  }
}
```

pour les types ils sont la pour décrire les différentes action possible de l'utilisateur voici les move où le paylod est l'unité affecter:
- "BuyUnit"
- "SellUnit"
- "MoveUnit" : ici le paylod contiens en premier lieu(entity1) l'unité affecter et en deuxième lieux (entity2) la nouvelle positions (x, y)
- "GiveToUnit" : ici le paylod contiens en premier lieu(entity1) l'unité affecter et en deuxième lieux (entity2) l'object affecté

Ensuite il y a le reste des type :
- "FuseObject" : ici le paylod contiens les deux objects a fusionner
- "RerollShop" : ici le paylod est vide (sera ignoré)
- "BuyExp" : ici le paylod est vide (sera ignoré)

Maintenant en ce qui concerne les check a faire pour s'assurer de la non triche :
- "BuyUnit" :
  - Frontend : check que l'on a assez d'argent pour le faire
  - Backend : 
    - check si l'unité est présente dans notre shop actuellement
    - check si le user a assez d'argent pour acheté l'unité
    - check si le user a la place pour acheté l'unité
- "SellUnit" :
  - check si le user a l'unité
- "MoveUnit"
  - check si le déplacement est dans la partie du user de l'arène (frontend aussi)
  - check si le user a cette unité
  - check si le user peut ajouté l'unité a son équipe si elle n'en fait pas déjà parti (frontend aussi)
- "GiveToUnit" :
  - check si l'on possède l'unité
  - check si l'on possède l'object dans l'inventaire
  - check si l'unité a pas déjà 3 objects (complété ?)
  - checker le type d'ôbject (remover change les règles a checker)
- "FuseObject" :
  - check qu'on a bien les deux objects a disposition
  - check que c'est deux objects qui peuvent être fuse
- "RerollShop" :
  - check que le user a bien l'argent pour roll (frontend aussi)
- "BuyExp" :
  - check que le user a bien l'argent pour acheter de l'exp
  - check que le user n'est pas niveau maximum

Enfin pour les messages serveur -> client il y aura aussi les type :
- "Error" : qui traduit que le move "id" est illegal
- "OK" : qui traduit que le move "id" est légal 