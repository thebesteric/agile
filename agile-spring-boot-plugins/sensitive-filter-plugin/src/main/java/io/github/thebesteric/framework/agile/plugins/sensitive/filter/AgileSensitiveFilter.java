package io.github.thebesteric.framework.agile.plugins.sensitive.filter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.thebesteric.framework.agile.commons.util.JsonUtils;
import io.github.thebesteric.framework.agile.commons.util.LoggerPrinter;
import io.github.thebesteric.framework.agile.plugins.sensitive.filter.config.AgileSensitiveFilterProperties;
import io.github.thebesteric.framework.agile.plugins.sensitive.filter.domain.SensitiveFilterResult;
import io.github.thebesteric.framework.agile.plugins.sensitive.filter.domain.TrieNode;
import io.github.thebesteric.framework.agile.plugins.sensitive.filter.exception.SensitiveException;
import io.github.thebesteric.framework.agile.plugins.sensitive.filter.extension.AgileOtherTypeSensitiveLoader;
import io.github.thebesteric.framework.agile.plugins.sensitive.filter.extension.AgileSensitiveResultProcessor;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * SensitiveFilter
 *
 * @author wangweijun
 * @version v1.0
 * @since 2025-01-08 13:43:55
 */
@Slf4j
public class AgileSensitiveFilter {

    private static final LoggerPrinter loggerPrinter = LoggerPrinter.newInstance();

    /** 配置文件 */
    private final AgileSensitiveFilterProperties properties;

    /** 其他方式敏感词加载器: will be autowired by user defined */
    private final AgileOtherTypeSensitiveLoader otherTypeSensitiveLoader;
    /** 结果处理器: will be autowired by user defined */
    private final AgileSensitiveResultProcessor resultProcessor;

    /** 敏感词文件 */
    private final File sensitiveFile;
    /** 根节点 */
    private TrieNode rootNode;
    /** 数据加载类型 */
    private final AgileSensitiveFilterProperties.LoadType loadType;
    /** 文件加载地址 */
    private final String filePath;

    /** 敏感词是否已经加载 */
    @Getter
    private boolean loaded;

    public AgileSensitiveFilter(AgileSensitiveFilterProperties properties) {
        this(properties, null, null);
    }

    public AgileSensitiveFilter(AgileSensitiveFilterProperties properties, @Nullable AgileOtherTypeSensitiveLoader otherTypeSensitiveLoader, @Nullable AgileSensitiveResultProcessor resultProcessor) {
        this.filePath = properties.getFilePath();
        this.loadType = properties.getLoadType();
        if (AgileSensitiveFilterProperties.LoadType.OTHER == this.loadType || StringUtils.isBlank(filePath)) {
            this.sensitiveFile = null;
        } else {
            this.sensitiveFile = new File(this.filePath);
        }
        this.rootNode = new TrieNode();
        this.properties = properties;
        this.otherTypeSensitiveLoader = otherTypeSensitiveLoader;
        this.resultProcessor = resultProcessor;
        this.loaded = false;
    }

    @PostConstruct
    public void init() {
        // 是否开启敏感词过滤
        if (!properties.isEnable()) {
            return;
        }
        // 以文件形式加载敏感词
        if (isLoadByFile()) {
            loggerPrinter.warn("敏感词文件不存在，请检查路径是否正确: {}", filePath);
            return;
        }
        // 加载敏感词
        load();
    }


    /**
     * 加载敏感词
     *
     * @author wangweijun
     * @since 2025/1/9 16:54
     */
    public synchronized void load() {
        if (loaded) {
            return;
        }
        // 加载敏感词
        List<String> sensitiveWords = loadSensitiveWords();
        // 添加到前缀树
        sensitiveWords.forEach(this::addKeyword);
        this.loaded = true;
        loggerPrinter.info("敏感词加载完成，加载方式: {}, 加载数量: {}", loadType, sensitiveWords.size());
    }

    /**
     * 重新加载敏感词
     *
     * @author wangweijun
     * @since 2025/1/9 16:57
     */
    public synchronized void reload() {
        this.loaded = false;
        this.rootNode = new TrieNode();
        load();
    }

    /**
     * 读取敏感词
     *
     * @return 敏感词集合
     *
     * @author wangweijun
     * @since 2025/1/8 17:46
     */
    private List<String> loadSensitiveWords() {
        List<String> sensitiveWords = new ArrayList<>();
        // 加载 JSON 文件（不要求后缀名，但是格式必须符合 JSON Array 标准）
        if (AgileSensitiveFilterProperties.LoadType.JSON == this.loadType) {
            try {
                ObjectMapper objectMapper = JsonUtils.MAPPER;
                sensitiveWords = objectMapper.readValue(sensitiveFile, new TypeReference<>() {
                });
            } catch (IOException e) {
                loggerPrinter.error("加载敏感词文件失败: " + e.getMessage());
            }
        }
        // 加载 TXT 文件（不要求后缀名，但是格式必须时一行一个敏感词）
        else if (AgileSensitiveFilterProperties.LoadType.TXT == this.loadType) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(sensitiveFile)))) {
                String keyword;
                while (StringUtils.isNotBlank((keyword = reader.readLine()))) {
                    sensitiveWords.add(keyword);
                }
            } catch (IOException e) {
                loggerPrinter.error("加载敏感词文件失败: " + e.getMessage());
            }
        }
        // 其他加载方式
        else {
            Objects.requireNonNull(otherTypeSensitiveLoader, "使用 OTHER 加载方式必须实现: AgileOtherTypeSensitiveLoader 接口");
            sensitiveWords = otherTypeSensitiveLoader.load();
        }
        return sensitiveWords;
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
        // 是否以文件形式加载敏感词
        if (isLoadByFile()) {
            loggerPrinter.warn("敏感词文件不存在，请检查路径是否正确");
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
     * 直接返回过滤结果
     *
     * @param text 待过滤的文本
     *
     * @return String
     *
     * @author wangweijun
     * @since 2025/1/9 09:20
     */
    public String getResult(String text) {
        SensitiveFilterResult result = this.filter(text);
        return result.getResult();
    }

    /**
     * 获取结果并抛出异常
     *
     * @param text 待过滤的文本
     *
     * @return String
     *
     * @author wangweijun
     * @since 2025/1/9 10:00
     */
    public String getResultOrElseThrow(String text) {
        return this.getResultOrElseThrow(text, result -> {
            List<String> sensitiveWords = result.getSensitiveWords().stream().map(SensitiveFilterResult.Sensitive::getKeyword).toList();
            return new SensitiveException(sensitiveWords);
        });
    }

    /**
     * 获取结果并抛出异常
     *
     * @param text     待过滤的文本
     * @param function 函数
     *
     * @return String
     *
     * @author wangweijun
     * @since 2025/1/9 10:01
     */
    public <E extends Throwable> String getResultOrElseThrow(String text, Function<SensitiveFilterResult, E> function) throws E {
        Objects.requireNonNull(function, "function is null");
        SensitiveFilterResult result = this.filter(text);
        if (!result.isPassed()) {
            throw function.apply(result);
        }
        return result.getResult();
    }

    /**
     * 获取结果并返回指定结果
     *
     * @param text         待过滤的文本
     * @param defaultValue 默认值
     *
     * @return String
     *
     * @author wangweijun
     * @since 2025/1/9 10:01
     */
    public String getResultOrElse(String text, String defaultValue) {
        return getResultOrElse(text, result -> defaultValue);
    }

    /**
     * 获取结果并返回指定结果
     *
     * @param text     待过滤的文本
     * @param function 函数
     *
     * @return String
     *
     * @author wangweijun
     * @since 2025/1/9 10:01
     */
    public String getResultOrElse(String text, Function<SensitiveFilterResult, String> function) {
        Objects.requireNonNull(function, "function is null");
        SensitiveFilterResult result = this.filter(text);
        if (!result.isPassed()) {
            return function.apply(result);
        }
        return result.getResult();
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

    /**
     * 是否以文件形式加载敏感词（非 OTHER，且文件及地址必须存在）
     *
     * @return boolean
     *
     * @author wangweijun
     * @since 2025/1/9 15:19
     */
    private boolean isLoadByFile() {
        return AgileSensitiveFilterProperties.LoadType.OTHER != this.loadType && (sensitiveFile == null || !sensitiveFile.exists());
    }
}
