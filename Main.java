package gitlet;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Nikki Trueblood
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) {
        Repository repo = new Repository();
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            return;
        }
        if (args[0].equals("init")) {
            repo.init();
        } else if (args[0].equals("add")) {
            repo.add(args[1]);
        } else if (args[0].equals("commit")) {
            repo.commit(args[1]);
        } else if (args[0].equals("log")) {
            repo.log();
        } else if (args[0].equals("checkout")) {
            if (args[1].equals("--")) {
                repo.checkout1(args[2]);
            } else if (args.length <= 2) {
                repo.checkout2(args[1]);
            } else if (args[2].equals("--")) {
                repo.checkout(args[1], args[3]);
            } else {
                System.out.println("Incorrect operands.");
                return;
            }
        } else if (args[0].equals("status")) {
            repo.status();
        } else if (args[0].equals("rm")) {
            repo.remove(args[1]);
        } else if (args[0].equals("global-log")) {
            repo.globalLog();
        } else if (args[0].equals("find")) {
            repo.find(args[1]);
        } else if (args[0].equals("branch")) {
            repo.branch(args[1]);
        } else if (args[0].equals("rm-branch")) {
            repo.rmBranch(args[1]);
        } else if (args[0].equals("reset")) {
            repo.reset(args[1]);
        } else if (args[0].equals("merge")) {
            repo.merge(args[1]);
        } else {
            System.out.println("No command with that name exists.");
        }
    }
}
