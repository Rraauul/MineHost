import HttpService from "../service/httpService.js";
import AuthGuard from "./AuthGuard.js";

// Initialiser l'AuthGuard pour protéger cette page
new AuthGuard();

class CreateWorld {
  constructor() {
    this.httpService = new HttpService();
    this.apiBaseUrl = this.httpService.baseUrl;

    // Éléments du DOM
    this.worldNameInput = document.getElementById("world-name");
    this.templateSelect = document.getElementById("world-template");
    this.ramInput = document.getElementById("ram-input");
    this.serverIdInput = document.getElementById("server-id");
    this.createButton = document.getElementById("create-world-btn");
    this.statusMessage = document.getElementById("status-message");
    this.usernameDisplay = document.getElementById("username-display");

    this.userOffer = null;
    this.userWorlds = null;

    // Initialisation des événements
    this.createButton.addEventListener("click", () => this.createNewWorld());

    // Chargement initial des données
    this.loadUserInfo();
    this.loadTemplates();
    this.loadUserOffer();
    this.loadUserWorlds();
  }

  // Charger les informations de l'utilisateur
  async loadUserInfo() {
    // Récupérer le nom d'utilisateur depuis localStorage
    const username = localStorage.getItem("user");
    if (username) {
      this.usernameDisplay.textContent = username;
    } else {
      // Rediriger si aucun utilisateur n'est trouvé
      this.httpService.logout();
    }
  }
  async loadUserOffer() {
    try {
      const response = await fetch(`${this.apiBaseUrl}offerByClient`, {
        method: "GET",
        credentials: "include",
      });

      if (!response.ok) {
        if (response.status === 401) {
          this.httpService.logout();
          return;
        }
        throw new Error(`Erreur HTTP: ${response.status}`);
      }

      this.userOffer = await response.json();
      console.log("Offre utilisateur chargée:", this.userOffer);
    } catch (error) {
      console.error("Erreur lors du chargement de l'offre:", error);
      this.showStatus("Erreur lors du chargement de votre abonnement", "error");
    }
  }
  async loadUserWorlds() {
    try {
      const response = await fetch(`${this.apiBaseUrl}getWorldsByUserId`, {
        method: "GET",
        credentials: "include",
      });

      if (!response.ok) {
        if (response.status === 401) {
          this.httpService.logout();
          return;
        }
        throw new Error(`Erreur HTTP: ${response.status}`);
      }

      this.userWorlds = await response.json();
      console.log("Mondes utilisateur chargés:", this.userWorlds);
    } catch (error) {
      console.error("Erreur lors du chargement des mondes:", error);
    }
  }
  // Charger les templates disponibles
  async loadTemplates() {
    try {
      const response = await fetch(`${this.apiBaseUrl}getTemplates`, {
        method: "GET",
        credentials: "include", // Pour inclure les cookies de session
      });

      if (!response.ok) {
        if (response.status === 401) {
          // Rediriger vers la page de connexion
          this.httpService.logout();
          return;
        }
        throw new Error(`Erreur HTTP: ${response.status}`);
      }

      const templates = await response.json();

      // Vider le select
      this.templateSelect.innerHTML = "";

      // Remplir avec les templates
      templates.forEach((template) => {
        const option = document.createElement("option");
        option.value = template;
        option.textContent = template;
        this.templateSelect.appendChild(option);
      });

      // Si aucun template n'est retourné
      if (templates.length === 0) {
        const option = document.createElement("option");
        option.value = "";
        option.textContent = "Aucun template disponible";
        this.templateSelect.appendChild(option);
        this.createButton.disabled = true;
      }
    } catch (error) {
      console.error("Erreur lors du chargement des templates:", error);
      this.showStatus("Erreur lors du chargement des templates", "error");
    }
  }
  checkRamAvailability(requestedRam) {
    // Si les données de l'offre ne sont pas disponibles, on ne peut pas vérifier
    if (!this.userOffer || !this.userWorlds) {
      this.showStatus(
        "Impossible de vérifier votre abonnement, veuillez réessayer",
        "error"
      );
      return false;
    }

    // RAM totale disponible selon l'abonnement
    const totalRamAllowed = this.userOffer.ram || 0;

    // Calculer la RAM déjà utilisée par les mondes existants
    const usedRam = this.userWorlds.reduce((total, world) => {
      return total + (parseInt(world.ram) || 0);
    }, 0);

    // RAM demandée pour le nouveau monde
    const newWorldRam = parseInt(requestedRam);

    // Vérifier si la RAM totale après ajout du nouveau monde dépasse la limite
    if (usedRam + newWorldRam > totalRamAllowed) {
      this.showStatus(
        `Limite de RAM dépassée! Votre abonnement permet ${totalRamAllowed}GB au total. Vous utilisez déjà ${usedRam}GB. Vous ne pouvez pas ajouter ${newWorldRam}GB supplémentaires.`,
        "error"
      );
      return false;
    }

    return true;
  }

  // Créer un nouveau monde
  async createNewWorld() {
    // Valider les entrées
    const name = this.worldNameInput.value.trim();
    const template = this.templateSelect.value;
    const ram = parseInt(this.ramInput.value);
    const serverId = parseInt(this.serverIdInput.value);

    // Validation des champs
    if (!name) {
      this.showStatus("Veuillez entrer un nom pour le monde", "error");
      return;
    }

    if (!template) {
      this.showStatus("Veuillez sélectionner un template", "error");
      return;
    }

    if (isNaN(ram) || ram < 1 || ram > 16) {
      this.showStatus("La RAM doit être entre 1 et 16 GB", "error");
      return;
    }

    if (isNaN(serverId) || serverId < 1) {
      this.showStatus("Veuillez entrer un ID de serveur valide", "error");
      return;
    }
    if (!this.checkRamAvailability(ram)) {
      return;
    }

    // Désactiver le bouton pendant la création
    this.createButton.disabled = true;
    this.showStatus("Création du monde en cours...", "info");

    try {
      // Construire les paramètres pour l'API
      const params = new URLSearchParams({
        name: name,
        template: template,
        ram: ram,
        serverId: serverId,
      });

      const response = await fetch(`${this.apiBaseUrl}createWorld?${params}`, {
        method: "POST",
        credentials: "include", // Pour inclure les cookies de session
      });

      if (!response.ok) {
        if (response.status === 401) {
          // Rediriger vers la page de connexion
          this.httpService.logout();
          return;
        }

        const errorText = await response.text();
        throw new Error(errorText || `Erreur HTTP: ${response.status}`);
      }

      // Traiter la réponse
      const result = await response.json().catch(() => {
        return { message: "Monde créé avec succès!" };
      });

      this.showStatus(result.message || "Monde créé avec succès!", "success");

      // Rediriger vers la page des mondes après un délai
      setTimeout(() => {
        window.location.href = "my-worlds.html";
      }, 2000);
    } catch (error) {
      console.error("Erreur lors de la création du monde:", error);
      this.showStatus(
        error.message || "Erreur lors de la création du monde",
        "error"
      );
    } finally {
      // Réactiver le bouton
      this.createButton.disabled = false;
    }
  }

  // Afficher un message de statut
  showStatus(message, type = "info") {
    this.statusMessage.textContent = message;
    this.statusMessage.className = `status-message ${type}`;

    // Faire défiler jusqu'au message
    this.statusMessage.scrollIntoView({ behavior: "smooth" });
  }
}

// Initialiser la classe lorsque le DOM est chargé
document.addEventListener("DOMContentLoaded", () => new CreateWorld());
