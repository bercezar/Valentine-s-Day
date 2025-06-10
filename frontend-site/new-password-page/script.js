document.addEventListener("DOMContentLoaded", () => {
  const inputEmailReset = document.getElementById("inputEmailReset");
  const inputPartnerNameReset = document.getElementById(
    "inputPartnerNameReset"
  );
  const inputNewPassword = document.getElementById("inputNewPassword");
  const btnResetPassword = document.getElementById("btnResetPassword");
  const btnVoltarTokenPage = document.getElementById("btnVoltarTokenPage");
  const btnVoltarLogin = document.getElementById("btnVoltarLogin");
  const mensagemResetStatus = document.getElementById("mensagemResetStatus");
  const mensagemErroReset = document.getElementById("mensagemErroReset");
  const container = document.querySelector(".container");

  const API_BASE_URL = "http://localhost:8080/api/couples";

  btnResetPassword.addEventListener("click", async () => {
    const email = inputEmailReset.value.trim();
    const partnerName = inputPartnerNameReset.value.trim();
    const newPassword = inputNewPassword.value.trim();

    if (!email || !partnerName || !newPassword) {
      mensagemErroReset.textContent = "Por favor, preencha todos os campos.";
      mensagemErroReset.style.display = "block";
      mensagemResetStatus.style.display = "none";
      setTimeout(() => {
        mensagemErroReset.style.display = "none";
      }, 3000);
      return;
    }

    try {
      const resetResponse = await fetch(
        `${API_BASE_URL}/reset-password?email=${encodeURIComponent(
          email
        )}&partnerName=${encodeURIComponent(
          partnerName
        )}&newPassword=${encodeURIComponent(newPassword)}`,
        {
          method: "PATCH",
          headers: {
            "Content-Type": "application/json",
          },
        }
      );

      if (resetResponse.ok) {
        mensagemResetStatus.textContent =
          "Senha redefinida com sucesso! Você já pode fazer login.";
        mensagemResetStatus.style.display = "block";
        mensagemErroReset.style.display = "none";

        inputEmailReset.value = "";
        inputPartnerNameReset.value = "";
        inputNewPassword.value = "";

        setTimeout(() => {
          window.location.href = "../landing-page/index.html";
        }, 3000);
      } else {
        const errorText = await resetResponse.text();
        mensagemErroReset.textContent = `Erro ao redefinir senha: ${
          errorText || resetResponse.statusText || "Erro no servidor."
        }`;
        mensagemErroReset.style.display = "block";
        mensagemResetStatus.style.display = "none";
        setTimeout(() => {
          mensagemErroReset.style.display = "none";
        }, 5000);
      }
    } catch (error) {
      console.error("Erro na comunicação com o backend:", error);
      mensagemErroReset.textContent =
        "Problema de conexão. Tente novamente mais tarde.";
      mensagemErroReset.style.display = "block";
      mensagemResetStatus.style.display = "none";
      setTimeout(() => {
        mensagemErroReset.style.display = "none";
      }, 3000);
    }
  });

  btnVoltarTokenPage.addEventListener("click", () => {
    window.location.href = "../token-page/index.html";
  });

  btnVoltarLogin.addEventListener("click", () => {
    window.location.href = "../landing-page/index.html";
  });

  inputNewPassword.addEventListener("keypress", (event) => {
    if (event.key === "Enter") btnResetPassword.click();
  });
  inputEmailReset.addEventListener("keypress", (event) => {
    if (event.key === "Enter") btnResetPassword.click();
  });
  inputPartnerNameReset.addEventListener("keypress", (event) => {
    if (event.key === "Enter") btnResetPassword.click();
  });
});
