
//var BASE_URL = "../../REST/src/main/java/com/example/REST/controller/Controller.java";
var BASE_URL = "https://apigw.fuentesr.emf-informatique.ch/";
class ServiceHttp {
    constructor() { }

    login(user, passwd, successCallback, errorCallback) {
        $.ajax({
            type: "POST",
            url: BASE_URL + "login",
            data: JSON.stringify({
                name: user,
                motDePasse: passwd
            }),
            contentType: "application/json",
            dataType: "json",
            xhrFields: {
                withCredentials: true // pour inclure les cookies
            },
            success: successCallback,
            error: errorCallback
        });
    }
    
    getUserByNom(nom, successCallback, errorCallback) { //check.
        $.ajax({
            type: "GET",
            dataType: "json",
            url: BASE_URL + "getUserByUsername",
            data: {
                username: nom
            },
            success: successCallback,
            error: errorCallback
        });
    }

    rechargerEmeraude(PK, nbEmeraudes, successCallback, errorCallback) { //check
        $.ajax({
            type: "POST",
            dataType: "text",
            url: BASE_URL + "addEmeraude",
            data: {
                pkUser: PK,
                emeraudes: nbEmeraudes
            },
            success: successCallback,
            error: errorCallback
        });
    }

    infoOffer(successCallback, errorCallback) { // top !
        $.ajax({
            type: "GET",
            dataType: "text",
            url: BASE_URL + "infoOffers",
            success: successCallback,
            error: errorCallback
        });
    }
    buyAbo(pkUser, fkAbo, successCallback, errorCallback) { // top !
        $.ajax({
            type: "POST",
            dataType: "text",
            url: BASE_URL + "acheterAbo",
            data: {
                pkUser: pkUser,
                Fkabonnement: fkAbo
            },
            success: successCallback,
            error: errorCallback
        });
    }

    changePassword(PK, passwd, successCallback, errorCallback) { // check.
        $.ajax({
            type: "PUT",
            dataType: "text",
            url: BASE_URL + "changePassword",
            data: {
                PK: PK,
                password: passwd
            },
            success: successCallback,
            error: errorCallback
        });
    }

    changeName(PK, newName, successCallback, errorCallback) { //check.
        $.ajax({
            type: "PUT",
            dataType: "text",
            url: BASE_URL + "changeName",
            data: {
                PK: PK,
                newName: newName
            },
            success: successCallback,
            error: errorCallback
        });
    }

    infoStockage(successCallback, errorCallback) { //check.
        $.ajax({
            type: "GET",
            dataType: "text",
            url: BASE_URL + "infoServer",
            data: {
                serverId: 1
            },
            xhrFields: {
                withCredentials: true
            },
            success: successCallback,
            error: errorCallback
        });
    }
}
