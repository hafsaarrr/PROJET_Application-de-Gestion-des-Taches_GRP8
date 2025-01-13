# Task Manager Application

## Description

Cette application Android de gestion de tâches permet aux utilisateurs de créer, éditer, supprimer et gérer des tâches avec des fonctionnalités telles que le tri, le filtrage et des notifications pour les tâches à venir. Elle permet également de définir des priorités, des catégories et des dates d'échéance pour chaque tâche. Les notifications sont envoyées une heure avant la date d'échéance des tâches.

## Fonctionnalités

- **Ajout de tâche** : Crée une nouvelle tâche avec un titre, une description, une date d'échéance, une priorité, une catégorie et un statut de complétion.
- **Modification de tâche** : Permet à l'utilisateur de modifier les détails d'une tâche existante.
- **Suppression de tâche** : Supprime une tâche de la liste.
- **Tri et filtrage** : Trie et filtre les tâches selon des critères définis (par priorité, par catégorie, etc.).
- **Notifications** : Envoie une notification une heure avant la date d'échéance d'une tâche.
- **Interface utilisateur** : Interface simple avec une `RecyclerView` pour afficher les tâches et un `FloatingActionButton` pour ajouter de nouvelles tâches.

## Prérequis

- Android Studio
- SDK Android 30+
- Dépendances suivantes dans le fichier `build.gradle` :
  - `androidx.appcompat:appcompat`
  - `androidx.recyclerview:recyclerview`
  - `com.google.android.material:material`

## Installation

1. **Cloner le projet** :
   ```
   git clone https://github.com/tonnomdutilisateur/task-manager.git
   ```

2. **Ouvrir le projet dans Android Studio**.

3. **Compiler et exécuter l'application** sur un appareil Android ou un émulateur.

## Dépendances

- **RecyclerView** : Utilisé pour afficher la liste des tâches.
- **Material Design** : Utilisé pour les composants d'interface tels que le `FloatingActionButton`.
- **AlarmManager** : Pour la gestion des alarmes et des notifications.
- **NotificationManager** : Pour la gestion et l'affichage des notifications.

## Auteurs

- **Développeur principal** : Hafsa Arrab, Asmae Essaih, Noha Keffaoui

## License

Ce projet est sous licence MIT. Voir le fichier [LICENSE](LICENSE) pour plus d'informations.
