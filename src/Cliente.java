import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.Random;
import java.util.StringTokenizer;

public class Cliente {
    private static Socket s2;

    private static BufferedReader leer;
    private static PrintWriter enviar;
    private static BufferedReader leerTerminal;

    //Matrices del juego
    private static Tablero[] tableros;

    private static int[][] tabJ2;
    private static int tabAsignadoJ2;
    private static Point posJ2;
    private static int aciertos;
    private static int fallos;

    private static int ganador;

    public static void main(String[] args) throws UnknownHostException, IOException{
        ServerSocket s1;
        boolean conectado1 = false;
        boolean conectado2 = false;

        Random rdm = new Random();
        initCliente();

        int Puerto1 = Integer.parseInt(args[0]);

        String IP2 = args[2];
        int Puerto2 = Integer.parseInt(args[1]);

        try{
            System.out.println("Se ha iniciado la aplicacion");
            s2 = new Socket(IP2, Puerto2);

            leer = new BufferedReader(new InputStreamReader(s2.getInputStream()));
            enviar = new PrintWriter(s2.getOutputStream());

            tabAsignadoJ2 = rdm.nextInt(tableros.length);

            posJ2 = tableros[tabAsignadoJ2].getStart();
            tabJ2[posJ2.x][posJ2.y] = tableros[tabAsignadoJ2].getValue(posJ2.x, posJ2.y);

            System.out.println("La conexion se ha realizado con exito");
            conectado1 = true;


        }
        catch (UnknownHostException err){
            System.out.println("No se reconoce el nombre del servidor: " + err);
        }
        catch (IOException err){
            conectado1 = false ;

        }

        try{
            s1 = new ServerSocket(Puerto1);
            s1.accept();
            System.out.println("Se ha conectado el otro cliente");
            conectado2 = true;

            if(conectado2 && !conectado1){
                s2 = new Socket(IP2, Puerto2);
                System.out.println("La conexion con el otro cliente se ha realizado correctamente");

                leer = new BufferedReader(new InputStreamReader(s2.getInputStream()));
                enviar = new PrintWriter(s2.getOutputStream());

                tabAsignadoJ2 = rdm.nextInt(tableros.length);

                posJ2 = tableros[tabAsignadoJ2].getStart();
                tabJ2[posJ2.x][posJ2.y] = tableros[tabAsignadoJ2].getValue(posJ2.x, posJ2.y);
            }
            else{
                mostrarTableros();
                while(darJugada()){
                    if(hayGanador()) {
                        ganador = 1;
                        break;
                    }
                }
                if(ganador != 1){
                    enviar.println("FIN TURNO");
                    enviar.flush();
                }
            }
        }
        catch (UnknownHostException err){
            System.out.println("No se reconoce el nombre del servidor: " + err);
        }
        catch (IOException err){
            System.out.println("Error en la conexion: " + err);
        }

        while (ganador == 0){
            String msg = leer.readLine();
            if(msg.equals("ACABA")){
                break;
            }
            else if(msg.equals("JUGAR")){
                System.out.println("\nEs tu turno haz tu jugada\n");
                enviarJugada();
            }
            else if(msg.equals("FIN TURNO")){
                System.out.println("\nEs el turno del contrincante, espera su jugada\n");
                mostrarTableros();
                while(darJugada()){
                    if(hayGanador()){
                        ganador = 1;
                        break;
                    }

                }
                if(ganador != 1){
                    enviar.println("FIN TURNO");
                    enviar.flush();
                }
            }
            System.out.println(msg);
        }

        System.out.println("\nFIN DE LA PARTIDA:");
        if(ganador != 0){
            System.out.println("Rsultados del contrincante: ");
            System.out.println("Aciertos: " + aciertos + "\tFallos: " + fallos);
            enviarResultados();

            String msg = leer.readLine();
            while (!msg.equals("RESULTADOS")){
                System.out.println(msg);
                msg = leer.readLine();
            }

            System.out.println("\nHAS PERDIDO");
        }
        else{
            String msg = leer.readLine();
            while (!msg.equals("RESULTADOS")){
                System.out.println(msg);
                msg = leer.readLine();
            }

            System.out.println("Rsultados del contrincante: ");
            System.out.println("Aciertos: " + aciertos + "\tFallos: " + fallos);
            enviarResultados();

            System.out.println("\nHAS GANADO");
        }



    }

    private static void enviarResultados(){
        enviar.println("ACABA");
        enviar.println("\nFIN DE LA PARTIDA:");
        enviar.println("Tus resultados: ");
        enviar.println("Aciertos: " + aciertos + "\tFallos: " + fallos);
        enviar.println("RESULTADOS");
        enviar.flush();
    }

    private static boolean hayGanador(){
        Point endPos = tableros[tabAsignadoJ2].getEnd();
        return endPos.x == posJ2.x && endPos.y == posJ2.y;
    }

    private static void enviarJugada(){
        try{
            System.out.println("Las coordenadas deben ir en la misma linea separadas por espacios\n");
            enviar.println(leerTerminal.readLine());
            enviar.flush();

            String respuesta = leer.readLine();
            while(!respuesta.equals("ok")){
                System.out.println(respuesta);
                System.out.println("Las coordenadas deben ir en la misma linea separadas por espacios\n");
                enviar.println(leerTerminal.readLine());
                enviar.flush();

                respuesta = leer.readLine();
            }

        }
        catch (IOException err){
            System.out.println("Error en la lectura");
            enviarJugada();
        }

    }

    private static boolean darJugada(){
        boolean inputOk = false;
        Point newPos = new Point();

        enviar.println("Tu posicion es: " + (posJ2.x+1) + ", " + (posJ2.y+1));
        enviar.println("JUGAR");
        enviar.flush();

        while(!inputOk){
            try {
                StringTokenizer st = new StringTokenizer(leer.readLine());
                if (st.countTokens() != 2) {
                    enviar.println("Error el número de parametros indicados es erroneo");
                    enviar.flush();
                    continue;
                }

                int fil = Integer.parseInt(st.nextToken());
                int col = Integer.parseInt(st.nextToken());

                if(fil < 1 || fil > 7){
                    enviar.println("Error el valor de columna proporcionado esta fuera del rango");
                    enviar.flush();
                    continue;
                }

                if(col < 1 || col > 7){
                    enviar.println("Error el valor de columna proporcionado esta fuera del rango");
                    enviar.flush();
                    continue;
                }

                fil -= 1;
                col -= 1;

                if((fil != posJ2.x && col != posJ2.y) || Math.abs(fil - posJ2.x) > 1 || Math.abs(col - posJ2.y) > 1){
                    enviar.println("Error movimiento no permitido");
                    enviar.flush();
                    continue;
                }

                if(fil == posJ2.x && col == posJ2.y){
                    enviar.println("Error, las coordenadas proporcionadas son tu posicion actual");
                    enviar.flush();
                    continue;
                }

                newPos.setLocation(fil, col);
                inputOk = true;
            }
            catch(NumberFormatException err){
                enviar.println("Error, uno de los valores dados no es un numero entero");
                enviar.flush();
            }
            catch (IOException er){
                System.out.println(er);
                return false;
            }
        }

        int value = tableros[tabAsignadoJ2].getValue(newPos.x, newPos.y);
        tabJ2[newPos.x][newPos.y] = value;

        if(value == 1) {
            posJ2 = newPos;
            aciertos++;
        }
        else
            fallos++;

        enviar.println("ok");
        enviar.flush();

        enviar.println("Tu jugada:");
        enviar.println("fila: " + (newPos.x + 1) + " columna: " + (newPos.y + 1));
        enviar.flush();

        System.out.println("Jugada del contrincante:");
        System.out.println("fila: " + (newPos.x + 1) + " columna: " + (newPos.y + 1));

        mostrarTableros();

        return value == 1;
    }

    private static void mostrarTableros(){
        System.out.println("____TABLERO DEL CONTRINCANTE_____");
        enviar.println("______TU TABLERO____");

        for(int i = 0; i <= 7; i++)
        {
            System.out.print(i + " ");
            enviar.print(i + " ");
        }

        System.out.println();
        enviar.println();

        for(int i = 0; i < 7; i++){
            System.out.print((i + 1) + " ");
            enviar.print((i + 1) + " ");
            for(int j = 0; j < 7; j++){
                if(tabJ2[i][j] == -1)
                {
                    System.out.print("? ");
                    enviar.print("? ");
                }
                else if(i == posJ2.x && j == posJ2.y){
                    System.out.print("X ");
                    enviar.print("X ");
                }
                else{
                    System.out.print(tabJ2[i][j] + " ");
                    enviar.print(tabJ2[i][j] + " ");
                }
            }

            System.out.println();
            enviar.println();
        }
        System.out.println("Aciertos: " + aciertos + "\tFallos: " + fallos);
        enviar.println("Aciertos: " + aciertos + "\tFallos: " + fallos);

        enviar.flush();
    }

    private static void initCliente(){
        leerTerminal = new BufferedReader(new InputStreamReader(System.in));
        tabJ2 = new int[7][7];
        generarTableros();

        for(int i = 0; i < 7; i++){
            for(int j = 0; j < 7; j++){
                tabJ2[i][j] = -1;
            }
        }
    }

    private static void generarTableros(){
        tableros = new Tablero[4];

        int[][] tab = new int[][] { {0, 0, 0, 0, 0, 0, 0},
                                    {1, 1, 1, 0, 0, 1, 1},
                                    {0, 0, 1, 0, 0, 1, 0},
                                    {0, 1, 1, 0, 1, 1, 0},
                                    {0, 1, 0, 0, 1, 0, 0},
                                    {0, 1, 0, 0, 1, 0, 0},
                                    {0, 1, 1, 1, 1, 0, 0}};
        tableros[0] = new Tablero(tab , new Point(1, 0), new Point(1, 6));

        tab = new int[][]{  {1, 1, 1, 1, 1, 1, 1},
                            {0, 0, 0, 0, 0, 0, 1},
                            {0, 0, 0, 0, 0, 0, 1},
                            {0, 0, 0, 0, 0, 1, 1},
                            {0, 0, 0, 0, 0, 1, 0},
                            {1, 1, 1, 0, 0, 1, 0},
                            {0, 0, 1, 1, 1, 1, 0}};
        tableros[1] = new Tablero(tab, new Point(5, 0), new Point(0, 0));

        tab = new int[][]{  {0, 1, 1, 1, 1, 1, 0},
                            {0, 1, 0, 0, 0, 1, 0},
                            {0, 1, 1, 1, 0, 1, 0},
                            {0, 0, 0, 1, 0, 1, 1},
                            {0, 1, 1, 1, 0, 0, 0},
                            {0, 1, 0, 0, 0, 0, 0},
                            {0, 1, 1, 1, 1, 1, 1}};
        tableros[2] = new Tablero(tab, new Point(6, 6), new Point(3, 6));

        tab = new int[][]{  {0, 0, 0, 1, 1, 1, 1},
                            {0, 0, 0, 1, 0, 0, 1},
                            {0, 0, 0, 1, 1, 0, 1},
                            {0, 0, 0, 0, 1, 0, 1},
                            {0, 0, 1, 1, 1, 0, 1},
                            {0, 1, 1, 0, 0, 0, 0},
                            {0, 1, 0, 0, 0, 0, 0}};
        tableros[3] = new Tablero(tab, new Point(6, 1), new Point(4, 6));
    }

    public static class Tablero{
        private int[][] tablero;
        private Point start;
        private Point end;

        public Tablero(int[][] t, Point start, Point end){
            this.tablero = t;
            this.start = start;
            this.end = end;
        }

        public int getValue(int x, int y){
            return tablero[x][y];
        }

        public Point getStart() {
            return start;
        }

        public Point getEnd() {
            return end;
        }
    }
}
