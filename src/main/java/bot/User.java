package bot;

import java.util.ArrayList;

public class User {
    private long chatId;
    private ArrayList<String> repos;
    private boolean isDeleteAction;

    public User(long chatId){
        this.chatId = chatId;
        this.repos = new ArrayList<>();
        this.isDeleteAction = false;
    }

    public long getChatId() {
        return chatId;
    }

    public ArrayList<String> getRepos() {
        return repos;
    }

    public void addNewRepo(String repo){
        this.repos.add(repo);
    }

    public void deleteRepo(String repo){
        this.repos.remove(repo);
    }

    public boolean isDeleteAction() {
        return isDeleteAction;
    }

    public void setDeleteAction(boolean deleteAction) {
        isDeleteAction = deleteAction;
    }
}
