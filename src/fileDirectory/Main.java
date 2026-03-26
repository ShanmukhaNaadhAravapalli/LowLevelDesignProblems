package fileDirectory;
import java.util.*;
import java.util.regex.Pattern;

abstract class AbstractNode {
    protected String name;
    protected Date createdAt;

    public AbstractNode(String name) {
        this.name = name;
        this.createdAt = new Date();
    }

    public String getName() {
        return name;
    }

    public Date getCreatedAt() {
        return createdAt;
    }
}

class FileNode extends AbstractNode {
    private String content;
    private int size;

    public FileNode(String name) {
        super(name);
        this.content = "";
        this.size = 0;
    }

    public void appendContent(String newContent) {
        this.content += newContent;
        this.size += newContent.length();
    }

    public String readContent() {
        return content;
    }

    public int getSize() {
        return size;
    }
}

class DirectoryNode extends AbstractNode {
    private Map<String, AbstractNode> children;

    public DirectoryNode(String name) {
        super(name);
        this.children = new HashMap<>();
    }

    public void addNode(AbstractNode node) {
        children.put(node.getName(), node);
    }

    public List<AbstractNode> getChildren() {
        return new ArrayList<>(children.values());
    }

    public AbstractNode getNode(String name) {
        return children.get(name);
    }
}
interface NodeFilter {
    boolean apply(AbstractNode node, Map<String, Object> params);
}
class FileSizeFilter implements NodeFilter {
    @Override
    public boolean apply(AbstractNode node, Map<String, Object> params) {
        if (!(node instanceof FileNode)) return true;
        if (!params.containsKey("minSize") || !params.containsKey("maxSize")) return true;

        int minSize = (int) params.get("minSize");
        int maxSize = (int) params.get("maxSize");
        int size = ((FileNode) node).getSize();

        return size >= minSize && size <= maxSize;
    }
}

class FilenameFilter implements NodeFilter {
    @Override
    public boolean apply(AbstractNode node, Map<String, Object> params) {
        if (!params.containsKey("filenameRegex")) return true;
        String regex = (String) params.get("filenameRegex");
        return Pattern.matches(regex, node.getName());
    }
}

class NodeFilterChain {
    private List<NodeFilter> filters;

    public NodeFilterChain() {
        this.filters = new ArrayList<>();
    }

    public void addFilter(NodeFilter filter) {
        filters.add(filter);
    }

    public boolean applyFilters(AbstractNode node, Map<String, Object> params) {
        for (NodeFilter filter : filters) {
            if (!filter.apply(node, params)) {
                return false; // If any filter fails, reject the node
            }
        }
        return true;
    }
}
interface NodeSearchStrategy {
    List<AbstractNode> search(DirectoryNode directory, Map<String, Object> params);
}
class FilenameAndSizeSearchStrategy implements NodeSearchStrategy {
    private NodeFilterChain filterChain;

    public FilenameAndSizeSearchStrategy() {
        this.filterChain = new NodeFilterChain();
        filterChain.addFilter(new FilenameFilter());
        filterChain.addFilter(new FileSizeFilter());
    }

    @Override
    public List<AbstractNode> search(DirectoryNode directory, Map<String, Object> params) {
        List<AbstractNode> result = new ArrayList<>();
        searchRecursive(directory, params, result);
        return result;
    }

    private void searchRecursive(DirectoryNode dir, Map<String, Object> params, List<AbstractNode> result) {
        for (AbstractNode node : dir.getChildren()) {
            if (filterChain.applyFilters(node, params)) {
                result.add(node);
            }
            if (node instanceof DirectoryNode) {
                searchRecursive((DirectoryNode) node, params, result);
            }
        }
    }
}

class FileSystem {
    private DirectoryNode root;

    public FileSystem() {
        this.root = new DirectoryNode("/");
    }

    private DirectoryNode traverse(String path, boolean createMissingDirs) {
        String[] parts = path.split("/");
        DirectoryNode current = root;

        for (int i = 1; i < parts.length; i++) {
            if (!(current.getNode(parts[i]) instanceof DirectoryNode)) {
                if (createMissingDirs) {
                    current.addNode(new DirectoryNode(parts[i]));
                } else {
                    return null;
                }
            }
            current = (DirectoryNode) current.getNode(parts[i]);
        }
        return current;
    }

    public void mkdir(String path) {
        traverse(path, true);
    }

    public void addFile(String filePath, String content) {
        DirectoryNode parent = traverse(filePath.substring(0, filePath.lastIndexOf("/")), true);
        String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);

        FileNode file = (FileNode) parent.getNode(fileName);
        if (file == null) {
            file = new FileNode(fileName);
            parent.addNode(file);
        }
        file.appendContent(content);
    }

    public List<AbstractNode> searchNodes(String directoryPath, NodeSearchStrategy strategy, Map<String, Object> params) {
        DirectoryNode directory = traverse(directoryPath, false);
        if (directory == null) {
            throw new IllegalArgumentException("Directory not found: " + directoryPath);
        }
        return strategy.search(directory, params);
    }
}

public class Main {
    public static void main(String[] args) {
        FileSystem fs = new FileSystem();

        fs.mkdir("/a/b/c");
        fs.addFile("/a/b/c/file1.txt", "Hello");
        fs.addFile("/a/b/c/file2.log", "Data");
        fs.addFile("/a/file3.txt", "Some large content to increase file size...");
        fs.addFile("/a/file4.txt", "Short");

        System.out.println("Searching for files and directories matching regex 'file.*\\.txt' with size between 5 and 50 bytes:");

        NodeSearchStrategy strategy = new FilenameAndSizeSearchStrategy();
        Map<String, Object> searchParams = new HashMap<>();
        searchParams.put("filenameRegex", "file.*\\.txt");
        searchParams.put("minSize", 5);
        searchParams.put("maxSize", 50);

        List<AbstractNode> foundNodes = fs.searchNodes("/a", strategy, searchParams);

        for (AbstractNode node : foundNodes) {
            if (node instanceof FileNode) {
                System.out.println("[FILE] " + node.getName() + " (Size: " + ((FileNode) node).getSize() + " bytes)");
            } else {
                System.out.println("[DIR] " + node.getName());
            }
        }
    }
}
