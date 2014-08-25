package control;

import util.ConfigurationFile;
import dao.DominoesDao;
import dao.DaoFactory;
import domain.Configuration;
import domain.Dominoes;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

public class Controller {

    /**
     * This function has begin when the user want using all matrices in Dominoes
     * database.
     *
     * @return Dominoes List
     * @throws IOException
     */
    public static ArrayList<Dominoes> loadAllMatrices() {
        DominoesDao result = DaoFactory.getDominoesDao(Configuration.accessMode);
        if (result == null) {
            throw new IllegalArgumentException("Invalid argument.\nAccess mode not defined");
        }
        try {
            return result.loadAllMatrices();
        } catch (IOException ex) {
        	ex.printStackTrace();
            return null;
        } catch (SQLException ex){
        	ex.printStackTrace();
        	return null;
        } catch (Exception ex){
        	ex.printStackTrace();
        	return null;
        }
    }
    
    /**
     * This function is used to initialize the Configuration class
     * @throws IOException
     * @throws Exception 
     */
    public static void loadConfiguration() throws IOException, Exception{
        new ConfigurationFile().loadConfigurationFile();
    }

    /**
     * This function has begin when the user want using a matrix in Dominoes
     * database.
     *
     * @param dominoes the row and the column this dominoes will be used in the
     * search to load
     * @return The domino, in database, which contains the row and the column
     * equal to row and column of the domino passed how parameters to this
     * function
     * @throws IOException
     */
    public static Dominoes loadMatrix(Dominoes dominoes) throws IOException {
        DominoesDao result = DaoFactory.getDominoesDao(Configuration.accessMode);
        if (result == null) {
            throw new IllegalArgumentException("Invalid argument.\nAccess mode not defined");
        }
        return result.loadMatrix(dominoes);
    }
    
    /**This function has begin when the user want to multiply two matrices. 
     *
     * @param dom1 First operator in multiplication
     * @param dom2 Second operator in multiplication
     * @return The result of multiplication
     */
    public static Dominoes MultiplyMatrices(Dominoes dom1, Dominoes dom2) {
        // call dominoes
        Dominoes result = dom1.multiply(dom2);
        return result;
    }
    
    /**
     * This functions is called when user want remove a matrix of database
     * @param dominoes The dominoes corresponding to matrix
     * @return True, in affirmative case
     * @throws IOException 
     */
    public static boolean removeMatrix(Dominoes dominoes) throws IOException {
        DominoesDao result = DaoFactory.getDominoesDao(Configuration.accessMode);

        if (result == null) {
            throw new IllegalArgumentException("Invalid argument.\nAccess mode not defined");
        }
        
        return result.removeMatrix(dominoes);
    }
    
    /**
     * This function has begin when the user want to save a matrix
     *
     * @param dominoes information to be saved
     * @return true, in case afirmative.
     * @throws IOException
     */
    public static boolean saveMatrix(Dominoes dominoes) throws IOException {
        DominoesDao result = DaoFactory.getDominoesDao(Configuration.accessMode);
        if (result == null) {
            throw new IllegalArgumentException("Invalid argument.\nAccess mode not defined");
        }
        return result.saveMatrix(dominoes);
    }

    /**This function has begin when the user want to transpose a matrix.
     * 
     * @param domino Matrix to be transposed
     * @return Return the transpose of the matrix in the parameter
     */
    public static Dominoes tranposeDominoes(Dominoes domino) {
        
    	domino.transpose();
    	
        return domino;

    }
    
    /**This function has begin when the user want to reduce a matrix.
     * 
     * @param domino Matrix to be reduced
     * @return Return the reduced matrix in the parameter
     */
    public static Dominoes reduceDominoes(Dominoes domino) {
        
    	domino.reduceRows();
    	
        return domino;
    }

    public static double opposite(double size, double index){
    	if(size < 0 || index < 0 || index > size){
    		throw new IllegalArgumentException("Invalid parameter."
    				+ "\nController.opposite(...) parameter is invalid");
    	}
    	double result = Math.abs(index - size);
    	return result;
   }
}
