CREATE TABLE evaluacion (
    id                  BIGSERIAL PRIMARY KEY,
    nombre              VARCHAR(200)  NOT NULL,
    descripcion         VARCHAR(2000) NOT NULL DEFAULT '',
    tiempo_limite_minutos INTEGER     NOT NULL,
    cantidad_preguntas  INTEGER       NOT NULL,
    creado_en           TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

ALTER TABLE evaluacion
    ADD CONSTRAINT evaluacion_tiempo_positivo CHECK (tiempo_limite_minutos >= 1);

ALTER TABLE evaluacion
    ADD CONSTRAINT evaluacion_preguntas_positivas CHECK (cantidad_preguntas >= 1);
