package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class GestionnaireClient extends Thread {

	private Socket socketCommunication;
	PrintWriter out = null; // le flux de sortie de socket
	BufferedReader in = null;

	public GestionnaireClient(Socket socketCommunication) {
		this.socketCommunication = socketCommunication;
	}

	//Un thread pour chaque client connecté
	@Override
	public void run() {

		try {
			out = new PrintWriter(socketCommunication.getOutputStream());
			in = new BufferedReader(new InputStreamReader(socketCommunication.getInputStream()));

			gererClient();

			out.close();
			in.close();
			socketCommunication.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//Gestion de la connexion du client 
	private void gererClient() {
		
		String nomFichier = obtenirNomFichier(getEntete());

		File fic = obtenirFichier(nomFichier);

		if(fic.isFile()) {
			lireFichierEtEnvoyer(fic);
			
		} else {
			enovyerErreur404();
		}
	}

	//Lire le contenu du fichier et l’envoyer au client avec l’entête succès
	private void lireFichierEtEnvoyer(File fic) {
		String corps = "";

		try(BufferedReader reader = new BufferedReader(new FileReader(fic))) {
			for(String ligne; (ligne = reader.readLine()) != null; ) {
				corps += ligne;
			}

			// longueur du corps de la réponse
			int len = corps.length();

			// envoie des entêtes
			out.print("HTTP/1.0 200 OK\r\n");
			out.print("Content-Length: " + len + "\r\n");
			out.print("Content-Type: text/html\r\n\r\n"); // envoie de la ligne vide
			// envoi de la réponse
			outPrintEtFlush(corps);
			
		} catch (FileNotFoundException ex) {

		} catch (IOException ex) {

		}
	}

	//Envoyer l'erreur 404 au client
	private void enovyerErreur404() {
		String corps = "";
				corps += "<html>";
				corps += "<body>";
				corps += "<p>";
				corps += "Erreur 404: le fichier introuvable";
				corps += "</p>";
				corps += "</body>";
				corps += "</html>";

			// longueur du corps de la réponse
			int len = corps.length();

			// envoie des entêtes
			out.print("HTTP/1.0 404 Not Found\r\n");
			out.print("Content-Length: " + len + "\r\n");
			out.print("Content-Type: text/html\r\n\r\n"); // envoie de la ligne vide
			// envoi de la réponse
			outPrintEtFlush(corps);

			System.out.println("Erreur 404: le fichier introuvable.");
	}

	//Obtenir l’entête de la requête 
	private String getEntete() {

		String entete = "";
		String s = "";
		try {
			// lecture de l'entête http
			// http est un protocole structuré en lignes
			while ((s = in.readLine()).compareTo("") != 0) {
				entete += s + "\r\n";
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return entete;
	}

	//Obtenir le nom du fichier dans l’entête
	private String obtenirNomFichier(String entete) {
		
		String nomFichier = "";

		if(entete != null) {
			nomFichier = entete.substring(entete.indexOf("/") + 1, entete.indexOf(" HTTP/"));
		} 
		
		return nomFichier;
	}
	
	//Obtenir le fichier sur le serveur
	private File obtenirFichier(String nomFichier) {
		File fic = new File ("src/siteHTTP/" + nomFichier);

		return fic;
	}

	//Out print et flush
	private void outPrintEtFlush (String chaine) {
		out.print(chaine);
		out.flush();
	}
}
