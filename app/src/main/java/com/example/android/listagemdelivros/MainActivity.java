package com.example.android.listagemdelivros;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String REQUEST_URL = "https://www.googleapis.com/books/v1/volumes?q=";
    private String palavraChave = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView erroTextView = (TextView) findViewById(R.id.textViewErro);
        if(isOnline())
            erroTextView.setText(R.string.erro_busca_vazia);
        else
            erroTextView.setText(R.string.erro_sem_internet);
    }

    public void buscar(View v){
        EditText editText = (EditText)findViewById(R.id.edit_text);
        palavraChave = editText.getText().toString();
        if(isOnline()) {
            LivroAsyncTask task = new LivroAsyncTask();
            task.execute(REQUEST_URL, palavraChave);
        }
        else {
            TextView erroTextView = (TextView) findViewById(R.id.textViewErro);
            erroTextView.setText(R.string.erro_sem_internet);
        }
    }

    public boolean isOnline(){
        boolean isOnline = false;
        try {
            Process p = java.lang.Runtime.getRuntime().exec("ping -c 1 www.google.com");
            int retorno = p.waitFor();
            if(retorno==0)
                isOnline = true;
        } catch (Exception exception) {
            Log.e(MainActivity.class.getSimpleName(), "Error with isOnline", exception);
        }
        return isOnline;
    }

    private class LivroAsyncTask extends AsyncTask<String,Void,ArrayList<Livro>>{

        private final static int SEM_ERRO = 1;
        private final static int ERRO_SEM_INTERNET = 2;
        private final static int ERRO_BUSCA_VAZIO = 3;
        private final static int ERRO_SEM_LIVROS = 4;
        private int erro = SEM_ERRO;

        @Override
        protected ArrayList<Livro> doInBackground(String... sUrl) {
            erro = SEM_ERRO;
            if(sUrl[1].equals("")) {
                erro = ERRO_BUSCA_VAZIO;
                return null;
            }
            URL url = createUrl(sUrl[0]+sUrl[1]);
            String jsonResponse = "";
            try {
                jsonResponse = makeHttpRequest(url);//marca erro sem internet
            } catch (IOException exception) {
                Log.e(MainActivity.class.getSimpleName(), "Error with doInBackground", exception);
            }
            if( erro == ERRO_SEM_INTERNET)
                return null;

            ArrayList<Livro> livros = extractFromJson(jsonResponse);
            if (livros == null)
                erro = ERRO_SEM_LIVROS;

            return livros;
        }

        @Override
        protected void onPostExecute(ArrayList<Livro> livros) {
            atualizarInterface(livros,erro);
        }

        private void atualizarInterface(ArrayList<Livro> livros,int erro){
            TextView erroTextView = (TextView) findViewById(R.id.textViewErro);
            erroTextView.setText("");
            ListView listView= (ListView) findViewById(R.id.layout_list);
            listView.setAdapter(null);

            switch (erro){
                case LivroAsyncTask.ERRO_BUSCA_VAZIO:
                    erroTextView.setText(R.string.erro_busca_vazia);
                    break;
                case LivroAsyncTask.ERRO_SEM_INTERNET:
                    erroTextView.setText(R.string.erro_sem_internet);
                    break;
                case LivroAsyncTask.ERRO_SEM_LIVROS:
                    erroTextView.setText(R.string.erro_sem_livros);
                    break;
                default:
                    LivroAdapter adapter = new LivroAdapter(MainActivity.this, livros);
                    listView.setAdapter(adapter);
                    break;
            }
        }

        private URL createUrl(String stringUrl) {
            URL url = null;
            try {
                url = new URL(stringUrl);
            } catch (MalformedURLException exception) {
                Log.e(MainActivity.class.getSimpleName(), "Error with createUrl", exception);
            }
            return url;
        }//fim createUrl

        private String makeHttpRequest(URL url) throws IOException {
            String jsonResponse = "";
            HttpURLConnection urlConnection = null;
            InputStream inputStream = null;
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setReadTimeout(R.integer.read_timeout /* milliseconds */);
                urlConnection.setConnectTimeout(R.integer.connect_timeout /* milliseconds */);
                urlConnection.connect();
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            }
            catch (IOException exception) {
                Log.e(MainActivity.class.getSimpleName(), "Error with makeHttpRequest", exception);
                erro = ERRO_SEM_INTERNET;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            }
            return jsonResponse;
        }//fim makeHttpRequest

        private String readFromStream(InputStream inputStream) throws IOException {
            StringBuilder output = new StringBuilder();
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line = reader.readLine();
                while (line != null) {
                    output.append(line);
                    line = reader.readLine();
                }
            }
            return output.toString();
        }//fim readFromStream

        private ArrayList<Livro> extractFromJson(String livroJSON) {
            try {
                JSONObject baseJsonResponse = new JSONObject(livroJSON);
                JSONArray itensArray = baseJsonResponse.getJSONArray("items");

                ArrayList<Livro> livros = new ArrayList<Livro>();

                for (int i=0; i< itensArray.length();i++) {

                    JSONObject primeiroItem = itensArray.getJSONObject(i);
                    JSONObject volumeInfo = primeiroItem.getJSONObject("volumeInfo");
                    String titulo = volumeInfo.getString("title");

                    if(volumeInfo.isNull("authors"))
                        livros.add(new Livro(titulo, "Sem autores"));
                    else {
                        JSONArray autores = volumeInfo.getJSONArray("authors");

                        livros.add(new Livro(titulo, autores.getString(0)));
                        for (int a = 1; a < autores.length(); a++) {
                            livros.get(i).setAutores(autores.getString(a));
                        }

                    }//fim else
                }

                return livros;

            } catch (JSONException exception) {
                Log.e(MainActivity.class.getSimpleName(), "Error with extractFromJson", exception);
            }
            return null;
        }//fim extractFromJson


    }//fim classe

}
