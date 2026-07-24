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
      const { tipo, usuario, campos } = body;

      //============SALVAR DADOS FORMULARIO==================
      if (path === "/salvar-dados") {

        if (!usuario || !tipo) {
          return new Response(JSON.stringify({
            success: false,
            error: "Dados inválidos."
          }), {
            status: 400,
            headers: { ...corsHeaders, "Content-Type": "application/json" }
          });
        }

        //===============TB_EQUIPE===============
        if (tipo === "equipe") {

          const {
            nome_equipe,
            nome_projeto,
            email,
            area_atuacao_curso,
            area_atuacao_projeto,
            nome_orientador,
            nome_coorientador,
            nome_integrante,
            nome_integrante2,
            nome_integrante3,
            nome_integrante4,
            nome_integrante5
          } = campos;

          const equipe = await env.DB.prepare(`
        SELECT id_equipe
        FROM tb_equipe
        WHERE usuario = ?
    `).bind(usuario).first();

          if (equipe) {

            await env.DB.prepare(`
            UPDATE tb_equipe
            SET
                nome_equipe = ?,
                nome_projeto = ?,
                email = ?,
                area_atuacao_curso = ?,
                area_atuacao_projeto = ?,
                nome_orientador = ?,
                nome_coorientador = ?,
                nome_integrante = ?,
                nome_integrante2 = ?,
                nome_integrante3 = ?,
                nome_integrante4 = ?,
                nome_integrante5 = ?
            WHERE usuario = ?
        `).bind(
              nome_equipe,
              nome_projeto,
              email,
              area_atuacao_curso,
              area_atuacao_projeto,
              nome_orientador,
              nome_coorientador,
              nome_integrante,
              nome_integrante2,
              nome_integrante3,
              nome_integrante4,
              nome_integrante5,
              usuario
            ).run();

          } else {

            await env.DB.prepare(`
            INSERT INTO tb_equipe
            (
                nome_equipe,
                nome_projeto,
                email,
                area_atuacao_curso,
                area_atuacao_projeto,
                nome_orientador,
                nome_coorientador,
                nome_integrante,
                nome_integrante2,
                nome_integrante3,
                nome_integrante4,
                nome_integrante5,
                usuario
            )
            VALUES
            (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        `).bind(
              nome_equipe,
              nome_projeto,
              email,
              area_atuacao_curso,
              area_atuacao_projeto,
              nome_orientador,
              nome_coorientador,
              nome_integrante,
              nome_integrante2,
              nome_integrante3,
              nome_integrante4,
              nome_integrante5,
              usuario
            ).run();

          }

          return new Response(JSON.stringify({
            success: true
          }), {
            status: 200,
            headers: {
              ...corsHeaders,
              "Content-Type": "application/json"
            }
          });

        }
        //===============TB_CONHECIMENTOS===============
        if (tipo === "conhecimentos") {

          const {
            plano_curso,
            conhecimentos_aplicados,
            capacidades_aplicadas
          } = campos;

          const equipe = await env.DB.prepare(`
    SELECT nome_equipe
    FROM tb_equipe
    WHERE usuario = ?
`).bind(usuario).first();

          if (!equipe) {
            return new Response(JSON.stringify({
              success: false,
              error: "Equipe não encontrada. Salve os dados da equipe primeiro."
            }), {
              status: 400,
              headers: {
                ...corsHeaders,
                "Content-Type": "application/json"
              }
            });
          }

          const usuarioEquipe = equipe.nome_equipe;

          const conhecimento = await env.DB.prepare(`
    SELECT id_conhecimentos
    FROM tb_conhecimentos
    WHERE usuario = ?
`).bind(usuarioEquipe).first();

          if (conhecimento) {

            await env.DB.prepare(`
            UPDATE tb_conhecimentos
            SET
                plano_curso = ?,
                conhecimentos_aplicados = ?,
                capacidades_aplicadas = ?
            WHERE usuario = ?
        `).bind(
              plano_curso,
              conhecimentos_aplicados,
              capacidades_aplicadas,
              usuarioEquipe
            ).run();

          } else {

            await env.DB.prepare(`
            INSERT INTO tb_conhecimentos
            (
                plano_curso,
                conhecimentos_aplicados,
                capacidades_aplicadas,
                usuario
            )
            VALUES
            (?, ?, ?, ?)
        `).bind(
              plano_curso,
              conhecimentos_aplicados,
              capacidades_aplicadas,
              usuarioEquipe
            ).run();

          }

          return new Response(JSON.stringify({
            success: true
          }), {
            status: 200,
            headers: {
              ...corsHeaders,
              "Content-Type": "application/json"
            }
          });
        }

        //===============TB_RECURSOS_APLICADOS===============
        if (tipo === "recursos") {

          const {
            ferramentas,
            equipamentos,
            descricao_produto,
            quant_comprada,
            quant_utilizada,
            preco_estimado,
            uni_medida,
            fornecedor_principal,
            modo_obtencao,
            disponibilidade,
            pagamento,
            alternativas_consideradas,
            preco_total
          } = campos;

          // Buscar id_equipe e nome_equipe usando o e-mail do usuário
          const equipe = await env.DB.prepare(`
        SELECT id_equipe, nome_equipe
        FROM tb_equipe
        WHERE usuario = ?
    `).bind(usuario).first();

          if (!equipe) {
            return new Response(JSON.stringify({
              success: false,
              error: "Equipe não encontrada."
            }), {
              status: 404,
              headers: {
                ...corsHeaders,
                "Content-Type": "application/json"
              }
            });
          }

          // Verifica se já existe registro
          const recursos = await env.DB.prepare(`
        SELECT id_recursos
        FROM tb_recursos_aplicados
        WHERE usuario = ?
    `).bind(equipe.nome_equipe).first();

          if (recursos) {

            await env.DB.prepare(`
            UPDATE tb_recursos_aplicados
            SET
                ferramentas = ?,
                equipamentos = ?,
                descricao_produto = ?,
                quant_comprada = ?,
                quant_utilizada = ?,
                preco_estimado = ?,
                uni_medida = ?,
                fornecedor_principal = ?,
                modo_obtencao = ?,
                disponibilidade = ?,
                pagamento = ?,
                alternativas_consideradas = ?,
                preco_total = ?
            WHERE usuario = ?
        `).bind(
              ferramentas,
              equipamentos,
              descricao_produto,
              quant_comprada,
              quant_utilizada,
              preco_estimado,
              uni_medida,
              fornecedor_principal,
              modo_obtencao,
              disponibilidade,
              pagamento,
              alternativas_consideradas,
              preco_total,
              equipe.nome_equipe
            ).run();

          } else {

            await env.DB.prepare(`
            INSERT INTO tb_recursos_aplicados
            (
                id_recursos,
                ferramentas,
                equipamentos,
                descricao_produto,
                quant_comprada,
                quant_utilizada,
                preco_estimado,
                uni_medida,
                fornecedor_principal,
                modo_obtencao,
                disponibilidade,
                pagamento,
                alternativas_consideradas,
                preco_total,
                usuario
            )
            VALUES
            (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        `).bind(
              equipe.id_equipe,
              ferramentas,
              equipamentos,
              descricao_produto,
              quant_comprada,
              quant_utilizada,
              preco_estimado,
              uni_medida,
              fornecedor_principal,
              modo_obtencao,
              disponibilidade,
              pagamento,
              alternativas_consideradas,
              preco_total,
              equipe.nome_equipe
            ).run();

          }

          return new Response(JSON.stringify({
            success: true
          }), {
            status: 200,
            headers: {
              ...corsHeaders,
              "Content-Type": "application/json"
            }
          });
        }

        //===============TB_CRONOGRAMA===============
        if (tipo === "cronograma") {

          const {
            processo,
            etapas,
            responsavel,
            data_inicio,
            data_final,
            observacoes
          } = campos;

          // Buscar id_equipe e nome_equipe pelo e-mail do usuário
          const equipe = await env.DB.prepare(`
    SELECT id_equipe, nome_equipe
    FROM tb_equipe
    WHERE usuario = ?
  `).bind(usuario).first();

          if (!equipe) {
            return new Response(JSON.stringify({
              success: false,
              error: "Equipe não encontrada."
            }), {
              status: 404,
              headers: {
                ...corsHeaders,
                "Content-Type": "application/json"
              }
            });
          }

          // Verifica se já existe cronograma
          const cronograma = await env.DB.prepare(`
    SELECT id_cronograma
    FROM tb_cronograma
    WHERE usuario = ?
  `).bind(equipe.nome_equipe).first();

          if (cronograma) {

            await env.DB.prepare(`
      UPDATE tb_cronograma
      SET
        processo = ?,
        etapas = ?,
        responsavel = ?,
        data_inicio = ?,
        data_final = ?,
        observacoes = ?
      WHERE usuario = ?
    `).bind(
              processo,
              etapas,
              responsavel,
              data_inicio,
              data_final,
              observacoes,
              equipe.nome_equipe
            ).run();

          } else {

            await env.DB.prepare(`
      INSERT INTO tb_cronograma
      (
        id_cronograma,
        processo,
        etapas,
        responsavel,
        data_inicio,
        data_final,
        observacoes,
        usuario
      )
      VALUES
      (?, ?, ?, ?, ?, ?, ?, ?)
    `).bind(
              equipe.id_equipe,
              processo,
              etapas,
              responsavel,
              data_inicio,
              data_final,
              observacoes,
              equipe.nome_equipe
            ).run();

          }

          return new Response(JSON.stringify({
            success: true
          }), {
            status: 200,
            headers: {
              ...corsHeaders,
              "Content-Type": "application/json"
            }
          });

        }

        //===============TB_CANVA===============
        if (tipo === "canva") {

          const {
            atividades_chaves,
            proposta_chave,
            relacionamentos_clientes,
            segmentos_clientes,
            recursos_chaves,
            canais,
            estrutura_custos,
            fluxo_receita,
            parceiros_chaves
          } = campos;

          // Buscar o nome da equipe pelo e-mail do usuário
          const equipe = await env.DB.prepare(`
    SELECT nome_equipe
    FROM tb_equipe
    WHERE usuario = ?
  `).bind(usuario).first();

          if (!equipe) {
            return new Response(JSON.stringify({
              success: false,
              error: "Equipe não encontrada."
            }), {
              status: 404,
              headers: {
                ...corsHeaders,
                "Content-Type": "application/json"
              }
            });
          }

          const canva = await env.DB.prepare(`
    SELECT id_canva
    FROM tb_canva
    WHERE usuario = ?
  `).bind(equipe.nome_equipe).first();

          if (canva) {

            await env.DB.prepare(`
      UPDATE tb_canva
      SET
        atividades_chaves = ?,
        proposta_chave = ?,
        relacionamentos_clientes = ?,
        segmentos_clientes = ?,
        recursos_chaves = ?,
        canais = ?,
        estrutura_custos = ?,
        fluxo_receita = ?,
        parceiros_chaves = ?
      WHERE usuario = ?
    `).bind(
              atividades_chaves,
              proposta_chave,
              relacionamentos_clientes,
              segmentos_clientes,
              recursos_chaves,
              canais,
              estrutura_custos,
              fluxo_receita,
              parceiros_chaves,
              equipe.nome_equipe
            ).run();

          } else {

            await env.DB.prepare(`
      INSERT INTO tb_canva
      (
        atividades_chaves,
        proposta_chave,
        relacionamentos_clientes,
        segmentos_clientes,
        recursos_chaves,
        canais,
        estrutura_custos,
        fluxo_receita,
        parceiros_chaves,
        usuario
      )
      VALUES
      (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    `).bind(
              atividades_chaves,
              proposta_chave,
              relacionamentos_clientes,
              segmentos_clientes,
              recursos_chaves,
              canais,
              estrutura_custos,
              fluxo_receita,
              parceiros_chaves,
              equipe.nome_equipe
            ).run();

          }

          return new Response(JSON.stringify({
            success: true
          }), {
            status: 200,
            headers: {
              ...corsHeaders,
              "Content-Type": "application/json"
            }
          });

        }

        //===============TB_EMPRESAS_FORMULARIO===============
        if (tipo === "empresa") {

          const {
            nome_empresa,
            cnpj,
            regiao,
            telefone_contato,
            email_contato,
            objetivos,
            problema_projeto
          } = campos;

          // Busca a equipe do usuário
          const equipe = await env.DB.prepare(`
    SELECT id_equipe
    FROM tb_equipe
    WHERE usuario = ?
  `).bind(usuario).first();

          if (!equipe) {
            return new Response(JSON.stringify({
              success: false,
              error: "Equipe não encontrada."
            }), {
              status: 404,
              headers: {
                ...corsHeaders,
                "Content-Type": "application/json"
              }
            });
          }

          // Verifica se já existe
          const empresa = await env.DB.prepare(`
    SELECT id_empresa_formulario
    FROM tb_empresas_formulario
    WHERE id_empresa_formulario = ?
  `).bind(equipe.id_equipe).first();

          if (empresa) {

            await env.DB.prepare(`
      UPDATE tb_empresas_formulario
      SET
        nome_empresa = ?,
        cnpj = ?,
        regiao = ?,
        telefone_contato = ?,
        email_contato = ?,
        objetivos = ?,
        problema_projeto = ?
      WHERE id_empresa_formulario = ?
    `).bind(
              nome_empresa,
              cnpj,
              regiao,
              telefone_contato,
              email_contato,
              objetivos,
              problema_projeto,
              equipe.id_equipe
            ).run();

          } else {

            await env.DB.prepare(`
      INSERT INTO tb_empresas_formulario
      (
        id_empresa_formulario,
        nome_empresa,
        cnpj,
        regiao,
        telefone_contato,
        email_contato,
        objetivos,
        problema_projeto
      )
      VALUES (?, ?, ?, ?, ?, ?, ?, ?)
    `).bind(
              equipe.id_equipe,
              nome_empresa,
              cnpj,
              regiao,
              telefone_contato,
              email_contato,
              objetivos,
              problema_projeto
            ).run();

          }

          return new Response(JSON.stringify({
            success: true
          }), {
            status: 200,
            headers: {
              ...corsHeaders,
              "Content-Type": "application/json"
            }
          });

        }

        //===============TB_PITCH===============
        if (tipo === "pitch") {

          const {
            roteiro
          } = campos;

          // Buscar dados da equipe
          const equipe = await env.DB.prepare(`
    SELECT id_equipe, nome_equipe
    FROM tb_equipe
    WHERE usuario = ?
  `).bind(usuario).first();

          if (!equipe) {
            return new Response(JSON.stringify({
              success: false,
              error: "Equipe não encontrada."
            }), {
              status: 404,
              headers: {
                ...corsHeaders,
                "Content-Type": "application/json"
              }
            });
          }

          const pitch = await env.DB.prepare(`
    SELECT id_pitch
    FROM tb_pitch
    WHERE usuario = ?
  `).bind(equipe.nome_equipe).first();

          if (pitch) {

            await env.DB.prepare(`
      UPDATE tb_pitch
      SET
        roteiro = ?
      WHERE usuario = ?
    `).bind(
              roteiro,
              equipe.nome_equipe
            ).run();

          } else {

            await env.DB.prepare(`
      INSERT INTO tb_pitch
      (
        id_pitch,
        roteiro,
        usuario
      )
      VALUES
      (?, ?, ?)
    `).bind(
              equipe.id_equipe,
              roteiro,
              equipe.nome_equipe
            ).run();

          }

          return new Response(JSON.stringify({
            success: true
          }), {
            status: 200,
            headers: {
              ...corsHeaders,
              "Content-Type": "application/json"
            }
          });

        }

        //===============TB_USO_IA===============
        if (tipo === "ia") {

          const {
            nome_ferramenta,
            link_acesso,
            tipo_licenca,
            etapa_uso,
            criacao_prompt,
            descricao_uso
          } = campos;

          // Busca o nome da equipe pelo e-mail
          const equipe = await env.DB.prepare(`
    SELECT nome_equipe
    FROM tb_equipe
    WHERE usuario = ?
  `).bind(usuario).first();

          if (!equipe) {
            return new Response(JSON.stringify({
              success: false,
              error: "Equipe não encontrada."
            }), {
              status: 404,
              headers: {
                ...corsHeaders,
                "Content-Type": "application/json"
              }
            });
          }

          const ia = await env.DB.prepare(`
    SELECT id_uso_ia
    FROM tb_uso_ia
    WHERE usuario = ?
  `).bind(equipe.nome_equipe).first();

          if (ia) {

            await env.DB.prepare(`
      UPDATE tb_uso_ia
      SET
        nome_ferramenta = ?,
        link_acesso = ?,
        tipo_licenca = ?,
        etapa_uso = ?,
        criacao_prompt = ?,
        descricao_uso = ?
      WHERE usuario = ?
    `).bind(
              nome_ferramenta,
              link_acesso,
              tipo_licenca,
              etapa_uso,
              criacao_prompt,
              descricao_uso,
              equipe.nome_equipe
            ).run();

          } else {

            await env.DB.prepare(`
      INSERT INTO tb_uso_ia
      (
        usuario,
        nome_ferramenta,
        link_acesso,
        tipo_licenca,
        etapa_uso,
        criacao_prompt,
        descricao_uso
      )
      VALUES (?, ?, ?, ?, ?, ?, ?)
    `).bind(
              equipe.nome_equipe,
              nome_ferramenta,
              link_acesso,
              tipo_licenca,
              etapa_uso,
              criacao_prompt,
              descricao_uso
            ).run();

          }

          return new Response(JSON.stringify({
            success: true
          }), {
            status: 200,
            headers: {
              ...corsHeaders,
              "Content-Type": "application/json"
            }
          });

        }

        //===============TB_ACOMPANHAMENTO_PROJETO===============
        if (tipo === "planilha") {

          const {
            tarefas,
            aluno_responsavel,
            professor_da_area,
            inicio_previsto,
            fim_previsto,
            inicio_realizado,
            fim_realizado,
            duracao,
            status,
            descricao_da_tarefa,
            dificuldades_enxergadas,
            impacto_nas_outras
          } = campos;

          // Buscar o nome da equipe
          const equipe = await env.DB.prepare(`
    SELECT nome_equipe
    FROM tb_equipe
    WHERE usuario = ?
  `).bind(usuario).first();

          if (!equipe) {
            return new Response(JSON.stringify({
              success: false,
              error: "Equipe não encontrada."
            }), {
              status: 404,
              headers: {
                ...corsHeaders,
                "Content-Type": "application/json"
              }
            });
          }

          const planilha = await env.DB.prepare(`
    SELECT id_acompanhamento_projeto
    FROM tb_acompanhamento_projeto
    WHERE usuario = ?
  `).bind(equipe.nome_equipe).first();

          if (planilha) {

            console.log({
              tarefas,
              aluno_responsavel,
              professor_da_area,
              inicio_previsto,
              fim_previsto,
              inicio_realizado,
              fim_realizado,
              duracao,
              status,
              descricao_da_tarefa,
              dificuldades_enxergadas,
              impacto_nas_outras
            });

            await env.DB.prepare(`
      UPDATE tb_acompanhamento_projeto
      SET
        tarefas = ?,
        aluno_responsavel = ?,
        professor_da_area = ?,
        inicio_previsto = ?,
        fim_previsto = ?,
        inicio_realizado = ?,
        fim_realizado = ?,
        duracao = ?,
        status = ?,
        descricao_da_tarefa = ?,
        dificuldades_enxergadas = ?,
        impacto_nas_outras = ?
      WHERE usuario = ?
    `).bind(
              tarefas,
              aluno_responsavel,
              professor_da_area,
              inicio_previsto,
              fim_previsto,
              inicio_realizado,
              fim_realizado,
              duracao,
              status,
              descricao_da_tarefa,
              dificuldades_enxergadas,
              impacto_nas_outras,
              equipe.nome_equipe
            ).run();

          } else {

            await env.DB.prepare(`
      INSERT INTO tb_acompanhamento_projeto
      (
        tarefas,
        aluno_responsavel,
        professor_da_area,
        inicio_previsto,
        fim_previsto,
        inicio_realizado,
        fim_realizado,
        duracao,
        status,
        descricao_da_tarefa,
        dificuldades_enxergadas,
        impacto_nas_outras,
        usuario
      )
      VALUES
      (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    `).bind(
              tarefas,
              aluno_responsavel,
              professor_da_area,
              inicio_previsto,
              fim_previsto,
              inicio_realizado,
              fim_realizado,
              duracao,
              status,
              descricao_da_tarefa,
              dificuldades_enxergadas,
              impacto_nas_outras,
              equipe.nome_equipe
            ).run();

          }

          return new Response(JSON.stringify({
            success: true
          }), {
            status: 200,
            headers: {
              ...corsHeaders,
              "Content-Type": "application/json"
            }
          });

        }

        //===============TB_INFORMACOES_COMPLEMENTARES===============
        if (tipo === "complementares") {

          const {
            unidade_nome_comercial,
            coordenador_pedagogico,
            gestor,
            empresa,
            projeto,
            descricao
          } = campos;

          const equipe = await env.DB.prepare(`
    SELECT nome_equipe
    FROM tb_equipe
    WHERE usuario = ?
  `).bind(usuario).first();

          if (!equipe) {
            return new Response(JSON.stringify({
              success: false,
              error: "Equipe não encontrada."
            }), {
              status: 404,
              headers: {
                ...corsHeaders,
                "Content-Type": "application/json"
              }
            });
          }

          const complemento = await env.DB.prepare(`
    SELECT id_informacoes_complementares
    FROM tb_informacoes_complementares
    WHERE usuario = ?
  `).bind(equipe.nome_equipe).first();

          if (complemento) {

            await env.DB.prepare(`
      UPDATE tb_informacoes_complementares
      SET
        unidade_nome_comercial = ?,
        coordenador_pedagogico = ?,
        gestor = ?,
        empresa = ?,
        projeto = ?,
        descricao = ?
      WHERE usuario = ?
    `).bind(
              unidade_nome_comercial,
              coordenador_pedagogico,
              gestor,
              empresa,
              projeto,
              descricao,
              equipe.nome_equipe
            ).run();

          } else {

            await env.DB.prepare(`
      INSERT INTO tb_informacoes_complementares
      (
        unidade_nome_comercial,
        coordenador_pedagogico,
        gestor,
        empresa,
        projeto,
        descricao,
        usuario
      )
      VALUES
      (?, ?, ?, ?, ?, ?, ?)
    `).bind(
              unidade_nome_comercial,
              coordenador_pedagogico,
              gestor,
              empresa,
              projeto,
              descricao,
              equipe.nome_equipe
            ).run();

          }

          return new Response(JSON.stringify({
            success: true
          }), {
            status: 200,
            headers: {
              ...corsHeaders,
              "Content-Type": "application/json"
            }
          });

        }

        //===============TB_INFORMACOES_COMPLETUDE===============
        if (tipo === "completude") {

          const {
            qtd,
            equipe_unidade_empresa,
            responsavel_preenchimento,
            dados_equipe,
            conhecimentos,
            recursos_aplicados,
            canvas_preencher,
            pitch_escrito,
            pitch_video,
            cronograma,
            foto_equipe,
            fotos_etapa_projeto
          } = campos;

          const equipe = await env.DB.prepare(`
    SELECT nome_equipe
    FROM tb_equipe
    WHERE usuario = ?
  `).bind(usuario).first();

          if (!equipe) {
            return new Response(JSON.stringify({
              success: false,
              error: "Equipe não encontrada."
            }), {
              status: 404,
              headers: {
                ...corsHeaders,
                "Content-Type": "application/json"
              }
            });
          }

          const completude = await env.DB.prepare(`
    SELECT id_informacoes_completude
    FROM tb_informacoes_completude
    WHERE usuario = ?
  `).bind(equipe.nome_equipe).first();

          if (completude) {

            await env.DB.prepare(`
      UPDATE tb_informacoes_completude
      SET
        qtd = ?,
        equipe_unidade_empresa = ?,
        responsavel_preenchimento = ?,
        dados_equipe = ?,
        conhecimentos = ?,
        recursos_aplicados = ?,
        canvas_preencher = ?,
        pitch_escrito = ?,
        pitch_video = ?,
        cronograma = ?,
        foto_equipe = ?,
        fotos_etapa_projeto = ?
      WHERE usuario = ?
    `).bind(
              qtd || 0,
              equipe_unidade_empresa,
              responsavel_preenchimento,
              dados_equipe,
              conhecimentos,
              recursos_aplicados,
              canvas_preencher,
              pitch_escrito,
              pitch_video,
              cronograma,
              foto_equipe,
              fotos_etapa_projeto,
              equipe.nome_equipe
            ).run();

          } else {

            await env.DB.prepare(`
      INSERT INTO tb_informacoes_completude
      (
        qtd,
        equipe_unidade_empresa,
        responsavel_preenchimento,
        dados_equipe,
        conhecimentos,
        recursos_aplicados,
        canvas_preencher,
        pitch_escrito,
        pitch_video,
        cronograma,
        foto_equipe,
        fotos_etapa_projeto,
        usuario
      )
      VALUES
      (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    `).bind(
              qtd || 0,
              equipe_unidade_empresa,
              responsavel_preenchimento,
              dados_equipe,
              conhecimentos,
              recursos_aplicados,
              canvas_preencher,
              pitch_escrito,
              pitch_video,
              cronograma,
              foto_equipe,
              fotos_etapa_projeto,
              equipe.nome_equipe
            ).run();

          }

          return new Response(JSON.stringify({
            success: true
          }), {
            status: 200,
            headers: {
              ...corsHeaders,
              "Content-Type": "application/json"
            }
          });

        }

        //===============TB_RELATORIO===============
        if (tipo === "relatorio") {

          const {
            nome_empresa,
            e_mail_empresa,
            setor_empresa,
            descricao,
            roteiro_pitch,
            integrante1,
            integrante2,
            integrante3,
            integrante4,
            integrante5,
            orientador,
            coorientador,
            nome_projeto,
            nome_equipe,
            area_atuacao_projeto,
            area_atuacao_curso,
            unidade_senai,
            gestor,
            ferramenta_ia,
            link_acesso,
            licenca,
            etapa_de_usu,
            prompt,
            motivo_usu,
            ferramentas_projeto,
            equipamentos_projeto,
            quant_compra,
            quant_utilizada,
            preco,
            fornecedor,
            modo_obtencao,
            processamento,
            alternativa_de_uso,
            quant_utilizada_2,
            forma_pagamento,
            preco_total
          } = campos;

          const relatorio = await env.DB.prepare(`
    SELECT id_relatorio
    FROM tb_relatorio
    WHERE usuario = ?
  `).bind(usuario).first();

          if (relatorio) {

            await env.DB.prepare(`
      UPDATE tb_relatorio
      SET
        nome_empresa = ?,
        e_mail_empresa = ?,
        setor_empresa = ?,
        descricao = ?,
        roteiro_pitch = ?,
        integrante1 = ?,
        integrante2 = ?,
        integrante3 = ?,
        integrante4 = ?,
        integrante5 = ?,
        orientador = ?,
        coorientador = ?,
        nome_projeto = ?,
        nome_equipe = ?,
        area_atuacao_projeto = ?,
        area_atuacao_curso = ?,
        unidade_senai = ?,
        gestor = ?,
        ferramenta_ia = ?,
        link_acesso = ?,
        licenca = ?,
        etapa_de_usu = ?,
        prompt = ?,
        motivo_usu = ?,
        ferramentas_projeto = ?,
        equipamentos_projeto = ?,
        quant_compra = ?,
        quant_utilizada = ?,
        preco = ?,
        fornecedor = ?,
        modo_obtencao = ?,
        processamento = ?,
        alternativa_de_uso = ?,
        quant_utilizada_2 = ?,
        forma_pagamento = ?,
        preco_total = ?
      WHERE usuario = ?
    `).bind(
              nome_empresa,
              e_mail_empresa,
              setor_empresa,
              descricao,
              roteiro_pitch,
              integrante1,
              integrante2,
              integrante3,
              integrante4,
              integrante5,
              orientador,
              coorientador,
              nome_projeto,
              nome_equipe,
              area_atuacao_projeto,
              area_atuacao_curso,
              unidade_senai,
              gestor,
              ferramenta_ia,
              link_acesso,
              licenca,
              etapa_de_usu,
              prompt,
              motivo_usu,
              ferramentas_projeto,
              equipamentos_projeto,
              quant_compra,
              quant_utilizada,
              preco,
              fornecedor,
              modo_obtencao,
              processamento,
              alternativa_de_uso,
              quant_utilizada_2,
              forma_pagamento,
              preco_total,
              usuario
            ).run();

          } else {

            await env.DB.prepare(`
      INSERT INTO tb_relatorio
      (
        nome_empresa,
        e_mail_empresa,
        setor_empresa,
        descricao,
        roteiro_pitch,
        integrante1,
        integrante2,
        integrante3,
        integrante4,
        integrante5,
        orientador,
        coorientador,
        nome_projeto,
        nome_equipe,
        area_atuacao_projeto,
        area_atuacao_curso,
        unidade_senai,
        gestor,
        ferramenta_ia,
        link_acesso,
        licenca,
        etapa_de_usu,
        prompt,
        motivo_usu,
        ferramentas_projeto,
        equipamentos_projeto,
        quant_compra,
        quant_utilizada,
        preco,
        fornecedor,
        modo_obtencao,
        processamento,
        alternativa_de_uso,
        quant_utilizada_2,
        forma_pagamento,
        preco_total,
        usuario
      )
      VALUES
      (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    `).bind(
              nome_empresa,
              e_mail_empresa,
              setor_empresa,
              descricao,
              roteiro_pitch,
              integrante1,
              integrante2,
              integrante3,
              integrante4,
              integrante5,
              orientador,
              coorientador,
              nome_projeto,
              nome_equipe,
              area_atuacao_projeto,
              area_atuacao_curso,
              unidade_senai,
              gestor,
              ferramenta_ia,
              link_acesso,
              licenca,
              etapa_de_usu,
              prompt,
              motivo_usu,
              ferramentas_projeto,
              equipamentos_projeto,
              quant_compra,
              quant_utilizada,
              preco,
              fornecedor,
              modo_obtencao,
              processamento,
              alternativa_de_uso,
              quant_utilizada_2,
              forma_pagamento,
              preco_total,
              usuario
            ).run();

          }

          return new Response(JSON.stringify({
            success: true
          }), {
            status: 200,
            headers: {
              ...corsHeaders,
              "Content-Type": "application/json"
            }
          });

        }

        if (tipo === "curriculo") {

          const { results } = await env.DB.prepare(`
    SELECT id_aluno
    FROM tb_curriculo_alunos
    WHERE usuario = ?
  `).bind(usuario).all();

          const existe = results && results.length > 0;

          if (existe) {

            await env.DB.prepare(`
      UPDATE tb_curriculo_alunos SET
        nome = ?,
        data_nascimento = ?,
        empresa_vinculado = ?,
        projeto = ?,
        telefone = ?,
        email = ?,
        habilidades = ?,
        fez_projeto = ?,
        cidade = ?,
        motivo_projeto = ?,
        aprendo_mais = ?,
        prefiro_trabalhar = ?
      WHERE usuario = ?
    `).bind(
              campos.nome,
              campos.data_nascimento,
              campos.empresa_vinculado,
              campos.projeto,
              campos.telefone,
              campos.email,
              campos.habilidades,
              campos.fez_projeto,
              campos.cidade,
              campos.motivo_projeto,
              campos.aprendo_mais,
              campos.prefiro_trabalhar,
              usuario
            ).run();

          } else {

            await env.DB.prepare(`
      INSERT INTO tb_curriculo_alunos (
        nome,
        data_nascimento,
        empresa_vinculado,
        projeto,
        telefone,
        email,
        habilidades,
        fez_projeto,
        cidade,
        motivo_projeto,
        aprendo_mais,
        prefiro_trabalhar,
        usuario
      ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    `).bind(
              campos.nome,
              campos.data_nascimento,
              campos.empresa_vinculado,
              campos.projeto,
              campos.telefone,
              campos.email,
              campos.habilidades,
              campos.fez_projeto,
              campos.cidade,
              campos.motivo_projeto,
              campos.aprendo_mais,
              campos.prefiro_trabalhar,
              usuario
            ).run();

          }

          return new Response(JSON.stringify({
            success: true,
            message: "Currículo salvo com sucesso."
          }), {
            headers: {
              ...corsHeaders,
              "Content-Type": "application/json"
            }
          });
        }

      }

      if (path === "/buscar-dados") {

        if (!usuario || !tipo) {

          return new Response(JSON.stringify({
            success: false,
            error: "Dados inválidos."
          }), {
            status: 400,
            headers: {
              ...corsHeaders,
              "Content-Type": "application/json"
            }
          });

        }

        // ================= TB_EQUIPE =================

        if (tipo === "equipe") {

          const equipe = await env.DB.prepare(`
            SELECT
                nome_equipe,
                nome_projeto,
                email,
                area_atuacao_curso,
                area_atuacao_projeto,
                nome_orientador,
                nome_coorientador,
                nome_integrante,
                nome_integrante2,
                nome_integrante3,
                nome_integrante4,
                nome_integrante5
            FROM tb_equipe
            WHERE usuario = ?
        `).bind(usuario).first();

          if (!equipe) {

            return new Response(JSON.stringify({
              success: false,
              error: "Nenhum registro encontrado."
            }), {
              status: 404,
              headers: {
                ...corsHeaders,
                "Content-Type": "application/json"
              }
            });

          }

          return new Response(JSON.stringify({
            success: true,
            dados: equipe
          }), {
            status: 200,
            headers: {
              ...corsHeaders,
              "Content-Type": "application/json"
            }
          });

        }
        //===============TB_CONHECIMENTOS===============
        if (tipo === "conhecimentos") {

          // Buscar o nome da equipe a partir do e-mail
          const equipe = await env.DB.prepare(`
        SELECT nome_equipe
        FROM tb_equipe
        WHERE usuario = ?
    `).bind(usuario).first();

          if (!equipe) {
            return new Response(JSON.stringify({
              success: false,
              error: "Equipe não encontrada."
            }), {
              status: 404,
              headers: {
                ...corsHeaders,
                "Content-Type": "application/json"
              }
            });
          }

          const usuarioEquipe = equipe.nome_equipe;

          // Buscar os conhecimentos usando o nome da equipe
          const conhecimento = await env.DB.prepare(`
        SELECT
            plano_curso,
            conhecimentos_aplicados,
            capacidades_aplicadas
        FROM tb_conhecimentos
        WHERE usuario = ?
    `).bind(usuarioEquipe).first();

          return new Response(JSON.stringify({
            success: true,
            dados: conhecimento || {}
          }), {
            status: 200,
            headers: {
              ...corsHeaders,
              "Content-Type": "application/json"
            }
          });

        }

        //===============TB_RECURSOS_APLICADOS===============
        if (tipo === "recursos") {

          // Buscar o nome da equipe a partir do e-mail do usuário
          const equipe = await env.DB.prepare(`
        SELECT nome_equipe
        FROM tb_equipe
        WHERE usuario = ?
    `).bind(usuario).first();

          if (!equipe) {
            return new Response(JSON.stringify({
              success: false,
              error: "Equipe não encontrada."
            }), {
              status: 404,
              headers: {
                ...corsHeaders,
                "Content-Type": "application/json"
              }
            });
          }

          // Buscar os recursos da equipe
          const recursos = await env.DB.prepare(`
        SELECT
            ferramentas,
            equipamentos,
            descricao_produto,
            quant_comprada,
            quant_utilizada,
            preco_estimado,
            uni_medida,
            fornecedor_principal,
            modo_obtencao,
            disponibilidade,
            pagamento,
            alternativas_consideradas,
            preco_total
        FROM tb_recursos_aplicados
        WHERE usuario = ?
    `).bind(equipe.nome_equipe).first();

          return new Response(JSON.stringify({
            success: true,
            dados: recursos || {}
          }), {
            status: 200,
            headers: {
              ...corsHeaders,
              "Content-Type": "application/json"
            }
          });
        }

        //===============TB_CRONOGRAMA===============
        if (tipo === "cronograma") {

          // Buscar o nome da equipe pelo e-mail do usuário
          const equipe = await env.DB.prepare(`
    SELECT nome_equipe
    FROM tb_equipe
    WHERE usuario = ?
  `).bind(usuario).first();

          if (!equipe) {
            return new Response(JSON.stringify({
              success: false,
              error: "Equipe não encontrada."
            }), {
              status: 404,
              headers: {
                ...corsHeaders,
                "Content-Type": "application/json"
              }
            });
          }

          const cronograma = await env.DB.prepare(`
    SELECT
      processo,
      etapas,
      responsavel,
      data_inicio,
      data_final,
      observacoes
    FROM tb_cronograma
    WHERE usuario = ?
  `).bind(equipe.nome_equipe).first();

          return new Response(JSON.stringify({
            success: true,
            dados: cronograma || {}
          }), {
            status: 200,
            headers: {
              ...corsHeaders,
              "Content-Type": "application/json"
            }
          });

        }

        //===============TB_CANVA===============
        if (tipo === "canva") {

          // Buscar o nome da equipe pelo e-mail
          const equipe = await env.DB.prepare(`
    SELECT nome_equipe
    FROM tb_equipe
    WHERE usuario = ?
  `).bind(usuario).first();

          if (!equipe) {
            return new Response(JSON.stringify({
              success: false,
              error: "Equipe não encontrada."
            }), {
              status: 404,
              headers: {
                ...corsHeaders,
                "Content-Type": "application/json"
              }
            });
          }

          const canva = await env.DB.prepare(`
    SELECT
      atividades_chaves,
      proposta_chave,
      relacionamentos_clientes,
      segmentos_clientes,
      recursos_chaves,
      canais,
      estrutura_custos,
      fluxo_receita,
      parceiros_chaves
    FROM tb_canva
    WHERE usuario = ?
  `).bind(equipe.nome_equipe).first();

          return new Response(JSON.stringify({
            success: true,
            dados: canva || {}
          }), {
            status: 200,
            headers: {
              ...corsHeaders,
              "Content-Type": "application/json"
            }
          });

        }


        //===============TB_EMPRESAS_FORMULARIO===============
        if (tipo === "empresa") {

          // Busca o id da equipe
          const equipe = await env.DB.prepare(`
    SELECT id_equipe
    FROM tb_equipe
    WHERE usuario = ?
  `).bind(usuario).first();

          if (!equipe) {
            return new Response(JSON.stringify({
              success: false,
              error: "Equipe não encontrada."
            }), {
              status: 404,
              headers: {
                ...corsHeaders,
                "Content-Type": "application/json"
              }
            });
          }

          const empresa = await env.DB.prepare(`
    SELECT
      nome_empresa,
      cnpj,
      regiao,
      telefone_contato,
      email_contato,
      objetivos,
      problema_projeto
    FROM tb_empresas_formulario
    WHERE id_empresa_formulario = ?
  `).bind(equipe.id_equipe).first();

          return new Response(JSON.stringify({
            success: true,
            dados: empresa || {}
          }), {
            status: 200,
            headers: {
              ...corsHeaders,
              "Content-Type": "application/json"
            }
          });

        }

        //===============TB_PITCH===============
        if (tipo === "pitch") {

          // Buscar o nome da equipe pelo e-mail
          const equipe = await env.DB.prepare(`
    SELECT nome_equipe
    FROM tb_equipe
    WHERE usuario = ?
  `).bind(usuario).first();

          if (!equipe) {
            return new Response(JSON.stringify({
              success: false,
              error: "Equipe não encontrada."
            }), {
              status: 404,
              headers: {
                ...corsHeaders,
                "Content-Type": "application/json"
              }
            });
          }

          const pitch = await env.DB.prepare(`
    SELECT
      roteiro
    FROM tb_pitch
    WHERE usuario = ?
  `).bind(equipe.nome_equipe).first();

          return new Response(JSON.stringify({
            success: true,
            dados: pitch || {}
          }), {
            status: 200,
            headers: {
              ...corsHeaders,
              "Content-Type": "application/json"
            }
          });

        } //===============TB_USO_IA===============
        if (tipo === "ia") {

          const equipe = await env.DB.prepare(`
    SELECT nome_equipe
    FROM tb_equipe
    WHERE usuario = ?
  `).bind(usuario).first();

          if (!equipe) {
            return new Response(JSON.stringify({
              success: false,
              error: "Equipe não encontrada."
            }), {
              status: 404,
              headers: {
                ...corsHeaders,
                "Content-Type": "application/json"
              }
            });
          }

          const ia = await env.DB.prepare(`
    SELECT
      nome_ferramenta,
      link_acesso,
      tipo_licenca,
      etapa_uso,
      criacao_prompt,
      descricao_uso
    FROM tb_uso_ia
    WHERE usuario = ?
  `).bind(equipe.nome_equipe).first();

          return new Response(JSON.stringify({
            success: true,
            dados: ia || {}
          }), {
            status: 200,
            headers: {
              ...corsHeaders,
              "Content-Type": "application/json"
            }
          });

        }

        //===============TB_ACOMPANHAMENTO_PROJETO===============
        if (tipo === "planilha") {

          const equipe = await env.DB.prepare(`
    SELECT nome_equipe
    FROM tb_equipe
    WHERE usuario = ?
  `).bind(usuario).first();

          if (!equipe) {
            return new Response(JSON.stringify({
              success: false,
              error: "Equipe não encontrada."
            }), {
              status: 404,
              headers: {
                ...corsHeaders,
                "Content-Type": "application/json"
              }
            });
          }

          const planilha = await env.DB.prepare(`
    SELECT
      tarefas,
      aluno_responsavel,
      professor_da_area,
      inicio_previsto,
      fim_previsto,
      inicio_realizado,
      fim_realizado,
      duracao,
      status,
      descricao_da_tarefa,
      dificuldades_enxergadas,
      impacto_nas_outras
    FROM tb_acompanhamento_projeto
    WHERE usuario = ?
  `).bind(equipe.nome_equipe).first();

          return new Response(JSON.stringify({
            success: true,
            dados: planilha || {}
          }), {
            status: 200,
            headers: {
              ...corsHeaders,
              "Content-Type": "application/json"
            }
          });

        }

        if (tipo === "complementares") {

          const equipe = await env.DB.prepare(`
    SELECT nome_equipe
    FROM tb_equipe
    WHERE usuario = ?
  `).bind(usuario).first();

          if (!equipe) {
            return new Response(JSON.stringify({
              success: false,
              error: "Equipe não encontrada."
            }), {
              status: 404,
              headers: {
                ...corsHeaders,
                "Content-Type": "application/json"
              }
            });
          }

          const dados = await env.DB.prepare(`
    SELECT
      unidade_nome_comercial,
      coordenador_pedagogico,
      gestor,
      empresa,
      projeto,
      descricao
    FROM tb_informacoes_complementares
    WHERE usuario = ?
  `).bind(equipe.nome_equipe).first();

          return new Response(JSON.stringify({
            success: true,
            dados: dados || {}
          }), {
            status: 200,
            headers: {
              ...corsHeaders,
              "Content-Type": "application/json"
            }
          });

        }

        //===============TB_INFORMACOES_COMPLETUDE===============
        if (tipo === "completude") {

          const equipe = await env.DB.prepare(`
    SELECT nome_equipe
    FROM tb_equipe
    WHERE usuario = ?
  `).bind(usuario).first();

          if (!equipe) {
            return new Response(JSON.stringify({
              success: false,
              error: "Equipe não encontrada."
            }), {
              status: 404,
              headers: {
                ...corsHeaders,
                "Content-Type": "application/json"
              }
            });
          }

          const dados = await env.DB.prepare(`
    SELECT
      qtd,
      equipe_unidade_empresa,
      responsavel_preenchimento,
      dados_equipe,
      conhecimentos,
      recursos_aplicados,
      canvas_preencher,
      pitch_escrito,
      pitch_video,
      cronograma,
      foto_equipe,
      fotos_etapa_projeto
    FROM tb_informacoes_completude
    WHERE usuario = ?
  `).bind(equipe.nome_equipe).first();

          return new Response(JSON.stringify({
            success: true,
            dados: dados || {}
          }), {
            status: 200,
            headers: {
              ...corsHeaders,
              "Content-Type": "application/json"
            }
          });

        }

        //===============TB_RELATORIO===============
        if (tipo === "relatorio") {

          const relatorio = await env.DB.prepare(`
    SELECT
      nome_empresa,
      e_mail_empresa,
      setor_empresa,
      descricao,
      roteiro_pitch,
      integrante1,
      integrante2,
      integrante3,
      integrante4,
      integrante5,
      orientador,
      coorientador,
      nome_projeto,
      nome_equipe,
      area_atuacao_projeto,
      area_atuacao_curso,
      unidade_senai,
      gestor,
      ferramenta_ia,
      link_acesso,
      licenca,
      etapa_de_usu,
      prompt,
      motivo_usu,
      ferramentas_projeto,
      equipamentos_projeto,
      quant_compra,
      quant_utilizada,
      preco,
      fornecedor,
      modo_obtencao,
      processamento,
      alternativa_de_uso,
      quant_utilizada_2,
      forma_pagamento,
      preco_total
    FROM tb_relatorio
    WHERE usuario = ?
  `).bind(usuario).first();

          return new Response(JSON.stringify({
            success: true,
            dados: relatorio || {}
          }), {
            status: 200,
            headers: {
              ...corsHeaders,
              "Content-Type": "application/json"
            }
          });

        }

        if (tipo === "curriculo") {

          const { results } = await env.DB.prepare(`
    SELECT *
    FROM tb_curriculo_alunos
    WHERE usuario = ?
    LIMIT 1
  `).bind(usuario).all();

          if (!results || results.length === 0) {
            return new Response(JSON.stringify({
              success: false,
              message: "Currículo não encontrado."
            }), {
              headers: {
                ...corsHeaders,
                "Content-Type": "application/json"
              }
            });
          }

          return new Response(JSON.stringify({
            success: true,
            dados: results[0]
          }), {
            headers: {
              ...corsHeaders,
              "Content-Type": "application/json"
            }
          });
        }

        return new Response(JSON.stringify({
          success: false,
          error: "Tipo de formulário inválido."
        }), {
          status: 400,
          headers: {
            ...corsHeaders,
            "Content-Type": "application/json"
          }
        });

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


if (method === "POST") {
  if (path === "/salvar-curriculo") {
    try {
      const body = await request.json();
      const { email } = body;
      if (!email) return new Response(JSON.stringify({ success: false, message: 'Email obrigatório' }), { status: 400 });

      // Lógica de salvar no DB (tb_curriculo_alunos)
      const curriculoExistente = await env.DB.prepare("SELECT id_aluno FROM tb_curriculo_alunos WHERE email = ?").bind(email).first();
      if (curriculoExistente) {
          await env.DB.prepare("UPDATE tb_curriculo_alunos SET nome=?, data_nacimento=?, telefone=?, cidade=?, habilidades=?, fez_projeto=?, projeto=?, empresa_vinculado=?, motivo_projeto=?, aprendo_mais=?, prefiro_trabalhar=? WHERE email=?")
          .bind(body.nome, body.data_nascimento, body.telefone, body.cidade, body.habilidades, body.fez_projeto, body.projeto, body.empresa_vinculado, body.motivo_projeto, body.aprendo_mais, body.prefiro_trabalhar, email).run();
      } else {
          await env.DB.prepare("INSERT INTO tb_curriculo_alunos (nome, email, data_nacimento, telefone, cidade, habilidades, fez_projeto, projeto, empresa_vinculado, motivo_projeto, aprendo_mais, prefiro_trabalhar, cpf) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")
          .bind(body.nome, email, body.data_nascimento, body.telefone, body.cidade, body.habilidades, body.fez_projeto, body.projeto, body.empresa_vinculado, body.motivo_projeto, body.aprendo_mais, body.prefiro_trabalhar, "").run();
      }
      return new Response(JSON.stringify({ success: true, message: 'Currículo salvo!' }), { headers: { ...corsHeaders, "Content-Type": "application/json" } });
    } catch (e) {
      return new Response(JSON.stringify({ success: false, error: e.message }), { status: 500 });
    }
  }
}



    } catch (error) {

      return new Response(JSON.stringify({ success: false, error: "Erro no banco: " + error.message }), { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } });

    }



    return new Response("Rota não encontrada", { status: 404, headers: corsHeaders });

  }
};