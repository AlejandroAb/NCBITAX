/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ncbitax;

import database.Transacciones;

/**
 * Main clas for accession and sequence utilities, like subsetting or parsing
 * ids
 *
 * @author aabdala
 */
public class AccessionUtils {
    
    public static void main(String[] args) {
        /* String database = "ncbitax";
        String user = "root";
        String host = "localhost";
        String password = "amorphis";
         */
        String database = "taxomap";
        String user = "aabdala";
        String host = "localhost";
        String password = "guest@nioz";
        String input = "";
        String table = "";
        String output = "";
        String mode = "";
        
        if (args.length == 0 || args[0].equals("-h") || args[0].equals("--help")) {
            System.out.println(help());
            System.exit(0);
        }
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-a") || args[i].equals("--accession_list")) {
                try {
                    i++;
                    input = args[i];
                } catch (ArrayIndexOutOfBoundsException aoie) {
                    System.err.println("Argument expected for accession_list  \nNeed help? use java ncbitax.AccessionUtils -h | --help");
                    System.exit(1);
                    
                }
                
            } else if (args[i].equals("-db") || args[i].equals("--database")) {
                try {
                    i++;
                    table = args[i];
                } catch (ArrayIndexOutOfBoundsException aoie) {
                    System.err.println("Argument expected for database  \nNeed help? use java ncbitax.AccessionUtils -h | --help");
                    System.exit(1);
                    
                }
                
            } else if (args[i].equals("-M") || args[i].equals("--MODE")) {
                try {
                    i++;
                    mode = args[i];
                } catch (ArrayIndexOutOfBoundsException aoie) {
                    System.err.println("Argument expected for -M option \nNeed help? use java ncbitax.AccessionUtils -h | --help");
                    System.exit(1);
                    
                }
            } else if (args[i].equals("-o") || args[i].equals("--out")) {
                try {
                    i++;
                    output = args[i];
                } catch (ArrayIndexOutOfBoundsException aoie) {
                    System.err.println("Argument expected for --out option \nNeed help? use java ncbitax.AccessionUtils -h | --help");
                    System.exit(1);
                    
                }
            } else if (args[i].equals("-p") || args[i].equals("--pass")) {
                try {
                    i++;
                    password = args[i];
                } catch (ArrayIndexOutOfBoundsException aoie) {
                    System.err.println("Argument expected for --pass option \nNeed help? use java ncbitax.AccessionUtils -h | --help");
                    System.exit(1);
                    
                }
            } else if (args[i].equals("-rd") || args[i].equals("--relational_database")) {
                try {
                    i++;
                    database = args[i];
                } catch (ArrayIndexOutOfBoundsException aoie) {
                    System.err.println("Argument expected for --database option \nNeed help? use java ncbitax.AccessionUtils -h | --help");
                    System.exit(1);
                    
                }
            }
        }
        if (mode.equals("PARSE")) {
            if (!input.equals("") || !table.equals("")) {
                Transacciones transacciones = new Transacciones(database, user, host, password);
                BlastdbParser bp = new BlastdbParser(transacciones);
                bp.parseFile(input, output, table);
                
            } else {
                System.err.println("MODE=PARSE This mode requires -a accessionList and -db ncbi_nt | ncbi_nr ");
            }
            
        } else {
            System.err.println("Incorrect mode operand. It should be one from: PARSE, SUBSET \nNeed help? use java ncbitax.AccessionUtils -h | --help");
        }
    }
    
    private static String help() {
        String help = "\n#####################################################\n"
                + "###            NCBI ACCESSION UTILS               ###\n"
                + "###                    v 1.0                      ###\n"
                + "###                             @author A. Abdala ###\n"
                + "####################################################\n\n"
                + "Usage java ncbitax.AccessionUtils -M MODE [options according to MODE]\n"
                + "Modes:\n"
                + "\tPARSE\tThis option parse a file with accessions, gis, seq_ids, tax_ids and seq tittles in order to store them into the relational database.\n\t\t*It requires  -a -db arguments\n"
                + "Arguments:\n"
                + "  -a\t--accession_list\tfile with result from blastdbcmd with -outfmt \"%a;%g;%i;%T;%t\".\n"
                + "  -o\t--out\tName of the outfile, otherwise write directly into database.\n"
                + "  -db\t--database\tName of the target database for which the accession numbers will be loaded.\n\t\tValid databases are: ncbi_nt, ncbi_nr";
        
        return help;
    }
    
}
