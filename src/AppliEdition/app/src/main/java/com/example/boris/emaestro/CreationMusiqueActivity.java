package com.example.boris.emaestro;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import BDD.db.DataBaseManager;
import BDD.to.*;
import BDD.to.Musique;

public class CreationMusiqueActivity extends Activity {

    final Activity thisActivity = this;

    final String EXTRA_NOMPARTITION="vide";
    final String EXTRA_NBMESURE="nbMesure";
    final String EXTRA_PULSATION="pulsation";
    final String EXTRA_UNITE="unite";
    final String EXTRA_TPSPARMESURE="nbTpsMesure";
    final String EXTRA_DRAGACTIF="drag";
    final String EXTRA_ID_PARTITION="idMusique";
    final String EXTRA_NEW_PARTITION="new";

    EditText pulsation;
    EditText nomPartitionE;
    EditText nbMesureE;

    String unite="";
    String nomPartition="";
    String nbMesure="";
    String nbPulsation="";
    String tpsParMesure="";

    Spinner tpsParMesureSpinner,  uniteSpinner;

    //BDD
    //final MusiqueDAO bddMusique = new MusiqueDAO(this);
    DataBaseManager bdd = new DataBaseManager(this);
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.creation_musique);

        bdd.open();

        pulsation = (EditText) findViewById(R.id.pulsation);
        nomPartitionE = (EditText) findViewById(R.id.nom);
        nbMesureE = (EditText) findViewById(R.id.nbMesure);
        uniteSpinner = (Spinner) findViewById(R.id.uniteTempo);
        tpsParMesureSpinner = (Spinner) findViewById(R.id.tempsParMesure);
        Button loginButton = (Button) findViewById(R.id.creer);

        // Spinner Unité du tempo
        List<String> uniteList = new ArrayList<String>();
        uniteList.add("ronde");
        uniteList.add("blanche");
        uniteList.add("noire");
        uniteList.add("croche");
        uniteList.add("ronde pointée");
        uniteList.add("blanche pointée");
        uniteList.add("noire pointée");
        uniteList.add("croche pointée");



        List<String> tpsMesure = new ArrayList<String>();
        for(int i=2;i<=8;i++){
            tpsMesure.add(String.valueOf(i));
        }

        // Adapter pour le spinner, pour l'affichage
        ArrayAdapter<String> dataAdapterUnite = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,uniteList );
        ArrayAdapter<String> dataAdapterTpsParMesure = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,tpsMesure);

        // Drop down layout style - affichage "amélioré"
        dataAdapterUnite.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dataAdapterTpsParMesure.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        uniteSpinner.setAdapter(dataAdapterUnite);
        tpsParMesureSpinner.setAdapter(dataAdapterTpsParMesure);
        tpsParMesureSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // On selecting a spinner item
                tpsParMesure = parent.getItemAtPosition(position).toString();
            }

            public void onNothingSelected(AdapterView<?> arg0) {
                //nothing
            }
        });

        //button listener pour création
        loginButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                nbMesure = nbMesureE.getText().toString();
                unite = uniteSpinner.getSelectedItem().toString();
                Partition p = new Partition();
                unite = String.valueOf(p.convertUniteStrInt(unite));
                nbPulsation = pulsation.getText().toString();
                nomPartition = nomPartitionE.getText().toString();
                // permet le passage de message dans un changement d'activité (startActivity)
                // Intent intent = new Intent(CreationMusiqueActivity.this, EditionActivity.class);
                Intent intent = new Intent(CreationMusiqueActivity.this, CatalogueActivity.class);

                intent.putExtra(EXTRA_NOMPARTITION, nomPartition);
                intent.putExtra(EXTRA_NBMESURE, nbMesure);
                intent.putExtra(EXTRA_PULSATION, nbPulsation);
                intent.putExtra(EXTRA_TPSPARMESURE, tpsParMesure);
                intent.putExtra(EXTRA_UNITE, unite);
                intent.putExtra(EXTRA_ID_PARTITION, "-1");
                intent.putExtra(EXTRA_NEW_PARTITION, "true");


                if (unite.length() <= 0) {
                    Toast.makeText(getApplicationContext(), "Veuillez choisir l'unité de temps", Toast.LENGTH_SHORT).show();
                } else if (tpsParMesure.length() <= 0) {
                    Toast.makeText(getApplicationContext(), "Veuillez choisir le nombre de temps par mesure", Toast.LENGTH_SHORT).show();
                } else if (pulsation.length() <= 0) {
                    Toast.makeText(getApplicationContext(), "Veuillez choisir une pulsation", Toast.LENGTH_SHORT).show();
                } else if (nbMesure.length() <= 0) {
                    Toast.makeText(getApplicationContext(), "Veuillez choisir un nombre de mesures valide", Toast.LENGTH_SHORT).show();
                } else if (nomPartition.length() <= 0) {
                    Toast.makeText(getApplicationContext(), "Veuillez choisir un nom de partition", Toast.LENGTH_SHORT).show();
                } else {

                    Musique musiqueDejaPresente = bdd.getMusique(nomPartition);
                    if (!musiqueDejaPresente.getName().equals(nomPartition)) {
                        //si le nom de la musique n'existe pas deja on ajoute la musique dans la BDD
                        if (Integer.parseInt(nbMesure) > 0) {
                            long err = bdd.save(new Musique(nomPartition, Integer.parseInt(nbMesure)));

                            if (err == -1) {
                                Toast.makeText(getApplicationContext(), "Erreur lors de l'ajout de la partition dans la base de donnée", Toast.LENGTH_SHORT).show();
                            } else {
                                int idmusique = bdd.getMusique(nomPartition).getId();
                                bdd.save(new VariationIntensite(idmusique, -1, 1, 1, 0));

                                bdd.save(new VariationTemps(idmusique, 1, Integer.parseInt(tpsParMesure), Integer.parseInt(nbPulsation), 1));//TODO : Gerer l'unite pulsation

                                bdd.close();
                                startActivity(intent);
                                thisActivity.finish();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Le nombre de mesures est incorrecte", Toast.LENGTH_LONG).show();

                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "La partition " + nomPartition + " existe déjà", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });


        //listener pour choix de l'unite
        uniteSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // On selecting a spinner item
                unite = parent.getItemAtPosition(position).toString();
            }

            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });

        //listener pour nbre de temps par mesure
        tpsParMesureSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // On selecting a spinner item
                tpsParMesure = parent.getItemAtPosition(position).toString();
            }
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });




    }




}
