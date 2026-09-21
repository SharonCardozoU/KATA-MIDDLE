ALTER TABLE pregunta
    ADD COLUMN puntaje NUMERIC(8, 2) NOT NULL DEFAULT 10;

ALTER TABLE pregunta
    ADD CONSTRAINT pregunta_puntaje_positivo CHECK (puntaje > 0);
