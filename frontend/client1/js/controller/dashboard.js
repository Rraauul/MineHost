import HttpService from "../service/httpService.js";
import AuthGuard from "./AuthGuard.js";

// Initialiser l'AuthGuard pour protéger cette page
new AuthGuard();

class Dashboard {
  constructor() {
    this.httpService = new HttpService();
    this.apiBaseUrl = this.httpService.baseUrl;

    // Éléments du DOM
    this.usernameDisplay = document.querySelector(".user-profile span");
    this.welcomeMessage = document.querySelector(".header-left h1");
    this.offerType = document.querySelector(".offer-card h3:first-child");
    this.offerStorage = document.querySelector(".offer-card h3:last-child");
    this.serverStatusContainer = document.querySelector(
      ".dashboard-card:nth-child(2) .card-content"
    );
    this.totalWorldsCount = document.querySelector(
      ".dashboard-card:nth-child(3) h2"
    );
    this.createServerBtn = document.getElementById("create-server-btn");
    this.logoutBtn = document.getElementById("logout-btn");

    // Initialisation des événements
    this.createServerBtn.addEventListener("click", () =>
      this.createNewServer()
    );
    this.logoutBtn.addEventListener("click", (e) => {
      e.preventDefault();
      this.httpService.logout();
    });

    // Chargement initial des données
    this.loadUserInfo();
    this.loadWorldsStatus();
    this.loadOfferInfo();
  }

  // Charger les informations de l'utilisateur
  async loadUserInfo() {
    // Récupérer le nom d'utilisateur depuis localStorage
    const username = localStorage.getItem("user");
    if (username) {
      this.usernameDisplay.textContent = username;
      this.welcomeMessage.textContent = `Welcome back, ${username}`;
    } else {
      // Rediriger si aucun utilisateur n'est trouvé
      this.httpService.logout();
    }
  }

  // Charger le statut des mondes
  async loadWorldsStatus() {
    try {
      const response = await fetch(`${this.apiBaseUrl}getWorldsByUserId`, {
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

      const worlds = await response.json();

      // Mettre à jour le nombre total de mondes
      this.totalWorldsCount.textContent = worlds.length;

      // Vider le conteneur de statut
      this.serverStatusContainer.innerHTML = "";

      // Remplir avec les statuts des mondes
      worlds.forEach((world) => {
        const isOnline =
          world.status &&
          (world.status.toUpperCase() === "ONLINE" ||
            world.status.toUpperCase() === "RUNNING" ||
            world.status.toLowerCase() === "running" ||
            world.status.toLowerCase() === "online");
        const statusElement = document.createElement("div");
        statusElement.className = "server-status";
        statusElement.innerHTML = `
          <span>${world.name}</span>
          <span class="status-icon ${isOnline ? "online" : "offline"}">${
          isOnline ? "✓" : "⊗"
        }</span>
        `;
        this.serverStatusContainer.appendChild(statusElement);
      });

      // Si aucun monde n'est trouvé
      if (worlds.length === 0) {
        const emptyElement = document.createElement("div");
        emptyElement.className = "server-status";
        emptyElement.innerHTML = `
          <span>Aucun monde trouvé</span>
        `;
        this.serverStatusContainer.appendChild(emptyElement);
      }
    } catch (error) {
      console.error("Erreur lors du chargement des statuts des mondes:", error);
      this.serverStatusContainer.innerHTML = `
        <div class="server-status">
          <span>Erreur de chargement</span>
        </div>
      `;
      this.totalWorldsCount.textContent = "N/A";
    }
  }

  // Charger les informations de l'offre
  async loadOfferInfo() {
    try {
      const response = await fetch(`${this.apiBaseUrl}offerByClient`, {
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

      const offerData = await response.json();

      // Mettre à jour les informations de l'offre
      if (offerData && offerData.nom) {
        this.offerType.textContent = offerData.nom;
      } else {
        this.offerType.textContent = "Free";
      }

      // Calculer l'espace disque utilisé
      await this.calculateStorageUsage(offerData);
    } catch (error) {
      console.error(
        "Erreur lors du chargement des informations de l'offre:",
        error
      );
      this.offerType.textContent = "Inconnu";
      this.offerStorage.textContent = "0Mo/0Go";
    }
  }

  // Calculer l'utilisation du stockage
  async calculateStorageUsage(offerData) {
    try {
      // Récupérer la liste des mondes pour calculer l'espace utilisé
      const response = await fetch(`${this.apiBaseUrl}getWorldsByUserId`, {
        method: "GET",
        credentials: "include",
      });

      if (!response.ok) {
        throw new Error(`Erreur HTTP: ${response.status}`);
      }

      const worlds = await response.json();

      // Calculer l'espace total utilisé en additionnant les tailles de tous les mondes
      let totalUsedSpace = 0;
      if (worlds && worlds.length > 0) {
        totalUsedSpace = worlds.reduce((total, world) => {
          // Prendre en compte le size en MB ou convertir depuis GB si nécessaire
          const worldSize = world.size
            ? world.size > 100
              ? world.size / 1024
              : world.size
            : 0; // Conversion si nécessaire
          return total + worldSize;
        }, 0);
      }

      const maxSpaceMo = offerData.maxStorage || offerData.stockage || 0;

      // Mettre à jour l'affichage
      this.offerStorage.textContent = `${totalUsedSpace}Go/${maxSpaceMo}Go`;
    } catch (error) {
      console.error(
        "Erreur lors du calcul de l'utilisation du stockage:",
        error
      );
      const maxSpaceMo = offerData.maxStorage || offerData.stockage || 0;
      this.offerStorage.textContent = `0Go/${maxSpaceMo}Go`;
    }
  }

  // Créer un nouveau serveur
  async createNewServer() {
    try {
      // Rediriger vers la page de création de monde
      window.location.href = "create-world.html";
    } catch (error) {
      console.error("Erreur lors de la redirection:", error);
      alert("Une erreur est survenue. Veuillez réessayer.");
    }
  }
}

// Initialiser la classe lorsque le DOM est chargé
document.addEventListener("DOMContentLoaded", () => new Dashboard());
