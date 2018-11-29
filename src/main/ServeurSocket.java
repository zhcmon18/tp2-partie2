package main;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServeurSocket {
    public static void main(String a[]) throws Exception {
        ServerSocket socketServeur = null;
		Socket socketCommunication = null;

		try {
			socketServeur = new ServerSocket(8085);

			while (true) {

				socketCommunication = socketServeur.accept();

				GestionnaireClient gestionnaireClient = new GestionnaireClient(socketCommunication);
				
				gestionnaireClient.start();
			}

		} catch (IOException e) {
		
		}
    }
}
