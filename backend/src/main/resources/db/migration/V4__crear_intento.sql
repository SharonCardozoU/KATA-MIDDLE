CREATE TABLE intento (
    id             BIGSERIAL PRIMARY KEY,
    evaluacion_id  BIGINT      NOT NULL REFERENCES evaluacion (id) ON DELETE CASCADE,
    iniciado_en    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE respuesta_intento (
    id                    BIGSERIAL PRIMARY KEY,
    intento_id            BIGINT        NOT NULL REFERENCES intento (id) ON DELETE CASCADE,
    pregunta_id           BIGINT        NOT NULL REFERENCES pregunta (id) ON DELETE CASCADE,
    lenguaje              VARCHAR(40)   NOT NULL,
    casos_exitosos        INTEGER       NOT NULL,
    total_casos           INTEGER       NOT NULL,
    porcentaje            NUMERIC(8, 2) NOT NULL,
    puntaje               NUMERIC(8, 2) NOT NULL,
    puntaje_maximo        NUMERIC(8, 2) NOT NULL,
    compilacion_exitosa   BOOLEAN       NOT NULL,
    respondido_en         TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    UNIQUE (intento_id, pregunta_id)
);

CREATE INDEX idx_intento_evaluacion ON intento (evaluacion_id);
CREATE INDEX idx_respuesta_intento ON respuesta_intento (intento_id);
