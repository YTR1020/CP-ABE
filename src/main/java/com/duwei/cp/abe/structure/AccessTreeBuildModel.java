package com.duwei.cp.abe.structure;

import com.duwei.cp.abe.attribute.Attribute;
import lombok.Data;

import java.util.List;

/**
 * @BelongsProject: CP-ABE
 * @BelongsPackage: com.duwei.cp.abe.structure
 * @Author: duwei
 * @Date: 2022/7/26 15:25
 * @Description: 根据模型构建访问树
 */
@Data
public class AccessTreeBuildModel {
    //访问树节点的唯一性标识
    private Integer id;
    //内部节点还是叶子节点
    private byte type;
    //阈值kx
    private int threshold;
    //索引index
    private int index;
    //属性
    private String attribute;
    //父亲ID,-1 表示没有父亲
    private Integer parentId;

    private AccessTreeBuildModel(){

    }

    public static AccessTreeBuildModel innerAccessTreeBuildModel(int id,int threshold,int index,int parentId){
        AccessTreeBuildModel buildModel = new AccessTreeBuildModel();
        buildModel.setId(id);  //内部节点的ID
        buildModel.setType(AccessTreeNodeType.INNER_NODE);  //节点类型为内部节点
        buildModel.setIndex(index);  //内部节点的索引index
        buildModel.setThreshold(threshold);  //内部节点的阈值kx
        buildModel.setParentId(parentId);  //内部节点的父节点ID
        return buildModel;
    }

    public static AccessTreeBuildModel leafAccessTreeBuildModel(int id,int index,String attributeName,int parentId){
        AccessTreeBuildModel buildModel = new AccessTreeBuildModel();
        buildModel.setId(id);  //叶子节点的ID
        buildModel.setType(AccessTreeNodeType.LEAF_NODE);  //节点类型为叶子节点
        buildModel.setIndex(index);  //叶子节点的索引index
        buildModel.setAttribute(attributeName);  //叶子节点的属性
        buildModel.setParentId(parentId);  //叶子节点的父节点ID
        return buildModel;
    }
}
