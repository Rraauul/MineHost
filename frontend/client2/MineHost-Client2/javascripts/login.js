class Login {
    constructor() {
        window.loginInstance = this;
    }

    testLogin() {
        console.log("Test login commence");
        var user = $("#username").val();
        var estConnecté = false;
        var password = $("#password").val();
        let serv = new ServiceHttp();
        if (user != "" && password != "") {
            // console.log("Envoie au backend...");
            this.user = user;
            serv.login(user, password, loginInstance.successLogin, loginInstance.erreurLogin);

        } else {
            document.getElementById("motDepasseInfo").innerText = "Veuillez remplir les champs";
        }

        console.log(user + " et mdp : " + password);

        //serv.infoOffer(offresInstance.successLoadOffer, offresInstance.erreurLoadOffer);d
    }
    successLogin(data) {
        document.getElementById("motDepasseInfo").innerText = "";
        let serv = new ServiceHttp();
        console.log("nom : " + loginInstance.user);
        serv.getUserByNom(loginInstance.user, loginInstance.getUserOk, loginInstance.erreurGetUser);

    }
    getUserOk(data) {
        console.log("getUserOk success");
        console.log("data:", data);
        console.log("Nom utilisateur : " + data.name);

        // Sauvegarde les informations de l'utilisateur dans localStorage
        utils.saveUser(data);  // stocke l'utilisateur dans le localStorage

        // Met à jour l'interface utilisateur pour refléter les informations de l'utilisateur
        utils.updateUserUI();  // met à jour l'UI avec les informations de l'utilisateur

        // Optionnel: Rediriger vers une autre page une fois connecté
        window.location.href = "accueil.html";  // ou n'importe quelle autre page
    }

    erreurGetUser(data) {
        console.log("erreur getUser :", data);
    }
    erreurLogin(data) {
        document.getElementById("motDepasseInfo").innerText = "Mot de passe ou Utilisateur Incorrect";
        console.log("erreur au backend...");
        console.log("data :", data);
    }
    getEstConnecté() {
        return estConnecté;
    }

}
new Login();