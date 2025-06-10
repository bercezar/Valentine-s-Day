document.addEventListener("DOMContentLoaded", () => {
  const inputPersonalName = document.getElementById("inputPersonalName");
  const inputEmail = document.getElementById("inputEmail");
  const inputPartnerName = document.getElementById("inputPartnerName");
  const inputPassword = document.getElementById("inputPassword");
  const inputAnniversaryDate = document.getElementById("inputAnniversaryDate");
  const inputPhotos = document.getElementById("inputPhotos");
  const previewPhotos = document.getElementById("previewPhotos");

  const btnRegistrar = document.getElementById("btnRegistrar");
  const mensagemErroRegistro = document.getElementById("mensagemErroRegistro");
  const container = document.querySelector(".container");

  const API_BASE_URL = "http://localhost:8080/api/couples";

  const MIN_PHOTOS = 3;
  const MAX_PHOTOS = 6;

  // --- NOVO: Lista para armazenar os arquivos selecionados ---
  let filesToUpload = new DataTransfer(); // Usa DataTransfer para gerenciar FileList mutável

  // --- Lógica para exibir miniaturas e controlar o input ---
  inputPhotos.addEventListener("change", (event) => {
    // Adiciona os arquivos recém-selecionados à lista `filesToUpload`
    for (let i = 0; i < event.target.files.length; i++) {
      const file = event.target.files[i];

      // Verifica se o limite máximo será excedido antes de adicionar
      if (filesToUpload.files.length < MAX_PHOTOS) {
        filesToUpload.items.add(file);
      } else {
        // Se exceder, avisa e não adiciona este arquivo
        mensagemErroRegistro.textContent = `Máximo de ${MAX_PHOTOS} fotos atingido. Não foi possível adicionar mais.`;
        mensagemErroRegistro.style.display = "block";
        setTimeout(() => {
          mensagemErroRegistro.style.display = "none";
        }, 3000);
        break; // Sai do loop para não tentar adicionar mais
      }
    }

    // Atualiza o input.files para refletir a lista acumulada (opcional, mas mantém o input sincronizado)
    // inputPhotos.files = filesToUpload.files; // Pode causar erros em alguns navegadores, melhor evitar diretamente

    // Renderiza todas as miniaturas acumuladas
    renderThumbnails(filesToUpload.files);

    // Limpa o input de arquivo visualmente para permitir nova seleção (importante para o 'change' disparar novamente)
    inputPhotos.value = ""; // Limpa o valor do input, não os arquivos em 'filesToUpload'

    // Desabilita/Habilita o input se o máximo for atingido (para novas seleções)
    if (filesToUpload.files.length >= MAX_PHOTOS) {
      inputPhotos.disabled = true;
      // Opcional: Adicionar uma mensagem visual de que o máximo foi atingido
      mensagemErroRegistro.textContent = `Máximo de ${MAX_PHOTOS} fotos selecionadas.`;
      mensagemErroRegistro.style.display = "block";
      setTimeout(() => {
        mensagemErroRegistro.style.display = "none";
      }, 3000);
    } else {
      inputPhotos.disabled = false;
      // Esconde a mensagem de erro se a seleção estiver OK até o momento
      mensagemErroRegistro.style.display = "none";
    }
  });

  // Função para renderizar as miniaturas
  const renderThumbnails = (files) => {
    previewPhotos.innerHTML = ""; // Limpa tudo antes de renderizar
    for (let i = 0; i < files.length; i++) {
      const file = files[i];
      if (file.type.startsWith("image/")) {
        const reader = new FileReader();
        reader.onload = (e) => {
          const thumbnailContainer = document.createElement("div");
          thumbnailContainer.classList.add("thumbnail-container");

          const img = document.createElement("img");
          img.src = e.target.result;
          img.classList.add("thumbnail");
          thumbnailContainer.appendChild(img);

          const removeBtn = document.createElement("button");
          removeBtn.textContent = "X";
          removeBtn.classList.add("remove-thumbnail-btn");
          removeBtn.onclick = () => removeThumbnail(file, thumbnailContainer); // Passa o arquivo e o container
          thumbnailContainer.appendChild(removeBtn);

          previewPhotos.appendChild(thumbnailContainer);
        };
        reader.readAsDataURL(file);
      }
    }
  };

  // Função para remover uma miniatura
  const removeThumbnail = (fileToRemove, containerToRemove) => {
    const newDt = new DataTransfer();
    for (let i = 0; i < filesToUpload.files.length; i++) {
      const file = filesToUpload.files[i];
      if (file !== fileToRemove) {
        // Adiciona todos os arquivos EXCETO o que queremos remover
        newDt.items.add(file);
      }
    }
    filesToUpload = newDt; // Atualiza a lista global de arquivos

    containerToRemove.remove(); // Remove o elemento HTML da miniatura

    // Reabilita o input de fotos se o limite não for mais atingido
    if (filesToUpload.files.length < MAX_PHOTOS) {
      inputPhotos.disabled = false;
    }

    // Se houver uma mensagem de erro visível, esconde-a
    mensagemErroRegistro.style.display = "none";
  };

  // --- Lógica de Registro ---
  btnRegistrar.addEventListener("click", async () => {
    const personalName = inputPersonalName.value.trim();
    const email = inputEmail.value.trim();
    const partnerName = inputPartnerName.value.trim();
    const password = inputPassword.value.trim();
    const anniversaryDate = inputAnniversaryDate.value;
    const files = filesToUpload.files; // Usa a lista acumulada de arquivos

    // --- VALIDAÇÕES COMPLETAS NO FRONTEND (ANTES DE ENVIAR) ---

    // 1. Validação de campos de texto
    if (
      !personalName ||
      !email ||
      !partnerName ||
      !password ||
      !anniversaryDate
    ) {
      mensagemErroRegistro.textContent =
        "Por favor, preencha todos os campos de texto.";
      mensagemErroRegistro.style.display = "block";
      setTimeout(() => {
        mensagemErroRegistro.style.display = "none";
      }, 3000);
      return;
    }

    // 2. Validação de formato de Email (básica, o backend fará a mais robusta)
    const emailRegex = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,6}$/;
    if (!emailRegex.test(email)) {
      mensagemErroRegistro.textContent =
        "Por favor, insira um formato de e-mail válido.";
      mensagemErroRegistro.style.display = "block";
      setTimeout(() => {
        mensagemErroRegistro.style.display = "none";
      }, 3000);
      return;
    }

    // 3. Validação de Senha (mínimo de 8 caracteres)
    if (password.length < 8) {
      mensagemErroRegistro.textContent =
        "A senha deve ter no mínimo 8 caracteres.";
      mensagemErroRegistro.style.display = "block";
      setTimeout(() => {
        mensagemErroRegistro.style.display = "none";
      }, 3000);
      return;
    }

    // 4. Validação de Quantidade de Fotos (FINAL)
    if (files.length === 0) {
      mensagemErroRegistro.textContent =
        "Por favor, selecione fotos para o registro.";
      mensagemErroRegistro.style.display = "block";
      setTimeout(() => {
        mensagemErroRegistro.style.display = "none";
      }, 3000);
      return;
    }
    if (files.length < MIN_PHOTOS || files.length > MAX_PHOTOS) {
      mensagemErroRegistro.textContent = `Por favor, selecione entre ${MIN_PHOTOS} e ${MAX_PHOTOS} fotos (você selecionou ${files.length}).`;
      mensagemErroRegistro.style.display = "block";
      setTimeout(() => {
        mensagemErroRegistro.style.display = "none";
      }, 5000);
      return;
    }

    // --- Construção e Envio do FormData ---
    const formData = new FormData();
    const requestData = {
      personalName,
      email,
      partnerName,
      password,
      anniversaryDate,
    };
    formData.append(
      "request",
      new Blob([JSON.stringify(requestData)], { type: "application/json" })
    );

    // Anexe os arquivos da lista acumulada
    for (let i = 0; i < files.length; i++) {
      formData.append("files", files[i]);
    }

    try {
      const registerResponse = await fetch(`${API_BASE_URL}/register`, {
        method: "POST",
        body: formData,
      });

      if (registerResponse.ok) {
        const responseData = await registerResponse.json();
        alert(
          `Conta criada com sucesso! Anote o CÓDIGO SECRETO para o acesso: ${responseData.accessCode} (Válido por 6 horas). Use-o junto com seu e-mail.`
        );
        container.classList.add("fade-out");
        setTimeout(() => {
          window.location.href = "../landing-page/index.html";
        }, 800);
      } else {
        const errorData = await registerResponse.json().catch(() => ({}));
        let errorMessage = `Erro no registro: ${
          errorData.message || registerResponse.statusText || "Dados inválidos."
        }`;

        if (
          errorData.message &&
          errorData.message.includes("e-mail já está em uso")
        ) {
          errorMessage =
            "Este e-mail já está cadastrado. Por favor, tente fazer login ou use a opção 'Gerar Código Secreto' na página de login.";
        } else if (
          errorData.message &&
          (errorData.message.includes("selecione entre 3 e 6 fotos") ||
            errorData.message.includes("vazio"))
        ) {
          errorMessage = errorData.message;
        }

        mensagemErroRegistro.textContent = errorMessage;
        mensagemErroRegistro.style.display = "block";
        setTimeout(() => {
          mensagemErroRegistro.style.display = "none";
        }, 5000);
      }
    } catch (error) {
      console.error("Erro na comunicação com o backend:", error);
      mensagemErroRegistro.textContent =
        "Problema de conexão. Tente novamente mais tarde.";
      mensagemErroRegistro.style.display = "block";
      setTimeout(() => {
        mensagemErroRegistro.style.display = "none";
      }, 3000);
    }
  });

  const btnVoltarLogin = document.getElementById("btnVoltarLogin");
  btnVoltarLogin.addEventListener("click", () => {
    window.location.href = "../landing-page/index.html";
  });

  inputAnniversaryDate.addEventListener("keypress", (event) => {
    if (event.key === "Enter") btnRegistrar.click();
  });
  inputPersonalName.addEventListener("keypress", (event) => {
    if (event.key === "Enter") btnRegistrar.click();
  });
  inputEmail.addEventListener("keypress", (event) => {
    if (event.key === "Enter") btnRegistrar.click();
  });
  inputPartnerName.addEventListener("keypress", (event) => {
    if (event.key === "Enter") btnRegistrar.click();
  });
  inputPassword.addEventListener("keypress", (event) => {
    if (event.key === "Enter") btnRegistrar.click();
  });
});
