INSERT INTO rm_kpi_summary(period_from, period_to, department_id, team_id, person_id, responses, unique_authors, unique_targets, avg_rating, positive_share, negative_share)
VALUES ('2026-01-01', '2026-03-31', 'd_1', 't_2', NULL, 120, 34, 18, 4.2, 0.76, 0.24);

INSERT INTO rm_person_positivity(period_from, period_to, department_id, team_id, person_id, display_name, positive, negative, total, avg_rating)
VALUES
('2026-01-01', '2026-03-31', 'd_1', 't_2', 'p_11', 'Ivan P.', 18, 4, 22, 4.6),
('2026-01-01', '2026-03-31', 'd_1', 't_2', 'p_12', 'Olga K.', 14, 5, 19, 4.1),
('2026-01-01', '2026-03-31', 'd_1', 't_2', 'p_13', 'Mira S.', 10, 7, 17, 3.8);

INSERT INTO rm_subcategory_freq(period_from, period_to, department_id, team_id, person_id, category_id, subcategory_id, subcategory_name, positive, negative, total)
VALUES
('2026-01-01', '2026-03-31', 'd_1', 't_2', NULL, 'cat_1', 'sub_1', 'Communication', 25, 2, 27),
('2026-01-01', '2026-03-31', 'd_1', 't_2', NULL, 'cat_1', 'sub_2', 'Delivery', 17, 4, 21),
('2026-01-01', '2026-03-31', 'd_1', 't_2', NULL, 'cat_2', 'sub_3', 'Teamwork', 14, 6, 20);

INSERT INTO rm_category_ratings(period_from, period_to, department_id, team_id, person_id, category_id, category_name, avg_rating, count)
VALUES
('2026-01-01', '2026-03-31', 'd_1', 't_2', NULL, 'cat_1', 'Performance', 4.1, 55),
('2026-01-01', '2026-03-31', 'd_1', 't_2', NULL, 'cat_2', 'Culture', 4.4, 65);

INSERT INTO rm_trend_points(metric, granularity, x_value, y_value, period_from, period_to, department_id, team_id, person_id)
VALUES
('responses', 'month', '2026-01', 12, '2026-01-01', '2026-03-31', 'd_1', 't_2', NULL),
('responses', 'month', '2026-02', 19, '2026-01-01', '2026-03-31', 'd_1', 't_2', NULL),
('responses', 'month', '2026-03', 25, '2026-01-01', '2026-03-31', 'd_1', 't_2', NULL),
('avgRating', 'month', '2026-01', 4.0, '2026-01-01', '2026-03-31', 'd_1', 't_2', NULL),
('avgRating', 'month', '2026-02', 4.2, '2026-01-01', '2026-03-31', 'd_1', 't_2', NULL),
('avgRating', 'month', '2026-03', 4.4, '2026-01-01', '2026-03-31', 'd_1', 't_2', NULL);

INSERT INTO rm_insights(period_from, period_to, department_id, team_id, type, subcategory_id, name, avg_rating, count)
VALUES
('2026-01-01', '2026-03-31', 'd_1', 't_2', 'best', 'sub_1', 'Communication', 4.8, 12),
('2026-01-01', '2026-03-31', 'd_1', 't_2', 'best', 'sub_2', 'Delivery', 4.6, 10),
('2026-01-01', '2026-03-31', 'd_1', 't_2', 'worst', 'sub_9', 'Ownership', 2.1, 9),
('2026-01-01', '2026-03-31', 'd_1', 't_2', 'worst', 'sub_8', 'Escalation', 2.4, 8);

INSERT INTO rm_network_nodes(period_from, period_to, node_id, label, team_id)
VALUES
('2026-01-01', '2026-03-31', 'p_11', 'Ivan P.', 't_2'),
('2026-01-01', '2026-03-31', 'p_12', 'Olga K.', 't_2');

INSERT INTO rm_network_edges(period_from, period_to, source, target, weight, avg_rating, positive, negative, team_id)
VALUES
('2026-01-01', '2026-03-31', 'p_11', 'p_12', 5, 4.4, 4, 1, 't_2');
