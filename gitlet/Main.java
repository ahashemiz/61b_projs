package gitlet;


import java.io.File;

/* Driver class for Gitlet, the tiny stupid version-control system.
   @author
*/
public class Main {


    /* Usage: java gitlet.Main ARGS, where ARGS contains
       <COMMAND> <OPERAND> .... */
    public static void main(String... args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            return;
        }
        File dir = new File(".gitlet");
        if (!dir.exists() && !args[0].equals("init")) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }
        switch (args[0]) {
            case "init":
                initCheck(args);
                break;
            case "add":
                addCheck(args);
                break;
            case "commit":
                if (args.length == 1 || args[1].equals("")) {
                    System.out.println("Please enter a commit message.");
                    break;
                }
                commitCheck(args);
                break;
            case "rm":
                rmCheck(args);
                break;
            case "log":
                logCheck(args);
                break;
            case "global-log":
                globalLogCheck(args);
                break;
            case "find":
                findCheck(args);
                break;
            case "status":
                statusCheck(args);
                break;
            case "checkout":
                switch (args.length) {
                    case 2:
                        Command.checkout3(args[1]);
                        break;
                    case 3:
                        if (args[1].equals("--")) {
                            Command.checkout1(args[2]);
                        } else {
                            System.out.println("Invalid input");
                        }
                        break;
                    case 4:
                        if (args[2].equals("--")) {
                            Command.checkout2(args[1], args[3]);
                        } else {
                            System.out.println("Incorrect operands.");
                        }
                        break;
                    default:
                        System.out.println("Invalid Input");
                }
                break;
            case "branch":
                branchCheck(args);
                break;
            case "rm-branch":
                rmBranchCheck(args);
                break;
            case "reset":
                resetCheck(args);
                break;
            case "merge":
                mergeCheck(args);
                break;
            default:
                System.out.println("No command with that name exists.");
        }
    }

    private static void initCheck(String... args) {
        if (args.length != 1) {
            System.out.println("Incorrect operands.");
            return;
        }
        Command.init();
    }

    private static void addCheck(String... args) {
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
            return;
        }
        Command.addHelper(args);
    }

    private static void commitCheck(String... args) {
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
            return;
        }
        if (args[1].length() == 0) {
            System.out.println("Please enter a commit message.");
            return;
        }
        Command.makeCommit(args[1], false);
    }

    private static void rmCheck(String... args) {
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
            return;
        }
        Command.removeHelper(args);
    }

    private static void logCheck(String... args) {
        if (args.length != 1) {
            System.out.println("Incorrect operands.");
            return;
        }
        Command.log();
    }

    private static void globalLogCheck(String... args) {
        if (args.length != 1) {
            System.out.println("Incorrect operands.");
            return;
        }
        Command.globalLog();
    }

    private static void findCheck(String... args) {
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
            return;
        }
        Command.find(args[1]);
    }

    private static void statusCheck(String... args) {
        if (args.length != 1) {
            System.out.println("Incorrect operands.");
            return;
        }
        Command.status();
    }

    private static void branchCheck(String... args) {
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
            return;
        }
        Command.branch(args[1]);
    }

    private static void rmBranchCheck(String... args) {
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
            return;
        }
        Command.rmBranch(args[1]);
    }

    private static void resetCheck(String... args) {
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
            return;
        }
        Command.reset(args[1]);
    }

    private static void mergeCheck(String... args) {
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
            return;
        }
        Command.merge(args[1]);
    }

}

