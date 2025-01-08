package io.github.thebesteric.framework.agile.plugins.sensitive.filter;

import io.github.thebesteric.framework.agile.commons.util.LoggerPrinter;
import io.github.thebesteric.framework.agile.plugins.sensitive.filter.config.AgileSensitiveFilterProperties;
import io.github.thebesteric.framework.agile.plugins.sensitive.filter.domain.SensitiveFilterResult;
import io.github.thebesteric.framework.agile.plugins.sensitive.filter.domain.TrieNode;
import io.github.thebesteric.framework.agile.plugins.sensitive.filter.processor.AgileSensitiveResultProcessor;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * SensitiveFilter
 *
 * @author wangweijun
 * @version v1.0
 * @since 2025-01-08 13:43:55
 */
@Slf4j
public class AgileSensitiveFilter {
    /** 配置文件 */
    private final AgileSensitiveFilterProperties properties;
    /** 结果处理器 */
    private AgileSensitiveResultProcessor resultProcessor;


    /** 敏感词文件 */
    private final File sensitiveFile;
    /** 根节点 */
    private final TrieNode rootNode;

    private static final String EMPTY_FILE_PATH = "not_exist_file_path";


    public AgileSensitiveFilter(AgileSensitiveFilterProperties properties, @Nullable AgileSensitiveResultProcessor resultProcessor) {
        String filePath = properties.getFilePath();
        if (StringUtils.isBlank(filePath)) {
            filePath = EMPTY_FILE_PATH;
        }
        this.properties = properties;
        this.sensitiveFile = new File(filePath);
        this.rootNode = new TrieNode();
        this.resultProcessor = resultProcessor;
    }

    @PostConstruct
    public void init() {
        // 是否开启敏感词过滤
        if (!properties.isEnable()) {
            return;
        }
        // 加载文件
        if (!sensitiveFile.exists()) {
            LoggerPrinter.warn(log, "敏感词文件不存在，请检查路径是否正确");
            return;
        }
        // 加载敏感词
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(sensitiveFile)))) {
            String keyword;
            while (StringUtils.isNotBlank((keyword = reader.readLine()))) {
                // 添加到前缀树
                this.addKeyword(keyword);
            }
        } catch (IOException e) {
            LoggerPrinter.error(log, "加载敏感词文件失败: " + e.getMessage());
        }
    }

    /**
     * 将一个敏感词添加到前缀树中
     *
     * @param keyword 敏感词
     *
     * @author wangweijun
     * @since 2025/1/8 13:58
     */
    private void addKeyword(String keyword) {
        TrieNode tempNode = rootNode;
        for (int i = 0; i < keyword.length(); i++) {
            char c = keyword.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);
            if (subNode == null) {
                // 初始化子节点
                subNode = new TrieNode();
                tempNode.addSubNode(c, subNode);
            }
            // 指向子节点,进入下一轮循环
            tempNode = subNode;
            // 设置结束标识
            if (i == keyword.length() - 1) {
                tempNode.setKeywordEnd(true);
            }
        }
    }

    /**
     * 敏感词过滤
     *
     * @param text 待过滤的文本
     *
     * @return 过滤结果
     *
     * @author wangweijun
     * @since 2025/1/8 14:01
     */
    public SensitiveFilterResult filter(String text) {
        if (StringUtils.isBlank(text) || !properties.isEnable()) {
            return SensitiveFilterResult.empty(text, properties.getPlaceholder());
        }
        if (!sensitiveFile.exists()) {
            LoggerPrinter.warn(log, "敏感词文件不存在，请检查路径是否正确");
            return SensitiveFilterResult.empty(text, properties.getPlaceholder());
        }
        // 节点指针
        TrieNode tempNode = rootNode;
        // 开始指针
        int begin = 0;
        // 位置指针
        int position = 0;
        // 最终结果
        StringBuilder contentResult = new StringBuilder();
        List<SensitiveFilterResult.Sensitive> sensitiveWords = new ArrayList<>();

        while (text.length() > position) {
            char c = text.charAt(position);
            // 跳过符号
            if (isSymbol(c)) {
                // 1. 若 tempNode 处于根节点，则将此符号计入结果,让 begin 指针右移
                // 2. 若 tempNode 处于子节点，则表示发现了敏感词，此时 begin 指针不移动
                if (tempNode == rootNode) {
                    // 加入结果集
                    contentResult.append(c);
                    begin++;
                }
                // 无论符号在开头或中间，position 指针都右移
                position++;
                continue;
            }

            // 检查下级节点
            tempNode = tempNode.getSubNode(c);
            if (tempNode == null) {
                // 表示当前 begin 指针指向的字符不是敏感词，即：加入结果集
                contentResult.append(text.charAt(begin));
                // begin 进入下一个位置
                begin++;
                // position 移动到 begin 的位置
                position = begin;
                // 重新指向根节点
                tempNode = rootNode;
            } else if (tempNode.isKeywordEnd()) {
                // 发现敏感词,将 begin ~ position 之间的字符串替换掉
                contentResult.append(properties.getPlaceholder());
                // 添加敏感词到结果集
                sensitiveWords.add(SensitiveFilterResult.Sensitive.of(begin, position, text.substring(begin, position + 1)));
                // position 进入下一个位置
                position++;
                // begin 移动到 position 的位置
                begin = position;
                // 重新指向根节点
                tempNode = rootNode;
            } else {
                // 检查下一个字符
                position++;
            }
        }

        // 将最后一个字符计入结果
        // 此时 position == text.length - 1; position 比 begin 先达到临界点，所以会出现最后得值没有添加进去，所以要手动添加进一次
        contentResult.append(text.substring(begin));

        // 返回结果
        SensitiveFilterResult result = SensitiveFilterResult.of(text, contentResult.toString(), properties.getPlaceholder(), sensitiveWords);
        if (resultProcessor != null) {
            resultProcessor.process(result);
        }
        return result;
    }

    /**
     * 判断是否为符号
     *
     * @param character 待判断的字符
     *
     * @return boolean
     *
     * @author wangweijun
     * @since 2025/1/8 14:03
     */
    private boolean isSymbol(Character character) {
        List<Character> extendSymbols = properties.getSymbols();
        if (extendSymbols.contains(character)) {
            return true;
        }
        // 0x2E80~0x9FFF 是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(character) && (character < 0x2E80 || character > 0x9FFF);
    }
}
