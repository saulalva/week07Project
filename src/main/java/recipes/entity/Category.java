package recipes.entity;

public class Category {
	private Integer categoryId;
	@Override
	public String toString() {
		return "ID=" + categoryId + ", categoryName=" + categoryName;
	}
	private String categoryName;
	public Integer getCategoryId() {
		return categoryId;
	}
	public void setCategoryId(Integer categoryId) {
		this.categoryId = categoryId;
	}
	public String getCategoryName() {
		return categoryName;
	}
	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}
	
}
