# Test Technique — Full Stack Engineer — Tendanz Group

## Moteur de Tarification Assurance

### Contexte

Développement d'un **moteur de tarification d'assurance** permettant de calculer le prix d'une couverture en fonction du profil client, du produit choisi et de la zone géographique.

---

### Structure du projet

```
├── backend/          # Spring Boot 3.2 — Java 17
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/tendanz/pricing/
│       │   ├── config/
│       │   │   └── CorsConfig.java              # CORS pour Angular
│       │   ├── controller/
│       │   │   ├── ProductController.java        # GET /api/products
│       │   │   └── QuoteController.java          # POST & GET /api/quotes
│       │   ├── service/
│       │   │   └── PricingService.java           # Logique métier de tarification
│       │   ├── repository/
│       │   │   ├── ProductRepository.java
│       │   │   ├── ZoneRepository.java
│       │   │   ├── PricingRuleRepository.java
│       │   │   └── QuoteRepository.java          # Requêtes custom
│       │   ├── entity/                           # Entités JPA
│       │   ├── dto/                              # QuoteRequest / QuoteResponse
│       │   ├── exception/
│       │   │   └── GlobalExceptionHandler.java   # Gestion centralisée des erreurs
│       │   └── enums/
│       │       └── AgeCategory.java
│       └── main/resources/
│           ├── schema.sql
│           ├── data.sql
│           └── application.yml
│
└── frontend/         # Angular 17 — Standalone Components
    ├── package.json
    └── src/app/
        ├── services/
        │   ├── quote.service.ts      # Appels API quotes
        │   └── product.service.ts    # Appels API products
        ├── pages/
        │   ├── quote-form/           # Formulaire de création
        │   ├── quote-list/           # Liste avec filtres et tri
        │   └── quote-detail/         # Détail d'un devis
        └── models/                   # Interfaces TypeScript
```

---

### Démarrage rapide

**Backend :**
```bash
cd backend
mvn spring-boot:run
# API disponible sur http://localhost:8080
# Console H2 : http://localhost:8080/h2-console
# JDBC URL: jdbc:h2:mem:testdb — user: sa — password: (vide)
```

**Frontend :**
```bash
cd frontend
npm install
ng serve
# App disponible sur http://localhost:4200
```

**Tests backend :**
```bash
cd backend
mvn test
# 8 tests — tous passent
```

---

### Formule de tarification

```
Prix Final = Taux de Base × Facteur Âge × Coefficient Zone
```

| Tranche d'âge | Catégorie | Facteur |
|---------------|-----------|---------|
| 18 - 24 ans   | YOUNG     | 1.30    |
| 25 - 45 ans   | ADULT     | 1.00    |
| 46 - 65 ans   | SENIOR    | 1.20    |
| 66 - 99 ans   | ELDERLY   | 1.50    |

| Zone        | Code | Coefficient |
|-------------|------|-------------|
| Grand Tunis | TUN  | 1.20        |
| Sfax        | SFX  | 1.00        |
| Sousse      | SOU  | 1.10        |

| Produit              | Taux de Base (TND) |
|----------------------|--------------------|
| Assurance Auto       | 500.00             |
| Assurance Habitation | 300.00             |
| Assurance Santé      | 800.00             |

**Exemple :** Client de 30 ans, zone Tunis, Assurance Auto = 500 × 1.00 × 1.20 = **600.00 TND**

---

### Choix techniques

**Backend**
- Architecture en couches classique : Controller → Service → Repository
- `@ControllerAdvice` pour centraliser la gestion des erreurs (400, 404, 500)
- `BigDecimal` pour tous les calculs financiers (précision garantie)
- `JavaTimeModule` enregistré sur `ObjectMapper` pour la sérialisation de `LocalDateTime`
- CORS configuré via `WebMvcConfigurer` pour autoriser `localhost:4200`
- Tests avec `@DataJpaTest` + `@Import` pour tester le service avec une vraie base H2

**Frontend**
- Services Angular avec `HttpClient` + opérateur `catchError` de RxJS
- `HttpParams` pour les filtres optionnels sur `GET /api/quotes`
- Reactive Forms avec `FormBuilder` et `Validators` pour la validation côté client
- Tri en mémoire sur les résultats de la liste (date / prix, asc / desc)
- Standalone components Angular 17 (pas de NgModule)

---

### Deadline

**Samedi 11 avril 2026 à 23h59**

Envoyez le lien de votre repository à : **recrutement.tn@tendanz.com**
