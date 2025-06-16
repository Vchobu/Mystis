# Projet de Jeu 2D - GDX2D

## Description

Ce projet est un jeu 2D développé avec la librairie **GDX2D**, combinant animation, combat contre des ennemis, gestion de projectiles et système de bonus. 

Le projet a été réalisé en groupe dans le cadre du cours de programmation orientée jeu vidéo, avec une répartition des tâches autour de la logique de jeu, de l’animation, des collisions, et de l’interface utilisateur.

---

## Interface utilisateur

### Menu principal
*Capture d'écran ici*  
![Main Menu](/screenshots/main_menu.png)

### Menu des options
*Capture d'écran ici*  
![Options](/screenshots/options.png)

### Gameplay
*Capture d'écran ici*  
![Gameplay](/screenshots/gameplay.png)

### Choix du bonus
*Capture d'écran ici*  
![Bonus Choice](/screenshots/bonus_choice.png)

---

## Démonstration vidéo

> *Vidéo illustrant le gameplay, les ennemis, les animations et l’interface utilisateur.*

[![Watch the video](video/gameplay.mp4)

---

## Fonctionnalités principales

- Apparition dynamique des ennemis
- Animations : déplacement, attaque, mort
- Projectiles avec détection de collision
- Système de repoussement des ennemis
- IA simple : poursuite du joueur
- Bonus et améliorations à choisir pendant le jeu
- Menu principal, pause et options interactives
- Gestion du son et du volume (musique et effets)

---

## Problèmes rencontrés

- Certaines **animations de mort** ne se déclenchent pas toujours correctement, notamment pour le héros.
- Le système de **repoussement des ennemis** fonctionne, mais peut appliquer une force excessive dans certains cas.
- Quelques ajustements de collisions et transitions restent à peaufiner.

---

## Propositions d'améliorations

- Ajouter une **caméra qui suit le joueur** pour permettre un monde plus grand et une navigation fluide.
- Agrandir la carte pour améliorer l'exploration.
-  Implémenter plus de types d’ennemis et de bonus.
- Optimiser les animations pour une meilleure fluidité.
- Améliorer la collaboration et la gestion de projet pour une répartition des tâches plus équilibrée.

---

## Utilisation de ChatGPT

Nous avons utilisé **ChatGPT** lors de phases de blocage ou pour valider certaines approches techniques. Cela nous a permis de débloquer des problèmes de logique, de clarifier certains comportements inattendus, et d’optimiser notre code plus rapidement. Nous avons également utilisé ChatGPT pour traduire le rapport.

---

## Guide d'installation

### Pré-requis
- Scala SDK
- IDE IntelliJ

### Étapes
**Cloner le projet** :
- File -> New -> Project from Version Control -> https://github.com/Vchobu/Mystis
- Configuration du SDK Scala
- Installer toutes les dépendances dans le dossier lib
- Exécuter MystisUnifiedApp.scala

