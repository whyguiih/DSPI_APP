PRAGMA defer_foreign_keys=TRUE;
CREATE TABLE d1_migrations(
		id         INTEGER PRIMARY KEY AUTOINCREMENT,
		name       TEXT UNIQUE,
		applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);
CREATE TABLE comments (
    id INTEGER PRIMARY KEY NOT NULL,
    author TEXT NOT NULL,
    content TEXT NOT NULL
);
CREATE TABLE tb_informacoes_completude (
  id_informacoes_completude INTEGER PRIMARY KEY AUTOINCREMENT,
  qtd INTEGER NOT NULL,
  equipe_unidade_empresa TEXT DEFAULT NULL,
  responsavel_preenchimento TEXT DEFAULT NULL,
  dados_equipe TEXT CHECK( dados_equipe IN ('Não iniciada','Parcial','Concluido') ) DEFAULT NULL,
  conhecimentos TEXT CHECK( conhecimentos IN ('Não iniciada','Parcial','Concluido') ) DEFAULT NULL,
  recursos_aplicados TEXT CHECK( recursos_aplicados IN ('Não iniciada','Parcial','Concluido') ) DEFAULT NULL,
  canvas_preencher TEXT CHECK( canvas_preencher IN ('Não iniciada','Parcial','Concluido') ) DEFAULT NULL,
  pitch_escrito TEXT CHECK( pitch_escrito IN ('Não iniciada','Parcial','Concluido') ) DEFAULT NULL,
  pitch_video TEXT CHECK( pitch_video IN ('Não iniciada','Parcial','Concluido') ) DEFAULT NULL,
  cronograma TEXT CHECK( cronograma IN ('Não iniciada','Parcial','Concluido') ) DEFAULT NULL,
  foto_equipe TEXT CHECK( foto_equipe IN ('Não iniciada','Parcial','Concluido') ) DEFAULT NULL,
  fotos_etapa_projeto TEXT CHECK( fotos_etapa_projeto IN ('Não iniciada','Parcial','Concluido') ) DEFAULT NULL
, usuario TEXT DEFAULT NULL);
INSERT INTO "tb_informacoes_completude" ("id_informacoes_completude","qtd","equipe_unidade_empresa","responsavel_preenchimento","dados_equipe","conhecimentos","recursos_aplicados","canvas_preencher","pitch_escrito","pitch_video","cronograma","foto_equipe","fotos_etapa_projeto","usuario") VALUES(3,0,NULL,NULL,'Não iniciada','Não iniciada','Não iniciada','Não iniciada','Não iniciada','Não iniciada','Não iniciada','Não iniciada','Não iniciada','professor@mail.co');
INSERT INTO "tb_informacoes_completude" ("id_informacoes_completude","qtd","equipe_unidade_empresa","responsavel_preenchimento","dados_equipe","conhecimentos","recursos_aplicados","canvas_preencher","pitch_escrito","pitch_video","cronograma","foto_equipe","fotos_etapa_projeto","usuario") VALUES(5,0,'','','Não iniciada','Não iniciada','Não iniciada','Não iniciada','Não iniciada','Não iniciada','Não iniciada','Não iniciada','Não iniciada','pau');
CREATE TABLE tb_informacoes_complementares (
  id_informacoes_complementares INTEGER PRIMARY KEY AUTOINCREMENT,
  unidade_nome_comercial TEXT NOT NULL,
  coordenador_pedagogico TEXT DEFAULT NULL,
  gestor TEXT DEFAULT NULL,
  empresa TEXT NOT NULL,
  projeto TEXT NOT NULL,
  descricao TEXT NOT NULL
, usuario TEXT DEFAULT NULL);
INSERT INTO "tb_informacoes_complementares" ("id_informacoes_complementares","unidade_nome_comercial","coordenador_pedagogico","gestor","empresa","projeto","descricao","usuario") VALUES(5,'','','','','','','pau');
CREATE TABLE tb_equipe (
  id_equipe INTEGER PRIMARY KEY AUTOINCREMENT,
  nome_integrante TEXT NOT NULL,
  nome_equipe TEXT UNIQUE DEFAULT NULL,
  nome_projeto TEXT NOT NULL,
  email TEXT NOT NULL,
  area_atuacao_curso TEXT NOT NULL,
  area_atuacao_projeto TEXT NOT NULL,
  nome_integrante2 TEXT DEFAULT NULL,
  nome_integrante3 TEXT DEFAULT NULL,
  nome_integrante4 TEXT DEFAULT NULL,
  nome_integrante5 TEXT DEFAULT NULL,
  nome_orientador TEXT DEFAULT NULL,
  nome_coorientador TEXT DEFAULT NULL,
  usuario TEXT DEFAULT NULL
, colum processado integer default 0, processado integer default 0);
INSERT INTO "tb_equipe" ("id_equipe","nome_integrante","nome_equipe","nome_projeto","email","area_atuacao_curso","area_atuacao_projeto","nome_integrante2","nome_integrante3","nome_integrante4","nome_integrante5","nome_orientador","nome_coorientador","usuario","colum","processado") VALUES(4,'','','','','','','','','','','','','nome equipe',0,0);
INSERT INTO "tb_equipe" ("id_equipe","nome_integrante","nome_equipe","nome_projeto","email","area_atuacao_curso","area_atuacao_projeto","nome_integrante2","nome_integrante3","nome_integrante4","nome_integrante5","nome_orientador","nome_coorientador","usuario","colum","processado") VALUES(5,'jjjj','pau','que late','larissa@gmail.com','mecatronica','sjdjj','Conta de Aluno','hhh','shsj','hssb','nsnsn','jwj2jj','professor@mail.co',0,0);
CREATE TABLE tb_cadastros (
  id_cadastro INTEGER PRIMARY KEY AUTOINCREMENT,
  nome_usuarios TEXT NOT NULL UNIQUE,
  senha TEXT NOT NULL,
  nivel_de_acesso INTEGER DEFAULT 0
, email varchar (150), foto_perfil varchar (300));
INSERT INTO "tb_cadastros" ("id_cadastro","nome_usuarios","senha","nivel_de_acesso","email","foto_perfil") VALUES(1,'Conta de Avaliador','avaliador',1,'avaliador@mail.co',NULL);
INSERT INTO "tb_cadastros" ("id_cadastro","nome_usuarios","senha","nivel_de_acesso","email","foto_perfil") VALUES(2,'Conta DR e DN','Lari',2,'drdn@mail.com','null');
INSERT INTO "tb_cadastros" ("id_cadastro","nome_usuarios","senha","nivel_de_acesso","email","foto_perfil") VALUES(3,'Conta de Professor','professor',3,'professor@mail.co',NULL);
INSERT INTO "tb_cadastros" ("id_cadastro","nome_usuarios","senha","nivel_de_acesso","email","foto_perfil") VALUES(4,'Conta de Empresa','empresa',4,'empresa@mail.co','null');
INSERT INTO "tb_cadastros" ("id_cadastro","nome_usuarios","senha","nivel_de_acesso","email","foto_perfil") VALUES(5,'Conta de Aluno','aluno',5,'aluno@mail.co','');
INSERT INTO "tb_cadastros" ("id_cadastro","nome_usuarios","senha","nivel_de_acesso","email","foto_perfil") VALUES(6,'Conta de Público Externo','publico',6,'publico@mail.co',NULL);
INSERT INTO "tb_cadastros" ("id_cadastro","nome_usuarios","senha","nivel_de_acesso","email","foto_perfil") VALUES(7,'Carlos Barbosa','carlos',6,'carlos@gmail.com',NULL);
INSERT INTO "tb_cadastros" ("id_cadastro","nome_usuarios","senha","nivel_de_acesso","email","foto_perfil") VALUES(8,'dr','dn',2,'dr@mail.co',NULL);
INSERT INTO "tb_cadastros" ("id_cadastro","nome_usuarios","senha","nivel_de_acesso","email","foto_perfil") VALUES(9,'Sarah','2911',2,'sarah.artuso2009@gmail.com',NULL);
INSERT INTO "tb_cadastros" ("id_cadastro","nome_usuarios","senha","nivel_de_acesso","email","foto_perfil") VALUES(10,'larissa','Lari',5,'larissagazoli45@gmail.com',NULL);
CREATE TABLE tb_conhecimentos (
  id_conhecimentos INTEGER PRIMARY KEY,
  plano_curso TEXT DEFAULT NULL,
  conhecimentos_aplicados TEXT NOT NULL,
  capacidades_aplicadas TEXT NOT NULL,
  usuario TEXT DEFAULT NULL,
  FOREIGN KEY (usuario) REFERENCES tb_equipe (nome_equipe) ON DELETE CASCADE ON UPDATE CASCADE
);
INSERT INTO "tb_conhecimentos" ("id_conhecimentos","plano_curso","conhecimentos_aplicados","capacidades_aplicadas","usuario") VALUES(1,'ejejj','jjj','jjjjhh','pau');
CREATE TABLE tb_participantes (
  id_participante INTEGER PRIMARY KEY AUTOINCREMENT,
  id_informacoes_complementares INTEGER NOT NULL,
  nome_enai_cax TEXT DEFAULT NULL,
  email TEXT DEFAULT NULL,
  tamanho_camiseta TEXT DEFAULT NULL,
  rg TEXT DEFAULT NULL,
  cpf TEXT DEFAULT NULL,
  data_nascimento DATE DEFAULT NULL,
  telefone TEXT DEFAULT NULL, matricula varchar (6),
  FOREIGN KEY (id_informacoes_complementares) REFERENCES tb_informacoes_complementares (id_informacoes_complementares) ON DELETE CASCADE
);
CREATE TABLE tb_pitch (
  id_pitch INTEGER PRIMARY KEY,
  roteiro TEXT NOT NULL,
  usuario TEXT DEFAULT NULL, "video_url" TEXT,
  FOREIGN KEY (usuario) REFERENCES tb_equipe (nome_equipe) ON DELETE CASCADE ON UPDATE CASCADE
);
INSERT INTO "tb_pitch" ("id_pitch","roteiro","usuario","video_url") VALUES(5,'','pau',NULL);
CREATE TABLE tb_recursos_aplicados (
  id_recursos INTEGER PRIMARY KEY,
  ferramentas TEXT NOT NULL,
  equipamentos TEXT NOT NULL,
  descricao_produto TEXT NOT NULL,
  quant_comprada TEXT NOT NULL,
  quant_utilizada TEXT NOT NULL,
  preco_estimado REAL NOT NULL,
  uni_medida TEXT DEFAULT NULL,
  fornecedor_principal TEXT NOT NULL,
  modo_obtencao TEXT NOT NULL,
  disponibilidade TEXT DEFAULT NULL,
  pagamento TEXT DEFAULT NULL,
  alternativas_consideradas TEXT DEFAULT NULL,
  preco_total REAL NOT NULL,
  usuario TEXT DEFAULT NULL,
  FOREIGN KEY (usuario) REFERENCES tb_equipe (nome_equipe) ON DELETE CASCADE ON UPDATE CASCADE,
  FOREIGN KEY (id_recursos) REFERENCES tb_equipe (id_equipe) ON DELETE CASCADE ON UPDATE CASCADE
);
INSERT INTO "tb_recursos_aplicados" ("id_recursos","ferramentas","equipamentos","descricao_produto","quant_comprada","quant_utilizada","preco_estimado","uni_medida","fornecedor_principal","modo_obtencao","disponibilidade","pagamento","alternativas_consideradas","preco_total","usuario") VALUES(4,'','','','','','','','','','','','','','');
INSERT INTO "tb_recursos_aplicados" ("id_recursos","ferramentas","equipamentos","descricao_produto","quant_comprada","quant_utilizada","preco_estimado","uni_medida","fornecedor_principal","modo_obtencao","disponibilidade","pagamento","alternativas_consideradas","preco_total","usuario") VALUES(5,'hh','bb','b','999','9',999,'bbb','bb','bb','bb','bbb','bbwb',94949,'pau');
CREATE TABLE tb_relatorio (
  id_relatorio INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
  nome_empresa TEXT NOT NULL,
  e_mail_empresa TEXT NOT NULL,
  setor_empresa TEXT,
  descricao TEXT NOT NULL,
  roteiro_pitch TEXT,
  integrante1 TEXT NOT NULL,
  integrante2 TEXT,
  integrante3 TEXT,
  integrante4 TEXT,
  integrante5 TEXT,
  orientador TEXT,
  coorientador TEXT,
  nome_projeto TEXT,
  nome_equipe TEXT,
  area_atuacao_projeto TEXT,
  area_atuacao_curso TEXT,
  unidade_senai TEXT,
  gestor TEXT,
  ferramenta_ia TEXT,
  link_acesso TEXT,
  licenca TEXT,
  etapa_de_usu TEXT,
  prompt TEXT,
  motivo_usu TEXT,
  ferramentas_projeto TEXT,
  equipamentos_projeto TEXT,
  quant_compra INTEGER,
  quant_utilizada INTEGER,
  preco REAL,
  fornecedor TEXT,
  modo_obtencao TEXT,
  processamento INTEGER DEFAULT 0,
  alternativa_de_uso TEXT,
  quant_utilizada_2 INTEGER,
  forma_pagamento TEXT,
  preco_total REAL
, usuario TEXT);
INSERT INTO "tb_relatorio" ("id_relatorio","nome_empresa","e_mail_empresa","setor_empresa","descricao","roteiro_pitch","integrante1","integrante2","integrante3","integrante4","integrante5","orientador","coorientador","nome_projeto","nome_equipe","area_atuacao_projeto","area_atuacao_curso","unidade_senai","gestor","ferramenta_ia","link_acesso","licenca","etapa_de_usu","prompt","motivo_usu","ferramentas_projeto","equipamentos_projeto","quant_compra","quant_utilizada","preco","fornecedor","modo_obtencao","processamento","alternativa_de_uso","quant_utilizada_2","forma_pagamento","preco_total","usuario") VALUES(3,'','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','nome equipe');
INSERT INTO "tb_relatorio" ("id_relatorio","nome_empresa","e_mail_empresa","setor_empresa","descricao","roteiro_pitch","integrante1","integrante2","integrante3","integrante4","integrante5","orientador","coorientador","nome_projeto","nome_equipe","area_atuacao_projeto","area_atuacao_curso","unidade_senai","gestor","ferramenta_ia","link_acesso","licenca","etapa_de_usu","prompt","motivo_usu","ferramentas_projeto","equipamentos_projeto","quant_compra","quant_utilizada","preco","fornecedor","modo_obtencao","processamento","alternativa_de_uso","quant_utilizada_2","forma_pagamento","preco_total","usuario") VALUES(4,'Não informado','Não informado','','Sem descrição','pipipi popopo','Conta de Aluno','aluno 2','','','','Conta de Professor','nome coorientador','nome projeto','nome equipe','area projeto','area curso','','','','','','','','','','',0,0,NULL,'','',1,'',NULL,NULL,0,NULL);
INSERT INTO "tb_relatorio" ("id_relatorio","nome_empresa","e_mail_empresa","setor_empresa","descricao","roteiro_pitch","integrante1","integrante2","integrante3","integrante4","integrante5","orientador","coorientador","nome_projeto","nome_equipe","area_atuacao_projeto","area_atuacao_curso","unidade_senai","gestor","ferramenta_ia","link_acesso","licenca","etapa_de_usu","prompt","motivo_usu","ferramentas_projeto","equipamentos_projeto","quant_compra","quant_utilizada","preco","fornecedor","modo_obtencao","processamento","alternativa_de_uso","quant_utilizada_2","forma_pagamento","preco_total","usuario") VALUES(5,'Não informado','Não informado','','Sem descrição','pipipi popopo','Conta de Aluno','aluno 2','','','','Conta de Professor','nome coorientador','nome projeto','nome equipe','area projeto','area curso','','','','','','','','','','',0,0,NULL,'','',0,'',NULL,NULL,0,NULL);
INSERT INTO "tb_relatorio" ("id_relatorio","nome_empresa","e_mail_empresa","setor_empresa","descricao","roteiro_pitch","integrante1","integrante2","integrante3","integrante4","integrante5","orientador","coorientador","nome_projeto","nome_equipe","area_atuacao_projeto","area_atuacao_curso","unidade_senai","gestor","ferramenta_ia","link_acesso","licenca","etapa_de_usu","prompt","motivo_usu","ferramentas_projeto","equipamentos_projeto","quant_compra","quant_utilizada","preco","fornecedor","modo_obtencao","processamento","alternativa_de_uso","quant_utilizada_2","forma_pagamento","preco_total","usuario") VALUES(6,'Não informado','Não informado','','Sem descrição','pipipi popopo','Conta de Aluno','aluno 1','aluno 2','aluno 3','','Conta de Professor','nome coorientador','nome projeto','nome equipe','area projeto','area curso','','','','','','','','','','',0,0,NULL,'','',0,'',NULL,NULL,0,NULL);
INSERT INTO "tb_relatorio" ("id_relatorio","nome_empresa","e_mail_empresa","setor_empresa","descricao","roteiro_pitch","integrante1","integrante2","integrante3","integrante4","integrante5","orientador","coorientador","nome_projeto","nome_equipe","area_atuacao_projeto","area_atuacao_curso","unidade_senai","gestor","ferramenta_ia","link_acesso","licenca","etapa_de_usu","prompt","motivo_usu","ferramentas_projeto","equipamentos_projeto","quant_compra","quant_utilizada","preco","fornecedor","modo_obtencao","processamento","alternativa_de_uso","quant_utilizada_2","forma_pagamento","preco_total","usuario") VALUES(7,'','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','professor@mail.co');
CREATE TABLE IF NOT EXISTS "tb_canva" (
  id_canva INTEGER PRIMARY KEY,
  atividades_chaves TEXT NOT NULL,
  proposta_chave TEXT NOT NULL,
  relacionamentos_clientes TEXT NOT NULL,
  segmentos_clientes TEXT NOT NULL,
  recursos_chaves TEXT NOT NULL,
  canais TEXT NOT NULL,
  estrutura_custos TEXT NOT NULL,
  fluxo_receita TEXT DEFAULT NULL,
  parceiros_chaves TEXT NOT NULL,
  usuario TEXT NOT NULL,
  FOREIGN KEY (usuario) REFERENCES tb_equipe (nome_equipe) ON DELETE CASCADE ON UPDATE CASCADE
);
INSERT INTO "tb_canva" ("id_canva","atividades_chaves","proposta_chave","relacionamentos_clientes","segmentos_clientes","recursos_chaves","canais","estrutura_custos","fluxo_receita","parceiros_chaves","usuario") VALUES(1,'','','','','','','','','','pau');
CREATE TABLE tb_cronograma (     id_cronograma INTEGER PRIMARY KEY,     processo TEXT NOT NULL,     etapas TEXT NOT NULL,     responsavel TEXT NOT NULL,     data_inicio DATE,     data_final DATE,     observacoes TEXT NOT NULL,     usuario TEXT,      FOREIGN KEY (id_cronograma)         REFERENCES tb_equipe(id_equipe)         ON DELETE CASCADE         ON UPDATE CASCADE,      FOREIGN KEY (usuario)         REFERENCES tb_equipe(nome_equipe)         ON DELETE CASCADE         ON UPDATE CASCADE );
INSERT INTO "tb_cronograma" ("id_cronograma","processo","etapas","responsavel","data_inicio","data_final","observacoes","usuario") VALUES(5,'','','','','','','pau');
CREATE TABLE tb_empresas (   id_empresa INTEGER PRIMARY KEY AUTOINCREMENT,   nome_empresa TEXT NOT NULL UNIQUE,   cnpj TEXT DEFAULT NULL,   telefone_contato TEXT DEFAULT NULL,   email_contato TEXT DEFAULT NULL,   endereco TEXT DEFAULT NULL , foto_perfil TEXT, descricao TEXT, setor TEXT, usuario TEXT DEFAULT NULL);
INSERT INTO "tb_empresas" ("id_empresa","nome_empresa","cnpj","telefone_contato","email_contato","endereco","foto_perfil","descricao","setor","usuario") VALUES(3,'Conta de Empresa','12345678000199','54996290304','empresa@mail.co','guardanapo 555, opulência, Uberlândia','null','bibi bobo','Agrícola','Conta de Empresa');
CREATE TABLE tb_empresas_formulario (     id_empresa_formulario INTEGER PRIMARY KEY,     nome_empresa TEXT NOT NULL,     cnpj TEXT,     regiao TEXT,     telefone_contato TEXT,     email_contato TEXT,     objetivos TEXT,     problema_projeto TEXT,      FOREIGN KEY(id_empresa_formulario)         REFERENCES tb_equipe(id_equipe)         ON DELETE CASCADE         ON UPDATE CASCADE );
INSERT INTO "tb_empresas_formulario" ("id_empresa_formulario","nome_empresa","cnpj","regiao","telefone_contato","email_contato","objetivos","problema_projeto") VALUES(5,'','','','','','','');
CREATE TABLE tb_uso_ia(     id_uso_ia INTEGER PRIMARY KEY,     usuario TEXT,     nome_ferramenta TEXT,     link_acesso TEXT,     tipo_licenca TEXT,     etapa_uso TEXT,     criacao_prompt TEXT,     descricao_uso TEXT,       FOREIGN KEY(usuario) REFERENCES tb_equipe(nome_equipe) );
INSERT INTO "tb_uso_ia" ("id_uso_ia","usuario","nome_ferramenta","link_acesso","tipo_licenca","etapa_uso","criacao_prompt","descricao_uso") VALUES(1,'pau','','','','','','');
CREATE TABLE tb_acompanhamento_projeto (   id_acompanhamento_projeto INTEGER PRIMARY KEY AUTOINCREMENT,   tarefas TEXT NOT NULL,   aluno_responsavel TEXT NOT NULL,   professor_da_area TEXT NOT NULL,   inicio_previsto DATE NOT NULL,   fim_previsto DATE NOT NULL,   inicio_realizado DATE DEFAULT NULL,   fim_realizado DATE DEFAULT NULL,   duracao INTEGER DEFAULT NULL,   status TEXT CHECK( status IN ('Não iniciado','Concluído','Em atraso','Necessitamos de auxílio') ) DEFAULT NULL,   descricao_da_tarefa TEXT NOT NULL,   dificuldades_enxergadas TEXT DEFAULT NULL,   impacto_nas_outras TEXT DEFAULT NULL , usuario TEXT DEFAULT NULL, comentario_empresa TEXT);
INSERT INTO "tb_acompanhamento_projeto" ("id_acompanhamento_projeto","tarefas","aluno_responsavel","professor_da_area","inicio_previsto","fim_previsto","inicio_realizado","fim_realizado","duracao","status","descricao_da_tarefa","dificuldades_enxergadas","impacto_nas_outras","usuario","comentario_empresa") VALUES(4,'','','','','','','','','Não iniciado','','','','pau',NULL);
CREATE TABLE tb_curriculo_alunos (     id_aluno INTEGER PRIMARY KEY,     nome TEXT NOT NULL,     data_nascimento DATE DEFAULT NULL,     empresa_vinculado TEXT DEFAULT NULL,     projeto TEXT NOT NULL,     telefone TEXT NOT NULL,     email TEXT NOT NULL,     habilidades TEXT DEFAULT NULL,     fez_projeto TEXT DEFAULT NULL,     cidade TEXT DEFAULT NULL,     motivo_projeto TEXT NOT NULL,     aprendo_mais TEXT DEFAULT NULL,     prefiro_trabalhar TEXT DEFAULT NULL,     usuario TEXT DEFAULT NULL,     tarefas_feitas INTEGER );
INSERT INTO "tb_curriculo_alunos" ("id_aluno","nome","data_nascimento","empresa_vinculado","projeto","telefone","email","habilidades","fez_projeto","cidade","motivo_projeto","aprendo_mais","prefiro_trabalhar","usuario","tarefas_feitas") VALUES(1,'Conta de Aluno',160309,'Conta de Empresa','nome projeto','54948434','aluno@mail.co','habilidades','desenvolvi','Rio de Janeiro','participei','aprendo melhor','trabalho','aluno@mail.co',NULL);
CREATE TABLE tb_necessidades_empresas (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    empresa_id INTEGER NOT NULL,
    nome TEXT NOT NULL,
    descricao TEXT NOT NULL,
    FOREIGN KEY (empresa_id) REFERENCES tb_empresas(id_empresa)
);
INSERT INTO "tb_necessidades_empresas" ("id","empresa_id","nome","descricao") VALUES(1,3,'Desenvolvimento de Api','queremos uma api');
CREATE TABLE tb_documentos (
    id_documento INTEGER PRIMARY KEY AUTOINCREMENT,
    nome_documento TEXT NOT NULL,
    tipo_documento TEXT NOT NULL,
    url_documento TEXT NOT NULL,
    usuario_vinculado TEXT NOT NULL,
    data_geracao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
INSERT INTO "tb_documentos" ("id_documento","nome_documento","tipo_documento","url_documento","usuario_vinculado","data_geracao") VALUES(3,'Curriculo_Conta de Aluno.pdf','Curriculo','https://pub-8b39c2fa88234341ac68682a11d82f77.r2.dev/documentos/curriculo_aluno_mail_co_1785111584214.pdf','aluno@mail.co','2026-07-27 00:19:44');
INSERT INTO "tb_documentos" ("id_documento","nome_documento","tipo_documento","url_documento","usuario_vinculado","data_geracao") VALUES(4,'Curriculo_Conta de Aluno.pdf','Curriculo','https://pub-8b39c2fa88234341ac68682a11d82f77.r2.dev/documentos/curriculo_aluno_mail_co_1785111982481.pdf','aluno@mail.co','2026-07-27 00:26:23');
INSERT INTO "tb_documentos" ("id_documento","nome_documento","tipo_documento","url_documento","usuario_vinculado","data_geracao") VALUES(5,'Curriculo_Conta de Aluno.pdf','Curriculo','https://pub-8b39c2fa88234341ac68682a11d82f77.r2.dev/documentos/curriculo_nome_equipe_1785112428106.pdf','nome equipe','2026-07-27 00:33:48');
INSERT INTO "tb_documentos" ("id_documento","nome_documento","tipo_documento","url_documento","usuario_vinculado","data_geracao") VALUES(6,'Curriculo_Conta de Aluno.pdf','Curriculo','https://pub-8b39c2fa88234341ac68682a11d82f77.r2.dev/documentos/curriculo_nome_equipe_1785112645951.pdf','nome equipe','2026-07-27 00:37:26');
INSERT INTO "tb_documentos" ("id_documento","nome_documento","tipo_documento","url_documento","usuario_vinculado","data_geracao") VALUES(7,'Curriculo_Conta de Aluno.pdf','Curriculo','https://pub-8b39c2fa88234341ac68682a11d82f77.r2.dev/documentos/curriculo_nome_equipe_1785112929533.pdf','nome equipe','2026-07-27 00:42:10');
INSERT INTO "tb_documentos" ("id_documento","nome_documento","tipo_documento","url_documento","usuario_vinculado","data_geracao") VALUES(8,'Curriculo_Conta de Aluno.pdf','Curriculo','https://pub-8b39c2fa88234341ac68682a11d82f77.r2.dev/documentos/curriculo_nome_equipe_1785114873605.pdf','nome equipe','2026-07-27 01:14:34');
INSERT INTO "tb_documentos" ("id_documento","nome_documento","tipo_documento","url_documento","usuario_vinculado","data_geracao") VALUES(9,'Curriculo_Conta de Aluno.pdf','Curriculo','https://pub-8b39c2fa88234341ac68682a11d82f77.r2.dev/documentos/curriculo_nome_equipe_1785116632110.pdf','nome equipe','2026-07-27 01:43:52');
INSERT INTO "tb_documentos" ("id_documento","nome_documento","tipo_documento","url_documento","usuario_vinculado","data_geracao") VALUES(10,'Curriculo_Conta de Aluno.pdf','Curriculo','https://pub-8b39c2fa88234341ac68682a11d82f77.r2.dev/documentos/curriculo_nome_equipe_1785116663854.pdf','nome equipe','2026-07-27 01:44:24');
INSERT INTO "tb_documentos" ("id_documento","nome_documento","tipo_documento","url_documento","usuario_vinculado","data_geracao") VALUES(11,'Curriculo_Conta de Aluno.pdf','Curriculo','https://pub-8b39c2fa88234341ac68682a11d82f77.r2.dev/documentos/curriculo_nome_equipe_1785116982503.pdf','nome equipe','2026-07-27 01:49:43');
INSERT INTO "tb_documentos" ("id_documento","nome_documento","tipo_documento","url_documento","usuario_vinculado","data_geracao") VALUES(12,'Curriculo_Conta de Aluno.pdf','Curriculo','https://pub-8b39c2fa88234341ac68682a11d82f77.r2.dev/documentos/curriculo_aluno_mail_co_1785117517397.pdf','aluno@mail.co','2026-07-27 01:58:37');
INSERT INTO "tb_documentos" ("id_documento","nome_documento","tipo_documento","url_documento","usuario_vinculado","data_geracao") VALUES(13,'Curriculo_Conta de Aluno.pdf','Curriculo','https://pub-8b39c2fa88234341ac68682a11d82f77.r2.dev/documentos/curriculo_aluno_mail_co_1785117532132.pdf','aluno@mail.co','2026-07-27 01:58:52');
DELETE FROM sqlite_sequence;
INSERT INTO "sqlite_sequence" ("name","seq") VALUES('tb_cadastros',10);
INSERT INTO "sqlite_sequence" ("name","seq") VALUES('tb_empresas',3);
INSERT INTO "sqlite_sequence" ("name","seq") VALUES('tb_informacoes_complementares',5);
INSERT INTO "sqlite_sequence" ("name","seq") VALUES('tb_equipe',5);
INSERT INTO "sqlite_sequence" ("name","seq") VALUES('tb_relatorio',7);
INSERT INTO "sqlite_sequence" ("name","seq") VALUES('tb_informacoes_completude',5);
INSERT INTO "sqlite_sequence" ("name","seq") VALUES('tb_acompanhamento_projeto',4);
INSERT INTO "sqlite_sequence" ("name","seq") VALUES('tb_documentos',13);
INSERT INTO "sqlite_sequence" ("name","seq") VALUES('tb_necessidades_empresas',1);
CREATE INDEX idx_equipe_usuario ON tb_equipe(usuario);
CREATE INDEX fk_conh_eqp ON tb_conhecimentos(usuario);
CREATE INDEX fk_info_participante ON tb_participantes(id_informacoes_complementares);
CREATE INDEX fk_pitch_eqp ON tb_pitch(usuario);
CREATE INDEX fk_rec_eqp_idx ON tb_recursos_aplicados(usuario);
CREATE UNIQUE INDEX idx_completude_usuario ON tb_informacoes_completude(usuario);
CREATE UNIQUE INDEX idx_equipe_owner ON tb_equipe(usuario);
