# Projet d'introduction à la sécurité
#### De Loïc Amagli [22007062]  
  
  
## Principe :
Le but de ce projet est de créer un "coffre-fort", un dossier empêchant l'accès des données qu'il contient , à un personne non-autorisée.
J'utilise pour cela FUSE qui permet de créer un système de fichiers personnalisé. 

##  Fonctionnement : 
Un dossier à protéger "Folder" est monté en un second dossier "Gate" ( attention : ce dernier doit être vide).
    
```
$ pyton3 myfs.py Folder Gate
```
    
on accède désormais aux fichiers de Folder via Gate. Le programme va créer des versions chiffrées des fichiers de Folder. Ils porteront l'extension ".enc"
Cela se fait grâce au chiffrement par bloc vu plus tôt dans l'année.

Vous remarquerez qu'un mot de de passe est demandé au montage. Si le bon mdp est renseigné, l'utilisateur pourra interagir normalement avec les fichiers.
Sinon, il recevra les chiffrés, chaque fois qu'il cherchera à accéder à tel ou tel fichier.

Voir Illustration :  
![figure 1](un.png)  
![figure 2](deux.png)  

Le mot de passe est écrit en dur dans le code, il s'agit de "password" . J'aurais aimé les gérer proprement, lui et la clé de chiffrement  
  
## Dépendances
les packages python suivants sont nécessaires  
```
os  
sys  
errno  
fusepy  
```  
[Libfuse](https://github.com/libfuse/libfuse) le sera peut-être aussi.

## Difficultés
La principale difficulté aura été d'utiliser FUSE. Il y a très peu de tutoriels disponibles. Les quelques uns disponibles sont soit trop avancés, trop vieux ou trop spécifiques. J'ai ainsi passé beaucoup de temps dans le l'incompréhension. Je suis par chance tombé sur un tutoriel en python, qui simplifiait asssez la chose pour que j'en parte.
