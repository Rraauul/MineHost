class ClientParam {
    constructor() {
        window.clientParamInstance = this;
    }
    deconnexion() {
        utils.clearUser(); // Efface l'utilisateur du localStorage
        window.alert("Déconnexion en cours...");
        window.location.href = "accueil.html"; // Redirige vers la page d'accueil
    }
    changeName() {
        let serv = new ServiceHttp();
        const nom = document.getElementById('input-nom').value;
        serv.getUserByNom(nom, clientParamInstance.nomDejaPris, clientParamInstance.nomDispo);
    }
    changePassword() {
        const newPassword = document.getElementById('new-password').value;
        const confirmPassword = document.getElementById('confirm-password').value;
        const messageLabel = document.getElementById('password-message');
        if (!newPassword || !confirmPassword) {
            messageLabel.textContent = "Veuillez remplir tous les champs.";
            messageLabel.style.color = "#d9534f";
            return;
        }
        if (newPassword !== confirmPassword) {
            messageLabel.textContent = "Les nouveaux mots de passe ne correspondent pas.";
            messageLabel.style.color = "#d9534f";
            return;
        }
        let serv = new ServiceHttp();
        const user = utils.getUser();
        let pk = user.id;
        serv.changePassword(pk, newPassword, clientParamInstance.changePasswordOK, clientParamInstance.changePasswordError);
    }

    changePasswordOK(data) {
        const messageLabel = document.getElementById('password-message');
        console.log("changePasswordOK success" + data);
        messageLabel.textContent = "Mot de passe modifié avec succès.";
        messageLabel.style.color = "#28a745"; // vert pour succès
    }
    changePasswordError(data) {
        console.log("changePasswordError error" + data);
    }

    nomDejaPris(data) {
        window.alert("Ce nom est déjà pris !");
    }
    nomDispo(data) {
        let serv = new ServiceHttp();
        const user = utils.getUser();
        let pk = user.id;
        const nom = document.getElementById('input-nom').value;
        serv.changeName(pk, nom, clientParamInstance.changeNameOk, clientParamInstance.changeNameError);
    }
    changeNameOk(data) {
        console.log("changeNameOk success");
        console.log("data:", data);
        let nom = document.getElementById('input-nom').value;
        let serv = new ServiceHttp();
        serv.getUserByNom(nom, clientParamInstance.getUserOk, clientParamInstance.erreurGetUser);
    }
    changeNameError(data) {
        console.log("changeNameError error");
        console.log("data:", data);
    }
    addEmeraude(nbEmeraudes) {
        let serv = new ServiceHttp();
        const user = utils.getUser();
        let pk = user.id;
        console.log("User pk :", pk + " et nbEmeraudes : " + nbEmeraudes);
        serv.rechargerEmeraude(pk, nbEmeraudes, clientParamInstance.addEmeraudeOk, clientParamInstance.addEmeraudeError);
    }
    addEmeraudeOk(data) {
        console.log("addEmeraudeOk success");
        console.log("data:", data);
        const user = utils.getUser();
        let nom = user.name;
        let serv = new ServiceHttp();
        serv.getUserByNom(nom, clientParamInstance.getUserOk, clientParamInstance.erreurGetUser);
    }
    getUserOk(data) {
        utils.saveUser(data);
        utils.updateUserUI();
        // window.location.href = "accueil.html";
    }
    erreurGetUser(data) {
        console.log("erreur getUser :", data);
    }
    addEmeraudeError(data) {
        console.log("addEmeraudeError error");
        console.log("data:", data);
    }

}
document.addEventListener("DOMContentLoaded", () => {
    const user = utils.getUser();
    if (user == null) {
        window.location.href = "login.html";
    }
});
new ClientParam();