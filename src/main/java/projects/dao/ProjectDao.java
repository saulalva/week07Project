package projects.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import projects.entity.Category;
import projects.entity.Material;
import projects.entity.Project;
import projects.entity.Step;
import projects.exception.DbException;
import provided.util.DaoBase;

public class ProjectDao extends DaoBase {
	private static final String CATEGORY_TABLE = "category";
	private static final String MATERIAL_TABLE = "material";
	private static final String PROJECT_TABLE = "project";
	private static final String PROJECT_CATEGORY_TABLE = "project_category";
	private static final String STEP_TABLE = "step";

	public Project insertProject(Project project) {
		// @formatter:off
		String sql = ""
				+ "INSERT INTO " + PROJECT_TABLE + " "
				+ "(project_name, estimated_hours, actual_hours, difficulty, notes) "
				+ "VALUES "
				+ "(?, ?, ?, ?, ?)";
		// @formatter:on
		
		
		try(Connection conn = DbConnection.getConnection()){
			startTransaction(conn);
		
			try(PreparedStatement stmt = conn.prepareStatement(sql)){
				setParameter(stmt, 1, project.getProjectName(), String.class);
				setParameter(stmt, 2, project.getEstimatedHours(), BigDecimal.class);
				setParameter(stmt, 3, project.getActualHours(), BigDecimal.class);
				setParameter(stmt, 4, project.getDifficulty(), Integer.class);
				setParameter(stmt, 5, project.getNotes(), String.class);
				
				stmt.executeUpdate();
				
				Integer projectId = getLastInsertId(conn, PROJECT_TABLE);
				commitTransaction(conn);
				
				project.setProjectId(projectId);
				return project;
				
			}
			catch(Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}
			
		}
		catch(SQLException e) {
			throw new DbException(e);
			
		}
		
	}

	public List<Project> fetchAllProjects() {
		//a.Write the SQL statement to return all projects not including materials, steps, or categories. 
		//Order the results by project name.
		String sql = "SELECT * FROM " + PROJECT_TABLE + " ORDER BY project_name";
		
		//b.Add a try-with-resource statement to obtain the Connection object. 
		//Catch the SQLException in a catch block and rethrow a new DbException, passing in the SQLException object.
		//c.      Inside the try block, start a new transaction.
		//d.     Add an inner try-with-resource statement to obtain the PreparedStatement from the Connection object. In a catch block, catch an Exception object. Rollback the transaction and throw a new DbException, passing in the Exception object as the cause.
		//e.     Inside the (currently) innermost try-with-resource statement, add a try-with-resource statement to obtain a ResultSet from the PreparedStatement. Include the import statement for ResultSet. It is in the java.sql package.
		//f.      Inside the new innermost try-with-resource, create and return a List of Projects.
		//g.     Loop through the result set. Create and assign each result row to a new Project object. Add the Project object to the List of Projects. 
		
		//b:
		try(Connection conn = DbConnection.getConnection()) {
			//c:
			startTransaction(conn);
			
			//d:
			try(PreparedStatement stmt = conn.prepareStatement(sql)) {
				//e:
				try(ResultSet rs = stmt.executeQuery()) {
					//f:
					List<Project> projects = new LinkedList<>();
					//g:
					while(rs.next()) {
						projects.add(extract(rs, Project.class));
					}
					return projects;
				}
			}
			//d.
			catch(Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}
		}
		//b:
		catch(SQLException e) {
			throw new DbException(e);
		}
		
	}

	public Optional<Project> fetchProjectById(Integer projectId) {
		//a:
		String sql = "SELECT * FROM " + PROJECT_TABLE + " WHERE project_id = ?";
		//b:
		try(Connection conn = DbConnection.getConnection()) {
			startTransaction(conn);
			//c:
			
			try {
				//e:
				Project project = null;
				//f:
				try(PreparedStatement stmt = conn.prepareStatement(sql)) {
					setParameter(stmt, 1, projectId, Integer.class);
					
					//g:
					try(ResultSet rs = stmt.executeQuery()){
						if(rs.next()) {
							project = extract(rs, Project.class);
						}
					}
				}
				//h:
				if(Objects.nonNull(project)) {
					project.getMaterials().addAll(fetchMaterialsForProject(conn, projectId));
					project.getSteps().addAll(fetchStepsForProject(conn, projectId));
					project.getCategories().addAll(fetchCategoriesForProject(conn, projectId));
				}
				
				commitTransaction(conn);
				return Optional.ofNullable(project);
			}
			//d:
			catch(Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}
		}
		//b:
		catch(SQLException e) {
			throw new DbException(e);
		}
		
	}
	private List<Material> fetchMaterialsForProject(Connection conn, Integer projectId) throws SQLException {
		
		String sql = "SELECT * FROM " + MATERIAL_TABLE + " WHERE project_id = ?";
		
		try(PreparedStatement stmt = conn.prepareStatement(sql)){
			setParameter(stmt, 1, projectId, Integer.class);
			
			try(ResultSet rs = stmt.executeQuery()) {
				List<Material> materials = new LinkedList<>();
				
				while(rs.next()) {
					materials.add(extract(rs, Material.class));
				}
				return materials;
			}
		}
		
	}
	private List<Step> fetchStepsForProject(Connection conn, Integer projectId) throws SQLException {
		String sql = "SELECT * FROM " + STEP_TABLE + " WHERE project_id = ?";
		
		try(PreparedStatement stmt = conn.prepareStatement(sql)) {
			setParameter(stmt, 1, projectId, Integer.class);
			
			try(ResultSet rs = stmt.executeQuery()) {
				List<Step> steps = new LinkedList<>();
				
				while(rs.next()) {
					steps.add(extract(rs, Step.class));
				}
				return steps;
			}
		}
		
	}

	private List<Category> fetchCategoriesForProject(Connection conn, Integer projectId) throws SQLException{
		// @formatter:off
		String sql = ""
				+ "SELECT c.* FROM " + CATEGORY_TABLE + " c "
				+ "JOIN " + PROJECT_CATEGORY_TABLE + " pc USING (category_id) "
				+ "WHERE project_id = ?";
		// @formatter:on
		
		try(PreparedStatement stmt = conn.prepareStatement(sql)) {
			setParameter(stmt, 1, projectId, Integer.class);
			
			try(ResultSet rs = stmt.executeQuery()){
				List<Category> categories = new LinkedList<>();
				
				
				while(rs.next()) {
					categories.add(extract(rs, Category.class));
				}
				return categories;
			}
		}
		
		
	}



}
