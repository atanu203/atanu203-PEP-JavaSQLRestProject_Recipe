package com.revature.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.revature.util.ConnectionUtil;
import com.revature.util.Page;
import com.revature.util.PageOptions;
import com.revature.model.Ingredient;




/**
 * The IngredientDAO class handles the CRUD operations for Ingredient objects. It provides methods for creating, retrieving, updating, and deleting Ingredient records from the database. 
 * 
 * This class relies on the ConnectionUtil class for database connectivity and also supports searching and paginating through Ingredient records.
 */

public class IngredientDAO {

    /** A utility class used for establishing connections to the database. */
    @SuppressWarnings("unused")
    private ConnectionUtil connectionUtil;

    /**
     * Constructs an IngredientDAO with the specified ConnectionUtil for database connectivity.
     * 
     * TODO: Finish the implementation so that this class's instance variables are initialized accordingly.
     * 
     * @param connectionUtil the utility used to connect to the database
     */
    public IngredientDAO(ConnectionUtil connectionUtil) {
        this.connectionUtil = connectionUtil;
    }

    /**
     * TODO: Retrieves an Ingredient record by its unique identifier.
     *
     * @param id the unique identifier of the Ingredient to retrieve.
     * @return the Ingredient object with the specified id.
     */
    public Ingredient getIngredientById(int id) {
        String sql = "SELECT * FROM Ingredient WHERE id = ?";
        try (var conn = connectionUtil.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapSingleRow(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * TODO: Creates a new Ingredient record in the database.
     *
     * @param ingredient the Ingredient object to be created.
     * @return the unique identifier of the created Ingredient.
     */
    public int createIngredient(Ingredient ingredient) {
        String sql = "INSERT INTO Ingredient (name) VALUES (?)";
        try (var conn = connectionUtil.getConnection();
             var stmt = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, ingredient.getName());
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) throw new SQLException("Creating ingredient failed, no rows affected.");
            try (var keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    ingredient.setId(id);
                    return id;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * TODO: Deletes an ingredient record from the database, including references in related tables.
     *
     * @param ingredient the Ingredient object to be deleted.
     */
    public void deleteIngredient(Ingredient ingredient) {
        String sql = "DELETE FROM Ingredient WHERE id = ?";
        try (var conn = connectionUtil.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, ingredient.getId());
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * TODO: Updates an existing Ingredient record in the database.
     *
     * @param ingredient the Ingredient object containing updated information.
     */
    public void updateIngredient(Ingredient ingredient) {
        String sql = "UPDATE Ingredient SET name = ? WHERE id = ?";
        try (var conn = connectionUtil.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, ingredient.getName());
            stmt.setInt(2, ingredient.getId());
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * TODO: Retrieves all ingredient records from the database.
     *
     * @return a list of all Ingredient objects.
     */
    public List<Ingredient> getAllIngredients() {
        List<Ingredient> ingredients = new ArrayList<>();
        String sql = "SELECT * FROM Ingredient ORDER BY id ASC";
        try (var conn = connectionUtil.getConnection();
             var stmt = conn.createStatement();
             var rs = stmt.executeQuery(sql)) {
            ingredients = mapRows(rs);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ingredients;
    }

    /**
     * TODO: Retrieves all ingredient records from the database with pagination options.
     *
     * @param pageOptions options for pagination and sorting.
     * @return a Page of Ingredient objects containing the retrieved ingredients.
     */
    public Page<Ingredient> getAllIngredients(PageOptions pageOptions) {
        String sortBy = pageOptions.getSortBy() != null ? pageOptions.getSortBy() : "id";
        String sortDirection = pageOptions.getSortDirection() != null ? pageOptions.getSortDirection() : "ASC";
        // Only allow sorting by id or name for safety
        if (!sortBy.equalsIgnoreCase("id") && !sortBy.equalsIgnoreCase("name")) sortBy = "id";
        if (!sortDirection.equalsIgnoreCase("asc") && !sortDirection.equalsIgnoreCase("desc")) sortDirection = "ASC";
        String sql = "SELECT * FROM Ingredient ORDER BY " + sortBy + " " + sortDirection;
        try (var conn = connectionUtil.getConnection();
             var stmt = conn.createStatement();
             var rs = stmt.executeQuery(sql)) {
            return pageResults(rs, pageOptions);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * TODO: Searches for Ingredient records by a search term in the name.
     *
     * @param term the search term to filter Ingredient names.
     * @return a list of Ingredient objects that match the search term.
     */
    public List<Ingredient> searchIngredients(String term) {
        List<Ingredient> ingredients = new ArrayList<>();
        String sql = "SELECT * FROM Ingredient WHERE name LIKE ? ORDER BY id ASC";
        try (var conn = connectionUtil.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + term + "%");
            try (var rs = stmt.executeQuery()) {
                ingredients = mapRows(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ingredients;
    }

    /**
     * TODO: Searches for Ingredient records by a search term in the name with pagination options.
     *
     * @param term the search term to filter Ingredient names.
     * @param pageOptions options for pagination and sorting.
     * @return a Page of Ingredient objects containing the retrieved ingredients.
     */
    public Page<Ingredient> searchIngredients(String term, PageOptions pageOptions) {
        String sortBy = pageOptions.getSortBy() != null ? pageOptions.getSortBy() : "id";
        String sortDirection = pageOptions.getSortDirection() != null ? pageOptions.getSortDirection() : "ASC";
        if (!sortBy.equalsIgnoreCase("id") && !sortBy.equalsIgnoreCase("name")) sortBy = "id";
        if (!sortDirection.equalsIgnoreCase("asc") && !sortDirection.equalsIgnoreCase("desc")) sortDirection = "ASC";
        String sql = "SELECT * FROM Ingredient WHERE name LIKE ? ORDER BY " + sortBy + " " + sortDirection;
        try (var conn = connectionUtil.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + term + "%");
            try (var rs = stmt.executeQuery()) {
                return pageResults(rs, pageOptions);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // below are helper methods for your convenience

    /**
     * Maps a single row from the ResultSet to an Ingredient object.
     *
     * @param resultSet the ResultSet containing Ingredient data.
     * @return an Ingredient object representing the row.
     * @throws SQLException if an error occurs while accessing the ResultSet.
     */
    private Ingredient mapSingleRow(ResultSet resultSet) throws SQLException {
        // Use lowercase column names to match most DB setups
        return new Ingredient(resultSet.getInt("id"), resultSet.getString("name"));
    }

    /**
     * Maps multiple rows from the ResultSet to a list of Ingredient objects.
     *
     * @param resultSet the ResultSet containing Ingredient data.
     * @return a list of Ingredient objects.
     * @throws SQLException if an error occurs while accessing the ResultSet.
     */
    private List<Ingredient> mapRows(ResultSet resultSet) throws SQLException {
        List<Ingredient> ingredients = new ArrayList<Ingredient>();
        while (resultSet.next()) {
            ingredients.add(mapSingleRow(resultSet));
        }
        return ingredients;
    }

    /**
     * Paginates the results of a ResultSet into a Page of Ingredient objects.
     *
     * @param resultSet the ResultSet containing Ingredient data.
     * @param pageOptions options for pagination and sorting.
     * @return a Page of Ingredient objects containing the paginated results.
     * @throws SQLException if an error occurs while accessing the ResultSet.
     */
    private Page<Ingredient> pageResults(ResultSet resultSet, PageOptions pageOptions) throws SQLException {
        List<Ingredient> ingredients = mapRows(resultSet);
        int offset = (pageOptions.getPageNumber() - 1) * pageOptions.getPageSize();
        int limit = Math.min(offset + pageOptions.getPageSize(), ingredients.size());
        List<Ingredient> subList = new ArrayList<>();
        if (offset < ingredients.size()) {
            subList = ingredients.subList(offset, limit);
        }
        return new Page<>(pageOptions.getPageNumber(), pageOptions.getPageSize(),
            (int) Math.ceil(ingredients.size() / ((float) pageOptions.getPageSize())), ingredients.size(), subList);
    }
}
