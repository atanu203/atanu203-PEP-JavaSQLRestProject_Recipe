package com.revature.dao;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.revature.util.ConnectionUtil;
import com.revature.util.Page;
import com.revature.util.PageOptions;
import com.revature.model.Chef;
import com.revature.model.Recipe;



/**
 * The RecipeDAO class abstracts the CRUD operations for Recipe objects.
 * This class utilizes the previously created classes and primarily functions as a pure functional class, meaning it doesn't store state apart from a  reference to ConnectionUtil for database connection purposes. 
 * 
 * Although the implementation may seem extensive for simple functionality, this design improves testability, maintainability, and extensibility of the overall infrastructure.
 */

public class RecipeDAO {

    /**
	 * DAO for managing Chef entities, used for retrieving chef details associated with recipes.
	 */
	private ChefDAO chefDAO;

	/**
	 * DAO for managing Ingredient entities, used for retrieving ingredient details for recipes.
	 */
    @SuppressWarnings("unused")
	private IngredientDAO ingredientDAO;

    /** A utility class for establishing connections to the database. */
    @SuppressWarnings("unused")
    private ConnectionUtil connectionUtil;

    /**
	 * Constructs a RecipeDAO instance with specified ChefDAO and IngredientDAO.
	 *
	 * TODO: Finish the implementation so that this class's instance variables are initialized accordingly.
	 * 
	 * @param chefDAO - the ChefDAO used for retrieving chef details.
	 * @param ingredientDAO - the IngredientDAO used for retrieving ingredient details.
     * @param connectionUtil - the utility used to connect to the database
	 */
	public RecipeDAO(ChefDAO chefDAO, IngredientDAO ingredientDAO, ConnectionUtil connectionUtil) {
		this.chefDAO = chefDAO;
		this.ingredientDAO = ingredientDAO;
		this.connectionUtil = connectionUtil;
	}

    /**
     * TODO: Retrieves all recipes from the database.
     * 
     * @return a list of all Recipe objects
     */

    public List<Recipe> getAllRecipes() {
		List<Recipe> recipes = new ArrayList<>();
		try (Connection conn = connectionUtil.getConnection();
			 Statement stmt = conn.createStatement();
			 ResultSet rs = stmt.executeQuery("SELECT * FROM Recipe")) {
			recipes = mapRows(rs);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return recipes;
    }

    /**
     * TODO: Retrieves a paginated list of all recipes from the database.
     * 
     * @param pageOptions options for pagination, including page size and page number
     * @return a paginated list of Recipe objects
     */
    public Page<Recipe> getAllRecipes(PageOptions pageOptions) {
		try (Connection conn = connectionUtil.getConnection();
			 Statement stmt = conn.createStatement();
			 ResultSet rs = stmt.executeQuery("SELECT * FROM Recipe")) {
			return pageResults(rs, pageOptions);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
    }

    /**
     * TODO: Searches for recipes that match a specified term.
     * 
     * @param term the search term to filter recipes by
     * @return a list of Recipe objects that match the search term
     */

    public List<Recipe> searchRecipesByTerm(String term) {
		List<Recipe> recipes = new ArrayList<>();
		String sql = "SELECT * FROM Recipe WHERE name LIKE ? OR instructions LIKE ?";
		try (Connection conn = connectionUtil.getConnection();
			 java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
			String likeTerm = "%" + term + "%";
			stmt.setString(1, likeTerm);
			stmt.setString(2, likeTerm);
			try (ResultSet rs = stmt.executeQuery()) {
				recipes = mapRows(rs);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return recipes;
    }

    /**
     * TODO: Searches for recipes that match a specified term and returns a paginated result.
     * 
     * @param term the search term to filter recipes by
     * @param pageOptions options for pagination, including page size and page number
     * @return a paginated list of Recipe objects that match the search term
     */

    public Page<Recipe> searchRecipesByTerm(String term, PageOptions pageOptions) {
		String sql = "SELECT * FROM Recipe WHERE name LIKE ? OR instructions LIKE ?";
		try (Connection conn = connectionUtil.getConnection();
			 java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
			String likeTerm = "%" + term + "%";
			stmt.setString(1, likeTerm);
			stmt.setString(2, likeTerm);
			try (ResultSet rs = stmt.executeQuery()) {
				return pageResults(rs, pageOptions);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
    }

    /**
     * TODO: Retrieves a specific recipe by its ID.
     * 
     * @param id the ID of the recipe to retrieve
     * @return the Recipe object corresponding to the given ID
     */

    public Recipe getRecipeById(int id) {
		String sql = "SELECT * FROM Recipe WHERE id = ?";
		try (Connection conn = connectionUtil.getConnection();
			 java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, id);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return mapSingleRow(rs);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
    }
        

    /**
     * TODO: Creates a new recipe in the database.
     * 
     * @param recipe the Recipe object to create
     * @return the ID of the newly created recipe
     */

    public int createRecipe(Recipe recipe) {
		String sql = "INSERT INTO Recipe (name, instructions, chef_id) VALUES (?, ?, ?)";
		try (Connection conn = connectionUtil.getConnection();
			 java.sql.PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			stmt.setString(1, recipe.getName());
			stmt.setString(2, recipe.getInstructions());
			stmt.setInt(3, recipe.getAuthor().getId());
			int affectedRows = stmt.executeUpdate();
			if (affectedRows == 0) {
				throw new SQLException("Creating recipe failed, no rows affected.");
			}
			try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
				if (generatedKeys.next()) {
					int id = generatedKeys.getInt(1);
					recipe.setId(id);
					return id;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
    }

    /**
     * TODO: Updates an existing recipe's instructions and chef_id in the database.
     * 
     * @param recipe the Recipe object with updated data
     */

    public void updateRecipe(Recipe recipe) {
		String sql = "UPDATE Recipe SET name = ?, instructions = ?, chef_id = ? WHERE id = ?";
		try (Connection conn = connectionUtil.getConnection();
			 java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, recipe.getName());
			stmt.setString(2, recipe.getInstructions());
			stmt.setInt(3, recipe.getAuthor().getId());
			stmt.setInt(4, recipe.getId());
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
    }

    /**
     * TODO: Deletes a specific recipe from the database.
     * 
     * @param recipe the Recipe object to delete
     */

    public void deleteRecipe(Recipe recipe) {
		String sql = "DELETE FROM Recipe WHERE id = ?";
		try (Connection conn = connectionUtil.getConnection();
			 java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, recipe.getId());
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
    }

    // below are helper methods for your convenience
	
	/**
	 * Maps a single row from the ResultSet to a Recipe object.
	 * This method extracts the recipe details such as ID, name, instructions,
	 * and associated chef from the ResultSet and constructs a Recipe instance.
	 *
	 * @param set the ResultSet containing the recipe data
	 * @return a Recipe object representing the mapped row
	 * @throws SQLException if there is an error accessing the ResultSet
	 */
	private Recipe mapSingleRow(ResultSet set) throws SQLException {
		int id = set.getInt("id");
		String name = set.getString("name");
		String instructions = set.getString("instructions");
		Chef author = chefDAO.getChefById(set.getInt("chef_id"));
		return new Recipe(id, name, instructions, author);
	}

	/**
	 * Maps multiple rows from a ResultSet to a list of Recipe objects.
	 * This method iterates through the ResultSet and calls mapSingleRow
	 * for each row, adding the resulting Recipe objects to a list.
	 *
	 * @param set the ResultSet containing multiple recipe rows
	 * @return a list of Recipe objects representing the mapped rows
	 * @throws SQLException if there is an error accessing the ResultSet
	 */
	private List<Recipe> mapRows(ResultSet set) throws SQLException {
		List<Recipe> recipes = new ArrayList<>();
		while (set.next()) {
			recipes.add(mapSingleRow(set));
		}
		return recipes;
	}

	/**
	 * Pages the results from a ResultSet into a Page object for the Recipe entity.
	 * This method processes the ResultSet to retrieve recipes, then slices the list
	 * based on the provided pagination options, and returns a Page object
	 * containing
	 * the paginated results.
	 *
	 * @param set the ResultSet containing recipe data
	 * @param pageOptions the PageOptions object containing pagination details
	 * @return a Page object containing the paginated list of Recipe objects
	 * @throws SQLException if there is an error accessing the ResultSet
	 */
	private Page<Recipe> pageResults(ResultSet set, PageOptions pageOptions) throws SQLException {
		List<Recipe> recipes = mapRows(set);
		int offset = (pageOptions.getPageNumber() - 1) * pageOptions.getPageSize();
		int limit = offset + pageOptions.getPageSize();
		List<Recipe> slicedList = sliceList(recipes, offset, limit);
		return new Page<>(pageOptions.getPageNumber(), pageOptions.getPageSize(),
				recipes.size() / pageOptions.getPageSize(), recipes.size(), slicedList);
	}

	/**
	 * Slices a list of Recipe objects from a specified start index to an end index.
	 * This method creates a sublist of the provided list, which can be used for
	 * pagination.
	 *
	 * @param list  the original list of Recipe objects
	 * @param start the starting index (inclusive) for the slice
	 * @param end   the ending index (exclusive) for the slice
	 * @return a list of Recipe objects representing the sliced portion
	 */
	private List<Recipe> sliceList(List<Recipe> list, int start, int end) {
		List<Recipe> sliced = new ArrayList<>();
		for (int i = start; i < end && i < list.size(); i++) {
			sliced.add(list.get(i));
		}
		return sliced;
	}
}

