package Model;

public class data {
    private String nameoffile;
    private int id;
    private String path;
    private String email;
    public data(int id, String nameoffile, String path, String email) {
        this.id = id;
        this.nameoffile  = nameoffile;
        this.path = path;
        this.email = email;
    }

    public data(int id, String nameoffile , String path) {
        this.id = id;
        this.nameoffile  = nameoffile ;
        this.path = path;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFileName() {
        return nameoffile ;
    }

    public void setFileName(String fileName) {
        this.nameoffile  = fileName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
