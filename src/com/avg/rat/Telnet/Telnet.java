package com.avg.rat.Telnet;

/**
 *
 * @author michal.nedbalek
 */
import java.io.*;
import java.net.*;

/**
 * Hlavní třída programu Telnet. Spouští se příkazem "java Telnet host port".
 */
public class Telnet {

    /**
     * Hlavní metoda. Připojí se k serveru a spustí dvě vlákna TelnetThread.
     * @param args
     */
    public static void main(String[] args) {
        //ověřit počet parametrů
        if (args.length < 2) {
            System.out.println("Použití: java Telnet host port");
            return;
        }

        String hostname = args[0]; //získat hostname

        //získat port
        int port = 0;
        try {
            port = Integer.parseInt(args[1]); //převést parametr na číslo
        } catch (NumberFormatException e) {
            System.out.println("Neplatný port");
            System.exit(-1);
        }

        //vytvořit adresu a soket
        InetSocketAddress addr = new InetSocketAddress(hostname, port);
        Socket socket = new Socket();

        try {
            socket.connect(addr); //pokusit se připojit

            //vlákno, které čte ze soketu a tiskne na konzoli
            Thread reading = new TelnetThread(socket.getInputStream(), System.out);
            //vlákno, které čte z konzole a posílá data do soketu
            Thread writing = new TelnetThread(System.in, socket.getOutputStream());
            writing.setDaemon(true); //vytvořit démona

            //spustit vlákna
            reading.start();
            writing.start();

            reading.join(); //počkat odpojení
            socket.close(); //uzavřit soket
        } /* Probuzení hlavního vlákna z čekání. Nemělo by nastat. */ catch (InterruptedException e) {
            e.printStackTrace();
        } /* Vypršel čas připojení k serveru. */ catch (SocketTimeoutException e) {
            System.out.println("Nelze se připojit k serveru.");
            System.exit(-1);
        } /* Neznámý host. */ catch (UnknownHostException e) {
            System.out.println("Neznámý host.");
            System.exit(-1);
        } /* Jiná IO výjimka. Obvykle NoRouteToHostException nebo ConnectException. */ catch (IOException e) {
            System.out.println("IO chyba:");
            e.printStackTrace();
            System.exit(-1);
        }

    }

    /**
     * Toto vlákno čeká na data ze zadaného vstupního proudu a okamžitě je
     * přeposílá do výstupního proudu.
     */
    static class TelnetThread extends Thread {

        /**
         * Vstupní proud.
         */
        private InputStream is;
        /**
         * Výstupní proud.
         */
        private OutputStream os;

        /**
         * Vytvoří nové vlákno pracující se zadanými proudy.
         */
        public TelnetThread(InputStream is, OutputStream os) {
            this.is = is;
            this.os = os;
        }

        /**
         * Hlavní metoda vlákna.
         */
        public void run() {
            byte[] b = new byte[64]; //vytvořit buffer

            try {
                while (true) {
                    int nbytes = is.read(b); //přečíst bajty
                    if (nbytes == -1) {
                        break; //ověřit konec proudu
                    }
                    os.write(b, 0, nbytes); //zapsat bajty
                }
                System.out.println("Vstupní proud uzavřen.");
            } catch (IOException e) {
                System.out.println("IO chyba:");
                e.printStackTrace();
                System.exit(-1);
            }
        }

    }

}
