package org.jboss.jbossts.resttxbridge.quickstart.jpa.service;

import java.net.URI;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jboss.jbossts.resttxbridge.annotation.Transactional;
import org.jboss.jbossts.resttxbridge.quickstart.jpa.model.Task;
import org.jboss.jbossts.resttxbridge.quickstart.jpa.model.TaskDao;
import org.jboss.jbossts.resttxbridge.quickstart.jpa.model.User;
import org.jboss.jbossts.resttxbridge.quickstart.jpa.model.UserDao;

/**
 * A JAX-RS resource for exposing REST endpoints for Task manipulation
 * 
 * @author Gytis Trikleris
 * 
 */
@Path("/")
public class TaskResource {
    @Inject
    private UserDao userDao;

    @Inject
    private TaskDao taskDao;

    @POST
    @Path("users/{username}")
    @Transactional
    public Response createUser(@PathParam("username") String username) {
        getUser(username);

        return Response.status(201).build();
    }
    
    @DELETE
    @Path("users")
    public void deleteUsers() {
        userDao.deleteUsers();
    }

    @POST
    @Path("tasks/{username}/{title}")
    @Transactional
    public Response createTask(@Context UriInfo info, @PathParam("username") String username,
            @PathParam("title") String taskTitle) {

        User user = getUser(username);
        Task task = new Task(taskTitle);

        taskDao.createTask(user, task);

        // Construct the URI for the newly created resource and put in into the Location header of the response
        // (assumes that there is only one occurrence of the task title in the request)
        String rawPath = info.getAbsolutePath().getRawPath().replace(task.getTitle(), task.getId().toString());
        UriBuilder uriBuilder = info.getAbsolutePathBuilder().replacePath(rawPath);
        URI uri = uriBuilder.build();

        return Response.created(uri).build();
    }

    @DELETE
    @Path("tasks/{username}/{id}")
    @Transactional
    public void deleteTaskById(@PathParam("username") String username, @PathParam("id") Long id) {
        User user = getUser(username);
        Task task = getTask(user, id);
        taskDao.deleteTask(task);
    }
    
    @DELETE
    @Path("tasks")
    public void deleteAllTasks() {
        taskDao.deleteTasks();
    }
    
    @GET
    @Path("tasks/{username}/{id}")
    @Produces({MediaType.APPLICATION_JSON})
    @Transactional
    public String getTaskById(@PathParam("username") String username, @PathParam("id") Long id) {
        User user = getUser(username);
        return getTask(user, id).toJson();
    }

    @GET
    @Path("tasks/{username}/{title}")
    @Produces({MediaType.APPLICATION_JSON})
    @Transactional
    public String getTasksByTitle(@PathParam("username") String username, @PathParam("title") String title)
            throws JSONException {
        
        JSONArray json = new JSONArray();
        List<Task> tasks = getTasks(getUser(username), title); 
        
        for (Task task : tasks) {
            json.put(new JSONObject(task.toJson()));
        }
        
        return json.toString(4);
    }

    @GET
    @Path("tasks/{username}")
    @Produces({MediaType.APPLICATION_JSON})
    @Transactional
    public String getTasks(@PathParam("username") String username) throws JSONException {
        JSONArray json = new JSONArray();
        List<Task> tasks = getTasks(getUser(username)); 
        
        for (Task task : tasks) {
            json.put(new JSONObject(task.toJson()));
        }
        
        return json.toString(4);
    }

    // Utility Methods

    private List<Task> getTasks(User user, String title) {
        return taskDao.getForTitle(user, title);
    }

    private List<Task> getTasks(User user) {
        return taskDao.getAll(user);
    }

    private Task getTask(User user, Long id) {
        for (Task task : taskDao.getAll(user))
            if (task.getId().equals(id))
                return task;

        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    private User getUser(String username) {
        try {
            User user = userDao.getForUsername(username);

            if (user == null) {
                user = new User(username);

                userDao.createUser(user);
            }

            return user;
        } catch (Exception e) {
            throw new WebApplicationException(e);
        }
    }

}
