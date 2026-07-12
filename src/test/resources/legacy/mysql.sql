INSERT INTO users (id, username, xp, created_at) VALUES
    (101, 'legacy-alice', 90, '2025-01-01 10:00:00.000000'),
    (102, 'legacy-bob', 40, '2025-01-02 10:00:00.000000');

INSERT INTO challenges (id, title, difficulty, context, expected_output_format, created_at) VALUES
    (201, '单用户架构题', 'INTERMEDIATE', '为全球用户设计可靠的订单服务。', 'DESIGN', '2025-01-03 10:00:00.000000'),
    (202, '共享系统设计题', 'ADVANCED', '设计支持多租户与审计能力的平台。', 'DESIGN', '2025-01-04 10:00:00.000000');

INSERT INTO challenge_requirements (challenge_id, requirement) VALUES
    (201, '说明一致性策略'),
    (202, '说明租户隔离'),
    (202, '说明审计留痕');
INSERT INTO challenge_constraints (challenge_id, constraint_text) VALUES
    (201, '峰值每秒一万请求'),
    (202, '跨区域部署');
INSERT INTO challenge_acceptance_criteria (challenge_id, criterion) VALUES
    (201, '方案能够降级'),
    (202, '共享数据不可泄漏');

INSERT INTO submissions (id, user_id, challenge_id, answer, submitted_at) VALUES
    (301, 101, 201, CONCAT('迁移前回答🙂—设计边界；', REPEAT('幂等、重试、补偿、可观测性。', 350)), '2025-01-05 10:00:00.000000'),
    (302, 101, 202, 'Alice 对共享题的回答', '2025-01-06 10:00:00.000000'),
    (303, 102, 202, 'Bob 对共享题的回答', '2025-01-07 10:00:00.000000');

INSERT INTO evaluations (
    id, submission_id, requirement_understanding, logical_clarity,
    technical_feasibility, edge_case_coverage, communication_structure,
    final_score, feedback, created_at
) VALUES
    (401, 301, 8.0, 7.5, 8.5, 7.0, 8.0, 78.0,
     CONCAT('迁移前反馈🙂—保持原文；', REPEAT('优点清晰，建议补充容量估算。', 260)), '2025-01-05 10:01:00.000000'),
    (402, 302, 7.0, 7.0, 7.5, 6.5, 7.0, 70.0,
     'Alice 的旧反馈', '2025-01-06 10:01:00.000000'),
    (403, 303, 8.5, 8.0, 8.0, 7.5, 8.5, 81.0,
     'Bob 的旧反馈', '2025-01-07 10:01:00.000000');
