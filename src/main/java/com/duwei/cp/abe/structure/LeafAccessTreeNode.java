package com.duwei.cp.abe.structure;


import com.duwei.cp.abe.attribute.Attribute;
import com.duwei.cp.abe.parameter.PublicKey;
import it.unisa.dia.gas.jpbc.Field;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @BelongsProject: JPBC-ABE
 * @BelongsPackage: com.duwei.jpbc.cp.abe.structure
 * @Author: duwei
 * @Date: 2022/7/21 17:01
 * @Description: 访问树叶节点
 */
@ToString
@Data
public class LeafAccessTreeNode extends AccessTreeNode {

    /**
     * 属性
     */
    private Attribute attribute;

    ////设置该叶子节点对应属性对应的系统公钥参数
    public LeafAccessTreeNode(String message, Field GO){
        Attribute attribute = new Attribute(message,GO);
        this.attribute = attribute;
    }

    public LeafAccessTreeNode (String attribute, PublicKey publicKey, AccessTreeNode parent, int index){
        this(attribute, publicKey.getPairingParameter().getG0());  //从公钥参数中获取生成该叶子节点对应的Element属性值
        super.setIndex(index);  //叶子节点的索引index
        super.setParent(parent);  //叶子节点的父节点
    }

    public LeafAccessTreeNode (String attribute, PublicKey publicKey , int index){
        this(attribute, publicKey.getPairingParameter().getG0());  //从公钥中获取生成该叶子节点对应的Element属性值
        super.setIndex(index);  //叶子节点的索引index
    }

    ////设置该叶子节点对应的属性
    public LeafAccessTreeNode(Attribute attribute) {
        this.attribute = attribute;
    }

    /**
     * 判断节点的属性是否属于用户属性集合
     * @return
     */
    public boolean attr(List<Attribute> attributes){
        return attributes.contains(this.attribute);
    }

    @Override
   public byte getAccessTreeNodeType() {
        return AccessTreeNodeType.LEAF_NODE;  ////节点类型为叶子节点
    }

    @Override
    public int getChildrenSize() {
        return 0;  //叶子节点的孩子节点数为0
    }

    //转化为字符串时即输出：节点类型为叶子节点；对应属性值；孩子节点个数；叶子节点索引；秘密值；父节点ID
    @Override
    public String toString(){
        return ("type : Leaf Node, attribute : " + attribute + ",   children size : " + getChildrenSize() + ",  index : " + getIndex() + "  secretNumber :  " + getSecretNumber() +  "  parentId  " + this.getParentId());
    }


}
