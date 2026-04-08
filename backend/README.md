# Pricing Engine — Backend

Spring Boot 3.2 / Java 17 backend for the Tendanz insurance pricing engine.

---

## Stack technique

| Technologie        | Usage                              |
|--------------------|------------------------------------|
| Java 17            | Langage                            |
| Spring Boot 3.2    | Framework applicatif               |
| Spring Data JPA    | ORM et accès aux données           |
| H2 (in-memory)     | Base de données de développement   |
| Lombok             | Réduction du boilerplate           |
| JUnit 5            | Tests unitaires                    |
| Jackson JSR310     | Sérialisation des dates Java 8+    |

---

## Structure

```
src/main/java/com/tendanz/pricing/
├── PricingApplication.java          # Point d'entrée + bean ObjectMapper
├── config/
│   └── CorsConfig.java              # Autorisation CORS pour Angular (port 4200)
├── controller/
│   ├── ProductController.java       # GET /api/products
│   └── QuoteController.java         # POST /api/quotes, GET /api/quotes, GET /api/quotes/{id}
├── service/
│   └── PricingService.java          # Calcul de tarification et récupération des devis
├── repository/
│   ├── ProductRepository.java
│   ├── ZoneRepository.java
│   ├── PricingRuleRepository.java
│   └── QuoteRepository.java         # findByClientName, findByProductId, findByFinalPrice...
├── entity/
│   ├── Product.java
│   ├── Zone.java
│   ├── PricingRule.java
│   └── Quote.java
├── dto/
│   ├── QuoteRequest.java            # Validation @NotNull, @Min, @Max, @NotBlank
│   └── QuoteResponse.java
├── enums/
│   └── AgeCategory.java             # YOUNG / ADULT / SENIOR / ELDERLY + fromAge()
└── exception/
    └── GlobalExceptionHandler.java  # @ControllerAdvice — 400 / 404 / 500
```

---

## Lancer l'application

### Prérequis
- Java 17+
- Maven 3.8+

### Démarrage
```bash
mvn spring-boot:run
```

API disponible sur `http://localhost:8080`

### Console H2
```
URL : http://localhost:8080/h2-console
JDBC URL : jdbc:h2:mem:testdb
User : sa
Password : (vide)
```

---

## API Endpoints

### GET /api/products
Retourne la liste des produits d'assurance disponibles.

```json
[
  { "id": 1, "name": "Assurance Auto", "description": "...", "createdAt": "..." },
  ...
]
```

### POST /api/quotes
Crée un nouveau devis avec calcul de prix automatique.

**Request body :**
```json
{
  "productId": 1,
  "zoneCode": "TUN",
  "clientName": "Ahmed Ben Ali",
  "clientAge": 30
}
```

**Response 201 Created :**
```json
{
  "quoteId": 1,
  "productName": "Assurance Auto",
  "zoneName": "Grand Tunis",
  "clientName": "Ahmed Ben Ali",
  "clientAge": 30,
  "basePrice": 500.00,
  "finalPrice": 600.00,
  "appliedRules": [
    "Produit: Assurance Auto - Taux de base: 500.00 TND",
    "Catégorie d'âge: ADULT (âge 30) - Facteur: 1.00",
    "Zone: Grand Tunis (TUN) - Coefficient: 1.20",
    "Prix final: 500.00 × 1.00 × 1.20 = 600.00 TND"
  ],
  "createdAt": "2026-04-08T16:19:18"
}
```

**Erreurs de validation (400) :**
```json
{
  "timestamp": "2026-04-08T16:00:00",
  "status": 400,
  "error": "Bad Request",
  "errors": {
    "clientAge": "Client must be at least 18 years old",
    "clientName": "Client name must be between 2 and 100 characters"
  }
}
```

### GET /api/quotes/{id}
Retourne un devis par son ID. Retourne 404 si introuvable.

### GET /api/quotes
Retourne tous les devis. Supporte des filtres optionnels :

| Paramètre   | Type   | Description                        |
|-------------|--------|------------------------------------|
| `productId` | Long   | Filtre par produit                 |
| `minPrice`  | Double | Filtre par prix final minimum      |

Exemples :
```
GET /api/quotes
GET /api/quotes?productId=1
GET /api/quotes?minPrice=700
GET /api/quotes?productId=1&minPrice=500
```

---

## Logique de tarification

```
Prix Final = Taux de Base × Facteur Âge × Coefficient Zone
```

Implémentée dans `PricingService.calculateQuote()` :
1. Chargement du produit (exception si introuvable)
2. Chargement de la zone par code (exception si introuvable)
3. Chargement de la règle de tarification du produit
4. Détermination de la catégorie d'âge via `AgeCategory.fromAge()`
5. Récupération du facteur âge via `getAgeFactor()`
6. Calcul : `baseRate × ageFactor × zoneCoefficient` arrondi à 2 décimales
7. Construction de la liste `appliedRules` documentant chaque étape
8. Persistance du devis et retour du DTO

---

## Gestion des erreurs

| Exception                        | Code HTTP | Description                        |
|----------------------------------|-----------|------------------------------------|
| `MethodArgumentNotValidException`| 400       | Validation @Valid échouée          |
| `IllegalArgumentException`       | 404       | Produit / zone / règle introuvable |
| `Exception` (fallback)           | 500       | Erreur inattendue (message générique) |

---

## Tests

```bash
mvn test
```

8 tests unitaires dans `PricingServiceTest` :

| Test | Résultat attendu |
|------|-----------------|
| `testCalculateQuoteForAdult` | 500 × 1.00 × 1.20 = **600.00 TND** |
| `testCalculateQuoteForYoungClient` | 500 × 1.30 × 1.20 = **780.00 TND** |
| `testCalculateQuoteForSeniorClient` | 500 × 1.20 × 1.20 = **720.00 TND** |
| `testCalculateQuoteForElderlyClient` | 500 × 1.50 × 1.20 = **900.00 TND** |
| `testCalculateQuoteWithInvalidProductId` | `IllegalArgumentException` |
| `testCalculateQuoteWithInvalidZoneCode` | `IllegalArgumentException` |
| `testGetQuoteById` | Création puis récupération, vérification des champs |
| `testAgeBoundaries` | Âge 24 → YOUNG (780), âge 25 → ADULT (600) |
