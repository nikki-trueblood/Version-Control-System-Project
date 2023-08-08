package gitlet;
import java.io.IOException;
import java.io.Serializable;
import java.util.TreeMap;
import java.io.File;
import java.util.HashMap;
import java.util.Set;

public class Repository implements Serializable {
    /** Current working directory. */
    private static File pvcwd = new File(System.getProperty("user.dir"));
    public static File cwd() {
        return pvcwd;
    }
    /** Gitlet directory. */
    private static File pvgit = Utils.join(cwd(), ".gitlet");
    public static File git() {
        return pvgit;
    }
    /** All commits ever created. */
    private static File pvcommits = Utils.join(git(), "commits");
    public static File commits() {
        return pvcommits;
    }
    /** All blobs ever created. */
    private static File pvblobs = Utils.join(git(), "blobs");
    public static File blobs() {
        return pvblobs;
    }
    /** Staging area for adding/removing. */
    private static File pvstagingArea = Utils.join(git(), "stagingArea");
    public static File stagingArea() {
        return pvstagingArea;
    }
    /** Repository. */
    private static File pvrepository = Utils.join(git(), "repository");
    public static File repository() {
        return pvrepository;
    }
    /** Directory for files staged for addition. */
    private static File pvadd = Utils.join(stagingArea(), "add");
    public static File add() {
        return pvadd;
    }
    /** Directory for files staged for removal. */
    private static File pvremove = Utils.join(stagingArea(), "remove");
    public static File remove() {
        return pvremove;
    }
    /** Uid of the most recent commit. */
    private static String pvlatestCommit = null;
    public static String latestCommit() {
        return pvlatestCommit;
    }
    public static void setLatestCommit(String uid) {
        pvlatestCommit = uid;
    }
    /** Treemap of all branches. */
    private TreeMap<String, Branches> pvbranches = new TreeMap<>();

    /** The current branch. */
    private Branches pvcurrentBranch;
    public Branches currentBranch() {
        return pvcurrentBranch;
    }
    public void setCurrentBranch(Branches b) {
        pvcurrentBranch = b;
    }
    /** The uid of the head commit of the current branch. */
    private String head;
    public Repository() {
        if (git().exists()) {
            Repository repo = Utils.readObject(repository(), Repository.class);
            this.head = repo.getHead();
            this.pvbranches = repo.getBranches();
            this.pvcurrentBranch = repo.currentBranch();
        }
    }
    public void saveRepository() {
        Utils.writeObject(repository(), this);
    }
    public void init() {
        if (git().exists()) {
            System.out.println("A gitlet version-control system "
                    + "already exists in the current directory.");
            return;
        } else {
            git().mkdir();
            commits().mkdir();
            blobs().mkdir();
            try {
                repository().createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            stagingArea().mkdir();
            add().mkdir();
            remove().mkdir();
        }
        Commit initial = new Commit("initial commit", null);
        head = initial.getUid();
        Branches b = new Branches("master");
        pvbranches.put("master", b);
        setCurrentBranch(b);
        saveRepository();
    }
    public void add(String fileName) {
        File toAdd = new File(fileName);
        if (toAdd.exists()) {
            if (Utils.plainFilenamesIn(add()).contains(fileName)) {
                File repeat = Utils.join(add(), fileName);
                repeat.delete();
            }
            if (Utils.plainFilenamesIn(remove()).contains(fileName)) {
                File f = Utils.join(remove(), fileName);
                f.delete();
            }
            String contents = Utils.readContentsAsString(toAdd);
            File copy = new File(".gitlet/stagingArea/add/" + fileName);
            File headFile = Utils.join(commits(), head);
            Commit headCommit = Utils.readObject(headFile, Commit.class);
            if (!copy.exists()) {
                try {
                    copy.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Utils.writeContents(copy, contents);
            if (headCommit.getParent() != null) {
                HashMap<String, String> parentBlobIds = headCommit.getBlobIds();
                if (parentBlobIds.containsKey(fileName)) {
                    File blobFile = Utils.join(blobs(),
                            parentBlobIds.get(fileName));
                    String blobContents = Utils.readContentsAsString(blobFile);
                    if (blobContents.equals(contents)) {
                        File c = Utils.join(add(), fileName);
                        c.delete();
                    }
                }
            }
            String copyUid = Utils.sha1(Utils.serialize(copy));
            copy = Utils.join(add(), copyUid);
        } else {
            System.out.println("File does not exist.");
        }
        saveRepository();
    }
    public void remove(String fileName) {
        File headFile = Utils.join(commits(), head);
        Commit headCommit = Utils.readObject(headFile, Commit.class);
        File found = Utils.join(add(), fileName);
        if (found.exists()) {
            found.delete();
        } else if (headCommit.getBlobIds().containsKey(fileName)) {
            found = new File(fileName);
            File rm = Utils.join(remove(), fileName);
            try {
                rm.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (found.exists()) {
                found.delete();
            }
        } else {
            System.out.println("No reason to remove the file.");
            return;
        }
        saveRepository();
    }
    public void commit(String message) {
        if (message.isBlank()) {
            System.out.println("Please enter a commit message.");
            return;
        }
        File[] toAdd = add().listFiles();
        File[] toRemove = remove().listFiles();
        if (toAdd.length == 0 && toRemove.length == 0) {
            System.out.println("No changes added to the commit.");
        }
        Commit newCommit = new Commit(message, head);
        setLatestCommit(newCommit.getUid());
        currentBranch().setPointer(newCommit.getUid());
        head = newCommit.getUid();
        saveRepository();
    }
    public void log() {
        File h = Utils.join(".gitlet/commits/" + head);
        System.out.println("===");
        Commit x = Utils.readObject(h, Commit.class);
        System.out.println("commit " + head);
        System.out.println("Date: " + x.getTimestamp() + " -0800");
        System.out.println(x.getMessage());
        String parent = x.getParent();
        while (parent != null) {
            System.out.println();
            File p = Utils.join(".gitlet/commits/" + parent);
            System.out.println("===");
            Commit y = Utils.readObject(p, Commit.class);
            System.out.println("commit " + parent);
            System.out.println("Date: " + y.getTimestamp() + " -0800");
            System.out.println(y.getMessage());
            parent = y.getParent();
        }
        saveRepository();
    }
    public void globalLog() {
        for (File f : commits().listFiles()) {
            System.out.println("===");
            Commit x = Utils.readObject(f, Commit.class);
            System.out.println("commit " + x.getUid());
            System.out.println("Date: " + x.getTimestamp() + " -0800");
            System.out.println(x.getMessage());
            String parent = x.getParent();
            while (parent != null) {
                System.out.println();
                File p = Utils.join(".gitlet/commits/" + parent);
                System.out.println("===");
                Commit y = Utils.readObject(p, Commit.class);
                System.out.println("commit " + parent);
                System.out.println("Date: " + y.getTimestamp() + " -0800");
                System.out.println(y.getMessage());
                parent = y.getParent();
            }
            saveRepository();
        }
    }
    public void checkout(String commitID, String fileName) {
        File c = null;
        if (commitID.length() < getHead().length()) {
            for (String name : Utils.plainFilenamesIn(commits())) {
                if (name.contains(commitID)) {
                    c = Utils.join(commits(), name);
                }
            }
            if (c == null) {
                System.out.println("No commit with that id exists.");
                return;
            }
        } else {
            c = new File(".gitlet/commits/" + commitID);
        }
        if (!c.exists()) {
            System.out.println("No commit with that id exists.");
            return;
        }
        Commit cc = Utils.readObject(c, Commit.class);
        HashMap<String, String> ccblobs = cc.getBlobIds();
        if (!ccblobs.containsKey(fileName)) {
            System.out.println("File does not exist in that commit.");
            return;
        }
        String blobUid = ccblobs.get(fileName);
        File blobFound = Utils.join(blobs(), blobUid);
        String contents = Utils.readContentsAsString(blobFound);
        File newFile = new File(fileName);
        if (!newFile.exists()) {
            try {
                newFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Utils.writeContents(newFile, contents);
        saveRepository();
    }
    public void checkout1(String fileName) {
        checkout(head, fileName);
        saveRepository();
    }
    public void checkout2(String branchName) {
        if (pvbranches.keySet().contains(branchName)) {
            Branches b = getBranches().get(branchName);
            if (pvcurrentBranch == b) {
                System.out.println("No need to checkout the current branch.");
                return;
            }
            File f = Utils.join(commits(), b.getPointer());
            Commit c = Utils.readObject(f, Commit.class);
            File headFile = Utils.join(commits(), head);
            Commit headCommit = Utils.readObject(headFile, Commit.class);
            for (String name : Utils.plainFilenamesIn(pvcwd)) {
                if (!headCommit.getBlobIds().keySet().contains(name)
                        && c.getBlobIds().keySet().contains(name)) {
                    System.out.println("There is an untracked file"
                            + " in the way; delete it, "
                            + "or add and commit it first.");
                    return;
                }
            }
            for (String n : Utils.plainFilenamesIn(pvcwd)) {
                if (headCommit.getBlobIds().containsKey(n)
                        && !c.getBlobIds().containsKey(n)) {
                    File g = new File(n);
                    g.delete();
                }
            }
            for (String key : c.getBlobIds().keySet()) {
                File x = new File(key);
                if (!x.exists()) {
                    try {
                        x.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                File blob = Utils.join(blobs(), c.getBlobIds().get(key));
                Utils.writeContents(x, Utils.readContentsAsString(blob));
            }
            setCurrentBranch(b);
            head = b.getPointer();
            for (File a : add().listFiles()) {
                a.delete();
            }
            for (File r : remove().listFiles()) {
                r.delete();
            }
        } else {
            File headFile = Utils.join(commits(), head);
            Commit headCommit = Utils.readObject(headFile, Commit.class);
            System.out.println("No such branch exists.");
            return;
        }
        saveRepository();
    }
    public String getHead() {
        return head;
    }
    public void reset(String commitid) {
        if (Utils.plainFilenamesIn(commits()).contains(commitid)) {
            File foundFile = Utils.join(commits(), commitid);
            Commit foundCommit = Utils.readObject(foundFile, Commit.class);
            File fHead = Utils.join(commits(), head);
            Commit headCommit = Utils.readObject(fHead, Commit.class);
            for (String name : Utils.plainFilenamesIn(pvcwd)) {
                if (!headCommit.getBlobIds().containsKey(name)
                        && foundCommit.getBlobIds().containsKey(name)) {
                    System.out.println("There is an untracked "
                            + "file in the way; " + "delete it, or "
                            + "add and commit it first.");
                    return;
                }
            }
            for (String name : foundCommit.getBlobIds().keySet()) {
                checkout(commitid, name);
            }
            head = commitid;
            currentBranch().setPointer(commitid);
            for (File a : add().listFiles()) {
                a.delete();
            }
            for (File r : remove().listFiles()) {
                r.delete();
            }
        } else {
            System.out.println("No commit with that id exists.");
        }
        saveRepository();
    }
    public void status() {
        if (!git().isDirectory()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }
        System.out.println("=== Branches ===");
        TreeMap b = getBranches();
        for (Object key : b.keySet()) {
            if (b.get(key).equals(currentBranch())) {
                System.out.println("*" + key);
            } else {
                System.out.println(key);

            }
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        for (File f : add().listFiles()) {
            System.out.println(f.getName());
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        for (File f : remove().listFiles()) {
            System.out.println(f.getName());
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        System.out.println("=== Untracked Files ===");
        File headFile = Utils.join(commits(), head);
        Commit headCommit = Utils.readObject(headFile, Commit.class);
        Set<String> keys = headCommit.getBlobIds().keySet();
        for (File f : git().listFiles()) {
            if (!keys.contains(f.getName()) && f.isFile()
                    && !(f.getName().equals("repository"))) {
                System.out.println(f.getName());
            }
        }
    }
    public void find(String message) {
        boolean found = false;
        File[] allCommits = commits().listFiles();
        for (File f : allCommits) {
            Commit c = Utils.readObject(f, Commit.class);
            if (c.getMessage().contains(message)) {
                System.out.println(c.getUid());
                found = true;
            }
        }
        if (!found) {
            System.out.println("Found no commit with that message.");
        }
        saveRepository();
    }
    public void branch(String name) {
        Branches newBranch = new Branches(name);
        if (getBranches().keySet().contains(name)) {
            System.out.println("A branch with that name already exists.");
            return;
        } else {
            getBranches().put(name, newBranch);
        }
        saveRepository();
    }
    public void rmBranch(String name) {
        if (!getBranches().containsKey(name)) {
            System.out.println("A branch with that name does not exist.");
            return;
        } else if (getBranches().get(name) == currentBranch()) {
            System.out.println("Cannot remove the current branch.");
        } else {
            getBranches().remove(name);
        }
        saveRepository();
    }
    public void merge(String branchName) {
        File headFile = Utils.join(commits(), head);
        Commit headCommit = Utils.readObject(headFile, Commit.class);
        if (!getBranches().containsKey(branchName)) {
            System.out.println("A branch with that name does not exist.");
            return;
        } else if (branchName.equals(currentBranch().getName())) {
            System.out.println("Cannot merge a branch with itself.");
            return;
        } else if (add().listFiles().length != 0
                || remove().listFiles().length != 0) {
            System.out.println("You have uncommitted changes.");
            return;
        } else {
            for (String name : Utils.plainFilenamesIn(cwd())) {
                if (!headCommit.getBlobIds().keySet().contains(name)) {
                    System.out.println("There is an untracked file"
                            + " in the way; delete it, "
                            + "or add and commit it first.");
                    return;
                }
            }

        }
        saveRepository();
    }
    private class Branches implements Serializable {
        /** Pointer to the head commit of the branch. */
        private String pointer = null;
        /** Name of the branch. */
        private String name;
        Branches(String x) {
            name = x;
            pointer = head;
        }
        public void setPointer(String p) {
            this.pointer = p;
        }
        public String getPointer() {
            return pointer;
        }
        public String getName() {
            return name;
        }
    }

    public TreeMap<String, Branches> getBranches() {
        return pvbranches;
    }
}
