async function handleUploadVideo(request, env, corsHeaders) {
  try {
    const formData = await request.formData();
    const usuario = formData.get('usuario');
    const videoFile = formData.get('video');

    if (!usuario || !videoFile) {
      return new Response(JSON.stringify({ success: false, error: 'Dados incompletos' }), {
        status: 400, headers: { ...corsHeaders, 'Content-Type': 'application/json' }
      });
    }

    const fileName = `pitches/${usuario.replace(/[^a-zA-Z0-9]/g, '_')}_${Date.now()}.mp4`;
    const R2_PUBLIC_URL_VIDEOS = "https://pub-8b39c2fa88234341ac68682a11d82f77.r2.dev";
    const videoUrl = `${R2_PUBLIC_URL_VIDEOS}/${fileName}`;

    await env.BUCKET_VIDEOS.put(fileName, videoFile.stream(), {
      httpMetadata: { contentType: 'video/mp4' }
    });

    if (env.DB) {
      const equipeRecord = await env.DB.prepare("SELECT id_equipe, nome_equipe, usuario FROM tb_equipe WHERE usuario = ? OR email = ? OR nome_equipe = ?").bind(usuario, usuario, usuario).first();
      if (equipeRecord) {
        const identifier = (equipeRecord.nome_equipe && equipeRecord.nome_equipe.trim() !== "") ? equipeRecord.nome_equipe : (equipeRecord.usuario || usuario);

        const completudeRecord = await env.DB.prepare("SELECT id_informacoes_completude FROM tb_informacoes_completude WHERE usuario = ? OR usuario = ? OR usuario = ?").bind(equipeRecord.nome_equipe, equipeRecord.usuario, usuario).first();
        if (completudeRecord) {
          await env.DB.prepare("UPDATE tb_informacoes_completude SET pitch_video = ? WHERE id_informacoes_completude = ?").bind("Concluido", completudeRecord.id_informacoes_completude).run();
        }

        const pitchRecord = await env.DB.prepare("SELECT id_pitch FROM tb_pitch WHERE usuario = ? OR usuario = ? OR usuario = ?").bind(equipeRecord.nome_equipe, equipeRecord.usuario, usuario).first();
        if (pitchRecord) {
          await env.DB.prepare("UPDATE tb_pitch SET video_url = ? WHERE id_pitch = ?").bind(videoUrl, pitchRecord.id_pitch).run();
        } else {
          await env.DB.prepare("INSERT INTO tb_pitch (id_pitch, usuario, video_url, roteiro) VALUES (?, ?, ?, ?)").bind(equipeRecord.id_equipe, identifier, videoUrl, "").run();
        }
      }
    }

    return new Response(JSON.stringify({ success: true, message: 'Vídeo salvo com sucesso!', path: fileName, video_url: videoUrl }), { status: 200, headers: { ...corsHeaders, 'Content-Type': 'application/json' } });
  } catch (err) {
    return new Response(JSON.stringify({ success: false, error: err.message }), { status: 500, headers: { ...corsHeaders, 'Content-Type': 'application/json' } });
  }
}

async function uploadBase64ToR2(base64String, identificador, env) {
  if (!base64String || typeof base64String !== 'string' || base64String.startsWith('http') || base64String.length < 100) {
    return base64String;
  }
  try {
    let cleanBase64 = base64String;
    if (base64String.includes('base64,')) { cleanBase64 = base64String.split('base64,')[1]; }
    cleanBase64 = cleanBase64.replace(/\s/g, "");
    const binaryString = atob(cleanBase64);
    const bytes = new Uint8Array(binaryString.length);
    for (let i = 0; i < binaryString.length; i++) { bytes[i] = binaryString.charCodeAt(i); }
    const safeName = (identificador || 'anon').replace(/[^a-zA-Z0-9]/g, '_');
    const fileName = `avatares/img_${safeName}_${Date.now()}.jpg`;
    const R2_PUBLIC_URL = "https://pub-8b39c2fa88234341ac68682a11d82f77.r2.dev";
    const bucket = env.BUCKET_AVATARES || env.BUCKET_VIDEOS;
    if (bucket) {
      await bucket.put(fileName, bytes, { httpMetadata: { contentType: 'image/jpeg' } });
      return `${R2_PUBLIC_URL}/${fileName}`;
    }
    return base64String.length > 300 ? null : base64String;
  } catch (error) {
    console.error("Erro ao converter/salvar a imagem no R2:", error);
    return base64String.length > 300 ? null : base64String;
  }
}

export default {
  async fetch(request, env) {
    const url = new URL(request.url);
    const path = url.pathname;
    const method = request.method;
    const corsHeaders = { "Access-Control-Allow-Origin": "*", "Access-Control-Allow-Methods": "POST, GET, OPTIONS", "Access-Control-Allow-Headers": "Content-Type" };

    if (method === "OPTIONS") return new Response(null, { headers: corsHeaders });

    if (method === "GET") {
      if (path === "/listar-empresas") {
        try {
          const { results } = await env.DB.prepare(`SELECT e.id_empresa, e.nome_empresa, e.cnpj, e.telefone_contato, e.email_contato, e.endereco, COALESCE(c.foto_perfil, e.foto_perfil) as foto_perfil, e.descricao, e.setor FROM tb_empresas e LEFT JOIN tb_cadastros c ON (e.email_contato = c.email OR e.usuario = c.nome_usuarios)`).all();
          return new Response(JSON.stringify({ success: true, data: results }), { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } });
        } catch (erro) { return new Response(JSON.stringify({ success: false, error: erro.message }), { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } }); }
      }
      if (path === "/listar-projetos") {
        try {
          const { results: projetos } = await env.DB.prepare(`SELECT e.nome_projeto, e.nome_equipe, e.nome_integrante, e.nome_integrante2, e.nome_integrante3, e.nome_integrante4, e.nome_integrante5, e.nome_orientador, e.nome_coorientador, e.usuario, a.status, a.tarefas, a.dificuldades_enxergadas, a.comentario_empresa, c.proposta_chave, c.segmentos_clientes, c.atividades_chaves, c.recursos_chaves, c.relacionamentos_clientes, c.canais, c.estrutura_custos, c.fluxo_receita, c.parceiros_chaves, ef.nome_empresa AS ef_nome, ef.email_contato AS ef_email, p.video_url FROM tb_equipe e LEFT JOIN tb_acompanhamento_projeto a ON (e.usuario = a.usuario OR e.nome_equipe = a.usuario) LEFT JOIN tb_canva c ON (e.usuario = c.usuario OR e.nome_equipe = c.usuario) LEFT JOIN tb_informacoes_complementares ic ON (e.usuario = ic.usuario OR e.nome_equipe = ic.usuario) LEFT JOIN tb_empresas_formulario ef ON (e.id_equipe = ef.id_empresa_formulario) LEFT JOIN tb_pitch p ON (e.nome_equipe = p.usuario OR e.usuario = p.usuario)`).all();
          const { results: empresas } = await env.DB.prepare(`SELECT id_empresa, nome_empresa, email_contato FROM tb_empresas`).all();
          const { results: cadastros } = await env.DB.prepare(`SELECT nome_usuarios, email FROM tb_cadastros`).all();
          const userMap = new Map();
          cadastros.forEach(u => { if (u.email) userMap.set(u.email.toLowerCase(), u.nome_usuarios); });
          const translate = (val) => { if (!val) return val; return userMap.get(val.toLowerCase()) || val; };
          const projetosTraduzidos = projetos.map(projeto => {
            projeto.nome_orientador = translate(projeto.nome_orientador);
            projeto.nome_coorientador = translate(projeto.nome_coorientador);
            const listaRaw = [projeto.nome_integrante, projeto.nome_integrante2, projeto.nome_integrante3, projeto.nome_integrante4, projeto.nome_integrante5];
            const listaTraduzida = listaRaw.map(n => translate(n)).filter(n => n && n.trim() !== "" && n.toLowerCase() !== "null");
            projeto.nome_integrante = listaTraduzida.join(", ") || "Sem integrantes";

            // Source of truth: tb_empresas_formulario
            projeto.empresa_vinculada = projeto.ef_nome || "";
            projeto.empresa_vinculada_email = (projeto.ef_email || "").trim().toLowerCase();

            // Fallback: if email is missing but we have a name, try to resolve from tb_empresas
            if (!projeto.empresa_vinculada_email && projeto.empresa_vinculada) {
              const searchKey = String(projeto.empresa_vinculada).trim().toLowerCase();
              const emp = empresas.find(e =>
                (e.nome_empresa && String(e.nome_empresa).trim().toLowerCase() === searchKey) ||
                (e.email_contato && String(e.email_contato).trim().toLowerCase() === searchKey)
              );
              if (emp) {
                projeto.empresa_vinculada_email = (emp.email_contato || "").trim().toLowerCase();
              }
            }

            console.log(`[DEBUG] Projeto "${projeto.nome_projeto}" - Empresa: ${projeto.empresa_vinculada} (${projeto.empresa_vinculada_email})`);
            return projeto;
          });
          return new Response(JSON.stringify({ success: true, data: projetosTraduzidos }), { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } });
        } catch (erro) { return new Response(JSON.stringify({ success: false, error: erro.message }), { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } }); }
      }
      if (path === "/listar-documentos") {
        try {
          const usuario = new URL(request.url).searchParams.get('usuario');
          if (!usuario) return new Response(JSON.stringify({ success: false, error: 'Usuário não fornecido' }), { status: 400, headers: { ...corsHeaders, 'Content-Type': 'application/json' } });

          // Crucial Fix: Flexible lookup (user identifier OR team name)
          // Documents can be linked to the user email/ID or to the resolved Team Name
          const equipe = await env.DB.prepare("SELECT nome_equipe FROM tb_equipe WHERE usuario = ? OR email = ? OR nome_equipe = ?").bind(usuario, usuario, usuario).first();
          const resolvedTeamName = equipe?.nome_equipe || "";

          const { results } = await env.DB.prepare("SELECT * FROM tb_documentos WHERE usuario_vinculado = ? OR usuario_vinculado = ? ORDER BY data_geracao DESC").bind(usuario, resolvedTeamName).all();
          return new Response(JSON.stringify({ success: true, data: results }), { status: 200, headers: { ...corsHeaders, 'Content-Type': 'application/json' } });
        } catch (err) { return new Response(JSON.stringify({ success: false, error: err.message }), { status: 500, headers: { ...corsHeaders, 'Content-Type': 'application/json' } }); }
      }
    }

    if (method === "POST") {
      if (path === "/upload-video") return await handleUploadVideo(request, env, corsHeaders);
      if (path === "/upload-documento") {
        try {
          const fd = await request.formData();
          const usuario = fd.get('usuario');
          const tipo = fd.get('tipo');
          const nome = fd.get('nome');
          const file = fd.get('file');
          if (!usuario || !file || !tipo) return new Response(JSON.stringify({ success: false, error: 'Dados incompletos' }), { status: 400, headers: { ...corsHeaders, 'Content-Type': 'application/json' } });
          const fileName = `documentos/${tipo.toLowerCase()}_${usuario.replace(/[^a-zA-Z0-9]/g, '_')}_${Date.now()}.pdf`;
          const R2_PUBLIC_URL = "https://pub-8b39c2fa88234341ac68682a11d82f77.r2.dev";
          const fileUrl = `${R2_PUBLIC_URL}/${fileName}`;
          await env.BUCKET_VIDEOS.put(fileName, file.stream(), { httpMetadata: { contentType: 'application/pdf' } });
          const equipe = await env.DB.prepare("SELECT nome_equipe, usuario FROM tb_equipe WHERE usuario = ? OR email = ? OR nome_equipe = ?").bind(usuario, usuario, usuario).first();
          const identifier = (equipe?.nome_equipe && equipe.nome_equipe.trim() !== "") ? equipe.nome_equipe : (equipe?.usuario || usuario);
          await env.DB.prepare(`INSERT INTO tb_documentos (nome_documento, tipo_documento, url_documento, usuario_vinculado) VALUES (?, ?, ?, ?)`).bind(nome || fileName, tipo, fileUrl, identifier).run();
          return new Response(JSON.stringify({ success: true, url: fileUrl }), { status: 200, headers: { ...corsHeaders, 'Content-Type': 'application/json' } });
        } catch (err) { return new Response(JSON.stringify({ success: false, error: err.message }), { status: 500, headers: { ...corsHeaders, 'Content-Type': 'application/json' } }); }
      }

      const body = await request.json();
      const { tipo, usuario, campos } = body;

      if (path === "/salvar-dados") {
        if (!usuario || !tipo) return new Response(JSON.stringify({ success: false, error: "Dados inválidos." }), { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } });

        try {
          const equipe = await env.DB.prepare("SELECT * FROM tb_equipe WHERE usuario = ? OR email = ? OR nome_equipe = ?").bind(usuario, usuario, usuario).first();
          if (!equipe && tipo !== "equipe" && tipo !== "curriculo") return new Response(JSON.stringify({ success: false, error: "Equipe não encontrada. Salve os dados da equipe primeiro." }), { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } });

          const { nome_equipe: nome_campos } = campos || {};
          const identifier = (nome_campos && nome_campos.trim() !== "") ? nome_campos : ((equipe?.nome_equipe && equipe.nome_equipe.trim() !== "") ? equipe.nome_equipe : (equipe?.usuario || usuario));

          if (tipo === "equipe") {
            try {
              const { nome_equipe, nome_projeto, email, area_atuacao_curso, area_atuacao_projeto, nome_orientador, nome_coorientador, nome_integrante, nome_integrante2, nome_integrante3, nome_integrante4, nome_integrante5 } = campos;
              if (equipe) {
                await env.DB.prepare(`UPDATE tb_equipe SET nome_equipe = ?, nome_projeto = ?, email = ?, area_atuacao_curso = ?, area_atuacao_projeto = ?, nome_orientador = ?, nome_coorientador = ?, nome_integrante = ?, nome_integrante2 = ?, nome_integrante3 = ?, nome_integrante4 = ?, nome_integrante5 = ? WHERE id_equipe = ?`).bind(nome_equipe, nome_projeto, email, area_atuacao_curso, area_atuacao_projeto, nome_orientador, nome_coorientador, nome_integrante, nome_integrante2, nome_integrante3, nome_integrante4, nome_integrante5, equipe.id_equipe).run();
              } else {
                await env.DB.prepare(`INSERT INTO tb_equipe (nome_equipe, nome_projeto, email, area_atuacao_curso, area_atuacao_projeto, nome_orientador, nome_coorientador, nome_integrante, nome_integrante2, nome_integrante3, nome_integrante4, nome_integrante5, usuario) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`).bind(nome_equipe, nome_projeto, email, area_atuacao_curso, area_atuacao_projeto, nome_orientador, nome_coorientador, nome_integrante, nome_integrante2, nome_integrante3, nome_integrante4, nome_integrante5, usuario).run();
                await env.DB.prepare(`INSERT INTO tb_informacoes_completude (usuario, qtd, dados_equipe, conhecimentos, recursos_aplicados, canvas_preencher, pitch_escrito, pitch_video, cronograma, foto_equipe, fotos_etapa_projeto) VALUES (?, 0, 'Não iniciada', 'Não iniciada', 'Não iniciada', 'Não iniciada', 'Não iniciada', 'Não iniciada', 'Não iniciada', 'Não iniciada', 'Não iniciada')`).bind(identifier).run();
              }
              return new Response(JSON.stringify({ success: true }), { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } });
            } catch (e) { console.error("Error saving equipe:", e); throw e; }
          }
          if (tipo === "conhecimentos") {
            try {
              const { plano_curso, conhecimentos_aplicados, capacidades_aplicadas } = campos;
              const record = await env.DB.prepare("SELECT id_conhecimentos FROM tb_conhecimentos WHERE usuario = ? OR usuario = ? OR usuario = ?").bind(equipe.nome_equipe, equipe.usuario, usuario).first();
              if (record) {
                await env.DB.prepare(`UPDATE tb_conhecimentos SET plano_curso = ?, conhecimentos_aplicados = ?, capacidades_aplicadas = ? WHERE id_conhecimentos = ?`).bind(plano_curso, conhecimentos_aplicados, capacidades_aplicadas, record.id_conhecimentos).run();
              } else {
                await env.DB.prepare(`INSERT INTO tb_conhecimentos (id_conhecimentos, plano_curso, conhecimentos_aplicados, capacidades_aplicadas, usuario) VALUES (?, ?, ?, ?, ?)`).bind(equipe.id_equipe, plano_curso, conhecimentos_aplicados, capacidades_aplicadas, identifier).run();
              }
              return new Response(JSON.stringify({ success: true }), { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } });
            } catch (e) { console.error("Error saving conhecimentos:", e); throw e; }
          }
          if (tipo === "recursos") {
            try {
              const { ferramentas, equipamentos, descricao_produto, quant_comprada, quant_utilizada, preco_estimado, uni_medida, fornecedor_principal, modo_obtencao, disponibilidade, pagamento, alternativas_consideradas, preco_total } = campos;
              const record = await env.DB.prepare("SELECT id_recursos FROM tb_recursos_aplicados WHERE usuario = ? OR usuario = ? OR usuario = ?").bind(equipe.nome_equipe, equipe.usuario, usuario).first();
              if (record) {
                await env.DB.prepare(`UPDATE tb_recursos_aplicados SET ferramentas = ?, equipamentos = ?, descricao_produto = ?, quant_comprada = ?, quant_utilizada = ?, preco_estimado = ?, uni_medida = ?, fornecedor_principal = ?, modo_obtencao = ?, disponibilidade = ?, pagamento = ?, alternativas_consideradas = ?, preco_total = ? WHERE id_recursos = ?`).bind(ferramentas, equipamentos, descricao_produto, quant_comprada, quant_utilizada, preco_estimado, uni_medida, fornecedor_principal, modo_obtencao, disponibilidade, pagamento, alternativas_consideradas, preco_total, record.id_recursos).run();
              } else {
                await env.DB.prepare(`INSERT INTO tb_recursos_aplicados (id_recursos, ferramentas, equipamentos, descricao_produto, quant_comprada, quant_utilizada, preco_estimado, uni_medida, fornecedor_principal, modo_obtencao, disponibilidade, pagamento, alternativas_consideradas, preco_total, usuario) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`).bind(equipe.id_equipe, ferramentas, equipamentos, descricao_produto, quant_comprada, quant_utilizada, preco_estimado, uni_medida, fornecedor_principal, modo_obtencao, disponibilidade, pagamento, alternativas_consideradas, preco_total, identifier).run();
              }
              return new Response(JSON.stringify({ success: true }), { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } });
            } catch (e) { console.error("Error saving recursos:", e); throw e; }
          }
          if (tipo === "cronograma") {
            try {
              const { processo, etapas, responsavel, data_inicio, data_final, observacoes } = campos;
              const record = await env.DB.prepare("SELECT id_cronograma FROM tb_cronograma WHERE usuario = ? OR usuario = ? OR usuario = ?").bind(equipe.nome_equipe, equipe.usuario, usuario).first();
              if (record) {
                await env.DB.prepare(`UPDATE tb_cronograma SET processo = ?, etapas = ?, responsavel = ?, data_inicio = ?, data_final = ?, observacoes = ? WHERE id_cronograma = ?`).bind(processo, etapas, responsavel, data_inicio, data_final, observacoes, record.id_cronograma).run();
              } else {
                await env.DB.prepare(`INSERT INTO tb_cronograma (id_cronograma, processo, etapas, responsavel, data_inicio, data_final, observacoes, usuario) VALUES (?, ?, ?, ?, ?, ?, ?, ?)`).bind(equipe.id_equipe, processo, etapas, responsavel, data_inicio, data_final, observacoes, identifier).run();
              }
              return new Response(JSON.stringify({ success: true }), { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } });
            } catch (e) { console.error("Error saving cronograma:", e); throw e; }
          }
          if (tipo === "canva") {
            try {
              const { atividades_chaves, proposta_chave, relacionamentos_clientes, segmentos_clientes, recursos_chaves, canais, estrutura_custos, fluxo_receita, parceiros_chaves } = campos;
              const record = await env.DB.prepare("SELECT id_canva FROM tb_canva WHERE usuario = ? OR usuario = ? OR usuario = ?").bind(equipe.nome_equipe, equipe.usuario, usuario).first();
              if (record) {
                await env.DB.prepare(`UPDATE tb_canva SET atividades_chaves = ?, proposta_chave = ?, relacionamentos_clientes = ?, segmentos_clientes = ?, recursos_chaves = ?, canais = ?, estrutura_custos = ?, fluxo_receita = ?, parceiros_chaves = ? WHERE id_canva = ?`).bind(atividades_chaves, proposta_chave, relacionamentos_clientes, segmentos_clientes, recursos_chaves, canais, estrutura_custos, fluxo_receita, parceiros_chaves, record.id_canva).run();
              } else {
                await env.DB.prepare(`INSERT INTO tb_canva (id_canva, atividades_chaves, proposta_chave, relacionamentos_clientes, segmentos_clientes, recursos_chaves, canais, estrutura_custos, fluxo_receita, parceiros_chaves, usuario) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`).bind(equipe.id_equipe, atividades_chaves, proposta_chave, relacionamentos_clientes, segmentos_clientes, recursos_chaves, canais, estrutura_custos, fluxo_receita, parceiros_chaves, identifier).run();
              }
              return new Response(JSON.stringify({ success: true }), { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } });
            } catch (e) { console.error("Error saving canva:", e); throw e; }
          }
          if (tipo === "empresa") {
            try {
              const { nome_empresa, cnpj, regiao, telefone_contato, email_contato, objetivos, problema_projeto } = campos;
              const record = await env.DB.prepare("SELECT id_empresa_formulario FROM tb_empresas_formulario WHERE id_empresa_formulario = ?").bind(equipe.id_equipe).first();
              if (record) {
                await env.DB.prepare(`UPDATE tb_empresas_formulario SET nome_empresa = ?, cnpj = ?, regiao = ?, telefone_contato = ?, email_contato = ?, objetivos = ?, problema_projeto = ? WHERE id_empresa_formulario = ?`).bind(nome_empresa, cnpj, regiao, telefone_contato, email_contato, objetivos, problema_projeto, equipe.id_equipe).run();
              } else {
                await env.DB.prepare(`INSERT INTO tb_empresas_formulario (id_empresa_formulario, nome_empresa, cnpj, regiao, telefone_contato, email_contato, objetivos, problema_projeto) VALUES (?, ?, ?, ?, ?, ?, ?, ?)`).bind(equipe.id_equipe, nome_empresa, cnpj, regiao, telefone_contato, email_contato, objetivos, problema_projeto).run();
              }
              return new Response(JSON.stringify({ success: true }), { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } });
            } catch (e) { console.error("Error saving empresa form:", e); throw e; }
          }
          if (tipo === "pitch") {
            try {
              const { roteiro } = campos;
              const record = await env.DB.prepare("SELECT id_pitch FROM tb_pitch WHERE usuario = ? OR usuario = ? OR usuario = ?").bind(equipe.nome_equipe, equipe.usuario, usuario).first();
              if (record) {
                await env.DB.prepare(`UPDATE tb_pitch SET roteiro = ? WHERE id_pitch = ?`).bind(roteiro, record.id_pitch).run();
              } else {
                await env.DB.prepare(`INSERT INTO tb_pitch (id_pitch, roteiro, usuario) VALUES (?, ?, ?)`).bind(equipe.id_equipe, roteiro, identifier).run();
              }
              return new Response(JSON.stringify({ success: true }), { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } });
            } catch (e) { console.error("Error saving pitch:", e); throw e; }
          }
          if (tipo === "ia") {
            try {
              const { nome_ferramenta, link_acesso, tipo_licenca, etapa_uso, criacao_prompt, descricao_uso } = campos;
              const record = await env.DB.prepare("SELECT id_uso_ia FROM tb_uso_ia WHERE usuario = ? OR usuario = ? OR usuario = ?").bind(equipe.nome_equipe, equipe.usuario, usuario).first();
              if (record) {
                await env.DB.prepare(`UPDATE tb_uso_ia SET nome_ferramenta = ?, link_acesso = ?, tipo_licenca = ?, etapa_uso = ?, criacao_prompt = ?, descricao_uso = ? WHERE id_uso_ia = ?`).bind(nome_ferramenta, link_acesso, tipo_licenca, etapa_uso, criacao_prompt, descricao_uso, record.id_uso_ia).run();
              } else {
                await env.DB.prepare(`INSERT INTO tb_uso_ia (id_uso_ia, usuario, nome_ferramenta, link_acesso, tipo_licenca, etapa_uso, criacao_prompt, descricao_uso) VALUES (?, ?, ?, ?, ?, ?, ?, ?)`).bind(equipe.id_equipe, identifier, nome_ferramenta, link_acesso, tipo_licenca, etapa_uso, criacao_prompt, descricao_uso).run();
              }
              return new Response(JSON.stringify({ success: true }), { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } });
            } catch (e) { console.error("Error saving IA:", e); throw e; }
          }
          if (tipo === "planilha") {
            try {
              const { tarefas, aluno_responsavel, professor_da_area, inicio_previsto, fim_previsto, inicio_realizado, fim_realizado, duracao, status, descricao_da_tarefa, dificuldades_enxergadas, impacto_nas_outras } = campos;
              const record = await env.DB.prepare("SELECT id_acompanhamento_projeto FROM tb_acompanhamento_projeto WHERE usuario = ? OR usuario = ? OR usuario = ?").bind(equipe.nome_equipe, equipe.usuario, usuario).first();
              if (record) {
                await env.DB.prepare(`UPDATE tb_acompanhamento_projeto SET tarefas = ?, aluno_responsavel = ?, professor_da_area = ?, inicio_previsto = ?, fim_previsto = ?, inicio_realizado = ?, fim_realizado = ?, duracao = ?, status = ?, descricao_da_tarefa = ?, dificuldades_enxergadas = ?, impacto_nas_outras = ? WHERE id_acompanhamento_projeto = ?`).bind(tarefas, aluno_responsavel, professor_da_area, inicio_previsto, fim_previsto, inicio_realizado, fim_realizado, duracao, status, descricao_da_tarefa, dificuldades_enxergadas, impacto_nas_outras, record.id_acompanhamento_projeto).run();
              } else {
                await env.DB.prepare(`INSERT INTO tb_acompanhamento_projeto (tarefas, aluno_responsavel, professor_da_area, inicio_previsto, fim_previsto, inicio_realizado, fim_realizado, duracao, status, descricao_da_tarefa, dificuldades_enxergadas, impacto_nas_outras, usuario) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`).bind(tarefas, aluno_responsavel, professor_da_area, inicio_previsto, fim_previsto, inicio_realizado, fim_realizado, duracao, status, descricao_da_tarefa, dificuldades_enxergadas, impacto_nas_outras, identifier).run();
              }
              return new Response(JSON.stringify({ success: true }), { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } });
            } catch (e) { console.error("Error saving planilha:", e); throw e; }
          }
          if (tipo === "complementares") {
            try {
              const { unidade_nome_comercial, coordenador_pedagogico, gestor, empresa, projeto, descricao } = campos;
              const record = await env.DB.prepare("SELECT id_informacoes_complementares FROM tb_informacoes_complementares WHERE usuario = ? OR usuario = ? OR usuario = ?").bind(equipe.nome_equipe, equipe.usuario, usuario).first();
              if (record) {
                await env.DB.prepare(`UPDATE tb_informacoes_complementares SET unidade_nome_comercial = ?, coordenador_pedagogico = ?, gestor = ?, empresa = ?, projeto = ?, descricao = ? WHERE id_informacoes_complementares = ?`).bind(unidade_nome_comercial, coordenador_pedagogico, gestor, empresa, projeto, descricao, record.id_informacoes_complementares).run();
              } else {
                await env.DB.prepare(`INSERT INTO tb_informacoes_complementares (unidade_nome_comercial, coordenador_pedagogico, gestor, empresa, projeto, descricao, usuario) VALUES (?, ?, ?, ?, ?, ?, ?)`).bind(unidade_nome_comercial, coordenador_pedagogico, gestor, empresa, projeto, descricao, identifier).run();
              }
              return new Response(JSON.stringify({ success: true }), { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } });
            } catch (e) { console.error("Error saving complementares:", e); throw e; }
          }
          if (tipo === "completude") {
            try {
              const { qtd, equipe_unidade_empresa, responsavel_preenchimento, dados_equipe, conhecimentos, recursos_aplicados, canvas_preencher, pitch_escrito, pitch_video, cronograma, foto_equipe, fotos_etapa_projeto } = campos;
              const record = await env.DB.prepare("SELECT id_informacoes_completude FROM tb_informacoes_completude WHERE usuario = ? OR usuario = ? OR usuario = ?").bind(equipe.nome_equipe, equipe.usuario, usuario).first();
              if (record) {
                await env.DB.prepare(`UPDATE tb_informacoes_completude SET qtd = ?, equipe_unidade_empresa = ?, responsavel_preenchimento = ?, dados_equipe = ?, conhecimentos = ?, recursos_aplicados = ?, canvas_preencher = ?, pitch_escrito = ?, pitch_video = ?, cronograma = ?, foto_equipe = ?, fotos_etapa_projeto = ? WHERE id_informacoes_completude = ?`).bind(qtd || 0, equipe_unidade_empresa, responsavel_preenchimento, dados_equipe, conhecimentos, recursos_aplicados, canvas_preencher, pitch_escrito, pitch_video, cronograma, foto_equipe, fotos_etapa_projeto, record.id_informacoes_completude).run();
              } else {
                await env.DB.prepare(`INSERT INTO tb_informacoes_completude (qtd, equipe_unidade_empresa, responsavel_preenchimento, dados_equipe, conhecimentos, recursos_aplicados, canvas_preencher, pitch_escrito, pitch_video, cronograma, foto_equipe, fotos_etapa_projeto, usuario) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`).bind(qtd || 0, equipe_unidade_empresa, responsavel_preenchimento, dados_equipe, conhecimentos, recursos_aplicados, canvas_preencher, pitch_escrito, pitch_video, cronograma, foto_equipe, fotos_etapa_projeto, identifier).run();
              }
              return new Response(JSON.stringify({ success: true }), { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } });
            } catch (e) { console.error("Error saving completude:", e); throw e; }
          }
          if (tipo === "relatorio") {
            try {
              const { nome_empresa, e_mail_empresa, setor_empresa, descricao, roteiro_pitch, integrante1, integrante2, integrante3, integrante4, integrante5, orientador, coorientador, nome_projeto, nome_equipe, area_atuacao_projeto, area_atuacao_curso, unidade_senai, gestor, ferramenta_ia, link_acesso, licenca, etapa_de_usu, prompt, motivo_usu, ferramentas_projeto, equipamentos_projeto, quant_compra, quant_utilizada, preco, fornecedor, modo_obtencao, processamento, alternativa_de_uso, quant_utilizada_2, forma_pagamento, preco_total } = campos;
              const record = await env.DB.prepare("SELECT id_relatorio FROM tb_relatorio WHERE usuario = ? OR usuario = ? OR usuario = ?").bind(equipe.nome_equipe, equipe.usuario, usuario).first();
              if (record) {
                await env.DB.prepare(`UPDATE tb_relatorio SET nome_empresa = ?, e_mail_empresa = ?, setor_empresa = ?, descricao = ?, roteiro_pitch = ?, integrante1 = ?, integrante2 = ?, integrante3 = ?, integrante4 = ?, integrante5 = ?, orientador = ?, coorientador = ?, nome_projeto = ?, nome_equipe = ?, area_atuacao_projeto = ?, area_atuacao_curso = ?, unidade_senai = ?, gestor = ?, ferramenta_ia = ?, link_acesso = ?, licenca = ?, etapa_de_usu = ?, prompt = ?, motivo_usu = ?, ferramentas_projeto = ?, equipamentos_projeto = ?, quant_compra = ?, quant_utilizada = ?, preco = ?, fornecedor = ?, modo_obtencao = ?, processamento = ?, alternativa_de_uso = ?, quant_utilizada_2 = ?, forma_pagamento = ?, preco_total = ? WHERE id_relatorio = ?`).bind(nome_empresa, e_mail_empresa, setor_empresa, descricao, roteiro_pitch, integrante1, integrante2, integrante3, integrante4, integrante5, orientador, coorientador, nome_projeto, nome_equipe, area_atuacao_projeto, area_atuacao_curso, unidade_senai, gestor, ferramenta_ia, link_acesso, licenca, etapa_de_usu, prompt, motivo_usu, ferramentas_projeto, equipamentos_projeto, quant_compra, quant_utilizada, preco, fornecedor, modo_obtencao, processamento, alternativa_de_uso, quant_utilizada_2, forma_pagamento, preco_total, record.id_relatorio).run();
              } else {
                await env.DB.prepare(`INSERT INTO tb_relatorio (nome_empresa, e_mail_empresa, setor_empresa, descricao, roteiro_pitch, integrante1, integrante2, integrante3, integrante4, integrante5, orientador, coorientador, nome_projeto, nome_equipe, area_atuacao_projeto, area_atuacao_curso, unidade_senai, gestor, ferramenta_ia, link_acesso, licenca, etapa_de_usu, prompt, motivo_usu, ferramentas_projeto, equipamentos_projeto, quant_compra, quant_utilizada, preco, fornecedor, modo_obtencao, processamento, alternativa_de_uso, quant_utilizada_2, forma_pagamento, preco_total, usuario) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`).bind(nome_empresa, e_mail_empresa, setor_empresa, descricao, roteiro_pitch, integrante1, integrante2, integrante3, integrante4, integrante5, orientador, coorientador, nome_projeto, nome_equipe, area_atuacao_projeto, area_atuacao_curso, unidade_senai, gestor, ferramenta_ia, link_acesso, licenca, etapa_de_usu, prompt, motivo_usu, ferramentas_projeto, equipamentos_projeto, quant_compra, quant_utilizada, preco, fornecedor, modo_obtencao, processamento, alternativa_de_uso, quant_utilizada_2, forma_pagamento, preco_total, identifier).run();
              }
              return new Response(JSON.stringify({ success: true }), { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } });
            } catch (e) { console.error("Error saving relatorio:", e); throw e; }
          }
          if (tipo === "curriculo") {
            try {
              const { nome, data_nascimento, empresa_vinculado, projeto, telefone, email, habilidades, fez_projeto, cidade, motivo_projeto, aprendo_mais, prefiro_trabalhar } = campos;
              const record = await env.DB.prepare("SELECT id_aluno FROM tb_curriculo_alunos WHERE usuario = ? OR email = ?").bind(usuario, usuario).first();
              if (record) {
                await env.DB.prepare(`UPDATE tb_curriculo_alunos SET nome = ?, data_nascimento = ?, empresa_vinculado = ?, projeto = ?, telefone = ?, email = ?, habilidades = ?, fez_projeto = ?, cidade = ?, motivo_projeto = ?, aprendo_mais = ?, prefiro_trabalhar = ? WHERE id_aluno = ?`).bind(nome, data_nascimento, empresa_vinculado, projeto, telefone, email, habilidades, fez_projeto, cidade, motivo_projeto, aprendo_mais, prefiro_trabalhar, record.id_aluno).run();
              } else {
                await env.DB.prepare(`INSERT INTO tb_curriculo_alunos (nome, data_nascimento, empresa_vinculado, projeto, telefone, email, habilidades, fez_projeto, cidade, motivo_projeto, aprendo_mais, prefiro_trabalhar, usuario) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`).bind(nome, data_nascimento, empresa_vinculado, projeto, telefone, email, habilidades, fez_projeto, cidade, motivo_projeto, aprendo_mais, prefiro_trabalhar, usuario).run();
              }
              return new Response(JSON.stringify({ success: true }), { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } });
            } catch (e) { console.error("Error saving curriculo:", e); throw e; }
          }
        } catch (error) { return new Response(JSON.stringify({ success: false, error: "Erro ao salvar: " + error.message }), { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } }); }
      }

      if (path === "/buscar-dados") {
        if (!usuario || !tipo) return new Response(JSON.stringify({ success: false, error: "Dados inválidos." }), { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } });
        try {
          const equipe = await env.DB.prepare("SELECT * FROM tb_equipe WHERE usuario = ? OR email = ? OR nome_equipe = ?").bind(usuario, usuario, usuario).first();
          if (!equipe && tipo !== "feedback" && tipo !== "curriculo" && tipo !== "necessidades") return new Response(JSON.stringify({ success: false, error: "Nenhum registro de equipe encontrado." }), { status: 404, headers: { ...corsHeaders, "Content-Type": "application/json" } });

          const resolvedTeamName = equipe?.nome_equipe || "";
          const identifier = (resolvedTeamName && resolvedTeamName.trim() !== "") ? resolvedTeamName : (equipe?.usuario || usuario);

          if (tipo === "feedback") {
            if (!equipe) return new Response(JSON.stringify({ success: true, dados: { comentario_empresa: "" } }), { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } });
            const record = await env.DB.prepare("SELECT comentario_empresa FROM tb_acompanhamento_projeto WHERE usuario = ? OR usuario = ? OR usuario = ?").bind(equipe.nome_equipe, equipe.usuario, usuario).first();
            return new Response(JSON.stringify({ success: true, dados: record || { comentario_empresa: "" } }), { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } });
          }
          if (tipo === "equipe") {
            // Ensure all columns from tb_equipe are mapped and returned
            const mappedEquipe = {
              id_equipe: equipe.id_equipe,
              nome_equipe: equipe.nome_equipe || "",
              nome_projeto: equipe.nome_projeto || "",
              email: equipe.email || "",
              area_atuacao_curso: equipe.area_atuacao_curso || "",
              area_atuacao_projeto: equipe.area_atuacao_projeto || "",
              nome_orientador: equipe.nome_orientador || "",
              nome_coorientador: equipe.nome_coorientador || "",
              nome_integrante: equipe.nome_integrante || "",
              nome_integrante2: equipe.nome_integrante2 || "",
              nome_integrante3: equipe.nome_integrante3 || "",
              nome_integrante4: equipe.nome_integrante4 || "",
              nome_integrante5: equipe.nome_integrante5 || "",
              usuario: equipe.usuario || "",
              processado: equipe.processado || 0
            };
            return new Response(JSON.stringify({ success: true, dados: mappedEquipe }), { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } });
          }
          if (tipo === "necessidades") {
            const empresa = await env.DB.prepare("SELECT id_empresa FROM tb_empresas WHERE email_contato = ? OR usuario = ?").bind(usuario, usuario).first();
            if (!empresa) return new Response(JSON.stringify({ success: true, dados: [] }), { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } });
            const { results } = await env.DB.prepare("SELECT nome, descricao FROM tb_necessidades_empresas WHERE empresa_id = ?").bind(empresa.id_empresa).all();
            return new Response(JSON.stringify({ success: true, dados: results || [] }), { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } });
          }
          if (tipo === "conhecimentos") {
            const record = await env.DB.prepare("SELECT plano_curso, conhecimentos_aplicados, capacidades_aplicadas FROM tb_conhecimentos WHERE usuario = ? OR usuario = ? OR usuario = ?").bind(equipe.nome_equipe, equipe.usuario, usuario).first();
            return new Response(JSON.stringify({ success: true, dados: record || {} }), { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } });
          }
          if (tipo === "recursos") {
            const record = await env.DB.prepare("SELECT ferramentas, equipamentos, descricao_produto, quant_comprada, quant_utilizada, preco_estimado, uni_medida, fornecedor_principal, modo_obtencao, disponibilidade, pagamento, alternativas_consideradas, preco_total FROM tb_recursos_aplicados WHERE usuario = ? OR usuario = ? OR usuario = ?").bind(equipe.nome_equipe, equipe.usuario, usuario).first();
            return new Response(JSON.stringify({ success: true, dados: record || {} }), { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } });
          }
          if (tipo === "cronograma") {
            const record = await env.DB.prepare("SELECT processo, etapas, responsavel, data_inicio, data_final, observacoes FROM tb_cronograma WHERE usuario = ? OR usuario = ? OR usuario = ?").bind(equipe.nome_equipe, equipe.usuario, usuario).first();
            return new Response(JSON.stringify({ success: true, dados: record || {} }), { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } });
          }
          if (tipo === "canva") {
            const record = await env.DB.prepare("SELECT atividades_chaves, proposta_chave, relacionamentos_clientes, segmentos_clientes, recursos_chaves, canais, estrutura_custos, fluxo_receita, parceiros_chaves FROM tb_canva WHERE usuario = ? OR usuario = ? OR usuario = ?").bind(equipe.nome_equipe, equipe.usuario, usuario).first();
            return new Response(JSON.stringify({ success: true, dados: record || {} }), { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } });
          }
          if (tipo === "empresa") {
            const record = await env.DB.prepare("SELECT nome_empresa, cnpj, regiao, telefone_contato, email_contato, objetivos, problema_projeto FROM tb_empresas_formulario WHERE id_empresa_formulario = ?").bind(equipe.id_equipe).first();
            return new Response(JSON.stringify({ success: true, dados: record || {} }), { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } });
          }
          if (tipo === "pitch") {
            const record = await env.DB.prepare("SELECT roteiro, video_url FROM tb_pitch WHERE usuario = ? OR usuario = ? OR usuario = ?").bind(equipe.nome_equipe, equipe.usuario, usuario).first();
            return new Response(JSON.stringify({ success: true, dados: record || {} }), { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } });
          }
          if (tipo === "ia") {
            const record = await env.DB.prepare("SELECT nome_ferramenta, link_acesso, tipo_licenca, etapa_uso, criacao_prompt, descricao_uso FROM tb_uso_ia WHERE usuario = ? OR usuario = ? OR usuario = ?").bind(equipe.nome_equipe, equipe.usuario, usuario).first();
            return new Response(JSON.stringify({ success: true, dados: record || {} }), { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } });
          }
          if (tipo === "planilha") {
            const record = await env.DB.prepare("SELECT tarefas, aluno_responsavel, professor_da_area, inicio_previsto, fim_previsto, inicio_realizado, fim_realizado, duracao, status, descricao_da_tarefa, dificuldades_enxergadas, impacto_nas_outras FROM tb_acompanhamento_projeto WHERE usuario = ? OR usuario = ? OR usuario = ?").bind(equipe.nome_equipe, equipe.usuario, usuario).first();
            return new Response(JSON.stringify({ success: true, dados: record || {} }), { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } });
          }
          if (tipo === "complementares") {
            const record = await env.DB.prepare("SELECT unidade_nome_comercial, coordenador_pedagogico, gestor, empresa, projeto, descricao FROM tb_informacoes_complementares WHERE usuario = ? OR usuario = ? OR usuario = ?").bind(equipe.nome_equipe, equipe.usuario, usuario).first();
            return new Response(JSON.stringify({ success: true, dados: record || {} }), { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } });
          }
          if (tipo === "completude") {
            const record = await env.DB.prepare("SELECT qtd, equipe_unidade_empresa, responsavel_preenchimento, dados_equipe, conhecimentos, recursos_aplicados, canvas_preencher, pitch_escrito, pitch_video, cronograma, foto_equipe, fotos_etapa_projeto FROM tb_informacoes_completude WHERE usuario = ? OR usuario = ? OR usuario = ?").bind(equipe.nome_equipe, equipe.usuario, usuario).first();
            return new Response(JSON.stringify({ success: true, dados: record || {} }), { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } });
          }
          if (tipo === "relatorio") {
            const record = await env.DB.prepare("SELECT * FROM tb_relatorio WHERE usuario = ? OR usuario = ? OR usuario = ?").bind(equipe.nome_equipe, equipe.usuario, usuario).first();
            return new Response(JSON.stringify({ success: true, dados: record || {} }), { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } });
          }
          if (tipo === "curriculo") {
            const record = await env.DB.prepare("SELECT * FROM tb_curriculo_alunos WHERE usuario = ? OR email = ?").bind(usuario, usuario).first();
            return new Response(JSON.stringify({ success: true, existe: !!record, dados: record || {} }), { headers: { ...corsHeaders, "Content-Type": "application/json" } });
          }
          return new Response(JSON.stringify({ success: false, error: "Tipo de formulário inválido." }), { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } });
        } catch (error) { return new Response(JSON.stringify({ success: false, error: "Erro ao buscar: " + error.message }), { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } }); }
      }

      if (path === "/salvar-comentario") {
        const { nome_equipe, comentario } = body;
        if (!nome_equipe) return new Response(JSON.stringify({ success: false, message: "Dados ausentes." }), { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } });
        try {
          const equipeRecord = await env.DB.prepare("SELECT id_equipe, nome_equipe, usuario FROM tb_equipe WHERE usuario = ? OR email = ? OR nome_equipe = ?").bind(nome_equipe, nome_equipe, nome_equipe).first();
          if (!equipeRecord) return new Response(JSON.stringify({ success: false, message: "Projeto não encontrado." }), { status: 404, headers: { ...corsHeaders, "Content-Type": "application/json" } });
          const identifier = (equipeRecord.nome_equipe && equipeRecord.nome_equipe.trim() !== "") ? equipeRecord.nome_equipe : (equipeRecord.usuario || nome_equipe);
          const record = await env.DB.prepare("SELECT id_acompanhamento_projeto FROM tb_acompanhamento_projeto WHERE usuario = ? OR usuario = ? OR usuario = ?").bind(equipeRecord.nome_equipe, equipeRecord.usuario, nome_equipe).first();
          if (record) {
            await env.DB.prepare("UPDATE tb_acompanhamento_projeto SET comentario_empresa = ? WHERE id_acompanhamento_projeto = ?").bind(comentario, record.id_acompanhamento_projeto).run();
            return new Response(JSON.stringify({ success: true, message: "Feedback salvo com sucesso!" }), { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } });
          } else {
            await env.DB.prepare("INSERT INTO tb_acompanhamento_projeto (tarefas, aluno_responsavel, professor_da_area, inicio_previsto, fim_previsto, status, descricao_da_tarefa, usuario, comentario_empresa) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)").bind("Pendentes", "A definir", "A definir", new Date().toISOString().split('T')[0], new Date().toISOString().split('T')[0], "Não iniciado", "Feedback inicial da empresa", identifier, comentario).run();
            return new Response(JSON.stringify({ success: true, message: "Feedback criado com sucesso!" }), { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } });
          }
        } catch (e) { return new Response(JSON.stringify({ success: false, error: e.message }), { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } }); }
      }

      if (path === "/atualizar-perfil") {
        const { email_atual, novo_nome, novo_email, foto_perfil, cnpj, endereco, setor, descricao, telefone_contato, nova_senha } = body;
        try {
          const urlFoto = await uploadBase64ToR2(foto_perfil, email_atual, env);
          const _senha = nova_senha !== undefined ? nova_senha : null;
          const infoCad = await env.DB.prepare("UPDATE tb_cadastros SET nome_usuarios = ?, email = ?, foto_perfil = ?, senha = COALESCE(?, senha) WHERE email = ?").bind(novo_nome, novo_email, urlFoto, _senha, email_atual).run();
          if (infoCad.meta.changes > 0) {
            await env.DB.prepare(`UPDATE tb_empresas SET foto_perfil = ?, nome_empresa = ?, usuario = ?, email_contato = ?, cnpj = COALESCE(?, cnpj), endereco = COALESCE(?, endereco), setor = COALESCE(?, setor), descricao = COALESCE(?, descricao), telefone_contato = COALESCE(?, telefone_contato), senha = COALESCE(?, senha) WHERE email_contato = ?`).bind(urlFoto, novo_nome, novo_nome, novo_email, cnpj||null, endereco||null, setor||null, descricao||null, telefone_contato||null, _senha, email_atual).run();
          } else {
            await env.DB.prepare("UPDATE tb_cadastros SET nome_usuarios = ?, email = ?, foto_perfil = ?, senha = COALESCE(?, senha) WHERE nome_usuarios = ?").bind(novo_nome, novo_email, urlFoto, _senha, email_atual).run();
            await env.DB.prepare(`UPDATE tb_empresas SET foto_perfil = ?, nome_empresa = ?, usuario = ?, email_contato = ?, cnpj = COALESCE(?, cnpj), endereco = COALESCE(?, endereco), setor = COALESCE(?, setor), descricao = COALESCE(?, descricao), telefone_contato = COALESCE(?, telefone_contato), senha = COALESCE(?, senha) WHERE usuario = ? OR nome_empresa = ?`).bind(urlFoto, novo_nome, novo_nome, novo_email, cnpj||null, endereco||null, setor||null, descricao||null, telefone_contato||null, _senha, email_atual, email_atual).run();
          }
          return new Response(JSON.stringify({ success: true, foto_url: urlFoto }), { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } });
        } catch (e) { return new Response(JSON.stringify({ success: false, error: e.message }), { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } }); }
      }

      if (path === "/login-google") {
        const { idToken } = body;
        if (!idToken) return new Response(JSON.stringify({ success: false, message: "Token ausente." }), { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } });
        const gResp = await fetch(`https://oauth2.googleapis.com/tokeninfo?id_token=${encodeURIComponent(idToken)}`);
        if (!gResp.ok) return new Response(JSON.stringify({ success: false, message: "Token inválido." }), { status: 401, headers: { ...corsHeaders, "Content-Type": "application/json" } });
        const gUser = await gResp.json();
        const email = gUser.email || "";
        let nome = gUser.name || email.split('@')[0];
        const res = await env.DB.prepare("SELECT * FROM tb_cadastros WHERE email = ?").bind(email).first();
        let user;
        if (res) { user = res; } else {
          try {
            const ins = await env.DB.prepare("INSERT INTO tb_cadastros (nome_usuarios, email, senha, nivel_de_acesso, foto_perfil) VALUES (?, ?, 'GOOGLE_AUTH', 6, ?)").bind(nome, email, gUser.picture || "").run();
            user = { id_cadastro: ins.meta.last_row_id, nome_usuarios: nome, email, nivel_de_acesso: 6, foto_perfil: gUser.picture };
          } catch {
            const insF = await env.DB.prepare("INSERT INTO tb_cadastros (nome_usuarios, email, senha, nivel_de_acesso, foto_perfil) VALUES (?, ?, 'GOOGLE_AUTH', 6, ?)").bind(email, email, gUser.picture || "").run();
            user = { id_cadastro: insF.meta.last_row_id, nome_usuarios: email, email, nivel_de_acesso: 6, foto_perfil: gUser.picture };
          }
        }
        return new Response(JSON.stringify({ success: true, id: user.id_cadastro, nivel: user.nivel_de_acesso, email_usuario: user.email, nome_usuario: user.nome_usuarios, foto_usuario: user.foto_perfil }), { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } });
      }

      if (path === "/cadastro") {
        const { nome_usuarios, email, senha, nivel_de_acesso } = body;
        if (!nome_usuarios || !email || !senha || nivel_de_acesso == null) return new Response(JSON.stringify({ success: false, error: "Preencha todos os campos." }), { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } });
        try {
          const ex = await env.DB.prepare("SELECT id_cadastro FROM tb_cadastros WHERE email = ?").bind(email).first();
          if (ex) return new Response(JSON.stringify({ success: false, error: "Este e-mail já está cadastrado." }), { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } });
          await env.DB.prepare("INSERT INTO tb_cadastros (nome_usuarios, senha, email, nivel_de_acesso) VALUES (?, ?, ?, ?)").bind(nome_usuarios, senha, email, nivel_de_acesso).run();
          if (Number(nivel_de_acesso) === 4) await env.DB.prepare("INSERT INTO tb_empresas (nome_empresa, email_contato, usuario) VALUES (?, ?, ?)").bind(nome_usuarios, email, nome_usuarios).run();
          return new Response(JSON.stringify({ success: true, message: "Usuário cadastrado com sucesso." }), { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } });
        } catch (e) { return new Response(JSON.stringify({ success: false, error: e.message }), { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } }); }
      }

      if (path === "/login") {
        const { email, senha } = body;
        const u = await env.DB.prepare("SELECT id_cadastro, nome_usuarios, nivel_de_acesso, email, foto_perfil FROM tb_cadastros WHERE (email = ? OR nome_usuarios = ?) AND senha = ?").bind(email, email, senha).first();
        if (u) return new Response(JSON.stringify({ success: true, id: u.id_cadastro, nome_usuario: u.nome_usuarios, nivel: u.nivel_de_acesso, email: u.email, foto_perfil: u.foto_perfil }), { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } });
        return new Response(JSON.stringify({ success: false, message: "E-mail ou senha incorretos!" }), { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } });
      }

      if (path === "/gerar-relatorio") {
        try {
          const uSol = new URL(request.url).searchParams.get("usuario");
          if (!uSol) return new Response(JSON.stringify({ success: false, message: "Usuário não fornecido." }), { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } });
          const { results } = await env.DB.prepare(`SELECT eq.*, emp.nome_empresa, emp.email_contato, emp.setor, emp.descricao AS descricao_empresa, p.roteiro, ic.unidade_nome_comercial, ic.gestor, ia.nome_ferramenta, ia.link_acesso, ia.tipo_licenca, ia.etapa_uso, ia.criacao_prompt, ia.descricao_uso AS descricao_ia, ra.ferramentas, ra.equipamentos, ra.quant_comprada, ra.quant_utilizada, ra.preco_total, ra.fornecedor_principal, ra.modo_obtencao, ra.alternativas_consideradas FROM tb_equipe eq LEFT JOIN tb_empresas emp ON (eq.usuario = emp.usuario OR eq.nome_equipe = emp.usuario) LEFT JOIN tb_pitch p ON (eq.usuario = p.usuario OR eq.nome_equipe = p.usuario) LEFT JOIN tb_informacoes_complementares ic ON (eq.usuario = ic.usuario OR eq.nome_equipe = ic.usuario) LEFT JOIN tb_uso_ia ia ON (eq.usuario = ia.usuario OR eq.nome_equipe = ia.usuario) LEFT JOIN tb_recursos_aplicados ra ON (eq.usuario = ra.usuario OR eq.nome_equipe = ra.usuario) WHERE eq.usuario = ? OR eq.nome_equipe = ?`).bind(uSol, uSol).all();
          if (!results || results.length === 0) return new Response(JSON.stringify({ success: true, message: "Nenhum dado novo encontrado." }), { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } });
          const batch = [];
          for (const row of results) {
            const rowIdentifier = (row.nome_equipe && row.nome_equipe.trim() !== "") ? row.nome_equipe : (row.usuario || uSol);
            batch.push(env.DB.prepare(`INSERT INTO tb_relatorio (nome_empresa, e_mail_empresa, setor_empresa, descricao, roteiro_pitch, integrante1, integrante2, integrante3, integrante4, integrante5, orientador, coorientador, nome_projeto, nome_equipe, area_atuacao_projeto, area_atuacao_curso, unidade_senai, gestor, ferramenta_ia, link_acesso, licenca, etapa_de_usu, prompt, motivo_usu, ferramentas_projeto, equipamentos_projeto, quant_compra, quant_utilizada, preco_total, fornecedor, modo_obtencao, alternativa_de_uso, processamento, usuario) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, ?)`).bind(row.nome_empresa || "Não informado", row.email_contato || "Não informado", row.setor || "", row.descricao_empresa || "Sem descrição", row.roteiro || "", row.nome_integrante || "Não informado", row.nome_integrante2 || "", row.nome_integrante3 || "", row.nome_integrante4 || "", row.nome_integrante5 || "", row.nome_orientador || "", row.nome_coorientador || "", row.nome_projeto || "", row.nome_equipe || "", row.area_atuacao_projeto || "", row.area_atuacao_curso || "", row.unidade_nome_comercial || "", row.gestor || "", row.nome_ferramenta || "", row.link_acesso || "", row.tipo_licenca || "", row.etapa_uso || "", row.criacao_prompt || "", row.descricao_ia || "", row.ferramentas || "", row.equipamentos || "", row.quant_comprada || 0, row.quant_utilizada || 0, row.preco_total || 0, row.fornecedor_principal || "", row.modo_obtencao || "", row.alternativas_consideradas || "", rowIdentifier));
            batch.push(env.DB.prepare(`UPDATE tb_equipe SET processado = 1 WHERE usuario = ?`).bind(row.usuario));
          }
          await env.DB.batch(batch);
          return new Response(JSON.stringify({ success: true, message: `Sucesso! O relatório foi gerado.` }), { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } });
        } catch (error) { return new Response(JSON.stringify({ success: false, error: error.message }), { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } }); }
      }

      if (path === "/gerar-canva") {
        try {
          const uSol = new URL(request.url).searchParams.get("usuario");
          if (!uSol) return new Response(JSON.stringify({ success: false, message: "Usuário não fornecido." }), { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } });
          const record = await env.DB.prepare(`SELECT c.* FROM tb_canva c LEFT JOIN tb_equipe eq ON (c.usuario = eq.usuario OR c.usuario = eq.nome_equipe) WHERE c.usuario = ? OR eq.usuario = ? OR eq.nome_equipe = ?`).bind(uSol, uSol, uSol).first();
          if (!record) return new Response(JSON.stringify({ success: false, message: "Canva não preenchido." }), { status: 404, headers: { ...corsHeaders, "Content-Type": "application/json" } });
          return new Response(JSON.stringify({ success: true, message: "Canva validado!" }), { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } });
        } catch (error) { return new Response(JSON.stringify({ success: false, error: error.message }), { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } }); }
      }

      if (path === "/necessidades-cadastro") {
        try {
          const { usuario: u, nome: n, descricao: d } = body;
          if (!u || !n || !d) return new Response(JSON.stringify({ success: false, message: "Dados incompletos." }), { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } });
          const eRec = await env.DB.prepare(`SELECT id_empresa FROM tb_empresas WHERE email_contato = ? OR usuario = ?`).bind(u, u).first();
          if (!eRec) return new Response(JSON.stringify({ success: false, message: "Empresa não encontrada." }), { status: 404, headers: { ...corsHeaders, "Content-Type": "application/json" } });
          await env.DB.prepare(`INSERT INTO tb_necessidades_empresas (empresa_id, nome, descricao) VALUES (?, ?, ?)`).bind(eRec.id_empresa, n, d).run();
          return new Response(JSON.stringify({ success: true, message: "Necessidade cadastrada!" }), { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } });
        } catch (dbError) { return new Response(JSON.stringify({ success: false, message: dbError.message }), { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } }); }
      }

      if (path === "/salvar-curriculo") {
        try {
          const { nome, email, data_nascimento, telefone, cidade, habilidades, fez_projeto, projeto, empresa_vinculado, motivo_projeto, aprendo_mais, prefiro_trabalhar, usuario_logado } = body;
          const emailBusca = usuario_logado || email;
          if (!emailBusca) return new Response(JSON.stringify({ success: false, message: 'E-mail não identificado.' }), { status: 400, headers: { ...corsHeaders, 'Content-Type': 'application/json' } });
          const val = (v) => (v === undefined || v === null) ? "" : String(v);
          const curEx = await env.DB.prepare("SELECT id_aluno FROM tb_curriculo_alunos WHERE usuario = ? OR email = ?").bind(emailBusca, emailBusca).first();
          if (curEx) {
            await env.DB.prepare(`UPDATE tb_curriculo_alunos SET nome = ?, data_nascimento = ?, telefone = ?, cidade = ?, habilidades = ?, fez_projeto = ?, projeto = ?, empresa_vinculado = ?, motivo_projeto = ?, aprendo_mais = ?, prefiro_trabalhar = ?, usuario = ? WHERE id_aluno = ?`).bind(val(nome), val(data_nascimento), val(telefone), val(cidade), val(habilidades), val(fez_projeto), val(projeto), val(empresa_vinculado), val(motivo_projeto), val(aprendo_mais), val(prefiro_trabalhar), emailBusca, curEx.id_aluno).run();
          } else {
            await env.DB.prepare(`INSERT INTO tb_curriculo_alunos (nome, email, data_nascimento, telefone, cidade, habilidades, fez_projeto, projeto, empresa_vinculado, motivo_projeto, aprendo_mais, prefiro_trabalhar, usuario) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`).bind(val(nome), val(email), val(data_nascimento), val(telefone), val(cidade), val(habilidades), val(fez_projeto), val(projeto), val(empresa_vinculado), val(motivo_projeto), val(aprendo_mais), val(prefiro_trabalhar), emailBusca).run();
          }
          return new Response(JSON.stringify({ success: true, message: 'Currículo salvo!' }), { headers: { ...corsHeaders, 'Content-Type': 'application/json' } });
        } catch (dbError) { return new Response(JSON.stringify({ success: false, message: dbError.message }), { status: 500, headers: { ...corsHeaders, 'Content-Type': 'application/json' } }); }
      }

      if (path === "/preencher-curriculo") {
        try {
          const { email_sessao, nome_usuario } = body;
          if (!email_sessao) return new Response(JSON.stringify({ success: false, error: 'E-mail não fornecido' }), { status: 400, headers: { ...corsHeaders, 'Content-Type': 'application/json' } });

          console.log(`[LOG] Iniciando preenchimento de currículo para: ${email_sessao}`);

          const pdfResp = await fetch(`https://avell.tailfdec8e.ts.net:8443/download-curriculo/${email_sessao}`).catch(e => {
            throw new Error(`Erro ao conectar ao servidor de currículos: ${e.message}`);
          });

          if (!pdfResp.ok) {
            const errorTxt = await pdfResp.text().catch(() => "Erro desconhecido");
            throw new Error(`Erro no servidor Python (${pdfResp.status}): ${errorTxt}`);
          }
          const pdfBuffer = await pdfResp.arrayBuffer();

          // Resolve resolvedTeamName from tb_equipe to ensure it appears in the correct project tab
          const equipe = await env.DB.prepare("SELECT nome_equipe FROM tb_equipe WHERE usuario = ? OR email = ? OR nome_equipe = ?").bind(email_sessao, email_sessao, email_sessao).first();
          const resolvedTeamName = equipe?.nome_equipe || nome_usuario || email_sessao;

          const fileName = `documentos/curriculo_${email_sessao.replace(/[^a-zA-Z0-9]/g, '_')}_${Date.now()}.pdf`;
          const R2_PUBLIC_URL_VIDEOS = "https://pub-8b39c2fa88234341ac68682a11d82f77.r2.dev";
          const fileUrl = `${R2_PUBLIC_URL_VIDEOS}/${fileName}`;

          await env.BUCKET_VIDEOS.put(fileName, pdfBuffer, { httpMetadata: { contentType: 'application/pdf' } });

          const docName = `Currículo - ${email_sessao}`;

          // Limpeza Agressiva: Apaga currículos antigos usando o e-mail no nome ou o nome do usuário
          // Isso limpa formatos antigos como "Curriculo_Conta de Aluno.pdf" e o formato novo
          const searchPatternEmail = `%${email_sessao}%`;
          const searchPatternNome = `%${nome_usuario}%`;

          await env.DB.prepare(`
            DELETE FROM tb_documentos
            WHERE tipo_documento = 'Curriculo'
            AND (nome_documento LIKE ? OR nome_documento LIKE ? OR usuario_vinculado = ? OR usuario_vinculado = ?)
          `).bind(searchPatternEmail, searchPatternNome, email_sessao, resolvedTeamName).run();

          await env.DB.prepare("INSERT INTO tb_documentos (nome_documento, tipo_documento, url_documento, usuario_vinculado) VALUES (?, ?, ?, ?)").bind(docName, "Curriculo", fileUrl, resolvedTeamName).run();

          console.log(`[LOG] Currículo salvo com sucesso para ${email_sessao}. Vinculado a: ${resolvedTeamName}`);
          return new Response(JSON.stringify({ success: true, url: fileUrl }), { status: 200, headers: { ...corsHeaders, 'Content-Type': 'application/json' } });
        } catch (err) {
          console.error(`[ERROR] Erro em /preencher-curriculo: ${err.message}`);
          return new Response(JSON.stringify({ success: false, error: err.message }), { status: 500, headers: { ...corsHeaders, 'Content-Type': 'application/json' } });
        }
      }

      if (path === "/excluir-projeto") {
        try {
          const { nome_equipe: input } = body;
          if (!input) return new Response(JSON.stringify({ success: false, error: "Nome não fornecido." }), { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } });
          const e = await env.DB.prepare(`SELECT id_equipe, nome_equipe, usuario FROM tb_equipe WHERE nome_equipe = ? OR usuario = ? OR email = ?`).bind(input, input, input).first();
          if (!e) return new Response(JSON.stringify({ success: false, error: "Projeto não encontrado." }), { status: 404, headers: { ...corsHeaders, "Content-Type": "application/json" } });
          const { id_equipe, nome_equipe, usuario } = e;
          const batch = [
            env.DB.prepare(`DELETE FROM tb_conhecimentos WHERE usuario = ?`).bind(nome_equipe),
            env.DB.prepare(`DELETE FROM tb_recursos_aplicados WHERE usuario = ?`).bind(nome_equipe),
            env.DB.prepare(`DELETE FROM tb_cronograma WHERE usuario = ?`).bind(nome_equipe),
            env.DB.prepare(`DELETE FROM tb_canva WHERE usuario = ?`).bind(nome_equipe),
            env.DB.prepare(`DELETE FROM tb_empresas_formulario WHERE id_empresa_formulario = ?`).bind(id_equipe),
            env.DB.prepare(`DELETE FROM tb_pitch WHERE usuario = ?`).bind(nome_equipe),
            env.DB.prepare(`DELETE FROM tb_uso_ia WHERE usuario = ?`).bind(nome_equipe),
            env.DB.prepare(`DELETE FROM tb_acompanhamento_projeto WHERE usuario = ?`).bind(nome_equipe),
            env.DB.prepare(`DELETE FROM tb_informacoes_complementares WHERE usuario = ?`).bind(nome_equipe),
            env.DB.prepare(`DELETE FROM tb_informacoes_completude WHERE usuario = ?`).bind(nome_equipe),
            env.DB.prepare(`DELETE FROM tb_relatorio WHERE usuario = ? OR usuario = ?`).bind(usuario, nome_equipe),
            env.DB.prepare(`DELETE FROM tb_equipe WHERE id_equipe = ?`).bind(id_equipe),
          ];
          await env.DB.batch(batch);
          return new Response(JSON.stringify({ success: true, message: "Projeto excluído." }), { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } });
        } catch (error) { return new Response(JSON.stringify({ success: false, error: error.message }), { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } }); }
      }
    }

    return new Response("Rota não encontrada", { status: 404, headers: corsHeaders });
  }
};
