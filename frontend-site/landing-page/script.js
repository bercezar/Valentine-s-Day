document.addEventListener("DOMContentLoaded", () => {
  const inputEmailLogin = document.getElementById("inputEmailLogin");
  const inputAccessCodeLogin = document.getElementById("inputAccessCodeLogin");
  const btnLogin = document.getElementById("btnLogin");
  const btnIrParaRegistro = document.getElementById("btnIrParaRegistro");
  const btnIrParaGerarToken = document.getElementById("btnIrParaGerarToken"); // NOVO BOTÃO
  const mensagemErro = document.getElementById("mensagemErro");
  const container = document.querySelector(".container");

  const API_BASE_URL = "http://localhost:8080/api/couples";

  // --- Lógica para o Login ---
  btnLogin.addEventListener("click", async () => {
    const emailInput = inputEmailLogin.value.trim();
    const accessCodeInput = inputAccessCodeLogin.value.trim();

    if (!emailInput || !accessCodeInput) {
      mensagemErro.textContent =
        "Por favor, preencha o e-mail e o código secreto para login.";
      mensagemErro.style.display = "block";
      setTimeout(() => {
        mensagemErro.style.display = "none";
      }, 3000);
      return;
    }

    try {
      const loginResponse = await fetch(
        `${API_BASE_URL}/login?email=${encodeURIComponent(
          emailInput
        )}&accessCode=${encodeURIComponent(accessCodeInput)}`
      );

      if (loginResponse.ok) {
        const coupleData = await loginResponse.json();
        localStorage.setItem("currentUser", JSON.stringify(coupleData));
        container.classList.add("fade-out");
        setTimeout(() => {
          window.location.href = "../main-page/index.html";
        }, 800);
      } else {
        let errorText = "Erro ao tentar acessar. Tente novamente.";
        if (loginResponse.status === 401) {
          errorText = "E-mail ou código secreto incorretos.";
        } else if (loginResponse.status === 410) {
          errorText =
            "Código secreto expirado. Por favor, solicite um novo código.";
        }

        mensagemErro.textContent = errorText;
        mensagemErro.style.display = "block";
        inputEmailLogin.value = "";
        inputAccessCodeLogin.value = "";
        inputEmailLogin.focus();
        setTimeout(() => {
          mensagemErro.style.display = "none";
        }, 3000);
      }
    } catch (error) {
      console.error("Erro na comunicação com o backend:", error);
      mensagemErro.textContent =
        "Problema de conexão. Verifique se o backend está rodando.";
      mensagemErro.style.display = "block";
      setTimeout(() => {
        mensagemErro.style.display = "none";
      }, 3000);
    }
  });

  // --- Lógica para Ir para Registro ---
  btnIrParaRegistro.addEventListener("click", () => {
    window.location.href = "../registro-page/index.html";
  });

  // --- Lógica para Ir para Gerar Token (NOVO) ---
  btnIrParaGerarToken.addEventListener("click", () => {
    window.location.href = "../gerar-token-page/index.html"; // Redireciona para a nova página
  });

  // --- Event Listeners para 'Enter' ---
  inputEmailLogin.addEventListener("keypress", (event) => {
    if (event.key === "Enter") btnLogin.click();
  });
  inputAccessCodeLogin.addEventListener("keypress", (event) => {
    if (event.key === "Enter") btnLogin.click();
  });
});
