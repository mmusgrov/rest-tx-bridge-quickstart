package org.jboss.jbossts.resttxbridge.quickstart.jpa.model;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Provides functionality for manipulation with users using persistence context from {@link Resources}.
 *
 * @author Gytis Trikleris
 * @author Lukas Fryc
 * @author Oliver Kiss
 *
 */
@Stateless
public class UserDaoImpl implements UserDao {

    @PersistenceContext
    private EntityManager em;

    public User getForUsername(String username) {
        List<User> result = em.createQuery("select u from User u where u.username = ?", User.class).setParameter(1, username)
                .getResultList();

        if (result.isEmpty()) {
            return null;
        }
        return result.get(0);
    }

    public void createUser(User user) {
        em.persist(user);
    }
    
    public void deleteUsers() {
        em.createQuery("DELETE FROM User").executeUpdate();
    }
}