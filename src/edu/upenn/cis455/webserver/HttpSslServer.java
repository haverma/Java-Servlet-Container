package edu.upenn.cis455.webserver;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.security.KeyStore;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

public class HttpSslServer{

	
	// Create and return the SSL socket
    public SSLServerSocket createSSLSocket(int port, String pass_phrase){
        try{
        	//loading the java keystore using the given passphrase
            KeyStore ks = KeyStore.getInstance("JKS");
            //Change the path of the hw1_keystore.jks file here to the current location
            InputStream is = new FileInputStream("/home/cis455/workspace/hverma/HW1/resources/hw1_ssl.jks");
            char[] keyStore_psswd = pass_phrase.toCharArray();
            char[] cert_passwd = keyStore_psswd;
            ks.load(is,keyStore_psswd);
        
            //Initiating the key manager
            KeyManagerFactory key_manager_factory = KeyManagerFactory.getInstance("SunX509");
            key_manager_factory.init(ks, cert_passwd);

             
            // Initiating the trust manager
            TrustManagerFactory trust_manager_factory = TrustManagerFactory.getInstance("SunX509");
            trust_manager_factory.init(ks);
  
             
            //Create the SSLContext
            SSLContext ssl_context = SSLContext.getInstance("TLS");
            ssl_context.init(key_manager_factory.getKeyManagers(), trust_manager_factory.getTrustManagers() , null);
            
            //Getting the server socket factory
            SSLServerSocketFactory sslfactory = ssl_context.getServerSocketFactory();
            
            //Creating the socket on the given port
            SSLServerSocket ssl_socket = (SSLServerSocket) sslfactory.createServerSocket(port);
            return ssl_socket;
            
        } catch (Exception e){
            System.out.println("Error while creating SSL server Socket: " + e.getMessage());
            e.printStackTrace();
        }  
        return null;
 
    }
     
   
}
