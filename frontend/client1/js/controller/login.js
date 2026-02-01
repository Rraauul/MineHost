import HttpService from "../service/httpService.js";

class Login {
  constructor() {
    this.http = new HttpService();
    this.loginForm = document.getElementById("loginForm");
    this.loginMessage = document.getElementById("login-message");

    // Vérifier si déjà connecté
    if (this.http.checkAuthentication()) {
      window.location.href = "views/dashboard.html";
      return;
    }

    this.init();
  }

  init() {
    this.loginForm.addEventListener("submit", (event) =>
      this.handleSubmit(event)
    );

    // Option "Se souvenir de moi"
    const rememberCheckbox = document.getElementById("remember");
    if (rememberCheckbox) {
      // Restaurer l'état précédent
      rememberCheckbox.checked = localStorage.getItem("rememberMe") === "true";

      // Restaurer le nom d'utilisateur si "Se souvenir de moi" est coché
      if (rememberCheckbox.checked && localStorage.getItem("rememberedUser")) {
        document.getElementById("username").value =
          localStorage.getItem("rememberedUser");
      }
    }
  }

  handleSubmit(event) {
    event.preventDefault();

    const username = document.getElementById("username").value.trim();
    const password = document.getElementById("password").value.trim();
    const rememberMe = document.getElementById("remember")?.checked || false;

    this.showMessage("Connexion en cours...", "login-message");

    // Sauvegarder les préférences de connexion
    if (rememberMe) {
      localStorage.setItem("rememberMe", "true");
      localStorage.setItem("rememberedUser", username);
    } else {
      localStorage.removeItem("rememberMe");
      localStorage.removeItem("rememberedUser");
    }

    this.http
      .login(username, password)
      .then((data) => {
        // Stockez le token/session si nécessaire
        if (data.token) {
          localStorage.setItem("authToken", data.token);
        }

        this.showMessage(
          "Connexion réussie. Redirection...",
          "login-message success"
        );
        setTimeout(() => {
          window.location.href = "views/dashboard.html";
        }, 1500);
      })
      .catch((error) => {
        console.error("Erreur détaillée:", error);
        this.showMessage(
          error.message || "Identifiants incorrects",
          "login-message error"
        );
      });
  }

  showMessage(message, className) {
    this.loginMessage.textContent = message;
    this.loginMessage.className = className;
  }
}

// Initialiser la classe Login lorsque le DOM est chargé
document.addEventListener("DOMContentLoaded", () => new Login());

export default Login;
