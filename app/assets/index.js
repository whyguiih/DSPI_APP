// ==========================================
// FUNÇÃO PARA UPLOAD DE VÍDEO NO CLOUDFLARE R2
// ==========================================
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

      if (path === "/cadastrar") {
        const { nome_usuarios, email, senha } = body;
        if (!nome_usuarios || !email || !senha) return new Response(JSON.stringify({ success: false, message: "Campos obrigatórios ausentes!" }), { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } });
        try {
          await env.DB.prepare("INSERT INTO tb_cadastros (nome_usuarios, email, senha, nivel_de_acesso) VALUES (?, ?, ?, 6)").bind(nome_usuarios, email, senha).run();
          return new Response(JSON.stringify({ success: true, message: "Usuário cadastrado com sucesso!" }), { status: 201, headers: { ...corsHeaders, "Content-Type": "application/json" } });
        } catch (error) {
          return new Response(JSON.stringify({ success: false, message: "Este e-mail já está cadastrado!" }), { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } });
        }
      }

      if (path === "/salvar-dados") {
        const { usuario, tipo } = body;

        if (!usuario || !tipo) {
          return new Response(JSON.stringify({ success: false, error: "Campos 'usuario' e 'tipo' são obrigatórios." }), { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } });
        }

        if (tipo === "equipe") {
          const { results } = await env.DB.prepare(`SELECT id_equipe FROM tb_equipe WHERE usuario = ?`).bind(usuario).all();
          const existe = results && results.length > 0;

          if (existe) {
            await env.DB.prepare(`
              UPDATE tb_equipe SET
                nome_integrante=?, nome_equipe=?, nome_projeto=?, email=?,
                area_atuacao_curso=?, area_atuacao_projeto=?,
                nome_integrante2=?, nome_integrante3=?, nome_integrante4=?, nome_integrante5=?,
                nome_orientador=?, nome_coorientador=?
              WHERE usuario=?
            `).bind(
              body.nome_integrante, body.nome_equipe, body.nome_projeto, body.email,
              body.area_atuacao_curso, body.area_atuacao_projeto,
              body.nome_integrante2, body.nome_integrante3, body.nome_integrante4, body.nome_integrante5,
              body.nome_orientador, body.nome_coorientador, usuario
            ).run();
          } else {
            await env.DB.prepare(`
              INSERT INTO tb_equipe(
                nome_integrante, nome_equipe, nome_projeto, email,
                area_atuacao_curso, area_atuacao_projeto,
                nome_integrante2, nome_integrante3, nome_integrante4, nome_integrante5,
                nome_orientador, nome_coorientador, usuario
              ) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)
            `).bind(
              body.nome_integrante, body.nome_equipe, body.nome_projeto, body.email,
              body.area_atuacao_curso, body.area_atuacao_projeto,
              body.nome_integrante2, body.nome_integrante3, body.nome_integrante4, body.nome_integrante5,
              body.nome_orientador, body.nome_coorientador, usuario
            ).run();
          }

          return new Response(JSON.stringify({ success: true }), { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } });
        } else {
          // Lógica genérica para outras tabelas
          const tabelas = {
            "conhecimentos": "tb_conhecimentos", "recursos": "tb_recursos_aplicados",
            "cronograma": "tb_cronograma", "canva": "tb_canva", "curriculo": "tb_curriculo_alunos",
            "empresas": "tb_empresas", "pitch": "tb_pitch", "uso_ia": "tb_uso_ia",
            "informacoes_complementares": "tb_informacoes_complementares", "informacoes_completude": "tb_informacoes_completude",
            "participantes": "tb_participantes", "relatorio": "tb_relatorio"
          };

          const tabela = tabelas[tipo];
          if (tabela) {
            // Buscamos o nome da equipe associado ao e-mail para tabelas que usam o nome_equipe como Foreign Key
            const usaEquipe = ["canva", "pitch", "curriculo", "recursos", "conhecimentos"];
            let idFinal = usuario;
            if (usaEquipe.includes(tipo)) {
              const equipeRecord = await env.DB.prepare("SELECT nome_equipe FROM tb_equipe WHERE usuario = ? OR email = ?").bind(usuario, usuario).first();
              if (equipeRecord) idFinal = equipeRecord.nome_equipe;
            }

            const campos = Object.keys(body).filter(k => k !== "usuario" && k !== "tipo");
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
            return new Response(JSON.stringify({ success: true }), { headers: { ...corsHeaders, "Content-Type": "application/json" } });
          }
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
          "acompanhamento_projeto": "tb_acompanhamento_projeto",
          "informacoes_complementares": "tb_informacoes_complementares", "informacoes_completude": "tb_informacoes_completude",
          "participantes": "tb_participantes", "relatorio": "tb_relatorio"
        };

        try {
          const userRecord = await env.DB.prepare("SELECT nome_usuarios, email FROM tb_cadastros WHERE email = ? OR nome_usuarios = ?").bind(usuario, usuario).first();
          const nomeReal = userRecord ? userRecord.nome_usuarios : usuario;
          const emailReal = (userRecord && userRecord.email) ? userRecord.email : usuario;
          const equipeRecord = await env.DB.prepare("SELECT nome_equipe FROM tb_equipe WHERE usuario = ? OR email = ?").bind(usuario, usuario).first();
          const nomeEquipe = equipeRecord ? equipeRecord.nome_equipe : usuario;

          if (tipo === "cronograma") {
            const { results } = await env.DB.prepare("SELECT * FROM tb_cronograma WHERE responsavel = ? OR responsavel = ? OR usuario = ?").bind(nomeReal, emailReal, nomeEquipe).all();
            return new Response(JSON.stringify({ success: true, existe: results.length > 0, data: results }), { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } });

          } else if (tipo === "empresas") {
            const dadosForm = await env.DB.prepare("SELECT * FROM tb_empresas WHERE email_contato = ? OR nome_empresa = ?").bind(emailReal, nomeReal).first();
            return new Response(JSON.stringify({ success: true, existe: dadosForm !== null, data: dadosForm }), { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } });

          } else if (tabelasValidas[tipo]) {
            const query = `SELECT * FROM ${tabelasValidas[tipo]} WHERE usuario = ? OR usuario = ? OR usuario = ?`;
            const dadosForm = await env.DB.prepare(query).bind(nomeReal, emailReal, nomeEquipe).first();

            return new Response(JSON.stringify({ success: true, existe: dadosForm !== null, data: dadosForm }), { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } });

          } else if (tipo === "participantes") {
            return new Response(JSON.stringify({ success: true }), { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } });
          }

        } catch (e) {
          return new Response(JSON.stringify({ success: false, error: e.message }), { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } });
        }
      }

    } catch (error) {
      return new Response(JSON.stringify({ success: false, error: "Erro na leitura do JSON: " + error.message }), { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } });
    }

    return new Response("Rota não encontrada", { status: 404, headers: corsHeaders });
  }
};