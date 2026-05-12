CREATE EXTENSION IF NOT EXISTS "pgcrypto";


CREATE TABLE bls_regions (
    code        VARCHAR(10)  PRIMARY KEY,
    name        VARCHAR(50)  NOT NULL,
    bls_suffix  VARCHAR(10)  NOT NULL,
    zip_prefix_ranges TEXT
);

INSERT INTO bls_regions VALUES
('national',  'National Average', '0000', '["all"]'),
('northeast', 'Northeast',        '0100', '["006-029","100-212","214-279"]'),
('midwest',   'Midwest',          '0200', '["460-480","490-499","530-549","550-569","600-641","660-679","690-697"]'),
('south',     'South',            '0300', '["195-212","245-268","270-299","300-342","345-349","355-379","386-397","700-799","850-855"]'),
('west',      'West',             '0400', '["800-816","820-831","835-849","856-857","890-898","900-961","970-994","995-999"]');



CREATE TABLE stores (
    id          VARCHAR(20)  PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    logo_url    VARCHAR(255),
    website_url VARCHAR(255),
    is_active   BOOLEAN      DEFAULT TRUE,
    created_at  TIMESTAMP    DEFAULT NOW()
);

INSERT INTO stores (id, name) VALUES
('walmart',     'Walmart'),
('kroger',      'Kroger'),
('aldi',        'Aldi'),
('target',      'Target'),
('whole_foods', 'Whole Foods'),
('trader_joes', 'Trader Joe''s');



CREATE TABLE store_price_factors (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id     VARCHAR(20)  REFERENCES stores(id),
    category     VARCHAR(50)  NOT NULL,
    factor       DECIMAL(5,3) NOT NULL,
    source       VARCHAR(100),
    last_updated DATE         DEFAULT CURRENT_DATE,
    UNIQUE(store_id, category)
);


INSERT INTO store_price_factors (store_id, category, factor, source) VALUES
('walmart',     'all', 0.920, 'Grocery industry benchmark — Walmart typically 8% below average'),
('kroger',      'all', 1.000, 'Grocery industry benchmark — Kroger at market average'),
('aldi',        'all', 0.750, 'Grocery industry benchmark — Aldi typically 25% below average'),
('target',      'all', 1.080, 'Grocery industry benchmark — Target typically 8% above average'),
('whole_foods', 'all', 1.250, 'Grocery industry benchmark — Whole Foods typically 25% above average'),
('trader_joes', 'all', 0.950, 'Grocery industry benchmark — Trader Joe''s typically 5% below average');



CREATE TABLE products (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    bls_series_id VARCHAR(30)  UNIQUE,
    bls_item_code VARCHAR(20),
    name          VARCHAR(200) NOT NULL,
    short_name    VARCHAR(100) NOT NULL,
    category      VARCHAR(50)  NOT NULL,
    unit          VARCHAR(30)  NOT NULL,
    image_url     VARCHAR(255),
    is_active     BOOLEAN      DEFAULT TRUE,
    created_at    TIMESTAMP    DEFAULT NOW(),
    updated_at    TIMESTAMP    DEFAULT NOW()
);

CREATE INDEX idx_products_category ON products(category) WHERE is_active = TRUE;
CREATE INDEX idx_products_bls ON products(bls_series_id);



CREATE TABLE users (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    email           VARCHAR(255) UNIQUE NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    name            VARCHAR(100) NOT NULL,
    zip_code        VARCHAR(10),
    bls_region      VARCHAR(10)  DEFAULT 'national' REFERENCES bls_regions(code),
    search_radius_miles INTEGER  DEFAULT 10,
    is_active       BOOLEAN      DEFAULT TRUE,
    created_at      TIMESTAMP    DEFAULT NOW(),
    updated_at      TIMESTAMP    DEFAULT NOW(),
    last_login_at   TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);



CREATE TABLE notification_preferences (
    id                UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id           UUID    UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    price_drop        BOOLEAN DEFAULT TRUE,
    anomaly_warning   BOOLEAN DEFAULT TRUE,
    deal_dna          BOOLEAN DEFAULT TRUE,
    forecast_update   BOOLEAN DEFAULT TRUE,
    weekly_email      BOOLEAN DEFAULT TRUE,
    updated_at        TIMESTAMP DEFAULT NOW()
);



CREATE TABLE user_tracked_stores (
    user_id     UUID        REFERENCES users(id)  ON DELETE CASCADE,
    store_id    VARCHAR(20) REFERENCES stores(id) ON DELETE CASCADE,
    added_at    TIMESTAMP   DEFAULT NOW(),
    PRIMARY KEY (user_id, store_id)
);


CREATE TABLE user_product_tracking (
    user_id      UUID         REFERENCES users(id)    ON DELETE CASCADE,
    product_id   UUID         REFERENCES products(id) ON DELETE CASCADE,
    target_price DECIMAL(8,2),
    added_at     TIMESTAMP    DEFAULT NOW(),
    PRIMARY KEY  (user_id, product_id)
);



CREATE TABLE price_history (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id   UUID         REFERENCES products(id),
    region_code  VARCHAR(10)  REFERENCES bls_regions(code),
    price        DECIMAL(8,4) NOT NULL,
    price_date   DATE         NOT NULL,
    data_source  VARCHAR(20)  NOT NULL DEFAULT 'bls',
    created_at   TIMESTAMP    DEFAULT NOW()
);


CREATE INDEX idx_price_history_product_date
    ON price_history(product_id, price_date DESC);

CREATE INDEX idx_price_history_region_date
    ON price_history(region_code, price_date DESC);

CREATE INDEX idx_price_history_product_region_date
    ON price_history(product_id, region_code, price_date DESC);


CREATE UNIQUE INDEX idx_price_history_unique
    ON price_history(product_id, region_code, price_date, data_source);



CREATE TABLE macro_indicators (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    indicator_code  VARCHAR(20)  NOT NULL,
    indicator_name  VARCHAR(100) NOT NULL,
    value           DECIMAL(14,6) NOT NULL,
    indicator_date  DATE         NOT NULL,
    frequency       VARCHAR(10)  NOT NULL DEFAULT 'monthly',
    created_at      TIMESTAMP    DEFAULT NOW()
);

CREATE UNIQUE INDEX idx_macro_unique
    ON macro_indicators(indicator_code, indicator_date);

CREATE INDEX idx_macro_date
    ON macro_indicators(indicator_date DESC);

INSERT INTO macro_indicators (indicator_code, indicator_name, value, indicator_date, frequency)
VALUES
('CPIAUCSL',      'Consumer Price Index - All Urban Consumers',         0, '2020-01-01', 'monthly'),
('CPIFABSL',      'CPI - Food and Beverages',                          0, '2020-01-01', 'monthly'),
('CUSR0000SAF11', 'CPI - Food at Home',                                0, '2020-01-01', 'monthly'),
('GASREGW',       'US Regular Conventional Gas Price (per gallon)',     0, '2020-01-01', 'weekly'),
('UNRATE',        'Unemployment Rate',                                   0, '2020-01-01', 'monthly'),
('UMCSENT',       'University of Michigan Consumer Sentiment Index',     0, '2020-01-01', 'monthly'),
('DCOILWTICO',    'Crude Oil Price WTI (per barrel)',                   0, '2020-01-01', 'daily'),
('DEXUSEU',       'US Dollar to Euro Exchange Rate',                    0, '2020-01-01', 'daily')
ON CONFLICT DO NOTHING;



CREATE TABLE price_forecasts (
    id               UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id       UUID         REFERENCES products(id),
    region_code      VARCHAR(10)  REFERENCES bls_regions(code),
    forecast_month   DATE         NOT NULL,
    current_price    DECIMAL(8,4) NOT NULL,
    predicted_price  DECIMAL(8,4) NOT NULL,
    percent_change   DECIMAL(6,2) NOT NULL,
    recommendation   VARCHAR(10)  NOT NULL CHECK (recommendation IN ('buy_now', 'wait')),
    confidence_score INTEGER      CHECK (confidence_score BETWEEN 0 AND 100),
    mape_error       DECIMAL(6,3) DEFAULT 3.03,
    model_version    VARCHAR(20)  DEFAULT 'ensemble_v1',
    generated_at     TIMESTAMP    DEFAULT NOW(),
    expires_at       TIMESTAMP    DEFAULT NOW() + INTERVAL '7 days',
    is_active        BOOLEAN      DEFAULT TRUE
);

CREATE INDEX idx_forecasts_product_region
    ON price_forecasts(product_id, region_code, forecast_month DESC);

CREATE INDEX idx_forecasts_active_expiry
    ON price_forecasts(is_active, expires_at)
    WHERE is_active = TRUE;


CREATE TABLE anomaly_detections (
    id                    UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id            UUID         REFERENCES products(id),
    region_code           VARCHAR(10)  REFERENCES bls_regions(code),
    detection_date        DATE         NOT NULL,
    reconstruction_error  DECIMAL(12,8) NOT NULL,
    threshold_used        DECIMAL(12,8) NOT NULL,
    is_anomalous          BOOLEAN      NOT NULL,
    severity              VARCHAR(10)  CHECK (severity IN ('none', 'low', 'medium', 'high')),
    notes                 TEXT,
    created_at            TIMESTAMP    DEFAULT NOW()
);

CREATE INDEX idx_anomaly_product_date
    ON anomaly_detections(product_id, detection_date DESC);

CREATE INDEX idx_anomaly_active
    ON anomaly_detections(is_anomalous, detection_date DESC)
    WHERE is_anomalous = TRUE;



CREATE TABLE inflation_shield_scores (
    id                UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id        UUID        REFERENCES products(id),
    region_code       VARCHAR(10) REFERENCES bls_regions(code),
    score             INTEGER     NOT NULL CHECK (score BETWEEN 0 AND 100),
    volatility_12m    DECIMAL(8,4),
    price_change_12m  DECIMAL(6,2),
    max_swing_12m     DECIMAL(6,2),
    calculated_at     TIMESTAMP   DEFAULT NOW(),
    UNIQUE(product_id, region_code)
);

CREATE INDEX idx_shield_score ON inflation_shield_scores(score);



CREATE TABLE deal_dna_patterns (
    id                    UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id            UUID        REFERENCES products(id),
    store_id              VARCHAR(20) REFERENCES stores(id),
    sale_frequency_days   INTEGER,
    typical_discount_pct  DECIMAL(5,2),
    last_sale_detected    DATE,
    next_predicted_sale   DATE,
    confidence_score      INTEGER     CHECK (confidence_score BETWEEN 0 AND 100),
    pattern_notes         TEXT,
    is_active             BOOLEAN     DEFAULT TRUE,
    last_calculated       DATE        DEFAULT CURRENT_DATE,
    UNIQUE(product_id, store_id)
);



CREATE TABLE user_cart_items (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID         REFERENCES users(id)    ON DELETE CASCADE,
    product_id  UUID         REFERENCES products(id) ON DELETE CASCADE,
    quantity    DECIMAL(6,2) DEFAULT 1,
    unit_override VARCHAR(20),
    notes       VARCHAR(200),
    added_at    TIMESTAMP    DEFAULT NOW(),
    UNIQUE(user_id, product_id)
);

CREATE INDEX idx_cart_user ON user_cart_items(user_id);



CREATE TABLE cart_optimizations (
    id                    UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id               UUID          REFERENCES users(id) ON DELETE CASCADE,
    total_items           INTEGER       NOT NULL,
    cheapest_single_total DECIMAL(8,2),
    cheapest_single_store VARCHAR(20)   REFERENCES stores(id),
    optimized_total       DECIMAL(8,2),
    weekly_savings        DECIMAL(8,2),
    annual_projection     DECIMAL(10,2),
    store_breakdown       JSONB         NOT NULL,
    wait_one_month_total  DECIMAL(8,2),
    wait_one_month_savings DECIMAL(8,2),
    calculated_at         TIMESTAMP     DEFAULT NOW(),
    expires_at            TIMESTAMP     DEFAULT NOW() + INTERVAL '1 day'
);

CREATE INDEX idx_cart_opt_user ON cart_optimizations(user_id, expires_at DESC);



CREATE TABLE alerts (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID         REFERENCES users(id)    ON DELETE CASCADE,
    product_id   UUID         REFERENCES products(id) ON DELETE SET NULL,
    store_id     VARCHAR(20)  REFERENCES stores(id)   ON DELETE SET NULL,
    alert_type   VARCHAR(30)  NOT NULL CHECK (alert_type IN (
                     'price_drop',
                     'anomaly',
                     'deal_dna',
                     'forecast_update',
                     'volatility_warning',
                     'target_met'
                 )),
    title        VARCHAR(200) NOT NULL,
    description  TEXT         NOT NULL,
    action_text  VARCHAR(200),
    is_read      BOOLEAN      DEFAULT FALSE,
    created_at   TIMESTAMP    DEFAULT NOW()
);

CREATE INDEX idx_alerts_user_unread
    ON alerts(user_id, created_at DESC)
    WHERE is_read = FALSE;

CREATE INDEX idx_alerts_user_all
    ON alerts(user_id, created_at DESC);
