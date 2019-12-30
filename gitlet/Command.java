package gitlet;


import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.Serializable;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

public class Command implements Serializable {

    private static DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void init() {
        File dir = new File(".gitlet");

        if (dir.exists()) {
            System.out.println("A gitlet version-control "
                    + "system already exists in the current directory.");
            return;
        } else {

            try {
                Variable variables = new Variable();
                Commit initialCommit = new Commit(variables);
                variables.allCommits.add(initialCommit);
                dir.createNewFile();
                ObjectOutputStream out = new
                        ObjectOutputStream(new FileOutputStream(dir));
                out.writeObject(variables);
                out.close();
            } catch (IOException excp) {
                throw new Error("WTF???");
            }
            // TO DO : Put as first commit in branch MAIN
        }
    }

    public static void add(String name, Variable variables) {

        File file = new File(name);
        if (!file.exists()) {
            System.out.println("File does not exist.");
            return;
        }
        Blob blob = new Blob(file);

        if (variables.lastCommit().getParent() != null) {
            if (variables.lastCommit().contentDictionary.containsKey(name)) {
                if (variables.removedFiles.contains(name)) {
                    variables.removedFiles.remove(name);
                }

                if (blob.getHashID().equals(variables.lastCommit()
                        .contentDictionary.get(name).getHashID())
                    /*&& !variables.TempRemoved.contains(name)*/) {
                    return;
                }
            }
        }
        if (variables.tempRemoved.contains(name)) {
            variables.tempRemoved.remove(name);
        }

        variables.sTAGING_DIRECTORY.put(name, blob);
        writeVariable(variables);
    }
    public static void addHelper(String[] args) {
        Variable variables = readVariable();
        for (int i = 1; i < args.length; i++) {
            add(args[i], variables);
        }
        writeVariable(variables);
    }

    public static void rm(String name, Variable variables) {

        File file = new File(name);
        if (variables.branchNameDictionary
                .get(variables.currentBranch)
                .getContent(name) != null) {
            variables.sTAGING_DIRECTORY.remove(name);   // unstage it if it was staged,
            variables.tempRemoved.add(name);
            variables.removedFiles.add(name);
            file.delete();
        } else if (variables.sTAGING_DIRECTORY.containsKey(name)) {
            variables.sTAGING_DIRECTORY.remove(name);   //unstage the file and do nothing else.
        } else {
            System.out.println("No reason to remove the file.");
        }
        writeVariable(variables);
    }
    public static void removeHelper(String[]args) {
        Variable variables = readVariable();
        for (int i = 1; i < args.length; i++) {
            rm(args[i], variables);
        }
        writeVariable(variables);
    }
    public static void makeCommit(String message, boolean merge) {

        Variable variables = readVariable();

        if (variables.sTAGING_DIRECTORY.size() == 0
                && variables.tempRemoved.size() == 0) {
            System.out.println("No changes added to the commit.");
            return;
        }
        Commit newCommit = new Commit(message, variables);
        variables.allCommits.add(newCommit);
        Commit.clearStage(variables);
        variables.removedFiles.clear();
        writeVariable(variables);
    }


    public static void log() {
        Variable variables = readVariable();

        Commit commit = variables.lastCommit();
        while (true) {
            System.out.println("===");
            System.out.println("Commit " + commit.getId());
            System.out.println(FORMAT.format(commit.getDate()));
            System.out.println(commit.getLog());
            System.out.println();
            if (commit.getParent() == null) {
                return;
            }
            commit = commit.getParent();

        }
    }

    public static void globalLog() {
        Variable variables = readVariable();
        for (Commit commit : variables.allCommits) {
            System.out.println("===");
            System.out.println("Commit " + commit.getId());
            System.out.println(FORMAT.format(commit.getDate()));
            System.out.println(commit.getLog());
            System.out.println();
        }
    }

    public static void find(String message) {
        Variable variables = readVariable();
        boolean exist = false;
        for (Commit commit : variables.allCommits) {
            if (commit.getLog().equals(message)) {
                System.out.println(commit.getId());
                exist = true;
            }
        }
        if (!exist) {
            System.out.println("Found no commit with that message.");
        }
    }

    public static void status() { //Order???
        Variable variables = readVariable();
        System.out.println("=== Branches ===");
        System.out.println("*" + variables.currentBranch);
        for (String branchName : variables.branchNameDictionary.keySet()) {
            if (branchName.equals(variables.currentBranch)) {
                continue;
            }
            System.out.println(branchName);
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        for (String fileName : variables.sTAGING_DIRECTORY.keySet()) {
            System.out.println(fileName);
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        for (String filename : variables.removedFiles) {
            System.out.println(filename);
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        System.out.println("=== Untracked Files ===");
    }

    public static void checkout1(String fileName) {
        Variable variables = readVariable();

        if (variables.lastCommit().getContent(fileName) == null) {
            System.out.println("File does not exist in that commit.");
        }
        File toOverWrite = new File(fileName);
        try {
            toOverWrite.createNewFile();
        } catch (IOException excp) {
            System.out.println("Have not overwritten.");
        }
        Utils.writeContents(toOverWrite,
                variables.branchNameDictionary.
                        get(variables.currentBranch).
                        getContent(fileName));

        writeVariable(variables);
    }


    public static void checkout2(String commitID, String fileName) {
        Variable variables = readVariable();
        for (Commit commit : variables.allCommits) {
            if (commit.getId().substring(0, 8).equals(commitID.substring(0, 8))) {
                if (commit.getContent(fileName) == null) {
                    System.out.println("File does not exist in that commit.");
                    return;
                }
                File f = new File(fileName);
                if (f.exists()) {
                    Utils.writeContents(new File(fileName), commit.getContent(fileName));
                } else {
                    try {
                        f.createNewFile();
                        Utils.writeContents(new File(fileName), commit.getContent(fileName));
                    } catch (IOException excp) {
                        System.out.println("Have not checkout.");
                    }
                }

                return;
            }
        }
        System.out.println("No commit with that id exists.");
        writeVariable(variables);
    }

    public static void checkout3(String branchName) {
        Variable variables = readVariable();

        //Check if blob files exists in WD and Current Branch
        // -- exit if Curr Branch doesnt have, but WD has.
        for (String s : Utils.plainFilenamesIn(variables.wDir)) {
            File file = new File(s);
//            System.out.println(s);
            if (s.equals(".gitlet")) {
                continue;
            }

            if (file.exists() && !variables.lastCommit().contentDictionary.containsKey(s)) {
                System.out.println("There is an untracked file in the way;"
                        + " delete it or add it first.");
                return;
            }
        }

        if (!variables.branchNameDictionary.containsKey(branchName)) {
            System.out.println("No such branch exists.");
            return;
        }
        if (variables.currentBranch == branchName) {
            System.out.println("No need to checkout the current branch.");
            return;
        }

        //WRITE TO WD
        for (String s : variables.lastCommit(branchName).contentDictionary.keySet()) {

            Blob targetblob = variables.lastCommit(branchName).contentDictionary.get(s);
            File file = new File(s);
            if (file.exists()) {
                Utils.writeContents(new File(s), targetblob.getContents());
            } else {
                try {
                    file.createNewFile();
                    Utils.writeContents(new File(s), targetblob.getContents());
                } catch (IOException excp) {
                    System.out.println("Have not checkout.");
                }
            }

        }

        //Delete from WD using restrictedDelete
        // if current branch contains blob not in checked out branch
        for (String s : Utils.plainFilenamesIn(variables.wDir)) {
            File file = new File(s);
            if (s.equals(".gitlet")) {
                continue;
            }
            if (!variables.lastCommit(branchName).contentDictionary.containsKey(s)) {
                file.delete();
            }
        }

        //Clear Staging Area
        variables.sTAGING_DIRECTORY.clear();

        //Set Current Branch to Checked out Branch
        variables.currentBranch = branchName;
        writeVariable(variables);
    }

    public static void branch(String branchName) {
        Variable variables = readVariable();
        if (variables.branchNameDictionary.containsKey(branchName)) {
            System.out.println("A branch with that name already exists.");
            return;
        }
        variables.branchNameDictionary.put(branchName, variables.lastCommit());
        variables.lastCommit().setSplit();
        writeVariable(variables);
    }

    public static void rmBranch(String branchName) {
        Variable variables = readVariable();
        if (!variables.branchNameDictionary.containsKey(branchName)) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        if (variables.currentBranch == branchName) {
            System.out.println("Cannot remove the current branch.");
            return;
        }
        variables.branchNameDictionary.remove(branchName);
        writeVariable(variables);
    }

    public static void reset(String commitID) {
        Variable variables = readVariable();
        for (Commit commit : variables.allCommits) {
            if (commit.getId().equals(commitID)) {
                for (String s : Utils.plainFilenamesIn(variables.wDir)) {
                    File file = new File(s);
                    if (s.equals(".gitlet")) {
                        continue;
                    }

                    if (/*file.exists()*/commit.contentDictionary.containsKey(s)
                            && !variables.lastCommit().
                            contentDictionary.containsKey(s)) {
                        //variables.lastCommit().ContentDictionary -- there's nothing in it ????????
                        System.out.println("There is an untracked file in the way; "
                                + "delete it or add it first.");
                        return;
                    }
                }

                //WRITE TO WD
                for (String s : commit.contentDictionary.keySet()) {

                    Blob targetblob = commit.contentDictionary.get(s);

                    Utils.writeContents(new File(s), targetblob.getContents());

                }

                //Delete from WD using restrictedDelete
                // if current branch contains blob not in checked out branch
                for (String s : Utils.plainFilenamesIn(variables.wDir)) {
                    File file = new File(s);
                    if (!commit.contentDictionary.containsKey(s)) {
                        file.delete();
                    }
                }
                variables.branchNameDictionary.replace(variables.currentBranch, commit);
                Commit.clearStage(variables);
                writeVariable(variables);
                return;
            }

        } System.out.println("No commit with that id exists.");



        writeVariable(variables);
    }

    // TODO
    public static void merge(String branchName) {
        Variable variables = readVariable();
        if (!variables.sTAGING_DIRECTORY.isEmpty() || !variables.tempRemoved.isEmpty()) {
            System.out.println("You have uncommitted changes.");
            return;
        } else if (!variables.branchNameDictionary.containsKey(branchName)) {
            System.out.println("A branch with that name does not exist.");
            return;
        } else if (variables.currentBranch.equals(branchName)) {
            System.out.println("Cannot merge a branch with itself.");
            return;
        }
        Commit splitcommit = findsplit(variables);
        if (variables.lastCommit(branchName).getSplit()) {
            System.out.println("Given branch is an ancestor of the current branch.");
            return;
        }
        if (splitcommit.equals(variables.lastCommit())) {
            System.out.println("Current branch fast-forwarded.");
            return;
        }
        HashMap<String, Blob> splitdictionary = splitcommit.contentDictionary;
        HashMap<String, Blob> branchdictionary = variables.lastCommit(branchName).contentDictionary;
        HashMap<String, Blob> currbranchdictionary = variables.lastCommit().contentDictionary;
        if (!check(variables)) {
            return;
        }
        for (String s : splitdictionary.keySet()) {
            if (currbranchdictionary.containsKey(s) && branchdictionary.containsKey(s)) {
                if (splitdictionary.get(s).getContents()
                        .equals(currbranchdictionary.get(s).getContents())
                        && !currbranchdictionary.get(s).getContents()
                        .equals(branchdictionary.get(s).getContents())) {
                    Utils.writeContents(new File(s), branchdictionary.get(s).getContents());
                }
                add(s, variables);
            } else if (currbranchdictionary.containsKey(s)) {
                if (splitdictionary.get(s).getContents()
                        .equals(currbranchdictionary.get(s).getContents())
                        && !branchdictionary.containsKey(s)) {
                    File file = new File(s);
                    file.delete();
                }
            }
        }
        for (String b : branchdictionary.keySet()) {
            if (!splitdictionary.containsKey(b)
                    && !currbranchdictionary.containsKey(b)) {
                File file = new File(b);
                try {
                    file.createNewFile();
                    Utils.writeContents(new File(b), branchdictionary.get(b).getContents());
                } catch (IOException excp) {
                    System.out.println("Have not checkout.");
                }
                add(b, variables);
            }
            if (currbranchdictionary.containsKey(b) && !currbranchdictionary.get(b).getHashID()
                        .equals(branchdictionary.get(b).getHashID())) {
                System.out.println("Encountered a merge conflict.");
                if (variables.sTAGING_DIRECTORY.containsKey(b)) {
                    variables.sTAGING_DIRECTORY.remove(b);
                }
                mergeCon(currbranchdictionary.get(b), branchdictionary.get(b));
                Utils.writeContents(new File(b), currbranchdictionary.get(b).getContents());
                return;
            }
        }
        for (String c : currbranchdictionary.keySet()) {
            if (!branchdictionary.containsKey(c) && splitdictionary.containsKey(c)
                    && !currbranchdictionary.get(c).getHashID()
                    .equals(splitdictionary.get(c).getHashID())) {
                System.out.println("Encountered a merge conflict.");
                mergeCon(currbranchdictionary.get(c), new Blob(new byte[0], c));
                Utils.writeContents(new File(c), currbranchdictionary.get(c).getContents());
                return;
            }
        }
        makeCommit("Merged " + variables.currentBranch + " with " + branchName + ".", true);
    }

    private static boolean check(Variable variables) {
        for (String s : Utils.plainFilenamesIn(variables.wDir)) {
            File file = new File(s);
            if (file.exists() && !variables.lastCommit().contentDictionary.containsKey(s)
                    && !s.equals(".gitlet")) {
                System.out.println("There is an untracked file in the way;"
                        + " delete it or add it first.");
                return false;
            }
        }
        return true;
    }

    private static Commit findsplit(Variable variable) {
        Commit split = variable.lastCommit();
        while (split != null) {
            if (split.getSplit()) {
                return split;
            }
            split = split.getParent();
        }
        return split;
    }

    private static void mergeCon(Blob curr, Blob bran) {
        byte[] head = "<<<<<<< HEAD\r\n".getBytes();
        byte[] mid = "=======\r\n".getBytes();
        byte[] end = ">>>>>>>\r\n".getBytes();
        byte[] newContents = new byte[curr.getContents().length
                                        + bran.getContents().length + 32];
        System.arraycopy(head, 0, newContents, 0, head.length);
        System.arraycopy(curr.getContents(), 0,
                        newContents, head.length, curr.getContents().length);
        System.arraycopy(mid, 0, newContents,
                head.length + curr.getContents().length, mid.length);
        System.arraycopy(bran.getContents(), 0, newContents,
                head.length + curr.getContents().length + mid.length, bran.getContents().length);
        System.arraycopy(end, 0, newContents,
                head.length + curr.getContents().length + mid.length + bran.getContents().length,
                        end.length);
        curr.writeContents(newContents);
    }

    public static Variable readVariable() {
        Variable variables;
        File inFile = new File(".gitlet");
        try {
            ObjectInputStream inp = new ObjectInputStream(new FileInputStream(inFile));
            variables = (Variable) inp.readObject();
            inp.close();
        } catch (IOException | ClassNotFoundException excp) {
            variables = null;
        }
        return variables;
    }

    public static void writeVariable(Variable variables) {
        File varFile = new File(".gitlet");
        Utils.writeContents(varFile, serialize(variables));
    }

    public static byte[] serialize(Serializable obj) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ObjectOutputStream objectStream = new ObjectOutputStream(stream);
            objectStream.writeObject(obj);
            objectStream.close();
            return stream.toByteArray();
        } catch (IOException excp) {
            System.out.println(excp);
            throw new Error("Internal error serializing commit.");
        }
    }
}



