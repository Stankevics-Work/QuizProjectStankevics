package pd1_stankevics_24;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Lietotāja abstraktā pamatklase, kas satur kopīgus datus un funkcionalitāti
 * visiem lietotāju tipiem (Students, Teacher, Admin).
 * 
 * @author armins.stankevics_24
 */
public abstract class User {
    private String name;
    private String login;
    private String password;
    private String userId;
    private String email;
    private boolean isActive;

    /**
     * Konstruktors lietotāja izveidei ar pamatinformāciju.
     *
     * @param name     lietotāja vārds un uzvārds
     * @param login    pieteikšanās lietotājvārds
     * @param password pieteikšanās parole
     * @throws IllegalArgumentException ja vārds, lietotājvārds vai parole neatbilst prasībām
     */
    public User(String name, String login, String password) {
        setName(name);
        setLogin(login);
        setPassword(password);
        this.userId = generateUserId();
        this.email = "";
        this.isActive = true;
    }

    /**
     * Konstruktors lietotāja izveidei ar e-pasta adresi.
     *
     * @param name     lietotāja vārds un uzvārds
     * @param login    pieteikšanās lietotājvārds
     * @param password pieteikšanās parole
     * @param email    lietotāja e-pasta adrese
     * @throws IllegalArgumentException ja vārds, lietotājvārds, parole vai e-pasts neatbilst prasībām
     */
    public User(String name, String login, String password, String email) {
        this(name, login, password);
        setEmail(email);
    }

    /**
     * Ģenerē unikālu lietotāja identifikatoru.
     *
     * @return unikāls lietotāja ID
     */
    private String generateUserId() {
        return "U" + System.currentTimeMillis() + (int)(Math.random() * 1000);
    }

    /**
     * Iestata lietotāja vārdu un uzvārdu.
     *
     * @param name lietotāja vārds un uzvārds (nedrīkst būt null vai tukšs)
     * @throws IllegalArgumentException ja vārds ir null vai tukšs
     */
    public final void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Lietotāja vārds nevar būt tukšs!");
        }
        this.name = name.trim();
    }

    /**
     * Iestata pieteikšanās lietotājvārdu.
     *
     * @param login pieteikšanās lietotājvārds (vismaz 3 simboli, drīkst saturēt burtus, ciparus, ., _, -)
     * @throws IllegalArgumentException ja lietotājvārds ir null, tukšs, īsāks par 3 simboliem vai satur neatļautas rakstzīmes
     */
    public final void setLogin(String login) {
        if (login == null || login.trim().isEmpty()) {
            throw new IllegalArgumentException("Lietotājvārds nevar būt tukšs!");
        }
        if (login.length() < 3) {
            throw new IllegalArgumentException("Lietotājvārdam jābūt vismaz 3 simbolus garam!");
        }
        if (!login.matches("[a-zA-Z0-9._-]+")) {
            throw new IllegalArgumentException("Lietotājvārds drīkst saturēt tikai burtus, ciparus, punktus, defises un apakšsvītru!");
        }
        this.login = login.trim();
    }

    /**
     * Iestata lietotāja paroli.
     *
     * @param password parole (vismaz 6 simboli)
     * @throws IllegalArgumentException ja parole ir null, tukša vai īsāka par 6 simboliem
     */
    public final void setPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Parole nevar būt tukša!");
        }
        if (!isPasswordStrong(password)) {
            throw new IllegalArgumentException("Parolei jābūt vismaz 6 simbolus garai un jāsatur vismaz viens cipars!");
        }
        this.password = password;
    }

    /**
     * Iestata lietotāja e-pasta adresi.
     *
     * @param email e-pasta adrese (jāatbilst e-pasta formātam)
     * @throws IllegalArgumentException ja e-pasta adreses formāts ir nederīgs
     */
    public final void setEmail(String email) {
        if (email != null && !email.trim().isEmpty()) {
            String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
            if (!Pattern.matches(emailRegex, email.trim())) {
                throw new IllegalArgumentException("Nederīga e-pasta adrese!");
            }
            this.email = email.trim();
        } else {
            this.email = "";
        }
    }

    /**
     * Iestata lietotāja konta aktīvo statusu.
     *
     * @param active true, ja konts ir aktīvs; false, ja bloķēts
     */
    public void setActive(boolean active) {
        this.isActive = active;
    }

    /**
     * Pārbauda vai parole atbilst stipruma prasībām (vismaz 6 simboli).
     *
     * @param password pārbaudāmā parole
     * @return true, ja parole ir vismaz 6 simbolus gara
     */
    public static boolean isPasswordStrong(String password) {
        return password != null && password.length() >= 6;
    }

    /**
     * Pārbauda vai parole atbilst stipruma prasībām ar papildu cipara prasību.
     *
     * @param password     pārbaudāmā parole
     * @param requireDigit true, ja parolei jāsatur vismaz viens cipars
     * @return true, ja parole atbilst visām prasībām
     */
    public static boolean isPasswordStrong(String password, boolean requireDigit) {
        if (password == null || password.length() < 6) {
            return false;
        }
        if (requireDigit) {
            return password.matches(".*\\d.*");
        }
        return true;
    }

    /**
     * Pārbauda vai ievadītais lietotājvārds un parole atbilst šim lietotājam.
     *
     * @param login    ievadītais lietotājvārds
     * @param password ievadītā parole
     * @return true, ja lietotājvārds un parole sakrīt ar šī lietotāja datiem
     */
    public boolean enter(String login, String password) {
        return this.login.equals(login) && this.password.equals(password);
    }

    // Getter metodes
    /**
     * Atgriež lietotāja vārdu un uzvārdu.
     *
     * @return lietotāja vārds un uzvārds
     */
    public String getName() { return name; }

    /**
     * Atgriež lietotāja pieteikšanās lietotājvārdu.
     *
     * @return lietotājvārds
     */
    public String getLogin() { return login; }

    /**
     * Atgriež lietotāja paroli.
     *
     * @return parole
     */
    public String getPassword() { return password; }

    /**
     * Atgriež lietotāja unikālo identifikatoru.
     *
     * @return lietotāja ID
     */
    public String getUserId() { return userId; }

    /**
     * Atgriež lietotāja e-pasta adresi.
     *
     * @return e-pasta adrese
     */
    public String getEmail() { return email; }

    /**
     * Pārbauda vai lietotāja konts ir aktīvs.
     *
     * @return true, ja konts ir aktīvs; false, ja bloķēts
     */
    public boolean isActive() { return isActive; }

    /**
     * Atgriež lietotāja lomas nosaukumu.
     *
     * @return lomas nosaukums (ADMIN, TEACHER, STUDENT)
     */
    public abstract String getRole();

    /**
     * Atgriež lietotāja tipa nosaukumu latviešu valodā.
     *
     * @return lietotāja tipa nosaukums (Administrators, Pasniedzējs, Students)
     */
    public abstract String getUserType();

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return Objects.equals(login, user.login);
    }

    @Override
    public int hashCode() {
        return Objects.hash(login);
    }

    @Override
    public String toString() {
        return name + " (" + login + ") - " + getUserType() + (isActive ? "" : " [BLOĶĒTS]");
    }
}