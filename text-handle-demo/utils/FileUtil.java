
/**
 * @author dover
 * @date 2025-03-04
 */
public class FileUtil {

    public static void createZip(String sourceDirPath, String zipFilePath) throws IOException {
        Path sourceDir = Paths.get(sourceDirPath);
        Path zipPath = Paths.get(zipFilePath);
        File zipFile = zipPath.toFile();
        if (!zipFile.exists()) Files.createTempFile(zipFile.getName(), zipFile.getName().substring(zipFile.getName().lastIndexOf(".")));
        // 创建一个 Map 来存储 Zip 文件系统的属性
        Map<String, String> env = new HashMap<>();
        env.put("create", "true");  // 如果文件不存在，则创建
        // 使用 zipfs 文件系统来处理 ZIP 文件
        try (FileSystem fs = FileSystems.newFileSystem(URI.create("jar:" + zipPath.toUri()), env)) {
            Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Path targetPath = fs.getPath(sourceDir.relativize(file).toString());
                    if (targetPath.getParent() != null) {
                        Files.createDirectories(targetPath.getParent());
                    }
                    Files.copy(file, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    if (!dir.equals(sourceDir)) {
                        Path targetDir = fs.getPath(sourceDir.relativize(dir).toString());
                        Files.createDirectories(targetDir);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    public static void main(String[] args) throws IOException {
        String sourceDirPath = "D:\\workspace\\dover-demo\\src\\test\\java\\com\\dover\\demo";
        String zipFilePath = "D:\\archive.zip";
        try {
            createZip(sourceDirPath, zipFilePath);
            System.out.println("目录已成功打包为 ZIP 文件: " + zipFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
