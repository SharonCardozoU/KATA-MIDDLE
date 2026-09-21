CREATE TABLE pregunta (
    id             BIGSERIAL PRIMARY KEY,
    evaluacion_id  BIGINT       NOT NULL REFERENCES evaluacion (id) ON DELETE CASCADE,
    nombre         VARCHAR(200) NOT NULL,
    descripcion    VARCHAR(4000) NOT NULL,
    lenguaje       VARCHAR(40)  NOT NULL,
    creado_en      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE caso_prueba (
    id               BIGSERIAL PRIMARY KEY,
    pregunta_id      BIGINT        NOT NULL REFERENCES pregunta (id) ON DELETE CASCADE,
    orden            INTEGER       NOT NULL,
    entrada          VARCHAR(8000) NOT NULL DEFAULT '',
    salida_esperada  VARCHAR(8000) NOT NULL
);

CREATE INDEX idx_pregunta_evaluacion ON pregunta (evaluacion_id);
CREATE INDEX idx_caso_pregunta ON caso_prueba (pregunta_id, orden);
