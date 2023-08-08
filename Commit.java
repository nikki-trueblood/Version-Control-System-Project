package gitlet;
import java.io.IOException;
import java.util.HashMap;
import java.io.Serializable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.io.File;

public class Commit implements Serializable {
    /** The commit message. */
    private String message;
    /** The time a commit was created. */
    private String timestamp;
    /** The uid of the parent commit. */
    private String parent;
    /** The uid of the commit object. */
    private String uid;
    /** A hashmap mapping the filenames to their blobs. */
    private HashMap<String, String> blobIds = new HashMap<>();

    public Commit(String msg, String p) {
        this.message = msg;
        this.parent = p;
        if (p == null) {
            timestamp = "Thu Jan 1 00:00:00 1970";
        } else {
            Timestamp t = new Timestamp(System.currentTimeMillis());
            String tt = (new SimpleDateFormat("EEE MMM "
                    + "dd HH:mm:ss yyyy")).format(t.getTime());
            timestamp = tt;
        }
        if (p != null) {
            File headFile = Utils.join(".gitlet/commits/", this.getParent());
            Commit headCommit = Utils.readObject(headFile, Commit.class);
            if (headCommit.getParent() != null) {
                HashMap<String, String> parentBlobIds =
                        headCommit.getBlobIds();
                blobIds = parentBlobIds;
            }
        }
        addFiles();
        removeFiles();
        uid = Utils.sha1(Utils.serialize(this));
        File newCommit = new File(".gitlet/commits/" + uid);
        try {
            newCommit.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Utils.writeObject(newCommit, this);
    }
    public String getTimestamp() {
        return timestamp;
    }
    public String getParent() {
        return parent;
    }
    public String getMessage() {
        return message;
    }
    public String getUid() {
        return uid;
    }
    public HashMap<String, String> getBlobIds() {
        return blobIds;
    }
    public void addFiles() {
        File[] toAdd = Repository.add().listFiles();
        if (toAdd != null) {
            for (File f : toAdd) {
                String name = f.getName();
                String contents = Utils.readContentsAsString(f);
                String uuid = Utils.sha1(contents);
                blobIds.put(name, uuid);
                File newBlob = new File(".gitlet/blobs/" + uuid);
                try {
                    newBlob.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Utils.writeContents(newBlob, contents);
                f.delete();
            }
        }
    }
    public void removeFiles() {
        File[] toRemove = Repository.remove().listFiles();
        if (toRemove != null) {
            for (File g : toRemove) {
                String name = g.getName();
                blobIds.remove(name);
                g.delete();
            }
        }
    }


}
