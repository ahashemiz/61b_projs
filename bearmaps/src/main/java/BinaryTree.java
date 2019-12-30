

public class BinaryTree<T> {

    private TreeNode root;

    public BinaryTree() {
        root = null;
    }

    public BinaryTree(TreeNode t) {
        root = t;
    }


    public class TreeNode {

        KDPoint coords;
        TreeNode left;
        TreeNode right;
        String axis;
        //int splitmedian;

        public TreeNode(KDPoint item) {
            this.coords = item; left = right = null;
        }

        public TreeNode(KDPoint item, TreeNode left, TreeNode right, String axis) {
            this.coords = item;
            this.left = left;
            this.right = right;
            this.axis = axis;
        }


    }
}
