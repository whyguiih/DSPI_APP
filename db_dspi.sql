-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Tempo de geração: 11/05/2026 às 19:17
-- Versão do servidor: 10.4.32-MariaDB
-- Versão do PHP: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Banco de dados: `db_amanda`
--
DROP DATABASE IF EXISTS `db_amanda`;
CREATE DATABASE IF NOT EXISTS `db_amanda` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `db_amanda`;

-- --------------------------------------------------------

--
-- Estrutura para tabela `tb_cadastros`
--

DROP TABLE IF EXISTS `tb_cadastros`;
CREATE TABLE IF NOT EXISTS `tb_cadastros` (
  `id_cadastro` int(11) NOT NULL AUTO_INCREMENT,
  `nome_usuarios` varchar(70) NOT NULL,
  `senha` varchar(25) NOT NULL,
  `nivel_de_acesso` int(11) DEFAULT 0,
  PRIMARY KEY (`id_cadastro`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estrutura para tabela `tb_canva`
--

DROP TABLE IF EXISTS `tb_canva`;
CREATE TABLE IF NOT EXISTS `tb_canva` (
  `id_canva` int(11) NOT NULL,
  `atividades_chaves` text NOT NULL,
  `proposta_chave` text NOT NULL,
  `relacionamentos_clientes` text NOT NULL,
  `segmentos_clientes` text NOT NULL,
  `recursos_chaves` text NOT NULL,
  `canais` text NOT NULL,
  `estrutura_custos` text NOT NULL,
  `fluxo_receita` text DEFAULT NULL,
  `parceiros_chaves` text NOT NULL,
  PRIMARY KEY (`id_canva`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estrutura para tabela `tb_conhecimentos`
--

DROP TABLE IF EXISTS `tb_conhecimentos`;
CREATE TABLE IF NOT EXISTS `tb_conhecimentos` (
  `id_conhecimentos` int(11) NOT NULL,
  `plano_curso` text DEFAULT NULL,
  `conhecimentos_aplicados` text NOT NULL,
  `capacidades_aplicadas` text NOT NULL,
  PRIMARY KEY (`id_conhecimentos`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estrutura para tabela `tb_cronograma`
--

DROP TABLE IF EXISTS `tb_cronograma`;
CREATE TABLE IF NOT EXISTS `tb_cronograma` (
  `id_cronograma` int(11) NOT NULL,
  `processo` text NOT NULL,
  `etapas` text NOT NULL,
  `responsavel` varchar(70) NOT NULL,
  `data_inicio` date DEFAULT NULL,
  `data_final` date DEFAULT NULL,
  `observacoes` text NOT NULL,
  PRIMARY KEY (`id_cronograma`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estrutura para tabela `tb_curriculo_alunos`
--

DROP TABLE IF EXISTS `tb_curriculo_alunos`;
CREATE TABLE IF NOT EXISTS `tb_curriculo_alunos` (
  `id_aluno` int(11) NOT NULL,
  `nome` varchar(70) NOT NULL,
  `data_nacimento` date DEFAULT NULL,
  `cpf` varchar(20) NOT NULL,
  `empresa_vinculado` varchar(100) DEFAULT NULL,
  `projeto` varchar(250) NOT NULL,
  `telefone` varchar(15) NOT NULL,
  `email` varchar(250) NOT NULL,
  `nome_reponsavel` varchar(70) DEFAULT NULL,
  `numero_responsavel` varchar(15) DEFAULT NULL,
  `email_repsonsavel` varchar(250) DEFAULT NULL,
  `habilidades` text DEFAULT NULL,
  `fez_projeto` varchar(500) DEFAULT NULL,
  `cidade` varchar(50) DEFAULT NULL,
  `motivo_projeto` varchar(500) NOT NULL,
  `aprendo_mais` varchar(100) DEFAULT NULL,
  `prefiro_trabalhar` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id_aluno`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estrutura para tabela `tb_equipe`
--

DROP TABLE IF EXISTS `tb_equipe`;
CREATE TABLE IF NOT EXISTS `tb_equipe` (
  `id_equipe` int(11) NOT NULL AUTO_INCREMENT,
  `nome_integrante` varchar(70) NOT NULL,
  `nome_equipe` varchar(250) DEFAULT NULL,
  `nome_projeto` varchar(250) NOT NULL,
  `email` varchar(250) NOT NULL,
  `area_atuacao_curso` text NOT NULL,
  `area_atuacao_projeto` text NOT NULL,
  PRIMARY KEY (`id_equipe`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estrutura para tabela `tb_pitch`
--

DROP TABLE IF EXISTS `tb_pitch`;
CREATE TABLE IF NOT EXISTS `tb_pitch` (
  `id_pitch` int(11) NOT NULL,
  `roteiro` text NOT NULL,
  PRIMARY KEY (`id_pitch`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estrutura para tabela `tb_recursos_aplicados`
--

DROP TABLE IF EXISTS `tb_recursos_aplicados`;
CREATE TABLE IF NOT EXISTS `tb_recursos_aplicados` (
  `id_recursos` int(11) NOT NULL,
  `ferramentas` varchar(150) NOT NULL,
  `equipamentos` varchar(150) NOT NULL,
  `descricao_produto` varchar(350) NOT NULL,
  `quant_comprada` varchar(3) NOT NULL,
  `quant_utilizada` varchar(3) NOT NULL,
  `preco_estimado` decimal(10,2) NOT NULL,
  `uni_medida` varchar(10) DEFAULT NULL,
  `fornecedor_principal` varchar(150) NOT NULL,
  `modo_obtencao` varchar(200) NOT NULL,
  `disponibilidade` varchar(100) DEFAULT NULL,
  `pagamento` varchar(15) DEFAULT NULL,
  `alternativas_consideradas` varchar(200) DEFAULT NULL,
  `preco_total` decimal(11,2) NOT NULL,
  PRIMARY KEY (`id_recursos`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Restrições para tabelas despejadas
--

--
-- Restrições para tabelas `tb_canva`
--
ALTER TABLE `tb_canva`
  ADD CONSTRAINT `tb_canva_ibfk_1` FOREIGN KEY (`id_canva`) REFERENCES `tb_equipe` (`id_equipe`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Restrições para tabelas `tb_conhecimentos`
--
ALTER TABLE `tb_conhecimentos`
  ADD CONSTRAINT `tb_conhecimentos_ibfk_1` FOREIGN KEY (`id_conhecimentos`) REFERENCES `tb_equipe` (`id_equipe`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Restrições para tabelas `tb_cronograma`
--
ALTER TABLE `tb_cronograma`
  ADD CONSTRAINT `tb_cronograma_ibfk_1` FOREIGN KEY (`id_cronograma`) REFERENCES `tb_equipe` (`id_equipe`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Restrições para tabelas `tb_curriculo_alunos`
--
ALTER TABLE `tb_curriculo_alunos`
  ADD CONSTRAINT `tb_curriculo_alunos_ibfk_1` FOREIGN KEY (`id_aluno`) REFERENCES `tb_equipe` (`id_equipe`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Restrições para tabelas `tb_pitch`
--
ALTER TABLE `tb_pitch`
  ADD CONSTRAINT `tb_pitch_ibfk_1` FOREIGN KEY (`id_pitch`) REFERENCES `tb_equipe` (`id_equipe`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Restrições para tabelas `tb_recursos_aplicados`
--
ALTER TABLE `tb_recursos_aplicados`
  ADD CONSTRAINT `tb_recursos_aplicados_ibfk_1` FOREIGN KEY (`id_recursos`) REFERENCES `tb_equipe` (`id_equipe`) ON DELETE CASCADE ON UPDATE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
