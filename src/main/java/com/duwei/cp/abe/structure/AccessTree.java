package com.duwei.cp.abe.structure;


import com.duwei.cp.abe.parameter.PublicKey;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @BelongsProject: JPBC-ABE
 * @BelongsPackage: com.duwei.jpbc.cp.abe.structure
 * @Author: duwei
 * @Date: 2022/7/21 17:07
 * @Description: 访问树结构
 */
@Data
public class AccessTree {
    private AccessTreeNode root;

    //根节点r
    private AccessTree(AccessTreeNode root) {
        this.root = root;
    }

    /**
     * 构建具体访问树节点(由公钥参数)
     *
     * @return
     */
    public static AccessTree build(PublicKey publicKey) {
        AccessTreeNode root = new InnerAccessTreeNode(2, 1, null);  //根节点即序号1的内部节点阈值kx为2，无父节点
        List<AccessTreeNode> children = new ArrayList<>();  //孩子树序列
        children.add(new LeafAccessTreeNode("学生", publicKey, root, 1));  //根节点(父节点)下，序号1的孩子节点，属性为“学生”，参数从公钥中获取
        children.add(new LeafAccessTreeNode("老师", publicKey, root, 2));  //根节点(父节点)下，序号2的孩子节点，属性为“老师”，参数从公钥中获取
        children.add(new LeafAccessTreeNode("硕士", publicKey, root, 3));  //根节点(父节点)下，序号3的孩子节点，属性为“硕士”，参数从公钥中获取
        root.setChildren(children);
        return new AccessTree(root);
    }


    /**
     * 构建基本访问树节点(由公钥参数、构造访问树模型)
     *
     * @return
     */
    public static AccessTree build(PublicKey publicKey, AccessTreeBuildModel[] accessTreeBuildModels) {
        Map<Integer, AccessTreeNode> idNodeMap = new HashMap<>();  //新建节点ID-节点映射的数据字典
        //遍历创建访问控制树的每个节点
        for (int i = 0; i < accessTreeBuildModels.length; i++) {
            AccessTreeBuildModel model = accessTreeBuildModels[i];
            AccessTreeNode node = null;
            if (model.getType() == AccessTreeNodeType.INNER_NODE) {
                //内部节点，设置对应阈值kx、索引index
                node = new InnerAccessTreeNode(model.getThreshold(), model.getIndex());
            } else if (model.getType() == AccessTreeNodeType.LEAF_NODE) {
                //叶子节点，设置对应属性、公钥参数、索引index
                node = new LeafAccessTreeNode(model.getAttribute(), publicKey, model.getIndex());
            }
            node.setParentId(model.getParentId());  //设置父节点ID
            idNodeMap.put(model.getId(), node);  //设置该节点的ID
        }
        //父亲节点ID  -  集合
        Map<Integer, List<AccessTreeNode>> collect = idNodeMap.values().stream().collect(Collectors.groupingBy(node -> node.getParentId()));
        idNodeMap.forEach((id, node) -> {
            List<AccessTreeNode> accessTreeNodes = collect.get(id);
            if (accessTreeNodes != null) {
                node.setChildren(accessTreeNodes);
                accessTreeNodes.forEach((child) -> child.setParent(node));
            }
        });

        //根节点元素ID必须为1
        return new AccessTree(idNodeMap.get(1));
    }
}
