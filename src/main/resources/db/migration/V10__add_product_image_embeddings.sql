-- ============================================================
-- V10__add_product_image_embeddings.sql
-- Image-search vector store (SigLIP, 768-dim) — SEPARATE from kb_chunks (bge-m3, 1024).
-- Rows are written by RAG's ImageIndexer, never by shop-api JPA (no entity).
-- Extension `vector` already created in V7.
-- product_id FK -> products(id) ON DELETE CASCADE (cleanup on product delete).
-- NO resource_id FK: avoids write-time races between RAG indexing and resource writes.
-- ============================================================
CREATE TABLE
    product_image_embeddings (
        product_id INTEGER NOT NULL,
        resource_id INTEGER PRIMARY KEY,
        url TEXT NOT NULL,
        embedding VECTOR (768) NOT NULL, -- google/siglip-base-patch16-224
        CONSTRAINT fk_pie_product FOREIGN KEY (product_id)
            REFERENCES products (id) ON DELETE CASCADE
    );

-- HNSW cosine index (mirrors V7's kb_chunks pattern): fast query, no training.
CREATE INDEX idx_pie_embedding ON product_image_embeddings USING hnsw (embedding vector_cosine_ops);

-- Lookup / cascade support for all embeddings of one product.
CREATE INDEX idx_pie_product ON product_image_embeddings (product_id);
