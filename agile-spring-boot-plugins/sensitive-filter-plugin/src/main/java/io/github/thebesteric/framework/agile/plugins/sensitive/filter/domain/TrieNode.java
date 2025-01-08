package io.github.thebesteric.framework.agile.plugins.sensitive.filter.domain;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 前缀树
 *
 * @author wangweijun
 * @version v1.0
 * @since 2025-01-08 13:28:21
 */
@Data
public class TrieNode {

    // 关键词结束标识
    private boolean isKeywordEnd = false;

    // 子节点（key 是下级字符,value 是下级节点）
    private Map<Character, TrieNode> subNodes = new HashMap<>();


    /**
     * 添加子节点
     *
     * @param character 字符
     * @param node      节点
     *
     * @author wangweijun
     * @since 2025/1/8 13:29
     */
    public void addSubNode(Character character, TrieNode node) {
        subNodes.put(character, node);
    }

    /**
     * 获取子节点
     *
     * @param character 字符
     *
     * @return TrieNode
     *
     * @author wangweijun
     * @since 2025/1/8 13:31
     */
    public TrieNode getSubNode(Character character) {
        return subNodes.get(character);
    }
}
