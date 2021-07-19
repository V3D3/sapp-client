package in.ac.iitm.sapp;

import org.json.JSONObject;

public interface TaskEventManagement {
    public void setTaskCompletion(int id, boolean status);
    public void deleteTask(int id);
    public void editTask(int id, JSONObject updatedParams);
    public void saveTasks();
}
