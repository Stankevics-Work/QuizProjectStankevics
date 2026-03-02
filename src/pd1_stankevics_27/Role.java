package pd1_stankevics_27;

/**
 * Lietotāja lomas klase.
 * 
 * @author stankevics_27
 * @version 1.0
 */
public class Role {
    private String role_id;
    private String role_name;
    private String description;

    /**
     * Izveido jaunu lomas objektu.
     * 
     * @param roleId lomas identifikators
     * @param roleName lomas nosaukums
     * @param description lomas apraksts
     */
    public Role(String roleId, String roleName, String description) {
        this.role_id = roleId;
        this.role_name = roleName;
        this.description = description;
    }

    /**
     * Pievieno lietotāju lomai.
     */
    public void assignUser(User user) {
        // TODO
    }

    /**
     * Atgriež lomas nosaukumu.
     */
    public String getRoleName() {
        return role_name;
    }
}