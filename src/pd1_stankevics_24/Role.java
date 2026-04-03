package pd1_stankevics_24;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Lietotāja lomas klase, kas definē atļauju kopumu un prioritāti.
 * Lomas tiek izmantotas, lai kontrolētu piekļuvi sistēmas funkcijām.
 * <p>
 * Katrai lomai ir savs nosaukums, apraksts, prioritāte (augstāks skaitlis
 * nozīmē lielākas tiesības) un atļauju saraksts.
 *
 * @author armins.stankevics_24
 * @see User
 */
public class Role {
    private String roleId;
    private String roleName;
    private String description;
    private List<String> permissions;
    private int priority;

    /**
     * Lomas konstantes
     */
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_TEACHER = "TEACHER";
    public static final String ROLE_STUDENT = "STUDENT";

    /**
     * Izveido jaunu lomas objektu ar pamatparametriem.
     * Atļaujas tiek inicializētas automātiski atbilstoši lomas nosaukumam.
     *
     * @param roleId      unikāls lomas identifikators
     * @param roleName    lomas nosaukums (ADMIN, TEACHER, STUDENT)
     * @param description lomas apraksts
     */
    public Role(String roleId, String roleName, String description) {
        this.roleId = roleId;
        this.roleName = roleName;
        this.description = description;
        this.permissions = new ArrayList<>();
        this.priority = 0;
        initializeDefaultPermissions();
    }

    /**
     * Izveido jaunu lomas objektu ar prioritāti.
     *
     * @param roleId      unikāls lomas identifikators
     * @param roleName    lomas nosaukums
     * @param description lomas apraksts
     * @param priority    lomas prioritāte (augstāks skaitlis = lielāka prioritāte)
     */
    public Role(String roleId, String roleName, String description, int priority) {
        this(roleId, roleName, description);
        setPriority(priority);
    }

    /**
     * Inicializē noklusējuma atļaujas atbilstoši lomai.
     */
    private void initializeDefaultPermissions() {
        switch (roleName) {
            case ROLE_ADMIN:
                permissions.add("USER_CREATE");
                permissions.add("USER_READ");
                permissions.add("USER_UPDATE");
                permissions.add("USER_DELETE");
                permissions.add("TEST_CREATE");
                permissions.add("TEST_READ");
                permissions.add("TEST_UPDATE");
                permissions.add("TEST_DELETE");
                permissions.add("SYSTEM_LOGS");
                permissions.add("BACKUP");
                break;
                
            case ROLE_TEACHER:
                permissions.add("USER_READ");
                permissions.add("TEST_CREATE");
                permissions.add("TEST_READ");
                permissions.add("TEST_UPDATE");
                permissions.add("QUESTION_CREATE");
                permissions.add("QUESTION_READ");
                permissions.add("QUESTION_UPDATE");
                permissions.add("RESULT_READ");
                break;
                
            case ROLE_STUDENT:
                permissions.add("TEST_READ");
                permissions.add("TEST_TAKE");
                permissions.add("RESULT_READ");
                permissions.add("RESULT_VIEW_OWN");
                break;
                
            default:
                permissions.add("TEST_READ");
                break;
        }
    }

    /**
     * Iestata lomas prioritāti.
     *
     * @param priority prioritātes vērtība (augstāks skaitlis = lielāka prioritāte)
     */
    public final void setPriority(int priority) {
        this.priority = priority;
    }

    /**
     * Pievieno atļauju lomai.
     *
     * @param permission atļaujas nosaukums (nedrīkst būt null vai tukšs)
     */
    public void addPermission(String permission) {
        if (permission != null && !permission.trim().isEmpty() && !permissions.contains(permission)) {
            permissions.add(permission.trim());
        }
    }

    /**
     * Noņem atļauju no lomas.
     *
     * @param permission atļaujas nosaukums
     * @return {@code true} ja atļauja tika atrasta un noņemta
     */
    public boolean removePermission(String permission) {
        return permissions.remove(permission);
    }

    /**
     * Pārbauda vai lomai ir noteikta atļauja.
     *
     * @param permission atļaujas nosaukums
     * @return {@code true} ja atļauja eksistē
     */
    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }

    /**
     * Pārbauda vai lomai ir visas norādītās atļaujas.
     *
     * @param requiredPermissions atļauju nosaukumi, kas jāpārbauda
     * @return {@code true} ja lomai ir visas norādītās atļaujas
     */
    public boolean hasAllPermissions(String... requiredPermissions) {
        for (String perm : requiredPermissions) {
            if (!permissions.contains(perm)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Pārbauda vai lomai ir kāda no norādītajām atļaujām.
     *
     * @param requiredPermissions atļauju nosaukumi, kas jāpārbauda
     * @return {@code true} ja lomai ir vismaz viena no norādītajām atļaujām
     */
    public boolean hasAnyPermission(String... requiredPermissions) {
        for (String perm : requiredPermissions) {
            if (permissions.contains(perm)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Atgriež formatētu lomas informāciju ar apmalēm.
     *
     * @return formatēta lomas informācija
     */
    public String getRoleInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("╔════════════════════════════════════╗\n");
        sb.append(String.format("║ %-34s ║\n", "LOMA: " + roleName));
        sb.append("╠════════════════════════════════════╣\n");
        sb.append(String.format("║ ID: %-30s ║\n", roleId));
        sb.append(String.format("║ Prioritāte: %-24d ║\n", priority));
        
        if (!description.isEmpty()) {
            sb.append("╠════════════════════════════════════╣\n");
            sb.append("║ APRAKSTS:                          ║\n");
            sb.append(String.format("║ %-34s ║\n", description));
        }
        
        sb.append("╠════════════════════════════════════╣\n");
        sb.append("║ ATĻAUJAS:                          ║\n");
        
        for (String perm : permissions) {
            sb.append(String.format("║ • %-31s ║\n", perm));
        }
        
        sb.append("╚════════════════════════════════════╝");
        return sb.toString();
    }

    // Getter metodes
    /**
     * Atgriež lomas unikālo identifikatoru.
     *
     * @return lomas ID
     */
    public String getRoleId() { return roleId; }

    /**
     * Atgriež lomas nosaukumu.
     *
     * @return lomas nosaukums
     */
    public String getRoleName() { return roleName; }

    /**
     * Atgriež lomas aprakstu.
     *
     * @return lomas apraksts
     */
    public String getDescription() { return description; }

    /**
     * Atgriež lomas atļauju saraksta kopiju.
     *
     * @return atļauju saraksts
     */
    public List<String> getPermissions() { return new ArrayList<>(permissions); }

    /**
     * Atgriež lomas prioritāti.
     *
     * @return prioritātes vērtība
     */
    public int getPriority() { return priority; }

    /**
     * Atgriež lomas atļauju skaitu.
     *
     * @return atļauju skaits
     */
    public int getPermissionsCount() { return permissions.size(); }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Role role = (Role) obj;
        return Objects.equals(roleId, role.roleId) || Objects.equals(roleName, role.roleName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roleId, roleName);
    }

    @Override
    public String toString() {
        return roleName + " (" + permissions.size() + " atļaujas)";
    }
}