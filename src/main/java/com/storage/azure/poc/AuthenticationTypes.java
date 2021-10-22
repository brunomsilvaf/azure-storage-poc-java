package com.storage.azure.poc;

public enum AuthenticationTypes {
    SAS_TOKEN,
    CONNECTION_STRING,
    KEY,
    SERVICE_PRINCIPAL, // client id, client secret, tenant id
    USER_CREDENTIALS  // client id, username, password
}
