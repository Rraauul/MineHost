class Utils {
    // Save user
    constructor() {
        this.user = JSON.parse(sessionStorage.getItem("user")) || null;
    }
    saveUser(user) {
        sessionStorage.setItem("user", JSON.stringify(user));
    }

    getUser() {
        const data = sessionStorage.getItem("user");
        return data ? JSON.parse(data) : null;
    }
    clearUser() {
        sessionStorage.removeItem("user");
    }
    isLoggedIn() {
        return this.getUser() !== null;
    }

    // Met à jour l'affichage du header : nom, emeraudes, avatar
    updateUserUI() {
       
        console.log("Data user :" + this.getUser())

        const user = this.getUser(); // Récupère l'utilisateur connecté (si connecté)

        // Si l'utilisateur n'est pas connecté, redirige vers login.html
        const avatarLink = document.querySelector("#avatar-link");
        const avatarImg = document.querySelector("#avatar");

        if (user == null) {
                //console.log("nom : " + user.name)
                 //console.log("emeraudes : " + user.nbEmeraudes)
            // Si l'utilisateur n'est pas connecté, redirige l'avatar vers login.html
            if (avatarLink) avatarLink.href = "login.html";  // L'avatar redirige vers login.html
            if (avatarImg) avatarImg.src = "images/usernc.png";  // Avatar par défaut pour non-connecté
            return; // Rien d'autre à faire si l'utilisateur n'est pas connecté
        }
        // Si l'utilisateur est connecté, l'avatar redirige vers param.html
        if (avatarLink) avatarLink.href = "clientParam.html"; // L'avatar redirige vers param.html
        if (avatarImg) avatarImg.src = "images/user.png"; // Met à jour l'avatar connecté

        // Met à jour le nom et le nombre d'émeraudes dans l'interface
        const nameSpan = document.querySelector("#user-name");
        const emeraldSpan = document.querySelector("#emerald-count");

        if (nameSpan) nameSpan.textContent = user.name;
        if (emeraldSpan) emeraldSpan.textContent = user.nbEmeraudes;
        const abonnementSpan = document.querySelector("#abonnement-info"); // <--- ici
        if (abonnementSpan) {
            abonnementSpan.textContent = user.abonnementNom ? user.abonnementNom : "Aucune";
        }
       
        //user.abonnementNom
    }


}
window.utils = new Utils();
