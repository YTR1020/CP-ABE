package com.duwei.cp.abe.structure;

import lombok.Data;



/**
 * @BelongsProject: JPBC-ABE
 * @BelongsPackage: com.duwei.jpbc.cp.abe.structure
 * @Author: duwei
 * @Date: 2022/7/21 17:04
 * @Description: 访问树内部节点
 */
@Data
public class InnerAccessTreeNode extends AccessTreeNode {
    /**
     * 阈值kx
     */
    private int threshold;

    public InnerAccessTreeNode(int threshold,int index,AccessTreeNode parent){
        this.threshold = threshold;  //内部节点的阈值kx
        super.setIndex(index);  //内部节点的索引index
        super.setParent(parent);  //内部节点的父节点
    }

    public InnerAccessTreeNode(int threshold,int index){
        this.threshold = threshold;  //内部节点的阈值kx
        super.setIndex(index);  //内部节点的索引index
    }

    @Override
    public byte getAccessTreeNodeType() {
        return AccessTreeNodeType.INNER_NODE;  //节点类型为内部节点
    }


    //转化为字符串时即输出：节点类型为内部节点；对应阈值大小；孩子节点个数；秘密值；父节点ID
    @Override
    public String toString(){
        return ("type : inner Node, threshold : " + threshold + ",   children size : " + getChildrenSize()  + "  secretNumber :  " + getSecretNumber() + "  parentId  " + this.getParentId());
    }

}
