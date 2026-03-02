package pd1_stankevics_27;

/**
 * Lietotāja pamatklase.
 * 
 * @author stankevics_27
 * @version 1.0
 */
public class User {
    private String name;
    private String login;
    private String password;

    /**
     * Izveido jaunu lietotāja objektu.
     * 
     * @param name lietotāja vārds un uzvārds
     * @param login lietotājvārds
     * @param password parole
     */
    public User(String name, String login, String password) {
        this.name = name;
        this.login = login;
        this.password = password;
    }

    /**
     * Pārbauda autorizācijas datus.
     */
    public boolean enter(String login, String password) {
        return this.login.equals(login) && this.password.equals(password);
    }

    /**
     * Atgriež lietotāja vārdu.
     */
    public String getName() {
        return name;
    }

    /**
     * Atgriež lietotājvārdu.
     */
    public String getLogin() {
        return login;
    }

    /**
     * Atgriež paroli.
     */
    public String getPassword() {
        return password;
    }
}