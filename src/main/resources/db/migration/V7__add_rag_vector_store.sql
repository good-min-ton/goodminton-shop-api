-- ============================================================
-- V7__add_rag_vector_store.sql
-- Vector store cho RAG chatbot (lớp A: static docs, lớp B: products).
-- Lớp C (price/stock/visibility) KHÔNG nằm trong table này — tool call.
-- Requires: Postgres image với pgvector (đổi sang pgvector/pgvector:pg15 trong compose).
-- ============================================================
CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE
    kb_chunks (
        id BIGSERIAL PRIMARY KEY,
        doc_type VARCHAR(20) NOT NULL, -- 'static' | 'product'
        source_id VARCHAR(200) NOT NULL, -- file path (static) | product_id (product)
        chunk_index INTEGER NOT NULL, -- 0, 1, 2... thứ tự chunk trong source
        content TEXT NOT NULL,
        metadata JSONB DEFAULT '{}',
        embedding VECTOR (1024), -- bge-m3 dimension
        created_at TIMESTAMP NOT NULL DEFAULT NOW (),
        updated_at TIMESTAMP NOT NULL DEFAULT NOW (),
        CONSTRAINT uq_kb_chunk UNIQUE (doc_type, source_id, chunk_index)
    );

-- HNSW index cho cosine similarity search. Insert chậm hơn ivfflat
-- nhưng query nhanh và không cần training, OK cho scale < 100K vectors.
CREATE INDEX idx_kb_chunks_embedding ON kb_chunks USING hnsw (embedding vector_cosine_ops);

-- Lookup nhanh tất cả chunks của 1 source (để DELETE khi product update)
CREATE INDEX idx_kb_chunks_source ON kb_chunks (doc_type, source_id);