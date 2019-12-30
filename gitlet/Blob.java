package gitlet;
import java.io.File;
import java.io.Serializable;

public class Blob implements Serializable {
    private String name;
    private byte[] contents;
    private String hashID;
    

    public Blob(File file) {
        name = file.getName();
        contents = Utils.readContents(file);
        hashID = Utils.sha1(contents);
    }

    public Blob(byte[] contents, String name) {
        this.name = name;
        this.contents = contents;
        hashID = Utils.sha1(contents);
    }

    public String getName() {
        return name;
    }

    public File getFile(String fileName) {
        File file = new File(fileName);
        if (file == null) {
            return null;
        }
        return file;
    }

    public byte[] getContents() {
        return contents;
    }

    public void writeContents(byte[] newContent) {
        contents = newContent;
    }

    public String getHashID() {
        return hashID;
    }

}
