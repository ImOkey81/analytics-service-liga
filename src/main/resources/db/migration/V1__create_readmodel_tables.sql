CREATE TABLE rm_kpi_summary (
    id BIGSERIAL PRIMARY KEY,
    period_from DATE NOT NULL,
    period_to DATE NOT NULL,
    department_id VARCHAR(64),
    team_id VARCHAR(64),
    person_id VARCHAR(64),
    responses BIGINT NOT NULL,
    unique_authors BIGINT NOT NULL,
    unique_targets BIGINT NOT NULL,
    avg_rating DOUBLE PRECISION NOT NULL,
    positive_share DOUBLE PRECISION NOT NULL,
    negative_share DOUBLE PRECISION NOT NULL
);

CREATE TABLE rm_person_positivity (
    id BIGSERIAL PRIMARY KEY,
    period_from DATE NOT NULL,
    period_to DATE NOT NULL,
    department_id VARCHAR(64),
    team_id VARCHAR(64),
    person_id VARCHAR(64),
    display_name VARCHAR(255) NOT NULL,
    positive BIGINT NOT NULL,
    negative BIGINT NOT NULL,
    total BIGINT NOT NULL,
    avg_rating DOUBLE PRECISION NOT NULL
);

CREATE TABLE rm_subcategory_freq (
    id BIGSERIAL PRIMARY KEY,
    period_from DATE NOT NULL,
    period_to DATE NOT NULL,
    department_id VARCHAR(64),
    team_id VARCHAR(64),
    person_id VARCHAR(64),
    category_id VARCHAR(64),
    subcategory_id VARCHAR(64) NOT NULL,
    subcategory_name VARCHAR(255) NOT NULL,
    positive BIGINT NOT NULL,
    negative BIGINT NOT NULL,
    total BIGINT NOT NULL
);

CREATE TABLE rm_category_ratings (
    id BIGSERIAL PRIMARY KEY,
    period_from DATE NOT NULL,
    period_to DATE NOT NULL,
    department_id VARCHAR(64),
    team_id VARCHAR(64),
    person_id VARCHAR(64),
    category_id VARCHAR(64) NOT NULL,
    category_name VARCHAR(255) NOT NULL,
    avg_rating DOUBLE PRECISION NOT NULL,
    count BIGINT NOT NULL
);

CREATE TABLE rm_trend_points (
    id BIGSERIAL PRIMARY KEY,
    metric VARCHAR(64) NOT NULL,
    granularity VARCHAR(16) NOT NULL,
    x_value VARCHAR(16) NOT NULL,
    y_value DOUBLE PRECISION NOT NULL,
    period_from DATE NOT NULL,
    period_to DATE NOT NULL,
    department_id VARCHAR(64),
    team_id VARCHAR(64),
    person_id VARCHAR(64)
);

CREATE TABLE rm_insights (
    id BIGSERIAL PRIMARY KEY,
    period_from DATE NOT NULL,
    period_to DATE NOT NULL,
    department_id VARCHAR(64),
    team_id VARCHAR(64),
    type VARCHAR(16) NOT NULL,
    subcategory_id VARCHAR(64) NOT NULL,
    name VARCHAR(255) NOT NULL,
    avg_rating DOUBLE PRECISION NOT NULL,
    count BIGINT NOT NULL
);

CREATE TABLE rm_network_nodes (
    id BIGSERIAL PRIMARY KEY,
    period_from DATE NOT NULL,
    period_to DATE NOT NULL,
    node_id VARCHAR(64) NOT NULL,
    label VARCHAR(255) NOT NULL,
    team_id VARCHAR(64)
);

CREATE TABLE rm_network_edges (
    id BIGSERIAL PRIMARY KEY,
    period_from DATE NOT NULL,
    period_to DATE NOT NULL,
    source VARCHAR(64) NOT NULL,
    target VARCHAR(64) NOT NULL,
    weight BIGINT NOT NULL,
    avg_rating DOUBLE PRECISION NOT NULL,
    positive BIGINT NOT NULL,
    negative BIGINT NOT NULL,
    team_id VARCHAR(64)
);
