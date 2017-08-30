/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ncbitax;

import database.Transacciones;

/**
 *
 * @author aabdala
 */
public class NCBIloader {

    /**
     * CLASSPATH: $export
     * CLASSPATH=/home/NIOZ.NL/aabdala/NetBeansProjects/NCBITAX/build/classes/:/home/NIOZ.NL/aabdala/javalibs/
     *
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
         String database = "ncbitax";
        String user = "root";
        String host = "localhost";
        String password = "amorphis";
        /*String database = "taxomap";
        String user = "aabdala";
        String host = "localhost";
        String password = "guest@nioz";
*/
        String log = "";
        String nodes = "";
        String names = "";
        String merge = "";
        if (args.length == 0 || args[0].equals("-h") || args[0].equals("--help")) {
            System.out.println(help());
            System.exit(0);
        }
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-n") || args[i].equals("--nodes")) {
                try {
                    i++;
                    nodes = args[i];
                } catch (ArrayIndexOutOfBoundsException aoie) {
                    System.err.println("Argument expected for nodes dmp file \nNeed help? use ncbiloader -h | --help");
                    System.exit(1);

                }
            } else if (args[i].equals("-a") || args[i].equals("--names")) {
                try {
                    i++;
                    names = args[i];
                } catch (ArrayIndexOutOfBoundsException aoie) {
                    System.err.println("Argument expected for names dmp file \nNeed help? use ncbiloader -h | --help");
                    System.exit(1);

                }
            } else if (args[i].equals("-m") || args[i].equals("--merge")) {
                try {
                    i++;
                    merge = args[i];
                } catch (ArrayIndexOutOfBoundsException aoie) {
                    System.err.println("Argument expected for merge dmp file \nNeed help? use ncbiloader -h | --help");
                    System.exit(1);

                }
            } else if (args[i].equals("-u") || args[i].equals("--user")) {
                try {
                    i++;
                    user = args[i];
                } catch (ArrayIndexOutOfBoundsException aoie) {
                    System.err.println("Argument expected for --user option \nNeed help? use Mapptaxids -h | --help");
                    System.exit(1);

                }
            } else if (args[i].equals("-p") || args[i].equals("--pass")) {
                try {
                    i++;
                    password = args[i];
                } catch (ArrayIndexOutOfBoundsException aoie) {
                    System.err.println("Argument expected for --pass option \nNeed help? use Mapptaxids -h | --help");
                    System.exit(1);

                }
            } else if (args[i].equals("-d") || args[i].equals("--database")) {
                try {
                    i++;
                    database = args[i];
                } catch (ArrayIndexOutOfBoundsException aoie) {
                    System.err.println("Argument expected for --database option \nNeed help? use Mapptaxids -h | --help");
                    System.exit(1);

                }
            }
        }
        if (!nodes.equals("") && !names.equals("")) {
            Transacciones transacciones = new Transacciones(database, user, host, password);
            NCBITaxCreator ncbi = new NCBITaxCreator(transacciones);
            log += ncbi.createTaxaListFromNCBI(nodes, names, true);
        } else if (!merge.equals("")) {
            Transacciones transacciones = new Transacciones(database, user, host, password);
            NCBITaxCreator ncbi = new NCBITaxCreator(transacciones);
            ncbi.loadMergedTaxs(merge);            
        }
    }

    private static String help() {
        String help = "\n#####################################################\n"
                + "###            NCBI TAXONOMI DB LOADER            ###\n"
                + "###                    v 2.0                      ###\n"
                + "###                             @author A. Abdala ###\n"
                + "####################################################\n\n"
                + "usage java ncbitax.ncbiloader -n NCBI_NODES_FILE.dmp -a NCBI_NAMES_FILE.dmp\n\n"
                + "-----   The help menu is under development    ------";

        return help;
    }

}
