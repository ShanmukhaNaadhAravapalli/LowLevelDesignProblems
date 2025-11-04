package unixFile;

import com.sun.source.tree.IfTree;

import javax.lang.model.type.ArrayType;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
    abstract int getSize();
    abstract boolean isDirectory();
}
class File extends AbstractNode {
    private byte[] content;
    public File(String name){
        super(name);
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public String getExtension() {
        return name.substring(name.indexOf(".") + 1);
    }

    @Override
    public int getSize() {
        return content.length;
    }

    @Override
    public boolean isDirectory(){
        return false;
    }

}

class Directory extends AbstractNode{
    private List<AbstractNode> children = new ArrayList<>();

    public Directory(String name) {
        super(name);
    }

    public List<AbstractNode> getChildren(){
        return children;
    }

    public void add(AbstractNode abstractNode){
        children.add(abstractNode);
    }

    @Override
    public int getSize() {
        int size = 0;
        for(AbstractNode node: children){
            size+= node.getSize();
        }
        return size;
    }

    public File getNode(String name){
        for(AbstractNode node: children){
            if(!node.isDirectory() &&  node.getName().equals(name))
                return (File) node;
        }
        return null;
    }

    public Directory getDirectory(String name){
        for(AbstractNode node: children){
            if(node.isDirectory() &&  node.getName().equals(name))
                return (Directory) node;
        }
        return null;
    }

    @Override
    public boolean isDirectory(){
        return true;
    }
}

// make this
class SearchParams {
    public Integer getMinSize() {
        return minSize;
    }

    public void setMinSize(Integer minSize) {
        this.minSize = minSize;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public Integer getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(Integer maxSize) {
        this.maxSize = maxSize;
    }

    public String getFilenameRegex() {
        return filenameRegex;
    }

    public void setFilenameRegex(String filenameRegex) {
        this.filenameRegex = filenameRegex;
    }

    private String extension;
    private Integer minSize;
    private Integer maxSize;
    private String filenameRegex;

}

interface IFilter {
    boolean isValid(SearchParams params, File file);
}

class ExtensionFilter implements IFilter {

    @Override
    public boolean isValid(SearchParams params, File file) {
        if (params.getExtension() == null) {
            return true;
        }

        return file.getExtension().equals(params.getExtension());
    }
}

class MaxSizeFilter implements IFilter{
    @Override
    public boolean isValid(SearchParams params, File file) {
        if (params.getMaxSize() == null) {
            return true;
        }

        return file.getSize() <= params.getMaxSize();
    }
}

class MinSizeFilter implements IFilter{
    @Override
    public boolean isValid(SearchParams params, File file) {
        if (params.getMinSize() == null) {
            return true;
        }

        return file.getSize() >= params.getMinSize();
    }
}

class NameFilter implements IFilter {

    @Override
    public boolean isValid(SearchParams params, File file) {
        if (params.getFilenameRegex() == null) {
            return true;
        }

        return Pattern.matches(params.getFilenameRegex(), file.getName());
    }

}

class FilterChain {
    private List<IFilter> filters;

    public FilterChain() {
        this.filters = new ArrayList<>();
    }
    public void addFilter(IFilter filter) {
        filters.add(filter);
    }

    public boolean applyFilters(File file, SearchParams searchParams){
        for(IFilter filter: filters){
            if(!filter.isValid(searchParams, file))
                return false;
        }
        return true;
    }

}

class FileNameSearchAndFilter {
    private FilterChain filterChain;
    public FileNameSearchAndFilter(){
        this.filterChain = new FilterChain();
        filterChain.addFilter(new ExtensionFilter());
        filterChain.addFilter(new MaxSizeFilter());
        filterChain.addFilter(new MaxSizeFilter());
        filterChain.addFilter(new NameFilter());
    }

    public List<File> search(Directory directory, SearchParams searchParams){
        List<File> result = new ArrayList<>();
        searchRecursive(directory, searchParams, result);
        return result;
    }

    public void searchRecursive(Directory directory, SearchParams searchParams, List<File> result){
        for(AbstractNode node: directory.getChildren()){
            if(node.isDirectory())
                searchRecursive((Directory) node, searchParams, result);
            else if(filterChain.applyFilters((File) node, searchParams))
                result.add((File) node);
        }
    }
}

class FileSystem {
    private Directory root;
    public FileSystem(){
        this.root = new Directory("/");
    }
    public Directory traverse(String path, boolean createMissingDirs){
        String parts[] = path.split("/");
        Directory current = root;
        for(int i = 1; i< parts.length ; i++){
            if(parts[i].isEmpty())
                continue;
            if(current.getDirectory(parts[i]) == null){
                if(createMissingDirs)
                    current.add(new Directory(parts[i]));
                else
                    return null;
            }
            current = (Directory) current.getDirectory(parts[i]);
        }
        return current;
    }
    public void mkdir(String path){
        traverse(path, true);
    }
    public void addFile(String filePath, byte[] content) {
        Directory parent = traverse(filePath.substring(0, filePath.lastIndexOf("/")), true);
        String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);

        File file = (File) parent.getNode(fileName);
        if (file == null) {
            file = new File(fileName);
            parent.add(file);
        }
        file.setContent(content);
    }
    public List<File> searchNodes(String directoryPath, FileNameSearchAndFilter strategy, SearchParams params) {
        Directory directory = traverse(directoryPath, false);
        if (directory == null) {
            throw new IllegalArgumentException("Directory not found: " + directoryPath);
        }
        return strategy.search(directory, params);
    }

}
public class UnixFileCommand {
}
