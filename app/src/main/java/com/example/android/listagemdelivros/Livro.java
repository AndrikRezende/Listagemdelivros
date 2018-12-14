package com.example.android.listagemdelivros;

import java.util.ArrayList;

/**
 * Created by Andrik on 11/03/2018.
 */

public class Livro {

    private String mTitulo;
    private ArrayList<String> mAutores;

    public Livro(String titulo, String primeiroAutor) {
        mTitulo = titulo;
        mAutores = new ArrayList<>();
        mAutores.add(primeiroAutor);
    }

    public String getTitulo() {
        return mTitulo;
    }

    public String getAutores(int index) {
        return mAutores.get(index);
    }

    public void setAutores(String outrosAutores){
        mAutores.add(outrosAutores);
    }

    public int getQuantidadeAutores(){
        return mAutores.size();
    }

    @Override
    public String toString() {
        return "Livro{" +
                "Titulo='" + mTitulo + '\'' +
                ", Autores=" + mAutores +
                '}';
    }
}
