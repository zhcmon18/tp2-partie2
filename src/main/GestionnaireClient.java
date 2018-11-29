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

	@Override
	public void run() {

		try {
			out = new PrintWriter(socketCommunication.getOutputStream());
			in = new BufferedReader(new InputStreamReader(socketCommunication.getInputStream()));

			gererClient();

			fermetureFluxEtSocket();

			interrupt();
		
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void gererClient() {
		
		String nomFichier = obtenirNomFichier(getRequete());

		File fic = obtenirFichier(nomFichier);

		if(fichierExistant(fic)) {
			lireFichierEtEnvoyer(fic);
			
		} else {
			enovyerErreur();
		}
	}

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

	private void enovyerErreur() {
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
	}


	private String getRequete() {

		String requete = "";
		String s = "";

		try {
			// lecture de l'entête http
			// http est un protocole structuré en lignes
			while ((s = in.readLine()).compareTo("") != 0) {
				requete += s + "\r\n";
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println(requete);

		return requete;
	}

	private String obtenirNomFichier(String requete) {
		
		String nomFichier = "";

		if(requete != null) {
			nomFichier = requete.substring(requete.indexOf("/") + 1, requete.indexOf(" HTTP/"));
		} 
		
		return nomFichier;
	}
	
	private File obtenirFichier(String nomFichier) {
		File fic = new File ("src/siteHTTP/" + nomFichier);

		return fic;
	}

	private Boolean fichierExistant(File fic) {
		
		if(fic.isFile()) {
			return true;
		} else {
			return false;
		}
	}

	private void outPrintEtFlush (String chaine) {
		out.print(chaine);
		out.flush();
	}

	public void fermetureFluxEtSocket() {
		try {
			in.close();
			out.close();
			socketCommunication.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
