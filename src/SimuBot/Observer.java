package SimuBot;

public interface Observer {
    public void update(AbstractData data);
    public String getClassName();
}