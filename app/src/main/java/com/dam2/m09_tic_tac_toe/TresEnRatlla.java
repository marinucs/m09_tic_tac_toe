package com.dam2.m09_tic_tac_toe;

import java.util.Scanner;

/* Programa que implementa el juego del tres en raya.

El juego empieza con el tablero en blanco e irá pidiendo movimientos alternativamente a cada jugador.

Siempre empezará el jugador X. 

Para indicar las coordenadas del movimiento será con la fila y la columna seguidas. Por ejemplo:
    - Casilla (0,0) = "00" (fila 0, columna 0)
    - Casilla (1,2) = "12" (fila 1, columna 2)
    
El juego finaliza cuando se cumplan una de las siguientes condiciones:

    - Si se introduce la letra "a" (sin importar mayús) en lugar de una coordenada válida. 
        En ese caso, el programa mostrará un mensaje de "X abandona" o "0 abandona".      
    - Si un jugador consigue tres en raya.
    - Si todas las casillas están ocupadas (empate).
    
Cuando se procesa una coordenada y la entrada corresponde a casilla libre, se marca la casilla y se muestra el resultado. Si el movimiento no finaliza el juego, pasa el turno al otro jugador.     

*/

public class TresEnRatlla {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        // declara i inicialitza el taulell
        char[][] taulell = new char[3][3];
        buidaTaulell(taulell);
        char jugador = 'X';
        System.out.println("Comença el joc");
        boolean comprova = true;
        // indica quin és el jugador que té el torn

        while (comprova) {
            mostraTaulell(taulell);

            // obté el moviment del jugador actual
            System.out.println(jugador + "?");
            String resposta = sc.nextLine();
            
            if(resposta.equals("a")) { // comprova abandonament 
                System.out.println(jugador + " abandona");
                break; 
            }
            
            if(resposta.length()!=2) {
                System.out.println("Error");
                continue;
            }

            // obté coordenades del moviment
            
            int fila = obteCoordenada
                (resposta.charAt(0), 
                taulell.length-1);
                
            int col = obteCoordenada
                (resposta.charAt(1), 
                taulell.length-1);
                
            if(fila < 0 || col < 0) { 
                System.out.println("Error");
                break;
            }
            
            // comprova si la casella està ocupada
            
            if(casellaOcupada(taulell, fila, col)) {
               comprova = false;
               System.out.println("Ocupada");
               break;
            } else {
                taulell[fila][col] = jugador;
             }         

            // realitza el moviment
            /* XXX taulell[fila][col] = jugador; >>>>>asigna jugador  */
            
            //taulell[fila][col] = jugador;

            // comprova jugador guanya

            if(jugadorGuanya(taulell, jugador)) {   
                System.out.println(jugador + " guanya"); 
                break;
            }    

            // comprova empat
            if(hiHaEmpat(taulell)) System.out.println("Empat");

            // passa torn a l'altre jugador
           
            if(jugador=='X') {
                jugador='O';
            } else {
                jugador='X';
            }
        }

        sc.close();

    }
    
    // buidaTaulell: Posa totes les posicions del taulell a buit
    public static void buidaTaulell(char[][] taulell) {

        for(int linia=0; linia<taulell.length; linia++) {
        
            for(int col=0; col<taulell.length; col++) {
                taulell[linia][col]='·';                          
            }
        } 
    }
    
    // mostraTaulell: Mostra el contingut del taulell //
    public static void mostraTaulell(char[][] taulell) {
    
        for(int linia=0; linia<taulell.length; linia++) {
        
            for(int col=0; col<taulell.length; col++) {
                System.out.print(taulell[linia][col]);                          
            }
                System.out.println();
        }
    }

//casellaOcupada: Retorna cert quan està ocupada la casella corresponent a la fila i columna  
        
    public static boolean casellaOcupada(char[][] taulell, int fila, int col) {
        
        boolean estaOcupada = true;
        
         for(fila=0; fila<taulell.length; fila++) {
        
            for(col=0; col<taulell.length; col++) {
                if(taulell[fila][col]=='·') {
                    estaOcupada = false;
                } else {
                    estaOcupada = true;
                }                
            }
        }
        
        return estaOcupada; 
    }
    
    // jugadorGuanya: Retorna cert quan el jugador ha fet un tres en ratlla al taulell i espera que jugador tingui com a valor 'X' o 'O'. //
    
    public static boolean jugadorGuanya(char[][] taulell, char jugador) {
        
        boolean guanya = true;
        
        // bucle dins dun bucle, comprovar todas las filas 
        for(int fila=0; fila<taulell.length; fila++) {
        
           for(int col=0; col<taulell.length; col++) {
                if (taulell[fila][col] != jugador) {
                    guanya = false;
                    break;
                }            
           }
        }

        //comprova jugador té tota la fila 0 -> función aparte 
        
        for(int col=0; col<taulell.length; col++) {
              if (taulell[0][col] != jugador) {
                guanya = false;
                break;
              }
        }
        
        // comprovar diagonal + diagonal inversa (???)

        return guanya;
    }

    // hiHaEmpat: Retorna cert quan ja no es poden fer més moviments//
    
    public static boolean hiHaEmpat(char[][] taulell) {
        
        boolean empat = true;
        // bucle anidat buscant un espai , si arrives fins al final hi ha empat
        
        for(int col=0; col<taulell.length; col++) {
              if (taulell[0][col] == '·') {
                empat = false; 
                break;
              }
        }
        
        return empat;
    }
    
    /* obteCoordenada: retorna valoro númeric si és vàlid per una coordenada o 1 altrament 

    - Formato incorrecto: la entrada no está formada por 2 dígitos entre el 0 y el 2.
        se muestra el mensaje "Error" y se vuelve a pedir movimiento al mismo jugador.
        
    */
    
    public static int obteCoordenada(char valor, int valorMax) {
    
        if(valor < '0' || valor > '9') return-1;
        
        int resultat = valor - '0'; // pq 0 es caracter 48 en ascii 49 es el 1 etc. 
        if(resultat <= valorMax) {
            return resultat;
        }
        
        return -1;          
    }
}