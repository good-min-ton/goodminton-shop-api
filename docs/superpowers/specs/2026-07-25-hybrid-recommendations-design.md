# Hybrid Product Recommendations — Design Spec

**Date:** 2026-07-25
**Repo:** `goodminton-shop-api` (Spring Boot). Consumes `goodminton-rag-service` `GET /products/{id}/similar`.
**Status:** Approved design, pre-implementation.

## Goal

Upgrade the product-detail "Có thể bạn sẽ thích" recommendations from pure rule-based to a **hybrid**: RAG semantic similarity (retrieve) + business signals (re-rank), and fix the bug where the section is invisible to non-logged-in shoppers.

## Problem (current state)

`RecommendationServiceImpl.getRecommendations(productId)` builds 8 items from three ordered sources:
1. `ProductRepository.findSimilar` — same `category.id` + `brand.id`, `isVisible`, `ORDER BY createdAt DESC` (≤5)
2. `OrderItemRepository.findBestSellerProductIds` — best sellers last 30 days (fill)
3. `ProductRepository.findOnSale` — variants with `salePrice` (fill)

Two weaknesses:
- **Content-blind:** two different rackets of the same brand+category get near-identical lists; a shoe product gets padded with rackets when there aren't enough same-category items. It cannot rank by actual specs/meaning or find cross-brand similars.
- **Invisible to guests (bug):** `GET /api/products/{productId}/recommendations` is NOT in `SecurityConfig.GET_PUBLIC_ENDPOINTS`, so anonymous visitors get 401. The frontend client attaches a Bearer token only when logged in (`lib/api.ts`), and the product page renders the carousel only when `recs.data.length > 0` — so for guests the call 401s and the section silently disappears.

## Approach (chosen)

**2-stage: semantic retrieve → business re-rank**, semantic-dominant, family excluded, graceful fallback to the existing rule-based pipeline. Frontend unchanged.

### Data flow — extend `RecommendationServiceImpl.getRecommendations(productId)`

```
1. Load product (404 if missing). Build excludedIds = self + related "family"
   (root via related_product_id + all siblings) — UNCHANGED from current logic.

2. STAGE 1 — retrieve (semantic):
   RagClient.similar(productId, RETRIEVE_K)  ->  List<RagCandidate{productId, similarity}>
     - RETRIEVE_K = 20 (config).
     - Drop any candidate in excludedIds (self + family) and any not visible.

3. STAGE 2 — re-rank (semantic-dominant + light business boost):
   For each surviving candidate:
     score = similarity
           + (isBestSeller30d ? BOOST_BESTSELLER : 0)
           + (isOnSale        ? BOOST_SALE       : 0)
   BOOST_BESTSELLER = 0.03, BOOST_SALE = 0.02 (config) — small enough that
   semantic order dominates and business signals only reorder near-ties.
   Sort by score DESC. Keep top TARGET_SIZE (8).
   (isBestSeller30d / isOnSale computed once per candidate set — see "Signal lookup".)

4. Enrich: map picked products -> ProductListItemResponse via the EXISTING
   ProductMapper + ResourceService thumbnail path (reuse toListItem()).

5. FILL — if fewer than TARGET_SIZE after Stage 3 (product not indexed, few
   neighbors, or RAG unavailable), top up with the EXISTING pipeline in order:
   findSimilar(category+brand) -> best sellers -> on sale, excluding already-picked.
   Guarantees 8 and is the graceful-degradation path.

6. Cache: unchanged @Cacheable(RECOMMENDATIONS_CACHE) — TTL 2h, so RAG is called
   at most once per product per 2h window.
```

### New component — `RagClient`

A Spring `@Component` that calls the RAG service.

- Method: `List<RagCandidate> similar(int productId, int limit)` where
  `RagCandidate` is a record `{ int productId, double similarity }`.
- HTTP: `GET {rag.base-url}/products/{productId}/similar?limit={limit}`.
- Response shape (RAG `SimilarProductsResponse`):
  `{ "product_id": "3", "count": N, "results": [ { "product_id": "9", "name": "...", "similarity": 0.977, "distance": 0.023, "chunk_count": 3 }, ... ] }`
  Parse `results[].product_id` (string → int) and `results[].similarity`.
- Timeout: connect + read ≈ 2s total (config `rag.timeout-ms=2000`).
- Failure handling: on ANY error (timeout, non-2xx incl. 404 not-indexed, connection refused, parse error) return an **empty list** and log at WARN. Never throw to the caller.

### Config (`application.yaml` + env)

```
rag:
  base-url: ${RAG_URL:http://rag-service:8000}
  timeout-ms: ${RAG_TIMEOUT_MS:2000}
  retrieve-k: ${RAG_RETRIEVE_K:20}
recommendations:
  boost-bestseller: ${REC_BOOST_BESTSELLER:0.03}
  boost-sale: ${REC_BOOST_SALE:0.02}
```

- **Prod** (compose): `RAG_URL=http://rag-service:8000` (container DNS).
- **Dev** (RAG native on host `:8000`, shop-api in docker): `RAG_URL=http://host.docker.internal:8000` and add `extra_hosts: ["host.docker.internal:host-gateway"]` to the shop-api service; OR run RAG in docker. Documented in the plan.

### Signal lookup (bestseller / on-sale) for re-rank

To boost, we need to know which candidate ids are bestsellers or on-sale — reuse existing queries but as membership sets over the candidate ids:
- Bestseller set: `OrderItemRepository.findBestSellerProductIds(COMPLETED, now-30d, excluded=∅, PageRequest large enough)` → intersect with candidate ids. (Reuse existing query; excluded set can be empty here since we only test membership.)
- On-sale set: candidates whose product has a variant with `salePrice != null`. Add a lightweight repository method `findIdsOnSaleIn(Collection<Integer> ids)` returning the subset of `ids` that are currently on sale, OR reuse `findOnSale` and intersect. Prefer the targeted `findIdsOnSaleIn(ids)` to avoid scanning the whole catalog.

### Security fix

Add `"/api/products/{productId}/recommendations"` to `SecurityConfig.GET_PUBLIC_ENDPOINTS`. Now guests see recommendations. No other endpoint's auth changes.

## Error handling & resilience

- RAG down / slow / product not indexed → `RagClient` returns empty → Stage 5 FILL produces a pure rule-based list (current behavior). The endpoint never fails due to RAG.
- RAG timeout kept short (2s) so a slow RAG never hangs the product page; the 2h cache means the cost is rare.
- Missing product → 404 as today.

## Testing

- **RagClient unit:** mock HTTP — happy path parses ids+similarity in order; non-2xx/404 → empty; timeout → empty; malformed JSON → empty (never throws).
- **Re-rank unit:** given a stubbed `RagClient` returning known (id, similarity): (a) semantic order preserved when no business signals; (b) a bestseller/on-sale item only jumps a near-tie, not a large-gap leader; (c) family/self excluded; (d) result capped at 8.
- **Fill/fallback unit:** stubbed `RagClient` returns empty → result equals the pure rule-based pipeline (8 items, existing behavior); returns 3 → topped up to 8 with rule-based, no duplicates.
- **Integration (`RecommendationServiceImpl`):** stub `RagClient`, real repositories against test data → assert final ordering + family exclusion + fill + 8 items.
- **Existing recommendation tests:** must still pass (behavior identical when RAG returns empty).
- **Security:** `GET /api/products/{id}/recommendations` returns 200 without a token.

## Scope / boundaries

- **In scope:** shop-api `RecommendationServiceImpl` extension, `RagClient`, config, one repository helper (`findIdsOnSaleIn`), `SecurityConfig` whitelist entry, tests.
- **Out of scope:** frontend (no change — same endpoint/carousel); RAG service (already built on `goodminton-rag-service` branch `feat/rag-quick-wins`); the standalone RAG `/similar` REST/chat tool.
- **Dependency:** the RAG `GET /products/{id}/similar` endpoint must be running/deployed (branch `feat/rag-quick-wins`, not yet merged) for the hybrid path to activate; until then the endpoint gracefully serves pure rule-based results.

## Non-goals (YAGNI)

- No collaborative-filtering / personalization (no user-behavior modeling).
- No score-fusion weight tuning UI; boosts are simple config constants.
- No new frontend section or endpoint; no persistence of semantic scores.
