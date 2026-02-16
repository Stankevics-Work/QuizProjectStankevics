package pd1_stankevics_27;

public class Role {
    private String role_id;
    private String role_name;
    private String description;

    public Role(String roleId, String roleName, String description) {
        this.role_id = roleId;
        this.role_name = roleName;
        this.description = description;
    }

    public void assignUser(User user) {
    }

    public String getRoleName() {
        return role_name;
    }
}
