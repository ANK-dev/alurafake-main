CREATE TABLE IF NOT EXISTS Task (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  orderIndex int NOT NULL,
  statement varchar(255) NOT NULL,
  type enum('OPEN_TEXT', 'MULTIPLE_CHOICE', 'SINGLE_CHOICE') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'OPEN_TEXT',
  course_id bigint(20) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT uq_task_course_statement UNIQUE (course_id, statement),
  CONSTRAINT uq_task_course_order UNIQUE (course_id, orderIndex),
  INDEX idx_task_course (course_id),
  CONSTRAINT fk_task_course FOREIGN KEY (course_id) REFERENCES Course (id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
