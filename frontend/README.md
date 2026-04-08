# Pricing Engine — Frontend

Angular 17 frontend (standalone components) pour le moteur de tarification Tendanz.

---

## Stack technique

| Technologie       | Usage                                  |
|-------------------|----------------------------------------|
| Angular 17        | Framework frontend                     |
| Standalone Components | Pas de NgModule                   |
| Reactive Forms    | Formulaire avec validation             |
| RxJS              | Gestion des flux HTTP asynchrones      |
| HttpClient        | Appels API REST                        |
| Angular Router    | Navigation entre les pages             |

---

## Structure

```
src/app/
├── models/
│   ├── quote.model.ts        # QuoteRequest, QuoteResponse
│   └── product.model.ts      # Product
├── services/
│   ├── quote.service.ts      # createQuote, getQuote, getQuotes
│   └── product.service.ts    # getProducts
├── pages/
│   ├── quote-form/           # Formulaire de création de devis
│   ├── quote-list/           # Liste des devis avec filtres et tri
│   └── quote-detail/         # Détail d'un devis
├── app.component.ts          # Navbar + router-outlet
└── app.routes.ts             # Configuration des routes
```

---

## Lancer l'application

```bash
npm install
ng serve
# http://localhost:4200
```

L'API backend doit tourner sur `http://localhost:8080`.

---

## Pages et fonctionnalités

### /quotes — Liste des devis
- Chargement de tous les devis au démarrage
- Filtre par produit (dropdown chargé depuis l'API)
- Filtre par prix minimum
- Tri par date ou par prix (ascendant / descendant, toggle)
- Clic sur une ligne → navigation vers le détail
- États : chargement, erreur, liste vide

### /quotes/new — Nouveau devis
- Formulaire réactif avec les champs :
  - `clientName` — requis, 2 à 100 caractères
  - `productId` — requis, chargé depuis `GET /api/products`
  - `zoneCode` — requis, parmi TUN / SFX / SOU
  - `clientAge` — requis, entre 18 et 99
- Validation affichée en temps réel sur chaque champ
- État de chargement pendant la soumission
- Succès → navigation automatique vers le détail du devis créé
- Erreur → message affiché sous le formulaire

### /quotes/:id — Détail d'un devis
- Chargement du devis par ID depuis les paramètres de route
- Affichage :
  - Informations client (nom, âge)
  - Détails assurance (produit, zone)
  - Règles de tarification appliquées (liste)
  - Récapitulatif des prix (base et final)
  - Date de création
- Bouton retour vers la liste
- États : chargement, erreur (devis introuvable)

---

## Services

### QuoteService

| Méthode | HTTP | Endpoint |
|---------|------|----------|
| `createQuote(request)` | POST | `/api/quotes` |
| `getQuote(id)` | GET | `/api/quotes/:id` |
| `getQuotes(filters?)` | GET | `/api/quotes?productId=X&minPrice=Y` |

Les filtres sont construits avec `HttpParams` et sont tous optionnels.

### ProductService

| Méthode | HTTP | Endpoint |
|---------|------|----------|
| `getProducts()` | GET | `/api/products` |

Tous les appels utilisent `catchError` pour retourner un message d'erreur lisible.

---

## Modèles TypeScript

### QuoteRequest
```typescript
{
  productId: number;
  zoneCode: string;    // 'TUN' | 'SFX' | 'SOU'
  clientName: string;
  clientAge: number;
}
```

### QuoteResponse
```typescript
{
  quoteId: number;
  productName: string;
  zoneName: string;
  clientName: string;
  clientAge: number;
  basePrice: number;
  finalPrice: number;
  appliedRules: string[];
  createdAt: string;   // ISO timestamp
}
```

### Product
```typescript
{
  id: number;
  name: string;
  description: string;
  createdAt: string;
}
```

---

## Configuration

L'URL de l'API est définie dans `src/environments/environment.ts` :

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api'
};
```

---

## Routes

| Path | Composant | Description |
|------|-----------|-------------|
| `/` | redirect | Redirige vers `/quotes` |
| `/quotes` | QuoteListComponent | Liste des devis |
| `/quotes/new` | QuoteFormComponent | Créer un devis |
| `/quotes/:id` | QuoteDetailComponent | Détail d'un devis |
| `**` | redirect | Redirige vers `/quotes` |
