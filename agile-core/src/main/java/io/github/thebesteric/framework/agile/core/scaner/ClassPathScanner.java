package io.github.thebesteric.framework.agile.core.scaner;

import io.github.thebesteric.framework.agile.commons.exception.ParseErrorException;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * ClassPathScanner
 *
 * @author Eric Joe
 * @version 1.0
 * @since 2022/7/25
 */
public interface ClassPathScanner {

    String CLASS_FILE_SUFFIX = ".class";
    String PACKAGE_SEPARATOR = ".";
    String PATH_SEPARATOR = File.separator;
    String PROTOCOL_FILE = "file";
    String PROTOCOL_JAR = "jar";

    default void scan(String projectPath, List<String> compilePaths) {
        projectPath = formatPath(projectPath);
        URL url = Thread.currentThread().getContextClassLoader().getResource("");
        if (url != null) {
            if (PROTOCOL_FILE.equals(url.getProtocol())) {
                doScan(new File(projectPath.endsWith(PATH_SEPARATOR) ? projectPath : projectPath + PATH_SEPARATOR), compilePaths);
            } else if (PROTOCOL_JAR.equals(url.getProtocol())) {
                try {
                    JarURLConnection connection = (JarURLConnection) url.openConnection();
                    JarFile jarFile = connection.getJarFile();
                    Enumeration<JarEntry> jarEntries = jarFile.entries();
                    while (jarEntries.hasMoreElements()) {
                        JarEntry jar = jarEntries.nextElement();
                        if (jar.isDirectory() || !jar.getName().endsWith(CLASS_FILE_SUFFIX)) {
                            continue;
                        }
                        String jarName = jar.getName();
                        String classPath = jarName.replaceAll(PATH_SEPARATOR, PACKAGE_SEPARATOR);
                        String className = classPath.substring(0, classPath.lastIndexOf(PACKAGE_SEPARATOR));
                        processClassFile(className);
                    }
                } catch (IOException e) {
                    throw new ParseErrorException("classpath scan error: ", e);
                }
            }
        }
    }

    default void doScan(File file, List<String> compilePaths) {
        if (file.isDirectory()) {
            for (File _file : Objects.requireNonNull(file.listFiles())) {
                doScan(_file, compilePaths);
            }
        } else {
            String filePath = file.getPath();
            int index = filePath.lastIndexOf(PACKAGE_SEPARATOR);
            if (index != -1 && CLASS_FILE_SUFFIX.equals(filePath.substring(index))) {
                filePath = extractLegalFilePath(filePath, compilePaths);
                if (filePath != null) {
                    String classPath = filePath.replace(PATH_SEPARATOR, PACKAGE_SEPARATOR);
                    String className = classPath.substring(0, classPath.lastIndexOf(PACKAGE_SEPARATOR));
                    processClassFile(className);
                }
            }
        }
    }

    default String extractLegalFilePath(String filePath, List<String> compilePaths) {
        for (String compilePath : compilePaths) {
            compilePath = formatPath(compilePath);
            int index = filePath.indexOf(compilePath);
            if (index != -1) {
                return filePath.substring(index + compilePath.length() + 1);
            }
        }
        return null;
    }

    default String formatPath(String path) {
        return path.replace("/", PATH_SEPARATOR).replace("\\", PATH_SEPARATOR);
    }

    void processClassFile(String className);
}
