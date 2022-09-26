import java.util.Scanner;

/**
 * This class represents a node in a binary tree.
 */
class Node {
    String data;
    Node left, right;
    Node(String data) {
        this.data = data;
        this.left = this.right = null;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        print(sb, "", "");
        return sb.toString();
    }

    public void print(StringBuilder sb, String prefix, String childPrefix) {
        sb.append(prefix);
        sb.append(data);
        sb.append("\n");
        
        if (right != null) {
            right.print(sb, childPrefix + "└── (R) ", childPrefix + "│    ");
        }
        if (left != null) {
            left.print(sb, childPrefix + "└── (L) ", childPrefix + "   ");
        }
    }
}
 
class CreateBinaryTree {
    private static Node node;
    
    /**
     * Checks if the "a" string is the first string alphabetically.
     * 
     * @param a String to compare
     * @param b String to compare
     * @return true if a is larger than b, false otherwise
     */
    public static boolean stringCompare(String a, String b) {
        char[] chars_a = a.toCharArray();
        char[] chars_b = b.toCharArray();
        int longest;
        if (chars_a.length > chars_b.length) {
            longest = chars_a.length;
        } else {
            longest = chars_b.length;
        }

        for (int i = 0; i < longest; i++) {
            int compare_a = 0;
            int compare_b = 0;

            if (i > chars_a.length - 1) {
                return false;
            }
            if (i > chars_b.length - 1) {
                return true;
            }
            
            if (chars_a[i] > 96) compare_a = chars_a[i] -= 32;
            else compare_a = chars_a[i];

            if (chars_b[i] > 96) compare_b = chars_b[i] -= 32;
            else compare_b = chars_b[i];
            
            if (compare_a < compare_b) {
                return true;
            } else if (compare_a > compare_b) {
                return false;
            }
        }

        return false;
    }
 
    public static Node addNode(Node node, String data) {
        // If the node is empty, create root node
        if (node == null)
            node = new Node(data);

        if (stringCompare(data, node.data))
            node.left = addNode(node.left, data);
        else if (stringCompare(node.data, data))
            node.right = addNode(node.right, data);
 
        return node;
    }
 
    // A wrapper function of addNode
    public static void create(String data) {
        node = addNode(node, data);
    }

    public static int height(Node node) {
        if (node == null) return 0;
        else {
            int left = height(node.left);
            int right = height(node.right);
            if (left > right) return left + 1;
            else return right + 1;
        }
    }
    
    // javac Tre.java && java CreateBinaryTree
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        for(;;) {  
            System.out.print("Enter words here (Type exit to exit): ");
            String[] input = sc.nextLine().split(" ");
            for (String i : input) {
                create(i);
                if(i.equals("exit")) {
                    sc.close();
                    return;
                }
            }
        System.out.println("\n" + node.toString());
        }
    }
}