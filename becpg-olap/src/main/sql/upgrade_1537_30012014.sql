--
-- Statistics Fact Table
--   becpg_statistics
-- 
DROP TABLE IF EXISTS `becpg_statistics`;

CREATE TABLE `becpg_statistics` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `total_memory` DOUBLE NOT NULL, 
  `free_memory` DOUBLE NOT NULL,
  `max_memory` DOUBLE NOT NULL,
  `connected_users`  INT NOT NULL,
  `non_heap_memory_usage` DOUBLE NOT NULL,
  `instance_id`   BIGINT(20) NOT NULL,
  `statistics_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
   PRIMARY KEY (`id`),
   FOREIGN KEY (instance_id) REFERENCES becpg_instance(`id`) ON DELETE CASCADE
  ) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE utf8_unicode_ci;
  
  
ALTER TABLE `becpg_instance` ADD COLUMN `instance_state` VARCHAR(4) NOT NULL DEFAULT 'DOWN' AFTER `id`;
  
  
 