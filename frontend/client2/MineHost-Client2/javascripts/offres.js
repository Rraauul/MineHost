class Offres {
    constructor() {
        document.addEventListener("DOMContentLoaded", () => {
        });
        window.offresInstance = this;
        this.storageServer = null ;
        this.ramServer =null ;
      //  let outils = new Utils();
        offresInstance.loadOffer();
    //    let estlogin = false;
    this.refreshStorage(() => {
        this.loadOffer();
    });
    
         console.log("Offres instance created");
    }

    loadOffer() {
        let serv = new ServiceHttp();
        serv.infoOffer(offresInstance.successLoadOffer, offresInstance.erreurLoadOffer);
    }
    successLoadOffer(data) {
        let parsedData;
        try {
            parsedData = JSON.parse(data); // On parse la chaîne JSON
        } catch (error) {
            console.error("Erreur lors du parsing JSON :", error);
            return;
        }
        const container = $("#offresContainer");
        container.empty();  // Vide le container avant d'ajouter les nouvelles offres
        
        parsedData.forEach((offre) => {
            let cardHtml = null;
            if(offre.stockage>offresInstance.storageServer || offre.ram>offresInstance.ramServer){
                cardHtml = offresInstance.creerOffreHtmlPasDispo(offre)
            }else{
                cardHtml = offresInstance.creerOffreHtmlDispo(offre);
            }
           
            container.append(cardHtml);  // Ajoute la carte dans le même container
        });
    }

    creerOffreHtmlDispo(offre) {
        return `
            <div class="offre-card">
                <h3>${offre.nom}</h3>
                <div class="price">${offre.prix} <img src="images/emeraude.png" alt="Emeraude" width="16"> /mois</div>
                <div class="details">
                    <p><strong>Stockage :</strong> ${offre.stockage} Go</p>
                    <p><strong>Ram :</strong> ${offre.ram} Go</p>
                    <p>Avec une IP dédiée !</p>
                </div>
                <button class="commander" onClick="offresInstance.commander(${offre.id})">Commander</button>
            </div>
        `;
    }
    creerOffreHtmlPasDispo(offre) {
        return `
            <div class="offre-card">
                <h3>${offre.nom}</h3>
                <div class="price">${offre.prix} <img src="images/emeraude.png" alt="Emeraude" width="16"> /mois</div>
                <div class="details">
                    <p><strong>Stockage :</strong> ${offre.stockage} Go</p>
                    <p><strong>Ram :</strong> ${offre.ram} Go</p>
                    <p>Avec une IP dédiée !</p>
                </div>
                <p class="unavailable">Offre indisponible</p>
            </div>
        `;
    }
    refreshStorage(callback) {
        let serv = new ServiceHttp();
        serv.infoStockage(
            (data) => this.refreshOK(data, callback), 
            this.refreshError
        );
    }
    refreshOK(data, callback) {
        console.log("refreshOK success");
        const parsedData = JSON.parse(data);
        const serverInfo = parsedData[0];
        this.ramServer = serverInfo.ram;
        this.storageServer = serverInfo.disk_available;
    
        console.log("RAM:", this.ramServer);
        console.log("Storage dispo:", this.storageServer);
    
        // Callback une fois que les données sont là
        if (callback) callback();
    }
    
    
    refreshError(data) {
        console.log("erreur refresh :", data);
    }

    erreurLoadOffer(data, xhr, status, error) {
        console.log("data :", data);
    }

    commander(pkAbo) {
        const user =  utils.getUser();
        console.log("Vous avez acheté l'abo : " + pkAbo);
       // console.log("avec l'user : " + user.pk);
        
        
        if (user != null) {
            console.log("Utilisateur connecté : " + user.name);
            let serv = new ServiceHttp();
            serv.buyAbo(user.id, pkAbo, offresInstance.buyAboOk, offresInstance.buyAboError);

           
        } else {
            window.location.href = "login.html";
        }

    }
    buyAboOk(data) {
        console.log("buyAboOk success");
        console.log("data:", data);
        const user = utils.getUser();
        let nom = user.name;
        let serv = new ServiceHttp();
        serv.getUserByNom(nom, offresInstance.getUserOk);

    }
    getUserOk(data){
        // Sauvegarde les informations de l'utilisateur dans localStorage
        utils.saveUser(data);  // stocke l'utilisateur dans le localStorage

        // Met à jour l'interface utilisateur pour refléter les informations de l'utilisateur
        utils.updateUserUI();  // met à jour l'UI avec les informations de l'utilisateur
        window.alert("Bravo ! Vous avez maintenant une nouvelle offre. Accédez à votre panel de gestion pour en profiter.");
         
    }
    buyAboError(data) {
        window.alert("Fonds insuffisants !");
        window.location.href = "clientParam.html";
    }

}
new Offres();