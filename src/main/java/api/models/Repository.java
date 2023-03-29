package api.models;

public class Repository {
    private String name;
    private String server;

    public String getName() {
        return name;
    }

    public String getServer() {
        return server;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getCompleteName() {
        return getName() + "@" + getServer();
    }

    @Override
    public String toString(){
        return name;
    }
}
