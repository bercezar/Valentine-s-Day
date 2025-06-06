document.addEventListener("DOMContentLoaded", () => {
  const inputEmailGenerate = document.getElementById("inputEmailGenerate");
  const inputPasswordGenerate = document.getElementById(
    "inputPasswordGenerate"
  );
  const btnGenerateToken = document.getElementById("btnGenerateToken");
  const btnVoltarLogin = document.getElementById("btnVoltarLogin"); // Botão para voltar
  const mensagemTokenGerado = document.getElementById("mensagemTokenGerado");
  const mensagemErroGenerate = document.getElementById("mensagemErroGenerate"); // Mensagem de erro para esta página
  const container = document.querySelector(".container");

  const API_BASE_URL = "http://localhost:8080/api/couples";

  btnGenerateToken.addEventListener("click", async () => {
    const emailGenerate = inputEmailGenerate.value.trim();
    const passwordGenerate = inputPasswordGenerate.value.trim();

    if (!emailGenerate || !passwordGenerate) {
      mensagemErroGenerate.textContent =
        "Por favor, preencha seu e-mail e sua senha.";
      mensagemErroGenerate.style.display = "block";
      setTimeout(() => {
        mensagemErroGenerate.style.display = "none";
      }, 3000);
      return;
    }

    try {
      // Chamada para o endpoint de login (para autenticar o presenteador)
      const authResponse = await fetch(
        `${API_BASE_URL}/login?email=${encodeURIComponent(
          emailGenerate
        )}&password=${encodeURIComponent(passwordGenerate)}`
      );

      if (!authResponse.ok) {
        mensagemErroGenerate.textContent =
          "E-mail ou senha de cadastro incorretos.";
        mensagemErroGenerate.style.display = "block";
        setTimeout(() => {
          mensagemErroGenerate.style.display = "none";
        }, 3000);
        return;
      }

      // Se autenticado, chama o endpoint para gerar novo access code (PATCH)
      // Note: O endpoint generate-access-code no backend espera @PathVariable email e @RequestParam newAccessCode
      // Vamos gerar um novo UUID para o accessCode aqui para o PATCH
      const newAccessCode = Math.random()
        .toString(36)
        .substring(2, 10)
        .toUpperCase(); // Gera um código aleatório simples

      const generateResponse = await fetch(
        `${API_BASE_URL}/${encodeURIComponent(
          emailGenerate
        )}/generate-access-code?newAccessCode=${encodeURIComponent(
          newAccessCode
        )}`,
        {
          method: "PATCH", // Usamos PATCH para atualizar uma parte do recurso
          headers: {
            "Content-Type": "application/json", // Não tem body, mas bom indicar
          },
        }
      );

      if (generateResponse.ok) {
        const responseData = await generateResponse.json();
        mensagemTokenGerado.textContent = `Novo Código Secreto: ${
          responseData.accessCode
        }. Válido até: ${new Date(
          responseData.accessCodeExpiration
        ).toLocaleString()}`;
        mensagemTokenGerado.style.display = "block";
        mensagemErroGenerate.style.display = "none";

        inputEmailGenerate.value = "";
        inputPasswordGenerate.value = "";
      } else {
        const errorData = await generateResponse.json();
        mensagemErroGenerate.textContent = `Erro ao gerar código: ${
          errorData.message || "Erro no servidor."
        }`;
        mensagemErroGenerate.style.display = "block";
        setTimeout(() => {
          mensagemErroGenerate.style.display = "none";
        }, 5000);
      }
    } catch (error) {
      console.error("Erro na comunicação com o backend:", error);
      mensagemErroGenerate.textContent =
        "Problema de conexão. Tente novamente mais tarde.";
      mensagemErroGenerate.style.display = "block";
      setTimeout(() => {
        mensagemErroGenerate.style.display = "none";
      }, 3000);
    }
  });

  // Lógica para voltar para o login
  btnVoltarLogin.addEventListener("click", () => {
    window.location.href = "../landing-page/index.html";
  });

  // Event Listeners para 'Enter'
  inputEmailGenerate.addEventListener("keypress", (event) => {
    if (event.key === "Enter") btnGenerateToken.click();
  });
  inputPasswordGenerate.addEventListener("keypress", (event) => {
    if (event.key === "Enter") btnGenerateToken.click();
  });
});
