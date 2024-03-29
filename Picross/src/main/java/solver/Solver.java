package solver;

import picrossgame.EtatCase;
import picrossgame.Picross;
import picrossgame.SerieBloc;
import tools.Combinaisons;

import java.io.PrintStream;
import java.util.ArrayList;


import static java.util.Arrays.stream;
import static tools.Mathematique.transpose;


public class Solver {

    private Picross picrossGame;




    public Solver(String fichierContraites){
        this.picrossGame=new Picross(fichierContraites);
    }

    public Picross getPicrossGame(){
        return(this.picrossGame);
    }


    /**
     * Première methode qui nettoie les blocs et blancs pour lesquelles leur position dans la grille est incertaine
     * @return une arraylist d'entiers contenant les emplacements des blancs et blocs dont on est certain de l'emplacement
     */
    public static ArrayList<Integer> compareCombinaisons(Combinaisons listCombinaisons){
        ArrayList<Integer> emplacementCertains = new ArrayList<>();
        // taille d'une combinaison
        int tailleComb = listCombinaisons.get(0).size();

        if (listCombinaisons.size() == 1) {
            emplacementCertains = listCombinaisons.get(0);
            return emplacementCertains;
        }
        for (int i = 0; i < tailleComb; i++) {
            int sum = 0;
            for (int j = 0; j < listCombinaisons.size(); j++) {
                int val = listCombinaisons.get(j).get(i);
                sum += val;
            }
            if (sum == listCombinaisons.size()) {
                emplacementCertains.add(1);
            } else if (sum == -listCombinaisons.size()) {
                emplacementCertains.add(-1);
            } else {
                emplacementCertains.add(0);
            }
        }
        return emplacementCertains;
    }



    /**
     * Permet de supprimer les combinaisons contenant des infirmations déjà trouvées
     * @param result
     * @param combs
     * @return Combinaisons
     */
    public static void supprCombs(ArrayList<Integer> result, Combinaisons combs){
        //tant qu'il y a des element dans combs
        int i=combs.size()-1;
        while (i>=0){
            ArrayList<Integer> comb=combs.get(i);
            for (int j = 0; j < comb.size() ; j++) {

                if(result.get(j)==1 && comb.get(j)!=1){
                    combs.removeComb(i);
                    break;
                }
                if(result.get(j)==-1 && comb.get(j)!=-1){
                    combs.removeComb(i);
                    break;
                }
                if(result.get(j)==0){
                    continue;
                }
            }
            i--;
        }
    }

    /**
     * Joint les deux résultats des lignes et colonnes dans une seule Combinaisons
     * @param lignes   : combinaisons lignes NON TRANSPOSES
     * @param colonnes : combinaisons colonnes NON TRANSPOSES
     * @return ArrayList<ArrayList < Integer>> : matrice de solution
     */
    public static Combinaisons joindreLignesColonnes(Combinaisons lignes, Combinaisons colonnes) {
        Combinaisons transColonne = transpose(colonnes);
        Combinaisons solutionMatrix = new Combinaisons();
        for (int i = 0; i < transColonne.size(); i++) {
            ArrayList<Integer> sousSolution = new ArrayList<>();
            for (int j = 0; j < transColonne.get(0).size(); j++) {
                if (transColonne.get(i).get(j) == 0 && lignes.get(i).get(j) == 0) {
                    sousSolution.add(0);
                } else if (transColonne.get(i).get(j) == 1 || lignes.get(i).get(j) == 1) {
                    sousSolution.add(1);
                } else {
                    sousSolution.add(-1);
                }
            }
            solutionMatrix.add(sousSolution);
        }
        return solutionMatrix;
    }

    /**
     * Translate l'ensemble des combinaisons pour un numéro de ligne de fichier donné
     * @param numeroLigneFichier
     * @return la liste des combinaisons translatees
     */
    public Combinaisons translateCombinaisons(int numeroLigneFichier) {
        PrintStream out = System.out;

        //pour traiter les ligne et les colonnes
        int m=this.picrossGame.getM();
        int n=this.picrossGame.getN();
        int tailleGrilleInter = 0;
        if (numeroLigneFichier > n) {
            tailleGrilleInter = m;
        } else if (numeroLigneFichier <= 0) {
            out.println("Le numéro de ligne choisis est incorrecte");
        } else {
            tailleGrilleInter = n;
        }

        //importation taille des blocs
        SerieBloc ligne = this.picrossGame.getContraintes().get(numeroLigneFichier - 1);

        //nombre de blocs dans un serieBloc
        int k = ligne.getTaille();
        //nombre de blancs entre les blocs
        int nbrBlancs = k - 1;
        //addition des tailles des blocs
        int tailleTotBlocs = 0;
        for (int i = 0; i < k; i++) {
            tailleTotBlocs += ligne.getBloc(i).getTaille();
        }
        // nombre de blancs en plus dans la ligne
        int nbrBlancPlus = m - (tailleTotBlocs + nbrBlancs);

        //liste dans laquelle seront mises les combinaisonsNonTranslatees translatées
        Combinaisons combinasonsTranslatees = new Combinaisons();

        //combinaisonsNonTranslatees trouvées non translatées
        Combinaisons combinaisonsNonTranslatees = new Combinaisons( k, nbrBlancPlus+k);


        for (int i=0;i<combinaisonsNonTranslatees.size();i++) {
            ArrayList<Integer> comb =combinaisonsNonTranslatees.get(i);
            ArrayList<Integer> combinaisonTranslatee = new ArrayList<>();
            //ajout des premières valeurs
            int premiereValeur = comb.get(0);
            for (int j = 0 ; j < premiereValeur; j++) {
                combinaisonTranslatee.add(-1);
            }

            for (int j = 0; j <= k - 1; j++) {
                //info taille du bloc
                int taillei = ligne.getBloc(j).getTaille();
                int diff;
                if (j < k - 1) {
                    //info taille du blanc
                    diff = comb.get(j + 1) - comb.get(j);
                } else {
                    diff = 0;
                }
                //ajout des blocs
                for (int l = 0; l < taillei; l++) {
                    combinaisonTranslatee.add(1);
                }

                //ajout des blancs après le blocs sans ajouter de blancs apres le dernier bloc
                if (j <= k - 2) {
                    for (int p = 0; p < diff; p++) {
                        combinaisonTranslatee.add(-1);
                    }
                }
            }
            //ajout des blancs en plus
            while (combinaisonTranslatee.size() < tailleGrilleInter) {
                combinaisonTranslatee.add(-1);
            }
            combinasonsTranslatees.add(combinaisonTranslatee);
        }
        return combinasonsTranslatees;
    }

    /**
     * Vérifie si le puzzle est résolu ou non
     * @return boolean
     */
    private boolean check_solved(Boolean[]lignes_termine,Boolean[]colonnes_termine) {
        boolean isSolved=true;
        for(Boolean lignes : lignes_termine){
            if(lignes==false){
                isSolved=false;
            }
        }
        for(Boolean colonnes : colonnes_termine){
            if(colonnes==false){
                isSolved=false;
            }
        }
        return(isSolved);
    }


    /**
     * Permet de résoudre le nonogram en considérant toutes les combinaisons possibles, en faisant leur intersection
     * et en supprimant les informations redondantes au fure et à mesure
     * @return EtatCase[][]
     */
    public EtatCase[][] resoudre(){

        boolean isSolved=false;

        int m=this.picrossGame.getM();
        int n=this.picrossGame.getN();
        EtatCase[][] solution = new EtatCase[n][m];
        Boolean [] lignes_termine = new Boolean[n];
        Boolean [] colonnes_termine = new Boolean[m];


        ArrayList<Combinaisons> combinaisonsLignes = new ArrayList<>();
        ArrayList<Combinaisons> combinaisonsColonnes = new ArrayList<>();
        Combinaisons lignesResult = new Combinaisons();
        Combinaisons colonneResult = new Combinaisons();
        Combinaisons finalResult = new Combinaisons();

        //Initialisation
        for (int i = 1; i < n+1; i++) {
            Combinaisons combI = translateCombinaisons(i);
            combinaisonsLignes.add(combI);
        }
        for (int j = n+1; j < n+m+1; j++) {
            Combinaisons combJ = translateCombinaisons(j);
            combinaisonsColonnes.add(combJ);
        }

        // Bouclage tant que le puzzle n'est pas résolue
        while (!isSolved){

            for (int i = 0; i < n; i++) {
                Combinaisons combI=combinaisonsLignes.get(i);
                ArrayList<Integer> resultLigne = compareCombinaisons(combI);
                lignes_termine[i]=verifyState(resultLigne);
                lignesResult.add(resultLigne);

            }
            for (int j = 0; j < m; j++) {
                Combinaisons combJ = combinaisonsColonnes.get(j);
                ArrayList<Integer> resultColonne = compareCombinaisons(combJ);
                colonnes_termine[j]=verifyState(resultColonne);
                colonneResult.add(resultColonne);
            }

            //met en commun le résultat sur les lignes et les colonnes

            finalResult=joindreLignesColonnes(lignesResult,colonneResult);

            //Suppression des informations
            for(int k=0;k<combinaisonsLignes.size();k++){
                supprCombs(finalResult.get(k),combinaisonsLignes.get(k));
            }
            Combinaisons transposedFinalResult = transpose(finalResult);
            for(int l=0;l<combinaisonsColonnes.size();l++){
                supprCombs(transposedFinalResult.get(l),combinaisonsColonnes.get(l));
            }
            isSolved=check_solved(lignes_termine,colonnes_termine);
            lignesResult.clear();
            colonneResult.clear();
            //Pour afficher la résolution petit à petit
            System.out.println(finalResult);
            for(Boolean elt:lignes_termine){
                System.out.print(elt +" ,");
            }
            for(Boolean elt:colonnes_termine){
                System.out.print(elt +" ,");
            }
            System.out.println();
        }

        solution= finalResult.toTable();

        return(solution);
    }

    /**
     * Verifie si une ligne est résolue ou non
     * @param finalComb
     * @return boolean  (true si résolue et false sinon)
     */
    private boolean verifyState(ArrayList<Integer> finalComb) {
        boolean state=true;
        if (finalComb.contains(0)){
            state=false;
        }
        return(state);
    }


}