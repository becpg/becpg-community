# Guide de Style de Code (Java 17)

Ce document décrit les **conventions de codage** pour ce projet.
Les objectifs sont la **clarté**, la **maintenabilité** et la **cohérence** à travers l’ensemble du code.

## 1. Version de Java

* Toujours utiliser **Java 17**.
* Exploiter les fonctionnalités de Java 17 :

  * Le *pattern matching* pour `instanceof`
  * Les expressions `switch`
  * Les blocs de texte (*text blocks*)
  * Les *records*, classes scellées (*sealed classes*) quand c’est approprié

✅ Exemple :

```java
if (object instanceof Integer i) {
    return i.intValue();
}

int i = switch (j) {
    case 1 -> 3;
    case 2 -> 4;
    default -> 0;
};
```

## 2. Déclaration de variables

* **Ne pas utiliser `var`**.
* Déclarer explicitement les types pour plus de lisibilité.

✅ Exemple :

```java
int number = 0;
ArrayList<String> list = new ArrayList<String>();
HashMap<Integer, String> map = new HashMap<>();
```

## 3. Lambdas et Interfaces Fonctionnelles

* Utiliser des **lambdas** plutôt que des classes anonymes quand c’est possible.
* Préférer les **références de méthodes** lorsqu’elles améliorent la lisibilité.

✅ Exemples :

```java
IntConsumer c = System.out::println;
Runnable r = () -> { /* faire quelque chose */ };

Comparator<Date> comparator =
    Comparator.nullsFirst(Comparator.comparing(Date::toString));
```

## 4. Streams

* Utiliser les Streams **avec parcimonie** :

  * Autorisés pour des pipelines **simples et lisibles**.
  * Éviter les streams imbriqués ou trop complexes.
* Préférer les **boucles `for`** lorsque la logique est complexe.

✅ Exemple :

```java
for (int id : ids) {
    double value = id / 2;
    System.out.println(value);
}

for (int i = 0; i < ids.length; i++) {
    System.out.println("ici");
}
```

## 5. Chaînes de caractères

* Utiliser **`String.join`** plutôt que la concaténation manuelle dans les boucles.
* Utiliser les **blocs de texte** pour les chaînes multilignes.

✅ Exemples :

```java
String concatenation = String.join(", ", texts);

String buf = """
    public class A {
        public void foo() {
        }
    }
    """;
```

## 6. Gestion des exceptions

* Utiliser le **multi-catch** lorsque cela rend le code plus clair.
* Garder les blocs d’exception courts et significatifs.

✅ Exemple :

```java
try {
    obj.throwingMethod();
} catch (IllegalArgumentException | IOException ioe) {
    ioe.printStackTrace();
}
```

## 7. Gestion des ressources

* Toujours utiliser le **try-with-resources** avec les `AutoCloseable`.

✅ Exemple :

```java
final FileInputStream inputStream = new FileInputStream("out.txt");
try (inputStream) {
    System.out.println(inputStream.read());
}
```

## 8. Objets et Comparaisons

* Utiliser `Objects.equals` pour des comparaisons null-safe.
* Utiliser `Objects.hash` lors de l’implémentation de `hashCode`.

✅ Exemples :

```java
return Objects.hash(aShort);

if (!Objects.equals(aText, other.aText)) {
    return false;
}
```

## 9. Types enveloppes (*Wrapper Types*)

* Éviter le boxing/déboxing inutile.
* Préférer les constantes plutôt que `valueOf` manuels.

✅ Exemple :

```java
Integer integerObject = Integer.MAX_VALUE;
Character cObject = Character.MAX_VALUE;

int i = integerObject.intValue();
char c = cObject.charValue();
```

## 10. Pattern Matching

* Utiliser le **pattern matching** pour simplifier les vérifications de type et les conversions.

✅ Exemple :

```java
if (x instanceof Integer xInt) {
    i = xInt.intValue();
} else if (x instanceof Double xDouble) {
    d = xDouble.doubleValue();
} else if (x instanceof Boolean xBoolean) {
    b = xBoolean.booleanValue();
} else {
    i = 0;
    d = 0.0D;
    b = false;
}
```

## 11. Règles Générales de Style

* Suivre les conventions standards de nommage Java (CamelCase, PascalCase pour les classes, UPPER\_CASE pour les constantes).
* **Accolades sur une nouvelle ligne** pour les classes et les méthodes.
* **Indentation de 4 espaces**, pas de tabulations.
* Garder les méthodes **courtes et ciblées** (≤ 30 lignes recommandé).
* Privilégier la **lisibilité plutôt que l’astuce**.
* Minimiser les bibliothèques externes :

  * Privilégier les API JDK et les utilitaires internes existants en premier.
  * Ajouter une dépendance uniquement avec un bénéfice clair et documenté (correction, performance ou gain de productivité significatif).
  * Éviter d’ajouter des bibliothèques pour des aides triviales (chaînes, collections, I/O simples).
  * Choisir de petites bibliothèques bien maintenues ; éviter les frameworks lourds sauf justification solide.

## 12. Formatage du Code Source et Outils

### Formatage Java

Avant d'être validé, une classe Java doit être nettoyée :

1. Clic droit > Source > Clean Up
2. Pas de commentaires en français, supprimer les avertissements Eclipse, pas de `System.out` ou de logs inutiles. Suivre les directives de SonarLint.

#### Paramètres de Configuration

* **Preferences > Java > Code Style > Import :** `becpg_cleanup.xml`
* **Preferences > Java > Formatter > Import :** `becpg_formatter.xml`

#### Conventions de Nommage

Les noms de variables et de méthodes doivent respecter les conventions décrites dans le [Google Java Style Guide - Naming](https://google.github.io/styleguide/javaguide.html#s5-naming).

### Formatage XML

Utiliser les options de formatage XML configurées dans votre IDE.

### Formatage JavaScript

**Web -> Client-side JavaScript -> Formatter -> beCPG [built-in]**

### Internationalisation (I18n)

Suivre les standards de formatage I18n configurés pour un formatage cohérent des fichiers de localisation.

✦ Tous les contributeurs doivent suivre ce guide lors de l’écriture ou de la revue du code.
