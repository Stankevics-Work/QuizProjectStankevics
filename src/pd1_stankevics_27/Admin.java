package pd1_stankevics_27;

import pd1_stankevics_27.User;
import java.util.ArrayList;
import java.util.List;

/**
 * Administratora lietotāja klase, kas paplašina User klasi.
 * Administratoram ir tiesības pārvaldīt visus sistēmas lietotājus -
 * pievienot jaunus lietotājus un apskatīt esošo lietotāju sarakstu.
 * 
 * @author stankevics_27
 * @version 1.0
 */
public class Admin extends User {

    private List<User> users = new ArrayList<>();

    /**
     * Izveido jaunu Admin objektu ar norādīto vārdu, lietotājvārdu un paroli.
     * 
     * @param name administratora vārds un uzvārds
     * @param login administratora lietotājvārds autorizācijai
     * @param password administratora parole autorizācijai
     */
    public Admin(String name, String login, String password) {
        super(name, login, password);
    }

    /**
     * Pievieno jaunu lietotāju administratora pārvaldītajā lietotāju sarakstā.
     * 
     * @param user pievienojamais lietotājs
     */
    public void addUser(User user) {
        users.add(user);
    }

    /**
     * Atgriež visu administratora pārvaldīto lietotāju sarakstu.
     * 
     * @return List<User> objekts ar visiem lietotājiem
     */
    public List<User> getUsers() {
        return users;
    }
}