-- Sample job mappings for testing
INSERT INTO job_rest_mapping (job_name, service_name, url, port) VALUES
('data-processing', 'data-service', 'http://localhost', 8081),
('report-generation', 'report-service', 'http://localhost', 8082),
('notification-send', 'notification-service', 'http://localhost', 8083);

