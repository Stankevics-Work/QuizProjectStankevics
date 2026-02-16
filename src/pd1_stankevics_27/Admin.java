package pd1_stankevics_27;

import pd1_stankevics_27.User;
import java.util.ArrayList;
import java.util.List;

public class Admin extends User {

    private List<User> users = new ArrayList<>();

    public Admin(String name, String login, String password) {
        super(name, login, password);
    }

    public void addUser(User user) {
        users.add(user);
    }

    public List<User> getUsers() {
        return users;
    }
}
