document.addEventListener("DOMContentLoaded", () => {
  const inputEmailGenerate = document.getElementById("inputEmailGenerate");
  const inputPasswordGenerate = document.getElementById(
    "inputPasswordGenerate"
  );
  const btnGenerateToken = document.getElementById("btnGenerateToken");
  const btnVoltarLogin = document.getElementById("btnVoltarLogin");
  const mensagemTokenGerado = document.getElementById("mensagemTokenGerado");
  const mensagemErroGenerate = document.getElementById("mensagemErroGenerate");
  const container = document.querySelector(".container");

  const API_BASE_URL = "http://localhost:8080/api/couples";

  btnGenerateToken.addEventListener("click", async () => {
    const emailGenerate = inputEmailGenerate.value.trim();
    const passwordGenerate = inputPasswordGenerate.value.trim();

    if (!emailGenerate || !passwordGenerate) {
      mensagemErroGenerate.textContent =
        "Por favor, preencha seu e-mail e sua senha.";
      mensagemErroGenerate.style.display = "block";
      mensagemTokenGerado.style.display = "none";
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
        const errorData = await authResponse.json().catch(() => ({}));
        let errorMessage = `E-mail ou senha de cadastro incorretos.`;
        if (errorData && errorData.message) {
          errorMessage = errorData.message;
        }
        mensagemErroGenerate.textContent = errorMessage;
        mensagemErroGenerate.style.display = "block";
        mensagemTokenGerado.style.display = "none";
        setTimeout(() => {
          mensagemErroGenerate.style.display = "none";
        }, 3000);
        return;
      }

      const newAccessCode = UUID.randomUUID()
        .toString()
        .substring(2, 10)
        .toUpperCase();

      const generateResponse = await fetch(
        `${API_BASE_URL}/${encodeURIComponent(
          emailGenerate
        )}/generate-access-code?newAccessCode=${encodeURIComponent(
          newAccessCode
        )}`,
        {
          method: "PATCH",
          headers: {
            "Content-Type": "application/json",
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
        const errorData = await generateResponse.json().catch(() => ({}));
        mensagemErroGenerate.textContent = `Erro ao gerar código: ${
          errorData.message ||
          generateResponse.statusText ||
          "Erro no servidor."
        }`;
        mensagemErroGenerate.style.display = "block";
        mensagemTokenGerado.style.display = "none";
        setTimeout(() => {
          mensagemErroGenerate.style.display = "none";
        }, 5000);
      }
    } catch (error) {
      console.error("Erro na comunicação com o backend:", error);
      mensagemErroGenerate.textContent =
        "Problema de conexão. Tente novamente mais tarde.";
      mensagemErroGenerate.style.display = "block";
      mensagemTokenGerado.style.display = "none";
      setTimeout(() => {
        mensagemErroGenerate.style.display = "none";
      }, 3000);
    }
  });

  btnVoltarLogin.addEventListener("click", () => {
    window.location.href = "../landing-page/index.html";
  });

  inputEmailGenerate.addEventListener("keypress", (event) => {
    if (event.key === "Enter") btnGenerateToken.click();
  });
  inputPasswordGenerate.addEventListener("keypress", (event) => {
    if (event.key === "Enter") btnGenerateToken.click();
  });
});
