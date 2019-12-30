package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class Variable implements Serializable {
    //"Variables within "Global Frame"
    String currentBranch; // Head
    HashMap<String, Blob> sTAGING_DIRECTORY; // <fileName, Blob>
    HashMap<String, Blob> blobDictionary; //<BlobID, Blob>
    HashMap<String, Commit> branchNameDictionary; // <BranchName, Commit>
    ArrayList<Commit> allCommits;
    ArrayList<String> removedFiles; //files that are removed
    ArrayList<String> tempRemoved; //files that need to be untracked by commit
    File wDir; // File obj of the working dir
    //static ArrayList<String> WDFiles;

    public Variable() {
        wDir = new File(System.getProperty("user.dir"));
        currentBranch = "master";
        sTAGING_DIRECTORY = new HashMap();
        blobDictionary = new HashMap();
        branchNameDictionary = new HashMap<>();
        allCommits = new ArrayList<>();
        tempRemoved = new ArrayList<>();
        removedFiles = new ArrayList<>();
        //WDFiles = (ArrayList<String>) Utils.plainFilenamesIn(System.getProperty("user.dir"));
    }



    public Commit lastCommit() {
        return branchNameDictionary.get(currentBranch);
    }

    public Commit lastCommit(String branchname) {
        return branchNameDictionary.get(branchname);
    }
}

