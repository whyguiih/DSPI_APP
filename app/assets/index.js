async function handleUploadVideo(request, env, corsHeaders) {

  try {

    const formData = await request.formData();

    const usuario = formData.get('usuario');

    const videoFile = formData.get('video'); // O arquivo 'pitch_video.mp4' enviado pelo Android



    if (!usuario || !videoFile) {

      return new Response(JSON.stringify({ success: false, error: 'Dados incompletos' }), {

        status: 400, headers: { ...corsHeaders, 'Content-Type': 'application/json' }

      });

    }



    // 1. Gerar um nome único para o arquivo para não sobrescrever outros

    const fileName = `pitches/${usuario.replace(/[^a-zA-Z0-9]/g, '_')}_${Date.now()}.mp4`;



    // URL pública baseada na Public Development URL do bucket 'dspi'

    const R2_PUBLIC_URL_VIDEOS = "https://pub-8b39c2fa88234341ac68682a11d82f77.r2.dev";

    const videoUrl = `${R2_PUBLIC_URL_VIDEOS}/${fileName}`;



    // 2. Salvar no Cloudflare R2 usando stream (mais eficiente para memória)

    await env.BUCKET_VIDEOS.put(fileName, videoFile.stream(), {

      httpMetadata: { contentType: 'video/mp4' }

    });



    // 3. Atualizar o Banco de Dados (D1)

    if (env.DB) {

      // Atualiza o status na tabela de completude respeitando a CHECK constraint ('Concluido')

      await env.DB.prepare(

        "UPDATE tb_informacoes_completude SET pitch_video = ? WHERE usuario = ?"

      ).bind("Concluido", usuario).run();



      // Salvar a URL na tabela de pitch

      // Nota: O campo 'usuario' na tb_pitch é uma Foreign Key para tb_equipe(nome_equipe)

      // Buscamos o nome da equipe associado ao e-mail/identificador

      const equipeRecord = await env.DB.prepare("SELECT nome_equipe FROM tb_equipe WHERE usuario = ? OR email = ?").bind(usuario, usuario).first();

      const idUsuario = equipeRecord ? equipeRecord.nome_equipe : usuario;



      const pitchExists = await env.DB.prepare("SELECT 1 FROM tb_pitch WHERE usuario = ?").bind(idUsuario).first();

      if (pitchExists) {

        await env.DB.prepare("UPDATE tb_pitch SET video_url = ? WHERE usuario = ?").bind(videoUrl, idUsuario).run();

      } else {

        await env.DB.prepare("INSERT INTO tb_pitch (usuario, video_url, roteiro) VALUES (?, ?, ?)")

          .bind(idUsuario, videoUrl, "").run();

      }

    }



    return new Response(JSON.stringify({

      success: true,

      message: 'Vídeo salvo com sucesso!',

      path: fileName,

      video_url: videoUrl

    }), { status: 200, headers: { ...corsHeaders, 'Content-Type': 'application/json' } });



  } catch (err) {

    return new Response(JSON.stringify({ success: false, error: err.message }), {

      status: 500, headers: { ...corsHeaders, 'Content-Type': 'application/json' }

    });

  }

}



// ==========================================

// FUNÇÃO PARA SALVAR IMAGEM NO CLOUDFLARE R2

// ==========================================

async function uploadBase64ToR2(base64String, identificador, env) {

  if (!base64String || base64String.startsWith('http') || base64String.length < 100) {

    return base64String;

  }



  try {

    const cleanBase64 = base64String.replace(/^data:image\/\w+;base64,/, "").replace(/\s/g, "");

    const binaryString = atob(cleanBase64);

    const bytes = new Uint8Array(binaryString.length);

    for (let i = 0; i < binaryString.length; i++) {

      bytes[i] = binaryString.charCodeAt(i);

    }



    const safeName = identificador.replace(/[^a-zA-Z0-9]/g, '_');

    const fileName = `img_${safeName}_${Date.now()}.jpg`;



    // Atualizado com a Public Development URL do bucket 'dspi'

    const R2_PUBLIC_URL = "https://pub-8b39c2fa88234341ac68682a11d82f77.r2.dev";



    if (env.BUCKET_AVATARES) {

      await env.BUCKET_AVATARES.put(fileName, bytes.buffer, {

        httpMetadata: { contentType: 'image/jpeg' }

      });

      return `${R2_PUBLIC_URL}/${fileName}`;

    } else {

      console.warn("ALERTA: BUCKET_AVATARES não foi configurado no Cloudflare.");

      return base64String;

    }



  } catch (error) {

    console.error("Erro ao converter/salvar a imagem no R2:", error);

    return base64String;

  }

}



export default {

  async fetch(request, env) {

    const url = new URL(request.url);

    const path = url.pathname;

    const method = request.method;



    const corsHeaders = {

      "Access-Control-Allow-Origin": "*",

      "Access-Control-Allow-Methods": "POST, GET, OPTIONS",

      "Access-Control-Allow-Headers": "Content-Type",

    };



    if (method === "OPTIONS") {

      return new Response(null, { headers: corsHeaders });

    }



    // ==========================================

    // ROTAS DO TIPO GET

    // ==========================================

    if (method === "GET") {

      if (path === "/listar-empresas") {

        try {

          const { results } = await env.DB.prepare(

            "SELECT id_empresa, nome_empresa, cnpj, telefone_contato, email_contato, endereco, foto_perfil, descricao, setor FROM tb_empresas"

          ).all();

          return new Response(JSON.stringify({ success: true, data: results }), { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } });

        } catch (erro) {

          return new Response(JSON.stringify({ success: false, error: erro.message }), { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } });

        }

      }



      if (path === "/listar-projetos") {

        try {

          const { results: projetos } = await env.DB.prepare(`

            SELECT

              e.nome_projeto, e.nome_equipe, e.nome_integrante, e.nome_orientador,

              a.status, a.tarefas, a.dificuldades_enxergadas, a.comentario_empresa,

              c.proposta_chave, c.segmentos_clientes, c.atividades_chaves, c.recursos_chaves, c.relacionamentos_clientes, c.canais, c.estrutura_custos, c.fluxo_receita, c.parceiros_chaves,

              ic.empresa AS empresa_vinculada,

              p.video_url

            FROM tb_equipe e

            LEFT JOIN tb_acompanhamento_projeto a ON e.usuario = a.usuario

            LEFT JOIN tb_canva c ON e.usuario = c.usuario

            LEFT JOIN tb_informacoes_complementares ic ON e.usuario = ic.usuario

            LEFT JOIN tb_pitch p ON (e.nome_equipe = p.usuario OR e.usuario = p.usuario)

          `).all();



          const { results: empresas } = await env.DB.prepare(`

            SELECT nome_empresa, email_contato FROM tb_empresas

          `).all();



          // A MÁGICA REVERSA: O App Android precisa do NOME.

          // Se o banco guardou o E-mail (como no caso da Threeeo), o código acha a empresa e troca pelo Nome.

          const projetosTraduzidos = projetos.map(projeto => {

            if (projeto.empresa_vinculada) {

              const empresaEncontrada = empresas.find(

                emp => emp.nome_empresa === projeto.empresa_vinculada || emp.email_contato === projeto.empresa_vinculada

              );

              // Forçamos o envio do NOME DA EMPRESA para o App Android

              if (empresaEncontrada && empresaEncontrada.nome_empresa) {

                projeto.empresa_vinculada = empresaEncontrada.nome_empresa;

              }

            }

            return projeto;

          });



          return new Response(JSON.stringify({ success: true, data: projetosTraduzidos }), { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } });

        } catch (erro) {

          return new Response(JSON.stringify({ success: false, error: erro.message }), { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } });

        }

      }

      if (path === "/detalhes-equipe") {
        const url = new URL(request.url);
        const equipeParam = url.searchParams.get("equipe") || url.searchParams.get("usuario");

        if (!equipeParam) {
          return new Response(JSON.stringify({ success: false, error: "Nome da equipe ou usuário não informado." }), { status: 400, headers: corsHeaders });
        }

        try {
          const equipeRecord = await env.DB.prepare("SELECT * FROM tb_equipe WHERE nome_equipe = ? OR usuario = ? OR email = ?").bind(equipeParam, equipeParam, equipeParam).first();
          const nomeEquipe = equipeRecord ? equipeRecord.nome_equipe : equipeParam;
          const idUsuario = equipeRecord ? equipeRecord.usuario : equipeParam;

          const [canva, pitch, cronograma, ia, recursos] = await Promise.all([
            env.DB.prepare("SELECT * FROM tb_canva WHERE usuario = ? OR usuario = ?").bind(nomeEquipe, idUsuario).first(),
            env.DB.prepare("SELECT * FROM tb_pitch WHERE usuario = ? OR usuario = ?").bind(nomeEquipe, idUsuario).first(),
            env.DB.prepare("SELECT * FROM tb_cronograma WHERE responsavel = ? OR usuario = ? OR usuario = ?").bind(nomeEquipe, nomeEquipe, idUsuario).all(),
            env.DB.prepare("SELECT * FROM tb_uso_ia WHERE usuario = ? OR usuario = ?").bind(nomeEquipe, idUsuario).all(),
            env.DB.prepare("SELECT * FROM tb_recursos_aplicados WHERE usuario = ? OR usuario = ?").bind(nomeEquipe, idUsuario).all()
          ]);

          return new Response(JSON.stringify({
            success: true,
            data: {
              equipe: equipeRecord || null,
              canva: canva || null,
              pitch: pitch || null,
              cronograma: cronograma.results || [],
              ia: ia.results || [],
              recursos: recursos.results || []
            }
          }), { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } });
        } catch (erro) {
          return new Response(JSON.stringify({ success: false, error: erro.message }), { status: 500, headers: corsHeaders });
        }
      }

      // ===============================================================
      // ROTA GET: BUSCAR DADOS DO CURRÍCULO (Para gerar o PDF ou tela)
      // ===============================================================
      if (path === "/buscar-curriculo") {
        const email = url.searchParams.get("email");
        const cpf = url.searchParams.get("cpf");
        const nome = url.searchParams.get("nome");

        if (!email && !cpf && !nome) {
          return new Response(JSON.stringify({ success: false, error: "Informe email, cpf ou nome na URL." }), { status: 400, headers: corsHeaders });
        }

        try {
          // Busca o currículo por qualquer um dos identificadores
          const curriculo = await env.DB.prepare(
            "SELECT * FROM tb_curriculo_alunos WHERE email = ? OR cpf = ? OR nome = ?"
          ).bind(email || "", cpf || "", nome || "").first();

          if (!curriculo) {
            return new Response(JSON.stringify({ success: false, message: "Currículo não encontrado." }), { status: 404, headers: corsHeaders });
          }

          // Formatando a resposta exatamente para os campos do seu PDF PDF
          const dadosPDF = {
            dados_pessoais: {
              nome: curriculo.nome,
              data_nascimento: curriculo.data_nacimento || "Não informada",
              telefone: curriculo.telefone || "Não informado",
              email: curriculo.email,
              cidade: curriculo.cidade || "Não informada"
            },
            vinculo_senai: {
              projeto: curriculo.projeto || "Nenhum projeto vinculado",
              empresa: curriculo.empresa_vinculado || "Nenhuma empresa vinculada",
              motivacao_projeto: curriculo.motivo_projeto || "Não informada"
            },
            competencias: {
              habilidades: curriculo.habilidades || "Não preenchido",
              experiencia_projetos: curriculo.fez_projeto || "Não preenchido",
              o_que_desenvolvi: curriculo.tarefas_feitas || "Nenhuma tarefa concluída no sistema"
            },
            perfil_aprendizagem_trabalho: {
              como_aprendo_mais: curriculo.aprendo_mais || "Não informado",
              como_prefiro_trabalhar: curriculo.prefiro_trabalhar || "Não informado"
            }
          };

          return new Response(JSON.stringify({ success: true, data: dadosPDF, raw: curriculo }), {
            status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" }
          });
        } catch (erro) {
          return new Response(JSON.stringify({ success: false, error: erro.message }), { status: 500, headers: corsHeaders });
        }
      }

    }



    // ==========================================

    // ROTAS DO TIPO POST

    // ==========================================

    if (method !== "POST") {

      return new Response("Método não permitido", { status: 405, headers: corsHeaders });

    }



    // ==========================================

    // ROTA DE UPLOAD DE VÍDEO (FORM-DATA)

    // ==========================================

    if (path === "/upload-video") {

      return await handleUploadVideo(request, env, corsHeaders);

    }



    try {

      const body = await request.json();

      if (path === "/salvar-curriculo") {
        try {
          const {
            nome, email, data_nascimento, telefone, cidade, habilidades,
            fez_projeto, projeto, empresa_vinculado, motivo_projeto,
            aprendo_mais, prefiro_trabalhar
          } = body;

          if (!email) {
            return new Response(JSON.stringify({ success: false, message: 'O campo e-mail é obrigatório para identificar o aluno.' }), {
              status: 400, headers: { ...corsHeaders, 'Content-Type': 'application/json' }
            });
          }

          // Prepara valores padrão para evitar erros de "undefined" no bind do D1
          const val = (v) => (v === undefined || v === null) ? "" : String(v);

          // Busca se já existe um registro para este email
          const curriculoExistente = await env.DB.prepare(
            "SELECT id_aluno FROM tb_curriculo_alunos WHERE email = ?"
          ).bind(email).first();

          if (curriculoExistente) {
            // 1. ATUALIZA
            await env.DB.prepare(`
              UPDATE tb_curriculo_alunos SET
                nome = ?, data_nacimento = ?, telefone = ?, cidade = ?, habilidades = ?,
                fez_projeto = ?, projeto = ?, empresa_vinculado = ?, motivo_projeto = ?,
                aprendo_mais = ?, prefiro_trabalhar = ?
              WHERE email = ?
            `).bind(
              val(nome), val(data_nascimento), val(telefone), val(cidade), val(habilidades),
              val(fez_projeto), val(projeto), val(empresa_vinculado), val(motivo_projeto),
              val(aprendo_mais), val(prefiro_trabalhar), email
            ).run();

            return new Response(JSON.stringify({ success: true, message: 'Currículo atualizado com sucesso!' }), {
              headers: { ...corsHeaders, 'Content-Type': 'application/json' }
            });

          } else {
            // 2. INSERE (Note que 'cpf' é obrigatório no seu banco, então enviamos vazio se não houver)
            await env.DB.prepare(`
              INSERT INTO tb_curriculo_alunos (
                nome, email, data_nacimento, telefone, cidade, habilidades,
                fez_projeto, projeto, empresa_vinculado, motivo_projeto,
                aprendo_mais, prefiro_trabalhar, cpf
              ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            `).bind(
              val(nome), email, val(data_nascimento), val(telefone), val(cidade), val(habilidades),
              val(fez_projeto), val(projeto), val(empresa_vinculado), val(motivo_projeto),
              val(aprendo_mais), val(prefiro_trabalhar), ""
            ).run();

            return new Response(JSON.stringify({ success: true, message: 'Currículo cadastrado com sucesso!' }), {
              headers: { ...corsHeaders, 'Content-Type': 'application/json' }
            });
          }
        } catch (error) {
          return new Response(JSON.stringify({ success: false, message: 'Erro no Banco de Dados: ' + error.message }), {
            status: 500, headers: { ...corsHeaders, 'Content-Type': 'application/json' }
          });
        }
      }



      if (path === "/atualizar-perfil") {

        const { email_atual, novo_nome, novo_email, foto_perfil } = body;



        try {

          const urlFotoSalva = await uploadBase64ToR2(foto_perfil, email_atual, env);



          const queryCadastros = "UPDATE tb_cadastros SET nome_usuarios = ?, email = ?, foto_perfil = ? WHERE email = ?";

          const infoCadastros = await env.DB.prepare(queryCadastros).bind(novo_nome, novo_email, urlFotoSalva, email_atual).run();



          if (infoCadastros.meta.changes > 0) {

            const queryEmpresas = "UPDATE tb_empresas SET foto_perfil = ? WHERE email_contato = ?";

            await env.DB.prepare(queryEmpresas).bind(urlFotoSalva, email_atual).run();

            return new Response(JSON.stringify({ success: true, foto_url: urlFotoSalva }), { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } });

          } else {

            const queryOld = "UPDATE tb_cadastros SET nome_usuarios = ?, email = ?, foto_perfil = ? WHERE nome_usuarios = ?";

            await env.DB.prepare(queryOld).bind(novo_nome, novo_email, urlFotoSalva, email_atual).run();

            return new Response(JSON.stringify({ success: true, foto_url: urlFotoSalva }), { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } });

          }

        } catch (e) {

          return new Response(JSON.stringify({ success: false, error: e.message }), { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } });

        }

      }



      if (path === "/login-google") {

        const { idToken } = body;

        if (!idToken) return new Response(JSON.stringify({ success: false, message: "Token ausente." }), { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } });

        const googleVerifyUrl = `https://oauth2.googleapis.com/tokeninfo?id_token=${idToken}`;

        const googleResponse = await fetch(googleVerifyUrl);

        if (!googleResponse.ok) return new Response(JSON.stringify({ success: false, message: "Token do Google inválido." }), { status: 401, headers: { ...corsHeaders, "Content-Type": "application/json" } });



        const googleUser = await googleResponse.json();

        const email = googleUser.email;

        const nome = googleUser.name;

        const foto = googleUser.picture;



        const stmtCheck = env.DB.prepare("SELECT nome_usuarios, email, nivel_de_acesso, foto_perfil FROM tb_cadastros WHERE email = ?").bind(email);

        const { results } = await stmtCheck.all();



        let usuarioFinal;

        if (results && results.length > 0) {

          usuarioFinal = results[0];

        } else {

          await env.DB.prepare("INSERT INTO tb_cadastros (nome_usuarios, email, senha, nivel_de_acesso, foto_perfil) VALUES (?, ?, ?, ?, ?)")

            .bind(nome, email, 'GOOGLE_AUTH', 6, foto).run();

          usuarioFinal = { nome_usuarios: nome, email: email, nivel_de_acesso: 6, foto_perfil: foto };

        }



        return new Response(JSON.stringify({

          success: true, nivel: usuarioFinal.nivel_de_acesso, email_usuario: usuarioFinal.email || usuarioFinal.nome_usuarios, nome_usuario: usuarioFinal.nome_usuarios, foto_usuario: usuarioFinal.foto_perfil, message: "Autenticado via Google com sucesso!"

        }), { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } });

      }



      // ==========================================

      // CADASTRO DE USUÁRIO

      // ==========================================

      if (path === "/cadastro") {



        const {

          nome,

          email,

          senha,

          nivel_de_acesso

        } = body;



        if (!nome || !email || !senha || nivel_de_acesso == null) {

          return new Response(

            JSON.stringify({

              success: false,

              error: "Preencha todos os campos."

            }),

            {

              status: 400,

              headers: {

                ...corsHeaders,

                "Content-Type": "application/json"

              }

            }

          );

        }



        try {



          // Verifica se o e-mail já existe

          const usuarioExistente = await env.DB.prepare(`

            SELECT id_cadastro

            FROM tb_cadastros

            WHERE email = ?

        `)

            .bind(email)

            .first();



          if (usuarioExistente) {



            return new Response(

              JSON.stringify({

                success: false,

                error: "Este e-mail já está cadastrado."

              }),

              {

                status: 400,

                headers: {

                  ...corsHeaders,

                  "Content-Type": "application/json"

                }

              }

            );



          }



          // Insere o usuário



          await env.DB.prepare(`

            INSERT INTO tb_cadastros

            (

                nome_usuarios,

                senha,

                nivel_de_acesso,

                email,

                foto_perfil

            )

            VALUES (?, ?, ?, ?, NULL)

        `)

            .bind(

              nome,

              senha,

              nivel_de_acesso,

              email

            )

            .run();



          return new Response(

            JSON.stringify({

              success: true,

              message: "Usuário cadastrado com sucesso."

            }),

            {

              status: 200,

              headers: {

                ...corsHeaders,

                "Content-Type": "application/json"

              }

            }

          );



        } catch (erro) {



          return new Response(

            JSON.stringify({

              success: false,

              error: erro.message

            }),

            {

              status: 500,

              headers: {

                ...corsHeaders,

                "Content-Type": "application/json"

              }

            }

          );



        }



      }



      if (path === "/login") {

        const { email, senha } = body;

        const stmt = env.DB.prepare("SELECT nome_usuarios, nivel_de_acesso, email, foto_perfil FROM tb_cadastros WHERE email = ? AND senha = ?").bind(email, senha);

        const { results } = await stmt.all();



        if (results && results.length > 0) {

          const user = results[0];

          return new Response(JSON.stringify({

            success: true, nome_usuario: user.nome_usuarios, nivel: user.nivel_de_acesso, email: user.email, foto_perfil: user.foto_perfil

          }), { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } });

        } else {

          return new Response(JSON.stringify({ success: false, message: "E-mail ou senha incorretos!" }), { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } });

        }

      }



      if (path === "/salvar-comentario") {

        const { nome_equipe, comentario } = body;

        if (!nome_equipe || !comentario) return new Response(JSON.stringify({ success: false, message: "Nome da equipe ou comentário ausente." }), { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } });



        const info = await env.DB.prepare(`

          UPDATE tb_acompanhamento_projeto

          SET comentario_empresa = ?

          WHERE usuario = ? OR usuario = (SELECT nome_usuarios FROM tb_cadastros WHERE email = ?)

        `).bind(comentario, nome_equipe, nome_equipe).run();



        if (info.meta.changes > 0) return new Response(JSON.stringify({ success: true, message: "Comentário salvo com sucesso!" }), { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } });

        else return new Response(JSON.stringify({ success: false, message: "Equipe não encontrada na base de dados." }), { status: 404, headers: { ...corsHeaders, "Content-Type": "application/json" } });

      }



     if (path === "/salvar-dados") {
  const { tipo } = body;
  // 1. Aceita tanto 'usuario' quanto 'nome_equipe' enviados no JSON
  const usuario = body.usuario || body.nome_equipe;

  if (!usuario || !tipo) {
    return new Response(JSON.stringify({
      success: false,
      error: "Os campos 'usuario' (ou 'nome_equipe') e 'tipo' são obrigatórios."
    }), {
      status: 200,
      headers: { ...corsHeaders, "Content-Type": "application/json" }
    });
  }

  if (tipo === "equipe") {
    // Verifica existência por usuario ou por nome_equipe
    const { results } = await env.DB.prepare(`
      SELECT id_equipe FROM tb_equipe WHERE usuario = ? OR nome_equipe = ?
    `).bind(usuario, usuario).all();

    const existe = results && results.length > 0;

    if (existe) {
      await env.DB.prepare(`
        UPDATE tb_equipe SET
          nome_integrante=?, nome_equipe=?, nome_projeto=?, email=?,
          area_atuacao_curso=?, area_atuacao_projeto=?, nome_integrante2=?,
          nome_integrante3=?, nome_integrante4=?, nome_integrante5=?,
          nome_orientador=?, nome_coorientador=?
        WHERE usuario=? OR nome_equipe=?
      `).bind(
        body.nome_integrante, body.nome_equipe, body.nome_projeto, body.email,
        body.area_atuacao_curso, body.area_atuacao_projeto, body.nome_integrante2,
        body.nome_integrante3, body.nome_integrante4, body.nome_integrante5,
        body.nome_orientador, body.nome_coorientador,
        usuario, usuario
      ).run();
    } else {
      await env.DB.prepare(`
        INSERT INTO tb_equipe(
          nome_integrante, nome_equipe, nome_projeto, email,
          area_atuacao_curso, area_atuacao_projeto, nome_integrante2,
          nome_integrante3, nome_integrante4, nome_integrante5,
          nome_orientador, nome_coorientador, usuario
        ) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)
      `).bind(
        body.nome_integrante, body.nome_equipe || usuario, body.nome_projeto, body.email,
        body.area_atuacao_curso, body.area_atuacao_projeto, body.nome_integrante2,
        body.nome_integrante3, body.nome_integrante4, body.nome_integrante5,
        body.nome_orientador, body.nome_coorientador, body.usuario || usuario
      ).run();
    }
    return new Response(JSON.stringify({ success: true }), { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } });
  }

  if (tipo === "recursos") {
    // 2. Resolve o nome_equipe buscando por usuario, email OU nome_equipe
    const equipeRecord = await env.DB.prepare(
      "SELECT nome_equipe FROM tb_equipe WHERE usuario = ? OR email = ? OR nome_equipe = ?"
    ).bind(usuario, usuario, usuario).first();

    const idFinal = equipeRecord ? equipeRecord.nome_equipe : usuario;

    const { results } = await env.DB.prepare(
      `SELECT id_recursos FROM tb_recursos_aplicados WHERE usuario = ?`
    ).bind(idFinal).all();

    const existe = results && results.length > 0;

    if (existe) {
      await env.DB.prepare(`
        UPDATE tb_recursos_aplicados SET
          ferramentas=?, equipamentos=?, descricao_produto=?, quant_comprada=?,
          quant_utilizada=?, preco_estimado=?, uni_medida=?, fornecedor_principal=?,
          modo_obtencao=?, disponibilidade=?, pagamento=?, alternativas_consideradas=?, preco_total=?
        WHERE usuario=?
      `).bind(
        body.ferramentas, body.equipamentos, body.descricao_produto, body.quant_comprada,
        body.quant_utilizada, body.preco_estimado, body.uni_medida, body.fornecedor_principal,
        body.modo_obtencao, body.disponibilidade, body.pagamento, body.alternativas_consideradas,
        body.preco_total, idFinal
      ).run();
    } else {
      await env.DB.prepare(`
        INSERT INTO tb_recursos_aplicados(
          ferramentas, equipamentos, descricao_produto, quant_comprada, quant_utilizada,
          preco_estimado, uni_medida, fornecedor_principal, modo_obtencao, disponibilidade,
          pagamento, alternativas_consideradas, preco_total, usuario
        ) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)
      `).bind(
        body.ferramentas, body.equipamentos, body.descricao_produto, body.quant_comprada,
        body.quant_utilizada, body.preco_estimado, body.uni_medida, body.fornecedor_principal,
        body.modo_obtencao, body.disponibilidade, body.pagamento, body.alternativas_consideradas,
        body.preco_total, idFinal
      ).run();
    }
    return new Response(JSON.stringify({ success: true }), { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } });
  }
  if (tipo === "cronograma" || tipo === "cronograma_especifico") {
  try {
    const { results } = await env.DB.prepare(`
      SELECT id_cronograma
      FROM tb_cronograma
      WHERE usuario = ? AND processo = ?
    `)
    .bind(usuario, body.processo || body.processos)
    .all();

    const existe = results && results.length > 0;

    if (existe) {
      await env.DB.prepare(`
        UPDATE tb_cronograma SET
          processo = ?,
          etapas = ?,
          responsavel = ?,
          data_inicio = ?,
          data_final = ?,
          observacoes = ?
        WHERE usuario = ? AND processo = ?
      `)
      .bind(
        body.processo || body.processos,
        body.etapas,
        body.responsavel,
        body.data_inicio || null,
        body.data_final || null,
        body.observacoes,
        usuario,
        body.processo || body.processos
      )
      .run();
    } else {
      await env.DB.prepare(`
        INSERT INTO tb_cronograma (
          processo, etapas, responsavel, data_inicio, data_final, observacoes, usuario
        )
        VALUES (?, ?, ?, ?, ?, ?, ?)
      `)
      .bind(
        body.processo || body.processos,
        body.etapas,
        body.responsavel,
        body.data_inicio || null,
        body.data_final || null,
        body.observacoes,
        usuario
      )
      .run();
    }

    return new Response(JSON.stringify({ success: true, message: "Cronograma salvo com sucesso" }), { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } });
  } catch (error) {
    return new Response(JSON.stringify({ success: false, error: error.message }), { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } });
  }
} else {
    // Lógica genérica para outras tabelas
    const tabelas = {
      "conhecimentos": "tb_conhecimentos",
      "recursos": "tb_recursos_aplicados",
      "canva": "tb_canva",
      "curriculo": "tb_curriculo_alunos",
      "empresas": "tb_empresas",
      "pitch": "tb_pitch",
      "uso_ia": "tb_uso_ia",
      "acompanhamento_projeto": "tb_acompanhamento_projeto",
      "planilha": "tb_acompanhamento_projeto",
      "informacoes_complementares": "tb_informacoes_complementares",
      "informacoes_completude": "tb_informacoes_completude",
      "participantes": "tb_participantes",
      "relatorio": "tb_relatorio",
      "necessidades": "tb_necessidades_empresas"
    };

    const tabela = tabelas[tipo];
    if (tabela) {
      const usaEquipe = ["canva", "pitch", "curriculo", "recursos", "conhecimentos", "informacoes_complementares", "informacoes_completude", "acompanhamento_projeto", "planilha"];
      let idFinal = usuario;

      if (usaEquipe.includes(tipo)) {
        const equipeRecord = await env.DB.prepare(
          "SELECT nome_equipe FROM tb_equipe WHERE usuario = ? OR email = ? OR nome_equipe = ?"
        ).bind(usuario, usuario, usuario).first();
        if (equipeRecord) idFinal = equipeRecord.nome_equipe;
      }

      // 3. Removemos campos de controle do array de campos
      const campos = Object.keys(body).filter(k => k !== "usuario" && k !== "tipo" && k !== "nome_equipe" && !k.startsWith("enai_cax") && !k.startsWith("email") && !k.startsWith("camiseta") && !k.startsWith("rg") && !k.startsWith("cpf") && !k.startsWith("data_nascimento") && !k.startsWith("idade") && !k.startsWith("telefone") && !k.startsWith("matricula"));

      const exists = await env.DB.prepare(`SELECT 1 FROM ${tabela} WHERE usuario = ?`).bind(idFinal).first();

      if (exists) {
        const setClause = campos.map(c => `${c} = ?`).join(", ");
        const values = campos.map(c => body[c]);
        await env.DB.prepare(`UPDATE ${tabela} SET ${setClause} WHERE usuario = ?`).bind(...values, idFinal).run();
      } else {
        const columns = ["usuario", ...campos].join(", ");
        const placeholders = ["?", ...campos.map(() => "?")].join(", ");
        const values = [idFinal, ...campos.map(c => body[c])];
        await env.DB.prepare(`INSERT INTO ${tabela} (${columns}) VALUES (${placeholders})`).bind(...values).run();
      }

      // Lógica especial para Participantes quando salvando Informações Complementares
      if (tipo === "informacoes_complementares") {
          const infoComp = await env.DB.prepare("SELECT id_informacoes_complementares FROM tb_informacoes_complementares WHERE usuario = ?").bind(idFinal).first();
          if (infoComp) {
              const idIC = infoComp.id_informacoes_complementares;
              await env.DB.prepare("DELETE FROM tb_participantes WHERE id_informacoes_complementares = ?").bind(idIC).run();

              for (let i = 1; i <= 7; i++) {
                  const nome = body[`enai_cax${i}`];
                  const emailP = body[`email${i}`];
                  if (nome && nome.trim() !== "") {
                      await env.DB.prepare(`
                          INSERT INTO tb_participantes (id_informacoes_complementares, nome_enai_cax, email, tamanho_camiseta, rg, cpf, data_nascimento, telefone, matricula)
                          VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                      `).bind(
                          idIC, nome, emailP, body[`camiseta${i}`] || "", body[`rg${i}`] || "", body[`cpf${i}`] || "", body[`data_nascimento${i}`] || "", body[`telefone${i}`] || "", body[`matricula${i}`] || ""
                      ).run();
                  }
              }
          }
      }

      return new Response(JSON.stringify({ success: true }), { headers: { ...corsHeaders, "Content-Type": "application/json" } });
    }
  }
}





    if (path === "/gerar-relatorio") {

        try {

          // 1. Pegar o usuário da URL (exemplo: /gerar-relatorio?usuario=joao@email.com)

          const url = new URL(request.url);

          const usuarioSolicitante = url.searchParams.get("usuario");



          if (!usuarioSolicitante) {

               return new Response(JSON.stringify({

                 success: false,

                 message: "Usuário não fornecido na requisição."

               }), { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } });

          }



          // 2. Fazer o SELECT buscando APENAS o usuário solicitante (usando AND eq.usuario = ?)

          const queryBusca = `

            SELECT

                eq.usuario, eq.id_equipe, eq.nome_equipe, eq.nome_projeto, eq.area_atuacao_curso, eq.area_atuacao_projeto,

                eq.nome_integrante, eq.nome_integrante2, eq.nome_integrante3, eq.nome_integrante4, eq.nome_integrante5,

                eq.nome_orientador, eq.nome_coorientador,

                emp.nome_empresa, emp.email_contato, emp.setor, emp.descricao AS descricao_empresa,

                p.roteiro,

                ic.unidade_nome_comercial, ic.gestor,

                ia.nome_ferramenta, ia.link_acesso, ia.tipo_licenca, ia.etapa_uso, ia.criacao_prompt, ia.descricao_uso AS descricao_ia,

                ra.ferramentas, ra.equipamentos, ra.quant_comprada, ra.quant_utilizada, ra.preco_total,

                ra.fornecedor_principal, ra.modo_obtencao, ra.alternativas_consideradas

            FROM tb_equipe eq

            LEFT JOIN tb_empresas emp ON (eq.usuario = emp.usuario OR eq.nome_equipe = emp.usuario)

            LEFT JOIN tb_pitch p ON (eq.usuario = p.usuario OR eq.nome_equipe = p.usuario)

            LEFT JOIN tb_informacoes_complementares ic ON (eq.usuario = ic.usuario OR eq.nome_equipe = ic.usuario)

            LEFT JOIN tb_uso_ia ia ON (eq.usuario = ia.usuario OR eq.nome_equipe = ia.usuario)

            LEFT JOIN tb_recursos_aplicados ra ON (eq.usuario = ra.usuario OR eq.nome_equipe = ra.usuario)
            WHERE eq.nome_equipe = ?

          `;



          // Passamos o usuarioSolicitante no bind()

          const { results } = await env.DB.prepare(queryBusca).bind(usuarioSolicitante).all();



          if (!results || results.length === 0) {

            return new Response(JSON.stringify({

              success: true,

              message: "Nenhum dado novo encontrado (já foi processado ou está incompleto)."

            }), { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } });

          }



          // 3. Preparar as queries de INSERT (tb_relatorio) e UPDATE (tb_equipe)

          const insertStmt = env.DB.prepare(`

            INSERT INTO tb_relatorio (

              nome_empresa, e_mail_empresa, setor_empresa, descricao, roteiro_pitch,

              integrante1, integrante2, integrante3, integrante4, integrante5,

              orientador, coorientador, nome_projeto, nome_equipe, area_atuacao_projeto,

              area_atuacao_curso, unidade_senai, gestor, ferramenta_ia, link_acesso,

              licenca, etapa_de_usu, prompt, motivo_usu, ferramentas_projeto,

              equipamentos_projeto, quant_compra, quant_utilizada, preco_total,

              fornecedor, modo_obtencao, alternativa_de_uso, processamento

            ) VALUES (

              ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0

            )

          `);



          // Atualizamos a tb_equipe baseando-se no usuario

          const updateEquipeStmt = env.DB.prepare(`UPDATE tb_equipe SET processado = 1 WHERE usuario = ?`);



          const batchQueries = [];



          // 4. Montar as transações

          for (const row of results) {

            batchQueries.push(

              insertStmt.bind(

                row.nome_empresa || "Não informado",

                row.email_contato || "Não informado",

                row.setor || "",

                row.descricao_empresa || "Sem descrição",

                row.roteiro || "",

                row.nome_integrante || "Não informado",

                row.nome_integrante2 || "",

                row.nome_integrante3 || "",

                row.nome_integrante4 || "",

                row.nome_integrante5 || "",

                row.nome_orientador || "",

                row.nome_coorientador || "",

                row.nome_projeto || "",

                row.nome_equipe || "",

                row.area_atuacao_projeto || "",

                row.area_atuacao_curso || "",

                row.unidade_nome_comercial || "",

                row.gestor || "",

                row.nome_ferramenta || "",

                row.link_acesso || "",

                row.tipo_licenca || "",

                row.etapa_uso || "",

                row.criacao_prompt || "",

                row.descricao_ia || "",

                row.ferramentas || "",

                row.equipamentos || "",

                row.quant_comprada || 0,

                row.quant_utilizada || 0,

                row.preco_total || 0,

                row.fornecedor_principal || "",

                row.modo_obtencao || "",

                row.alternativas_consideradas || ""

              )

            );



            batchQueries.push(updateEquipeStmt.bind(row.usuario));

          }



          // 5. Executa todas as inserções e updates

          await env.DB.batch(batchQueries);



          return new Response(JSON.stringify({

            success: true,

            message: `Sucesso! O relatório foi transferido e gerado.`

          }), { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } });



        } catch (error) {

          return new Response(JSON.stringify({

            success: false,

            error: "Erro ao gerar relatórios: " + error.message

          }), { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } });

        }

      }







if (path === "/gerar-canva") {

        try {

          const url = new URL(request.url);

          const usuarioSolicitante = url.searchParams.get("usuario");



          if (!usuarioSolicitante) {

               return new Response(JSON.stringify({

                 success: false, message: "Usuário não fornecido na requisição."

               }), { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } });

          }



          // Buscamos na tb_canva verificando se o usuário bate com a coluna usuario

          // ou se pertence à equipe daquele usuário

          const queryBusca = `

            SELECT c.* FROM tb_canva c

            LEFT JOIN tb_equipe eq ON (c.usuario = eq.usuario OR c.usuario = eq.nome_equipe)

            WHERE c.usuario = ? OR eq.usuario = ? OR eq.nome_equipe = ?

          `;



          const record = await env.DB.prepare(queryBusca).bind(usuarioSolicitante, usuarioSolicitante, usuarioSolicitante).first();



          if (!record) {

            return new Response(JSON.stringify({

              success: false,

              message: "Canva não preenchido ou não encontrado para esta equipe."

            }), { status: 404, headers: { ...corsHeaders, "Content-Type": "application/json" } });

          }



          return new Response(JSON.stringify({

            success: true,

            message: "Canva validado! Iniciando download..."

          }), { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } });



        } catch (error) {

          return new Response(JSON.stringify({

            success: false, error: "Erro ao validar Canva: " + error.message

          }), { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } });

        }

      }









      if (path === "/buscar-dados") {
        const { usuario, tipo } = body;

        if (!usuario || !tipo) {
          return new Response(JSON.stringify({ success: false, error: "Campos obrigatórios." }), { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } });
        }

        const tabelasValidas = {
          "equipe": "tb_equipe", "conhecimentos": "tb_conhecimentos", "recursos": "tb_recursos_aplicados",
          "cronograma": "tb_cronograma", "canva": "tb_canva", "curriculo": "tb_curriculo_alunos",
          "empresas": "tb_empresas", "pitch": "tb_pitch", "uso_ia": "tb_uso_ia",
          "acompanhamento_projeto": "tb_acompanhamento_projeto", "planilha": "tb_acompanhamento_projeto",
          "informacoes_complementares": "tb_informacoes_complementares", "informacoes_completude": "tb_informacoes_completude",
          "participantes": "tb_participantes", "relatorio": "tb_relatorio"
        };

        try {
          const userRecord = await env.DB.prepare("SELECT nome_usuarios, email FROM tb_cadastros WHERE email = ? OR nome_usuarios = ?").bind(usuario, usuario).first();
          const nomeReal = userRecord ? userRecord.nome_usuarios : usuario;
          const emailReal = (userRecord && userRecord.email) ? userRecord.email : usuario;
          const equipeRecord = await env.DB.prepare("SELECT nome_equipe FROM tb_equipe WHERE usuario = ? OR email = ?").bind(usuario, usuario).first();
          const nomeEquipe = equipeRecord ? equipeRecord.nome_equipe : usuario;

          if (tipo === "cronograma" || tipo === "cronograma_especifico") {
            const { results } = await env.DB.prepare("SELECT * FROM tb_cronograma WHERE responsavel = ? OR responsavel = ? OR usuario = ?").bind(nomeReal, emailReal, nomeEquipe).all();
            return new Response(JSON.stringify({ success: true, existe: results.length > 0, data: results }), { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } });

          } else if (tipo === "empresas") {
            const dadosForm = await env.DB.prepare("SELECT * FROM tb_empresas WHERE email_contato = ? OR nome_empresa = ? OR usuario = ?").bind(emailReal, nomeReal, usuario).first();
            return new Response(JSON.stringify({ success: true, existe: dadosForm !== null, data: dadosForm }), { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } });

          } else if (tipo === "informacoes_complementares") {
              const dadosForm = await env.DB.prepare("SELECT * FROM tb_informacoes_complementares WHERE usuario = ? OR usuario = ? OR usuario = ?").bind(nomeReal, emailReal, nomeEquipe).first();
              if (dadosForm) {
                  const { results: participantes } = await env.DB.prepare("SELECT * FROM tb_participantes WHERE id_informacoes_complementares = ?").bind(dadosForm.id_informacoes_complementares).all();
                  participantes.forEach((p, index) => {
                      const i = index + 1;
                      dadosForm[`enai_cax${i}`] = p.nome_enai_cax;
                      dadosForm[`email${i}`] = p.email;
                      dadosForm[`camiseta${i}`] = p.tamanho_camiseta;
                      dadosForm[`rg${i}`] = p.rg;
                      dadosForm[`cpf${i}`] = p.cpf;
                      dadosForm[`data_nascimento${i}`] = p.data_nascimento;
                      dadosForm[`telefone${i}`] = p.telefone;
                      dadosForm[`matricula${i}`] = p.matricula;
                  });
              }
              return new Response(JSON.stringify({ success: true, existe: dadosForm !== null, data: dadosForm }), { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } });

          } else if (tipo === "participantes") {
              // Busca participantes baseada na equipe do usuário
              const query = `
                SELECT p.* FROM tb_participantes p
                JOIN tb_informacoes_complementares ic ON p.id_informacoes_complementares = ic.id_informacoes_complementares
                WHERE ic.usuario = ? OR ic.usuario = ? OR ic.usuario = ?
              `;
              const dadosForm = await env.DB.prepare(query).bind(nomeReal, emailReal, nomeEquipe).first();
              return new Response(JSON.stringify({ success: true, existe: dadosForm !== null, data: dadosForm }), { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } });

          } else if (tabelasValidas[tipo]) {
            const query = `SELECT * FROM ${tabelasValidas[tipo]} WHERE usuario = ? OR usuario = ? OR usuario = ?`;
            const dadosForm = await env.DB.prepare(query).bind(nomeReal, emailReal, nomeEquipe).first();

            return new Response(JSON.stringify({ success: true, existe: dadosForm !== null, data: dadosForm }), { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } });

          }

        } catch (e) {
          return new Response(JSON.stringify({ success: false, error: e.message }), { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } });
        }
      }








      // ===============================================================

      // ROTA POST: RASTREAR E PREENCHER CURRÍCULO AUTOMATICAMENTE

      // ===============================================================

      if (method === "POST" && path === "/preencher-curriculo") {

        const { nome_usuario, email_usuario, cpf_usuario, habilidades, fez_projeto, cidade, motivo_projeto, aprendo_mais, prefiro_trabalhar } = body;



        if (!nome_usuario) {

          return new Response(JSON.stringify({ success: false, message: "O nome de usuário é obrigatório." }), {

            status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" }

          });

        }



        try {

          // -------------------------------------------------------------

          // ETAPA 1: Buscar dados base na tb_participantes

          // -------------------------------------------------------------

          let participante = null;



          if (cpf_usuario) {

            participante = await env.DB.prepare("SELECT * FROM tb_participantes WHERE nome_enai_cax = ? AND cpf = ?").bind(nome_usuario, cpf_usuario).first();

          }

          if (!participante && email_usuario) {

            participante = await env.DB.prepare("SELECT * FROM tb_participantes WHERE nome_enai_cax = ? AND email = ?").bind(nome_usuario, email_usuario).first();

          }

          if (!participante) {

            participante = await env.DB.prepare("SELECT * FROM tb_participantes WHERE nome_enai_cax = ?").bind(nome_usuario).first();

          }



          let dbNome = nome_usuario;

          let dbDataNascimento = null;

          let dbCpf = cpf_usuario || "Não informado";

          let dbTelefone = "Não informado";

          let dbEmail = email_usuario || "Não informado";

          let idInfoComplementares = null;



          if (participante) {

            dbNome = participante.nome_enai_cax || dbNome;

            dbDataNascimento = participante.data_nascimento || null;

            dbCpf = participante.cpf || dbCpf;

            dbTelefone = participante.telefone || dbTelefone;

            dbEmail = participante.email || dbEmail;

            idInfoComplementares = participante.id_informacoes_complementares;

          }



          // -------------------------------------------------------------

          // ETAPA 2: Buscar na tb_informacoes_complementares

          // -------------------------------------------------------------

          let dbProjeto = "Não informado";

          let dbUsuarioEquipe = null;

          let dbEmpresaVinculada = "Não informada";

          let dbMotivoProjeto = motivo_projeto || "Participação em projeto de inovação SENAI/FUTUR.E";



          if (idInfoComplementares) {

            const infoComp = await env.DB.prepare("SELECT * FROM tb_informacoes_complementares WHERE id_informacoes_complementares = ?")

              .bind(idInfoComplementares).first();



            if (infoComp) {

              dbProjeto = infoComp.projeto || dbProjeto;

              dbUsuarioEquipe = infoComp.usuario || null;

              dbEmpresaVinculada = infoComp.empresa || dbEmpresaVinculada;

              if (!motivo_projeto && infoComp.descricao) {

                dbMotivoProjeto = infoComp.descricao; // Usa a descrição do projeto como motivação padrão

              }

            }

          }



          // -------------------------------------------------------------

          // ETAPA 3: Buscar tarefas na tb_acompanhamento_projeto (O QUE DESENVOLVI)

          // -------------------------------------------------------------

          // CORREÇÃO: Na sua tabela a coluna se chama 'aluno_responsavel' e não 'responsavel'

          const tarefasQuery = await env.DB.prepare(

            "SELECT tarefas FROM tb_acompanhamento_projeto WHERE aluno_responsavel = ? AND status = 'Concluído'"

          ).bind(dbNome).all();



          let tarefasFeitasStr = "Nenhuma tarefa concluída registrada até o momento.";

          if (tarefasQuery && tarefasQuery.results.length > 0) {

            tarefasFeitasStr = tarefasQuery.results.map(t => t.tarefas).join(" • ");

          }



          // -------------------------------------------------------------

          // ETAPA 4: Salvar / Atualizar na tb_curriculo_alunos

          // -------------------------------------------------------------

          const curriculoExistente = await env.DB.prepare("SELECT * FROM tb_curriculo_alunos WHERE email = ? OR cpf = ? OR nome = ?").bind(dbEmail, dbCpf, dbNome).first();



          if (curriculoExistente) {

            // Se já existe, atualizamos os dados rastreados, mas PRESERVAMOS o que o aluno preencheu manualmente caso não venha no body

            await env.DB.prepare(`

              UPDATE tb_curriculo_alunos SET

                nome = ?, data_nacimento = ?, telefone = ?,

                empresa_vinculado = ?, projeto = ?, usuario = ?,

                tarefas_feitas = ?,

                habilidades = COALESCE(?, habilidades),

                fez_projeto = COALESCE(?, fez_projeto),

                cidade = COALESCE(?, cidade),

                motivo_projeto = COALESCE(?, motivo_projeto),

                aprendo_mais = COALESCE(?, aprendo_mais),

                prefiro_trabalhar = COALESCE(?, prefiro_trabalhar)

              WHERE id_aluno = ?

            `).bind(

              dbNome, dbDataNascimento, dbTelefone,

              dbEmpresaVinculada, dbProjeto, dbUsuarioEquipe,

              tarefasFeitasStr,

              habilidades || null, fez_projeto || null, cidade || null, dbMotivoProjeto || null, aprendo_mais || null, prefiro_trabalhar || null,

              curriculoExistente.id_aluno

            ).run();

          } else {

            // INSERT inicial

            await env.DB.prepare(`

              INSERT INTO tb_curriculo_alunos (

                nome, data_nacimento, cpf, empresa_vinculado, projeto,

                telefone, email, habilidades, fez_projeto, cidade,

                motivo_projeto, aprendo_mais, prefiro_trabalhar,

                usuario, tarefas_feitas

              ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)

            `).bind(

              dbNome, dbDataNascimento, dbCpf, dbEmpresaVinculada, dbProjeto,

              dbTelefone, dbEmail, habilidades || null, fez_projeto || null, cidade || null,

              dbMotivoProjeto, aprendo_mais || null, prefiro_trabalhar || null,

              dbUsuarioEquipe, tarefasFeitasStr

            ).run();

          }



          return new Response(JSON.stringify({

            success: true,

            message: "Currículo sincronizado com sucesso!",

            resumo_rastreado: {

              nome: dbNome,

              projeto: dbProjeto,

              empresa: dbEmpresaVinculada,

              tarefas_desenvolvidas: tarefasFeitasStr

            }

          }), { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } });



        } catch (error) {

          return new Response(JSON.stringify({ success: false, error: "Erro no banco: " + error.message }), { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } });

        }

      }









    } catch (error) {

      return new Response(JSON.stringify({ success: false, error: "Erro na leitura do JSON: " + error.message }), { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } });

    }



    return new Response("Rota não encontrada", { status: 404, headers: corsHeaders });

  }
};