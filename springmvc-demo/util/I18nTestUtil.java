
class I18nHandleTest {

    @SneakyThrows
    public static void main(String[] args) {
        checkErrorNo(ResourceUtils.getFile("classpath:messages_zh.properties"));
//        HashMap<String, String> cache = parseValidationMsg(ResourceUtils.getFile("classpath:export.txt"));
//        replaceJavaFile(cache);
//        parseBizMsg(ResourceUtils.getFile("classpath:export.txt"));
//        toEnum(ResourceUtils.getFile("classpath:messages_zh.properties"), "Test.txt");
    }

    @SneakyThrows
    public static void checkErrorNo(File file) {
        List<String> strings = Files.readAllLines(file.toPath());
//        Set<String> keyList = strings.stream().filter(StringUtils::isNotBlank).map(line -> line.split("=")[0]).collect(Collectors.toSet());
//        for (int i = 1000001; i < 1000910; i++) {
//            String key = "MES" + i;
//            if(!keyList.contains(key)) {
//                System.out.println(key);
//            }
//        }
        HashMap<String, Boolean> valueSet = new HashMap<>();
        for (String string : strings) {
            if (StringUtils.hasText(string)) {
                if (!string.endsWith("=")) {
                    String value = string.split("=")[1];
                    if (valueSet.containsKey(value) && !valueSet.get(value)) {
                        System.out.println(StringEscapeUtils.unescapeJava(value));
                        valueSet.put(value, true);
                    } else {
                        valueSet.put(value, false);
                    }
                }
            }
        }

    }

    private static HashMap<String, String> parseBizMsg(File file) throws IOException {
        BufferedReader bufferedReader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8);
        Pattern omsResultPattern = Pattern.compile("OmsResult\\.error\\(\"(.+)\"\\)");
        HashMap<String, String> cache = new HashMap<>();
        int idx = 5000002;
        String s;
        while ((s = bufferedReader.readLine()) != null) {
            Matcher matcher = omsResultPattern.matcher(s);
            if (matcher.find()) {
                String value = replaceTokens(StringEscapeUtils.unescapeJava(matcher.group(1)));
                if (!cache.containsKey(value)) {
                    String key = "SCMOMS" + idx++;
                    String result = key + "=" + value;
                    System.out.println(result);
                    cache.put(value, key);
                }
            }
        }
        bufferedReader.close();
        return cache;
    }

    //String s = "该替代方案{0}已经{0}，无需{0}再次{0}操作";
    //如果字符串s中包含多个{0}, 则将其按顺序替换为{0}, {1}, {2}, 以此类推
    public static String replaceTokens(String input) {
        if (input == null || !input.contains("{0}")) {
            return input;
        }
        StringBuilder result = new StringBuilder();
        int tokenCount = 0;
        int lastIndex = 0;
        while (true) {
            int index = input.indexOf("{0}", lastIndex);
            if (index == -1) {
                result.append(input.substring(lastIndex));
                break;
            }
            result.append(input, lastIndex, index);
            result.append("{").append(tokenCount++).append("}");
            lastIndex = index + 3; // 跳过 "{0}"
        }
        return result.toString();
    }

    @SneakyThrows
    public static void toEnum(File file, String targetFilePath) {
        String inputFilePath = file.getPath();
        Path inputFilePathObj = Paths.get(inputFilePath);
        targetFilePath = inputFilePathObj.getParent().toString() + targetFilePath;
        try {
            List<String> lines = Files.readAllLines(inputFilePathObj);
            List<String> enumConstants = new ArrayList<>();
            for (String line : lines) {
                if (line.trim().isEmpty() || line.startsWith("#")) {
                    continue; // 忽略空行和注释
                }
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    String value = StringEscapeUtils.unescapeJava(parts[1].trim());
                    // 添加注释和枚举定义
                    enumConstants.add("// " + value);
                    enumConstants.add(key + "(\"" + key + "\", \"" + value + "\"),");
                    enumConstants.add("");
                }
            }
            writeEnumToFile(targetFilePath, enumConstants);
            System.out.println("✅ 枚举代码已成功生成！");
        } catch (IOException e) {
            System.err.println("❌ 文件读取失败: " + e.getMessage());
        }
    }

    private static void writeEnumToFile(String outputPath, List<String> enumConstants) throws IOException {
        Path path = Paths.get(outputPath);
        BufferedWriter writer = Files.newBufferedWriter(path);
        for (String line : enumConstants) {
            writer.write("    " + line + "\n");
        }
        writer.close();
    }

    @SneakyThrows
    public static HashMap<String, String> parseValidationMsg(File file) {
        Path path = file.toPath();
        BufferedReader bufferedReader = Files.newBufferedReader(path, StandardCharsets.UTF_8);
        String s;
        int idx = 5000001;
        HashMap<String, String> cache = new HashMap<>(600);
        while ((s = bufferedReader.readLine()) != null) {
            Matcher matcher = pattern.matcher(s);
            if (matcher.find()) {
                String value = replaceTokens(StringEscapeUtils.unescapeJava(matcher.group(1)));
                if (!cache.containsKey(value)) {
                    String key = "MES" + idx++;
                    String result = key + "=" + value;
                    System.out.println(result);
                    cache.put(value, key);
                }
            }
//            System.out.println("MES" + idx++ + "=" + replaceTokens(StringEscapeUtils.unescapeJava(s)));
//            System.out.println(replaceTokens(StringEscapeUtils.unescapeJava(s)));
        }
        bufferedReader.close();
        return cache;
    }

    static Pattern pattern = Pattern.compile("message\\s*=\\s*\"(.+)\"");

    @SneakyThrows
    public static void replaceJavaFile(HashMap<String, String> cache) {
        String suffix = ".java";
//        Path path = Paths.get("D:\\workspace\\Manufacturing-Execution-System\\mes-admin-service\\src\\main\\java\\com\\zczy\\mes\\dto");
        Path path = Paths.get("D:\\workspace\\Manufacturing-Execution-System\\mes-common\\src\\main\\java\\com\\zczy\\mes\\common\\dto\\PageDto.java");
        recursiveReplace(cache, path.toFile(), suffix);
    }

    private static void recursiveReplace(HashMap<String, String> cache, File currentFile, String suffix) throws IOException {
        if (currentFile.isDirectory()) {
            File[] files = currentFile.listFiles();
            if (files != null) {
                for (File f : files) {
                    recursiveReplace(cache, f, suffix);
                }
            }
        } else if (currentFile.isFile()) {
            replaceValidationFile(cache, currentFile, suffix);
        }
    }

    private static void replaceValidationFile(HashMap<String, String> cache, File f, String s) throws IOException {
        if (f.getName().endsWith(s)) {
//                    if(f.getName().contains("ScheduleOrderQueryReq")) {
            Path filePath = f.toPath();
            BufferedReader bufferedReader = Files.newBufferedReader(f.toPath(), StandardCharsets.UTF_8);
            String line;
            List<String> modifiedLines = new ArrayList<>();
            while ((line = bufferedReader.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    String substring = matcher.group(1);
                    if (cache.containsKey(substring))
                        line = line.replace(substring, cache.get(substring));
                }
                modifiedLines.add(line);
            }
            bufferedReader.close();
            Files.write(filePath, modifiedLines, StandardOpenOption.TRUNCATE_EXISTING);
        }
    }
}