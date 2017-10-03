/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ncbitax;

import database.Transacciones;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import utils.StringUtils;

/**
 * This class is created to parse output files extracted from blast dbs with
 * blastdbcmd, like the following one: blastdbcmd -entry all -db
 * ncbi_nt/blast/nt -outfmt "%a;%g;%i;%T;%t" -out nt.idlist. In order to parse
 * it an load different tables like ncbi_nt, ncbi_nr
 *
 * @author aabdala
 */
public class BlastdbParser {

    private Transacciones transacciones;

    public BlastdbParser(Transacciones transacciones) {
        this.transacciones = transacciones;
    }

    public BlastdbParser() {
        this.transacciones = null;
    }

    /**
     * Method for parse and insert/write nt or nr records into relational
     * database
     *
     * @param input input file with blastdbcmd result. Expected format: -outfmt
     * "%a;%g;%i;%T;%t"
     * @param output if present queries are writen into outfile, otherwise
     * directly inserted into DBf
     * @param table the name of the table where the records are writen, for the
     * moment possible values are: nt, nr
     */
    public void parseFile(String input, String output, String table) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(input));
            FileWriter writer = null;
            boolean toOF = false;
            if (output.length() > 0) {
                writer = new FileWriter(output);
                toOF = true;
            }
            String linea;
            StringUtils su = new StringUtils();
            int ok = 0;
            int nok = 0;
            int line = 0;
            System.out.println("*********************\n"
                    + "Processing file:" + input);
            while ((linea = reader.readLine()) != null) {
                line++;
                String tokens[] = linea.split(";");
                String title = tokens[4];
                //if title contains ; it will be splited on more tokens
                for (int i = 5; i < tokens.length; i++) {
                    title += ";" + tokens[i];
                }
                String query = "INSERT IGNORE INTO " + table + " VALUES('" + tokens[0] + "', " + tokens[1] + ", '" + tokens[2] + "'," + tokens[3] + ", '" + su.scapeSQL(title) + "')";
                if (toOF) {
                    writer.write(query + ";\n");
                    ok++;
                } else {
                    if (transacciones.insertaQuery(query)) {
                        ok++;
                    } else {
                        System.err.println("Error inserting query: " + query + "\nline:" + line);
                        nok++;
                    }
                }
            }
            System.out.println("Lines processed: " + line);
            if (toOF) {
                System.out.println("Queries created: " + ok);
                System.out.println("Outputfile: " + output);
            } else {
                System.out.println("Records inserted: " + ok);
                System.out.println("Records NOK: " + nok);
            }
            System.out.println("*********************");

        } catch (FileNotFoundException ex) {
            Logger.getLogger(BlastdbParser.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("There is no file: " + input);
        } catch (IOException ex) {
            Logger.getLogger(BlastdbParser.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Error writing file: " + output);
        }
    }
}
