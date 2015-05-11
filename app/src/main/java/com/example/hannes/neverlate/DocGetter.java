package com.example.hannes.neverlate;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.w3c.dom.Document;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by Michal on 15-04-24.
 */
public class DocGetter implements  Runnable {

    private String uri;
    private Document doc;

    public DocGetter(String uri){
        this.uri = uri;
    }

    @Override
    public void run() {
        Log.d("GoogleMapsDirection", uri);
        try {
           // wait();
            HttpClient httpClient = new DefaultHttpClient();
            HttpContext localContext = new BasicHttpContext();
            HttpPost httpPost = new HttpPost(uri);
            HttpResponse response = httpClient.execute(httpPost, localContext);
            InputStream in = response.getEntity().getContent();
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            doc = builder.parse(in);
            //notifyAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public Document getDocument(){
/*            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        while(doc == null){

        }
        this.notifyAll();*/
        return doc;
    }
}
