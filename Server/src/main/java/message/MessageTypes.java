package message;

public enum MessageTypes {

    CREATE(),
    SECURE(),       // menssage to complete the registration, to send secure data 
    WHOIS(),        // from uuid get the internal id
    WANTLOG(),      // to show intension of login by the client
    FAKELOGIN(),    // to do tests 
    SCLOGIN(),      // to login in the server with the Smart Card CC
    PASSLOGIN(),    // to login in the server with Password
    PBKEY(),        // get publickey from a given uuid, this used authentication certificate
    LIST(),
    NEW(),
    ALL(),
    SEND(),
    RECV(),
    RECEIPT(),
    STATUS()
}