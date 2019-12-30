package gitlet;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
// this is the java file for Commit command


public class Commit extends Utils implements Serializable {
    private LocalDateTime date;
    private String log;
    private String id;
    private Commit parent;
    private boolean isSplit = false;
    ArrayList<Commit> ancestors;
    public HashMap<String, Blob> contentDictionary; //<filename, Blob>

    public Commit(Variable variables) { // making initial commit
        this.date = LocalDateTime.now();
        this.log = "initial commit";
        ArrayList<Object> sha1Ref = new ArrayList<>();
        this.parent = null;
        contentDictionary = new HashMap<>();
        variables.branchNameDictionary.put("master", this);
        variables.currentBranch = "master";
        sha1Ref.add(log.getBytes());
        sha1Ref.add(date.toString());
        this.id = sha1(sha1Ref);
        ancestors = ancestor();
    }

    public Commit(String logMessage, Variable variables) {
        ArrayList<Object> sha1Ref = new ArrayList<>();
        this.date = LocalDateTime.now();
        this.log = logMessage;
        this.parent = variables.branchNameDictionary.get(variables.currentBranch);
        variables.branchNameDictionary.put(variables.currentBranch, this);
        this.contentDictionary = new HashMap<>(parent.contentDictionary);
        snapshot(variables);
        for (Blob blob : contentDictionary.values()) {
            sha1Ref.add(blob.getContents());
        }
        sha1Ref.add(parent.id.getBytes());
        sha1Ref.add(log.getBytes());
        sha1Ref.add(date.toString().getBytes());
        this.id = sha1(sha1Ref);
        ancestors = ancestor();
    }
    public static void clearStage(Variable variables) {
        variables.sTAGING_DIRECTORY.clear();
        variables.tempRemoved.clear();
    }
    public void snapshot(Variable variables) {
        for (String fileName : variables.sTAGING_DIRECTORY.keySet()) {
            if (contentDictionary.containsKey(fileName)) {
                contentDictionary.replace(fileName, variables.sTAGING_DIRECTORY.get(fileName));
            } else {
                contentDictionary.put(fileName, variables.sTAGING_DIRECTORY.get(fileName));
            }
        }
        for (String rmFilename : variables.tempRemoved) {
            contentDictionary.remove(rmFilename);   //Untrack the files marked rm for next commit
        }
        return;
    } // save a snapshot of certain files

    public LocalDateTime getDate() {
        return date;
    }

    public String getLog() {
        return log;
    }

    public Commit getParent() {
        return parent;
    }

    public String getId() {
        return id;
    }

    public byte[] getContent(String fileName) {
        Blob file = contentDictionary.get(fileName);
        if (file == null) {
            return null;
        }
        return file.getContents();
    }
    public ArrayList<Commit> ancestor() {
        ArrayList<Commit> toReturn = new ArrayList<>();
        Commit copy = this;
        while (copy != null) {
            toReturn.add(copy);
            copy = copy.parent;
        }
        return toReturn;
    }
    public boolean getSplit() {
        return isSplit;
    }

    public void setSplit() {
        isSplit = !isSplit;
    }
}
