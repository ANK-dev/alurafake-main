CREATE TABLE IF NOT EXISTS TaskOption (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  optionText varchar(80) NOT NULL,
  isCorrect boolean NOT NULL,
  task_id bigint(20) NOT NULL,
  PRIMARY KEY (id),
  INDEX idx_option_task (task_id),
  CONSTRAINT fk_option_task FOREIGN KEY (task_id) REFERENCES Task (id)
    ON UPDATE CASCADE
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
