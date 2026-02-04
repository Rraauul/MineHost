import HttpService from "../service/httpService.js";

class AuthGuard {
  constructor() {
    this.httpService = new HttpService();
    this.init();
  }

  init() {
    // Vérifier si l'utilisateur est connecté
    const isAuthenticated = this.httpService.checkAuthentication();

    // Obtenir le chemin actuel
    const currentPath = window.location.pathname;

    // Pages qui ne nécessitent pas d'authentification
    const publicPages = ["/frontend/client1/index.html"];

    // Vérifier si la page actuelle requiert une authentification
    const requiresAuth = !publicPages.includes(currentPath);

    if (requiresAuth && !isAuthenticated) {
      // Rediriger vers la page de connexion si non authentifié
      console.log("Accès non autorisé, redirection vers la page de connexion");
      window.location.href = "/frontend/client1/index.html";
    } else if (!requiresAuth && isAuthenticated && currentPath !== "/frontend/client1/index.html") {
      // Rediriger vers le dashboard si déjà authentifié et sur une page publique
      console.log("Déjà connecté, redirection vers le dashboard");
      window.location.href = "/frontend/client1/views/dashboard.html";
    }

    // Configurer les événements de déconnexion
    const logoutBtn = document.getElementById("logout-btn");
    if (logoutBtn) {
      logoutBtn.addEventListener("click", () => this.logout());
    }
  }

  logout() {
    this.httpService
      .logout()
      .then(() => {
        console.log("Déconnexion réussie");
      })
      .catch((error) => {
        console.error("Erreur lors de la déconnexion:", error);
        alert("Erreur lors de la déconnexion. Veuillez réessayer.");
      });
  }
}

// Initialiser l'AuthGuard
document.addEventListener("DOMContentLoaded", () => new AuthGuard());

export default AuthGuard;
