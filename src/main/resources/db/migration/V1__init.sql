-- Esquema inicial (baseline) de YouFinance.
-- Generado a partir del esquema real (dump MySQL 8). Solo estructura, sin datos.
-- Tablas ordenadas por dependencias de clave foránea para poder crearse en una
-- base de datos vacía. En BDs ya existentes este script NO se ejecuta: Flyway
-- las marca como baseline (V1) sin tocarlas.

CREATE TABLE `users` (
  `id` char(36) NOT NULL,
  `dashboard_config` longtext,
  `email` varchar(255) DEFAULT NULL,
  `foto_perfil` longtext,
  `idioma` varchar(8) NOT NULL DEFAULT 'es',
  `moneda` varchar(3) NOT NULL DEFAULT 'EUR',
  `password` varchar(255) NOT NULL,
  `role` enum('ROLE_ADMIN','ROLE_USER') NOT NULL,
  `username` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKr43af9ap4edm43mmtq01oddj6` (`username`),
  UNIQUE KEY `UK6dotkott2kjsp8vw4d0m25fb7` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `categoria` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nombre` varchar(255) NOT NULL,
  `tipo_movimiento` enum('GASTO','INGRESO','INVERSION') NOT NULL,
  `user_id` char(36) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKcro9w3htpfcxx9qkwpth7eyv1` (`user_id`),
  CONSTRAINT `FKcro9w3htpfcxx9qkwpth7eyv1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `cuenta` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `importe` decimal(38,2) NOT NULL,
  `nombre` varchar(255) NOT NULL,
  `user_id` char(36) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKpddi3u98q5pklvtnvusxgwrhv` (`user_id`),
  CONSTRAINT `FKpddi3u98q5pklvtnvusxgwrhv` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `deuda` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `acreedor` varchar(255) NOT NULL,
  `cantidad_pagada` decimal(38,2) NOT NULL,
  `cuota` decimal(38,2) DEFAULT NULL,
  `fecha_vencimiento` date DEFAULT NULL,
  `frecuencia` enum('ANUAL','MENSUAL') DEFAULT NULL,
  `importe` decimal(38,2) NOT NULL,
  `interes` decimal(38,2) DEFAULT NULL,
  `nombre` varchar(255) NOT NULL,
  `user_id` char(36) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK8wan2k4rfr2611tole7ix0p4f` (`user_id`),
  CONSTRAINT `FK8wan2k4rfr2611tole7ix0p4f` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `feedback` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `categoria` enum('INCIDENCIA','MEJORA','OTRO','PREGUNTA') NOT NULL,
  `fecha_creacion` datetime(6) NOT NULL,
  `mensaje` varchar(2000) NOT NULL,
  `user_id` char(36) NOT NULL,
  `estado` enum('DESCARTADA','PENDIENTE','RESUELTA') NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKpwwmhguqianghvi1wohmtsm8l` (`user_id`),
  CONSTRAINT `FKpwwmhguqianghvi1wohmtsm8l` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `patrimonio_snapshot` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `actualizado_en` datetime(6) NOT NULL,
  `mes` date NOT NULL,
  `patrimonio_neto` decimal(38,2) NOT NULL,
  `total_cuentas` decimal(38,2) NOT NULL,
  `total_deudas` decimal(38,2) NOT NULL,
  `total_inversiones` decimal(38,2) NOT NULL,
  `user_id` char(36) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_snapshot_user_mes` (`user_id`,`mes`),
  CONSTRAINT `FKr0al4jw75vmyg4lta9ebu9n5w` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `presupuesto` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `anio` int NOT NULL,
  `cantidad_base` decimal(38,2) NOT NULL,
  `descontar_gastos_fijos` bit(1) NOT NULL,
  `fecha_creacion` date NOT NULL,
  `mes` int NOT NULL,
  `nombre` varchar(255) NOT NULL,
  `periodo` enum('MENSUAL','SEMANAL') NOT NULL,
  `semana` int DEFAULT NULL,
  `user_id` char(36) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKbskpku6mqenc4dvdnit81wmmt` (`user_id`),
  CONSTRAINT `FKbskpku6mqenc4dvdnit81wmmt` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `gasto_recurrente` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `fecha_primer_pago` date DEFAULT NULL,
  `fecha_ultimo_pago` date DEFAULT NULL,
  `frecuencia` enum('ANUAL','MENSUAL') DEFAULT NULL,
  `is_active` bit(1) NOT NULL,
  `nombre` varchar(255) NOT NULL,
  `tipo_pago` enum('RECURRENTE','SUSCRIPCION') DEFAULT NULL,
  `categoria_id` bigint NOT NULL,
  `user_id` char(36) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK5cpfq1asoc3opv04jdr5jwp20` (`categoria_id`),
  KEY `FKpuqrei0c4k3qoc424l0gfs8kk` (`user_id`),
  CONSTRAINT `FK5cpfq1asoc3opv04jdr5jwp20` FOREIGN KEY (`categoria_id`) REFERENCES `categoria` (`id`),
  CONSTRAINT `FKpuqrei0c4k3qoc424l0gfs8kk` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `inversion` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `capital_aportado` decimal(38,2) NOT NULL,
  `capital_total` decimal(38,2) NOT NULL,
  `categoria_id` bigint NOT NULL,
  `user_id` char(36) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKm9n97fyou08b3h20mgtri2x77` (`categoria_id`),
  KEY `FKbyay4df09wp9mq1bht3fpdlio` (`user_id`),
  CONSTRAINT `FKbyay4df09wp9mq1bht3fpdlio` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKm9n97fyou08b3h20mgtri2x77` FOREIGN KEY (`categoria_id`) REFERENCES `categoria` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `presupuesto_partida` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `importe` decimal(38,2) NOT NULL,
  `nombre` varchar(255) DEFAULT NULL,
  `categoria_id` bigint DEFAULT NULL,
  `presupuesto_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK4ta999mjad55h0fc2ptwbsaxg` (`categoria_id`),
  KEY `FKp0m4p5oycty33ox3m0uck0287` (`presupuesto_id`),
  CONSTRAINT `FK4ta999mjad55h0fc2ptwbsaxg` FOREIGN KEY (`categoria_id`) REFERENCES `categoria` (`id`),
  CONSTRAINT `FKp0m4p5oycty33ox3m0uck0287` FOREIGN KEY (`presupuesto_id`) REFERENCES `presupuesto` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `recurrente_precio` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `fecha_variacion_importe` date NOT NULL,
  `importe` decimal(38,2) NOT NULL,
  `gasto_recurrente_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKe9ri4p1kd0pw0ybnacjsh3ppg` (`gasto_recurrente_id`),
  CONSTRAINT `FKe9ri4p1kd0pw0ybnacjsh3ppg` FOREIGN KEY (`gasto_recurrente_id`) REFERENCES `gasto_recurrente` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `transaccion` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `descripcion` varchar(255) DEFAULT NULL,
  `fecha_transaccion` date DEFAULT NULL,
  `importe` decimal(38,2) NOT NULL,
  `tipo_movimiento` enum('GASTO','INGRESO','INVERSION') DEFAULT NULL,
  `categoria_id` bigint NOT NULL,
  `cuenta_id` bigint NOT NULL,
  `user_id` char(36) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKk7db1p3y2mxyhrflylujs3bx7` (`categoria_id`),
  KEY `FKkkale73n3p5vwbgxa49yiyqgx` (`cuenta_id`),
  KEY `FKb9y4n0lxwlbm49ufkg4r4spqi` (`user_id`),
  CONSTRAINT `FKb9y4n0lxwlbm49ufkg4r4spqi` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKk7db1p3y2mxyhrflylujs3bx7` FOREIGN KEY (`categoria_id`) REFERENCES `categoria` (`id`),
  CONSTRAINT `FKkkale73n3p5vwbgxa49yiyqgx` FOREIGN KEY (`cuenta_id`) REFERENCES `cuenta` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
