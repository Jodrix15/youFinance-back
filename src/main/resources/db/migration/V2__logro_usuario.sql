-- Logros desbloqueados por usuario.
CREATE TABLE `logro_usuario` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `codigo` varchar(60) NOT NULL,
  `fecha_desbloqueo` datetime(6) NOT NULL,
  `user_id` char(36) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_logro_usuario` (`user_id`,`codigo`),
  CONSTRAINT `fk_logro_usuario_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
