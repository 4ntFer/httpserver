import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class HandlingClient extends Thread{
    private Socket socket;
    private ArrayList<HandlingClient> clientsList; // lista de todas as comunicações abertas com clientes
    private OutputStream output; //saída do cliente
    private InputStream input; //entrada no cliente

    public HandlingClient (Socket clientSocket, ArrayList<HandlingClient> clientsList){
        socket = clientSocket;
        this.clientsList = clientsList;

        try {
            output =socket.getOutputStream();
            input = socket.getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Cliente connectatdo: " + socket.getInetAddress().getHostAddress());

        start();
    }

    public void run(){

        try {
            HttpRequest request = readRequest();

            if(request != null && request.getMethod().equals("GET")){
                handleGetRequest(request);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        try {
            System.out.println("Cliente " + socket.getInetAddress().getHostAddress() + " saiu!");
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }finally {
            clientsList.remove(this);
        }
    }

    //Lê a requisição enviado do cliente
    private HttpRequest readRequest() throws IOException {
        ByteArrayOutputStream request = new ByteArrayOutputStream();
        final byte[] buff = new byte[2048];

        int read;
        while(
                (read =
                    input.read(buff, 0, Math.min(input.available(), 2048))
                ) > 0
        ){
            request.write(buff, 0, read);
        }
        return parseMetadata(new ByteArrayInputStream(request.toByteArray()));
    }

    /*
     * Analisa e monta o objeto que representa uma requisação HTTP
     * a partir dos dados obtidos da entrada, ou seja, a requisição
     * enviada pelo cliente.
     */
    private HttpRequest parseMetadata(InputStream data) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(data));
        String firstLine = bufferedReader.readLine();
        String method;
        String url;

        if(firstLine != null){
            method =  firstLine.split("\\s+")[0];
            url = firstLine.split("\\s+")[1];

            Map<String, String> headers = new HashMap<String, String>();
            String headerLine;
            while((headerLine = bufferedReader.readLine()) != null){
                if(!headerLine.trim().isEmpty()){
                    String key = headerLine.split(":\\s")[0];
                    String value  = headerLine.split(":\\s")[1];

                    headers.put(key, value);
                }
            }

            return new HttpRequest(method, url, headers);
        }


        return null;
    }

    /*
    * Post response method
     */
    private void handleGetRequest(HttpRequest request) throws IOException {
        File file = new File("src/res" + request.getUrl());
        FileInputStream fileInputStream = null;

        StringBuilder responseMetadata = new StringBuilder();
        if(file.exists()){
            String extesion = file.getName().split("\\.")[1];
            String contentType = "";

            switch(extesion){
                case "html": contentType = "text/html"; break;
                case "jpeg": contentType = "image/jpeg"; break;
                case "jpg": contentType = "image/jpeg"; break;
            }

            System.out.println("requested " + file.getName() + " exists, posting file");

            //build header
            fileInputStream = new FileInputStream(file);

            responseMetadata.append("HTTP/1.1 200 OK\r\n");

            responseMetadata.append("Content-Type: "+ contentType +"\r\n");
            responseMetadata.append(String.format("Content-Length: %d\r\n", fileInputStream.available()));

            responseMetadata.append("\r\n");

            //post header
            output.write(responseMetadata.toString().getBytes(StandardCharsets.UTF_8));

            //post file
            fileInputStream.transferTo(output);
        }else {
            //404 not found
            System.out.println("requested " + file.getName() + " not exists, posting 404");
            output.write(
                    "HTTP/1.1 404 Not Found\r\n\r\n".getBytes(StandardCharsets.UTF_8)
            );
            output.write("<h1>404 :(<h1>".getBytes(StandardCharsets.UTF_8));
        }

    }
}
