class HttpService {
  constructor() {
    this.baseUrl = "https://apigw.fuentesr.emf-informatique.ch/";
    this.isAuthenticated = this.checkAuthentication();
  }

  checkAuthentication() {
    return !!localStorage.getItem("user") || this.hasCookie("JSESSIONID");
  }
  hasCookie(name) {
    return document.cookie.split(";").some((c) => {
      return c.trim().startsWith(name + "=");
    });
  }
  async login(username, password) {
    try {
      const response = await fetch(this.baseUrl + "login", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          name: username,
          motDePasse: password,
        }),
        credentials: "include", // Important pour recevoir et envoyer les cookies
      });

      if (!response.ok) {
        const errorText = await response.text();
        let errorMessage;
        try {
          const errorData = JSON.parse(errorText);
          errorMessage =
            errorData?.message || `Erreur HTTP: ${response.status}`;
        } catch {
          errorMessage = errorText || `Erreur HTTP: ${response.status}`;
        }
        throw new Error(errorMessage);
      }

      // Stocker les infos utilisateur
      localStorage.setItem("user", username);

      // Vérifier le type de contenu
      const contentType = response.headers.get("content-type");
      if (contentType && contentType.includes("application/json")) {
        return await response.json();
      } else {
        // Si ce n'est pas du JSON, retourner un objet avec le texte
        const text = await response.text();
        return { message: text };
      }
    } catch (error) {
      console.error("Erreur dans HttpService.login:", error);
      throw error;
    }
  }
  async logout() {
    try {
      // Appel au backend pour se déconnecter (si nécessaire)
      const response = await fetch(this.baseUrl + "logout", {
        method: "POST",
        credentials: "include",
      }).catch(() => {
        // Si l'appel échoue, on continue quand même
        console.warn(
          "Échec de l'appel à l'API logout, nettoyage local uniquement"
        );
      });

      // Nettoyage local (toujours effectué, même si l'appel API échoue)
      localStorage.removeItem("user");
      localStorage.removeItem("authToken");

      // Redirection vers la page de connexion
      window.location.href = "/133/index.html";

      return true;
    } catch (error) {
      console.error("Erreur lors de la déconnexion:", error);
      throw error;
    }
  }

  // Vérifie si l'utilisateur est authentifié, sinon redirige
  ensureAuthenticated() {
    if (!this.checkAuthentication()) {
      window.location.href = "/133/index.html";

      return false;
    }
    return true;
  }
}

export default HttpService;
