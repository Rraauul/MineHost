import HttpService from "../service/httpService.js";
import AuthGuard from "./AuthGuard.js";

// Initialiser l'AuthGuard pour protéger cette page
new AuthGuard();

class MyWorlds {
  constructor() {
    this.httpService = new HttpService();
    this.apiBaseUrl = this.httpService.baseUrl;

    // Éléments du DOM
    this.toggleSidebar = document.getElementById("toggle-sidebar");
    this.sidebar = document.getElementById("sidebar");
    this.logoutBtn = document.getElementById("logout-btn");
    this.worldListContainer = document.querySelector(".world-list");
    this.usageInfo = document.querySelector(".usage-info");
    this.usernameDisplay = document.querySelector(".user-profile span");

    // Initialisation des événements
    this.initEventListeners();

    // Chargement initial des données
    this.loadUserInfo();
    this.loadUserWorlds();
    this.loadUserOffer();
  }

  // Initialiser les écouteurs d'événements
  initEventListeners() {
    // Toggle sidebar
    this.toggleSidebar.addEventListener("click", () =>
      this.toggleSidebarHandler()
    );

    // Logout
    this.logoutBtn.addEventListener("click", (e) => {
      e.preventDefault();
      this.httpService.logout();
    });
  }

  // Gérer le toggle du sidebar
  toggleSidebarHandler() {
    this.sidebar.classList.toggle("collapsed");

    // Mettre à jour l'icône du bouton
    if (this.sidebar.classList.contains("collapsed")) {
      this.toggleSidebar.querySelector("span").textContent = "▶";
    } else {
      this.toggleSidebar.querySelector("span").textContent = "◀";
    }
  }

  // Charger les informations de l'utilisateur
  async loadUserInfo() {
    // Récupérer le nom d'utilisateur depuis localStorage
    const username = localStorage.getItem("user");
    if (username && this.usernameDisplay) {
      this.usernameDisplay.textContent = username;
    } else {
      // Rediriger si aucun utilisateur n'est trouvé
      this.httpService.logout();
    }
  }

  // Charger les mondes de l'utilisateur
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

      const worlds = await response.json();

      // Vider la liste existante
      this.worldListContainer.innerHTML = "";

      // Vérifier si la liste des mondes est vide
      if (!worlds || worlds.length === 0) {
        this.showEmptyWorldsMessage();
      } else {
        // Pour chaque monde, obtenir les informations les plus récentes
        for (const world of worlds) {
          // Obtenir les informations à jour pour ce monde spécifique
          try {
            const worldInfoResponse = await fetch(
              `${this.apiBaseUrl}infoWorld?worldId=${world.id}`,
              {
                method: "GET",
                credentials: "include",
              }
            );

            if (worldInfoResponse.ok) {
              const worldInfo = await worldInfoResponse.json();
              if (worldInfo && worldInfo.length > 0) {
                // Utiliser les informations les plus récentes
                this.addWorldToList(worldInfo[0]);
              } else {
                // Utiliser les informations de la liste initiale si aucune info spécifique n'est disponible
                this.addWorldToList(world);
              }
            } else {
              // En cas d'erreur, utiliser les données de la liste initiale
              this.addWorldToList(world);
            }
          } catch (error) {
            console.error(
              `Erreur lors de la récupération des infos pour le monde ${world.id}:`,
              error
            );
            this.addWorldToList(world);
          }
        }
      }

      // Mettre à jour l'espace utilisé
      this.updateSpaceUsage(worlds || []);
    } catch (error) {
      console.error("Erreur lors du chargement des mondes:", error);
      this.showStatus("Erreur lors du chargement des mondes", "error");
      // Afficher le message d'absence de mondes en cas d'erreur
      this.showEmptyWorldsMessage();
    }
  }
  // Afficher un message quand aucun monde n'est disponible
  showEmptyWorldsMessage() {
    this.worldListContainer.innerHTML = `
      <div class="empty-worlds-message">
        <div class="empty-state">
          <div class="empty-icon">☁</div>
          <h3>Vous n'avez pas encore de mondes</h3>
          <p>Créez votre premier monde pour commencer à jouer !</p>
          <a href="create-world.html" class="create-world-btn">Créer un monde</a>
        </div>
      </div>
    `;
  }

  // Ajouter un monde à la liste
  addWorldToList(world) {
    const worldItem = document.createElement("div");
    worldItem.className = "world-item";
    worldItem.dataset.worldId = world.id;

    // Formater la date pour plus de lisibilité
    const creationDate = new Date(world.dateCreation);
    const formattedDate =
      creationDate.toLocaleDateString() +
      " " +
      creationDate.toLocaleTimeString();

    // Déterminer la classe de statut pour le style
    const statusClass =
      world.status && world.status.toLowerCase() === "running"
        ? "status-running"
        : "status-stopped";

    // Vérification plus robuste pour l'adresse ngrok
    const serverStatus =
      world.status && world.status.toLowerCase() === "running"
        ? world.addressNgrok
          ? `IP: ${world.addressNgrok}`
          : "Server online (IP not available)"
        : "Server offline";

    worldItem.innerHTML = `
      <div class="world-info">
        <div class="world-status">
          <span class="world-name ${statusClass}">${world.name} is ${
      world.status || "unknown"
    }</span>
          <div class="world-controls">
            ${
              !world.status || world.status.toLowerCase() === "stopped"
                ? '<button class="control-btn play-btn" title="Start world">▶</button>'
                : '<button class="control-btn stop-btn" title="Stop world">■</button>'
            }
            <button class="control-btn delete-btn" title="Delete world">🗑</button>
            <button class="control-btn console-btn" title="View console">≡</button>
            <button class="control-btn download-btn" title="Download logs">↓</button>
          </div>
        </div>
        <div class="world-details">
          <p>Size: ${world.size ? world.size.toFixed(2) : "0.00"} GB</p>
          <p>RAM: ${world.ram || "0"} GB</p>
          <p>Template: ${world.template || "N/A"}</p>
          <p>Created: ${formattedDate}</p>
        </div>
        <div class="server-ip">
          <span>${serverStatus}</span>
        </div>
      </div>
    `;

    // Ajouter les écouteurs d'événements aux boutons de contrôle
    this.attachWorldControlListeners(worldItem, world.id);

    // Ajouter le monde à la liste
    this.worldListContainer.appendChild(worldItem);
  }

  // Attacher les écouteurs d'événements aux boutons de contrôle
  attachWorldControlListeners(worldItem, worldId) {
    // Bouton démarrer monde
    const playBtn = worldItem.querySelector(".play-btn");
    if (playBtn) {
      playBtn.addEventListener("click", () => this.startWorld(worldId));
    }

    // Bouton arrêter monde
    const stopBtn = worldItem.querySelector(".stop-btn");
    if (stopBtn) {
      stopBtn.addEventListener("click", () => this.stopWorld(worldId));
    }

    // Bouton supprimer monde
    const deleteBtn = worldItem.querySelector(".delete-btn");
    if (deleteBtn) {
      deleteBtn.addEventListener("click", () => this.deleteWorld(worldId));
    }

    // Bouton console (pour implémentation future)
    const consoleBtn = worldItem.querySelector(".console-btn");
    if (consoleBtn) {
      consoleBtn.addEventListener("click", () => this.viewConsole(worldId));
    }

    // Bouton télécharger logs
    const downloadBtn = worldItem.querySelector(".download-btn");
    if (downloadBtn) {
      downloadBtn.addEventListener("click", () => this.downloadLogs(worldId));
    }
  }

  // Démarrer un monde
  async startWorld(worldId) {
    try {
      const response = await fetch(
        `${this.apiBaseUrl}startWorld?worldId=${worldId}`,
        {
          method: "POST",
          credentials: "include",
        }
      );

      if (!response.ok) {
        if (response.status === 401) {
          this.httpService.logout();
          return;
        }
        throw new Error(`Erreur HTTP: ${response.status}`);
      }

      // Rafraîchir les informations du monde pour obtenir l'adresse IP
      await this.refreshWorldInfo(worldId);
      this.showStatus(`Démarrage du monde en cours...`, "info");
    } catch (error) {
      console.error("Erreur lors du démarrage du monde:", error);
      this.showStatus("Erreur lors du démarrage du monde", "error");
    }
  }

  // Arrêter un monde
  async stopWorld(worldId) {
    try {
      const response = await fetch(
        `${this.apiBaseUrl}stopWorld?worldId=${worldId}`,
        {
          method: "POST",
          credentials: "include",
        }
      );

      if (!response.ok) {
        if (response.status === 401) {
          this.httpService.logout();
          return;
        }
        throw new Error(`Erreur HTTP: ${response.status}`);
      }

      // Rafraîchir les informations du monde
      this.refreshWorldInfo(worldId);
      this.showStatus(`Arrêt du monde en cours...`, "info");
    } catch (error) {
      console.error("Erreur lors de l'arrêt du monde:", error);
      this.showStatus("Erreur lors de l'arrêt du monde", "error");
    }
  }

  // Supprimer un monde (avec confirmation)
  async deleteWorld(worldId) {
    if (
      confirm(
        "Êtes-vous sûr de vouloir supprimer ce monde ? Cette action est irréversible."
      )
    ) {
      try {
        const response = await fetch(
          `${this.apiBaseUrl}deleteWorld?worldId=${worldId}`,
          {
            method: "DELETE",
            credentials: "include",
          }
        );

        if (!response.ok) {
          if (response.status === 401) {
            this.httpService.logout();
            return;
          }
          throw new Error(`Erreur HTTP: ${response.status}`);
        }

        // Supprimer le monde de la liste
        const worldItem = document.querySelector(
          `.world-item[data-world-id="${worldId}"]`
        );
        if (worldItem) {
          worldItem.remove();
        }

        // Recharger tous les mondes pour mettre à jour les informations d'utilisation
        this.loadUserWorlds();
        this.showStatus("Monde supprimé avec succès", "success");
      } catch (error) {
        console.error("Erreur lors de la suppression du monde:", error);
        this.showStatus("Erreur lors de la suppression du monde", "error");
      }
    }
  }

  // Voir la console du monde (fonction placeholder)
  viewConsole(worldId) {
    alert(
      "La fonctionnalité de console sera implémentée dans une future mise à jour."
    );
    // L'implémentation future pourrait ouvrir une modal avec la sortie de la console
  }

  // Télécharger les logs du monde
  downloadLogs(worldId) {
    window.location.href = `${this.apiBaseUrl}downloadLog?worldId=${worldId}`;
  }

  // Rafraîchir les informations d'un monde spécifique
  async refreshWorldInfo(worldId) {
    try {
      const response = await fetch(
        `${this.apiBaseUrl}infoWorld?worldId=${worldId}`,
        {
          method: "GET",
          credentials: "include",
        }
      );

      if (!response.ok) {
        if (response.status === 401) {
          this.httpService.logout();
          return;
        }
        throw new Error(`Erreur HTTP: ${response.status}`);
      }

      const worldInfo = await response.json();

      if (worldInfo && worldInfo.length > 0) {
        const world = worldInfo[0];

        // Trouver l'élément du monde dans le DOM
        const worldItem = document.querySelector(
          `.world-item[data-world-id="${worldId}"]`
        );
        if (worldItem) {
          // Mettre à jour le statut et les contrôles du monde
          const worldNameSpan = worldItem.querySelector(".world-name");
          worldNameSpan.textContent = `${world.name} is ${world.status}`;
          worldNameSpan.className = `world-name ${
            world.status.toLowerCase() === "running"
              ? "status-running"
              : "status-stopped"
          }`;

          // Mettre à jour les contrôles
          const controlsDiv = worldItem.querySelector(".world-controls");
          if (world.status.toLowerCase() === "running") {
            controlsDiv.innerHTML = `
              <button class="control-btn stop-btn" title="Stop world">■</button>
              <button class="control-btn delete-btn" title="Delete world">🗑</button>
              <button class="control-btn console-btn" title="View console">≡</button>
              <button class="control-btn download-btn" title="Download logs">↓</button>
            `;
          } else {
            controlsDiv.innerHTML = `
              <button class="control-btn play-btn" title="Start world">▶</button>
              <button class="control-btn delete-btn" title="Delete world">🗑</button>
              <button class="control-btn console-btn" title="View console">≡</button>
              <button class="control-btn download-btn" title="Download logs">↓</button>
            `;
          }

          // Mettre à jour les détails du monde
          const detailsDiv = worldItem.querySelector(".world-details");
          detailsDiv.innerHTML = `
            <p>Size: ${world.size ? world.size.toFixed(2) : "0.00"} GB</p>
            <p>RAM: ${world.ram || "0"} GB</p>
            <p>Template: ${world.template || "N/A"}</p>
            <p>Created: ${new Date(
              world.dateCreation
            ).toLocaleDateString()} ${new Date(
            world.dateCreation
          ).toLocaleTimeString()}</p>
          `;

          // Mettre à jour l'IP du serveur - Afficher l'adresse IP si elle existe et si le serveur est en cours d'exécution
          // Dans la partie qui met à jour l'IP du serveur
          const serverIpDiv = worldItem.querySelector(".server-ip");
          serverIpDiv.innerHTML = `
  <span>${
    world.status && world.status.toLowerCase() === "running"
      ? world.addressNgrok
        ? `IP: ${world.addressNgrok}`
        : "Server online (IP not available)"
      : "Server offline"
  }</span>
`;

          // Rattacher les écouteurs d'événements
          this.attachWorldControlListeners(worldItem, worldId);
        }
      }
    } catch (error) {
      console.error(
        "Erreur lors du rafraîchissement des informations du monde:",
        error
      );
    }
  }

  // Charger les informations d'offre de l'utilisateur
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

      const offer = await response.json();

      if (offer) {
        // Calculer l'utilisation du disque par l'utilisateur à partir de ses mondes
        const worldsResponse = await fetch(
          `${this.apiBaseUrl}getWorldsByUserId`,
          {
            method: "GET",
            credentials: "include",
          }
        );

        if (worldsResponse.ok) {
          const worlds = await worldsResponse.json();
          const totalUsedSpace =
            worlds && worlds.length > 0
              ? worlds.reduce((total, world) => total + (world.size || 0), 0)
              : 0;

          // Mettre à jour les informations d'utilisation avec les détails de l'offre
          this.usageInfo.innerHTML = `
            <h3>Space Used: ${totalUsedSpace.toFixed(2)}GB/${
            offer.stockage || "0"
          }GB</h3>
            <p>Your Plan: ${offer.nom || "Free"} (${offer.ram || "0"}GB RAM, ${
            offer.stockage || "0"
          }GB Storage)</p>
            <p>Monthly Price: $${(offer.prix || 0).toFixed(2)}</p>
          `;
        } else {
          // En cas d'erreur lors de la récupération des mondes
          this.usageInfo.innerHTML = `
            <h3>Space Used: 0.00GB/${offer.stockage || "0"}GB</h3>
            <p>Your Plan: ${offer.nom || "Free"} (${offer.ram || "0"}GB RAM, ${
            offer.stockage || "0"
          }GB Storage)</p>
            <p>Monthly Price: $${(offer.prix || 0).toFixed(2)}</p>
          `;
        }
      } else {
        // Si aucune offre n'est retournée
        this.usageInfo.innerHTML = `
          <h3>Space Used: 0.00GB/0GB</h3>
          <p>Your Plan: Free</p>
          <p>Monthly Price: $0.00</p>
        `;
      }
    } catch (error) {
      console.error("Erreur lors du chargement de l'offre:", error);
      // Afficher des informations par défaut en cas d'erreur
      this.usageInfo.innerHTML = `
        <h3>Space Used: 0.00GB/0GB</h3>
        <p>Your Plan: Free</p>
        <p>Monthly Price: $0.00</p>
      `;
    }
  }

  // Obtenir les informations du serveur
  async getServerInfo() {
    try {
      const response = await fetch(`${this.apiBaseUrl}infoServer?serverId=1`, {
        method: "GET",
        credentials: "include",
      });

      if (!response.ok) {
        if (response.status === 401) {
          this.httpService.logout();
          return null;
        }
        throw new Error(`Erreur HTTP: ${response.status}`);
      }

      return await response.json();
    } catch (error) {
      console.error(
        "Erreur lors de l'obtention des informations du serveur:",
        error
      );
      return null;
    }
  }

  // Mettre à jour l'affichage de l'utilisation de l'espace en fonction des mondes
  updateSpaceUsage(worlds) {
    // Calculer l'espace total utilisé en additionnant toutes les tailles de monde
    const totalSize =
      worlds && worlds.length > 0
        ? worlds.reduce((total, world) => total + (world.size || 0), 0)
        : 0;

    // Mettre à jour dans le contexte des informations d'offre
    this.loadUserOffer();
  }

  // Afficher un message de statut
  showStatus(message, type = "info") {
    // Vérifier si l'élément de message de statut existe, sinon le créer
    let statusMessage = document.getElementById("status-message");
    if (!statusMessage) {
      statusMessage = document.createElement("div");
      statusMessage.id = "status-message";
      document.querySelector(".worlds-container").appendChild(statusMessage);
    }

    statusMessage.textContent = message;
    statusMessage.className = `status-message ${type}`;

    // Faire défiler jusqu'au message
    statusMessage.scrollIntoView({ behavior: "smooth" });

    // Effacer le message après un certain délai pour les messages de succès
    if (type === "success") {
      setTimeout(() => {
        statusMessage.textContent = "";
        statusMessage.className = "status-message";
      }, 3000);
    }
  }
}

// Initialiser la classe lorsque le DOM est chargé
document.addEventListener("DOMContentLoaded", () => new MyWorlds());
