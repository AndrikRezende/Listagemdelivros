package com.example.android.listagemdelivros;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;


/**
 * Created by Andrik on 14/03/2018.
 */

public class LivroAdapter extends ArrayAdapter<Livro>{

    public LivroAdapter(Context context, ArrayList<Livro> livros) {
        super(context, 0, livros);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.list_item, parent, false);
        }

        Livro currentLivro = getItem(position);

        TextView titulo = (TextView) listItemView.findViewById(R.id.id_titulo);
        titulo.setText(currentLivro.getTitulo());

        TextView autores = (TextView) listItemView.findViewById(R.id.id_autores);
        String sAutores=currentLivro.getAutores(0);
        for(int i=1;i<currentLivro.getQuantidadeAutores();i++)
            sAutores=sAutores+", "+currentLivro.getAutores(i);
        autores.setText(sAutores);

        return listItemView;
    }
}
