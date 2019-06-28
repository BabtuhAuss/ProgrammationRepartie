package fr.baptiste.main;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ClientUnique{
	private Socket connexion = null;
	private PrintWriter writer = null;
	private BufferedInputStream reader = null;

	// Notre liste de commandes. Le serveur nous répondra différemment selon la
	// commande utilisée.
	private static int count = 0;
	private String name = "Client-";

	private ArrayList<Long> tableResponse = null;
	private ArrayList<Socket> connexions = null;

	long total = 0;
	int totalCount = 0;

	int numWorkers = 8000000;

	public ClientUnique(List<String> hosts, List<Integer> ports)  {
		name += ++count;
		totalCount = hosts.size();
		connexions = new ArrayList<Socket>();
		tableResponse = new ArrayList<Long>();
		try {
			for (String host : hosts) {
				int compteur = 0;
				connexions.add(new Socket(host, ports.get(compteur)));
				compteur++;
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {

		try {

			for (Socket connexion : connexions) {
				writer = new PrintWriter(connexion.getOutputStream(), true);
				reader = new BufferedInputStream(connexion.getInputStream());
				// On envoie la commande au serveur

				writer.write("" + numWorkers);
				// TOUJOURS UTILISER flush() POUR ENVOYER RÉELLEMENT DES INFOS
				// AU SERVEUR
				writer.flush();
				
				// On attend la réponse
				String response = read();
				tableResponse.add(Long.parseLong(response));
				

				System.out.println(tableResponse);
				writer.write("CLOSE");
				writer.flush();
				writer.close();
			}
			
			for(Long e : tableResponse){
				total += e;
			}
			double pi = 4.0 * total / totalCount / numWorkers;
			System.out.println("PI : " + pi);
			System.out.println("Difference to exact value of pi: " + (pi - Math.PI));
			System.out.println("Error: " + (pi - Math.PI) / Math.PI * 100 + " %");
			
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}


	// Méthode pour lire les réponses du serveur
	private String read() throws IOException {
		String response = "";
		int stream;
		byte[] b = new byte[4096];
		stream = reader.read(b);
		response = new String(b, 0, stream);
		return response;
	}

	public static void main(String[] args) {
		String host = "127.0.0.1";
		ArrayList<String> hosts = new ArrayList<String>();
		ArrayList<Integer> ports = new ArrayList<Integer>();
		for (int i = 5000; i < 5002; i++) {
			hosts.add(host);
			ports.add(i);
		}

		long startTime = System.nanoTime();
		ClientUnique cp =new ClientUnique(hosts,ports);
		cp.run();
		long stopTime = System.nanoTime();
		System.out.println("Time Duration: " + (stopTime - startTime)/1000000 + "ms");
		
	}
}
