/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ncbitax;

import database.Transacciones;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * This program is created to work as an interface between the user and the
 * taxonomy database. It offers multiple options in order to allow the user
 * create different mapping files, as well as retrieve taxonomic linage, LCA,
 * among other utilities.
 *
 * @author aabdala
 */
public class Mapptaxids {

    /**
     * Main method
     *
     * @param args arguments, run program with -h or --help
     */
    public static void main(String[] args) {
        String database = "taxomap";
        String user = "aabdala";
        String host = "localhost";
        String password = "guest@nioz";
        /*
        String user = "root";
        String host = "localhost";
        String password = "amorphis";
        String database = "ncbitax";
         */
        String log = "";
        String mappFile = "";
        String fastaFile = "";
        String sep = ";";
        String outFile = "mappedFile.txt";
        String split = ""; //how to split
        String rankType = "";//ALL or blanck | KNOWN | CLASSIC | CUSTOM  
        String rankValues = ""; //used for custom rankType
        String mode = "SINGLE";//SINGLE or blank | BATCH  | DB
        int batch = 10;
        boolean debug = false;
        boolean notFasta = false;
        boolean appendTaxID = false;
        boolean withHashMap = false;
        int taxCol = -1;//which column of the spplited line contains the taxid (from 1)
        int accCol = -1;//which column of the spplited line contains the acc (from 1)
        if (args.length == 0 || args[0].equals("-h") || args[0].equals("--help")) {
            System.out.println(help());
            System.exit(0);
        }
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-m") || args[i].equals("--map")) {
                try {
                    i++;
                    mappFile = args[i];
                } catch (ArrayIndexOutOfBoundsException aoie) {
                    System.err.println("Argument expected for mapping file \nNeed help? use Mapptaxids -h | --help");
                    System.exit(1);

                }
            } else if (args[i].equals("-f") || args[i].equals("--fasta")) {
                try {
                    i++;
                    fastaFile = args[i];
                } catch (ArrayIndexOutOfBoundsException aoie) {
                    System.err.println("Argument expected for -f (fasta file) option \nNeed help? use Mapptaxids -h | --help");
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
            } else if (args[i].equals("-e") || args[i].equals("--sep")) {
                try {
                    i++;
                    sep = args[i];
                } catch (ArrayIndexOutOfBoundsException aoie) {
                    System.err.println("Argument expected for -sep option \nNeed help? use Mapptaxids -h | --help");
                    System.exit(1);

                }
            } else if (args[i].equals("-M") || args[i].equals("--mode")) {
                try {
                    i++;
                    mode = args[i].toUpperCase();
                } catch (ArrayIndexOutOfBoundsException aoie) {
                    System.err.println("Argument expected for -m (mode) option \nNeed help? use Mapptaxids -h | --help");
                    System.exit(1);

                }
            } else if (args[i].equals("-o") || args[i].equals("--out")) {
                try {
                    i++;
                    outFile = args[i];
                } catch (ArrayIndexOutOfBoundsException aoie) {
                    System.err.println("Argument expected for -o option (output file) \nNeed help? use Mapptaxids -h | --help");
                    System.exit(1);

                }
            } else if (args[i].equals("-r") || args[i].equals("--rank")) {
                try {
                    i++;
                    rankType = args[i].toUpperCase();
                    if (rankType.equals("CUSTOM")) { //se espera una linea de string comma separated con los niveles deseados
                        try {
                            i++;
                            rankValues = args[i];
                        } catch (ArrayIndexOutOfBoundsException aoie) {
                            System.err.println("Argument expected for -r with option CUSTOM (rankType) i.e -r CUSTOM kingdom,phylum,class \nNeed help? use Mapptaxids -h | --help");
                            System.exit(1);

                        }
                    }
                } catch (ArrayIndexOutOfBoundsException aoie) {
                    System.err.println("Argument expected for -r option (rankType) \nNeed help? use Mapptaxids -h | --help");
                    System.exit(1);

                }
            } else if (args[i].equals("-tc") || args[i].equals("--tax-column")) {
                try {
                    i++;
                    taxCol = Integer.parseInt(args[i]);

                } catch (ArrayIndexOutOfBoundsException aoie) {
                    System.err.println("Argument expected for -c option \nNeed help? use Mapptaxids -h | --help");
                    System.exit(1);
                } catch (NumberFormatException nfe) {
                    System.err.println("Numeric argument expected for -tc option \nNeed help? use Mapptaxids -h | --help");
                    System.exit(1);

                }
            } else if (args[i].equals("-ac") || args[i].equals("--acc-column")) {
                try {
                    i++;
                    accCol = Integer.parseInt(args[i]);

                } catch (ArrayIndexOutOfBoundsException aoie) {
                    System.err.println("Argument expected for -ac option \nNeed help? use Mapptaxids -h | --help");
                    System.exit(1);
                } catch (NumberFormatException nfe) {
                    System.err.println("Numeric argument expected for -ac option \nNeed help? use Mapptaxids -h | --help");
                    System.exit(1);

                }
            } else if (args[i].equals("-b") || args[i].equals("--batch")) {
                try {
                    i++;
                    batch = Integer.parseInt(args[i]);

                } catch (ArrayIndexOutOfBoundsException aoie) {
                    System.err.println("Argument expected for -b option \nNeed help? use Mapptaxids -h | --help");
                    System.exit(1);
                } catch (NumberFormatException nfe) {
                    System.err.println("Numeric argument expected for -b option \nNeed help? use Mapptaxids -h | --help");
                    System.exit(1);

                }
            } else if (args[i].equals("-s") || args[i].equals("--split")) {
                try {
                    i++;
                    split = args[i];

                } catch (ArrayIndexOutOfBoundsException aoie) {
                    System.err.println("Argument expected for -s option  \nNeed help? use Mapptaxids -h | --help");
                    System.exit(1);
                }
            } else if (args[i].equals("-v") || args[i].equals("--verbose")) {
                debug = true;
            } else if (args[i].equals("-n") || args[i].equals("--not-fasta")) {
                notFasta = true;
            } else if (args[i].equals("-h") || args[i].equals("--hash")) {
                withHashMap = true;
            } else if (args[i].equals("-t") || args[i].equals("--taxid")) {
                appendTaxID = true;
            } else {
                System.err.println("Wrong argument: " + args[i] + "\nNeed help? use Mapptaxids -h | --help");
                System.exit(1);
            }
        }
        if ((!mappFile.equals("") && !fastaFile.equals("") && !mode.equals("DB")) || (!mappFile.equals("") && mode.equals("DB")) || (!mappFile.equals("") && fastaFile.equals("") && mode.equals("MERGED")) || (!mappFile.equals("") && mode.equals("QIIME"))) {
            Transacciones transacciones = new Transacciones(database, user, host, password);
            // NCBITaxCreator ncbi = new NCBITaxCreator(transacciones);
            // log += ncbi.createTaxaListFromNCBI(nodes, names,true);
            NCBIFastaMapping fmapp = new NCBIFastaMapping(mappFile, fastaFile, transacciones);
            fmapp.setOutput(outFile);
            fmapp.setAppendTaxid(appendTaxID);
            fmapp.setWithHashMap(withHashMap);
            fmapp.setSep(sep);
            fmapp.setNotFasta(notFasta);
            if (rankType.length() > 0) {
                fmapp.setRankType(rankType);
            }
            if (rankValues.length() > 0) {
                fmapp.setRankValues(rankValues);
            }
            if (taxCol > 0) {
                fmapp.setTaxIDCol(taxCol);
            }
            if (accCol > 0) {
                fmapp.setAccCol(accCol);
            }
            if (split.length() > 0) {
                fmapp.setSplitChars(split);
            }
            if (debug) {
                fmapp.setDebug(debug);
            }
            try {
                if (mode.equals("SINGLE")) {
                    fmapp.mappTaxIDs();
                } else if (mode.equals("BATCH")) {
                    fmapp.mappBulkTaxIDs(batch);
                } else if (mode.equals("DB")) {
                    if (fmapp.getRankType().equals("CLASSIC")) {
                        fmapp.completeTaxaQiime(false);
                    } else {
                        fmapp.completeTaxa();
                    }

                } else if (mode.equals("MERGED")) {
                    fmapp.processNotAtDB();
                } else if (mode.equals("QIIME")) {
                    fmapp.completeTaxaQiime(true);
                } else {
                    System.err.println("Wrong mode: " + mode + "\nValid modes are: SINGLE, BATCH and DB. Need help? use Mapptaxids -h | --help");
                    System.exit(1);
                }

            } catch (FileNotFoundException fnfe) {
                System.err.println("Some of the input files can not be found. Please see the following log for more details:\n" + fnfe.getMessage());
            } catch (IOException ioe) {
                System.err.println("Error accessing file. Please see the following log for more details:\n" + ioe.getMessage());
            }
        } else {
            System.err.println("This program needs");
        }
    }

    /**
     * Method to display the help menu
     *
     * @return
     */
    private static String help() {
        String help = "\n#######################################################\n"
                + "###            NCBI TAXONOMI MAPPER                 ###\n"
                + "###                    v 1.4                        ###\n"
                + "###                             @company       NIOZ ###\n"
                + "###                             @author   A. Abdala ###\n"
                + "#######################################################\n\n"
                + "Program for mapping accesion numbers to tax ids and tax ids to taxonomic linage\n"
                + "usage java ncbitax.Mapptaxids -m Mapping file -f Fasta file [options]\n\n"
                + "Mandatory arguments:\n"
                + "  -m\t--map\tMapping file with correspondance between accession numbers and tax ids. This kind of files can be found at: ftp://ftp.ncbi.nlm.nih.gov/pub/taxonomy/accession2taxid/README\n"
                + "  -f\t--fasta\tFasta file with the accession number to be mapped against tax id. Only for mode DB it is not mandatory!\n"
                + "\nOptional arguments:\n"
                + "  -tc\t--tax-column\tThe column on the mapping file were the taxid is found (def value equal to NCBI mapping files default col = 3)\n"
                + "  -ac\t--acc-column\tThe column on the mapping file were the accession number is found (def value equal to NCBI mapping files default col = 2)\n"
                + "  -o\t--output\tName of the output file\n"
                + "  -e\t--sep\tChar to separate taxonomic levels. Default is ';'\n"
                + "  -s\t--split\tChar or regex to split mapping file. Default is tab '\\t'\n"
                + "  -n\t--not-fasta\tIf the input file to be mapped is not a fasta file, it should be a file with a list of accessions on that case, use this flag\n"
                + "  -b\t--batch\tUsed on BATCH mode. Number of elements to be searched on one single grep.\n"
                + "  -M\t--mode\tThis flag determines the search strategy. Valid option are:"
                + "\n  \t\tSINGLE Default value. Needs the mapping file and the fasta file. Performs the search by grepping element per element"
                + "\n  \t\tBATCH Needs the mapping file and the fasta file. Performs the search by grepping A BATCH of elements. The default batch number is ten but can by changed with -b or --batch options"
                + "\n  \t\tDB This option suppose that the user already have a mapped file with only all the required accessions and tax ids, so it only takes the maping file and search for the taxonomic information into the DB."
                + "\n  \t\tQIIME This option suppose that the user already have a mapped file with only all the required accessions and tax ids, so it only takes the maping file and search for the taxonomic information into the DB and create a mapping file like the one required for QIIME assign_taxonomy.py script."
                + "\n  \t\tMERGED This option takes as input the mapping file, which in this case should be the output file generated when the tax id is not found on reference DB, on this cases the file extension is '.not_at_db'.\n"
                + "  -t\t--taxid\tIf this flag is present the output file will contain an extra column with the taxid of the linage\n"
                + "  -h\t--hash\tIf this flag is present the program will use HashMap to reduce overhead on database access. IN order to use this option be sure to have enough memory. If you use this option posible HeapOverflowException\n"
                + "  -r\t--rank\tTaxonomic ranks to be considered for the ouput. Valid options are:"
                + "\n  \t\tALL Default value. returns all the taxonomic levels "
                + "\n  \t\tKNOWN Discard all taxonomic levels with label 'no rank' "
                + "\n  \t\tCLASSIC use 'regular' taxa levels: kingdom, phylum, class, order, family, genus, species, subspecies"
                + "\n  \t\tCUSTOM this option allows to select desired taxa levels, so after using this flag, comma separated levels should be included."
                + "\n  \t\tValid taxonomic levels are: kingdom,superkingdom,subkingdom,superphylum,phylum,subphylum,superclass,infraclass,class,subclass,parvorder,superorder,infraorder,order,suborder,superfamily,family,subfamily,tribe,subtribe,genus,subgenus,species,species group,species subgroup,subspecies,forma,varietas";

        return help;
    }
}
