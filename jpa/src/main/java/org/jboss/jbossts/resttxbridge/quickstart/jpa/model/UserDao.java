package org.jboss.jbossts.resttxbridge.quickstart.jpa.model;

import javax.ejb.Local;

/**
 * Basic operations for manipulation with users
 *
 * @author Gytis Trikleris
 * @author Lukas Fryc
 *
 */
@Local
public interface UserDao {

    public User getForUsername(String username);

    public void createUser(User user);
    
    public void deleteUsers();
}
